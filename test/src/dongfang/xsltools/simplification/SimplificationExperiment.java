package dongfang.xsltools.simplification;

import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ShowStopperErrorReporter;
import dongfang.xsltools.diagnostics.ToLegalXSLConverter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLocatableException;
import dongfang.xsltools.model.ModelConfiguration;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetModuleFactory;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.resolver.URLResolutionContext;
import dongfang.xsltools.util.UniqueNameGenerator;
import dongfang.xsltools.util.Util;

/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */

/**
 * How to use the simplifier...
 * 
 * @author dongfang
 */
public class SimplificationExperiment {

  public static void main(String[] args) throws Exception {

    String inputname = args[0];

    if (!Util.isURL(inputname))
      inputname = Util.toUrlString(inputname);

    /*
     * Logging reporter
     */
    // ErrorReporter cesspool = new LoggingErrorReporter("errors.log");
    /*
     * Exception-throwing reporter
     */
    ErrorReporter cesspool = new ShowStopperErrorReporter();

    UniqueNameGenerator names = new UniqueNameGenerator();

    try {
      ResolutionContext context = new URLResolutionContext();

      StylesheetModuleFactory fac = ModelConfiguration.current
          .getStylesheetModuleFactory();

      Stylesheet stylesheet = fac.createStylesheet(inputname, context,
          new ShowStopperErrorReporter());

      /*
       * Do the simplification stuff
       */
      DefaultSimplifier.getInstance(context, cesspool, names).process(
          stylesheet);

      /*
       * Re-abbreviate explicit child::, attribute:: and /descendant-or-self::
       * axes
       */
      ToLegalXSLConverter.getInstance(cesspool, names).process(stylesheet);

      ExternalModuleSaver saver = new ExternalModuleSaver("simplified-",
          cesspool);

      saver.process(stylesheet);

    } catch (XSLToolsLocatableException ex) {
      cesspool.reportError(ex);
    } catch (XSLToolsException ex) {
      cesspool.reportError(ex);
    }
  }
}