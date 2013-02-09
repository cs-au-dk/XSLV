package dongfang.xsltools.xmlclass.xsd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Automata;
import dk.brics.misc.Origin;
import dk.brics.relaxng.Param;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.diagnostics.ParseLocationUtil;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.ElementDecl;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.TextNT;

class XSDComplexContentType extends XSDAbstractComplexType {

  final boolean isMixed;

  // TODO: Always safe to rely on the uses of each decl instead??
  final Map<QName, XSDElementDecl> declaredElementContent = new HashMap<QName, XSDElementDecl>();

  // ref names of element decls that are refd with maxOccurs!=0 in this type
  // final Set<QName> possibleContentRefs = new HashSet<QName>();

  XSDComplexContentType(QName name, QName derivedFromRef, List cmOrCmExtension,
      short derivationMethod, String block, String mixed, Origin origin) {
    super(name, derivedFromRef, derivationMethod, cmOrCmExtension, block,
        origin);
    if (mixed == null)
      mixed = "";
    this.isMixed = "true".equals(mixed.trim());
  }

  // Construct derivative from AnyType... by restriction.
  XSDComplexContentType(QName name, List cmOrCmExtension, String block,
      String mixed, Origin origin) {
    super(name, XSDSchemaConstants.ANYTYPE_QNAME, XSDType.DER_RESTRICTION,
        cmOrCmExtension, block, origin);
    if (mixed == null)
      mixed = "";
    this.isMixed = "true".equals(mixed.trim());
  }

  // might not even need to be in API.
  public boolean _isMixed() {
    return isMixed;
  }

  /*
   * Read minOccurs / maxOccurs with default behavior
   */
  static int getOccurs(Element e, QName attName) {
    String attVal = e.attributeValue(attName);
    if (attVal == null)
      return 1;
    try {
      return Integer.parseInt(attVal);
    } catch (NumberFormatException ex) {
      if ("unbounded".equals(attVal))
        return Integer.MAX_VALUE;
      throw ex;
    }
  }

  /*
   * For any SG subfragment modelled over a node n, apply the cardinality by the
   * minOccurs / maxOccurs attrs of n to the subfragment.
   * 
   * If maxOccurs is 0, return an empty sequence. If single is true, we are
   * constructing a model for a step with a [position()=xxx] predicate. It
   * should have 0 or 1 cardinality
   */
  private static Node constructCardinal(Node target, int min, int max,
      SGFragment fraggle, boolean single, Origin containerOrigin) {
    if (min == 1 && max == 1)
      return target;

    if (single) {
      max = 1;
      if (min > 0)
        min = 1;
    }

    if (max == Integer.MAX_VALUE) {
      if (min == 1) {
        return fraggle.constructOneManyCardinal(target, "cardinal-1plus");
      }
      // construct sequence of target min times, followed by target-star
      List<Integer> content = new ArrayList<Integer>(min + 1);
      for (int i = 0; i < min; i++) {
        content.add(target.getIndex());
      }
      Node star = fraggle.constructZeroManyCardinal(target.getIndex(),
          "cardinal");
      content.add(star.getIndex());
      if (content.size() > 1)
        return fraggle.createSequenceNode(content, new Origin(containerOrigin.getFile(), min, 0));
      return fraggle.getNodeAt(content.get(0));
    }
    {
      if (min > max) // pinch off
        return fraggle.createEpsilonNode();

      List<Integer> content = new ArrayList<Integer>(max);
      for (int i = 0; i < min; i++) {
        content.add(target.getIndex());
      }
      if (min != max) {
        Node opt = fraggle.constructOptionalCardinal(target, "cardinal");
        for (int i = min; i < max; i++) {
          content.add(opt.getIndex());
        }
      }
      if (content.size() > 1)
        return fraggle.createSequenceNode(content, containerOrigin);
      return fraggle.getNodeAt(content.get(0));
    }
  }

  private static Node constructCardinal(Node target, Element original,
      SGFragment fraggle, boolean single, Origin origin) {
    int min = getOccurs(original, XSDSchemaConstants.ATTR_MIN_OCCURS_QNAME);
    int max = getOccurs(original, XSDSchemaConstants.ATTR_MAX_OCCURS_QNAME);
    return constructCardinal(target, min, max, fraggle, single, origin);
  }

  /*
   * Determine of a content model element (compositor, whatever...) may have a
   * reachable element decl downward of it (or be it)
   */
  private boolean localMayHaveChildElements() {
    return !declaredElementContent.isEmpty();
  }

  /*
   * Determine of a content model element (compositor, whatever...) in locally
   * declared model may havee a reachable element decl downward of it (or be it)
   * private boolean localMayHaveChildElements() { //for (Object o :
   * cmOrCmExtension) { Element e = (Element) o; if
   * (localMayHaveChildElements(e)) return true; } return false; }
   */

  /*
   * Take the element representing a complete (for derived-by-restricion types)
   * or a partial (for derived-by-extension types) content model, and
   * recursively search it for element references (there are no attribute
   * references). IF maxOccurs is zero somewhere, then disregard the subtree
   * rooted there.
   * 
   * This was dumped from XSDElementDecl.
   */
  private void cookCM(Element e, XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {

    int max = XSDComplexContentType.getOccurs(e,
        XSDSchemaConstants.ATTR_MAX_OCCURS_QNAME);

    if (max != 0) {
      if (e.getQName().equals(XSDSchemaConstants.ELEM_ELEMENT_QNAME)) {

        String selemRef = e.attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);

        QName elemRef = ElementNamespaceExpander
            .qNameForXSLAttributeValue(selemRef, e,
                NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);

        XSDElementDecl elemDecl = schema.getElementDecl(elemRef);

        if (elemDecl == null)
          throw new XSLToolsSchemaException(
              "Element referenced, but declaration is missing: "
                  + Dom4jUtil.clarkName(elemRef));

        // refname to defname, saw that?
        XSDElementDecl ufo = declaredElementContent.get(elemDecl.getQName());
        if (ufo != null && ufo != elemDecl) {
          System.err
              .println("Two declarations in a model had same name but different identity. Dubious! Maybe this in not single-type");
        }
        declaredElementContent.put(elemDecl.getQName(), elemDecl);

        // Verdammt, we can not properly implement block semantics here !!!
        // OK, we can ... if refs are not allowed to have different block, nil,
        // etc.
        // from the one they refer to. Sure as H hope they are not.

      } else if (XSDSchemaConstants.ELEM_ANY_QNAME.equals(e.getQName())) {
        // skip
      } else {
        QName name = e.getQName();

        if (XSDSchemaConstants.ELEM_ATTRIBUTE_QNAME.equals(name)
            || XSDSchemaConstants.ELEM_ANY_ATTRIBUTE_QNAME.equals(name))
          return;

        if (!XSDSchemaConstants.ELEM_GROUP_QNAME.equals(name)
            && !XSDSchemaConstants.ELEM_ALL_QNAME.equals(name)
            && !XSDSchemaConstants.ELEM_CHOICE_QNAME.equals(name)
            && !XSDSchemaConstants.ELEM_SEQUENCE_QNAME.equals(name)) {
          Dom4jUtil.prettyPrint(e, System.err);
          throw new XSLToolsSchemaException(
              "Illegal element in content model: " + Dom4jUtil.clarkName(name));
        }
        List cm = e.elements();

        for (Iterator it = cm.iterator(); it.hasNext();) {
          Element child = (Element) it.next();
          cookCM(child, schema);
        }
      }
    }
  }

  @Override
  public void cookCM(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
    

    if (cmOrCmExtension.isEmpty())
      return;
    cookCM((Element) cmOrCmExtension.get(0), schema);
    // todo ! attributter
  }

  public boolean mayHaveChildElements() {
    if (derivationMethod == DER_EXTENSION) {
      return localMayHaveChildElements() | derivedFrom.mayHaveChildElements();
    }
    return localMayHaveChildElements();
  }

  @Override
public Map<QName, XSDElementDecl> getLocalElementContents() {
    Map<QName, XSDElementDecl> m = new HashMap<QName, XSDElementDecl>();
    getLocalElementContents(m);
    return m;
  }

  /*
   * visse-vasse! public void getLocalElementContents(Map<QName,
   * XSDElementDecl> m) { m.putAll(declaredElementContents); }
   */
  @Override
public Map<QName, XSDElementDecl> getLocalAndInheritedElementContents() {
    Map<QName, XSDElementDecl> m = new HashMap<QName, XSDElementDecl>();
    getLocalAndInheritedElementContents(m);
    return m;
  }

  @Override
public void getLocalElementContents(Map<QName, XSDElementDecl> m) {
    m.putAll(declaredElementContent);
  }

  @Override
public void getLocalAndInheritedElementContents(Map<QName, XSDElementDecl> m) {
    getLocalElementContents(m);
    if (derivationMethod == DER_EXTENSION)
      derivedFrom.getLocalAndInheritedElementContents(m);
  }

  public Automaton getValueOfAutomaton(XSDSchema schema)
      throws XSLToolsSchemaException {
//    System.err.println("Careful, is nil checked?");
    // TODO: If not mixed (remember to look upward for inherited mix),
    // and the content model is absolutely empty, return empty lang aut.
    // Maybe this feature really fits better on the element decls.
    if (isMixed) {
      return Automata.get("string");
    }

    // Not mixed, no declared children: Well, TODO, check if comments
    // and PIs are still an option then.
    if (!mayHaveChildElements())
      return Automaton.makeEmptyString();

    // Approximate: Anything at all.
    return Automata.get("string");
  }

  /*
   * Construct summary graph fragment for the model part in this definition: If
   * derived by restriction, it will be a complete model, if derived by
   * extension, it will describe the extension.
   */
  private int constructLEM(Element e, SGFragment fraggle, XSDSchema schema,
      Set<DeclaredNodeType> weCare, boolean single, ContentOrder _order,
      boolean allowInterleave) throws XSLToolsSchemaException {

    Node n;

    if (e.getQName().equals(XSDSchemaConstants.ELEM_ELEMENT_QNAME)) {
      // we have found an element decl; pick out the declaration...
      String srefname = e.attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);
      try {
        QName refname = ElementNamespaceExpander
            .qNameForXSLAttributeValue(srefname, e,
                NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
        XSDElementDecl ed = schema.getElementDecl(refname);

        // first do the substitution group thing
        Set<? extends ElementDecl> SGRPSubstitutes = ed.getSGRPSubstituteables();
        Set<ElementUse> slam = new HashSet<ElementUse>();

        // then throw in all the DNTs really meant
        for (ElementDecl d : SGRPSubstitutes) {
          if (!d.isAbstract()) // this test should no be necessary...
            slam.addAll(d.getAllUses());
        }

        //slam.retainAll(types);

          List<Integer> substitutes = new LinkedList<Integer>();
            // Oh no oh no, we are not finished .. now we need to consider, for each
            // substitue, its possible dynamic redeclarations...
          for (DeclaredNodeType type2 : weCare) {
            if (slam.contains(type2.getOriginalDeclaration())) {
              substitutes.add(fraggle.createPlaceholder(type2).getIndex());
            }
          }
          if (substitutes.size() > 1)
            n = fraggle.createChoiceNode(substitutes, getOrigin());
          else if (substitutes.size() > 0)
            n = fraggle.getNodeAt(substitutes.get(0));
          else
            n = null; // found an elem ref but since its type has no flow, don't
        // make a placeholder
      } catch (XSLToolsXPathUnresolvedNamespaceException ex) {
        throw new XSLToolsSchemaException(ex);
      }
    }

    else if (e.getQName().equals(XSDSchemaConstants.ELEM_ATTRIBUTE_QNAME)) {
      throw new AssertionError("Die-hard attribute!");
    }

    else if (e.getQName().equals(XSDSchemaConstants.ELEM_ANY_ATTRIBUTE_QNAME)) {
      // n = constructAnyAttribute(e, fraggle, schema);
      throw new AssertionError("Die-hard anyAttribute!");
    }

    /*
     * Approximate to the possible RESULT of the TRANSFORM (not to the input) :
     * Anything at all.
     */
    else if (e.getQName().equals(XSDSchemaConstants.ELEM_ANY_QNAME)) {
      n = fraggle.getAnyElement(allowInterleave);
    }

    else if (e.getQName().equals(XSDSchemaConstants.ELEM_SEQUENCE_QNAME)) {
      List<Integer> contents = new ArrayList<Integer>(e.elements().size());
      for (Iterator it = e.elements().iterator(); it.hasNext();) {
        Element ch = (Element) it.next();
        int r = constructLEM(ch, fraggle, schema, weCare, single, _order, false);
        if (r != -1)
          contents.add(r);
      }

      if (contents.size() == 0)
        n = null;

      else if (contents.size() != 1)
        switch (_order) {
        case FORWARD:
          n = fraggle.createSequenceNode(contents, ParseLocationUtil.getOrigin(
              getOrigin(), e, "xs:sequence"));
          break;
        case REVERSE:
          List<Integer> contents2 = new ArrayList<Integer>(contents.size());
          for (int i = contents.size() - 1; i >= 0; i--) {
            contents2.add(contents.get(i));
          }
          n = fraggle.createSequenceNode(contents2, ParseLocationUtil
              .getOrigin(getOrigin(), e, "xs:sequence"));
          break;
        case RANDOM:
          n = fraggle
              .createInterleaveNode(contents, allowInterleave,
                  ParseLocationUtil.getOrigin(getOrigin(), e,
                      "xs:random-sequence"));
          break;
        default:
          throw new AssertionError("HUH???");
        }
      else
        n = fraggle.getNodeAt(contents.get(0));
    }

    else if (e.getQName().equals(XSDSchemaConstants.ELEM_CHOICE_QNAME)) {
      List<Integer> contents = new ArrayList<Integer>(e.elements().size());
      for (Iterator it = e.elements().iterator(); it.hasNext();) {
        Element ch = (Element) it.next();
        int r = constructLEM(ch, fraggle, schema, weCare, single, _order,
            allowInterleave);
        if (r != -1)
          contents.add(r);
      }

      if (contents.size() == 0)
        n = null;
      else if (contents.size() != 1)
        n = fraggle.createChoiceNode(contents, ParseLocationUtil.getOrigin(
            getOrigin(), e, "xs:choice"));
      else
        n = fraggle.getNodeAt(contents.get(0));
    }

    else if (e.getQName().equals(XSDSchemaConstants.ELEM_ALL_QNAME)) {
      List<Integer> contents = new ArrayList<Integer>(e.elements().size());
      for (Iterator it = e.elements().iterator(); it.hasNext();) {
        Element ch = (Element) it.next();
        int r = constructLEM(ch, fraggle, schema, weCare, single, _order,
            allowInterleave);
        if (r != -1)
          contents.add(r);
      }

      if (contents.size() != 1)
        n = fraggle.createInterleaveNode(contents, allowInterleave,
            ParseLocationUtil.getOrigin(getOrigin(), e, "xs:all"));
      else
        n = fraggle.getNodeAt(contents.get(0));

    } else
      throw new XSLToolsSchemaException("Unhandled case: "
          + e.getQualifiedName());

    if (n == null)
      return -1;

    return constructCardinal(n, e, fraggle, single, getOrigin()).getIndex();
  }

  /*
   * Construct complete summary graph model, including what this type derived
   * from by extension.
   */
  // @Override
  @Override
boolean constructInheritedElementModel(SGFragment fraggle,
      Set<DeclaredNodeType> types, XSDSchema schema, List<Integer> tanker,
      boolean single, ContentOrder order, boolean allowInterleave)
      throws XSLToolsSchemaException {

    boolean result = false;
    /*
     * we want to do this pre-order, so that the local model -- which is merely
     * an extension contribution -- comes last.
     */
    if (derivationMethod == DER_EXTENSION) {
      result = ((XSDAbstractType)derivedFrom).constructInheritedElementModel(fraggle, types,
          schema, tanker, single, order, allowInterleave);
    }

    /*
     * Empty model, quit!
     */

    if // (cmOrCmExtension.size() == 0) {
    (!mayHaveChildElements()) {
      return result;
    }

    if (cmOrCmExtension.size() == 0) {
      return result;
    }

    Element e = (Element) cmOrCmExtension.get(0);

    if (e.getQName().equals(XSDSchemaConstants.ELEM_ATTRIBUTE_QNAME)
        || e.getQName().equals(XSDSchemaConstants.ELEM_ANY_ATTRIBUTE_QNAME)) {
      // attribute decls / refs come last (but before anyAttribute).
      // If there is a content model, then it comes before the attributes.
      // TODO: See that the empty complexType is handled correctly!
      throw new AssertionError("Die-hard attribute declaration!");
    }

    if (cmOrCmExtension.size() != 1) {
      System.err
          .println("Malformed XSD?? More than one nonattribute, nonannotation member of a content model");
    }

    int r = constructLEM(e, fraggle, schema, types, single, order,
        allowInterleave);

    if (r != -1) {
      tanker.add(r);
      return true;
    }

    return false;
  }

  /*
   * Seems to take the grand scenic tour of inherited contents and text,
   * comment, PI.
   */
  public Node constructChildFM(SGFragment fraggle,
      XSDSchema schema, boolean maxOne, 
      Set<DeclaredNodeType> types,
      ContentOrder order, 
      boolean allowInterleave)
      throws XSLToolsSchemaException {

    List<Integer> tanker = new LinkedList<Integer>();

    constructInheritedElementModel(fraggle, types, schema, tanker, maxOne,
        order, allowInterleave);

    Node seq;

    if (tanker.isEmpty()) {
      seq = fraggle.createEpsilonNode();
    } else if (tanker.size() == 1) {
      seq = fraggle.getNodeAt(tanker.get(0));
    } else
      switch (order) {
      case FORWARD:
        seq = fraggle.createSequenceNode(tanker, getOrigin());
        break;
      case REVERSE:
        List<Integer> tanker2 = new ArrayList<Integer>(tanker.size());
        for (int i = tanker.size() - 1; i >= 0; i--) {
          tanker2.add(tanker.get(i));
        }
        seq = fraggle.createSequenceNode(tanker2, getOrigin());
        break;
      case RANDOM:
        seq = fraggle.createInterleaveNode(tanker, allowInterleave, getOrigin());
        break;
      default:
        throw new AssertionError("HUH??");
      }

    if (isMixed && types.contains(TextNT.chameleonInstance)) {
      List<Integer> contents = new LinkedList<Integer>();
      contents.add(seq.getIndex());
      Node texer = fraggle.createPlaceholder(new TextNT(), getOrigin());
      contents.add(texer.getIndex());
      if (!tanker.isEmpty())
        seq = fraggle.createInterleaveNode(contents, allowInterleave, getOrigin());
      else 
        seq = texer;
      //seq = fraggle.createSequenceNode (contents, new Origin("mixed", 0, 0));
    }

    return fraggle.wrapInCommentPIConstruct(seq, types
        .contains(CommentNT.instance), getOrigin());
  }

  public boolean mayContainTextNodes() {
    return isMixed;
  }

  public Automaton languageOfTextNodes(XSDSchema schema) {
    if (isMixed)
      return Automata.get("string");
    return Automaton.makeEmpty();
  }

  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    if (skipDiagnostics())
      return;
    Element me = fac.createElement(getClass().getSimpleName());
    parent.add(me);
    moreDiagnostics(me, fac, configuration);
    me.addAttribute("isMixed", Boolean.toString(isMixed));
  }

  public Automaton makeValueOfAutomaton(XSDSchema clazz, List<Param> facets) throws XSLToolsSchemaException {
    return getValueOfAutomaton(clazz);
  }
}
