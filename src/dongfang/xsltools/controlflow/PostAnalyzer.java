package dongfang.xsltools.controlflow;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;

public class PostAnalyzer implements FlowAnalyzer {
  private CompositeFlowAnalyzer a = new TimingCompositeFlowAnalyzer();

  private static PostAnalyzer instance = new PostAnalyzer();

  public static PostAnalyzer getInstance() {
    return instance;
  }

  private PostAnalyzer() {
    a.addAnalyzer(DeathFollowerUpper.class);
  }

  public void analyze(/*List<TemplateRule> liveRules*/
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
