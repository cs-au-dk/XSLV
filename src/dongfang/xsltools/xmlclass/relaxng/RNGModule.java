/*
 * dongfang M. Sc. Thesis
 * Created on 2005-09-26
 */
package dongfang.xsltools.xmlclass.relaxng;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.xmlclass.CharGenerator;
import dongfang.xsltools.xmlclass.schemaside.AttributeDecl;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.PINT;

/**
 * Representation of a RNG grammar.
 * 
 * @author dongfang
 */
public class RNGModule extends SingleTypeXMLClass {

  private Map<String, RNGElementDecl> definitions = new HashMap<String, RNGElementDecl>();

  RNGModule(Document xml) throws XSLToolsException {
    init(xml);
  }

  void init(Document xml) throws XSLToolsException {

    Element documentElement = xml.getRootElement();

    if (!documentElement.getNamespaceURI().equals(
        RelaxNGConstants.NAMESPACE_URI))
      throw new XSLToolsException(
          "Document seems not to be a Relax NG Schema instance");

    /*
     * If the document is not rooted in a grammar, make it that
     */
    /*
     * if (!documentElement.getName().equals(RelaxNGConstants.ELEM_GRAMMAR)) {
     * Element grammar = fac.createElement(RelaxNGConstants.ELEM_GRAMMAR_QNAME);
     * Element start = fac.createElement(RelaxNGConstants.ELEM_START_QNAME);
     * grammar.add(start); documentElement.detach(); start.add(documentElement);
     * xml.add(grammar); documentElement = grammar; }
     */
    /*
     * Sniff out the start element
     */
    String startName = null;

    /*
     * Make up start and defintion sets
     */

    for (Iterator child = documentElement.elementIterator(); child.hasNext();) {
      Element temp = (Element) child.next();

      if (temp.getQName().equals(RelaxNGConstants.ELEM_START_QNAME)) {
        if (startName != null)
          throw new XSLToolsException("More than one start element!");
        Element startDef = (Element) temp.elements().get(0);
        startName = startDef.attributeValue(RelaxNGConstants.ATTR_NAME_QNAME);

      } else if (temp.getQName().equals(RelaxNGConstants.ELEM_DEFINE_QNAME)) {
        String name = temp.attributeValue(RelaxNGConstants.ATTR_NAME_QNAME);
        if (name == null)
          throw new XSLToolsException("Definition had no name!");
        Element decl = (Element) temp.elements().get(0);
        QName qname = getElementDeclarationName(decl);

        RNGElementDecl element = new RNGElementDecl(qname, decl, this);

        definitions.put(name, element);

      } else {
        System.err.println("Unhandled top level element: " + temp);
      }
    }

    if (startName != null) {
      this.documentElementDecl = resolve(startName);
    } else
      throw new XSLToolsException(
          "Grammar had no start element (fix-me; I don't really look so hard for them)!");

    CharGenerator kanone = new CharGenerator();

    // Map<QName, Character> characterNames = new HashMap<QName, Character>();

    for (Iterator<RNGElementDecl> decls = definitions.values().iterator(); decls
        .hasNext();) {
      // String definedElementName = definition.next();
      // RNGElementDecl decl = definitions.get(definedElementName);
      RNGElementDecl decl = decls.next();
      decl.processContentModel(this);
      decl.fixupCharacterNames(this, kanone);
    }
    // now everybody has a char name, and we should be able to assign 'em a
    // ancestor language automaton
    // how to do ... well top down I presume is the more clever ... inherit and
    // extend parent's automaton.
    // this.schemaATSPathAutomaton = new Automaton();

    State commentOveroverroot = new State();
    State commentOverroot = new State();
    State commentAccept = new State();
    commentAccept.setAccept(true);
    commentNodeTypeAutomaton.setInitialState(commentOveroverroot);

    commentOveroverroot.addTransition(new Transition(CharGenerator
        .getRootChar(), commentOverroot));

    State PIOveroverroot = new State();
    State PIOverroot = new State();
    State PIAccept = new State();
    PIAccept.setAccept(true);
    PINodeTypeAutomaton.setInitialState(PIOveroverroot);

    PIOveroverroot.addTransition(new Transition(CharGenerator.getRootChar(),
        PIOverroot));

    State textOveroverroot = new State();
    State textOverroot = new State();
    State textAccept = new State();
    textAccept.setAccept(true);
    textNodeTypeAutomaton.setInitialState(textOveroverroot);

    textOveroverroot.addTransition(new Transition(CharGenerator.getRootChar(),
        textOverroot));

    State overoverroot = new State();
    State overroot = new State();

    // Automaton allElementsAutomaton = new Automaton();
    this.schemaATSPathAutomaton.setInitialState(overoverroot);

    overoverroot.addTransition(new Transition(CharGenerator.getRootChar(),
        overroot));

    ((RNGElementDecl) documentElementDecl).constructATSPathAutomaton(overroot,
        commentOverroot, commentAccept, PIOverroot, PIAccept, textOverroot,
        textAccept);

    for (RNGElementDecl decl : definitions.values()) {
      // This is only for the abstract evaluation, really.
      addElementDecl(decl);
      // Plug in element (and, hopefully, soon attribute) individual automata.
      decl.snapshootATSPathAutomaton(this);

      if (decl.acceptsCommentsPIs()) {
        // Add element as possible parent:
        commentPIParents.add(decl);
      }

      // Possibly add element as possible parent of PCDATA:
      if (decl.mayContainTextNodes()) {
        // Add element as possible parent:
        pcDataParents.add(decl);
      }
    }

    // for the valid ancestor path automaton for the whole grammar, set all
    // states to true
    // TODO: It also needs to accept comment and PI for any bloody element +
    // root,
    // as well as attributes and PCData. However it does accept root already
    // TODO: On the other hand there is no individual automaton for root.
    // It should be possible to make an easy automaton for comments and PIs:
    // Just make a (rootchar | elementregexp) . (commentchar | PIchar) regexp,
    // and then cook an automaton on that.
    // Maybe this hack is just confusing.

    overroot.setAccept(true);
    Automaton acceptRoot = (Automaton) schemaATSPathAutomaton.clone();
    overroot.setAccept(false);
    this.rootNodeTypeAutomaton = acceptRoot;

    commentOverroot.addTransition(new Transition(CommentNT.instance
        .getCharRepresentation(this), commentAccept));
    PIOverroot.addTransition(new Transition(PINT.chameleonInstance
        .getCharRepresentation(this), PIAccept));

    fixUpAutomata(overoverroot, overroot);

    elementRegExp = kanone.getElementRegExp();
    attributeRegExp = kanone.getAttributeRegExp();
  }

  static QName getElementDeclarationName(Element decl) {
    Element nameCarrier = (Element) decl.elements().get(0);
    String ns = decl.attributeValue(RelaxNGConstants.ATTR_NS_QNAME);
    String elementName = nameCarrier.getText();
    return QName.get(elementName, ns);
  }

  RNGElementDecl resolve(String name) {
    return definitions.get(name);
  }

  public AttributeDecl getAttributeDecl(QName elementName,
      QName attributeName) {
    // TODO Auto-generated method stub
    return null;
  }

  int getMagicnumber() {
    LinkedList<String> ss = new LinkedList<String>();
    LinkedList<ElementUse> decls = new LinkedList<ElementUse>();

    for (RNGElementDecl elem : definitions.values()) {
      decls.add(elem);
      Character C = elem.getCharRepresentation(null);// characterNames.get(elem.getName());
      // if (ss[i]==null)
      ss.add(new String(new char[] { C.charValue() }));
    }

    int n = 0;

    while (!ss.isEmpty()) {
      preserveOnlyDupes(ss, decls);
      if (hasDupes(decls))
        return Integer.MAX_VALUE;
      if (ss.size() != decls.size()) {
        System.err.println("underligt");
      }
      n++;
      ListIterator<String> ssi = ss.listIterator();
      ListIterator<ElementUse> declsi = decls.listIterator();
      while (ssi.hasNext()) {
        String s = ssi.next();
        /* ElementUse decl = */ declsi.next();
        Collection<ElementUse> parents = null;// decl.getParentUses();
        boolean first = true;
        for (ElementUse parent : parents) {
          char c = parent.getCharRepresentation(null);
          if (first) {
            ssi.set(s + new String(new char[] { c }));
            declsi.set(parent);
            first = false;
          } else {
            ssi.add(s + new String(new char[] { c }));
            declsi.add(parent);
          }
        }
      }
    }
    return n;
  }

  private void preserveOnlyDupes(LinkedList<String> ss,
      LinkedList<ElementUse> decls) {
    ListIterator<String> ssi = ss.listIterator();
    Set<String> check = new HashSet<String>();
    Set<String> dupes = new HashSet<String>();

    while (ssi.hasNext()) {
      String s = ssi.next();
      if (check.contains(s))
        dupes.add(s);
      else
        check.add(s);
    }

    ssi = ss.listIterator();
    ListIterator<ElementUse> declsi = decls.listIterator();

    while (ssi.hasNext()) {
      String s = ssi.next();
      declsi.next();
      if (!dupes.contains(s)) {
        ssi.remove();
        declsi.remove();
      }
    }
  }

  private boolean hasDupes(LinkedList<ElementUse> elementDecls) {
    return new HashSet<ElementUse>(elementDecls).size() < elementDecls.size();
  }

  /*
   * public Set<LinkedList<Integer>> getChoicePaths() { Set<LinkedList<Integer>>
   * result = new HashSet<LinkedList<Integer>>(); LinkedList<Integer> down =
   * new LinkedList<Integer>(); getChoicePaths(startElement, result, down);
   * return result; }
   * 
   * boolean getChoicePathsChildren(Element e, Set<LinkedList<Integer>>
   * result, LinkedList<Integer> down) { boolean foundChoose = false; for
   * (Iterator<Element> it = e.elementIterator(); it.hasNext();) { if
   * (getChoicePaths(it.next(), result, down)) foundChoose = true; } return
   * foundChoose; }
   * 
   * boolean getChoicePaths(Element e, Set<LinkedList<Integer>> result,
   * LinkedList<Integer> down) { if
   * (e.getNamespaceURI().equals(RelaxNGConstants.NAMESPACE_URI)) { if
   * (e.getName().equals(RelaxNGConstants.ELEM_REF)) { String name =
   * e.attributeValue(RelaxNGConstants.ATTR_NAME_QNAME); e = resolve(name,
   * SELF); }
   * 
   * if (e.getName().equals(RelaxNGConstants.ELEM_CHOICE)) { int i = 0; boolean
   * foundChoose = false; for (Iterator<Element> it = e.elementIterator();
   * it.hasNext();) { LinkedList<Integer> temp = new LinkedList<Integer>(down);
   * temp.add(new Integer(i++)); if (getChoicePaths(it.next(), result, temp))
   * foundChoose = true; else result.add(temp); } return true; }
   * 
   * else if (e.getName().equals(RelaxNGConstants.ELEM_ELEMENT) ||
   * e.getName().equals(RelaxNGConstants.ELEM_EMPTY) ||
   * e.getName().equals(RelaxNGConstants.ELEM_ATTRIBUTE)) { // result.add(down);
   * return getChoicePathsChildren(e, result, down); }
   * 
   * else if (e.getName().equals(RelaxNGConstants.ELEM_GROUP) ||
   * e.getName().equals(RelaxNGConstants.ELEM_ONE_OR_MORE) ||
   * e.getName().equals(RelaxNGConstants.ELEM_DEFINE)) { return
   * getChoicePathsChildren(e, result, down); }
   * 
   * else if (e.getName().equals(RelaxNGConstants.ELEM_START)) { return
   * getChoicePathsChildren(e, result, down); } else { // throw new
   * AssertionError("Unhandled Relax NG Element: " + e); return false; } }
   * return false; }
   * 
   * public Set<Element> getAttributeDeclarations(List<Integer> path) { Set<Element>
   * result = new HashSet<Element>(); getAttributeDeclarations(startElement,
   * result, new LinkedList<Integer>(path)); return result; }
   * 
   * void getAttributeDeclarations(Element e, Set<Element> result, LinkedList<Integer>
   * path) { if (e.getNamespaceURI().equals(RelaxNGConstants.NAMESPACE_URI)) {
   * 
   * if (e.getName().equals(RelaxNGConstants.ELEM_REF)) { String name =
   * e.attributeValue(RelaxNGConstants.ATTR_NAME_QNAME); e = resolve(name,
   * SELF); getAttributeDeclarations(e, result, path); }
   * 
   * else if (e.getName().equals(RelaxNGConstants.ELEM_CHOICE)) { Integer I =
   * path.getFirst(); e = (Element) e.elements().get(I.intValue());
   * path.removeFirst(); // have to do a recurse here, as choices may be nested
   * directly. getAttributeDeclarations(e, result, path); }
   * 
   * else if (e.getName().equals(RelaxNGConstants.ELEM_ATTRIBUTE)) {
   * result.add(e); }
   * 
   * else if (e.getName().equals(RelaxNGConstants.ELEM_GROUP) ||
   * e.getName().equals(RelaxNGConstants.ELEM_ONE_OR_MORE) ||
   * e.getName().equals(RelaxNGConstants.ELEM_ZERO_OR_MORE) ||
   * e.getName().equals(RelaxNGConstants.ELEM_START)) {
   * 
   * for (Iterator<Element> it = e.elementIterator(); it.hasNext();) {
   * getAttributeDeclarations(it.next(), result, path); } /* } } else { throw
   * new AssertionError("Unhandled Relax NG Element: " + e); } } } }
   */

  /*
   * public Element getDeclaration(LinkedList<Integer> path) { return
   * getDeclaration(new LinkedList<Integer>(path), startElement); }
   * 
   * public Element getDeclaration(LinkedList<Integer> path, Element decl) { if
   * (decl.getName().equals(RelaxNGConstants.ELEM_REF)) { String name =
   * decl.attributeValue(RelaxNGConstants.ATTR_NAME_QNAME); decl = resolve(name,
   * SELF); }
   * 
   * if (decl.getQName().equals(RelaxNGConstants.ELEM_CHOICE_QNAME)) { Integer I =
   * path.removeFirst(); int i = I.intValue(); Element cont =
   * (Element)decl.elements().get(i); return getDeclaration(path, cont); } else {
   * Element result = (Element)decl.clone(); for (Iterator<Element> children =
   * decl.elementIterator(); children.hasNext();) { Element child =
   * children.next(); result.add(getDeclaration(path, child)); } return result; } }
   */

  /*
   * public ElementDecl cheatGetx(QName qn) { for (ElementDecl decl :
   * allElementDecls) { if (decl.getQName().equals(qn)) return decl; } return
   * null; }
   */
}
