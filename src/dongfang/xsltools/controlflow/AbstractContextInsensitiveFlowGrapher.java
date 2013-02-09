/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-19
 */
package dongfang.xsltools.controlflow;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.xmlclass.xslside.NodeType;
import dongfang.xsltools.xmlclass.xslside.UndeclaredNodeType;
import dongfang.xsltools.xpath2.XPathPathExpr;

/**
 * @author dongfang
 */
public abstract class AbstractContextInsensitiveFlowGrapher implements
    FlowAnalyzer {

  abstract Set<UndeclaredNodeType> flowSurvivors(XPathPathExpr path1,
      TemplateRule target, ValidationContext context) throws XSLToolsException;

  abstract int priorityOverrideTest(TemplateRule e1Target,
      Set<? extends NodeType> e1EdgeFlow, TemplateRule e2Target,
      Set<? extends NodeType> e2EdgeFlow, ValidationContext context)
      throws XSLToolsException;

  /**
   * Fixed point iteration over a list of template rules: For each rule in the
   * list, additivelyAnalyzeEdge is called, finding new (and old) flows (of just
   * control) from the current rule to any other rule. The flow graph will GROW
   * towards fixed point.
   * 
   * @param templates
   * @param validationContext
   * @throws XSLToolsXPathException
   */
  public void analyze(/*List<TemplateRule> liveRules,*/
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode bootstrapMode) throws XSLToolsException {

    /*
     * This list is consumed from one end and appended to at the other.
     */
    LinkedList<NewFlow> newRulesReached = new LinkedList<NewFlow>();

    // Add dummy new context node in the dummy root template rule:
    newRulesReached.add(new SourcedNewFlow(bootstrapTemplateRule,
        bootstrapMode, bootstrapTemplateRule.applies.get(0),
        bootstrapTemplateRule.applies.get(0).selections.get(0)));

    LinkedList<NewFlow> newNewRulesReached = new LinkedList<NewFlow>();

    // Iterate until no new context nodes.
    while (!newRulesReached.isEmpty()) {
      NewFlow newFlow = newRulesReached.removeFirst();
      TemplateRule newRule = newFlow.target;
      ContextMode newMode = newFlow.contextMode;
      // TODO: Replace this by a boolean add thingy on TemplateRule.
      // Perform edge analysis for this context node:
      analyzeEdge(newRule, newMode, newNewRulesReached, validationContext);

      // Add produced flow to the list of context nodes:
      newRulesReached.addAll(newNewRulesReached);
      newNewRulesReached.clear();
    }
  }

  /**
   * Context insensitive edge analysis on a single TemplateRule, which means
   * calling locateCandidateEdges on each instruction in the template rule (only
   * does stuff (adding candidate edges to work list) if ApplyTemplateInst). For
   * addition of the Dong and Bailey schema-less approach, that should probably
   * be separated out into a separate step.
   * 
   * @param ruleReached
   * @return
   * @throws XSLToolsException
   */
  void analyzeEdge(TemplateRule current, ContextMode mode,
      List<NewFlow> result, ValidationContext context) throws XSLToolsException {
    // Find candidate edges:

    LinkedList<SourcedNewFlow> edgeWorkList = new LinkedList<SourcedNewFlow>();

    // Fill 'er up: For each instruction in rule:
    // Will cause every same-mode target of every apply-templates
    // instruction
    // to be added (as a CandidateEdge)
    for (ApplyTemplatesInst inst : current.applies) {
      inst.locateCandidateEdges(mode, edgeWorkList);
    }

    // Determine edge flow for each edge:
    // Improve precision of above and seperate away below (is there any
    // harm done in applying below step for ALL edges at the same time,
    // not for each template rule but for the whole thing at the same
    // time??)
    analyzeCandidateEdges(edgeWorkList, result, context);

    for (ListIterator<NewFlow> it = result.listIterator(); it.hasNext();) {
      NewFlow flow = it.next();
      if (!flow.target.addMode(flow.contextMode))
        it.remove();
    }
  }

  /**
   * Analyzes all CandidateEdges derived from a single rule. Could this be
   * modified to analyze all candidate edges from a wholw stylesheet in one go?
   * 
   * @param candidateEdges
   * @return
   * @throws XSLToolsException
   */
  void analyzeCandidateEdges(LinkedList<SourcedNewFlow> candidateEdges,
      List<? super NewFlow> result, ValidationContext context)
      throws XSLToolsException {
    // Break if no candidate edges in list:
    if (candidateEdges.isEmpty())
      return;

    PerformanceLogger pa = DiagnosticsConfiguration.current
        .getPerformanceLogger();

    SourcedNewFlow someFlow = candidateEdges.getFirst();
    // Fetch common info for the edges:
    // oops that problem for the whole stylesheet at a time computation
    // .. but
    // one could keep this updated (move it down).
    ContextMode mode = someFlow.contextMode;

    // Determine if each edge could possibly be reached
    // given any context node:
    // Iterate work list:
    while (!candidateEdges.isEmpty()) {
      // Stats:
      pa.incrementCounter("InsensitiveEdgesConsidered_", getClass()
          .getSimpleName());

      // Remove from work list:
      SourcedNewFlow flow = candidateEdges.removeFirst();
      // Sanity checks:

      Selection selection = flow.sourceSelect;

      // Does the edge already exist?
      // in the future, this is not really an error condition.
      /*
       * if (selection.hasContextInsensitiveTarget(edge.target)) { throw new
       * AssertionError("Edge already exists!"); } else {
       */
      /*
       * Analyze edge: Create concatenated selection path and fetch match path:
       * Is the concatenation of the match template rule of the select, with the
       * select (aaah, that makes a good argument for splitting the match
       * expressions!)
       */

      XPathPathExpr path1 = selection.getInsensitiveMatchSelectPath();

      // XPathPathExpr path1 = selection.originalPath;

      Set<UndeclaredNodeType> simulatedEdgeFlow = flowSurvivors(path1,
          flow.target, context);
      // Add or discard edge:
      if (simulatedEdgeFlow.isEmpty()) {
        pa.incrementCounter("InsensitiveEdgesKilled_", getClass()
            .getSimpleName());
      } else {
        // The edge passed the flow propagation tests. It generates flow!
        TemplateRule newTarget = flow.target;

        int newOverridden = TemplateRule.NO_OVERRIDE;

        /*
         * Examine already exsiting flows...
         */
        Iterator<TemplateRule> edgeIter = selection
            .getContextInsensitiveTargetSet(mode).iterator();

        TemplateRule existingTarget = null;

        // Is our new edge being overridden?
        while (edgeIter.hasNext() && newOverridden == 0) {
          existingTarget = edgeIter.next();
          Set<UndeclaredNodeType> existingEdgeFlow = selection
              .getContextInsensitiveEdgeFlow(mode, existingTarget);

          // Override flow:
          newOverridden = priorityOverrideTest(existingTarget,
              existingEdgeFlow, newTarget, simulatedEdgeFlow, context);
        }

        if (newOverridden != TemplateRule.NO_OVERRIDE) {
          // New edge is overridden by existing. Dump the edge.
          // death of flow implemented by forgetting about it...
          flow.sourceApply.reportDeath(new OverriddenContextFlow(
              existingTarget, newTarget, newOverridden, getClass()
                  .getSimpleName()
                  + ", old-kills-new"));
          pa.incrementCounter("OldEdgeOverridesNew_", getClass()
              .getSimpleName());
        } else {
          // Test override of existing flow:
          edgeIter = selection.getContextInsensitiveTargetSet(mode).iterator();

          while (edgeIter.hasNext()) {
            existingTarget = edgeIter.next();
            Set<UndeclaredNodeType> existingEdgeFlow = selection
                .getContextInsensitiveEdgeFlow(mode, existingTarget);

            // Override flow:
            int oldEdgeOverridden = priorityOverrideTest(newTarget,
                simulatedEdgeFlow, existingTarget, existingEdgeFlow, context);

            if (oldEdgeOverridden != TemplateRule.NO_OVERRIDE) {
              // Existing edge is overridden by the new one. Remove the
              // edge.
              /*
               * CandidateEdge oldEdge = new CandidateEdge(edge.sourceApply,
               * edge.pathIndex, edge.sourceSelect, edge.contextNode,
               * existingTarget);
               */
              if (ControlFlowConfiguration.current.generateFlowDeathReport()) {
                flow.sourceApply.reportDeath(new OverriddenContextFlow(
                    flow.target, existingTarget, oldEdgeOverridden, getClass()
                        .getSimpleName()
                        + ", new-kills-old"));
              }
              pa.incrementCounter("NewEdgeOverridesOld_", getClass()
                  .getSimpleName());
              // Remove:
              edgeIter.remove();
            }
          }
        }

        if (newOverridden == TemplateRule.NO_OVERRIDE) {
          // Add edge and its flow to graph:
          
          selection.putContextInsensitiveEdgeFlow(mode, flow.target,
              simulatedEdgeFlow);
          flow.rollMode();
          flow.target.addSchemalessContextType(simulatedEdgeFlow);
          result.add(flow);
          pa.incrementCounter("InsensitiveEdgesSurvived_", getClass()
              .getSimpleName());
          // Add to statistics:
        } // if (!newOverridden)
      }
    }

    // Construct new rules reached set (new -- each template rule
    // only runs through this mill once per new mode)
    // For each instruction:
  }
}
