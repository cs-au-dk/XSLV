package dongfang.xsltools.controlflow;

import java.util.LinkedList;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;

/**
 * The phi-m analyzer: Hook up invokers with mode-compatible templates
 * @author dongfang
 */
public class ModeCompatibilityFlowAnalyzer implements FlowAnalyzer {

  private static ModeCompatibilityFlowAnalyzer instance = new ModeCompatibilityFlowAnalyzer();

  public static ModeCompatibilityFlowAnalyzer getInstance() {
    return instance;
  }

  public void analyze(
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode bootstrapMode) throws XSLToolsException {

    PerformanceLogger pa = DiagnosticsConfiguration.current
        .getPerformanceLogger();

    // work list
    LinkedList<NewFlow> work = new LinkedList<NewFlow>();

    // next gen. work list
    LinkedList<NewFlow> propagationCandidates = new LinkedList<NewFlow>();

    work.add(new NewFlow(bootstrapTemplateRule, bootstrapMode));

    while (!work.isEmpty()) {
      NewFlow flow = work.removeFirst();

      TemplateRule source = flow.target;
      ContextMode mode = flow.contextMode;

      for (ApplyTemplatesInst apply : source.applies) {
        apply.locateModeCompatibleEdges(xcfg.templateRules, mode, propagationCandidates,
            pa);
      }

      for (NewFlow candidate : propagationCandidates) {
        if (candidate.target.addMode(candidate.contextMode)) {
          work.add(candidate);
        }
      }
      propagationCandidates.clear();
    }
  }
}
