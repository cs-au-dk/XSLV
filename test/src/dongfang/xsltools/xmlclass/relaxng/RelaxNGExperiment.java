/*
 * dongfang M. Sc. Thesis
 * Created on 01-10-2005
 */
package dongfang.xsltools.xmlclass.relaxng;

import dongfang.xsltools.configuration.ConfigurationFactory;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.resolver.URLResolutionContext;
import dongfang.xsltools.util.Util;

/**
 * @author dongfang
 */

public class RelaxNGExperiment {
  public static void main(String[] args) throws Exception {

    ConfigurationFactory.setVerboseLoggerConfiguration();

    String stinkdyr = args[0];

    stinkdyr = Util.toUrlString(stinkdyr);

    ResolutionContext context = new URLResolutionContext();

    try {
      RNGModule module = RelaxNGFactory.getSimplifiedRelaxNG(context, stinkdyr);
      module = RelaxNGFactory.getSimplifiedRelaxNG(context, stinkdyr);
      module = RelaxNGFactory.getSimplifiedRelaxNG(context, stinkdyr);
      module = RelaxNGFactory.getSimplifiedRelaxNG(context, stinkdyr);
      module = RelaxNGFactory.getSimplifiedRelaxNG(context, stinkdyr);
      // System.out.println(module.getElementTypeCount());
    } catch (Exception ex) {
      System.out.println(ex);
    }
  }
}
