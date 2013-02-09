package dongfang.xsltools.diagnostics;

import java.util.HashSet;
import java.util.Set;

public class DiagnosticsConfiguration {
  public static DiagnosticsConfiguration current = new DiagnosticsConfiguration();

  private static PerformanceLogger pa = new ProgressDiagnosticsPerformanceLogger(); 
	  //new DummyPerformanceLogger();

  public static final int ORIGINAL = 0;

  public static final int SIMPLIFIED = 1;

  public PerformanceLogger getPerformanceLogger() {
    return pa;
  }

  public boolean outputDeadFlowDiagsInControlFlowGraph() {
    return true;
  }

  public boolean outputAncestorStringsInControlFlowGraph() {
    return false;
  }

  public boolean outputSchemalessStuffInControlFlowGraph() {
    return false;
  }

  public boolean outputAutomataInControlFlowGraph() {
    return false;
  }

  public boolean outputAllInstructionsInControlFlowGraph() {
    return true;
  }

  public boolean outputCharNamesInControlFlowGraph() {
    return false;
  }

  public static boolean useOriginStringsForFragments() {
    return true;
  }

  public Set<Object> getDefaultDiagnosticsConfiguration() {
    Set<Object> result = new HashSet<Object>();
    if (outputDeadFlowDiagsInControlFlowGraph())
      result.add("outputDeadFlowDiagsInControlFlowGraph");
    if (outputSchemalessStuffInControlFlowGraph())
      result.add("outputSchemalessStuffInControlFlowGraph");
    if (outputAncestorStringsInControlFlowGraph())
      result.add("outputAncestorStringsInControlFlowGraph");
    if (outputAutomataInControlFlowGraph())
      result.add("outputAutomataInControlFlowGraph");
    if (outputAllInstructionsInControlFlowGraph())
      result.add("outputAllInstructionsInControlFlowGraph");
    if (outputCharNamesInControlFlowGraph())
      result.add("outputCharNamesInControlFlowGraph");
    if (useOriginStringsForFragments())
      result.add("useOriginStringsForFragments");
    return result;
  }

  public static boolean outputDeadFlowDiagsInControlFlowGraph(Set<Object> configuration) {
    return configuration.contains("outputDeadFlowDiagsInControlFlowGraph");
  }

  public static boolean outputAncestorStringsInControlFlowGraph(Set<Object> configuration) {
    return configuration.contains("outputAncestorStringsInControlFlowGraph");
  }

  public static boolean outputSchemalessStuffInControlFlowGraph(Set<Object> configuration) {
    return configuration.contains("outputSchemalessStuffInControlFlowGraph");
  }

  public static boolean outputAutomataInControlFlowGraph(Set<Object> configuration) {
    return configuration.contains("outputAutomataInControlFlowGraph");
  }

  public static boolean outputAllInstructionsInControlFlowGraph(Set<Object> configuration) {
    return configuration.contains("outputAllInstructionsInControlFlowGraph");
  }

  public static boolean outputCharNamesInControlFlowGraph(Set<Object> configuration) {
    return configuration.contains("outputCharNamesInControlFlowGraph");
  }

  public static boolean useOriginStringsForFragments(Set<Object> configuration) {
    return configuration.contains("useOriginStringsForFragments");
  }

  public boolean traceControlFlow() {
    return false;
  }

  public int traceControlFlowTickCount() {
    return 100000;
  }

  public boolean traceResourceLoading() {
    return traceControlFlow();
  }

  public String getGeneratedHTMLPrefix() {
    return "tmp/html";
  }

  public String getGeneratedHTMLStylesheetName() {
    //return "simple-annotated-xml-to-xhtml-tabular.xsl";
    return "simple-annotated-xml-to-xhtml.xsl";
  }

  public boolean collectXPathPathexps() {
    return false;
  }
}
