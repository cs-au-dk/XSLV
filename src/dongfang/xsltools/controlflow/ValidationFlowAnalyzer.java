package dongfang.xsltools.controlflow;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;

public class ValidationFlowAnalyzer implements FlowAnalyzer {
  private CompositeFlowAnalyzer a = new TimingCompositeFlowAnalyzer();

  private static ValidationFlowAnalyzer instance = new ValidationFlowAnalyzer();

  public static ValidationFlowAnalyzer getInstance() {
    return instance;
  }

  private ValidationFlowAnalyzer() {
    a.addAnalyzer(ModeCompatibilityFlowAnalyzer.class);
    // we need to use the template mode sets again, so clear them
    a.addAnalyzer(ModeSetZapper.class);
    a.addAnalyzer(RawTAGFlowGrapher.class);
    a.addAnalyzer(DeadTemplateTagger.class);
    a.addAnalyzer(BFG9000Analyzer.class);
    if (ControlFlowConfiguration.current.generateFlowDeathReport()) {
      a.addAnalyzer(InsensitiveFlowDeathalyzer.class);
    }
  }

  public void analyze(/*List<TemplateRule> liveRules,*/
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
