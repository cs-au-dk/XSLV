package dongfang.xsltools.controlflow;

import java.util.LinkedList;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.exceptions.XSLToolsException;

public class ContextSensitiveNoReachableOutputAnalyzer implements FlowAnalyzer {

  public static ContextSensitiveNoReachableOutputAnalyzer instance = new ContextSensitiveNoReachableOutputAnalyzer();

  public static ContextSensitiveNoReachableOutputAnalyzer getInstance() {
    return instance;
  }

  public void analyze(/*List<TemplateRule> liveRules,*/
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode bootstrapMode) throws XSLToolsException {

    LinkedList<NewContextFlow> work = new LinkedList<NewContextFlow>();
    for (TemplateRule rule : xcfg.templateRules) {
      rule.initSensitiveOutputtersReachable(work);
    }

    while (!work.isEmpty()) {
      NewContextFlow flow = work.removeFirst();
      flow.target.addSensitiveOutputtersReachable(flow.contextMode,
          flow.contextType, work);
    }

    // nuke the reverse flow maps; they are space hogs
    for (TemplateRule rule : xcfg.templateRules) {
      rule.sensitiveInflows = null;
    }
  }
}
