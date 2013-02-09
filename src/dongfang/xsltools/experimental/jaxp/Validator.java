/*
 * dongfang M. Sc. Thesis
 * Created on 2005-07-29
 */
package dongfang.xsltools.experimental.jaxp;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.resolver.URLResolutionContext;
import dongfang.xsltools.util.Util;

/**
 * @author snk
 */
public class Validator {

  boolean errors = false;

  class ReportingDefaultHandler extends DefaultHandler {

    @Override
	public void error(SAXParseException e) throws SAXException {
      super.error(e);
      System.err.println(e);
      errors = true;
    }

    @Override
	public void fatalError(SAXParseException e) throws SAXException {
      super.fatalError(e);
      System.err.println(e);
      errors = true;
    }

    @Override
	public void warning(SAXParseException e) throws SAXException {
      super.warning(e);
      System.err.println("Warning: " + e);
    }
  }

  boolean validate(InputSource input, Object schemaLocationHint)
      throws ParserConfigurationException, SAXException, IOException {

    SAXParserFactory spfactory = SAXParserFactory.newInstance();
    spfactory.setNamespaceAware(true);
    spfactory.setValidating(true);

    SAXParser saxparser = spfactory.newSAXParser();

    // write your handler for processing events and handling error
    DefaultHandler handler = new ReportingDefaultHandler();

    saxparser.setProperty(
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
        "http://www.w3.org/2001/XMLSchema");

    if (schemaLocationHint != null)
      saxparser.setProperty(
          "http://java.sun.com/xml/jaxp/properties/schemaSource",
          schemaLocationHint);

    /*
     * java.lang.String that points to the URI of the schema java.io.InputStream
     * with the contents of the schema org.xml.sax.InputSource java.io.File an
     * array of java.lang.Object with the contents being one of the types
     * defined above.
     */

    // parse the XML and report events and errors (if any) to the handler
    saxparser.parse(input, handler);

    return !errors;
  }

  public static void main(String[] args) throws Exception {
    ResolutionContext connie = new URLResolutionContext();
    String inputname = Util.toUrlString(args[0]);
    InputSource input = connie.resolveStream(inputname, "fims", (short) -1);
    InputSource schemaLocation = null;
    if (args.length > 1) {
      String outputname = Util.toUrlString(args[1]);
      schemaLocation = connie.resolveStream(outputname, "fims", (short) -1);
    }
    System.out.println(new Validator().validate(input, schemaLocation));
  }
}