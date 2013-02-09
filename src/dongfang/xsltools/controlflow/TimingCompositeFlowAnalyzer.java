package dongfang.xsltools.controlflow;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;

/**
 * A simple composite analyzer that also times the analysis 
 * (using the usual PerformanceAnalyzer)
 * @author dongfang
 */
public class TimingCompositeFlowAnalyzer extends CompositeFlowAnalyzer {
  private int noRuns = 1;

  private String processName = "ValidationFlowAnalyzer";

  PerformanceLogger pa = DiagnosticsConfiguration.current
      .getPerformanceLogger();

  static CompositeFlowAnalyzer getInstance() {
    return new TimingCompositeFlowAnalyzer();
  }

  @Override
protected void analyze(FlowAnalyzer analyzer, 
  ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode mode) throws XSLToolsException {
    pa.startTimer(analyzer.getClass().getSimpleName(), processName);
    super.analyze(analyzer, xcfg, validationContext,
        bootstrapTemplateRule, mode);
    pa.stopTimer(analyzer.getClass().getSimpleName(), processName);
  }

  @Override
public ControlFlowGraph analyze(Stylesheet stylesheet,
      ValidationContext validationContext, ErrorReporter cesspool)
      throws XSLToolsException {
    // pa.startTimer(processName, parentName);
    for (int i = 0; i < noRuns - 1; i++) {
      super.analyze(stylesheet, validationContext, cesspool);
    }
    ControlFlowGraph result = super.analyze(stylesheet, validationContext,
        cesspool);
    // pa.stopTimer(processName, parentName);
    return result;
  }
}
