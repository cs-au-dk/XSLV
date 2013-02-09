package dongfang.xsltools.validation;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

public class ValidationRunSaverBean extends ValidationRunDecoratorBase {
  Stylesheet semPreservedStylesheet;
  
  Stylesheet approximatedStylesheet;

  SingleTypeXMLClass inputType;

  XMLGraph outputType;

  ControlFlowGraph xcfg;

  XMLGraph controlFlowSG;

  ValidationResult result;

  PerformanceLogger pa;

  ValidationRunSaverBean(ValidationRun decorated) {
    super(decorated);
  }

  @Override
public void setControlFlowSG(XMLGraph controlFlowSG) {
    super.setControlFlowSG(controlFlowSG);
    this.controlFlowSG = controlFlowSG;
  }

  public SingleTypeXMLClass getInputType() {
    return inputType;
  }

  @Override
public void setInputType(SingleTypeXMLClass inputType) {
    super.setInputType(inputType);
    this.inputType = inputType;
  }

  public XMLGraph getOutputType() {
    return outputType;
  }

  @Override
public void setOutputType(XMLGraph outputType) {
    super.setOutputType(outputType);
    this.outputType = outputType;
  }

  @Override
public ValidationResult getValidationResult() {
    return result;
  }

  @Override
public void setValidationResult(ValidationResult result) {
    super.setValidationResult(result);
    this.result = result;
  }

  public Stylesheet getSemanticsPreservedStylesheet() {
    return semPreservedStylesheet;
  }

  public Stylesheet getApproximatedStylesheet() {
    return approximatedStylesheet;
  }

  @Override
public void setSemPreservingSimplifiedStylesheet(Stylesheet ss) {
    super.setSemPreservingSimplifiedStylesheet(ss);
    this.semPreservedStylesheet = ss;
  }

  @Override
public void setApproxSimplifiedStylesheet(Stylesheet ss) {
    super.setApproxSimplifiedStylesheet(ss);
    this.approximatedStylesheet = ss;
  }

  public ControlFlowGraph getXcfg() {
    return xcfg;
  }

  @Override
public void setXcfg(ControlFlowGraph xcfg) {
    super.setXcfg(xcfg);
    this.xcfg = xcfg;
  }

  @Override
public void setPerformanceLogger(PerformanceLogger pa) {
    super.setPerformanceLogger(pa);
    this.pa = pa;
  }
}
