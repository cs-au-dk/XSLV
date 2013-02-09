package dongfang.xsltools.controlflow;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.RootNT;
import dongfang.xsltools.xmlclass.xslside.UndeclaredNodeType;

public abstract class AbstractContextSensitiveAnalyzer implements FlowAnalyzer {

  /*
   * If true, a simple and memory hungry test will check that the same (source
   * template, context type, target template) triple is never tested a 2nd time.
   */
  protected static final boolean DEBUG = false;

  private static final int TICK = DiagnosticsConfiguration.current
      .traceControlFlowTickCount();

  /*
   * If inflooping suspected, turn to true and watch the console.
   */
  protected static final boolean CANONICALIZE_CONTEXT_SETS = ControlFlowConfiguration.current
      .canonicalizeContextSets();

  protected Set<String> sanityCheck = DEBUG ? new HashSet<String>() : null;

  public void analyze(/*List<TemplateRule> liveRules,*/
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode bootstrapMode) throws XSLToolsException {

    boolean makeProgressTicks = DiagnosticsConfiguration.current
        .traceControlFlow();

    Map<String, Automaton> globalAlphaMap = new HashMap<String, Automaton>();

    /*
     * All this is for the progress ticks only.
     */
    SingleTypeXMLClass inputType = validationContext
        .getInputType(validationContext.getSchemaIdentifier("", ResolutionContext.INPUT));

    inputType.clearSearchCache();

    int contextTypeCount = inputType.getTotalTypeCount();
    long theUltimateLimit = contextTypeCount * xcfg.templateRules.size()
        * xcfg.templateRules.size();
    long harvest = 0;
    long lastHarvest = 0;
    // double progress = 0;

    Map<String, Map<DeclaredNodeType, Set<DeclaredNodeType>>> skunk = 
      new HashMap<String, Map<DeclaredNodeType, Set<DeclaredNodeType>>>();

    LinkedList<NewContextFlow> newContextNodes = new LinkedList<NewContextFlow>();

    // if (ControlFlowConfiguration.current.usePrefilterCache()) {
    for (TemplateRule r : xcfg.templateRules) {
      r.initDefinitelyAcceptableTypes(inputType);
      // }
    }

    // Add dummy new context node in the dummy root template rule:
    newContextNodes.add(new NewContextFlow(RootNT.instance,
        bootstrapTemplateRule, bootstrapMode));

    bootstrapTemplateRule.addContextType(bootstrapMode, RootNT.instance, null);
    Set<? extends UndeclaredNodeType> s = Collections
        .singleton(RootNT.instance);

    bootstrapTemplateRule.addSchemalessContextType(s);

    LinkedList<NewContextFlow> newNewContexts = new LinkedList<NewContextFlow>();

    // Iterate until no new context nodes.
    while (!newContextNodes.isEmpty()) {
      NewContextFlow newContext = newContextNodes.removeFirst();

      // Note: The node is already added to the rule's context set at this
      // point. Sanity check:
      // assert
      // (newContext.target.getContextSet().contains(newContext.contextNode));

      // Perform edge analysis for this context node:

      edgeAnalysis(validationContext, newContext, xcfg.templateRules, skunk,
          globalAlphaMap, newNewContexts);

      if (makeProgressTicks) {
        harvest += newContextNodes.size();

        double newProgress = (double) harvest / (double) theUltimateLimit;

        // long tickBelow = (long) (progress * 500);
        // long tickAbove = (long) (newProgress * 500);

        // if (tickAbove != tickBelow) {
        if (harvest >= lastHarvest + TICK) {
          System.out.println(((int) (newProgress * TICK)) / 10.0
              + " % of #templates^2 * #types");
          System.out.println(harvest + " new flows so far...");
          lastHarvest = harvest - harvest % TICK;
        }
        // progress = newProgress;
      }

      // Add produced flow to the list of context nodes:
      newContextNodes.addAll(newNewContexts);
      newNewContexts.clear();
    }
  }

  /**
   * For a new TemplateRule * DeclaredNodeType combo, examines which candidate
   * edges are worthwhile considering.
   * 
   * @param inputType
   * @param newContext
   * @param templateRules
   * @param result
   * @throws XSLToolsException
   */
  protected void edgeAnalysis(
      ValidationContext context,
      NewContextFlow newContext,
      List<? extends TemplateRule> templateRules,
      Map<String, Map<DeclaredNodeType, Set<DeclaredNodeType>>> skunk,
      Map<String, Automaton> globalAlphaMap, List<? super NewContextFlow> result)
      throws XSLToolsException {

    SingleTypeXMLClass inputType = context.getInputType(context
        .getSchemaIdentifier("", ResolutionContext.INPUT));

    PerformanceLogger pa = DiagnosticsConfiguration.current
        .getPerformanceLogger();

    DeclaredNodeType contextType = newContext.contextType;
    ContextMode contextMode = newContext.contextMode;
    TemplateRule source = newContext.target;

    // Find candidate edges:
    LinkedList<CandidateEdge> edgeWorkList = new LinkedList<CandidateEdge>();

    // For each instruction in target rule:

    for (ApplyTemplatesInst inst : source.applies) {
      int selec = 0;
      for (Selection sele : inst.selections) {
        pa.startTimer("CandidateSelection", getClass().getSimpleName());
        locateCandidateEdges(sele, contextType, inputType, edgeWorkList, skunk,
            inst, contextMode, selec++);
        pa.stopTimer("CandidateSelection", getClass().getSimpleName());

        pa.startTimer("CandidateAnalysis", getClass().getSimpleName());
        if (!edgeWorkList.isEmpty()) {
          analyzeCandidateEdges(context, source, edgeWorkList, result,
              globalAlphaMap, pa);
          edgeWorkList.clear();
        }
        pa.stopTimer("CandidateAnalysis", getClass().getSimpleName());
      }
    }
  }

  protected abstract void analyzeCandidateEdges(ValidationContext context,
      TemplateRule source, LinkedList<CandidateEdge> candidateEdges,
      List<? super NewContextFlow> result,
      Map<String, Automaton> globalAlphaMap, PerformanceLogger pa)
      throws XSLToolsException;

  protected abstract void locateCandidateEdges(
      Selection sele,
      DeclaredNodeType contextType,
      SingleTypeXMLClass inputType,
      LinkedList<CandidateEdge> work,
      Map<String, Map<DeclaredNodeType, Set<DeclaredNodeType>>> _skunk,
      ApplyTemplatesInst apply, ContextMode mode, int selIndex)
      throws XSLToolsException;
}
