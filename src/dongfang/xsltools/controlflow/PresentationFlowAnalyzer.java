package dongfang.xsltools.controlflow;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.exceptions.XSLToolsException;

public class PresentationFlowAnalyzer implements FlowAnalyzer {
  private CompositeFlowAnalyzer a = new TimingCompositeFlowAnalyzer();

  private static PresentationFlowAnalyzer instance = new PresentationFlowAnalyzer();

  static PresentationFlowAnalyzer getInstance() {
    return instance;
  }

  public PresentationFlowAnalyzer() {
    a.addAnalyzer(RawTAGFlowGrapher.class);
    a.addAnalyzer(DeadTemplateTagger.class);
    a.addAnalyzer(BFG9000Analyzer.class);
    a.addAnalyzer(InsensitiveFlowDeathalyzer.class);
  }

  public void analyze(/*List<TemplateRule> liveRules,*/
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode bootstrapMode) throws XSLToolsException {
    a.analyze(xcfg, validationContext, bootstrapTemplateRule,
        bootstrapMode);
  }
}
