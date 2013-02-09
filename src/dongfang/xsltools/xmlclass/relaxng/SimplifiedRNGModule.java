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

import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.resolver.ResolutionContext;

public class SimplifiedRNGModule {
  private static final String prefix = "resources/xslt";

  private static final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory
      .newInstance();

  private static final String[] stylesheetNames = { "step7.2.xsl",
      "step7.3.xsl", "step7.4.xsl", "step7.5.xsl", "step7.7.xsl",
      "step7.8.xsl", "step7.9.xsl", "step7.10.xsl", "step7.11.xsl",
      "step7.12.xsl", "step7.13.xsl", "step7.14.xsl", "step7.15.xsl",
      "step7.16.xsl", "step7.18.xsl", "step7.19.xsl", "step7.20.xsl",
      "step7.22.xsl" };

  private static XMLFilter combinedTransform;
  static {
    try {
      combinedTransform = buildTransformer(stylesheetNames, prefix);
    } catch (Exception ex) {
      throw new AssertionError("Error in init. Should never happen.");
    }
  }

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

  public static Document getSimplifiedRelaxNG(ResolutionContext context,
      String systemId) throws XSLToolsLoadException {
    // Transformer trafo = stf.newTransformer(new StreamSource("tmp/id.xsl"));
    // XMLReader rdr = XMLReaderFactory.createXMLReader();

    // rdr.setFeature("http://xml.org/sax/features/namespaces", true);
    // rdr.setFeature("http://xml.org/sax/features/namespace-prefixes", false);

    try {
      InputSource source = context.resolveStream(systemId,
          "Relax NG module",
          ResolutionContext.RELAXNG_MODULE_IDENTIFIER_KEY);

      DocumentResult res = new DocumentResult();
      // trafo.transform(source, res);
      trafo.transform(new SAXSource(combinedTransform, source), res);
      return res.getDocument();
    } catch (IOException ex) {
      throw new XSLToolsLoadException(ex);
    } catch (TransformerException ex) {
      throw new XSLToolsLoadException(ex);
    }
  }
}
