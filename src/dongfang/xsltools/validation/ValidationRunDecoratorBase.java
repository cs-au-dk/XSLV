package dongfang.xsltools.validation;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

/**
 * A unviersal delegate / decorator base class for ValidationRun's
 * @author dongfang
 *
 */
public class ValidationRunDecoratorBase implements ValidationRun {

  ValidationRun decorated;

  ValidationRunDecoratorBase(ValidationRun decorated) {
    this.decorated = decorated;
  }

  public short getProgress() {
    return decorated.getProgress();
  }

  public ValidationResult getValidationResult() {
    return decorated.getValidationResult();
  }

  public boolean relaxateProgress(short progress) {
    return decorated.relaxateProgress(progress);
  }

  public void setControlFlowSG(XMLGraph controlFlowSG) {
    decorated.setControlFlowSG(controlFlowSG);
  }

  public void setInputType(SingleTypeXMLClass inputType) {
    decorated.setInputType(inputType);
  }

  public void setOutputType(XMLGraph outputType) {
    decorated.setOutputType(outputType);
  }

  public void setPerformanceLogger(PerformanceLogger pa) {
    decorated.setPerformanceLogger(pa);
  }

  public void setSemPreservingSimplifiedStylesheet(Stylesheet ss) {
    decorated.setSemPreservingSimplifiedStylesheet(ss);
  }

  public void setApproxSimplifiedStylesheet(Stylesheet ss) {
    decorated.setApproxSimplifiedStylesheet(ss);
  }

  public void setValidationResult(ValidationResult result) {
    decorated.setValidationResult(result);
  }

  public void setXcfg(ControlFlowGraph xcfg) {
    decorated.setXcfg(xcfg);
  }
}
