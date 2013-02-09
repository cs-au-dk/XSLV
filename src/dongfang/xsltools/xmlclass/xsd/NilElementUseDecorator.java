package dongfang.xsltools.xmlclass.xsd;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.ElementDecl;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.AbstractNodeType;
import dongfang.xsltools.xmlclass.xslside.CharNameResolver;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NodeType;
import dongfang.xsltools.xmlclass.xslside.PINT;
import dongfang.xsltools.xpath2.XPathAxisStep;

/**
 * A representaion of the nilled version of declared-nillable elements.
 * They have no content, but have a nil=true attribute. Implemented
 * as a decorator.
 * @author dongfang
 */
public class NilElementUseDecorator extends AbstractNodeType implements
    ElementUse {

  private final ElementUse decorated;

  // set from outside of here (fix-me).
  XSINilUse nillert;

  NilElementUseDecorator(ElementUse decorated) {
    this.decorated = decorated;
  }

  public void attributeUses(Map<QName, AttributeUse> dumper) {
    decorated.attributeUses(dumper);
    // squeeze out optional nil=false that may have been added.
    dumper.put(nillert.getQName(), nillert);
  }

  public void childDeclarations(Map<QName, ElementDecl> dumper) {
    return;
  }

  public ElementDecl myElementDeclaration() {
    return decorated.myElementDeclaration();
  }

  @Override
  public Node constructAncestorFM(
      SGFragment fraggle, SingleTypeXMLClass clazz,
      boolean maxOne, Set<DeclaredNodeType> typeSet)
      throws XSLToolsSchemaException {
    return decorated.constructAncestorFM(fraggle, clazz, maxOne, typeSet);
  }

  @Override
public Node constructAttributeFM(SGFragment fraggle,
      SingleTypeXMLClass clazz, Set<DeclaredNodeType> attrs)
      throws XSLToolsSchemaException {

    // TODO! Beautify!!!
    Set<DeclaredNodeType> altattrs = new HashSet<DeclaredNodeType>();
    for (DeclaredNodeType t : attrs) {
      if (!(t instanceof XSINilUse))
        altattrs.add(t);
    }
    altattrs.add(nillert);
    return seriousAttributeFM(fraggle, clazz, attrs);
  }

  @Override
  public Node constructChildFM(SGFragment fraggle, 
      SingleTypeXMLClass clazz,
      boolean maxOne,
      Set<DeclaredNodeType> typeSet,
      boolean allowInterleave, ContentOrder order)
      throws XSLToolsSchemaException {
    return fraggle.wrapInCommentPIConstruct(fraggle.createEpsilonNode(), true, null);
  }

  public Node constructParentFM(SGFragment fraggle, SingleTypeXMLClass clazz,
      Set<DeclaredNodeType> typeSet)
      throws XSLToolsSchemaException {
    return decorated.constructParentFM(fraggle, clazz, typeSet);
  }

  @Override
  public Node constructDescendantFM(
      SGFragment fraggle, SingleTypeXMLClass clazz, 
      boolean maxOne, Set<DeclaredNodeType> typeSet) throws XSLToolsSchemaException {
    return fraggle.wrapInCommentPIConstruct(fraggle.createEpsilonNode(), true, null);
  }

  /*
   * public void getGuaranteedDescendants(SingleTypeXMLClass clazz, Set<? super
   * DeclaredNodeType> tanker) { // TODO Auto-generated method stub }
   */

  @Override
  public Object getIdentifier() {
    return decorated.getIdentifier();
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    return Automaton.makeEmptyString();
  }

  public boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    return decorated.matches(s, clazz);
  }

  @Override
  public void runAttributeAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    decorated.runAttributeAxis(clazz, result);
    // TODO: Don't we want to get rid of optional nil=false
    // attribute?
    result.add(nillert);
  }

  @Override
  public void runChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    result.add(CommentNT.instance);
    result.add(PINT.chameleonInstance);
  }

  public void runParentAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    decorated.runParentAxis(clazz, result);
  }

  @Override
  public void runReverseAttributeAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    decorated.runReverseAttributeAxis(clazz, result);
  }

  @Override
  public void runReverseChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    decorated.runReverseChildAxis(clazz, result);
  }

  @Override
  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement(getClass().getSimpleName());
    me.addAttribute("nil", Boolean.toString(true));
    parent.add(me);
    decorated.diagnostics(me, fac, configuration);
  }

  public Automaton getClarkNameAutomaton() {
    return decorated.getClarkNameAutomaton();
  }

  public QName getQName() {
    return decorated.getQName();
  }

  @Override
public char getCharRepresentation(CharNameResolver resolver) {
    return decorated.getCharRepresentation(resolver);
  }

  @Override
  public String toLabelString() {
    return decorated.toLabelString() + "(nilled)";
  }

  @Override
  public int compareTo(NodeType o) {
    return decorated.compareTo(o);
  }

  public boolean acceptsCommentPIs() {
    return decorated.acceptsCommentPIs();
  }

  public boolean acceptsText() {
    return false;// !nilled && decorated.acceptsText();
  }

  @Override
  public Node constructDescendantOrSelfFM(
      SGFragment fraggle,SingleTypeXMLClass clazz, 
      boolean maxOne, 
      Set<DeclaredNodeType> typeSet,
      DeclaredNodeType selfType, ContentOrder order)
      throws XSLToolsSchemaException {
    return constructSelfFM(fraggle, selfType);
  }

  public Automaton getATSAutomaton(SingleTypeXMLClass clazz) {
    return decorated.getATSAutomaton(clazz);
  }

  public Set<? extends ElementUse> getSGRPSubstituteableElementUses() {
    return decorated.getSGRPSubstituteableElementUses();
  }

  public boolean isNilled() {
    return true;
  }

  public boolean typeMayDeriveFrom(QName typeQName)
      throws XSLToolsSchemaException {
    return decorated.typeMayDeriveFrom(typeQName);
  }

  public void fixupParentReferences(ElementUse canonical) {
    decorated.fixupParentReferences(canonical);
  }

  public void fixupParentReferences() {
    decorated.fixupParentReferences(this);
  }

  public void addAttributeUse(AttributeUse use) {
    decorated.addAttributeUse(use);
  }
  
  public Node constructInstantiationFM(SGFragment fraggle, int content, SingleTypeXMLClass clazz) 
  throws XSLToolsSchemaException {
    return decorated.constructInstantiationFM(fraggle, content, clazz);
  }
  
  public Origin getDeclarationOrigin() {
    return decorated.getDeclarationOrigin();
  }
  
  public DeclaredNodeType getOriginalDeclaration() {
    return this;
  }
}
