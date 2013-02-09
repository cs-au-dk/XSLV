package dongfang.xsltools.validation;

import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

public class InputToucherRun extends ValidationRunDecoratorBase {

  // DEAD !!! code.
  SingleTypeXMLClass is;

  public InputToucherRun(SingleTypeXMLClass is, ValidationRun decorated) {
    super(decorated);
    this.is = is;
  }

  @Override
  public void setXcfg(ControlFlowGraph xcfg) {
    // TODO Auto-generated method stub
    super.setXcfg(xcfg);

    /*
     * if (Selection.totalSlammer == null) {
     * //System.err.println(getClass().getSimpleName() + ": Oops, my automaton
     * disappered"); return; }
     * 
     * if (Selection.totalSlammer.isEmpty()) {
     * //System.err.println(getClass().getSimpleName() + ": Oops, my automaton
     * never was updated. Use automaton algorithm please."); return; }
     */
    // Set<BackendDecl> death = is.unusedTypes(Selection.totalSlammer);
    // System.out.println("These types are never used: ");
    // System.out.println(death);
  }
}
