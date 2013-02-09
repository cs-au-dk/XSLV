package dongfang.xsltools.validation;

import java.io.PrintStream;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

/**
 * A ValidationRun that prints little messages about how things are progressing.
 * @author dongfang
 *
 */
public class VerboseRunDecorator extends ValidationRunDecoratorBase {

  PrintStream out;

  public VerboseRunDecorator(ValidationRun decorated, PrintStream out) {
    super(decorated);
    this.out = out;
  }

  @Override
public void setControlFlowSG(XMLGraph controlFlowSG) {
    super.setControlFlowSG(controlFlowSG);
    out.println("SG contruction done");
  }

  @Override
public void setInputType(SingleTypeXMLClass inputType) {
    super.setInputType(inputType);
    out.println("Input type construction done");
  }

  @Override
public void setOutputType(XMLGraph outputType) {
    super.setOutputType(outputType);
    out.println("Output type construction done");
  }

  @Override
public void setSemPreservingSimplifiedStylesheet(Stylesheet ss) {
    super.setSemPreservingSimplifiedStylesheet(ss);
    out.println("Stylesheet simplification done");
  }

  @Override
public void setApproxSimplifiedStylesheet(Stylesheet ss) {
    super.setApproxSimplifiedStylesheet(ss);
    out.println("Stylesheet simplification done");
  }

  @Override
public void setValidationResult(ValidationResult result) {
    super.setValidationResult(result);
    out.println("Validation done");
  }

  @Override
public void setXcfg(ControlFlowGraph xcfg) {
    super.setXcfg(xcfg);
    out.println("Control flow algorithm done");
  }
}
