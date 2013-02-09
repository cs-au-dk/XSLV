/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-21
 */
package dongfang.xsltools.controlflow;

import java.util.HashSet;
import java.util.Set;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.xmlclass.xslside.NodeType;

/**
 * @author dongfang
 */
public class InsensitiveFlowDeathalyzer implements FlowAnalyzer {
  private static InsensitiveFlowDeathalyzer instance = new InsensitiveFlowDeathalyzer();

  static InsensitiveFlowDeathalyzer getInstance() {
    return instance;
  }

  public void analyze(/*List<TemplateRule> liveRules,*/
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode _mode) {
    for (TemplateRule rule : xcfg.templateRules) {
      for (ApplyTemplatesInst apply : rule.applies) {

        Set<TemplateRule> diff = new HashSet<TemplateRule>();

        for (Selection selection : apply.selections) {
          if (selection.originalPath.toString().endsWith("*") || selection.originalPath.toString().endsWith("node()")) {}
          else {
          for (ContextMode mode : rule.getContextModeSet()) {
            diff.addAll(selection.getContextInsensitiveTargetSet(mode));
          }
          diff.removeAll(selection./*getContextSensitiveTargetSet()*/getAllTargets());
        }
        }

        /* experimental ! */
        for (TemplateRule target : diff) {
          DeadContextFlow deathCause = new DeadContextFlow(target,
              DeadContextFlow.DOES_NOT_MATCH_SCHEMA, getClass().getSimpleName());

          for (Selection selection : apply.selections) {
            for (ContextMode mode : rule.getContextModeSet()) {
              Set<? extends NodeType> lostTypes = selection
                  .getContextInsensitiveEdgeFlow(mode, target);
              deathCause.addLostNodeTypes(lostTypes);
            }
            apply.reportDeath(deathCause);
          }
        }
      }
    }
  }
}