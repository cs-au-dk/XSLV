package dongfang.xsltools.experimental;

import dongfang.xsltools.diagnostics.ShowStopperErrorReporter;
import dongfang.xsltools.model.ModelConfiguration;
import dongfang.xsltools.model.StylesheetModuleFactory;
import dongfang.xsltools.resolver.URLResolutionContext;

public class Kaboom {
  public static void main(String[] args) throws Exception {
    String xammm = "file:test/resources/simplification/loader/A.xsl";
    StylesheetModuleFactory f = ModelConfiguration.current
        .getStylesheetModuleFactory();
    f.createStylesheet(xammm, "", new URLResolutionContext(),
        new ShowStopperErrorReporter());
    System.out.println("nemt nok!");
  }
}
