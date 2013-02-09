package dongfang.xsltools.xmlclass.relaxng;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.VisitorSupport;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.misc.Automata;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.CharGenerator;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ElementDecl;
import dongfang.xsltools.xmlclass.schemaside.ElementDeclImpl;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xpath2.XPathAxisStep;

public class RNGElementDecl extends ElementDeclImpl implements ElementUse {
  // private final QName name;
  // private final ElementNT myNodeType;
  final Element myDeclaration;

  // TODO: Update this
  private boolean acceptsText = true;

  // Automaton ATSPathAutomaton;

  /*
   * Since we by assumption are single type, there are no problems in
   * identifying children by QName -- they will be the same type. Parents might
   * be not as easy...
   */
  // final Map<QName, RNGElementDecl> parents = new HashMap<QName,
  // RNGElementDecl>();
  final Map<QName, RNGElementDecl> children = new HashMap<QName, RNGElementDecl>();

  RNGElementDecl(QName name, Element myDeclaration, RNGModule myModule)
      throws XSLToolsSchemaException {
    super(name);
    this.myDeclaration = myDeclaration;
  }

  void processContentModel(final RNGModule module) {
    myDeclaration.accept(new VisitorSupport() {
      @Override
      public void visit(Element node) {
        if (node.getQName().equals(RelaxNGConstants.ELEM_REF_QNAME)) {
          String name = node.attributeValue(RelaxNGConstants.ATTR_NAME_QNAME);
          RNGElementDecl child = module.resolve(name);
          RNGElementDecl.this.children.put(child.getQName(), child);
          // RNGElementDecl.this.childDecls.add(child);
          child.addParentUse(RNGElementDecl.this);
        }
      }
    });
  }

  @Override
public void snapshootATSPathAutomaton(SingleTypeXMLClass clazz) {
    super.snapshootATSPathAutomaton(clazz);
  }

  @Override
public void fixupCharacterNames(SingleTypeXMLClass clazz,
      CharGenerator charGen) throws XSLToolsSchemaException {
    super.fixupCharacterNames(clazz, charGen);
  }

  @Override
public void constructATSPathAutomaton(State parentState,
      State commentParentState, State commentAcceptState, State PIParentState,
      State PIAcceptState, State textParentState, State textAcceptState) {

    if (myState == null) {
      myState = new State();

      State myCommentState = new State();
      Transition trans = new Transition(getCharRepresentation(), myCommentState);
      commentParentState.addTransition(trans);

      if (acceptsCommentsPIs()) {
        trans = new Transition(CharGenerator.getCommentChar(),
            commentAcceptState);
        myCommentState.addTransition(trans);
      }

      State myPIState = new State();
      trans = new Transition(getCharRepresentation(), myPIState);
      PIParentState.addTransition(trans);

      if (acceptsCommentsPIs()) {
        trans = new Transition(CharGenerator.getPIChar(), PIAcceptState);
        myPIState.addTransition(trans);
      }

      State myTextState = new State();
      trans = new Transition(getCharRepresentation(), myTextState);
      textParentState.addTransition(trans);

      if (mayContainTextNodes()) {
        trans = new Transition(CharGenerator.getPCDATAChar(), textAcceptState);
        myTextState.addTransition(trans);
      }

      for (ElementUse child : children.values()) {
        ((RNGElementDecl) child).constructATSPathAutomaton(myState,
            myCommentState, commentAcceptState, myPIState, PIAcceptState,
            myTextState, textAcceptState);
      }
    }

    char myChar = getCharRepresentation();
    Transition trans = new Transition(myChar, myState);
    parentState.addTransition(trans);
  }

  public boolean isAbstract() {
    return false;
  }

  public boolean acceptsCommentsPIs() {
    return true;
  }

  public boolean mayContainTextNodes() {
    return acceptsText;
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz) {
    return Automata.get("string");
  }

  @Override
  public Origin getOrigin() {
    throw new AssertionError("Not impl.");
  }

  @Override
  public Automaton languageOfTextNodes(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    throw new AssertionError("Not impl.");
  }

  public Node constructParentModel(SGFragment fraggle,
      SingleTypeXMLClass clazz, Set<DeclaredNodeType> typeSet,
      boolean singleMultiplicity) throws XSLToolsSchemaException {
    throw new AssertionError("Not impl.");
  }

  public boolean acceptsCommentPIs() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean acceptsText() {
    // TODO Auto-generated method stub
    return false;
  }

  public void attributeUses(Map<QName, AttributeUse> dumper) {
    // TODO Auto-generated method stub

  }

  public void childDeclarations(Map<QName, ElementDecl> dumper) {
    // TODO Auto-generated method stub

  }

  public Set<? extends ElementUse> getSGRPSubstituteableElementUses() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isNilled() {
    // TODO Auto-generated method stub
    return false;
  }

  public ElementDecl myElementDeclaration() {
    // TODO Auto-generated method stub
    return null;
  }

  public void addSelfUse(ElementUse use) {
    // TODO Auto-generated method stub

  }

  public Set<? extends ElementUse> getAllUses() {
    // TODO Auto-generated method stub
    return null;
  }

  public void getAllUses(Set<? super ElementUse> dumper) {
    // TODO Auto-generated method stub

  }

  public Map<QName, AttributeUse> getAllVariationAttributeUses() {
    // TODO Auto-generated method stub
    return null;
  }

  public Set<ElementUse> getCommentsPIAcceptingVariations() {
    // TODO Auto-generated method stub
    return null;
  }

  public Set<? extends ElementDecl> getSGRPSubstituteableElementDecls() {
    // TODO Auto-generated method stub
    return null;
  }

  public Set<ElementUse> getTextAcceptingVariations() {
    // TODO Auto-generated method stub
    return null;
  }

  public Set<ElementDecl> getWidenedChildElementDecls() {
    // TODO Auto-generated method stub
    return null;
  }

  public void getWidenedChildElementDecls(Set<? super ElementDecl> dumper) {
    // TODO Auto-generated method stub

  }

  public void processContentModel(SingleTypeXMLClass clazz,
      Map<QName, ElementDecl> tempNameToElementMap)
       {
    // TODO Auto-generated method stub

  }

  public Node constructParentFM(SGFragment fraggle, SingleTypeXMLClass clazz,
      Set<DeclaredNodeType> typeSe)
      throws XSLToolsSchemaException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    // TODO Auto-generated method stub
    return false;
  }

  public void runParentAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    // TODO Auto-generated method stub

  }

  public void fixupParentReferences() {
    // TODO Auto-generated method stub

  }

  public void fixupParentReferences(ElementUse canonical) {
    // TODO Auto-generated method stub

  }

  public void addAttributeUse(AttributeUse use) {
    // TODO Auto-generated method stub
    
  }

  public Origin getDeclarationOrigin() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * public void getGuaranteedDescendants( SingleTypeXMLClass clazz, Set<?
   * super DeclaredNodeType> result) { throw new AssertionError("Not
   * implemented."); }
   */
  public DeclaredNodeType getOriginalDeclaration() {
    return this;
  }
}
