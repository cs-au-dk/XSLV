package dongfang.xsltools.validation;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

public class ResultFeedbackValidationContext extends ValidationContextDecoratorBase  {
  final ResultListener rl;

  public ResultFeedbackValidationContext(ValidationContext vcon, ResultListener rl) {
    setValidationContext(vcon);
    this.rl = rl;
  }

  @Override
  public ControlFlowGraph getControlFlowGraph(ErrorReporter cesspool) throws XSLToolsException {
    ControlFlowGraph xcfg = vcon.getControlFlowGraph(cesspool);
    rl.setXcfg(xcfg);
    return xcfg;
  }

  @Override
  public SingleTypeXMLClass getInputType(String documentIdentifier) throws XSLToolsLoadException {
    SingleTypeXMLClass type = vcon.getInputType(documentIdentifier);
    rl.setInputType(type);
    return type;
  }

  @Override
  public XMLGraph getOutputType(String systemId) throws XSLToolsLoadException {
    XMLGraph type = vcon.getOutputType(systemId);
    rl.setOutputType(type);
    return type;
  }

  @Override
  public Stylesheet getStylesheet(ErrorReporter cesspool) throws XSLToolsException {
    Stylesheet ss = vcon.getStylesheet(cesspool);
    rl.setApproxSimplifiedStylesheet(ss);
    return ss;
  }

  @Override
  public void validate(ErrorReporter cesspool, ValidationResult result) throws XSLToolsException {
    vcon.validate(cesspool, result);
    rl.setValidationResult(result);
  }
}
