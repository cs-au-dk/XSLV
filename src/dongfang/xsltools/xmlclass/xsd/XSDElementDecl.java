package dongfang.xsltools.xmlclass.xsd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ElementDecl;
import dongfang.xsltools.xmlclass.schemaside.ElementDeclImpl;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;

class XSDElementDecl extends ElementDeclImpl {

  QName declaredTypeName;

  XSDAbstractType declaredType;

  final QName substitutionGroupName;

  private final Set<XSDElementDecl> SGRPSubstitutionDecls = new HashSet<XSDElementDecl>();

  private final Set<ElementUse> allSelfUses = new HashSet<ElementUse>();

  final boolean abstrakt;

  final boolean nillable;

  final short block;

  final Origin origin;

  XSDElementDecl(QName elementName, QName typeDeclName,
      XSDAbstractType declaredType, Element original, String blockDefault,
      Origin origin) throws XSLToolsXPathUnresolvedNamespaceException {
    super(elementName);

    this.declaredTypeName = typeDeclName;
    this.declaredType = declaredType;

    this.SGRPSubstitutionDecls.add(this);
    this.origin = origin;

    String blockAttVal = original
        .attributeValue(XSDSchemaConstants.ATTR_BLOCK_QNAME);
    if (blockAttVal == null)
      blockAttVal = blockDefault;
    if (blockAttVal == null)
      blockAttVal = "";
    short block = 0;
    if (blockAttVal.contains("#all") || blockAttVal.contains("extension"))
      block |= XSDType.DER_EXTENSION;
    if (blockAttVal.contains("#all") || blockAttVal.contains("restriction"))
      block |= XSDType.DER_RESTRICTION;
    if (blockAttVal.contains("#all") || blockAttVal.contains("substitution"))
      block |= XSDType.DECL_DER_SUBSTITUTION;

    this.block = block;

    String snillable = original
        .attributeValue(XSDSchemaConstants.ATTR_NILLABLE_QNAME);

    if (snillable != null)
      snillable = snillable.trim();

    nillable = "true".equals(snillable);

    String sgrp = original
        .attributeValue(XSDSchemaConstants.ATTR_SUBSTITUTION_GROUP_QNAME);
    if (sgrp != null) {
      substitutionGroupName = ElementNamespaceExpander
          .qNameForXSLAttributeValue(sgrp, original,
              NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
    } else
      // DO NOT EVEN THINK OF defaulting it to the element's own
      // name; it might alias with some other element of the same
      // name.
      substitutionGroupName = null;

    String sabstract = original
        .attributeValue(XSDSchemaConstants.ATTR_ABSTRACT_QNAME);
    if (sabstract != null)
      sabstract = sabstract.trim();
    abstrakt = "true".equals(sabstract);

    // tentative substitution group: This may be abstract, and must then
    // be removed again.
    // addToSubstitutionGroup(this);

    /*
     * Set<? extends XSDType> derivedTypes;
     * 
     * if (declaredType instanceof XSDComplexType) { derivedTypes =
     * ((XSDComplexType) declaredType)
     * .transitiveExtensionSubstitutableTypes(block); } else derivedTypes =
     * Collections.singleton(declaredType);
     * 
     * for (XSDType _type : derivedTypes) { System.err.println("This element had
     * a derivative: " + _type); }
     */
  }

  @Override
  protected void constructATSPathAutomatonForLocalFeatures(State parentState,
      State commentParentState, State commentAcceptState, State PIParentState,
      State PIAcceptState, State textParentState, State textAcceptState) {
    for (XSDElementDecl subst : SGRPSubstitutionDecls) {
      if (subst != this) {
        subst.constructATSPathAutomaton(parentState, commentParentState,
            commentAcceptState, PIParentState, PIAcceptState, textParentState,
            textAcceptState);
      }
    }
  }

  void addToSubstitutionGroup(XSDElementDecl decl) {
    SGRPSubstitutionDecls.add(decl);
  }

  public boolean isAbstract() {
    return abstrakt;
  }

  public Set<? extends ElementDecl> getSGRPSubstituteables() {
    return SGRPSubstitutionDecls;
  }

  public void addSelfUse(ElementUse selfUse) {
    allSelfUses.add(selfUse);
  }

  @Override
  public Automaton languageOfTextNodes(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    return declaredType.languageOfTextNodes((XSDSchema) clazz);
  }

  // public Map<QName, ElementUse> getChildElementUseMap() {
  //    
  // }
  /*
   * For the complete base and partial extension types, find reachable elements
   * and attributes. Just find sets of them; do not look into structure or
   * anything fancy. NOTE: This has NOTHING to do with substitution for SUBtypes
   * (xsi:type). This ONLY looks upward. For the xsi:type thing -- replacing one
   * element with multiple types by several elements of each one type -- that is
   * not done now.
   */
  /*
   * void processContentModel(final XSDSchema schema) throws
   * XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
   * 
   * Set<XSDType> done = new HashSet<XSDType>();
   * 
   * XSDType myType = schema.getTypedef(declaredTypeName);
   * 
   * if (myType instanceof XSDComplexType) {
   * 
   * XSDComplexType act = (XSDComplexType) myType;
   * 
   * assert (this.attributeUses.isEmpty()) : "Already did attributes of element
   * once";
   * 
   * Set<AbstractAttributeUse> aus = act.attributeUses(); for
   * (AbstractAttributeUse au : aus) {
   * this.attributeUses.put(au.getAttributeDeclaration().getQName(), new
   * XSDAttributeUse(au, this)); } }
   * 
   * while (myType != null) { if (done.contains(myType)) { throw new
   * XSLToolsSchemaException( "Cyclic type definition; one member in cycle is: " +
   * Dom4jUtil.clarkName(myType.getQName())); }
   * 
   * done.add(myType); // decision right now: Just throw in stuff from all
   * derivations. // Later on we might want to include only from the declared
   * type, and // then introduce new models (??) for this element with derived
   * types. // Traverse CONTENT and ATTRBUTE models... if (myType instanceof
   * XSDComplexContentType) { XSDComplexContentType cct =
   * (XSDComplexContentType) myType; List cm = cct.cmOrCmExtension; for
   * (Iterator it = cm.iterator(); it.hasNext();) { Element e = (Element)
   * it.next(); if
   * (e.getQName().equals(XSDSchemaConstants.ELEM_ATTRIBUTE_QNAME)) { } else if
   * (e.getQName().equals( XSDSchemaConstants.ELEM_ANY_ATTRIBUTE_QNAME)) { }
   * else if (e.getQName().equals(
   * XSDSchemaConstants.ELEM_ATTRIBUTE_GROUP_QNAME)) { throw new
   * AssertionError("HUH? Attribute group still here? " + e); } else { //
   * recursively traverse CONTENT model. cookCM(e, schema); } } // if we are a
   * type that is extended, include stuff up supertype... if
   * (cct.getDerivationMethod() == XSDType.DER_EXTENSION) { XSDType superType =
   * cct.getDerivedFrom(); if (myType == superType) // stop loop at anyType
   * myType = null; else myType = superType; } else myType = null; } else myType =
   * null; // stop recursive ascent. } }
   */

  @Override
  public boolean typeMayDeriveTo(QName othersName, SingleTypeXMLClass clazz) {
    XSDSchema schema = (XSDSchema) clazz;
    XSDType other = schema.getTypedef(othersName);
    return other.deriveableFrom(declaredTypeName);
  }

  @Override
  public boolean typeMayDeriveFrom(QName typeName) {
    return declaredType.deriveableFrom(typeName);
  }

  @Override
  protected void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration,
      boolean verbose) {
    me.addAttribute("declaredType", Dom4jUtil.clarkName(declaredTypeName));
    if (substitutionGroupName != null)
      me.addAttribute("substitutionGroup", Dom4jUtil
          .clarkName(substitutionGroupName));
    if (verbose) {
      Element substitutes = fac.createElement("substitutes");
      me.add(substitutes);
      if (SGRPSubstitutionDecls.size() > 1) {
        for (ElementDecl substitute : SGRPSubstitutionDecls) {
          Element dsubstitute = fac.createElement("substitute");
          substitutes.add(dsubstitute);
          dsubstitute.addAttribute("name", Dom4jUtil.clarkName(substitute
              .getQName()));
        }
      } else if (SGRPSubstitutionDecls.size() == 1) {
        if (SGRPSubstitutionDecls.contains(this)) {
          substitutes.addAttribute("singleton-of", "myself");
        } else {
          substitutes.addAttribute("WRONGSINGLETON", SGRPSubstitutionDecls
              .iterator().next().toString());
        }
      } else {
        substitutes.addAttribute("WHAT-BUGGYORWHAT", "EMPTY");
      }
    }
    me.addAttribute("blockExtension", Boolean
        .toString((block & XSDType.DER_EXTENSION) != 0));
    me.addAttribute("blockRestriction", Boolean
        .toString((block & XSDType.DER_RESTRICTION) != 0));
    me.addAttribute("blockSubstitution", Boolean
        .toString((block & XSDType.DECL_DER_SUBSTITUTION) != 0));
    // me.addAttribute("nilled", Boolean.toString(nilled));
    me.addAttribute("abstract", Boolean.toString(abstrakt));
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    // as far as I know, there is NO derivation issue!
    // complex, by extension: Cannot add mixed
    // complex, by restriction: Cannot restrict mixed
    // simple, by extension: Some attributes are added, and so what
    // simple, by restriction: Ok so type can by substituted by a simple type
    // that is a subtype.
    // Union type is the same as the supertype anyway...
    // Automaton result;
    return declaredType.getValueOfAutomaton((XSDSchema) clazz);
  }

  public Node constructParentModel(SGFragment fraggle,
      SingleTypeXMLClass clazz, Set<DeclaredNodeType> weCare, boolean single)
      throws XSLToolsSchemaException {
    Set<Integer> idxx = new HashSet<Integer>();
    int i = 0;
    Set<? extends ElementUse> parentUses = getParentUses();
    for (DeclaredNodeType type2 : weCare) {
      if (parentUses.contains(type2)) {
        idxx.add(fraggle.createPlaceholder(type2).getIndex());
      }
    }
    /*
    for (ElementUse parent : getParentUses()) {
      if (types.contains(parent))
        idxx.add(i = fraggle.createPlaceholder(parent).getIndex());
    }
    */
    if (idxx.size() == 0)
      return fraggle.createEpsilonNode();
    if (idxx.size() == 1)
      return fraggle.getNodeAt(i);
    Node goose = fraggle.createChoiceNode(idxx, getOrigin());
    return goose;
  }

  @Override
public Origin getOrigin() {
    return origin;
  }

  public Set<ElementUse> getAllUses() {
    return allSelfUses;
  }

  public void getAllUses(Set<? super ElementUse> dumper) {
    dumper.addAll(allSelfUses);
  }

  public Map<QName, AttributeUse> getAllVariationAttributeUses() {
    Map<QName, AttributeUse> result = new HashMap<QName, AttributeUse>();
    for (ElementUse eu : allSelfUses) {
      eu.attributeUses(result);
    }
    return result;
  }

  public Set<ElementUse> getCommentsPIAcceptingVariations() {
    Set<ElementUse> result = new HashSet<ElementUse>();
    for (ElementUse u : allSelfUses) {
      if (u.acceptsCommentPIs())
        result.add(u);
    }
    return result;
  }

  public Set<? extends ElementDecl> getSGRPSubstituteableElementDecls() {
    return SGRPSubstitutionDecls;
  }

  public void getSGRPSubstituteableElementDecls(
      Set<? super ElementDecl> dumper) {
    dumper.addAll(SGRPSubstitutionDecls);
  }

  public Set<ElementUse> getTextAcceptingVariations() {
    Set<ElementUse> result = new HashSet<ElementUse>();
    for (ElementUse u : allSelfUses) {
      if (u.acceptsText())
        result.add(u);
    }
    return result;
  }

  public Set<ElementDecl> getWidenedChildElementDecls() {
    Set<ElementDecl> result = new HashSet<ElementDecl>();
    getWidenedChildElementDecls(result);
    return result;
  }

  public void getWidenedChildElementDecls(Set<? super ElementDecl> dumper) {
    dumper.addAll(declaredType.getLocalAndInheritedElementContents().values());
    if ((block & XSDType.DER_EXTENSION) == 0) {
      Set<XSDComplexType> typeFan = declaredType
          .transitiveExtensionSubstitutableTypes(block);
      for (XSDComplexType fanden : typeFan) {
        dumper.addAll(fanden.getLocalElementContents().values());
      }
    }
  }

  public void processContentModel(SingleTypeXMLClass clazz,
      Map<QName, ElementDecl> tempNameToElementMap)
       {
    declaredType.processUses(this, nillable, (XSDSchema)clazz);
  }

  public void fixupParentReferences() {
    for (ElementUse use : getAllUses()) {
      use.fixupParentReferences();
    }
  }
}
