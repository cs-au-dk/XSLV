package dongfang.xsltools.xmlclass.relaxng;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Document;
import org.dom4j.io.DocumentResult;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.util.Dom4jUtil;

public class RelaxNGFactory {

  private static final String prefix = "resources/xslt";

  private static final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory
      .newInstance();

  private static final String[] stylesheetNames = { "step7.2.xsl",
      "step7.3.xsl", "step7.4.xsl", "step7.5.xsl", "step7.7.xsl",
      "step7.8.xsl", "step7.9.xsl", "step7.10.xsl", "step7.11.xsl",
      "step7.12.xsl", "step7.13.xsl", "step7.14.xsl", "step7.15.xsl",
      "step7.16.xsl", "step7.18.xsl", "step7.19.xsl", "step7.20.xsl",
      "step7.22.xsl" };

  // private static XMLFilter combinedTransform;

  /*
   * static { try { combinedTransform = buildTransformer(stylesheetNames,
   * prefix); } catch (Exception ex) { throw new AssertionError("Error in init.
   * Should never happen."); } }
   */

  private static final Transformer trafo;
  static {
    try {
      trafo = stf.newTransformer(new StreamSource(prefix + File.separatorChar
          + stylesheetNames[stylesheetNames.length - 1]));
    } catch (TransformerConfigurationException ex) {
      throw new AssertionError("Error in init. Should never happen.");
    }
  }

  private static XMLFilter buildTransformer(String[] stylesheetNames,
      String prefix) throws TransformerConfigurationException {
    XMLFilter parent = null;
    XMLFilter norm1 = parent;
    for (int i = 0; i < stylesheetNames.length - 1; i++) {
      // System.err.println("Loading: " + stylesheetNames[i]);
      norm1 = stf.newXMLFilter(new StreamSource(prefix + File.separatorChar
          + stylesheetNames[i]));
      if (parent != null)
        norm1.setParent(parent);
      parent = norm1;
    }
    return norm1;
  }

  public static RNGModule getSimplifiedRelaxNG(ResolutionContext context,
      String systemId) throws XSLToolsException {
    try {
      InputSource is = context.resolveStream(systemId,
          "Relax NG module", 
          ResolutionContext.RELAXNG_MODULE_IDENTIFIER_KEY);
      return getSimplifiedRelaxNG(is);
    } catch (IOException ex) {
      throw new XSLToolsLoadException(ex);
    }
  }

  public static RNGModule getSimplifiedRelaxNG(InputSource source)
      throws XSLToolsException {
    try {
      DocumentResult res = new DocumentResult();

      XMLFilter combinedTransform = buildTransformer(stylesheetNames, prefix);

      if (combinedTransform == null) {
        // pipeline has length 1; run only final stage.
        trafo.transform(new SAXSource(source), res);
      } else {
        // use the pre-built pipeline
        trafo.transform(new SAXSource(combinedTransform, source), res);
      }

      Document d = res.getDocument();

      Dom4jUtil.debugPrettyPrint(d);

      return new RNGModule(d);

    } catch (TransformerException ex) {
      throw new XSLToolsLoadException(ex);
    }

    /*
     * ResolutionContext snyd = new URLResolutionContext();
     * NoParseLocationStylesheetModuleFactory fac = new
     * NoParseLocationStylesheetModuleFactory(); fac.setValidating(false);
     * Document d = fac.read(SystemId, snyd, new ShowStopperErrorReporter());
     * return new RNGModule(d);
     */
  }
}
