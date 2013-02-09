/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-21
 */
package dongfang.xsltools.simplification;

import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.ModelConfiguration;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * A wrapper over a CompositeProcessor that performs the full semantics
 * preserving simplification + approximative simplification.
 * 
 * @author dongfang
 */
public class DefaultSimplifier {

  private CompositeProcessor s;

  private DefaultSimplifier() {
  }

  public static DefaultSimplifier getInstance(ResolutionContext context,
      ErrorReporter cesspool, UniqueNameGenerator names) {
    DefaultSimplifier instance = new DefaultSimplifier();

    /*
     * An awful diagnostics thing: Do we want to see intermediate results?
     */
    if (SimplifierConfiguration.current.dumpIntermediateResultsAtEachStep()) {
      instance.s = ImmediatelyVerboseCompositeProcessor.getInstance(cesspool,
          names);
    }

    else {
      instance.s = CompositeProcessor.getInstance(cesspool, names);
    }

    /*
     * An awful diagnostics thing: Do we want to make sanity checks?
     */
    boolean checkEachStep = SimplifierConfiguration.current
        .checkElementIDsAtEveryStage();
    boolean checkFinally = SimplifierConfiguration.current
        .checkElementIDAfterApproxmatingSimplification();

    if (checkEachStep)
      instance.s.addSimplifier(new ElementIdentityChecker("outset"));

    SemanticsPreservingSimplifier sps = SemanticsPreservingSimplifier
        .getInstance(context, cesspool, names);

    instance.s.addSimplifier(sps);

    if (checkEachStep)
      instance.s.addSimplifier(new ElementIdentityChecker(
          "Post semantics preserving simplifier"));

    instance.s.addSimplifier(ParameterSimplifier.class);

    if (checkEachStep)
      instance.s.addSimplifier(new ElementIdentityChecker(
          "Post parameter simplifier"));

    instance.s.addSimplifier(ApproximativeSimplifier.class);

    if (checkEachStep || checkFinally)
      instance.s.addSimplifier(new ElementIdentityChecker(
          "Post approximative simplifier"));

    return instance;
  }

  /**
   * Simplify and return stylesheet.
   * @param systemId
   * @param context
   * @param cesspool
   * @return
   * @throws XSLToolsException
   */
  public Stylesheet getStylesheet(String systemId, ResolutionContext context,
      ErrorReporter cesspool) throws XSLToolsException {

    PerformanceLogger pa = DiagnosticsConfiguration.current
        .getPerformanceLogger();

    pa.startTimer("GetStylesheet", "Simplification");

    Stylesheet stylesheet = ModelConfiguration.current
        .getStylesheetModuleFactory().createStylesheet(systemId, ResolutionContext.MSG_XSL_PRINCIPAL, context,
            cesspool);

    pa.stopTimer("GetStylesheet", "Simplification");

    if (!SimplifierConfiguration.current
        .shouldContinueStylesheetProcessingAfterSyntacticErrors()
        && cesspool.hasErrors())
      return null;

    pa.startTimer("SimplificationProper", "Simplification");

    s.process(stylesheet);

    pa.stopTimer("SimplificationProper", "Simplification");

    return stylesheet;
  }

  public void process(Stylesheet stylesheet) throws XSLToolsException {
    s.process(stylesheet);
  }
}