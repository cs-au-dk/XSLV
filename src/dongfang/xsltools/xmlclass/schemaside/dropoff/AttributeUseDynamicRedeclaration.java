package dongfang.xsltools.xmlclass.schemaside.dropoff;

import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.AttributeDecl;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;

public abstract class AttributeUseDynamicRedeclaration 
extends DynamicRedeclaration implements AttributeUse {

  public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz, Cardinal cardinal) {
    return ((AttributeUse)decorated).constructFlowModel(fraggle, clazz, cardinal);
  }

  public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz) {
    // TODO Auto-generated method stub
    return ((AttributeUse)decorated).constructFlowModel(fraggle, clazz);
  }

  public AttributeDecl getAttributeDeclaration() {
    // TODO Auto-generated method stub
    return ((AttributeUse)decorated).getAttributeDeclaration();
  }

  public Cardinal getCardinality() {
    // TODO Auto-generated method stub
    return ((AttributeUse)decorated).getCardinality();
  }

  public QName getOwnerElementQName() {
    // TODO Auto-generated method stub
    return ((AttributeUse)decorated).getOwnerElementQName();
  }

  public ElementUse getOwnerElementUse() {
    // TODO Auto-generated method stub
    return ((AttributeUse)decorated).getOwnerElementUse();
  }

  public boolean typeMayDeriveFrom(QName name) throws XSLToolsSchemaException {
    // TODO Auto-generated method stub
    return ((AttributeUse)decorated).typeMayDeriveFrom(name);
  }

  public Automaton getClarkNameAutomaton() {
    // TODO Auto-generated method stub
    return ((AttributeUse)decorated).getClarkNameAutomaton();
  }

  public QName getQName() {
    // TODO Auto-generated method stub
    return ((AttributeUse)decorated).getQName();
  }

  public AttributeUseDynamicRedeclaration(DeclaredNodeType decorated) {
    super(decorated);
  }
}
