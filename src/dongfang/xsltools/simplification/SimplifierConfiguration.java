package dongfang.xsltools.simplification;

/**
 * All configuration of the simplifier is supposed to go here.
 * @author dongfang
 */
public class SimplifierConfiguration {

  public static SimplifierConfiguration current = new SimplifierConfiguration();

  public static enum WhitespaceNodeBehaviour {
    PRESERVE, REMOVE
  };

  private boolean shouldContinueStylesheetProcessingAfterSyntacticErrors = false;

  private boolean checkElementIDsAtEveryStage = false;

  private boolean checkElementIDAfterSemanticsPreservingSimplification = false;

  private boolean checkElementIDAfterApproxmatingSimplification = false;

  private boolean removeAttributeSetDeclarations = true;

  private boolean removeVariableDeclarations = true;

  private boolean removeParameterDeclarations = true;

  private boolean dumpIntermediateResultsAtEachStep = false;

  private String intermediateDumpPath = "tmp/simplification";

  private String externalModuleSaverDefaultPath = "tmp";

  private String intermediateDumpPrefix = "";

  private boolean alsoDumpOriginalDocument = false;

  private boolean dumpToplevelBindings = false;

  private int performanceTimingRunCount = 1;

  private WhitespaceNodeBehaviour whitespaceNodeBehaviour = WhitespaceNodeBehaviour.REMOVE;

  public boolean shouldContinueStylesheetProcessingAfterSyntacticErrors() {
    return shouldContinueStylesheetProcessingAfterSyntacticErrors;
  }

  /*
   * Check that element ID constraints are satisfied. No good for performance,
   * but turn on if simplifier behaves wierd / buggy
   */
  public boolean checkElementIDsAtEveryStage() {
    return checkElementIDsAtEveryStage;
  }

  /*
   * Check that element ID constraints are satisfied. Small impact on
   * performance, turn on if simplifier behaves wierd / buggy
   */
  public boolean checkElementIDAfterSemanticsPreservingSimplification() {
    return checkElementIDAfterSemanticsPreservingSimplification;
  }

  /*
   * Check that element ID constraints are satisfied. Small impact on
   * performance, turn on if simplifier behaves wierd / buggy
   */
  public boolean checkElementIDAfterApproxmatingSimplification() {
    return checkElementIDAfterApproxmatingSimplification;
  }

  /*
   * Remove expanded xsl:variable or not
   */
  public boolean removeVariableDeclarations() {
    return removeVariableDeclarations;
  }

  /*
   * Remove expanded xsl:attribute-set or not
   */
  public boolean removeAttributeSetDeclarations() {
    return removeAttributeSetDeclarations;
  }

  /*
   * Remove expanded xsl:param or not
   */
  public boolean removeParameterDeclarations() {
    return removeParameterDeclarations;
  }

  /*
   * Dump simplified (and possibly original) principal module (not default
   * rules, and not includes / imports) after EACH step of simplification. A
   * great way to see what happens (combine with the top level binding dumper)
   * and a real disk and performance hog.
   */
  public boolean dumpIntermediateResultsAtEachStep() {
    return dumpIntermediateResultsAtEachStep;
  }

  /*
   * only has effect when dumpIntermediateResultsAtEachStep() is true
   */
  public String intermediateDumpPath() {
    return intermediateDumpPath;
  }

  /*
   * only has effect when dumpIntermediateResultsAtEachStep() is true
   */
  public String intermediateDumpPrefix() {
    return intermediateDumpPrefix;
  }

  /*
   * only has effect when dumpIntermediateResultsAtEachStep() is true
   */
  public boolean alsoDumpOriginalDocument() {
    return alsoDumpOriginalDocument;
  }

  public boolean dumpToplevelBindings() {
    return dumpToplevelBindings;
  }

  /*
   * Won't work together with dumpIntermediateResultsAtEachStep() == true
   */
  public int performanceTimingRunCount() {
    return performanceTimingRunCount;
  }

  public String getExternalModuleSaverDefaultPath() {
    return externalModuleSaverDefaultPath;
  }

  /*
   * Keep them or zap them .. zapping them is probably not safe with
   * preserve-whitespace
   */
  public WhitespaceNodeBehaviour getWhitespaceOnlyTextNodeBevaviour() {
    return whitespaceNodeBehaviour;
  }

  public boolean addTransformOriginComments() {
    return false;
  }
}
