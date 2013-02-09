package dongfang.xsltools.controlflow;

import java.util.LinkedList;
import java.util.List;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.exceptions.XSLToolsException;

public class NoReachableOutputAnalyzer implements FlowAnalyzer {

  public static NoReachableOutputAnalyzer instance = new NoReachableOutputAnalyzer();

  public static NoReachableOutputAnalyzer getInstance() {
    return instance;
  }

  public void analyze(/*List<TemplateRule> liveRules,*/
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode bootstrapMode) throws XSLToolsException {
    updateInflowSetOfTargets(xcfg.templateRules);
    LinkedList<TemplateRule> work = new LinkedList<TemplateRule>();
    initReachableOutputInstructions(xcfg.templateRules, work);
    propagateHasReachableOutputInstructions(work);

    for (TemplateRule rule : xcfg.templateRules) {
      if (!rule.hasLocalOutputInstructions && !rule.pronouncedDead) {
        if (!rule.getAllTargets().isEmpty()) {
          System.out.println("Found a no-output rule: " + rule);
          System.out.println("It had targets: " + rule.getAllTargets());
        }
      }
    }
  }

  // has-reachable-outputter analysis stage 1: let all successors know that we
  // call them
  void updateInflowSetOfTargets(List<TemplateRule> liveRules) {
    for (TemplateRule rule : liveRules) {
      rule.updateInflowSetOfTargets();
    }
  }

  // has-reachable-outputter analysis stage 2: Init work list: All templates
  // with local output are added
  void initReachableOutputInstructions(List<TemplateRule> liveRules,
      LinkedList<TemplateRule> work) {
    for (TemplateRule rule : liveRules) {
      if (rule.hasLocalOutputInstructions) {
        work.add(rule);
      }
    }
  }

  // has-reachable-outputter analysis stage 3: We are called by some rule on the
  // work list being handled.
  // That is, we can reach output. Mark ourselves as such, and if that was not
  // known before, add everything
  // one step upstream to work list.
  void propagateHasReachableOutputInstructions(LinkedList<TemplateRule> workList) {
    while (!workList.isEmpty()) {
      TemplateRule rule = workList.removeFirst();
      if (!rule.hasLocalOutputInstructions) {
        rule.hasLocalOutputInstructions = true;
        workList.addAll(rule.inflows);
      }
    }
  }
}
