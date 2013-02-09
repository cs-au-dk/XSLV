package dongfang.xsltools.xmlclass.xsd;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Origin;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.schemaside.AbstractAttributeUse;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ConcreteAttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;

/**
 * Abstract base class for any XML Schema type.
 * @author dongfang
 */
abstract class XSDAbstractType implements XSDType {
  /*
   * Name of this type.
   */
  final QName name;

  /*
   * QName of the type that this type is derived from. For the anyType ur-type,
   * that is again anyType.
   */
  final QName derivedFromName;

  /*
   * One of DER_RESTRICTION, DER_EXTENSION, DER_LIST, DER_UNION
   */
  final short derivationMethod;

  final XSITypeAttr typeAttribute;

  final Origin origin;

  /*
   * An OR sum af all blocked derivation methods of the type. Zero for simple
   * types, which are not derivation blockable.
   */
  short block;

  XSDSimpleType derivedFrom;

  boolean skipDiagnostics;

  XSDAbstractType(QName refName, QName derivedFromRef, short derivationMethod,
      Origin origin) {
    this.name = refName;
    this.derivedFromName = derivedFromRef;
    this.derivationMethod = derivationMethod;
    this.origin = origin;
    this.typeAttribute = new XSITypeAttr(refName);
  }

  public QName getQName() {
    return name;
  }

  public QName getDerivedFromQName() {
    return derivedFromName;
  }

  public XSDType getDerivedFrom() {
    return derivedFrom;
  }

  public short getDerivationMethod() {
    return derivationMethod;
  }

  public Origin getOrigin() {
    return origin;
  }

  public void fixDerivedFrom(XSDSchema schema) throws XSLToolsSchemaException {
    XSDSimpleType f = (XSDSimpleType) schema.getTypedef(derivedFromName);
    if (f == null)
      throw new XSLToolsSchemaException("Could not resolve type for name: "
          + Dom4jUtil.clarkName(derivedFromName));
    this.derivedFrom = f;
  }

  boolean constructInheritedElementModel(SGFragment fraggle,
      Set<DeclaredNodeType> types, XSDSchema schema, List<Integer> tanker,
      boolean single, ContentOrder order, boolean allowInterleave)
      throws XSLToolsSchemaException {
    return false;
  }

  abstract void addDerivative(XSDType type, short derivationMethod)
      throws XSLToolsSchemaException;

  public short derivationMethodsFrom(QName othersRefName) {
    if (name.equals(othersRefName))
      return DER_REFLECT;

    // stop infinite recursion in case of anyType, which derives
    // from itself.
    if (this == derivedFrom)
      return derivationMethod;

    int result = derivationMethod
        | ((XSDAbstractType) derivedFrom).derivationMethodsFrom(othersRefName);

    short sresult = (short) result;

    assert (result == sresult) : "Serious arith screwup!!";

    return sresult;
  }

  /*
   * public boolean deriveableFrom(QName othersName) { return
   * (derivationMethodsFrom(othersName) & DER_REFLECT) != 0; }
   * 
   * public short getBlockedDerivationsFrom(QName othersName) { if
   * (name.equals(othersName)) // here meaning: It is never forbidden to derive
   * from oneself. return DER_NOTHING_BLOCKED; / * stop infinite recursion in
   * case of anyType, which derives from itself. Return REFLECT, here meaning:
   * Miss, the set of blocked derivation methods from other makes no sense, as
   * this type is not deriveable from other / if (this == derivedFrom) return
   * DER_BLOCK_MEANINGLESS;
   * 
   * int result = derivedFrom.block |
   * derivedFrom.getBlockedDerivationsFrom(othersName);
   * 
   * short sresult = (short)result;
   * 
   * assert(result==sresult) : "Serious arith screwup!!";
   * 
   * return sresult; }
   */

  public boolean deriveableFrom(QName othersName, int usedMethods,
      int blockedMethods) {
    if (this.name.equals(othersName))
      return true;

    // we have slammed into anyType w/o looking for that. Give up.
    if (this == derivedFrom)
      return false;

    // step one up. We use the method that this type was derived by.
    usedMethods |= derivationMethod;

    // block whatever the supertype blocks (+ what was already blocked)
    blockedMethods |= ((XSDAbstractType) derivedFrom).block;

    // is there an overlap, quit
    if ((usedMethods & blockedMethods) != 0)
      return false;

    // recurse: Nothing went wrong in this der. step; see if we are
    // equally lucky the rest of the way...
    return ((XSDAbstractType) derivedFrom).deriveableFrom(othersName,
        usedMethods, blockedMethods);
  }

  public boolean deriveableFrom(QName othersName) {
    return deriveableFrom(othersName, 0, 0);
  }

  public boolean deriveableFrom(QName othersName, int blockedMethods) {
    return deriveableFrom(othersName, 0, blockedMethods);
  }

  public Map<QName, XSDElementDecl> getLocalAndInheritedElementContents() {
    return Collections.emptyMap();
  }

  public void getLocalAndInheritedElementContents(Map<QName, XSDElementDecl> m) {
  }

  public Map<QName, XSDElementDecl> getLocalElementContents() {
    return Collections.emptyMap();
  }

  public void getLocalElementContents(Map<QName, XSDElementDecl> m) {
  }

  public Set<XSDComplexType> immediateExtensionSubstitutableTypes() {
    return Collections.emptySet();
  }

  public Set<XSDComplexType> reflexiveImmediateExtensionSubstitutableTypes() {
    return Collections.emptySet();
  }

  public Set<XSDComplexType> reflexiveTransitiveExtensionSubstitutableTypes(
      short block) {
    return Collections.emptySet();
  }

  public Set<XSDComplexType> transitiveExtensionSubstitutableTypes(short block) {
    return Collections.emptySet();
  }

  public XSITypeAttr getTypeAttribute() {
    return typeAttribute;
  }

  private void makeAttributeUses(ElementUse euse) {
    Set<AbstractAttributeUse> uses = getLocalAndInheritedAttributeUses();
    for (AbstractAttributeUse use : uses) {
      AttributeUse cuse = new ConcreteAttributeUse(use, euse);
      euse.addAttributeUse(cuse);
    }
  }

  private void processUses(XSDSchema schema, XSDElementDecl decl, boolean nilem) {
    {
      ElementUse declaredUse = new SubsumptionElementUse(decl, this);
      makeAttributeUses(declaredUse);

      XSITypeUse declaredTypeUse = new XSITypeUse(getTypeAttribute(), false,
          declaredUse);

      // optional declared-type typer
      declaredUse.addAttributeUse(declaredTypeUse);
//      schema.addSpecialAttrUse()

      if (nilem) {
        NilElementUseDecorator euse2 = new NilElementUseDecorator(declaredUse);
        XSINilUse nil = new XSINilUse(schema.getNilAttr(), true, euse2);
        declaredUse.addAttributeUse(nil);
        euse2.nillert = nil;
        decl.addSelfUse(euse2);
      } else {
        XSINilUse nilAttr = new XSINilUse(schema.getNilAttr(), false, declaredUse);
        declaredUse.addAttributeUse(nilAttr);
        decl.addSelfUse(declaredUse);
      }
    }
    
    if ((decl.block & XSDType.DER_EXTENSION) == 0) { // testen er vist
                                                      // overfloedig...
      for (XSDType t2 : transitiveExtensionSubstitutableTypes(decl.block)) {
        SubsumptionElementUse derivedUse = new SubsumptionElementUse(decl, t2);
        makeAttributeUses(derivedUse);

        XSITypeUse explicitTypeUse = new XSITypeUse(getTypeAttribute(), true,
            derivedUse);
        if (nilem) {
          NilElementUseDecorator euse2 = new NilElementUseDecorator(derivedUse);
          XSINilUse nil = new XSINilUse(schema.getNilAttr(), nilem, euse2);
          derivedUse.addAttributeUse(nil);
          euse2.nillert = nil;
          decl.addSelfUse(euse2);
        } else {
          XSINilUse nil = new XSINilUse(schema.getNilAttr(), nilem, derivedUse);
          derivedUse.addAttributeUse(nil);
          decl.addSelfUse(derivedUse);
        }
        derivedUse.addAttributeUse(explicitTypeUse);
      }
    }
  }

  void processUses(XSDElementDecl decl, boolean nillable, XSDSchema schema) {
    processUses(schema, decl, false);
    if (nillable)
      processUses(schema, decl, true);
  }

  public final boolean skipDiagnostics() {
    return skipDiagnostics;
  }

  void cookCM(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
  }

  void cookAttributeModel(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
  }

  @Override
public String toString() {
    Automaton dragon = null;

    try {
      dragon = languageOfTextNodes(null);
    } catch (Exception ex) {
    }

    String ex = null;
    if (dragon != null)
      ex = dragon.getShortestExample(true);
    if (ex == null)
      ex = "null";

    return "<" + getClass().getSimpleName() + " refName=\""
        + Dom4jUtil.clarkName(name) + "\" derivedFromRef=\""
        + Dom4jUtil.clarkName(derivedFromName) + "\""
        + " contentStringExample=\"" + ex + "\"/>";
  }
}
