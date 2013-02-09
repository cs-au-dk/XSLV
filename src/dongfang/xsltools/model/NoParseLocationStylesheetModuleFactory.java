/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.SAXReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import dongfang.XMLConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.resolver.ResolutionContext;

/**
 * A stylesheet module factory that uses the plain old Dom4J document builder,
 * not doing any parse position but still adds element IDs
 * 
 * @author dongfang
 */
public class NoParseLocationStylesheetModuleFactory implements
    StylesheetModuleFactory {

  /*
   * public static ParseLocationStylesheetModuleFactory getInstance() { return
   * instance; }
   */

  class NoParseLocationErrorHandler implements ErrorHandler {
    ErrorReporter cesspool;

    NoParseLocationErrorHandler(ErrorReporter cesspool) {
      this.cesspool = cesspool;
    }

    public void error(SAXParseException exception) throws SAXException {
      XSLToolsException wrapper = new XSLToolsException(exception);
      try {
        cesspool.reportError(wrapper);
      } catch (XSLToolsException wrapped) {
        throw exception;
      }
    }

    public void fatalError(SAXParseException exception) throws SAXException {
      XSLToolsException wrapper = new XSLToolsException(exception);
      try {
        cesspool.reportError(wrapper);
      } catch (XSLToolsException wrapped) {
        throw exception;
      }
    }

    public void warning(SAXParseException exception) throws SAXException {
      XSLToolsException wrapper = new XSLToolsException(exception);
      try {
        cesspool.reportError(wrapper);
      } catch (XSLToolsException wrapped) {
        throw exception;
      }
    }
  }

  SAXReader makeSAXReader(boolean validate) {
    return new SAXReader(validate);
  }

  SAXReader makeSAXReader(Object expectedKind) throws SAXException {

    String schemaLang = null;
    String schemaLocationHint = null;

    boolean willValidate;

    if (expectedKind == ResolutionContext.MSG_XSL_ANY) {
      schemaLang = "http://www.w3.org/2001/XMLSchema";
      schemaLocationHint = "file:resources/schema/schema-for-xslt20.xsd";
      willValidate = ModelConfiguration.current.schemaValidateXSLTSource();
    } else if (expectedKind == ResolutionContext.MSG_XSD) {
      schemaLang = "http://www.w3.org/2001/XMLSchema";
      schemaLocationHint = "file:resources/schema/XMLSchema.xsd";
      willValidate = ModelConfiguration.current.schemaValidateXMLSchema();
    } else
      willValidate = false;

    SAXReader reader = makeSAXReader(willValidate);

    reader.setFeature("http://xml.org/sax/features/namespaces", true);
    reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);

    reader.setFeature(
        "http://apache.org/xml/features/continue-after-fatal-error",
        ModelConfiguration.current.continueAfterXSLTParseOrValidationError());

    reader.setMergeAdjacentText(ModelConfiguration.current
        .parserMergesAdjacentTextNodes());

    reader.setDefaultHandler(null);

    reader.setValidation(willValidate);
    reader.setFeature("http://xml.org/sax/features/validation", willValidate);

    reader.setFeature("http://apache.org/xml/features/validation/schema",
        willValidate);

    reader
        .setFeature(
            "http://apache.org/xml/features/validation/schema-full-checking",
            false);

    if (willValidate) {
      reader.setProperty(
          "http://java.sun.com/xml/jaxp/properties/schemaLanguage", schemaLang);
      reader.setProperty(
          "http://java.sun.com/xml/jaxp/properties/schemaSource",
          schemaLocationHint);

      // We have to perform a dummy load with the reader, in order to get
      // schemas
      // loaded before the (eeewww) custom entity resolver is installed..

      // final EntityResolver sniff = reader.getEntityResolver();

      /*
       * reader.setEntityResolver(new EntityResolver() { public InputSource
       * resolveEntity(String publicId, String systemId) throws SAXException,
       * IOException { System.err.println("Resolving: " + systemId); return
       * sniff.resolveEntity(publicId, systemId); } });
       * 
       * try { reader.read("file:resources/schema/tiny.xml"); } catch (Exception
       * ex) { // it's just a dummy call. }
       */
    }
    return reader;
  }

  private static Map<Object, SAXReader> readerCache = new HashMap<Object, SAXReader>();

  ErrorHandler getErrorHandler(ErrorReporter cesspool) {
    return new NoParseLocationErrorHandler(cesspool);
  }

  protected SAXReader getSAXReader(DocumentFactory fac, Object expectedKind,
      final ErrorReporter cesspool) throws SAXException {

    SAXReader reader = null;

    if (ModelConfiguration.current.threadUnsafelyReuseReaders())
      reader = readerCache.get(expectedKind);

    if (reader == null) {
      reader = makeSAXReader(expectedKind);
      if (ModelConfiguration.current.threadUnsafelyReuseReaders())
        readerCache.put(expectedKind, reader);
    }

    reader.setErrorHandler(getErrorHandler(cesspool));

    return reader;
  }

  public Document read (
      final String systemId, 
      String userExplanation, 
      int humanKey,
      Object expectedKind, 
      final ResolutionContext context,
      final ErrorReporter cesspool) throws XSLToolsLoadException {
    try {
      DocumentFactory fac = ModelConfiguration.current.getDocumentFactory();
      SAXReader reader = getSAXReader(fac, expectedKind, cesspool);

      /*
       * final EntityResolver emergency = reader.getEntityResolver();
       * reader.setEntityResolver(new EntityResolver() { public InputSource
       * resolveEntity(String publicId, String isystemId) throws SAXException,
       * IOException { if (isystemId == null) throw new IOException( "Me
       * confused. Can only do non null system IDs."); // try { //URI mine = new
       * URI(systemId); //int p = isystemId.lastIndexOf('/'); //isystemId =
       * isystemId.substring(p + 1); //URI result = mine.resolve(isystemId);
       * //System.err.println("Now slurping: " + result); //return
       * context.resolveStream(result.toString(), //
       * ResolutionContext.HUMAN_UNKNOWN_RESOURCE); InputSource is =
       * emergency.resolveEntity(publicId, isystemId); return is; // } catch
       * (URISyntaxException ex) { // throw new IOException(ex.getMessage()); // } }
       * });
       */

      InputSource is = context.resolveStream(systemId, userExplanation, humanKey);

/*
      if (systemId.contains("xlink.xsd")) {
        Reader r = is.getCharacterStream();
        int c;
        while ((c = r.read()) >= 0) {
          System.out.print((char) c);
        }
        r.close();
      }
  */
      
      Document doc = reader.read(is);

      if (DiagnosticsConfiguration.current.traceResourceLoading())
        System.out.println("Loaded resource: " + systemId);

      return doc;
    } catch (DocumentException e) {
      throw new XSLToolsLoadException(e);
    } catch (SAXException e) {
      throw new XSLToolsLoadException(e);
    } catch (IOException e) {
      throw new XSLToolsLoadException(e);
    }
  }

  void loadStylesheetModule(StylesheetModule container, String systemId,
      String userExplanation, ResolutionContext context, ErrorReporter cesspool)
      throws XSLToolsLoadException {

    Document doc = null;

    try {
      doc = read(
          systemId,
          //ResolutionContext.MSG_XSL_PRINCIPAL,
          userExplanation,
          ResolutionContext.STYLESHEET_PRINCIPAL_MODULE_IDENTIFIER_KEY,
          ResolutionContext.MSG_XSL_ANY, context, cesspool);
    } catch (XSLToolsLoadException ex) {
      try {
        cesspool.reportError(ex);
      } catch (XSLToolsException ex2) {
        throw (XSLToolsLoadException) ex2;
      }
    }
    // insert validator namespace declaration (needed by all annotation added!)

    if (doc != null && doc.getRootElement() != null) {

      Namespace testNs;

      // this stupid hack could be abolished again...
      if ((testNs = doc.getRootElement().getNamespaceForPrefix(
          XMLConstants.DONGFANG_PREFIX)) != null
          && !testNs.getURI().equals(XMLConstants.DONGFANG_URI))

        throw new XSLToolsLoadException(
            "The "
                + XMLConstants.DONGFANG_PREFIX
                + " prefix is already used in the document, and not referring to the validator namespace ("
                + XMLConstants.DONGFANG_URI + "). Try use a different prefix.");

    } else {
      // this stupid hack could be abolished again...
      DocumentFactory fac = new DocumentFactory();// FactoryFactory.instance.getDocumentFactory();
      Element r = fac.createElement(XSLConstants.ELEM_TRANSFORM_QNAME);
      r.addComment("Generated automatically, as the input document was unparseable");
      doc = fac.createDocument(r);
    }

    doc.getRootElement().add(XMLConstants.DONGFANG_NAMESPACE);
    doc.getRootElement().addAttribute(XMLConstants.ORIGINAL_URI_ATT_QNAME,
        systemId.toString());

    container.setDocument(doc, StylesheetModule.ORIGINAL);
    container.reassignOriginalIDs();
    container.setDocument((Document) container.getDocument(
        StylesheetModule.ORIGINAL).clone(), StylesheetModule.CORE);

    container.reassignCoreIDs(container
        .getDocument(StylesheetModule.CORE));
  }

  public StylesheetModule createStylesheetModule(String systemId,
      String userExplanation, ResolutionContext context, int groupnumber, int modulenumber,
      ErrorReporter cesspool) throws XSLToolsLoadException {
    StylesheetModule m = new StylesheetModule(systemId, null, null,
        groupnumber, modulenumber);
    loadStylesheetModule(m, systemId, userExplanation, context, cesspool);
    return m;
  }

  public StylesheetLevel createStylesheetLevel(String systemId,
      String userExplanation, ResolutionContext context, int groupnumber, ErrorReporter cesspool)
      throws XSLToolsLoadException {
    StylesheetModule m = createStylesheetModule(systemId, userExplanation, context, groupnumber,
        0, cesspool);
    StylesheetLevel group = new StylesheetLevel();
    group.addContent(m);
    return group;
  }

  public Stylesheet createStylesheet(String systemId,
      String userExplanation, ResolutionContext context, ErrorReporter cesspool)
      throws XSLToolsLoadException {
    Stylesheet s = new Stylesheet();
    s.setPrincipalGroup(createStylesheetLevel(systemId, userExplanation, context, 0, cesspool));
    return s;
  }
}