/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-19
 */
package dongfang.xsltools.controlflow;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.exceptions.XSLToolsException;

/**
 * @author dongfang
 */

/*
 * Consider separate methods for context sens vs context insens. @author
 * dongfang
 */
public interface FlowAnalyzer {
  void analyze(/*List<TemplateRule> liveRules,*/
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode bootstrapMode) throws XSLToolsException;
}
