package dongfang.xsltools.xmlclass.schemaside;

import java.util.Set;

import org.dom4j.QName;

import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xpath2.XPathAxisStep;

public class ConcreteAttributeUse extends AbstractAttributeUse implements
    AttributeUse {

  final private ElementUse owner;

  public ConcreteAttributeUse(AttributeDecl attributeDecl,
      Cardinal cardinality, String fixedValue, ElementUse owner) {
    super(attributeDecl, cardinality, fixedValue);
    this.owner = owner;
  }

  public ConcreteAttributeUse(AbstractAttributeUse template, ElementUse owner) {
    super(template.attributeDecl, template.cardinal, template.fixedValue);
    this.owner = owner;
  }

  /*
  public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz,
      AttributeUse realMcCoy, AttributeUse.Cardinal cardinal) {
    if (cardinal == AttributeUse.Cardinal.PROHIBITED) {
      return fraggle.createEpsilonNode();
    }

    Node attr = fraggle.createPlaceholder(realMcCoy);

    if (cardinal == AttributeUse.Cardinal.REQUIRED)
      return attr;
    return fraggle.constructOptionalCardinal(attr, getDeclarationOrigin());
  }
  */
  
  public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz,
    AttributeUse.Cardinal cardinal) {
    if (cardinal == AttributeUse.Cardinal.PROHIBITED) {
      return fraggle.createEpsilonNode();
    }

    Node attr = fraggle.createPlaceholder(this);

    if (cardinal == AttributeUse.Cardinal.REQUIRED)
      return attr;
    return fraggle.constructOptionalCardinal(attr, getDeclarationOrigin());
  }

  /*
  public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz,
      AttributeUse realMcCoy) {
    return constructFlowModel(fraggle, clazz, realMcCoy, cardinal);
  }
  */
  
  public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz) {
    return constructFlowModel(fraggle, clazz, cardinal);
  }

  /*
  public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz) {
    return constructFlowModel(fraggle, clazz, this);
  }
  */

  public boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    return s.accept(this, clazz);
  }

  public QName getOwnerElementQName() {
    return owner.getQName();
  }

  public ElementUse getOwnerElementUse() {
    return owner;
  }

  public Node constructParentFM(SGFragment fraggle, 
  SingleTypeXMLClass clazz, Set<DeclaredNodeType> typeSet)
      throws XSLToolsSchemaException {
    return constructParentFM(fraggle, owner);
  }

  public void runParentAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    runParentAxis(result, owner);
  }

  @Override
  public void runReverseAttributeAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    runParentAxis(result, owner);
  }

  public Node constructInstantiationFM(SGFragment fraggle, int content, SingleTypeXMLClass clazz) 
  throws XSLToolsSchemaException {
    Node realcontent = fraggle.createTextNode
    (getValueOfAutomaton(clazz), getDeclarationOrigin());
    return fraggle.createAttributeNode
    (getClarkNameAutomaton(), realcontent.getIndex(), getDeclarationOrigin());
  }
  
  public DeclaredNodeType getOriginalDeclaration() {
    return this;
  }
}
