package dongfang.xsltools.controlflow;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;

/**
 * The new flow algorithm (more or less the main result of the thesis).
 * It is fast, and as precise as the improved old algorithm (BFG9000Analyzer with friends)
 * @author dongfang
 *
 */
public class FastValidationFlowAnalyzer implements FlowAnalyzer {
  private CompositeFlowAnalyzer a = new TimingCompositeFlowAnalyzer();

  private static FastValidationFlowAnalyzer instance = new FastValidationFlowAnalyzer();

  public static FastValidationFlowAnalyzer getInstance() {
    return instance;
  }

  private FastValidationFlowAnalyzer() {
    // The phi-m thing
    a.addAnalyzer(ModeCompatibilityFlowAnalyzer.class);
    // we need to use the template mode sets again, so clear them
    a.addAnalyzer(ModeSetZapper.class);
    a.addAnalyzer(RawTAGFlowGrapher.class);
    a.addAnalyzer(DeadTemplateTagger.class);
    a.addAnalyzer(FastContextSensitiveAnalyzer.class);

    if (ControlFlowConfiguration.current.useNoOutputSGTruncation())
      a.addAnalyzer(ContextSensitiveNoReachableOutputAnalyzer.class);

    if (ControlFlowConfiguration.current.generateFlowDeathReport()) {
      a.addAnalyzer(InsensitiveFlowDeathalyzer.class);
      //a.addAnalyzer(DeathFollowerUpper.class);
    }
  }

  public void analyze(
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode bootstrapMode) throws XSLToolsException {

    a.analyze(xcfg, validationContext, bootstrapTemplateRule,
        bootstrapMode);
  }

  public ControlFlowGraph analyze(Stylesheet stylesheet,
      ValidationContext validationContext, ErrorReporter cesspool)
      throws XSLToolsException {

    return a.analyze(stylesheet, validationContext, cesspool);
  }
}
