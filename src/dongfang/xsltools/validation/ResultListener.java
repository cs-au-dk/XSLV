package dongfang.xsltools.validation;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

public interface ResultListener {

  void setControlFlowSG(XMLGraph controlFlowSG);

  void setInputType(SingleTypeXMLClass inputType);

  void setOutputType(XMLGraph outputType);

  void setValidationResult(ValidationResult result);

  void setSemPreservingSimplifiedStylesheet(Stylesheet ss);
  
  void setApproxSimplifiedStylesheet(Stylesheet ss);

  void setXcfg(ControlFlowGraph xcfg);

  void setPerformanceLogger(PerformanceLogger pa);
}
