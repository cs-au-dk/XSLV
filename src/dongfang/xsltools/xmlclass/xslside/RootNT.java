package dongfang.xsltools.xmlclass.xslside;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Automata;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.Declaration;
import dongfang.xsltools.xmlclass.schemaside.ElementDecl;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xpath2.XPathAxisStep;

public class RootNT extends AbstractNodeType implements DeclaredNodeType,
    UndeclaredNodeType, Declaration {
  public static final RootNT instance = new RootNT();

  private static final Origin origin = new Origin("[root]",0,0);

  private RootNT() {
  }

  public boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz) {
    return s.accept(this, clazz);
  }

  // @Override
  @Override
public char getCharRepresentation(CharNameResolver clazz) {
    return '0';
  }

  /**
   * TODO: Make separable from XML classes and automata somehow.
   */
  public Automaton getATSAutomaton(SingleTypeXMLClass clazz) {
    return clazz.getRootNodeTypeAutomaton();
  }

  @Override
public boolean equals(Object val) {
    return val == this;
  }

  @Override
public int hashCode() {
    return 0;
  }

  @Override
  public void runChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    // Add the document element:
    result.addAll(clazz.getDocumentElementDecl().getAllUses());
    // Add comments and PIs:
    result.add(CommentNT.instance);
    result.add(PINT.chameleonInstance);
  }

  // @Override
  public void runParentAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
  }

  public Node constructParentFM(
      SGFragment fraggle, SingleTypeXMLClass clazz,
      Set<DeclaredNodeType> typeSet)
      throws XSLToolsSchemaException {
    return fraggle.createEpsilonNode();
  }

  @Override
  public Node constructChildFM(SGFragment fraggle, SingleTypeXMLClass clazz,
      boolean maxOne, Set<DeclaredNodeType> weCare,
      boolean _allowInterleave, ContentOrder order) {

    // TODO: Union over substitutes (type substitutes that is...)
    ElementDecl docElm = clazz.getDocumentElementDecl();

    Set<? extends ElementUse> variations = new HashSet<ElementUse>(docElm.getAllUses());

    //variations.retainAll(typeSet);

    List<Integer> substitutes = new LinkedList<Integer>();

    for (DeclaredNodeType type2 : weCare) {
    // TODO: Verify that this works: Should ve not look at the original declaration, really??
    // OK rededlarations of root are not everyday. They are probable never-never.
      if (variations.contains(type2.getOriginalDeclaration()))
        substitutes.add(fraggle.createPlaceholder(type2).getIndex());
    }
    /*
    for (ElementUse ed2 : variations) {
      substitutes.add(fraggle.createPlaceholder(ed2).getIndex());
    }
    */
    Node almostresult;

    if (substitutes.size() > 1)
      almostresult = fraggle.createChoiceNode(substitutes, new Origin("subsumption-varieties", 0, 0));
    else if (substitutes.size() > 0)
      almostresult = fraggle.getNodeAt(substitutes.get(0));
    else
      return fraggle.createEpsilonNode();
    return fraggle.wrapInCommentPIConstruct(almostresult, weCare.contains(CommentNT.instance), null);
  }

  public Node constructAncestorFM(SGFragment fraggle, SingleTypeXMLClass clazz,
      Set<DeclaredNodeType> typeSet, boolean singleMultiplicity)
      throws XSLToolsSchemaException {
     //checkEmpty(typeSet);
     return fraggle.createEpsilonNode();
  }

  @Override
  public Node constructDescendantFM (
      SGFragment fraggle, SingleTypeXMLClass clazz, 
      boolean maxOne,
      Set<DeclaredNodeType> typeSet) throws XSLToolsSchemaException {
    return constructAllModel(fraggle, clazz, maxOne, typeSet);
  }

  /*
   * @Override public void getGuaranteedDescendants( SingleTypeXMLClass clazz,
   * Set<? super DeclaredNodeType> result) { // TODO: Some way to iterate over
   * type leaves only would be nice. //(well .. naah just iterate over additions
   * to content, ok...) DeclaredNodeType docElm =
   * clazz.getDocumentElementDecl(); if (result.add(docElm)) {
   * docElm.getGuaranteedDescendants(clazz, result); } }
   */

  /*
   * @Override public Node constructDescendantModel(SGFragment fraggle,
   * SingleTypeXMLClass clazz, Set<DeclaredNodeType> typeSet, boolean
   * singleMultiplicity) throws XSLToolsSchemaException {
   * 
   * return construct(fraggle, clazz, typeSet, singleMultiplicity);
   * 
   * Set<DeclaredNodeType> guar = new HashSet<DeclaredNodeType>();
   * getGuaranteedDescendants(clazz, guar);
   * 
   * guar.retainAll(typeSet);
   * 
   * Node guar_n = constructAllModel(fraggle, clazz, guar, singleMultiplicity);
   * 
   * Set<DeclaredNodeType> copy = new HashSet<DeclaredNodeType>(typeSet);
   * copy.removeAll(guar);
   * 
   * Node nonguar_n = constructPanicModel(fraggle, clazz, copy,
   * singleMultiplicity);
   * 
   * List<Integer> content = new ArrayList<Integer>(2);
   *  // TODO: Outstanding issue with single-cardinality.
   * content.add(guar_n.getIndex()); content.add(nonguar_n.getIndex());
   * 
   * Node result;
   * 
   * if (singleMultiplicity) result = fraggle.createChoiceNode(content, new
   * Origin("root-guaranteed-desc", 0, 0)); else result =
   * fraggle.createInterleaveNode(content, new Origin("root-guaranteed-desc", 0,
   * 0)); return result; }
   */

  /*
   * public SGFragment constructSGFragment(SingleTypeXMLClass clazz,
   * SummaryGraph sg, short axis, Set<DeclaredNodeType> newNodeSet) {
   * SGFragment result = new SGFragment(sg); switch (axis) { case
   * XPathAxisStep.CHILD: ElementDecl docElm = clazz.getDocumentElementDecl();
   * result.setEntryNode(result.createPlaceholder(docElm)); return result;
   * default: // make an empty choice. Set<Integer> es =
   * Collections.emptySet(); result.setEntryNode(result.createChoiceNode(es, new
   * Origin("[root]", 0, 0))); return result; } }
   */

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz) {
    // give up! It's highly improbable that the document element
    // in either source nor target have simple types... or are easy
    // to approximate well.
    return Automata.get("string");
  }

  @Override
public String toString() {
    return "[root]";
  }

  public Node constructInstantiationFM(SGFragment fraggle, int content, SingleTypeXMLClass clazz) throws XSLToolsSchemaException {
    fraggle.setEntryNode(fraggle.getNodeAt(content));
    return fraggle.getNodeAt(content);
    //return fraggle.get(this);
  }
  
  public Origin getDeclarationOrigin() {
    return origin;
  }
  
  public DeclaredNodeType getOriginalDeclaration() {
    return this;
  }
}
