package dongfang.xsltools.xmlclass.xsd;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.misc.Origin;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.schemaside.AbstractAttributeUse;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;

/**
 * Abstract base class for XML Schema complex types (they are always user
 * defined; the "anyType" ur-type is simulated to be user defined, too).
 * @author dongfang
 */
abstract class XSDAbstractComplexType extends XSDAbstractType implements
    XSDComplexType, XSDSimpleType {
  /*
   * Basically the names of all direct subtypes by extension, or, TODO: if this
   * type is blocked for extension, nothing at all.
   */
  private final Set<XSDComplexType> declaredExtensionDerivatives 
  = new HashSet<XSDComplexType>();

  /*
   * Attribute uses for this type.
   */
  private Set<AbstractAttributeUse> attributeUses;

  /*
   * if null then there is no anyAttribute. If non null then we have one...
   */
  Element anyAttributeUse;

  /*
   * If we're derived by restriction, then this is the complete content model. If
   * we're derived by extension, this is the extension. Currently, this may
   * include references to attributes.
   */
  final List cmOrCmExtension;

  XSDAbstractComplexType(QName refName, QName derivedFromRef,
      short derivationMethod, List cmOrCmExtension, String sblock, Origin origin) {
    super(refName, derivedFromRef, derivationMethod, origin);
    this.cmOrCmExtension = cmOrCmExtension;
    if (sblock == null)
      sblock = "";

    short block = 0;
    if (sblock.contains("#all") || sblock.contains("restriction"))
      block |= XSDType.DER_RESTRICTION;
    if (sblock.contains("#all") || sblock.contains("extension"))
      block |= XSDType.DER_EXTENSION;
    this.block = block;
  }

  XSDAbstractComplexType(QName refName, QName derivedFromRef,
      short derivationMethod, List cmOrCmExtension, String sblock,
      Origin origin, boolean skipDiagnostics) {

    this(refName, derivedFromRef, derivationMethod, cmOrCmExtension, sblock,
        origin);
    this.skipDiagnostics = skipDiagnostics;
  }

  /*
   * Return all attribute uses for the type, including those inherited through
   * derivation by extension from a complex type. IF there are name collisions
   * (as when making an optional attribute required in derivation), the
   * attribute use from the SUBtype is included.
   */
//  @Override
  public Set<AbstractAttributeUse> getLocalAndInheritedAttributeUses() {
    if (attributeUses == null)
      throw new AssertionError("Call processAttributes first, cowboy");

    Set<AbstractAttributeUse> result = new HashSet<AbstractAttributeUse>();
    // From the most authoritative part of the XSD spec, the Primer:
    // However, attribute declarations do not need to be repeated in the derived
    // type definition;
    // in this example, RestrictedPurchaseOrderType will inherit the orderDate
    // attribute declaration
    // from PurchaseOrderType.
    // -- so we do dza same.
    if (derivedFrom instanceof XSDComplexType) {
      if (XSDSchemaConstants.ANYTYPE_QNAME.equals(name)) {
      } else {
        XSDComplexType derivedFrom = (XSDComplexType) this.derivedFrom;
        result.addAll(derivedFrom.getLocalAndInheritedAttributeUses());
      }

      // Make redefines override by pushing out any same-name attributes.
      for (AbstractAttributeUse use : attributeUses) {
        for (Iterator<AbstractAttributeUse> opferIter = result.iterator(); opferIter
            .hasNext();) {
          AbstractAttributeUse opfer = opferIter.next();
          if (opfer.getQName().equals(use.getQName()))
            opferIter.remove();
        }
      }
    }
    result.addAll(attributeUses);
    return result;
  }

  /*
   * DONE: Decision: When derivation by extension blocked, where should that
   * have effect? Here, or when inserting in set, of some other place? Haha, all
   * places. Overkill...
   */

  /**
   * Get all types that directly extend this and may substitute it
   * (that is, holderBlock does not block for extension, and this
   * type is not blocked for extension)
   */
  public Set<XSDComplexType> immediateExtensionSubstitutableTypes(
      short holderBlock) {
    // If parameter disallows derivation by extension, return nothing.
    if ((holderBlock & XSDType.DER_EXTENSION) != 0)
      return new HashSet<XSDComplexType>();

    // Copy the forcefully made set
    // if this type definition disallows subst by extension,
    // the set is just empty anyway.
    Set<XSDComplexType> substs = new HashSet<XSDComplexType>(
        declaredExtensionDerivatives);

    // Make sure the
    // for (Iterator<XSDComplexType> it = substs.iterator(); it.hasNext();) {
    // XSDComplexType type = it.next();
    // if (type.getDerivationMethod() != DER_EXTENSION)
    // it.remove();
    // }
    return substs;
  }

  /**
   * As above method, but add in this type itself.
   */
  public Set<XSDComplexType> reflexiveImmediateExtensionSubstitutableTypes(
      short holderBlock) {
    Set<XSDComplexType> result = immediateExtensionSubstitutableTypes(holderBlock);
    result.add(this);
    return result;
  }

  /**
   * Get all types that directly or indirectly extend this and may substitute it
   * (that is, holderBlock does not block for extension, and this
   * type is not blocked for extension)
   */
  @Override
public Set<XSDComplexType> transitiveExtensionSubstitutableTypes(
      short holderBlock) {
    // Cook a set of our immediate substitutes, or the empty set if the
    // parameter disallowed derivation by extension.
    Set<XSDComplexType> temp = immediateExtensionSubstitutableTypes(holderBlock);

    // A copy for iterating over.
    Set<XSDComplexType> result = new HashSet<XSDComplexType>();

    // for each of them, draw in its transitive, nonreflexive closure.
    for (XSDComplexType t : temp) {
      result.addAll(t.reflexiveTransitiveExtensionSubstitutableTypes(block));
    }

    // That's it.
    return result;
  }

  /**
   * As above method, but add in this type itself.
   */
  @Override
public Set<XSDComplexType> reflexiveTransitiveExtensionSubstitutableTypes(
      short holderBlock) {
    Set<XSDComplexType> result = transitiveExtensionSubstitutableTypes(holderBlock);
    result.add(this);
    return result;
  }

  public short getKind() {
    return KIND_COMPLEX;
  }

  @Override
public void addDerivative(XSDType type, short derivationMethod)
      throws XSLToolsSchemaException {
    // could opt to add only those that are derived by extension...
    if (!(type instanceof XSDComplexType)
        && !type.getQName().equals(XSDSchemaConstants.ANYSIMPLETYPE_QNAME)) {
      throw new XSLToolsSchemaException(
          "Derivative of complex type (other than anyType) was not complex ("
              + Dom4jUtil.clarkName(type.getQName()) + " deriving from"
              + Dom4jUtil.clarkName(getQName()) + ")");
    }
    if (derivationMethod == DER_EXTENSION)
      declaredExtensionDerivatives.add((XSDComplexType) type);
  }

  /*
   * public Node constructAttributeFlowModel(SGFragment fraggle, Set<DeclaredNodeType>
   * types, XSDSchema schema) throws XSLToolsSchemaException { Set<XSDAttributeUse>
   * attu = attributeUses(); List<Integer> content = new LinkedList<Integer>();
   * for (XSDAttributeUse au : attu) { if
   * (types.contains(au.getAttributeDeclaration()))
   * content.add(au.constructFlowModel(fraggle, schema).getIndex()); } if
   * (content.size() == 1) return fraggle.getNodeAt(content.get(0)); Node inter =
   * fraggle.createSequenceNode(content, null); return inter; }
   * 
   * Onnnnnce again: If we are derived by extension, we will have to navigate to
   * the supertype, pick up the attributes there and add ours (that might be
   * shuffled to the API method for retrieving the attributes.) It will probably
   * make the best sense if ALL attributes for each (complex) type are returned --
   * not just some strange partial result.
   * 
   * This is almost the same as processContentModel()... ?? Move to ElementDecl
   * class?? (and delete the attributeUses set here??) No, not so good idea...
   * because we still need the navigability to the superTYPE, that must be able
   * to report its attribute uses independently of the elements that use the
   * type.
   */

  @Override
void cookAttributeModel(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {

    if (attributeUses != null)
      throw new AssertionError(
          "processAttributes called for the 2nd time .. how many times do you want to do it really?");

    attributeUses = new HashSet<AbstractAttributeUse>();

    for (Iterator it = cmOrCmExtension.iterator(); it.hasNext();) {
      Element e = (Element) it.next();
      if (e.getQName().equals(XSDSchemaConstants.ELEM_ATTRIBUTE_QNAME)) {
        String sref = e.attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);
        QName ref = ElementNamespaceExpander.qNameForXSLAttributeValue(sref, e,
            NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
        XSDAttributeDecl att = schema.getAttributeDecl(ref);
        if (att == null)
          throw new XSLToolsSchemaException("The attribute referenced: "
              + Dom4jUtil.clarkName(ref) + " does not exist");
        String suse = e.attributeValue(XSDSchemaConstants.ATTR_USE_QNAME);
        if (suse == null)
          suse = "optional"; // default value.
        else
          suse = suse.trim();
        AttributeUse.Cardinal card = AttributeUse.Cardinal.UNKNOWN;
        if (suse.equals("optional"))
          card = AttributeUse.Cardinal.OPTIONAL;
        else if (suse.equals("required"))
          card = AttributeUse.Cardinal.REQUIRED;
        else if (suse.equals("prohibited"))
          card = AttributeUse.Cardinal.PROHIBITED;
        if (card == AttributeUse.Cardinal.UNKNOWN)
          throw new XSLToolsSchemaException("Bad value for use attribute: "
              + suse);

        AbstractAttributeUse use = new AbstractAttributeUse(att, card, e
            .attributeValue(XSDSchemaConstants.ATTR_FIXED_QNAME));
        attributeUses.add(use);
        it.remove(); // zabamm!!! Death on you!
      } else if (e.getQName().equals(
          XSDSchemaConstants.ELEM_ANY_ATTRIBUTE_QNAME)) {
        this.anyAttributeUse = e;
        it.remove(); // zabamm!!! Death on you!
      }
    }
  }

  void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration) {
    me.addAttribute("refName", Dom4jUtil.clarkName(name));
    me.addAttribute("blockExtension", Boolean
        .toString((block & XSDType.DER_EXTENSION) != 0));
    me.addAttribute("blockRestriction", Boolean
        .toString((block & XSDType.DER_RESTRICTION) != 0));
    me.addAttribute("derivedFrom", Dom4jUtil.clarkName(derivedFromName));
    String dk;
    switch (derivationMethod) {
    case DER_EXTENSION:
      dk = "extension";
      break;
    case DER_LIST:
      dk = "list";
      break;
    case DER_RESTRICTION:
      dk = "restriction";
      break;
    case DER_UNION:
      dk = "union";
      break;
    default:
      dk = "???";
    }
    me.addAttribute("derivationMethod", dk);
    Element ederivatives = fac.createElement("declaredDerivatives");
    me.add(ederivatives);
    /*
     * for (QName name : declaredDerivatives) { Element ederivative =
     * fac.createElement("derivative"); ederivatives.add(ederivative);
     * ederivative.addAttribute("name", Dom4jUtil.clarkName(name)); }
     */
    Element eattributeRefs = fac.createElement("attributeRefs");
    me.add(eattributeRefs);
    for (AbstractAttributeUse attributeUse : getLocalAndInheritedAttributeUses()) {
      // Element eattributeRef = fac.createElement("attributeUse");
      // eattributeRefs.add(eattributeRef);
      // eattributeRef.addAttribute("ref", Dom4jUtil.clarkName(attributeRef));
      attributeUse.diagnostics(eattributeRefs, fac, configuration);
    }
    if (anyAttributeUse != null) {
      eattributeRefs.add((Element) anyAttributeUse.clone());
    }
  }

  /*
   * "IIRC, with extensions everything in the content model of the original is
   * inherited. There's some kind of rule that if the base type and the extended
   * type both include an attribute wildcard then the union of the two must be
   * expressible - which disallows one saying "X" and the other saying
   * "##other". But I forget the details." MK (Michael Kay, in mail to me on
   * xml-dev)
   */
  public boolean hasAttributeWildcard() {
    boolean b = anyAttributeUse != null;
    if (derivationMethod == DER_EXTENSION)
      return derivedFrom.hasAttributeWildcard() | b;
    return b;
  }
}
