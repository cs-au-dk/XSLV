package dongfang.xsltools.validation;

import java.io.IOException;

import org.xml.sax.InputSource;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

public class TimerValidationContext extends ValidationContextDecoratorBase {
  final PerformanceLogger pa;

  public TimerValidationContext(ValidationContext vcon, PerformanceLogger pa) {
    super(vcon);
    this.pa = pa;
  }

  @Override
  public void earlyStreamRequest(String systemId, String userExplanation, int humanKey) throws IOException {
    vcon.earlyStreamRequest(systemId, userExplanation, humanKey);
  }

  @Override
  public void earlyStringRequest(String id, String user, String none, int humanReadableKey) throws IOException {
    vcon.earlyStringRequest(id, user, none, humanReadableKey);
  }

  @Override
  public String getNamespaceURIIdentifier(String s, short io) {
    return vcon.getNamespaceURIIdentifier(s, io);
  }

  @Override
  public String getRootElementNameIdentifier(String s, short io) {
    return vcon.getRootElementNameIdentifier(s, io);
  }

  @Override
  public String getSchemaIdentifier(String documentIdentifier, short io) {
    return vcon.getSchemaIdentifier(documentIdentifier, io);
  }

  @Override
  public String getStylesheetIdentifier() {
    return vcon.getStylesheetIdentifier();
  }

  @Override
  public InputSource resolveStream(String systemId, String userExplanation, int humanKey) throws IOException {
    return vcon.resolveStream(systemId, userExplanation, humanKey);
  }

  @Override
  public String resolveString(String systemId, String user, String none, int humanKey) throws IOException {
    return vcon.resolveString(systemId, user, none, humanKey);
  }

  @Override
  public Stylesheet getStylesheet(ErrorReporter cesspool) throws XSLToolsException {
    try {
      pa.startTimer("getStylesheet", "root");
      return vcon.getStylesheet(cesspool);
    } finally {
      pa.stopTimer("getStylesheet", "root");
    }
  }

  @Override
  public SingleTypeXMLClass getInputType(String documentIdentifier) throws XSLToolsLoadException {
    try {
      pa.startTimer("getInputType", "root");
      return vcon.getInputType(documentIdentifier);
    } finally {
      pa.stopTimer("getInputType", "root");
    }
  }

  @Override
  public ControlFlowGraph getControlFlowGraph(ErrorReporter cesspool) throws XSLToolsException {
    try {
      pa.startTimer("getControlFlowGraph", "root");
      return vcon.getControlFlowGraph(cesspool);
    } finally {
      pa.stopTimer("getControlFlowGraph", "root");
    }
  }

  @Override
  public XMLGraph getOutputType(String systemId) throws XSLToolsLoadException {
    try {
      pa.startTimer("getOutputType", "root");
      return vcon.getOutputType(systemId);
    } finally {
      pa.stopTimer("getOutputType", "root");
    }
  }

  @Override
  public void validate(ErrorReporter cesspool, ValidationResult result) throws XSLToolsException {
    try {
      pa.startTimer("validate", "root");
      vcon.validate(cesspool, result);
    } finally {
      pa.stopTimer("validate", "root");
    }
  }
}
