/*
 * Created on Feb 20, 2005
 */
package dongfang.xsltools.experimental.jaxp;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author dongfang
 */
public class XSLSAXBuilderXMLFilter extends XMLFilterImpl {

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#endDocument()
   */
  @Override
public void endDocument() throws SAXException {
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
    super.endElement(uri, localName, qName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
   */
  @Override
public void endPrefixMapping(String prefix) throws SAXException {
    super.endPrefixMapping(prefix);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.ContentHandler#startDocument()
   */
  @Override
public void startDocument() throws SAXException {
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
      Attributes atts) throws SAXException {
    super.startElement(uri, localName, qName, atts);
  }
}