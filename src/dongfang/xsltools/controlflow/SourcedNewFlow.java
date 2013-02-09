package dongfang.xsltools.controlflow;

public class SourcedNewFlow extends NewFlow {
  final ApplyTemplatesInst sourceApply;

  final Selection sourceSelect;

  SourcedNewFlow(TemplateRule target, ContextMode contextMode,
      ApplyTemplatesInst sourceApply, Selection sourceSelect) {
    super(target, contextMode);
    this.sourceApply = sourceApply;
    this.sourceSelect = sourceSelect;
  }

  /*
   * Roll the mode from the mode flowing into the containing template, to the
   * mode flowing out of the apply-templates instruction.
   */
  void rollMode() {
    this.contextMode = this.sourceApply.getMode().contextualize(
        this.contextMode);
  }
}
