package dongfang.xsltools.controlflow;

import java.util.ListIterator;
import java.util.Set;

import dongfang.xsltools.context.ValidationContext;

/**
 * Pronounce templates w/o schemaless context types dead.
 */
public class DeadTemplateTagger implements FlowAnalyzer {

  public static DeadTemplateTagger instance = new DeadTemplateTagger();

  public static DeadTemplateTagger getInstance() {
    return instance;
  }

  public void analyze(ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode mode) {
    ListIterator<TemplateRule> ai = xcfg.templateRules.listIterator();
    while (ai.hasNext()) {
      TemplateRule a = ai.next();
      Set csa = a.getSchemalessContextSet();
      if (csa.isEmpty()) {
        a.pronounceDead();
      }
    }
  }
}
