package dongfang.xsltools.validation;

import java.io.IOException;

import org.xml.sax.InputSource;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

public abstract class ValidationContextDecoratorBase implements ValidationContext {
  
  protected ValidationContext vcon;
  
  protected ValidationContextDecoratorBase() {}
  
  protected ValidationContextDecoratorBase(ValidationContext vcon) {
    setValidationContext(vcon);
  }
  
  protected void setValidationContext(ValidationContext vcon) {
    if (vcon == this)
      throw new RuntimeException("Are you serious, setting the delegate backend to the delegate itself ???");
    this.vcon = vcon;
  }

  public void earlyStreamRequest(String systemId, String userExplanation, int humanKey) throws IOException {
    vcon.earlyStreamRequest(systemId, userExplanation, humanKey);
  }

  public void earlyStringRequest(String id, String user, String none, int humanReadableKey) throws IOException {
    vcon.earlyStringRequest(id, user, none, humanReadableKey);
  }

  public ControlFlowGraph getControlFlowGraph(ErrorReporter cesspool) throws XSLToolsException {
    return vcon.getControlFlowGraph(cesspool);
  }

  public XMLGraph getControlFlowXMLGraph(ErrorReporter cesspool) throws XSLToolsException {
    return vcon.getControlFlowXMLGraph(cesspool);
  }

  public SingleTypeXMLClass getInputType(String documentIdentifier) throws XSLToolsLoadException {
    return vcon.getInputType(documentIdentifier);
  }

  public String getNamespaceURIIdentifier(String s, short io) {
    return vcon.getNamespaceURIIdentifier(s, io);
  }

  public XMLGraph getOutputType(String systemId) throws XSLToolsLoadException {
    return vcon.getOutputType(systemId);
  }

  public String getRootElementNameIdentifier(String s, short io) {
    return vcon.getRootElementNameIdentifier(s, io);
  }

  public String getSchemaIdentifier(String documentIdentifier, short io) {
    return vcon.getSchemaIdentifier(documentIdentifier, io);
  }

  public Stylesheet getStylesheet(ErrorReporter cesspool) throws XSLToolsException {
    return vcon.getStylesheet(cesspool);
  }

  public String getStylesheetIdentifier() {
    return vcon.getStylesheetIdentifier();
  }

  public InputSource resolveStream(String systemId, String userExplanation, int humanKey) throws IOException {
    return vcon.resolveStream(systemId, userExplanation, humanKey);
  }

  public String resolveString(String systemId, String user, String none, int humanKey) throws IOException {
    return vcon.resolveString(systemId, user, none, humanKey);
  }

  public void validate(ErrorReporter cesspool, ValidationResult result) throws XSLToolsException {
    vcon.validate(cesspool, result);
  }
  
  public void setResolver(ResolutionContext resolver) {
    vcon.setResolver(resolver);
  }
  
  public void pushMessage(String target, String message) {
    vcon.pushMessage(target, message);
  }
  
  public void reset() {
    vcon.reset();
  }
}
