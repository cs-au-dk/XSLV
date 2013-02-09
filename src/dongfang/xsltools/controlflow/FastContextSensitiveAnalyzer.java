package dongfang.xsltools.controlflow;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import dk.brics.automaton.Automaton;
import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsUnhandledNodeTestException;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.schemaside.dropoff.DynamicRedeclaration;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathPathExpr;

/**
 * A rather precise test that makes no use of ancestor path automata. Fast like
 * @$#$% !! ZOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOM!
 * @author dongfang
 */
public class FastContextSensitiveAnalyzer extends
    AbstractContextSensitiveAnalyzer {

  private static FastContextSensitiveAnalyzer instance = new FastContextSensitiveAnalyzer();

  public static FastContextSensitiveAnalyzer getInstance() {
    return instance;
  }

  @Override
  public void analyze(
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode bootstrapMode) throws XSLToolsException {
    if (ControlFlowConfiguration.current.candidateFilterAlgorithm() != ControlFlowConfiguration.ControlFlowAlgorithm.ABSTRACT_EVALUATION) {
      throw new AssertionError(
          "This will only work iff Selection is configured to use abstract evaluation (\"mill\") test.");
    }
    super.analyze(xcfg, validationContext, bootstrapTemplateRule,
        bootstrapMode);
  }

  @Override
  protected void analyzeCandidateEdges(ValidationContext context,
      TemplateRule source, LinkedList<CandidateEdge> candidateEdges,
      List<? super NewContextFlow> result,
      Map<String, Automaton> globalAplhaMap, PerformanceLogger pa)
      throws XSLToolsException {

    CandidateEdge someEdge = candidateEdges.getFirst();

    // These should be the same for all edges, so we just pull'em from the first
    // one.
    ContextFlow kludder = someEdge.getContextFlow();

    DeclaredNodeType contextType = someEdge.contextType;

    for (CandidateEdge ce : candidateEdges) {

      Set<DeclaredNodeType> flow = ce.maybeSurviveableTypes;
        Set<DeclaredNodeType> aav = kludder == null ? null : kludder
          .get(ce.target);
      if (aav != null) {
        aav.addAll(flow);
        flow = aav;
      }

      /*
       * In the comp. analysis, we rely on shooting down flows from the
       * Selections by reference!!! Therefore, strict aliasing betw. the context
       * flows here and in the selections.
       */
      ce.sourceSelect.putContextFlow(ce.contextMode, contextType, ce.target,
          flow);

      pa.incrementCounter("AnalyzedCandidateEdges", "SensitiveFlowGrapher");

      if (flow.isEmpty()) {
        pa.incrementCounter("CandidateEdgesWithoutFlow",
            "AnalyzedCandidateEdges");
        continue;
      }
    }

    kludder = someEdge.getContextFlow();

    pa.startTimer("CompetitionAnalysis", "CandidateAnalysis");
    if (candidateEdges.size() > 1)
      examineFlowCompetition(context, source, contextType, candidateEdges,
          kludder, pa);
    pa.stopTimer("CompetitionAnalysis", "CandidateAnalysis");

    ContextMode contextMode = someEdge.sourceApply.getMode().contextualize(
        someEdge.contextMode);

    for (Entry<TemplateRule, Set<DeclaredNodeType>> e : kludder.entrySet()) {
      Set<DeclaredNodeType> newContextNodes = e.getValue();
      if (!newContextNodes.isEmpty()) {
        pa.incrementCounter("MadeNewSensitiveEdges", "AnalyzedCandidateEdges");
      } else {
        pa
            .incrementCounter("PinchedOffByCompetition",
                "AnalyzedCandidateEdges");
      }

      TemplateRule rule = e.getKey();

      for (DeclaredNodeType nt : newContextNodes) {

        if (rule.addContextType(contextMode, nt, someEdge.sourceApply)) {
          if (!rule.mode.accepts(contextMode)) {
            System.err.println("Griseri !!!, rule " + rule
                + " did not accept mode " + contextMode + " rolled from "
                + someEdge.contextMode + " fired from " + someEdge.sourceApply);
            System.err.println("src template index: "
                + someEdge.sourceApply.containingRule.index);
            System.err.println("tgt template index: " + rule.index);
            System.err.println();
          }

          result.add(new NewContextFlow(nt, rule, contextMode));

          if (DEBUG) {
            String sanityChecker = source.index + "--" + nt.toString() + "-->"
                + rule.index;
            if (this.sanityCheck.contains(sanityChecker))
              System.err
                  .println("This is the 2nd or more time we find the flow "
                      + sanityChecker);
            else
              sanityCheck.add(sanityChecker);
          }
        }
      }
    }
    candidateEdges.clear();
  }

  @Override
  protected void locateCandidateEdges(
      Selection sele,
      DeclaredNodeType contextType,
      SingleTypeXMLClass inputType,
      LinkedList<CandidateEdge> work,
      Map<String, Map<DeclaredNodeType, Set<DeclaredNodeType>>> _skunk,
      ApplyTemplatesInst apply, ContextMode mode, int selIndex)
      throws XSLToolsException {
    sele.locateCandidateEdgesForMills(contextType, inputType, work, _skunk,
        apply, mode, selIndex);
  }

 private boolean alwaysPassesPredicate(DeclaredNodeType candidate, XPathPathExpr expr, SingleTypeXMLClass clazz)
 throws XSLToolsSchemaException {
	 return DynamicRedeclaration.alwaysPassesPredicate(candidate, expr, clazz);
 }
  
  /**
   * Eliminate those of the new context flows that can be shown not to exist
   * because they are overridden by suction from higher precedence/priority
   * templates.
   * 
   * @param inputType
   * @param source
   * @param contextType
   * @param candidateEdges
   * @param newFlow
   * @param pa
   * @throws XSLToolsSchemaException
   * @throws XSLToolsUnhandledNodeTestException
   */
  private void examineFlowCompetition(ValidationContext context,
      TemplateRule source, DeclaredNodeType contextType,
      List<CandidateEdge> candidateEdges, ContextFlow newFlow,
      PerformanceLogger pa) throws XSLToolsException {

    SingleTypeXMLClass inputType = context.getInputType(context
        .getSchemaIdentifier("", ResolutionContext.INPUT));

    CandidateEdge someEdge = candidateEdges.get(0);

    XPathExpr MCS = someEdge.sourceSelect.makeAlphaNodeSetExpForMills(
        contextType, inputType);

    for (CandidateEdge x : candidateEdges) {
      TemplateRule xtarget = x.target;
      int deathCause;

      pa.startTimer("OverrideTest", "CompetitionAnalysis");

      for (CandidateEdge y : candidateEdges) {

        if (x.target == y.target)
          continue;

        TemplateRule ytarget = y.target;
        
        if ((deathCause = ytarget.compareTo(xtarget)) >= 0
            && (!xtarget.match.hasPredicates()
             || alwaysPassesPredicate(contextType, xtarget.match, inputType))) {

          Set<DeclaredNodeType> challengedFlows = new HashSet<DeclaredNodeType>(
              newFlow.get(ytarget));

          // no common flow, then no override.
          challengedFlows.retainAll(newFlow.get(xtarget));

          if (challengedFlows.isEmpty())
            continue;

          for (DeclaredNodeType raprap : challengedFlows) {

            Set<DeclaredNodeType> typeSet = Collections.singleton(raprap);

            boolean matchMatchResult = inputType.allPossibleFlowsCovered(
                xtarget.match, ytarget.match, typeSet, false, contextType);

            boolean matchSelectResult = false;

            if (!matchMatchResult && MCS instanceof XPathPathExpr) {
              matchSelectResult = inputType.allPossibleFlowsCovered(
                  (XPathPathExpr) MCS, ytarget.match, typeSet, true, contextType);
            }

            if (matchMatchResult || matchSelectResult) {
              if (matchMatchResult && deathCause == 0) {
                String message = "There is a possible priority conflict between ";
                message += xtarget.toLabelString();
                message += " and ";
                message += ytarget.toLabelString();
                message += " @ context type " + raprap.toLabelString();
                context.pushMessage("flow-competition", message);

                if (!xtarget.getId(StylesheetModule.ORIGINAL).
                    equals(ytarget.getId(StylesheetModule.ORIGINAL))) {
                  pa.incrementCounter("RealCompetitionWarnings", "Death");
                } else {
                  pa.incrementCounter("FalseCompetitionWarnings", "Death");
                }
              }
              if (deathCause != 0) {
                if (!newFlow.get(xtarget).remove(raprap)) {
                  throw new AssertionError(
                      "Flow was not there in the first place??");
                }

                DeadContextFlow cod = new OverriddenContextFlow(ytarget,
                    xtarget, deathCause, getClass().getSimpleName());

                cod.addLostNodeType(raprap);

                x.sourceApply.reportDeath(cod);

                if (matchMatchResult)
                  pa.incrementCounter("FastTestOverrideKills",
                      "SensitiveCompetitionAnalysis");

                else if (matchSelectResult)
                  pa.incrementCounter("ExhaustiveTestOverrideKills",
                      "SensitiveCompetitionAnalysis");
              }
            }
          }
        }
      }
      pa.stopTimer("OverrideTest", "CompetitionAnalysis");
    }
  }
}
