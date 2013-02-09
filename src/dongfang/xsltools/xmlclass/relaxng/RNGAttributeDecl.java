package dongfang.xsltools.xmlclass.relaxng;

import java.util.Set;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.misc.Automata;
import dk.brics.misc.Origin;
import dongfang.xsltools.xmlclass.schemaside.AttributeDeclImpl;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

public class RNGAttributeDecl extends AttributeDeclImpl {
  protected RNGAttributeDecl(RNGElementDecl myElement, QName myName) {
    super(myName);
    // addOwnerElementDecl(myElement);
  }

  @Override
public void constructATSPathAutomaton(State ownerElementState) {
    super.constructATSPathAutomaton(ownerElementState);
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz) {
    // TODO Auto-generated method stub
    return Automata.get("string");
  }

  @Override
protected void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration) {
  }

  @Override
public Origin getOrigin() {
    return new Origin("TODO RNGAttributeDecl", 0, 0);
  }
}
