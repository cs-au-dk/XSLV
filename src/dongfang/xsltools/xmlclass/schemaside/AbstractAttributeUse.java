package dongfang.xsltools.xmlclass.schemaside;

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
import dongfang.xsltools.xmlclass.xslside.AttributeNT;
import dongfang.xsltools.xmlclass.xslside.CharNameResolver;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NodeType;

/*
 * Stupid base class for attribute uses
 * TODO: Why it this not just an AbstractNodeType?
 * Why are concrete instances made?
 */
public class AbstractAttributeUse extends AttributeNT {

  protected final AttributeDecl attributeDecl;

  protected final AttributeUse.Cardinal cardinal;

  protected final String fixedValue;

  public AbstractAttributeUse(AttributeDecl attributeDecl,
      AttributeUse.Cardinal cardinal, String fixedValue) {
    super(attributeDecl.getQName());
    this.attributeDecl = attributeDecl;
    this.cardinal = cardinal;
    this.fixedValue = fixedValue;
  }

  public AttributeDecl getAttributeDeclaration() {
    return attributeDecl;
  }

  public AttributeUse.Cardinal getCardinality() {
    return cardinal;
  }

  public String getFixedValue() {
    return fixedValue;
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    if (fixedValue != null) {
      return Automaton.makeString(fixedValue.trim());
    }
    return attributeDecl.getValueOfAutomaton(clazz);
  }

//  public Set<? extends ElementUse> getOwnerElementDecls() {
//    return attributeDecl.getParentUses();
//  }

  @Override
public Set<QName> getOwnerElementNames() {
    return attributeDecl.getOwnerElementNames();
  }

  @Override
public QName getQName() {
    return attributeDecl.getQName();
  }

  @Override
public Automaton getClarkNameAutomaton() {
    return attributeDecl.getClarkNameAutomaton();
  }

  /*
   * This is the central idea of having attribute uses: Common auto
   */
  public Automaton getATSAutomaton(SingleTypeXMLClass clazz) {
    return attributeDecl.getATSAutomaton(clazz);
  }

  @Override
public Object getIdentifier() {
    return attributeDecl.getIdentifier();
  }

  /*
   * public boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz) throws
   * XSLToolsSchemaException { return attributeDecl.matches(s, clazz); }
   */

  @Override
public char getCharRepresentation(CharNameResolver resolver) {
    return attributeDecl.getCharRepresentation(resolver);
  }

  @Override
public String toLabelString() {
    return attributeDecl.toLabelString();
  }

  @Override
public int hashCode() {
    return attributeDecl.hashCode();
  }

  @Override
public boolean equals(Object o) {
    // if (!(o instanceof BackendAttributeDecl))
    // return false;

    if (o instanceof AttributeUse) {
      // attribute uses are canonical (could also check that decl and
      // owner element are the same, but that makes precious little
      // difference...
      // ouch! Decorators #%$#%@%$!!!
      if (o instanceof AttributeUseDecorator) {
        return equals(((AttributeUseDecorator)o).getDecoratedUse());
      }
      
      /*
       * Experiment:An attribute use and its dropoff are not the same.
      if (o instanceof DropOffDecorator)
        return equals(((DropOffDecorator)o).getDecorated());
      */
      return this == o;
    }

    // BackendAttributeDecl d = (BackendAttributeDecl)o;
    // ok so it's a plain attribute decl.
    // System.err.println("TODO: Inspect equals semantics here");
    // boolean gedefis = attributeDecl.equals(d) &&
    // d.getAllUses().contains(this);
    // return gedefis;
    return false;
  }

  static String cardinalityToString(AttributeUse.Cardinal c) {
    switch (c) {
    case REQUIRED:
      return "required";
    case OPTIONAL:
      return "optional";
    case PROHIBITED:
      return "prohibited";
    default:
      return "invalid";
    }
  }

  // public ElementUse getOwnerElementUse();

  public boolean typeMayDeriveFrom(QName type) throws XSLToolsSchemaException {
    return attributeDecl.typeMayDeriveFrom(type);
  }

  @Override
  public void runChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
  }

  @Override
  public void runAttributeAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
  }

  @Override
  public void runReverseChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
  }

  /*
   * @Override public void runReverseAttributeAxis(SingleTypeXMLClass clazz, Set<?
   * super DeclaredNodeType> result) { runParentAxis(clazz, result); }
   * 
   * public void runParentAxis(SingleTypeXMLClass clazz, Set<? super
   * DeclaredNodeType> result) { result.add(getOwnerElementUse()); }
   * 
   * public QName getOwnerElementQName() { return
   * getOwnerElementUse().getQName(); }
   *  // hehehhehehe... gnæk gnæk.... public Node constructParentFM(SGFragment
   * fraggle, SingleTypeXMLClass clazz, Set<DeclaredNodeType> typeSet, boolean
   * singleMultiplicity) throws XSLToolsSchemaException { return
   * fraggle.createPlaceholder(this.getOwnerElementUse()); }
   */

  public void runParentAxis(Set<? super DeclaredNodeType> result,
      ElementUse ownerElementUse) {
    result.add(ownerElementUse);
  }

  // hehehhehehe... gnæk gnæk....
  public Node constructParentFM(SGFragment fraggle, ElementUse ownerUse) {
    return fraggle.createPlaceholder(ownerUse);
  }

  @Override
  public Node constructDescendantOrSelfFM(SGFragment fraggle,
      SingleTypeXMLClass clazz, boolean singleMultiplicity,  Set<DeclaredNodeType> typeSet,
      DeclaredNodeType selfType,ContentOrder order) {
    return constructSelfFM(fraggle, selfType);
  }

  /*
   * public void getGuaranteedDescendants( SingleTypeXMLClass clazz, Set<?
   * super DeclaredNodeType> result) { }
   */

  @Override
public String toString() {
    return "<" + getClass().getSimpleName() + " cardinality=\""
        + cardinalityToString(cardinal) + "\" fixedValue=\""
        + (fixedValue == null ? "undefined" : fixedValue) + "\">"
        + attributeDecl.toString() + "</" + getClass().getSimpleName() + ">";
  }

  @Override
public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement(getClass().getSimpleName());
    parent.add(me);
    String use = cardinalityToString(cardinal);
    me.addAttribute("use", use);
    String fixed = fixedValue;
    if (fixed != null)
      me.addAttribute("fixed", fixed);
    attributeDecl.diagnostics(me, fac, configuration);
  }

  // just for a stable ordering in listings of node type sets, nothing else...
  @Override
public int compareTo(NodeType nt) {
    String s1 = toString();
    String s2 = nt.toString();
    int r = s1.compareTo(s2);
    if (r != 0)
      return r;
    if (equals(s2))
      return 0;
    return 1;
  }
  
  public Origin getDeclarationOrigin() {
    return attributeDecl.getOrigin();
  }
}
