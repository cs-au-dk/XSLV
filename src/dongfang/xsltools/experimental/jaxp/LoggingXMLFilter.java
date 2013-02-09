/*
 * dongfang M. Sc. Thesis
 * Created on 04-02-2005
 */
package dongfang.xsltools.experimental.jaxp;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author dongfang
 * 
 */
public class LoggingXMLFilter extends XMLFilterImpl {

  protected LoggingXMLFilter(XMLReader parent) {
    super(parent);
  }

  static Logger logger = Logger.global;// Logger.getLogger("fido");
  static {
    logger.setLevel(Level.ALL);
    Handler h = new StreamHandler(System.out, new SimpleFormatter());
    h.setLevel(Level.ALL);
    logger.addHandler(h);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#characters(char[], int, int)
   */
  @Override
public void characters(char[] ch, int start, int length) throws SAXException {
    logger.fine("<!--characters start-->" + new String(ch, start, length)
        + "<!--end-->");
    super.characters(ch, start, length);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#endDocument()
   */
  @Override
public void endDocument() throws SAXException {
    logger.info("<!--end of doc-->");
    super.endDocument();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
public void endElement(String uri, String localName, String qName)
      throws SAXException {
    logger.fine("</" + "uri=\"" + uri + "\" localName=\"" + localName
        + "\" qName=\"" + qName + "\"");
    super.endElement(uri, localName, qName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
   */
  @Override
public void endPrefixMapping(String prefix) throws SAXException {
    logger.fine("<!--endPrefixMapping prefix=\"" + prefix + "\"->");
    super.endPrefixMapping(prefix);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
   */
  @Override
public void error(SAXParseException e) throws SAXException {
    logger.warning("<!-- error exception=\"" + e.toString() + "\"-->");
    super.error(e);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   */
  @Override
public void fatalError(SAXParseException e) throws SAXException {
    logger.warning("<!-- fatalerror exception=\"" + e.toString() + "\"-->");
    super.fatalError(e);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
   */
  @Override
public void ignorableWhitespace(char[] ch, int start, int length)
      throws SAXException {
    logger.fine("<!--ignorableWhitespace-->" + new String(ch, start, length));
    super.ignorableWhitespace(ch, start, length);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.DTDHandler#notationDecl(java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
public void notationDecl(String name, String publicId, String systemId)
      throws SAXException {
    logger.fine("<!-- <!NOTATION " + name + " " + publicId + " " + systemId
        + ">-->");
    super.notationDecl(name, publicId, systemId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
   *      java.lang.String)
   */
  @Override
public void processingInstruction(String target, String data)
      throws SAXException {
    logger.fine("<?" + target + " " + data + "?>");
    super.processingInstruction(target, data);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
   *      java.lang.String)
   */
  @Override
public InputSource resolveEntity(String publicId, String systemId)
      throws SAXException {
    try {
      InputSource is = super.resolveEntity(publicId, systemId);
      logger.fine("<!-- resolve publicId=\"" + publicId + "\" systemId=\""
          + systemId + "\" resolvedTo=\"" + is + "\"-->");
      return is;
    } catch (IOException e) {
      throw new SAXException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
   */
  @Override
public void setDocumentLocator(Locator locator) {
    logger.fine("<!-- setDocumentLocator locator=\"" + locator + "\"-->");
    super.setDocumentLocator(locator);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
   */
  @Override
public void skippedEntity(String name) throws SAXException {
    logger.fine("<!-- skippedEntity locator=\"" + name + "\"-->");
    super.skippedEntity(name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#startDocument()
   */
  @Override
public void startDocument() throws SAXException {
    logger.info("<!--start of doc-->");
    super.startDocument();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
   *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
   */
  @Override
public void startElement(String uri, String localName, String qName,
      Attributes attributes) throws SAXException {
    super.startElement(uri, localName, qName, attributes);
    logger.fine("<" + "uri=\"" + uri + "\" localName=\"" + localName
        + "\" qName=\"" + qName + "\"");
    logger.fine("attribs: " + attributes);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
   *      java.lang.String)
   */
  @Override
public void startPrefixMapping(String prefix, String uri) throws SAXException {
    logger.info("<!--startPrefixMapping prefix=\"" + prefix + "\" uri=\"" + uri
        + "\"-->");
    super.startPrefixMapping(prefix, uri);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.DTDHandler#unparsedEntityDecl(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
public void unparsedEntityDecl(String name, String publicId, String systemId,
      String notationName) throws SAXException {
    logger.info("<!--unparsedEntityDecl name=\"" + name + "\" publicId=\""
        + publicId + "\" systemId=\"" + systemId + "\"-->");
    super.unparsedEntityDecl(name, publicId, systemId, notationName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
   */
  @Override
public void warning(SAXParseException e) throws SAXException {
    logger.warning("<!-- warning exception=\"" + e + "\"-->");
    super.warning(e);
  }

  public static void main(String[] args) throws SAXException, IOException,
      ParserConfigurationException {
    // boolean validate = false;
    // boolean namespaceAware = false;

    // XMLReader parser = XMLReaderFactory.createXMLReader("orx.xml.sax.");
    SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    SAXParser parser = parserFactory.newSAXParser();
    XMLReader reader = parser.getXMLReader();

    reader.setFeature("http://xml.org/sax/features/namespaces", true);
    reader.setFeature("http://xml.org/sax/features/namespace-prefixes", false);

    LoggingXMLFilter filt = new LoggingXMLFilter(reader);

    DefaultHandler dummy = new DefaultHandler();
    filt.setContentHandler(dummy);
    // filt.setDocumentLocator(dummy);
    filt.setDTDHandler(dummy);
    filt.setEntityResolver(dummy);
    filt.setErrorHandler(dummy);

    filt.setParent(reader);

    InputSource src = new InputSource(args[0]);

    reader.setContentHandler(filt);
    reader.setDTDHandler(filt);
    reader.setErrorHandler(filt);

    reader.parse(src);
  }
}