package dongfang.xsltools.controlflow;

import java.util.ListIterator;
import java.util.Set;

import dongfang.xsltools.context.ValidationContext;

/**
 * Reset the context-mode set of each template.
 * If already empty, pronounce template dead.
 * @author dongfang
 */
  public class ModeSetZapper implements FlowAnalyzer {

  public static ModeSetZapper instance = new ModeSetZapper();

  public static ModeSetZapper getInstance() {
    return instance;
  }

  public void analyze(
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode mode) {
    ListIterator<TemplateRule> ai = xcfg.templateRules.listIterator();
    while (ai.hasNext()) {
      TemplateRule a = ai.next();
      Set<? extends ContextMode> csa = a.getContextModeSet();
      if (csa.isEmpty()) {
        a.pronounceDead();
      } else {
        csa.clear();
      }
    }
  }
}
