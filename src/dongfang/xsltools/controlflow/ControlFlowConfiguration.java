package dongfang.xsltools.controlflow;

public class ControlFlowConfiguration {

  public static enum ControlFlowAlgorithm {

    /*
     * Use the abstract XPath evaluation test for detecting edges that are
     * infeasible anyway, and hard / soft kill them
     */
    ABSTRACT_EVALUATION,

    /*
     * Use the insensitive regexp intersection test for detecting edges that are
     * infeasible anyway, and hard / soft kill them. May be combined with the
     * XPath evaluation test (but they almost always come up with the same
     * result in practise, so there's no reason to use both).
     */
    INSENSITIVE_REGEXP
  };

  public static ControlFlowConfiguration current = new ControlFlowConfiguration();

  public final boolean removeVarDeclarations() {
    return true;
  }

  public final boolean useColoredContextTypes() {
    return true;
  }

  public final boolean useColoredFlowPropagation() {
    return useColoredContextTypes() & true;
  }

  public final boolean useOrthogonalAbstractTest() {
    return true;
  }

  public final boolean useCommentPIPropagation() {
    return false;
  }

  public final ControlFlowAlgorithm candidateFilterAlgorithm() {
    return ControlFlowAlgorithm.ABSTRACT_EVALUATION;
  }

  /*
   * Make candidate edge even if it is determined already at this stage that the
   * edge will never carry any flow. The edge is marked as "could have been
   * killed". @return
   */
  public final boolean killCandidateEdgesOnlySoftly() {
    return false;
  }

  public final boolean sanityCheckContextSetsWithContextFlows() {
    return false;
  }

  public final boolean reduceSummaryGraph() {
    return true;
  }

  public final boolean checkSummaryGraph() {
    return false;
  }

  public final boolean generateFlowDeathReport() {
    return true;
  }

  public final boolean runCodeAssistAlgorithms() {
    return false;
  }

  public final boolean usePrefilterCache() {
    return true;
  }

  public final boolean useAlphaAutomatonCache() {
    return true;
  }

  public final boolean useAlphaBetaResultCache() {
    return true;
  }

  /*
   * currently broken, do not use. Does not regard the "same types different
   * modes" situation.
   */
  public final boolean canonicalizeContextSets() {
    return false;
  }

  public final boolean useNoOutputSGTruncation() {
    return false;
  }
}
