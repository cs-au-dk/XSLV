package dongfang.xsltools.xmlclass.schemaside;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.xslside.CharNameResolver;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NodeType;
import dongfang.xsltools.xpath2.XPathAxisStep;

public abstract class AttributeUseDecorator implements AttributeUse {
  protected final AttributeUse decorated;

  protected AttributeUse getDecoratedUse() {
    return decorated;
  }

  protected AttributeUseDecorator(AttributeUse decorated) {
    this.decorated = decorated;
  }

  public Node constructAncestorFM(SGFragment fraggle, SingleTypeXMLClass clazz, boolean maxOne,
      Set<DeclaredNodeType> typeSet) throws XSLToolsSchemaException {
    return decorated.constructAncestorFM(fraggle, clazz, maxOne, typeSet);
  }

  public Node constructAncestorOrSelfFM(SGFragment fraggle, SingleTypeXMLClass clazz, boolean maxOne,
      Set<DeclaredNodeType> typeSet, DeclaredNodeType selfType, ContentOrder order) throws XSLToolsSchemaException {
    return decorated.constructAncestorOrSelfFM(fraggle, clazz, maxOne, typeSet, selfType, order);
  }

  public Node constructAttributeFM(SGFragment fraggle, SingleTypeXMLClass clazz, Set<DeclaredNodeType> typeSet)
      throws XSLToolsSchemaException {
    return decorated.constructAttributeFM(fraggle, clazz, typeSet);
  }

  public Node constructChildFM(SGFragment fraggle, SingleTypeXMLClass clazz, boolean maxOne,
      Set<DeclaredNodeType> typeSet, boolean allowInterleave, ContentOrder order) throws XSLToolsSchemaException {
    return decorated.constructChildFM(fraggle, clazz, maxOne, typeSet, allowInterleave, order);
  }

  public Node constructDescendantFM(SGFragment fraggle, SingleTypeXMLClass clazz, boolean maxOne,
      Set<DeclaredNodeType> typeSet) throws XSLToolsSchemaException {
    return decorated.constructDescendantFM(fraggle, clazz, maxOne, typeSet);
  }

  public Node constructDescendantOrSelfFM(SGFragment fraggle, SingleTypeXMLClass clazz, boolean maxOne,
      Set<DeclaredNodeType> typeSet, DeclaredNodeType selfType, ContentOrder order) throws XSLToolsSchemaException {
    return decorated.constructDescendantOrSelfFM(fraggle, clazz, maxOne, typeSet, selfType, order);
  }

  public Node constructInstantiationFM(SGFragment fraggle, int content, SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    return decorated.constructInstantiationFM(fraggle, content, clazz);
  }

  public Node constructParentFM(SGFragment fraggle, SingleTypeXMLClass clazz, Set<DeclaredNodeType> typeSet)
      throws XSLToolsSchemaException {
    return decorated.constructParentFM(fraggle, clazz, typeSet);
  }

  public Node constructSelfFM(SGFragment fraggle, DeclaredNodeType selfType) throws XSLToolsSchemaException {
    return decorated.constructSelfFM(fraggle, selfType);
  }

  public Automaton getATSAutomaton(SingleTypeXMLClass clazz) {
    return decorated.getATSAutomaton(clazz);
  }

  public Origin getDeclarationOrigin() {
    return decorated.getDeclarationOrigin();
  }

  public Object getIdentifier() {
    return decorated.getIdentifier();
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz) throws XSLToolsSchemaException {
    return decorated.getValueOfAutomaton(clazz);
  }

  public boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz) throws XSLToolsSchemaException {
    return decorated.matches(s, clazz);
  }

  public void runAttributeAxis(SingleTypeXMLClass clazz, Set<? super DeclaredNodeType> result) {
    decorated.runAttributeAxis(clazz, result);
  }

  public void runChildAxis(SingleTypeXMLClass clazz, Set<? super DeclaredNodeType> result)
      throws XSLToolsSchemaException {
    decorated.runChildAxis(clazz, result);
  }

  public void runParentAxis(SingleTypeXMLClass clazz, Set<? super DeclaredNodeType> result) {
    decorated.runParentAxis(clazz, result);
  }

  public void runReverseAttributeAxis(SingleTypeXMLClass clazz, Set<? super DeclaredNodeType> result) {
    decorated.runReverseAttributeAxis(clazz, result);
  }

  public void runReverseChildAxis(SingleTypeXMLClass clazz, Set<? super DeclaredNodeType> result) {
    decorated.runReverseChildAxis(clazz, result);
  }

  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    org.dom4j.Element me = fac.createElement(getClass().getSimpleName());
    parent.add(me);
    decorated.diagnostics(me, fac, configuration);
  }

  public Automaton getClarkNameAutomaton() {
    return decorated.getClarkNameAutomaton();
  }

  public QName getQName() {
    return decorated.getQName();
  }

  public char getCharRepresentation(CharNameResolver resolver) {
    return decorated.getCharRepresentation(resolver);
  }

  public String toLabelString() {
    return decorated.toLabelString();
  }

  //public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz, AttributeUse real, AttributeUse.Cardinal cardinal) {
  //  return decorated.constructFlowModel(fraggle, clazz, real, cardinal);}

  public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz, AttributeUse.Cardinal cardinal) {
    return decorated.constructFlowModel(fraggle, clazz, cardinal);
  }

  //public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz, AttributeUse real) {
  //return decorated.constructFlowModel(fraggle, clazz, real);}

  public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz) {
    return decorated.constructFlowModel(fraggle, clazz);
  }

  public AttributeDecl getAttributeDeclaration() {
    return decorated.getAttributeDeclaration();
  }

  public Cardinal getCardinality() {
    return decorated.getCardinality();
  }

  public QName getOwnerElementQName() {
    return decorated.getOwnerElementQName();
  }

  public ElementUse getOwnerElementUse() {
    return decorated.getOwnerElementUse();
  }

  public boolean typeMayDeriveFrom(QName name) throws XSLToolsSchemaException {
    return decorated.typeMayDeriveFrom(name);
  }

  public int compareTo(NodeType o) {
    return decorated.compareTo(o);
  }

  @Override
public boolean equals(Object o) {
    return decorated.equals(o);
  }

  @Override
public int hashCode() {
    return decorated.hashCode();
  }

  public DeclaredNodeType getOriginalDeclaration() {
    return getDecoratedUse();
  }

  public Collection<DeclaredNodeType> getMyTypes(Collection<DeclaredNodeType> choices) {
    System.err.println("getMyTypes on " + getClass().getSimpleName() + ", not really finished.");
    Collection<DeclaredNodeType> result = new HashSet<DeclaredNodeType>();
    for (DeclaredNodeType type : choices) {
      if (type.equals(this))
        result.add(type);
    }
    return result;
  }
}
