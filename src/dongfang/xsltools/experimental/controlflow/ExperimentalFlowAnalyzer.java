package dongfang.xsltools.experimental.controlflow;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.controlflow.CompositeFlowAnalyzer;
import dongfang.xsltools.controlflow.ContextMode;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.controlflow.DeadTemplateTagger;
import dongfang.xsltools.controlflow.FastContextSensitiveAnalyzer;
import dongfang.xsltools.controlflow.FlowAnalyzer;
import dongfang.xsltools.controlflow.RawTAGFlowGrapher;
import dongfang.xsltools.controlflow.TemplateRule;
import dongfang.xsltools.controlflow.TimingCompositeFlowAnalyzer;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;

public class ExperimentalFlowAnalyzer implements FlowAnalyzer {
  private CompositeFlowAnalyzer a = new TimingCompositeFlowAnalyzer();

  private static ExperimentalFlowAnalyzer instance = new ExperimentalFlowAnalyzer();

  public static ExperimentalFlowAnalyzer getInstance() {
    return instance;
  }

  private ExperimentalFlowAnalyzer() {
    a.addAnalyzer(RawTAGFlowGrapher.class);
    a.addAnalyzer(DeadTemplateTagger.class);
    a.addAnalyzer(FastContextSensitiveAnalyzer.class);
  }

  public void analyze(ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode mode) throws XSLToolsException {
    a.analyze(xcfg, validationContext, bootstrapTemplateRule, mode);
  }

  public ControlFlowGraph analyze(Stylesheet stylesheet,
      ValidationContext validationContext, ErrorReporter cesspool)
      throws XSLToolsException {
    return a.analyze(stylesheet, validationContext, cesspool);
  }
}
