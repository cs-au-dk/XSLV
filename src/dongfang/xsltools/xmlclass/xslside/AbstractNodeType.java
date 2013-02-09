package dongfang.xsltools.xmlclass.xslside;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.ControlFlowConfiguration;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

/**
 * A set of node types representing named elements and attributes as well as
 * single node types such as root, text and so on. Although there may exist more
 * than on objects representing a single node type (such as the root, or
 * elements named 'title'), overriding the equals() and hashCode() methods of
 * Object ensure that they behave as one.
 */
public abstract class AbstractNodeType implements NodeType {

  /*
   * Node test after principal Element axis test.
   */
  protected Set<NodeType> matchesE(Set<NodeType> delta, QName qname) {
    NodeType tester = new ElementNT(qname);
    if (delta.contains(ONE_ANY_NAME_ELEMENT_NT))
      return Collections.singleton(tester);
    if (delta.contains(tester))
      return Collections.singleton(tester);
    return Collections.emptySet();
  }

  /*
   * Node test after principal Attribute axis test.
   */
  protected Set<NodeType> matchesA(Set<NodeType> delta, QName qname) {
    NodeType tester = new AttributeNT(qname);
    if (delta.contains(ONE_ANY_NAME_ATTRIBUTE_NT))
      return Collections.singleton(tester);
    if (delta.contains(tester))
      return Collections.singleton(tester);
    return Collections.emptySet();
  }

  public void runChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result)
      throws XSLToolsSchemaException {
  }

  /*
   * Intensionally left unimplemented! public void
   * runParentAxis(SingleTypeXMLClass clazz, Set<? super DeclaredNodeType>
   * result) { }
   */

  public void runAttributeAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
  }

  public void runReverseChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
  }

  public void runReverseAttributeAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
  }

/*
  protected void checkEmpty(Set<DeclaredNodeType> typeSet) {
    if (!typeSet.isEmpty()) {
      System.err.println("I am: " + this);
      System.err.println("(anyway, I am class): " + this.getClass());
      new RuntimeException().printStackTrace();
      //throw new AssertionError(
      //    "Conficting axis and node type! Selected set should be empty, bu is not:"
      //        + typeSet + ". Could be an override missing.");
      System.out.println(typeSet);
  }
  }
  */
  /*
   * Construct a model of types KNOWN to be selected, but in unknown order.
   * Useful for descendant-or-self::.....test, as long as the descendant set is
   * a sure to be present one.
   */
  static Node ss_constructAllModel(SGFragment fraggle, SingleTypeXMLClass clazz,
      Set<DeclaredNodeType> weCare,boolean maxOne) {

    List<Integer> contents = new LinkedList<Integer>();

    for (DeclaredNodeType type : weCare) {
      if ((type == CommentNT.instance || type instanceof PINT)
          && !ControlFlowConfiguration.current.useCommentPIPropagation())
        continue;
      Node n = fraggle.createPlaceholder(type);
      contents.add(n.getIndex());
    }

    Node result;

    if (maxOne) {
      if (contents.size() == 1) {
        result = fraggle.getNodeAt(contents.get(0));
      } else {
        result = fraggle.createChoiceNode(contents, new Origin("all-model", 0,
            0));
      }
    } else {
      if (contents.size() == 0) {
        result = fraggle.createEpsilonNode();
      } else if (contents.size() == 1) {
        result = fraggle.getNodeAt(contents.get(0));
      } else {
        result = fraggle.createChoiceNode(contents, new Origin("all-model", 0,
            0));
      }
    }
    return result;
  }

  protected Node constructAllModel(SGFragment fraggle,
      SingleTypeXMLClass clazz, boolean maxOne, Set<DeclaredNodeType> typeSet)
      throws XSLToolsSchemaException {
    return ss_constructAllModel(fraggle, clazz, typeSet, maxOne);
  }

  /*
   * Make panic construct: Everything in the type set, and with zero to infinite
   * cardinality...
   */
  public static Node s_constructPanicModel(
      SGFragment fraggle,
      SingleTypeXMLClass clazz,
      boolean maxOne,
      Set<DeclaredNodeType> typeSet) {

    List<Integer> contents = new LinkedList<Integer>();
    for (DeclaredNodeType type : typeSet) {
      if ((type == CommentNT.instance || type instanceof PINT)
          && !ControlFlowConfiguration.current.useCommentPIPropagation())
        continue;
      Node n = fraggle.createPlaceholder(type);
      contents.add(n.getIndex());
    }
    Node almostresult;
    if (contents.size() == 1) {
      almostresult = fraggle.getNodeAt(contents.get(0));
    } else if (contents.isEmpty()) {
      return fraggle.createEpsilonNode();
    } else {
      almostresult = fraggle.createChoiceNode(contents, new Origin(
          "panic-model", 0, 0));
    }
    // If we're promised just a single selection, return a zero-one over result
    if (maxOne) {
      return almostresult;
    } 
    return fraggle.createOneOrMoreNode(almostresult, null);
  }

  protected Node constructZeroInfPanicModel(SGFragment fraggle,
      SingleTypeXMLClass clazz, 
      boolean maxOne, 
      Set<DeclaredNodeType> typeSet) throws XSLToolsSchemaException {
    return s_constructPanicModel(fraggle, clazz, maxOne, typeSet);
  }

  /*
   * Default Attribute is: empty (true for all but: Element )
   */
  public Node constructAttributeFM(SGFragment fraggle,
      SingleTypeXMLClass clazz, Set<DeclaredNodeType> typeSet)
      throws XSLToolsSchemaException {
    //checkEmpty(typeSet);
    return fraggle.createEpsilonNode();
  }

  protected Node seriousAttributeFM(SGFragment fraggle,
      SingleTypeXMLClass clazz, Set<DeclaredNodeType> attrs) {
    List<Integer> idxx = new LinkedList<Integer>();
    for (DeclaredNodeType use : attrs) {
      AttributeUse au = (AttributeUse) use;
      Node n = au.constructFlowModel(fraggle, clazz);
      idxx.add(n.getIndex());
    }
    if (idxx.size() == 0)
      return fraggle.createEpsilonNode();
    if (idxx.size() == 1)
      return fraggle.getNodeAt(idxx.get(0));
    return fraggle.createSequenceNode(idxx, null);
  }

  /*
   * Default parent is: empty (true for all but: Element Text Comment PI
   * Attribute ), really only leaving RootNT, so just to play safe we strip from
   * here.
   */
  /*
   * public Node constructParentModel( SGFragment fraggle, SingleTypeXMLClass
   * clazz, Set<DeclaredNodeType> typeSet, boolean singleMultiplicity) throws
   * XSLToolsSchemaException { return
   * ControlFlowFunctions.createEpsilonNode(fraggle); }
   */
  /*
   * Default child is: empty (true for all but: Root, Element )
   */

  public Node constructChildFM(
      SGFragment fraggle, 
      SingleTypeXMLClass clazz,
      boolean maxOne,
      Set<DeclaredNodeType> typeSet, 
      boolean allowInterleave, ContentOrder order)
      throws XSLToolsSchemaException {
    //checkEmpty(typeSet);
    return fraggle.createEpsilonNode();
  }

  public Node constructSelfFM(SGFragment fraggle, DeclaredNodeType type) {
    return fraggle.createPlaceholder(type);
  }

  /*
   * TODO: Semantics of order..?
   */
  public Node constructAncestorOrSelfFM(SGFragment fraggle,
      SingleTypeXMLClass clazz, 
      boolean maxOne,
      Set<DeclaredNodeType> typeSet,
      DeclaredNodeType selfType, ContentOrder order)
      throws XSLToolsSchemaException {
    Node self = constructSelfFM(fraggle, selfType);
    List<Integer> content = new ArrayList<Integer>(2);
    if (!maxOne) {
      Node ancestors = constructAncestorFM(fraggle, clazz, maxOne, typeSet);
    if (order == ContentOrder.REVERSE) {
      content.add(ancestors.getIndex());
      content.add(self.getIndex());
    } else {
      content.add(self.getIndex());
      content.add(ancestors.getIndex());
    }} else {
      return self;
    }
    return fraggle.createSequenceNode(content, null);
  }

  /*
   * TODO: Semantics of order..?
   */
  
  public Node constructDescendantOrSelfFM(SGFragment fraggle,
      SingleTypeXMLClass clazz, 
      boolean maxOne,
      Set<DeclaredNodeType> typeSet,
      DeclaredNodeType selfType, ContentOrder order)
      throws XSLToolsSchemaException {
    // TODO: Right now, the self type also goes into
    // the descendant set; represented twice.
    // If descendant-of-self were evaluated with separate
    // results for descendant and for self, this could
    // be sharpened somewhat.
    // Again, what is really needed is a proper document
    // order model.
    Node self = constructSelfFM(fraggle, selfType);
    List<Integer> content = new ArrayList<Integer>(2);
if (!maxOne) {
    Node descendants = constructDescendantFM(fraggle, clazz, maxOne, typeSet);
    if (order == ContentOrder.REVERSE) {
      content.add(descendants.getIndex());
      content.add(self.getIndex());
    } else {
      content.add(self.getIndex());
      content.add(descendants.getIndex());
    }
}else {return self;}
    return fraggle.createSequenceNode(content, null);
  }

  /*
   * Default ancestor is: panic (true for all but: Root ) Reason for
   * cardinality: There may be down to zero ancestors of a kind in the set: if
   * they are not on the only ancestor path to the root. There may be inf many
   * of them: If there are cycles in the schema TODO: Take advantage of known
   * ancestor language.
   */
  public Node constructAncestorFM(
      SGFragment fraggle, 
      SingleTypeXMLClass clazz,
      boolean maxOne,
      Set<DeclaredNodeType> typeSet)
      throws XSLToolsSchemaException {
    return constructZeroInfPanicModel(fraggle, clazz, maxOne, typeSet);
  }

  /*
   * Default descendant is: empty (true for all but: Root, Element )
   */
  public Node constructDescendantFM(SGFragment fraggle,
      SingleTypeXMLClass clazz, boolean maxOne, Set<DeclaredNodeType> typeSet) throws XSLToolsSchemaException {
    //checkEmpty(typeSet);
    return fraggle.createEpsilonNode();
  }

  /*
   * public void getGuaranteedDescendants(SingleTypeXMLClass clazz, Set<? super
   * DeclaredNodeType> result) { }
   */

  // public Set<? extends DeclaredNodeType> getAllUses() {
  // return Collections.singleton((DeclaredNodeType)this);
  // }
  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement("node");
    parent.add(me);
    me.addAttribute("type", toString());
  }

  /*
   * Get an unique identifier that can be garbage collected before this object.
   * For weak map keys, mapping to this object, in caches.
   */
  public Object getIdentifier() {
    return toString();
  }

  public String toLabelString() {
    return toString();
  }

  /*
   * just for a stable ordering in listings of node type sets, nothing else...
   * no semantics...
   */
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
  
  @Override
public boolean equals(Object o) {
    /*
     * Experiment: A declaration and its dropoffs are not the same.
    if (o instanceof DropOffDecorator) {
      return equals(((DropOffDecorator)o).getDecorated());
    }*/
    return super.equals(o);
  }

  public char getCharRepresentation(CharNameResolver resolver) {
    // TODO Auto-generated method stub
    return 0;
  }
  
  
  public Collection<DeclaredNodeType> getMyTypes(Collection<DeclaredNodeType> choices) {
    Collection<DeclaredNodeType> result = new HashSet<DeclaredNodeType>();
    for (DeclaredNodeType type : choices) {
      if (type.getOriginalDeclaration().equals(this))
        result.add(type);
    }
    return result;
  }
}
