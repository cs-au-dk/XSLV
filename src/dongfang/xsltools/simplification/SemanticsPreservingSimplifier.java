/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-21
 */
package dongfang.xsltools.simplification;

import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.XPathCollector;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.ModelConfiguration;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.util.ToplevelBindingDumper;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * A wrapper over a CompositeSimplifier that performs the full semantics
 * preserving simplification.
 * 
 * @author dongfang
 */
public class SemanticsPreservingSimplifier implements StylesheetProcessor {
  private CompositeProcessor s;

  private SemanticsPreservingSimplifier() {
  }

  public static SemanticsPreservingSimplifier getInstance(
      ResolutionContext context, ErrorReporter cesspool,
      UniqueNameGenerator names) {

    SemanticsPreservingSimplifier instance = new SemanticsPreservingSimplifier();

    if (SimplifierConfiguration.current.dumpIntermediateResultsAtEachStep()) {
      instance.s = ImmediatelyVerboseCompositeProcessor.getInstance(cesspool,
          names);
    }

    else
      instance.s = CompositeProcessor.getInstance(cesspool, names);

    ExternalModuleLoader loader = ExternalModuleLoader.getInstance(cesspool,
        names);

    // special case: This guy needs to know about the resolution context.
    loader.setContext(context);

    boolean checkIdsAtEveryStage = SimplifierConfiguration.current
        .checkElementIDsAtEveryStage();

    boolean checkIdsAfterFinalStage = SimplifierConfiguration.current
        .checkElementIDAfterSemanticsPreservingSimplification();

    if (checkIdsAtEveryStage)
      instance.s.addSimplifier(new ElementIdentityChecker("At outset"));

    instance.s.addSimplifier(loader);

    if (checkIdsAtEveryStage)
      instance.s.addSimplifier(new ElementIdentityChecker(
          "Post external module loader"));

    instance.s.addSimplifier(ContextFreeSimplifier.class);

    if (checkIdsAtEveryStage)
      instance.s.addSimplifier(new ElementIdentityChecker(
          "Post context free simplifer"));

    instance.s.addSimplifier(RestructuringSimplifier.class);

    if (SimplifierConfiguration.current.dumpToplevelBindings()) {
      instance.s.addSimplifier(ToplevelBindingDumper.class);
    }

    if (checkIdsAtEveryStage)
      instance.s.addSimplifier(new ElementIdentityChecker(
          "restructuring simplifier"));

    instance.s.addSimplifier(ResolutionSimplifier2.class);

    if (checkIdsAtEveryStage)
      instance.s.addSimplifier(new ElementIdentityChecker(
          "resolution simplifier"));

    instance.s.addSimplifier(TemplateSimplifier.class);

    if (checkIdsAtEveryStage)
      instance.s.addSimplifier(new ElementIdentityChecker(
          "template simplifier I"));

    instance.s.addSimplifier(TemplateSimplifier2.class);

    if (checkIdsAtEveryStage || checkIdsAfterFinalStage)
      instance.s.addSimplifier(new ElementIdentityChecker(
          "template simplifier II"));

    // TODO: Should this happen after approximative simplification?
    instance.s.addSimplifier(IdToElementMapUpdater.class);

    if (checkIdsAtEveryStage || checkIdsAfterFinalStage)
      instance.s.addSimplifier(new ElementIdentityChecker("ID-elemap updater"));

    if (DiagnosticsConfiguration.current.collectXPathPathexps())
      instance.s.addSimplifier(XPathCollector.class);

    return instance;
  }

  public Stylesheet getStylesheet(String systemId, ResolutionContext context,
      ErrorReporter cesspool) throws XSLToolsException {
    Stylesheet stylesheet = ModelConfiguration.current
        .getStylesheetModuleFactory().createStylesheet(
            systemId, 
            ResolutionContext.MSG_XSL_PRINCIPAL,
            context,
            cesspool);
    s.process(stylesheet);
    return stylesheet;
  }

  public void process(Stylesheet stylesheet) throws XSLToolsException {
    s.process(stylesheet);
  }
}