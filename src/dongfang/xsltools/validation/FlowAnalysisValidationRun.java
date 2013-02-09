package dongfang.xsltools.validation;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

public class FlowAnalysisValidationRun implements ValidationRun {
  short progress;
  Stylesheet stylesheet;
  
  
  public boolean relaxateProgress(short progress) {
    this.progress = (short) Math.max(progress, this.progress);
    return this.progress < XCFG_CONSTRUCTED;
  }

  public short getProgress() {
    return progress;
  }

  public ValidationResult getValidationResult() {
    throw new AssertionError();
  }

  public void setControlFlowSG(XMLGraph controlFlowSG) {
    throw new AssertionError();
  }

  public void setInputType(SingleTypeXMLClass inputType) {
  }

  public void setOutputType(XMLGraph outputType) {
    throw new AssertionError();
  }

  public void setPerformanceLogger(PerformanceLogger pa) {
  }

  public Stylesheet getStylesheet() {
    return stylesheet;
  }
  
  public void setApproxSimplifiedStylesheet(Stylesheet ss) {
    this.stylesheet = ss;
  }

  public void setSemPreservingSimplifiedStylesheet(Stylesheet ss) {
    this.stylesheet = ss;
  }

  public void setValidationResult(ValidationResult result) {
    throw new AssertionError();
  }

  public void setXcfg(ControlFlowGraph xcfg) {
  }
}
