package dongfang.xsltools.xmlclass.dtd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.misc.Automata;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.dtdparser.DTDAttributeDecl;
import dongfang.dtdparser.contentmodel.DTDContentModel;
import dongfang.dtdparser.contentmodel.DTDDeclContainer;
import dongfang.dtdparser.contentmodel.DTDDeclItem;
import dongfang.dtdparser.contentmodel.DTDDeclNamedRef;
import dongfang.dtdparser.declparser.DTDDeclNamedRefImpl;
import dongfang.xsltools.controlflow.ControlFlowConfiguration;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.CharGenerator;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ConcreteAttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.ElementDecl;
import dongfang.xsltools.xmlclass.schemaside.ElementDeclImpl;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NodeType;
import dongfang.xsltools.xmlclass.xslside.PINT;
import dongfang.xsltools.xmlclass.xslside.RootNT;
import dongfang.xsltools.xmlclass.xslside.TextNT;
import dongfang.xsltools.xpath2.XPathAxisStep;

/**
 * Represents an element type described by a DTD. Still have a problem w what to
 * do w the root as parent of root elm.
 */
public class DTDElementDecl extends ElementDeclImpl implements ElementUse {
  DTDContentModel contentModel;

  private final Origin origin;

  // Set of ElementDecl objects representing possible children.
  protected final Map<QName, DTDElementDecl> childElementDecls = new HashMap<QName, DTDElementDecl>();

  // Set of AttributeUse objects representing possible attributes.
  protected final Map<QName, AttributeUse> attributeUses = new HashMap<QName, AttributeUse>();

  public boolean isAbstract() {
    return false;
  }

  boolean pcdataAllowed; // Specifies whether PCDATA is allowed as content.

  public boolean acceptsText() {
    return pcdataAllowed;
  }

  public Set<ElementUse> getTextAcceptingVariations() {
    if (!pcdataAllowed)
      return Collections.emptySet();
    return Collections.singleton((ElementUse) this);
  }

  private boolean commentsPIsAllowed;

  public boolean acceptsCommentPIs() {
    return pcdataAllowed;
  }

  public Set<ElementUse> getCommentsPIAcceptingVariations() {
    if (!commentsPIsAllowed)
      return Collections.emptySet();
    return Collections.singleton((ElementUse) this);
  }

  DTDElementDecl(DTD clazz, QName name, dongfang.dtdparser.DTDElementDecl rdecl)
      throws XSLToolsSchemaException {

    super(name);

    this.origin = new Origin(rdecl.getParseSystemId(), rdecl
        .getParseLocationLine(), rdecl.getParseLocationColumn());

    clazz.addElementDeclaration(this);

    this.contentModel = rdecl.getContentModel();

    for (dongfang.dtdparser.DTDAttributeDecl rattr : rdecl.attributeDecls()) {

      // Construct specialized object:
      dongfang.xsltools.xmlclass.dtd.DTDAttributeDecl att = new dongfang.xsltools.xmlclass.dtd.DTDAttributeDecl(
          rattr);

      DTDAttributeDecl.Modes mode = rattr.getMode();

      // this.type = original.type.toString();

      boolean alwaysPresent;

      // TODO: How bout FIXED???
      alwaysPresent = (mode == DTDAttributeDecl.Modes.REQUIRED);

      String fixedValue = rattr.getValue();

      if (rattr.getMode() != DTDAttributeDecl.Modes.FIXED)
        fixedValue = null;

      ConcreteAttributeUse use = new ConcreteAttributeUse(att,
          alwaysPresent ? AttributeUse.Cardinal.REQUIRED
              : AttributeUse.Cardinal.OPTIONAL, fixedValue, this);

      // Add to lists:
      attributeUses.put(att.getQName(), use);
      clazz.addAttributeDeclaration(att);
    }

    // Pre-set allowing comments and PIs. They will be removed again if content
    // model turns out to be EMPTY:
    pcdataAllowed = false;
    commentsPIsAllowed = true;
    // pisAllowed = true;
    // childDecls.add(CommentNT.instance);
    // childDecls.add(PINT.instance);
  }

  /**
   * Process local content model and generate local parents and children tables.
   */
  public void processContentModel(SingleTypeXMLClass clazz,
      Map<QName, ElementDecl> elements) throws XSLToolsSchemaException {
    processContentModel(elements, contentModel);
    // System.out.println(this + " --> " + regexContentModel(elements, clazz,
    // contentModel));
  }

  /**
   * Process DTD definitions and construct internal structure of DTDElements and
   * the NodeType structures. Assumes a pre-setting of allowing comments and
   * PIs. If the content model is EMPTY, these will be removed.
   */
  protected void processContentModel(Map<QName, ElementDecl> elements,
      DTDContentModel model) throws XSLToolsSchemaException {
    // System.out.println("DTD ITEM: " + item);
    if (model instanceof DTDDeclNamedRefImpl) { // an element ref
      String childLocalName = ((DTDDeclNamedRefImpl) model).getName();
      QName childName = QName.get(childLocalName, getQName().getNamespace());
      // Fetch child element object:
      DTDElementDecl childElm = (DTDElementDecl) elements.get(childName);
      if (childElm == null)
        throw new XSLToolsSchemaException("In '"
            + Dom4jUtil.clarkName(getQName())
            + " content model: Element type '" + childName
            + "' does not exist!");

      // Add to children tables:
      childElementDecls.put(childName, childElm);
      // childDecls.add(childElm);

      // Add to the parents tables of the child:
      // childElm.parents.put(getName(), this);
      childElm.addParentUse(this);
    } else if (model instanceof DTDDeclContainer) {
      DTDDeclContainer container = (DTDDeclContainer) model;
      for (DTDDeclItem contained : container.getContent()) {
        processContentModel(elements, contained);
      }

      if (model.getFlavor() == DTDContentModel.Flavor.MIXED) {
        pcdataAllowed = true;
        // childDecls.add(TextNT.instance);
      }
    } else if (model.getFlavor() == DTDContentModel.Flavor.EMPTY) {
      // Nothing allowed. Not even comments.
      pcdataAllowed = commentsPIsAllowed = false;
    } else if (model.getFlavor() == DTDContentModel.Flavor.ANY) {
      // Note: Technically, the XML spec does not say that comments
      // and PIs are allowed in ANY declared content.. But that is
      // simply so stupid that it must be an error. I assume the
      // possible presence of comments and PIs under ANY, which is a
      // sound and conservative approximation anyway.
      // PCDATA osv:
      pcdataAllowed = commentsPIsAllowed = true;

      // Add all elements of this DTD:
      for (ElementDecl child : elements.values()) {
        DTDElementDecl asDTD = (DTDElementDecl) child;

        // Add to children table:
        childElementDecls.put(child.getQName(), asDTD);
        // childDecls.add(child);

        // Add to parents table of child:
        // child.parents.put(getName(), this);
        asDTD.addParentUse(this);
      }
    } else {
      throw new AssertionError("Unknown content model item " + model);
    }
  }

  private String wrapCommentPI(char c, SingleTypeXMLClass clazz) {
    return wrapCommentPI("" + c, clazz);
  }

  private String wrapCommentPI(String s, SingleTypeXMLClass clazz) {
    return "(" + s + ")";// "("
    // +CommentNT.instance.getCharRepresentation(clazz)
    // + '|' + PINT.chameleonInstance.getCharRepresentation(clazz) +")*" + s +
    // "(" +CommentNT.instance.getCharRepresentation(clazz)
    // + '|' + PINT.chameleonInstance.getCharRepresentation(clazz) +")*";
  }

  /**
   * Make a regexp over the lanuage of the content model.
   */
  private String regexContentModel(SingleTypeXMLClass clazz,
      DTDContentModel model) throws XSLToolsSchemaException {
    StringBuilder result = new StringBuilder();
    result.append('(');
    if (model instanceof DTDDeclNamedRef) { // an element ref
      String childLocalName = ((DTDDeclNamedRef) model).getName();
      QName childName = QName.get(childLocalName, getQName().getNamespace());
      // Fetch child element object:
      DTDElementDecl childElm = childElementDecls.get(childName);
      char c = childElm.getCharRepresentation(clazz);
      result.append(wrapCommentPI(c, clazz));
    } else if (model instanceof DTDDeclContainer) {
      DTDDeclContainer container = (DTDDeclContainer) model;
      for (Iterator<DTDDeclItem> items = container.getContent().iterator(); items
          .hasNext();) {
        String recurse = regexContentModel(clazz, items.next());
        result.append(recurse);
        if (items.hasNext()) {
          if (container.getOperator() == DTDDeclContainer.Operator.CHOICE) {
            result.append('|');
          }
        }
      }
      if (model.getFlavor() == DTDContentModel.Flavor.MIXED) {
        result.append(wrapCommentPI(TextNT.chameleonInstance
            .getCharRepresentation(clazz), clazz));
      }
    } else if (model.getFlavor() == DTDContentModel.Flavor.EMPTY) {
      result.append("()");
    } else if (model.getFlavor() == DTDContentModel.Flavor.ANY) {
      result.append('(');
      result.append(wrapCommentPI(clazz.getElementRegExp(), clazz));
      result.append(")*");
    } else {
      throw new AssertionError("Unknown content model item " + model);
    }
    result.append(')');
    if (model instanceof DTDDeclItem) {
      DTDDeclItem declitem = (DTDDeclItem) model;
      result.append(declitem.occurenceToString());
    }
    return result.toString();
  }

  /**
   * Make a regexp over the lanuage of the content model.
   */
  String regexContentModel(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    return regexContentModel(clazz, contentModel);
  }

  /*
   * 
   * @Override public void getGuaranteedDescendants(SingleTypeXMLClass clazz,
   * Set<? super DeclaredNodeType> result) {
   * getGuaranteedDescendants(contentModel, clazz, result); }
   * 
   * protected void getGuaranteedDescendants(DTDItem item, SingleTypeXMLClass
   * clazz, Set<? super DeclaredNodeType> result) { / * for(AttributeUse u:
   * getAttributeUses()) { if (u.getCardinality() == AttributeUse.REQUIRED)
   * result.add(u); } / // TODO: text comment pi DTDCardinal cardinal =
   * item.cardinal; if (cardinal == DTDCardinal.OPTIONAL) return; if (cardinal ==
   * DTDCardinal.ZEROMANY) return; // if (cardinal == DTDCardinal.ONEMANY)
   * 
   * if (item instanceof DTDName) { // an element ref String childLocalName =
   * ((DTDName) item).getValue(); QName childName = QName.get(childLocalName,
   * getQName().getNamespace()); // Fetch child element object: ElementUse
   * childElm = childElementDecls.get(childName); if (result.add(childElm))
   * childElm.getGuaranteedDescendants(clazz, result); return; }
   * 
   * if (item instanceof DTDContainer) { DTDContainer container = (DTDContainer)
   * item; Iterator iter = container.getItemsVec().iterator(); while
   * (iter.hasNext()) { getGuaranteedDescendants((DTDItem) iter.next(), clazz,
   * result); } return; }
   * 
   * if (item instanceof DTDEmpty) { return; }
   * 
   * if (item instanceof DTDAny) { // Add all elements of this DTD: for
   * (BackendElementDecl child : clazz.getAllElementDecls()) {
   * System.err.println("Watt it zhiz???"); if (result.add((ElementUse)child))
   * child.getGuaranteedDescendants(clazz, result); } return; } }
   */
  protected void supportCommonATSPathAutomata(State parentState,
      State commentParentState, State commentAcceptState, State PIParentState,
      State PIAcceptState, State textParentState, State textAcceptState) {

    Transition trans;

    if (myState == null) {
      myState = new State();

      myCommentState = new State();

      if (acceptsCommentPIs()) {
        trans = new Transition(CharGenerator.getCommentChar(),
            commentAcceptState);
        myCommentState.addTransition(trans);
      }

      myPIState = new State();

      if (acceptsCommentPIs()) {
        trans = new Transition(CharGenerator.getPIChar(), PIAcceptState);
        myPIState.addTransition(trans);
      }

      myTextState = new State();

      if (acceptsText()) {
        trans = new Transition(CharGenerator.getPCDATAChar(), textAcceptState);
        myTextState.addTransition(trans);
      }

      for (ElementUse d : childElementDecls.values()) {
        ((DTDElementDecl) d).supportCommonATSPathAutomata(myState,
            myCommentState, commentAcceptState, myPIState, PIAcceptState,
            myTextState, textAcceptState);
      }

      for (AttributeUse a : attributeUses.values()) {
        ((dongfang.xsltools.xmlclass.dtd.DTDAttributeDecl) a
            .getAttributeDeclaration()).constructATSPathAutomaton(myState);
      }
    }

    trans = new Transition(getCharRepresentation(), myState);
    parentState.addTransition(trans);

    trans = new Transition(getCharRepresentation(), myCommentState);
    commentParentState.addTransition(trans);

    trans = new Transition(getCharRepresentation(), myPIState);
    PIParentState.addTransition(trans);

    trans = new Transition(getCharRepresentation(), myTextState);
    textParentState.addTransition(trans);
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz) {
    if (commentsPIsAllowed) // that is to say, not EMPTY
      return Automata.get("string");
    return Automaton.makeEmptyString();
  }

  @Override
  public void runReverseChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    runParentAxis(clazz, result);
  }

  public void runParentAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    result.addAll(getParentUses());
    if (this == clazz.getDocumentElementDecl())
      result.add(RootNT.instance);
  }

  @Override
public Origin getOrigin() {
    return origin;
  }

  public Origin getDeclarationOrigin() {
    return getOrigin();
  }

  private Node constructCardinal(Node victim, SGFragment fragment,
      DTDDeclItem item) {
    if (item.getOccurence() == DTDDeclItem.DTDOccurence.ONE)
      return victim;
    else if (item.getOccurence() == DTDDeclItem.DTDOccurence.ZEROORONE)
      return fragment.constructOptionalCardinal(victim, "cardinal");
    else if (item.getOccurence() == DTDDeclItem.DTDOccurence.ONEORMORE)
      return fragment.constructOneManyCardinal(victim, "cardinal");
    else if (item.getOccurence() == DTDDeclItem.DTDOccurence.ZEROORMORE)
      return fragment.constructZeroManyCardinal(victim, "cardinal");
    return null; // or throw some nasty exception
  }

  private Node constructAnyCM(SGFragment fraggle, Set<DeclaredNodeType> weCare) {
    // can we use ref nodes?
    // redeclaration-safety seems ok.
    List<Integer> contents = new ArrayList<Integer>(weCare.size());
    if (weCare.isEmpty()) {
      return fraggle.createSequenceNode(contents, null);
    }
    for (DeclaredNodeType type : weCare) {
      Node ph = fraggle.createPlaceholder(type);
      contents.add(ph.getIndex());
    }
    Node result = fraggle.createChoiceNode(contents, new Origin("ANY", 0, 0));
    return fraggle.constructZeroManyCardinal(result, "ANY");
  }

  private void constructContainerCM(DTD clazz, List<DTDDeclItem> items,
      List<Integer> idxx, SGFragment fragment, Set<DeclaredNodeType> weCare,
      ContentOrder order, boolean allowInterleave) {
    for (DTDDeclItem item : items) {
      Node n = constructCM(clazz, item, fragment, weCare, order,
          allowInterleave);
      if (n != null)
        idxx.add(n.getIndex());
    }
  }

  private Node constructCM(DTD clazz, DTDContentModel item, SGFragment fraggle,
      Set<DeclaredNodeType> weCare, ContentOrder _order, boolean allowInterleave) {
    Node result = null;
    // String origin = item.getClass().getSimpleName();
    List<Integer> idxx = new LinkedList<Integer>();

    if (item instanceof DTDDeclContainer) {
      // feed list in recursion
      DTDDeclContainer container = (DTDDeclContainer) item;
      List<DTDDeclItem> items = container.getContent();
      constructContainerCM(clazz, items, idxx, fraggle, weCare, _order, idxx
          .size() == 1);
      if (idxx.size() == 1)
        result = fraggle.getNodeAt(idxx.get(0)); // TO DO: Harmless wrt
      // Harmless wrt
      // comment / PI?
      // else {
      if (container.getFlavor() == DTDContentModel.Flavor.MIXED
          && weCare.contains(TextNT.chameleonInstance)) {
        idxx
            .add(fraggle.createPlaceholder(TextNT.chameleonInstance).getIndex());
        result = fraggle.createInterleaveNode(idxx, allowInterleave,
            getOrigin());
      } else if (container.getOperator() == DTDDeclContainer.Operator.CHOICE) {
        result = fraggle.createChoiceNode(idxx, getOrigin());
      } else {
        result = fraggle.createSequenceNode(idxx, getOrigin());
      }
      // }
      result = constructCardinal(result, fraggle, container);
    } else if (item.getFlavor() == DTDContentModel.Flavor.ANY) {
      result = constructAnyCM(fraggle, weCare);
    } else if (item instanceof DTDDeclNamedRef) {
      // result = dumper.createGapNode(idxx, origin);
      DTDDeclNamedRef named = (DTDDeclNamedRef) item;
      QName name = QName.get(named.getName(), clazz.getElementNamespaceURI());
      // fix up the fragment's map
      ElementUse type = childElementDecls.get(name);

      /*
       * Death ray experiment related to dynamic redeclarations
       */

      List<Integer> redeclarationChoices = new LinkedList<Integer>();

      for (DeclaredNodeType type2 : weCare) {
        if (type2.getOriginalDeclaration().equals(type)) {
          Node n = fraggle.createPlaceholder(type2);
          // add to chooser
          redeclarationChoices.add(n.getIndex());
        }
      }

      if (redeclarationChoices.size() == 0)
        result = fraggle.createEpsilonNode();
      else if (redeclarationChoices.size() == 1)
        result = fraggle.getNodeAt(redeclarationChoices.get(0));
      else
        result = fraggle.createChoiceNode(redeclarationChoices, new Origin(
            "REDECL", 0, 0));

      result = constructCardinal(result, fraggle, named);

      /*
       * Code WAS before death ray experiment: if (weCare.contains(type)) {
       * result = fraggle.createPlaceholder(type); result =
       * constructCardinal(result, fraggle, named); }
       */
    } else if (item.getFlavor() == DTDContentModel.Flavor.EMPTY) {
      result = fraggle.createSequenceNode(idxx, getOrigin());
    }
    return result;
  }

  @Override
  public Node constructChildFM(SGFragment fraggle, SingleTypeXMLClass clazz,
      boolean singleMultiplicity, Set<DeclaredNodeType> typeSet,
      boolean allowInterleave, ContentOrder order)
      throws XSLToolsSchemaException {
    return constructCM((DTD) clazz, contentModel, fraggle, typeSet, order,
        allowInterleave);
  }

  public Node constructParentFM(SGFragment fraggle, SingleTypeXMLClass clazz,
      Set<DeclaredNodeType> typeSet) {
    Set<Integer> idxx = new HashSet<Integer>();
    int i = 0;
    /*
     * Redeclaration experiment.
     */
    for (DeclaredNodeType type2 : typeSet) {
      if (getParentUses().contains(type2.getOriginalDeclaration()))
        idxx.add(i = fraggle.createPlaceholder(type2).getIndex());
    }
    /*
     * Code WAS before experiment: for (ElementUse parent : getParentUses()) {
     * if (typeSet.contains(parent)) idxx.add(i =
     * fraggle.createPlaceholder(parent).getIndex()); }
     */
    if (idxx.size() == 1)
      return fraggle.getNodeAt(i);
    Node goose = fraggle.createChoiceNode(idxx, getOrigin());
    return goose;
  }

  @Override
  public Node constructAttributeFM(SGFragment fraggle,
      SingleTypeXMLClass clazz, Set<DeclaredNodeType> weCare) {
    List<Integer> idxx = new LinkedList<Integer>();
    Node result;
    for (AttributeUse use : attributeUses.values()) {
      /*
       * Death ray experiment related to dynamic redeclarations
       */
      List<Integer> redeclarationChoices = new LinkedList<Integer>();

      for (DeclaredNodeType type2 : weCare) {
        /*
         * Make a chooser over redeclarations of <code>use</code>:
         */
        if (type2.getOriginalDeclaration().equals(use)) {
          Node n = ((AttributeUse) type2).constructFlowModel(fraggle, clazz);
          // add to chooser
          redeclarationChoices.add(n.getIndex());
        }
      }

      /*
       * If that chooser has size 1, replace by sole option.
       */
      if (redeclarationChoices.size() == 0)
        result = fraggle.createEpsilonNode();
      else if (redeclarationChoices.size() == 1)
        result = fraggle.getNodeAt(redeclarationChoices.get(0));
      else
        /*
         * A sanity check against 0 would be good here.
         */
        result = fraggle.createChoiceNode(redeclarationChoices, new Origin(
            "REDECL", 0, 0));
      idxx.add(result.getIndex());
    }
    if (idxx.size() == 1)
      return fraggle.getNodeAt(idxx.get(0));
    return fraggle.createSequenceNode(idxx, getOrigin());
  }

  public void attributeUses(Map<QName, AttributeUse> dumper) {
    dumper.putAll(attributeUses);
  }

  public void childDeclarations(Map<QName, ElementDecl> dumper) {
    // TODO Auto-generated method stub
    for (Map.Entry<QName, DTDElementDecl> e : childElementDecls.entrySet())
      dumper.put(e.getKey(), e.getValue());
  }

  public ElementDecl myElementDeclaration() {
    return this;
  }

  public void getAllUses(Set<? super ElementUse> dumper) {
    dumper.add(this);
  }

  public Set<ElementUse> getAllUses() {
    return Collections.singleton((ElementUse) this);
  }

  public void getUses(Set<? super ElementUse> dumper) {
    dumper.add(this);
  }

  public Set<? extends ElementDecl> getSGRPSubstituteableElementDecls() {
    return Collections.singleton((ElementDecl) this);
  }

  public Set<ElementUse> getSGRPSubstituteableElementUses() {
    return Collections.singleton((ElementUse) this);
  }

  public void getSGRPSubstituteableElementUses(Set<? super ElementUse> dumper) {
    dumper.add(this);
  }

  public boolean isNilled() {
    return false;
  }

  public Collection<? extends ElementDecl> getWidenedChildElementDecls() {
    // return Collections.singleton((BackendElementDecl)this);
    return childElementDecls.values();
  }

  public void getWidenedChildElementDecls(Set<? super ElementDecl> dumper) {
    dumper.add(this);
  }

  public Map<QName, AttributeUse> getAllVariationAttributeUses() {
    return attributeUses;
  }

  public boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    return s.accept(this, clazz);
  }

  public void addSelfUse(ElementUse use) {
  }

  public Set<? extends ElementUse> getSelfUses() {
    return Collections.singleton(this);
  }

  @Override
  public Automaton languageOfTextNodes(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    if (pcdataAllowed)
      return Automata.get("string");
    return Automaton.makeEmpty();
  }

  public void fixupParentReferences() {
  }

  public void fixupParentReferences(ElementUse canonical) {
  }

  @Override
  public void runAttributeAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    Collection<? extends AttributeUse> zxzx = attributeUses.values();
    result.addAll(zxzx);
  }

  @Override
  public void runChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) throws XSLToolsSchemaException {
    result.addAll(childElementDecls.values());

    if (acceptsCommentPIs()) {
      result.add(CommentNT.instance);
      result.add(PINT.chameleonInstance);
    }

    if (acceptsText()) {
      if (ControlFlowConfiguration.current.useColoredContextTypes()) {
        result.add(new TextNT(languageOfTextNodes(clazz), origin));
      } else
        result.add(TextNT.chameleonInstance);
    }
  }

  @Override
  public int compareTo(NodeType o) {
    // TODO Auto-generated method stub
    return 0;
  }

  public void addAttributeUse(AttributeUse use) {
    attributeUses.put(use.getQName(), use);
  }

  /*
   * Get original declaration of dynamic redeclarations.
   */
  public DeclaredNodeType getOriginalDeclaration() {
    return this;
  }
}