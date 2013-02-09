/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.model;

import org.dom4j.DocumentFactory;
import org.dom4j.io.DispatchHandler;
import org.dom4j.io.SAXContentHandler;
import org.dom4j.io.SAXReader;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import dongfang.XMLConstants;
import dongfang.xsltools.diagnostics.AttributeParseLocation;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.util.ListStack;

/**
 * Using this class requires some widening of access of some fields in some
 * dom4j classes. No big deal to do it, though (and dom4j ought to have that
 * information open to subclasses anyway, IMO.) Alternatively, DELETE this
 * class, and alter ModelConfiguration to no longer support parse location.
 * Then, is should compile ok without modifications to dom4j.
 * 
 * @author dongfang
 */
public class ParseLocationStylesheetModuleFactory extends
    NoParseLocationStylesheetModuleFactory {

  class ParseLocationErrorHandler implements ErrorHandler {
    ErrorReporter cesspool;

    ParseLocationErrorHandler(ErrorReporter cesspool) {
      this.cesspool = cesspool;
    }

    public void error(SAXParseException exception) throws SAXException {
      try {
        cesspool.reportError(exception.getSystemId(),
            exception.getLineNumber(), exception.getColumnNumber(), exception);
      } catch (XSLToolsException wrapped) {
        throw exception;
      }
    }

    public void fatalError(SAXParseException exception) throws SAXException {
      try {
        cesspool.reportError(exception.getSystemId(),
            exception.getLineNumber(), exception.getColumnNumber(), exception);
      } catch (XSLToolsException wrapped) {
        throw exception;
      }
    }

    public void warning(SAXParseException exception) throws SAXException {
      try {
        cesspool.reportError(exception.getSystemId(),
            exception.getLineNumber(), exception.getColumnNumber(), exception);
      } catch (XSLToolsException wrapped) {
        throw exception;
      }
    }
  }

  @Override
  ErrorHandler getErrorHandler(ErrorReporter cesspool) {
    return new ParseLocationErrorHandler(cesspool);
  }

  @Override
  SAXReader makeSAXReader(boolean validating) {
    return new ParseLocationSAXReader(ModelConfiguration.current
        .getDocumentFactory(), validating);
  }

  class ParseLocationSAXReader extends SAXReader {
    @Override
	protected SAXContentHandler createContentHandler(XMLReader reader) {
      return new ParseLocationSAXContentHandler(ParseLocationSAXReader.this
          .getDocumentFactory(), dispatchHandler);
    }

    public ParseLocationSAXReader(boolean validating) {
      super(validating);
    }

    public ParseLocationSAXReader(DocumentFactory fac, boolean isValidating) {
      super(fac, isValidating);
    }

    @Override
    public DocumentFactory getDocumentFactory() {
      return ModelConfiguration.current.getDocumentFactory();
    }
  }

  class ParseLocationSAXContentHandler extends SAXContentHandler {
    private int currentLine;

    private int currentColumn;

    private boolean usingParseLocationElements = ModelConfiguration.current
        .getParseLocationConfig() == ModelConfiguration.ParseLocationConfig.PARSELOCATIONS_IN_SPECIAL_ELEMENT_CLASS;

    private boolean addParseLocationAttributes = ModelConfiguration.current
        .getParseLocationConfig() == ModelConfiguration.ParseLocationConfig.PARSELOCATIONS_AS_ATTRIBUTES;

    private ListStack<AttributeParseLocation> ppStack = addParseLocationAttributes ? new ListStack<AttributeParseLocation>()
        : null;

    public ParseLocationSAXContentHandler(DocumentFactory fac,
        DispatchHandler dh) {
      super(fac, dh);
    }

    @Override
    public void characters(char[] ch, int start, int end) throws SAXException {
      currentLine = locator.getLineNumber();
      currentColumn = locator.getColumnNumber();
      super.characters(ch, start, end);
    }

    @Override
    public void comment(char[] ch, int start, int end) throws SAXException {
      currentLine = locator.getLineNumber();
      currentColumn = locator.getColumnNumber();
      super.comment(ch, start, end);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
      currentLine = locator.getLineNumber();
      currentColumn = locator.getColumnNumber();
      super.endPrefixMapping(prefix);
    }

    @Override
    public void processingInstruction(String target, String data)
        throws SAXException {
      currentLine = locator.getLineNumber();
      currentColumn = locator.getColumnNumber();
      super.processingInstruction(target, data);
    }

    @Override
    public void startCDATA() throws SAXException {
      currentLine = locator.getLineNumber();
      currentColumn = locator.getColumnNumber();
      super.startCDATA();
    }

    @Override
    public void startDocument() throws SAXException {
      currentLine = locator.getLineNumber();
      currentColumn = locator.getColumnNumber();
      super.startDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
      currentLine = locator.getLineNumber();
      currentColumn = locator.getColumnNumber();
      super.startPrefixMapping(prefix, uri);
    }

    @Override
    public void startElement(String namespaceURI, String localName,
        String qualifiedName, Attributes attributes) throws SAXException {
      super.startElement(namespaceURI, localName, qualifiedName, attributes);

      // Parse locations in attributes
      // TODO: Method configurable.
      if (addParseLocationAttributes) {
        AttributeParseLocation pp =
        // moduleGenerated.newParseLocation(elementIdentifier); new
        new AttributeParseLocation(currentLine, currentColumn,
            currentLine = locator.getLineNumber(), currentColumn = locator
                .getColumnNumber());
        ppStack.push(pp);
      }
      // parse location in elements
      ParseLocationElement ple = (ParseLocationElement) currentElement;
      ple.elementStartTagBeginningLine = currentLine;
      ple.elementStartTagBeginningColumn = currentColumn;
      ple.elementStartTagEndLine = (currentLine = locator.getLineNumber());
      ple.elementStartTagEndColumn = (currentColumn = locator.getColumnNumber());
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {
      if (usingParseLocationElements) {
        ParseLocationElement ple = (ParseLocationElement) currentElement;
        ple.elementEndTagEndLine = currentLine = locator.getLineNumber();
        ple.elementEndTagEndColumn = currentColumn = locator.getColumnNumber();
      } else if (addParseLocationAttributes) {
        AttributeParseLocation pp = ppStack.pop();
        pp.setElementEndTagEndLine(currentLine = locator.getLineNumber());
        pp.setElementEndTagEndColumn(currentColumn = locator.getColumnNumber());
        currentElement.addAttribute(
            XMLConstants.PARSELOCATION_DECORATION_QNAME, pp.toString());
      }
      super.endElement(namespaceURI, localName, qName);
    }

    @Override
    public void endCDATA() throws SAXException {
      currentLine = locator.getLineNumber();
      currentColumn = locator.getColumnNumber();
      super.endCDATA();
    }
  }
}