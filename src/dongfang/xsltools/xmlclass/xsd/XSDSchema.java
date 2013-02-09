/*
 * Created on Jun 3, 2005
 */
package dongfang.xsltools.xmlclass.xsd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.UniqueNameGenerator;
import dongfang.xsltools.xmlclass.CharGenerator;
import dongfang.xsltools.xmlclass.schemaside.AttributeDecl;
import dongfang.xsltools.xmlclass.schemaside.ElementDecl;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.PINT;

/**
 * @author dongfang
 */
class XSDSchema extends SingleTypeXMLClass {

  /*
   * A list, not a Set, only for the ease of namespace collision checking: Some
   * of these XSDSchemaDocuments may share a target namespace. If a Map from
   * target namespaces to documents had been used, documents with the same
   * target namespace would conflict. Instead, a list is simply used, and no
   * documents are lost.
   * 
   * How to implement redefine: Replace this set by a map (target namespace to
   * XSDAbstractDocument). Make a composite XSDAbstractDocument implementation,
   * that represents everything in one target namespace. It should then be easy
   * to implement scoped resolution (redefiners will resolve in whatever they
   * redefine).
   */
  private final Map<String, XSDAbstractDocument> rawDocuments = 
    new HashMap<String, XSDAbstractDocument>();

  private XSINilAttr nilAttr = new XSINilAttr();

  // TODO : See How to implement redefine above
  XSDElementDecl getElementDecl(QName name) {
    for (XSDAbstractDocument d : rawDocuments.values()) {
      XSDElementDecl decls = d.getElementDecl(name);
      if (decls != null)
        return decls;
    }
    return null;
  }

  // TODO : See How to implement redefine above
  XSDAttributeDecl getAttributeDecl(QName name) {
    for (XSDAbstractDocument d : rawDocuments.values()) {
      XSDAttributeDecl decl = d.getAttributeDecl(name);
      if (decl != null)
        return decl;
    }
    return null;
  }

  // TODO : See How to implement redefine above
  XSDType getTypedef(QName name) {
    for (XSDAbstractDocument d : rawDocuments.values()) {
      XSDType def = d.getTypedef(name);
      if (def != null)
        return def;
    }
    return null;
  }

  // TODO : See How to implement redefine above
  XSDElementDecl getToplevelElementDecl(QName name) {
    for (XSDAbstractDocument d : rawDocuments.values()) {
      XSDElementDecl decl = d.getToplevelElementDecl(name);
      if (decl != null)
        return decl;
    }
    return null;
  }

  // TODO : See How to implement redefine above
  void prepareBuiltinTypeDocument() throws XSLToolsException {
    XSDSchemaDocument bi = XSDBuiltinType.makeBuiltinTypeDocument();
    rawDocuments.put("urn:builtin-types", bi);
  }

  /*
   * | Make the XSDType instances corresponding to all declared types. That
   * includes the built-in types in XML Schema. Nothing is done yet to fix up
   * their references to each other, or to anything else for that matter (that
   * will wait till they are all created!)
   */
  void makeDefs() throws XSLToolsXPathUnresolvedNamespaceException,
      XSLToolsSchemaException {

    // the ones declared in the schema documents
    for (XSDAbstractDocument doc : rawDocuments.values()) {
      doc.makeTypeDefs(this);
    }
  }

  /*
   * Make all element and atribute declarations.
   */
  void makeDecls() throws XSLToolsXPathUnresolvedNamespaceException,
      XSLToolsSchemaException {
    // First, a little massage on the DOM trees to make them uniform
    // (put on default derivations if not explicit, remove redundant
    // type elements etc.)
    for (XSDAbstractDocument doc : rawDocuments.values()) {
      doc.prepareDecls();
    }
    // Make the declarations
    for (XSDAbstractDocument doc : rawDocuments.values()) {
      doc.makeDecls(this);
    }
  }

  /*
   * Make all element and atribute declarations.
   */
  void cookTypes() throws XSLToolsXPathUnresolvedNamespaceException,
      XSLToolsSchemaException {
    // First, a little massage on the DOM trees to make them uniform
    // (put on default derivations if not explicit, remove redundant
    // type elements etc.)
    for (XSDAbstractDocument doc : rawDocuments.values()) {
      doc.cookCM(this);
    }
  }

  @Override
protected void addElementDecl(ElementDecl decl) {
    /*
     * An XML Schema special feature: If we can find another same name, same
     * type declaration, then they declare the same shit.. However, they may
     * have different nillable property substitution group block restrictions ..
     * so we do NOT merge them (bloody language; it wouldn't have been all that
     * hard).
     */
    super.addElementDecl(decl);
  }

  @Override
protected void addAttributeDecl(AttributeDecl decl) {
    super.addAttributeDecl(decl);
  }

  /*
   * Check that documents don't have colliding definitions / declarations.
   * Really just an experiment to determine the importance, or lack of it, of
   * implementing redefine... Works by testing the i'th of all n documents
   * against number i+i..n
   */
  void checkDocumentCompatibility() {
    List<String> keyz = new ArrayList<String>(rawDocuments.keySet());

    for (int i = 0; i < keyz.size(); i++) {
      String k1 = keyz.get(i);
      XSDAbstractDocument d1 = rawDocuments.get(k1);
      for (int j = i + 1; j < keyz.size(); j++) {
        String k2 = keyz.get(j);
        XSDAbstractDocument d2 = rawDocuments.get(k2);
        if (!d1.getTargetNamespaceURI().equals(d2.getTargetNamespaceURI()))
          continue;
        d1.checkDocumentCompatibility(d2);
        d2.checkDocumentCompatibility(d1);
      }
    }
  }

  void addSchemaDocument(String id, XSDSchemaDocument document) {
    rawDocuments.put(id, document);
  }

  /*
   * Get an attribute group definition from the first document found that
   * defined it..
   */
  Element getAttributeGroupDef(QName name) {
    for (XSDAbstractDocument doc : rawDocuments.values()) {
      if (doc.getTargetNamespaceURI().equals(name.getNamespaceURI())) {
        Element sniff = doc.getAttributeGroupDef(name);
        if (sniff != null)
          return sniff;
      }
    }
    return null;
  }

  /*
   * Get a model group definition from the first document found that defined
   * it..
   */
  Element getModelGroupDef(QName name) {
    for (XSDAbstractDocument doc : rawDocuments.values()) {
      if (doc.getTargetNamespaceURI().equals(name.getNamespaceURI())) {
        Element sniff = doc.getModelGroupDef(name);
        if (sniff != null)
          return sniff;
      }
    }
    return null;
  }

  /**
   * Return the non-annotation contents of an element. It is assumed that the
   * container has at most one non-annotation child (as per the Recommendation).
   * (in fact the annotation check should not be necessary... they have been
   * killed)
   * 
   * @param container
   * @return
   */
  /*
   * private static Element getCM(Element container) { int contentChildCount =
   * 0; Element result = null; for (Iterator contentIterator =
   * container.elements().iterator(); contentIterator .hasNext();) { Element
   * element = (Element)contentIterator.next(); if
   * (element.getName().equals("annotation")) continue; result = element;
   * contentChildCount++; } if (contentChildCount > 1)
   * System.err.println("Achtung!! More than one content element!"); return
   * result; }
   */
  /**
   * An attempt at unifying all the ref and local content model stuff.
   * 
   * @param container
   * @param referringAttVal
   * @return
   * @throws XSLToolsXPathUnresolvedNamespaceException
   */
  /*
   * public Element getElementDeclaration(Element element) throws
   * XSLToolsXPathUnresolvedNamespaceException { String refAttVal = element
   * .attributeValue(XSDSchemaConstants.ATTR_NAME_QNAME); if (refAttVal != null) {
   * QName qname = ElementNamespaceExpander.qNameForXSLAttributeValue(
   * refAttVal, element,
   * NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE); for
   * (XSDSchemaDocument doc : rawDocuments) { if
   * (doc.getTargetNamespaceURI().equals(qname.getNamespaceURI())) { Element e =
   * doc.getElementDeclaration(qname); if (e != null) return getDeclaration(e); } } }
   * return getDeclaration(element); }
   */
  /*
   * Make copies of namespace nodes on every element in their scope, as to
   * facilitate safe and easy splitting of the tree. NOTE: Strictly speaking, it
   * is not necessary to tear the DOM tree apart in order to get the
   * localization effect (declarations without nested definitions and
   * definitions without nested declarations), BUT conceptually tearing apart is
   * easier to understand. One COULD just add ref attributes WITHOUT tearing.
   * There might be a small performance advantage -- but then again it might be
   * confusing, and when traversing the structures later on, more conplicated
   * stop-recursion tests will be required.
   */
  void flattenDocuments() throws XSLToolsXPathUnresolvedNamespaceException,
      XSLToolsSchemaException {
    // Set (as a map) of inherited-from-above namespace bindings
    for (XSDAbstractDocument doc : rawDocuments.values()) {
      /*
       * Brutally copy namespace binding thruoghout their scope, remove
       * annotations, and resolve model and attribute groups.
       */
      doc.prepareDOM4Flattening(this);
    }

    /*
     * This is for making fresh names for fresh ref's
     */
    UniqueNameGenerator names = new UniqueNameGenerator();

    for (XSDAbstractDocument doc : rawDocuments.values()) {
      doc.killGroupDefs();
    }

    for (XSDAbstractDocument doc : rawDocuments.values()) {
      doc.flatten(names);
    }
  }

  boolean substitutionGroupOK(XSDElementDecl c, XSDElementDecl d,
      short constraint) {
    if (c == d)
      return true;

    // if constraint disallows substitution, forget about it.
    if ((constraint & XSDType.DECL_DER_SUBSTITUTION) != 0)
      return false;

    // if d never was in c's substitution group, forget it.
    if (!c.getSGRPSubstituteables().contains(d))
      return false;

    return d.declaredType.deriveableFrom(c.declaredTypeName);
    /*
     * short typeDerMethods = d.declaredType
     * .derivationMethodsFrom(c.declaredTypeName);
     * 
     * short typeDersProhibited = d.declaredType
     * .getBlockedDerivationsFrom(c.declaredTypeName);
     * 
     * if ((typeDersProhibited & XSDType.DER_BLOCK_MEANINGLESS) != 0) {
     * System.err .println("Meaningless type derivation from type of " +
     * Dom4jUtil.clarkName(d.getQName()) + " to type of " +
     * Dom4jUtil.clarkName(c.getQName()) + " the latter was still in the
     * substitution group of the former??!?"); return false; } return
     * (typeDerMethods & (typeDersProhibited | constraint)) == 0;
     */

  }

  /*
   * Fix up substitution groups -- that is, for each substitution group HEAD,
   * let its declaration know about the substitutions declared. This does not
   * pay into account the blocking mechanism -- that will have to be handled
   * elsewhere (or introduced here).
   */
  void initSubstitutionGrps() throws XSLToolsSchemaException {

    // First, make every substitution group head know all of
    // its substitutions, including itself.
    // It is assumed at this time that all element declarations'
    // substitute sets are empty, and that all element declarations
    // either have a substitution group name (given explicitly), or
    // if not given explicitly, null.

    boolean stillHasUnresolvedUnknownTypes = true;
    while (stillHasUnresolvedUnknownTypes) {

      stillHasUnresolvedUnknownTypes = false;

      // for each decl
      for (ElementDecl ed : getAllElementDecls()) {
        XSDElementDecl xed = (XSDElementDecl) ed;

        QName sgrpq = xed.substitutionGroupName;

        if (sgrpq == null) {
          if (!xed.isAbstract()) {
            xed.addToSubstitutionGroup(xed);
          }

          if (xed.declaredTypeName
              .equals(XSDSchemaConstants.UNSPECIFIED_SIMPLETYPE_QNAME)) {
            xed.declaredTypeName = XSDSchemaConstants.ANYSIMPLETYPE_QNAME;
            xed.declaredType = (XSDAbstractType) getTypedef(XSDSchemaConstants.ANYSIMPLETYPE_QNAME);
          }

          if (xed.declaredTypeName
              .equals(XSDSchemaConstants.UNSPECIFIED_TYPE_QNAME)) {
            xed.declaredTypeName = XSDSchemaConstants.ANYTYPE_QNAME;
            xed.declaredType = (XSDAbstractType) getTypedef(XSDSchemaConstants.ANYTYPE_QNAME);
          }
        } else {
          XSDElementDecl head = getToplevelElementDecl(sgrpq);
          if (head == null)
            throw new XSLToolsSchemaException(
                "substituteGroup refers to a non-toplevel element decl (or a nonexisting one!): "
                    + Dom4jUtil.clarkName(sgrpq));
          head.addToSubstitutionGroup(xed);

          boolean headEquallyConfused = head.declaredTypeName
              .equals(XSDSchemaConstants.UNSPECIFIED_TYPE_QNAME)
              || head.declaredTypeName
                  .equals(XSDSchemaConstants.UNSPECIFIED_SIMPLETYPE_QNAME);

          if (headEquallyConfused) {
            // head was unknown type too, must suck that from head of head in
            // next iteration...
            stillHasUnresolvedUnknownTypes = true;
          } else {
            if (xed.declaredTypeName
                .equals(XSDSchemaConstants.UNSPECIFIED_TYPE_QNAME)
                || xed.declaredTypeName
                    .equals(XSDSchemaConstants.UNSPECIFIED_SIMPLETYPE_QNAME)) {
              xed.declaredTypeName = head.declaredTypeName;
              xed.declaredType = head.declaredType;
            }
          }
        }
      }
    }

    // Then, pull in susbtitutions of substitutions etc., making a maximum
    // set of potential substitutions for each element decl, as per
    // Schema Component Constraint: Substitution Group OK (Transitive):
    boolean somethingHappened = false;
    Set<ElementDecl> delta = new HashSet<ElementDecl>();
    do {
      somethingHappened = false;
      for (ElementDecl ed : getAllElementDecls()) {
        XSDElementDecl xed = (XSDElementDecl) ed;
        Set<? extends ElementDecl> firstOrderSubsts = xed
            .getSGRPSubstituteables();

        // suck in second order to first order set
        for (ElementDecl sucker : firstOrderSubsts) {
          for (ElementDecl superSucker : sucker
              .getSGRPSubstituteableElementDecls()) {
            if (!firstOrderSubsts.contains(superSucker))
              delta.add(superSucker);
          }
        }

        if (!delta.isEmpty()) {
          somethingHappened = true;
          for (ElementDecl d : delta) {
            xed.addToSubstitutionGroup((XSDElementDecl) d);
          }
          delta.clear();
        }
      }
    } while (somethingHappened);

    // should be the case anyway, but just to be sure
    delta.clear();

    // finally, remove every non-OK substitute:
    for (ElementDecl ed : getAllElementDecls()) {
      XSDElementDecl xed = (XSDElementDecl) ed;
      Set<? extends ElementDecl> substs = xed.getSGRPSubstituteables();
      for (ElementDecl gsubst : substs) {
        XSDElementDecl subst = (XSDElementDecl) gsubst;
        if (subst.abstrakt)
          delta.add(subst);
        else if (!substitutionGroupOK(xed, subst, xed.block))
          delta.add(subst);
      }
      if (!delta.isEmpty()) {
        substs.removeAll(delta);
        delta.clear();
      }
    }
  }

  void initSchemaTypeHierarchy() throws XSLToolsSchemaException,
      XSLToolsXPathUnresolvedNamespaceException {
    for (XSDAbstractDocument doc : rawDocuments.values()) {
      doc.initSchemaTypeHierarchy(this);
    }
  }

  /*
   * Accept user hint for document element name. TODO: Qualify by URI.
   */
  @Override
protected ElementDecl sniffElementDecl(String name) {
    for (ElementDecl decl : getAllElementDecls()) {
      ElementDecl test = decl;
      if (test.getQName().getName().equals(name)
          && getToplevelElementDecl(test.getQName()) == test)
        return test;
    }
    return null;
  }

  /**
   * This also makes parent / child hierarchies ready for a try at
   * auto-detecting the root element
   */
  void initContentModels() throws XSLToolsSchemaException,
      XSLToolsXPathUnresolvedNamespaceException {
    CharGenerator kanone = new CharGenerator();

    for (Iterator<? extends ElementDecl> decls = getAllElementDecls()
        .iterator(); decls.hasNext();) {
      XSDElementDecl decl = (XSDElementDecl) decls.next();
      /*
       * Let the declaration sniff up its possible content model members (just a
       * flat set of declarations). That WILL include those that are inherited
       * because the declaration inherits some complex model type by extension.
       * It will NOT look in the downward direction (for xsi:type extensions).
       */

      // System.err.println("TODO: Check consequences of removing
      // processContentModel");
      decl.processContentModel(this, null);

      /*
       * Fixup the QName to char mapping of declarations (the char names are for
       * the automata to feast on...)
       */
      decl.fixupCharacterNames(this, kanone);

      // Add element as possible parent:
      commentPIParents.addAll(decl.getCommentsPIAcceptingVariations());

      // Possibly add element as possible parent of PCDATA:
      // Add element as possible parent:
      pcDataParents.addAll(decl.getTextAcceptingVariations());
    }

    for (Iterator<? extends ElementDecl> decls = getAllElementDecls()
        .iterator(); decls.hasNext();) {
      XSDElementDecl decl = (XSDElementDecl) decls.next();
      decl.fixupParentReferences();
    }
    /*
     * Done! Record the regexps that will select any-element vs. any-attribute
     * for later use.
     */
    elementRegExp = kanone.getElementRegExp();
    attributeRegExp = kanone.getAttributeRegExp();
  }

  /*
   * TODO: Lots of copy and paste in here -- make common w Relax NG
   */
  void initAutomata() {
    State commentOveroverroot = new State();
    State commentOverroot = new State();
    State commentAccept = new State();
    commentAccept.setAccept(true);
    commentNodeTypeAutomaton.setInitialState(commentOveroverroot);

    // all ATS paths for a comment begin with [root]
    commentOveroverroot.addTransition(new Transition(CharGenerator
        .getRootChar(), commentOverroot));

    State PIOveroverroot = new State();
    State PIOverroot = new State();
    State PIAccept = new State();
    PIAccept.setAccept(true);
    PINodeTypeAutomaton.setInitialState(PIOveroverroot);

    // all ATS paths for a PI begin with [root]
    PIOveroverroot.addTransition(new Transition(CharGenerator.getRootChar(),
        PIOverroot));

    State textOveroverroot = new State();
    State textOverroot = new State();
    State textAccept = new State();
    textAccept.setAccept(true);
    textNodeTypeAutomaton.setInitialState(textOveroverroot);

    // all ATS paths for a text begin with [root]
    textOveroverroot.addTransition(new Transition(CharGenerator.getRootChar(),
        textOverroot));

    State overoverroot = new State();
    State overroot = new State();

    this.schemaATSPathAutomaton.setInitialState(overoverroot);

    // all ATS paths for everything else begin with [root]
    overoverroot.addTransition(new Transition(CharGenerator.getRootChar(),
        overroot));

    ((XSDElementDecl) documentElementDecl).constructATSPathAutomaton(overroot,
        commentOverroot, commentAccept, PIOverroot, PIAccept, textOverroot,
        textAccept);

    for (ElementDecl decl : getAllElementDecls()) {
      // This is only for the abstract evaluation, really.
      // addElementDeclaration(decl);
      // Plug in element (and, hopefully, soon attribute) individual automata.
      ((XSDElementDecl) decl).snapshootATSPathAutomaton(this);
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

    // Comments and PIs are OK before and after document element
    commentOverroot.addTransition(new Transition(CommentNT.instance
        .getCharRepresentation(this), commentAccept));
    PIOverroot.addTransition(new Transition(PINT.chameleonInstance
        .getCharRepresentation(this), PIAccept));

    fixUpAutomata(overoverroot, overroot);
  }

  void init(ValidationContext context, short io) throws XSLToolsException {

    PerformanceLogger pa = DiagnosticsConfiguration.current
        .getPerformanceLogger();

    pa.startTimer("FirstStages", "InputSchema");

    /*
     * Make document with anyType definition
     */
    prepareBuiltinTypeDocument();

    /*
     * Flatten all docs
     */
    flattenDocuments();

    /*
     * See the collision / lack-of-redefine situation
     */
    checkDocumentCompatibility();

    /*
     * Type definitions
     */
    makeDefs();

    /*
     * Declarations
     */
    makeDecls();

    /*
     * Declarations
     */

    pa.stopTimer("FirstStages", "InputSchema");

    pa.startTimer("Structures", "InputSchema");

    initSchemaTypeHierarchy();

    initSubstitutionGrps();

    cookTypes();

    initContentModels();

    String rootElemName = null;

    try {
      String _systemId =
        context.getRootElementNameIdentifier(
        ResolutionContext.SystemInterfaceStrings
        [ResolutionContext.INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY + io], io);
      
      context.resolveString(_systemId,
          "Name of designated root element", "Auto-detect", 
          ResolutionContext.INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY + io);
    } catch (IOException ex) {
      throw new XSLToolsSchemaException(ex);
    }

    /*String docElm = */detectDocumentElement(rootElemName, context);

    //context.pushMessage(this.getClass().getSimpleName(),
    //    "Content model processed, document element is: " + docElm);

    pa.stopTimer("Structures", "InputSchema");

    if (1 != 1) {
      pa.startTimer("Automata", "InputSchema");
      initAutomata();
      pa.stopTimer("Automata", "InputSchema");

      if (DO_SELF_TEST) {
        selfTest(context);
      }

      context.pushMessage(this.getClass().getSimpleName(),
          "Ancestor path automata constructed");
    }

    diagnoseStats();
  }

  List<XSDElementDecl> getAllElementDeclsAsList() {
    List<XSDElementDecl> result = new ArrayList<XSDElementDecl>();
    for (XSDAbstractDocument d : rawDocuments.values()) {
      result.addAll(d.localElementDeclsByRef.values());
    }
    return result;
  }

  List<XSDAttributeDecl> getAllAttributeDeclsAsList() {
    List<XSDAttributeDecl> result = new ArrayList<XSDAttributeDecl>();
    for (XSDAbstractDocument d : rawDocuments.values()) {
      result.addAll(d.localAttributeDeclsByRef.values());
    }
    return result;
  }

  List<XSDType> getAllTypedefsAsList() {
    List<XSDType> result = new ArrayList<XSDType>();
    for (XSDAbstractDocument d : rawDocuments.values()) {
      result.addAll(d.localTypedefsByRef.values());
    }
    return result;
  }

  QName getRefNameFor(XSDElementDecl d) {
    QName result;
    for (XSDAbstractDocument doc : rawDocuments.values()) {
      if ((result = doc.getRefNameFor(d)) != null)
        return result;
    }
    return null;
  }

  XSINilAttr getNilAttr() {
    return nilAttr;
  }

  @Override
  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    List<XSDElementDecl> allElementDecls = getAllElementDeclsAsList();
    List<XSDAttributeDecl> allAttributeDecls = getAllAttributeDeclsAsList();
    List<XSDType> allTypedefs = getAllTypedefsAsList();

    Element me = fac.createElement(getClass().getSimpleName());
    parent.add(me);

    me
        .addAttribute("elementRefCount", Integer.toString(allElementDecls
            .size()));
    me.addAttribute("elementCount", Integer.toString(allElementDecls.size()));
    int check = 0;
    for (Set<ElementDecl> s : getAllElementDeclsByName().values()) {
      check += s.size();
    }
    me.addAttribute("declByNameCount", Integer.toString(check));

    me.addAttribute("documentElementDecl", Dom4jUtil
        .clarkName(documentElementDecl == null ? null : documentElementDecl
            .getQName()));

    Dom4jUtil.collectionDiagnostics(me, rawDocuments, "documents", fac, configuration);

    Element typeDefs = fac.createElement("typeDefs");
    me.add(typeDefs);

    Collections.sort(allTypedefs, new Comparator<XSDType>() {
      public int compare(XSDType o1, XSDType o2) {
        String s1 = Dom4jUtil.clarkName(o1.getQName());
        String s2 = Dom4jUtil.clarkName(o2.getQName());
        return s1.compareTo(s2);
      }
    });

    for (XSDType d : allTypedefs) {
      d.diagnostics(typeDefs, fac, configuration);
    }

    Element delementDecls = fac.createElement("elementDecls");
    me.add(delementDecls);

    Collections.sort(allElementDecls, new Comparator<XSDElementDecl>() {
      public int compare(XSDElementDecl o1, XSDElementDecl o2) {
        String s1 = Dom4jUtil.clarkName(o1.getQName());
        String s2 = Dom4jUtil.clarkName(o2.getQName());
        return s1.compareTo(s2);
      }
    });

    for (XSDElementDecl d : allElementDecls) {
      d.diagnostics(delementDecls, fac, configuration, true);
    }

    Collections.sort(allAttributeDecls, new Comparator<XSDAttributeDecl>() {
      public int compare(XSDAttributeDecl o1, XSDAttributeDecl o2) {
        String s1 = Dom4jUtil.clarkName(o1.getQName());
        String s2 = Dom4jUtil.clarkName(o2.getQName());
        return s1.compareTo(s2);
      }
    });

    Element attributeDecls = fac.createElement("attributeDecls");
    me.add(attributeDecls);

    for (XSDAttributeDecl d : allAttributeDecls) {
      d.diagnostics(attributeDecls, fac, configuration);
    }

    Element aliases = fac.createElement("aliasGroups");
    me.add(aliases);
    for (Map.Entry<QName, ? extends Set<ElementDecl>> e : getAllElementDeclsByName()
        .entrySet()) {
      if (e.getValue().size() > 1) {
        Element group = fac.createElement("aliasGroup");
        aliases.add(group);
        for (ElementDecl ed : e.getValue()) {
          ed.diagnostics(group, fac, configuration, false);
        }
      }
    }
  }
}
