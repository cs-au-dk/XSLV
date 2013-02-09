package dongfang.xsltools.xmlclass;

import java.io.BufferedReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.resolver.ResolutionContext;

public class SchemaKindSniffer {
  static class SnifferHandler extends DefaultHandler {
    String result = ResolutionContext.
    SystemInterfaceStrings[ResolutionContext.UNKNOWN_RESOURCE];

    @Override
    public void error(SAXParseException e) throws SAXException {
      result = ResolutionContext.MSG_FAIL;
      throw e;
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      result = ResolutionContext.MSG_FAIL;
      throw e;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
        Attributes attributes) throws SAXException {
      if ("http://www.w3.org/2001/XMLSchema".equals(uri)) {
        result = ResolutionContext.MSG_XSD;
      } else if ("http://relaxng.org/ns/structure/1.0".equals(uri)) {
        result = ResolutionContext.MSG_RNG;
      } else
        result = ResolutionContext.
        SystemInterfaceStrings[ResolutionContext.UNKNOWN_RESOURCE];
      throw new SAXException("stop!");
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
      super.warning(e);
    }
  }

  public static String sniffSchemaKind(ValidationContext context,
      String schemaId, short io) {
    String result = ResolutionContext.
    SystemInterfaceStrings[ResolutionContext.UNKNOWN_RESOURCE];
    SnifferHandler handler = new SnifferHandler();
    try {
      InputSource s = context
          .resolveStream(
              schemaId,
              ResolutionContext.MSG_UNKNOWN_KIND_SCHEMA,
              //ResolutionContext.HUMAN_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY);
              io);
      SAXParserFactory f = SAXParserFactory.newInstance();
      f.setValidating(false);
      f.setNamespaceAware(true);
      SAXParser sp = f.newSAXParser();
      sp.parse(s, handler);
    } catch (IOException ex) {
      result = ResolutionContext.MSG_FAIL;
    } catch (SAXException ex) {
      // normal termination...
      result = handler.result;
      if (result == ResolutionContext.MSG_FAIL) {
        // give DTD a chance
        try {
          InputSource s = context
              .resolveStream(
                  schemaId,
                  "A schema document: DTD, XML Schema or Restricted Relax NG are accepted",
                  ResolutionContext.SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY);
          BufferedReader r = new BufferedReader(s.getCharacterStream());
          while (result == ResolutionContext.MSG_FAIL && r.ready()) {
            String line = r.readLine();
            if (line != null
                && (line.contains("<!ELEMENT") || line.contains("<!ATTLIST")|| line.contains("<!ENTITY"))) {
              result = ResolutionContext.MSG_DTD;
              break;
            } else if (line == null)
              break;
          }
        } catch (IOException ex2) {
          result = ResolutionContext.MSG_FAIL;
        }
      }
    } catch (ParserConfigurationException ex) {
      throw new AssertionError("This should never happen");
    } catch (Throwable any) {
      result = ResolutionContext.MSG_FAIL;
    }
    return result;
  }
}
