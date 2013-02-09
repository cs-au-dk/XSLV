package dongfang.xsltools.xmlclass.schemaside.dropoff;

import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NamedNodeType;

public abstract class NamedTypeDynamicRedeclaration extends
DynamicRedeclaration implements NamedNodeType {

  public NamedTypeDynamicRedeclaration(DeclaredNodeType decorated) {
    super(decorated);
  }

  public Automaton getClarkNameAutomaton() {
    return ((ElementUse)decorated).getClarkNameAutomaton();
  }

  public QName getQName() {
    return ((ElementUse)decorated).getQName();
  }
  
  @Override
  protected boolean dynamicRedeclarationPropertiesEquals(DynamicRedeclaration o) {
    return decorated != null && decorated.equals(o.decorated);
  }
}
