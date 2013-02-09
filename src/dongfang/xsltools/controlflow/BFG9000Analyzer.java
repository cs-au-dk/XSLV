package dongfang.xsltools.controlflow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.PINT;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathPathExpr;

public class BFG9000Analyzer extends AbstractContextSensitiveAnalyzer {

  private static BFG9000Analyzer instance = new BFG9000Analyzer();

  /*
   * Set to false if bug in the reverse step-to-step abstract XPath eval thing
   * is suspected. This is going to hurt performance-wise, though.
   */
  private static final boolean BOLDLY_TRUST_SURVIVAL_PREDICTIONS = true;

  public static BFG9000Analyzer getInstance() {
    return instance;
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
    sele.locateCandidateEdgesForAutomata(contextType, inputType, work, _skunk,
        apply, mode, selIndex);
  }

  @Override
protected void analyzeCandidateEdges(ValidationContext context,
      TemplateRule source, LinkedList<CandidateEdge> candidateEdges,
      List<? super NewContextFlow> result,
      Map<String, Automaton> globalAlphaMap, PerformanceLogger pa)

  throws XSLToolsException {

    SingleTypeXMLClass inputType = context.getInputType(context
        .getSchemaIdentifier("", ResolutionContext.INPUT));

    CandidateEdge someEdge = candidateEdges.getFirst();

    ContextFlow kludder = someEdge.getContextFlow();
    ContextMode contextMode = someEdge.contextMode;

    // These should be the same for all edges, so we just pull'em from the first
    // one.
    DeclaredNodeType contextType = someEdge.contextType;
    contextMode = someEdge.sourceApply.getMode().contextualize(contextMode);

    /*
     * Set<DeclaredNodeType> sa1 = new HashSet<DeclaredNodeType>(); Set<Selection>
     * sa2 = new HashSet<Selection>();
     */

    for (Iterator<CandidateEdge> cei = candidateEdges.iterator(); cei.hasNext();) {
      CandidateEdge ce = cei.next();
      /*
       * sa1.add(ce.contextType); sa2.add(ce.sourceSelect);
       * 
       * if (sa1.size() != 1) { throw new AssertionError("More than one context
       * node: " + sa1); }
       * 
       * if (sa2.size() != 1) { throw new AssertionError("More than one context
       * node: " + sa2); }
       */
      boolean flowWasPredictedSurvivors = true;
      Set<DeclaredNodeType> flow = null;

      if (BOLDLY_TRUST_SURVIVAL_PREDICTIONS)
        flow = null; // ce.definitelySurviveableTypes;

      if (flow == null) {
        flow = ce.sourceSelect.getContextFlow(ce, inputType, globalAlphaMap);
        flowWasPredictedSurvivors = false;

        // temp exp: See what was dumped.
        Set<DeclaredNodeType> death = ce.maybeSurviveableTypes;
        death.removeAll(flow);
        for (DeclaredNodeType t : death) {
          pa.incrementCounter("AutomatonKill", "SensitiveFlowGrapher");
        }
      }

      /*
       * Some sanity tests
       */
      if (DEBUG) {
        if (flow.isEmpty() && ce.definitelySurviveableTypes != null) {
          if (!(flow.equals(ce.definitelySurviveableTypes)))
            System.err
                .println("Assertion screwup! Edge was predicted to survive, but did not. Target match exp is "
                    + ce.target.match
                    + " MCS of source is "
                    + ce.getAlphaExpressionForMills(inputType));
        }

        if (!flow.isEmpty() && ce.wasPredictedToDie) {
          System.err
              .println("Assertion screwup! Edge was predicted to die, but actually carried flow. Target match exp is "
                  + ce.target.match
                  + " MCS of source is "
                  + ce.getAlphaExpressionForMills(inputType));
        }
      }
      /*
       * End of some sanity tests
       */

      /*
       * Some killer effeciency stats (reverse mill test)
       */
      if (flowWasPredictedSurvivors) {
        pa.incrementCounter("SurvivalTestPredictedSurvival",
            "SensitiveFlowGrapher");
      } else {

        Set<? extends DeclaredNodeType> ref = ce.maybeSurviveableTypes;
        if (ref == null)
          ref = Collections.emptySet();

        for (DeclaredNodeType t : ref) {
          if (flow.contains(t))
            pa.incrementCounter("Bluestamping", "SensitiveFlowGrapher");
          else
            pa.incrementCounter("SquarelyKilled", "SensitiveFlowGrapher");
        }

        for (DeclaredNodeType t : flow) {
          if (ref.contains(t))
            ;
          else
            pa.incrementCounter("MaybeSurvivorFailure", "SensitiveFlowGrapher");
        }

        if (flow.isEmpty()) {
          // pa.incrementCounter("SurvivalCorrectlyPredictedNoSurvival",
          // "SensitiveFlowGrapher");
        } else {

          // if (ce.definitelySurviveableTypes != null) {
          // pa
          // .incrementCounter("SurvivalTestScrewedUp",
          // "SensitiveFlowGrapher");
          // } else {
          // pa.incrementCounter("SurvivalTestMissed", "SensitiveFlowGrapher");
          // }
        }
      }

      /*
       * Store each candidate edge's flow in the context flow of the whold
       * selection
       */
      Set<DeclaredNodeType> aav = kludder == null ? null : kludder
          .get(ce.target);
      if (aav != null) {
        aav.addAll(flow);
        flow = aav;
      }
      // kludder.put(ce.target, flow);

      /*
       * In the comp. analysis, we rely on shooting down flows from the
       * Selections by reference!!! Therefore, strict aliasing betw. the context
       * flows here and in the selections.
       */
      ce.sourceSelect.putContextFlow(ce.contextMode, contextType, ce.target,
          flow);
    }

    kludder = someEdge.getContextFlow();

    pa.startTimer("CompetitionAnalysis", "CandidateAnalysis");
    if (candidateEdges.size() > 1)
      examineFlowCompetition(context, source, contextType, candidateEdges,
          kludder, globalAlphaMap, pa);
    pa.stopTimer("CompetitionAnalysis", "CandidateAnalysis");

    /*
     * Flow set merge: Canonicalize identical sets. Not yet tested...
     */

    /*
     * Seems to work no problems... and fast enough, too. Can still switch it
     * off, no probs either.
     */

    if (CANONICALIZE_CONTEXT_SETS) {
      // someEdge.canonicalizeFlows();
    }

    for (Entry<TemplateRule, Set<DeclaredNodeType>> e : kludder.entrySet()) {
      Set<DeclaredNodeType> newContextNodes = e.getValue();
      TemplateRule rule = e.getKey();
      for (DeclaredNodeType nt : newContextNodes) {
        if (rule.addContextType(contextMode, nt, someEdge.sourceApply)) {
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
      Map<String, Automaton> globalAlphaMap, PerformanceLogger pa)
      throws XSLToolsException {

    SingleTypeXMLClass inputType = context.getInputType(context
        .getSchemaIdentifier("", ResolutionContext.INPUT));

    boolean useFastTests = false; // ControlFlowConfiguration.current.candidateFilterAlgorithm()
    // == ControlFlowConfiguration.ControlFlowAlgorithm.ABSTRACT_EVALUATION;

    CandidateEdge someEdge = candidateEdges.get(0);

    XPathExpr MCS;
    if (useFastTests)
      MCS = someEdge.getAlphaExpressionForMills(inputType);
    else
      MCS = null;

    for (CandidateEdge x : candidateEdges) {
      TemplateRule xtarget = x.target;
      int deathCause;
      pa.startTimer("OverrideTest", "CompetitionAnalysis");

      Automaton plhs = null;

      for (CandidateEdge y : candidateEdges) {
        if (x.target == y.target)
          continue;
        TemplateRule ytarget = y.target;
        if ((deathCause = ytarget.compareTo(xtarget)) >= 0
            && !xtarget.match.hasPredicates()) {

          /*
           * competitionTest(inputType, MCS, plhs, xtarget,
           * newFlow.get(xtarget), ytarget, newFlow.get(ytarget), deathCause,
           * pa);
           */

          Set<DeclaredNodeType> challengedFlows = new HashSet<DeclaredNodeType>(
              newFlow.get(ytarget));

          // no common flow, then no override.
          challengedFlows.retainAll(newFlow.get(xtarget));
          if (challengedFlows.isEmpty())
            continue;

          if (challengedFlows.size() == 1
              && challengedFlows.contains(PINT.chameleonInstance)) {
            Iterator<DeclaredNodeType> it = challengedFlows.iterator();
            DeclaredNodeType maybe_chameleon = it.next();

            // we don't listen to priority complaints about
            // the cham PINT.
            if (maybe_chameleon == PINT.chameleonInstance)
              continue;
          }

          Automaton cmc = null;

          for (DeclaredNodeType raprap : challengedFlows) {

            // We have a definite override iff one or more of these is true
            boolean automatonTestResult = false;
            boolean yexhaustiveTestResult = false;
            boolean fastTestResult = false;

            // See if there are any types shared among flows
            Set<DeclaredNodeType> typeSet = Collections.singleton(raprap);

            if (useFastTests)
              // First try on fasts: Plain defeat test
              fastTestResult = inputType.allPossibleFlowsCovered(xtarget.match,
                  ytarget.match, typeSet, false, contextType);

            // If first try failed to conclude anything, see if flow is
            // exhaustive
            if (useFastTests && !fastTestResult && MCS instanceof XPathPathExpr) {
              yexhaustiveTestResult = inputType.allPossibleFlowsCovered(
                  (XPathPathExpr) MCS, ytarget.match, typeSet, true, contextType);
            }

            // If debugging, or the fast tests failed to conclude anything
            // (including the casw where they did, because they were never run),
            // throw in the heavy (automaton) equipment
            if (DEBUG || (!fastTestResult && !yexhaustiveTestResult)) {
              if (plhs == null) {
                // delay constructing the automaton till everything else failed.
                Automaton MCSA = x.sourceSelect
                    .getAlphaIntersectInputAutomaton(contextType, inputType,
                        globalAlphaMap);
                Automaton oldMatch = xtarget.lazyMakeMatchAutomaton(inputType);
                plhs = oldMatch.intersection(MCSA);
              }

              if (cmc == null) {
                Automaton challengerMatch = ytarget
                    .lazyMakeMatchAutomaton(inputType);
                cmc = challengerMatch.complement();
              }

              Automaton typeSpecific = raprap.getATSAutomaton(inputType);
              Automaton lhs = plhs.intersection(typeSpecific);
              Automaton diff = lhs.intersection(cmc);
              automatonTestResult = diff.isEmpty();
            }
            // ok see if we got on to something...
            if (fastTestResult || yexhaustiveTestResult || automatonTestResult) {
              if (deathCause == 0) {
                String message = "There is a possible priority conflict between ";
                message += xtarget.toLabelString();
                message += " and ";
                message += ytarget.toLabelString();
                message += " @ context type " + raprap.toLabelString();
                context.pushMessage("flow-competition", message);
              } else {
                // System.err.println("Override: " + xtarget + " lost " + raprap
                // + ", cause=" + deathCause);
                if (!newFlow.get(xtarget).remove(raprap)) {
                  throw new AssertionError(
                      "Flow was not there in the first place??");
                }

                DeadContextFlow cod = new OverriddenContextFlow(ytarget,
                    xtarget, deathCause, getClass().getSimpleName());

                cod.addLostNodeType(raprap);

                x.sourceApply.reportDeath(cod);

                if (fastTestResult)
                  pa.incrementCounter("FastTestOverrideKills",
                      "SensitiveFlowGrapher");
                else if (yexhaustiveTestResult)
                  pa.incrementCounter("ExhaustiveTestOverrideKills",
                      "SensitiveFlowGrapher");
                else if (automatonTestResult)
                  pa.incrementCounter("AutomatonTestOverrideKills",
                      "SensitiveFlowGrapher");
              }
            }
            if (DEBUG
                && (automatonTestResult && !(fastTestResult || yexhaustiveTestResult))) {
              pa.incrementCounter("FastTestScrewups", "CompetitionAnalysis");
            }
          }
        }
      }
      pa.stopTimer("OverrideTest", "CompetitionAnalysis");
    }
  }

  /*
   * private void examineFlowCompetition2(SingleTypeXMLClass inputType,
   * TemplateRule source, DeclaredNodeType contextNode, List<CandidateEdge>
   * candidateEdges, ContextFlow newFlow, PerformanceAnalyzer pa) throws
   * XSLToolsSchemaException, XSLToolsUnhandledNodeTestException,
   * XSLToolsXPathException { What we really want here is -- tada -- a sorting
   * algorithm!! It will sort the template rules in the order in which they have
   * priority wrt absorbing flow, ignoring predicates. We are able to compare
   * any PAIR OF template rules; this is what we used before in a stupid
   * quadratic algorithm. Now we want, for each outflowing type, to make a TOTAL
   * ordering of ALL applicable flow receiving template rules (ignoring
   * predicates). We can then easily find: For apply-templates: The flow
   * receivers are the highest priority template without predicates, along with
   * all higher priority templates WITH predicates. For apply-imports: The flow
   * receivers are as above, but only among templates that are in imports of the
   * module containing the apply-imports, or defaults. For next-match: The flow
   * receivers are as in apply-templates, but only templates with lower priority
   * than the template containing the next-match are considered.
   * 
   * TODO: Default selection: Apply: child::node() The other two: self::node()
   * (this guarantees that the flow emitter is always on the list of flow
   * receivers in the other two cases......)
   * 
   * How to determine what are imports??
   * 
   * Old way: preorder labeling (the two #3s are included together, same level)
   * 1 / \ 2 5 / \ 3-3 4
   * 
   * 
   * Amazing discoveries: imp(2)={3,4}, imp(1)={2,3,4,5}
   * 
   * Could remodel this to: (last number is postorder labeling .. WELL!) (1:5) / \
   * (2:4) (5:5) / \ (3:3) (4:4) (3:3)
   * 
   * It is easy to see that imp(a:b) = {a+1, .., b}. SLAM.
   * 
   * Tactique here is then to make up a list (unordered) of flow targets for
   * each context type, and then bubble or merge sort.
   */

  /*
   * Map<DeclaredNodeType, List<TemplateRule>> flowCompetitionHierarchy = new
   * HashMap<DeclaredNodeType, List<TemplateRule>>();
   * 
   * for (Map.Entry<TemplateRule, Set<DeclaredNodeType>> e :
   * newFlow.entrySet()) { for (DeclaredNodeType nt : e.getValue()) { List<TemplateRule>
   * trl = flowCompetitionHierarchy.get(nt); if (trl==null) { trl = new
   * ArrayList<TemplateRule>(4); flowCompetitionHierarchy.put(nt, trl); }
   * trl.add(e.getKey()); } }
   * 
   * for (Map.Entry<DeclaredNodeType, List<TemplateRule>> e :
   * flowCompetitionHierarchy.entrySet()) { testUnique(e.getValue());
   * Collections.sort(e.getValue()); DeclaredNodeType raprap = e.getKey(); // we
   * want to iterate from one end of the list to the // other, finding the
   * highest ordered template that // is guaranteed to suck up all flow of the
   * context // type -- under the special conditions that apply // because of
   * the kind of apply. // We start accepting as soon as the application
   * condition // is fulfilled (that is, anything goes for apply-templates, //
   * lower-than-emitter priority for that next-match, and //
   * imported-from-emitter-module for apply-imports, and // then accept anything
   * with predicates, and the first // full flow sucker without predictes
   * (right? No..) Automaton plhs = null; Automaton cmc = null;
   * 
   * 
   * boolean automatonTestResult = false; boolean fastTestResult =
   * inputType.allPossibleFlowsCovered( xtarget.match, ytarget.match,
   * Collections.singleton(raprap));
   * 
   * if (DEBUG || !fastTestResult) { if (plhs == null) { // delay constructing
   * the automaton till everything else failed. Automaton MCS =
   * x.sourceSelect.getAlphaIntersectInputAutomaton( contextNode, inputType);
   * Automaton oldMatch = xtarget.lazyMakeMatchAutomaton(inputType); plhs =
   * oldMatch.intersection(MCS); }
   * 
   * if (cmc == null) { Automaton challengerMatch = ytarget
   * .lazyMakeMatchAutomaton(inputType); cmc = challengerMatch.complement(); }
   * 
   * Automaton typeSpecific = raprap.getATSAutomaton(inputType); Automaton lhs =
   * plhs.intersection(typeSpecific); Automaton diff = lhs.intersection(cmc);
   * automatonTestResult = diff.isEmpty(); } if (fastTestResult ||
   * automatonTestResult) { if (deathCause == 0) { System.err .println("Achtung!
   * Possible priority conflict between "); System.err.println(xtarget);
   * System.err.println(ytarget); System.err.println("@ context type " +
   * raprap); System.err.println(" (sorry about saying this twice) "); } else { //
   * System.err.println("Override: " + xtarget + " lost " + raprap // + ",
   * cause=" + deathCause); if (!newFlow.get(xtarget).remove(raprap)) { throw
   * new AssertionError( "Flow was not there in the first place??"); } if
   * (fastTestResult) pa.incrementCounter("FastTestOverrideKills",
   * "CompetitionAnalysis"); if (automatonTestResult)
   * pa.incrementCounter("AutomatonTestOverrideKills", "CompetitionAnalysis"); } }
   * if (DEBUG && (automatonTestResult && !fastTestResult)) {
   * pa.incrementCounter("FastTestScrewups", "CompetitionAnalysis"); } } } }
   * pa.stopTimer("OverrideTest", "CompetitionAnalysis"); } }
   */
}
