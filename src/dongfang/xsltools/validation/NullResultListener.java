package dongfang.xsltools.validation;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

public class NullResultListener implements ResultListener {
  public void setControlFlowSG(XMLGraph controlFlowSG) {
    // We have no interest in it; let him keep it
  }

  public void setInputType(SingleTypeXMLClass inputType) {
    //  We have no interest in it; let him keep it
  }

  public void setOutputType(XMLGraph outputType) {
    //  We have no interest in it; let him keep it
  }

  public void setPerformanceLogger(PerformanceLogger pa) {
    //  We have no interest in it; let him keep it
  }

  public void setApproxSimplifiedStylesheet(Stylesheet ss) {
    //  We have no interest in it; let him keep it
  }

  public void setSemPreservingSimplifiedStylesheet(Stylesheet ss) {
    //  We have no interest in it; let him keep it
  }

  public void setXcfg(ControlFlowGraph xcfg) {
    //  We have no interest in it; let him keep it
  }

  public void setValidationResult(ValidationResult result) {
    //  We have no interest in it; let him keep it
  }
}
