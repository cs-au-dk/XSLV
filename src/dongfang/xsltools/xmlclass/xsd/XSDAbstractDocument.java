package dongfang.xsltools.xmlclass.xsd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.UniqueNameGenerator;

/*
 * This class was intended for abstracting XSD documents from
 * having anyting to do with DOM / Dom4j. The feature is not yet
 * used, however (and there is still a little talk of Elements
 * in some parameter types).
 */
public abstract class XSDAbstractDocument {
  /*
   * base URI of this schema document.
   */
  final String systemId;

  Namespace targetNamespace;

  /*
   * Submap of those element declarations that are at top level. They candidate
   * for declaring instance document document (root) elements... Local means:
   * For this schema document only, not global throughout the schema.
   */
  final Map<QName, XSDElementDecl> localToplevelElements = new HashMap<QName, XSDElementDecl>();

  /*
   * All element declarations by their ref-name (the name by which they are
   * ref'd in content models etc).
   */
  final Map<QName, XSDElementDecl> localElementDeclsByRef = new HashMap<QName, XSDElementDecl>();

  /*
   * All attribute declarations by their ref-name (the name by which they are
   * ref'd in content models etc).
   */
  final Map<QName, XSDAttributeDecl> localAttributeDeclsByRef = new HashMap<QName, XSDAttributeDecl>();

  /*
   * All type definitions by their ref-name (the name by which they are ref'd in
   * declarations and other type definitions).
   */
  final Map<QName, XSDAbstractType> localTypedefsByRef = new HashMap<QName, XSDAbstractType>();

  XSDAbstractDocument(String systemId) {
    this.systemId = systemId;
  }

  XSDElementDecl getElementDecl(QName name) {
    return localElementDeclsByRef.get(name);
  }

  XSDAttributeDecl getAttributeDecl(QName name) {
    return localAttributeDeclsByRef.get(name);
  }

  XSDType getTypedef(QName name) {
    return localTypedefsByRef.get(name);
  }

  XSDElementDecl getToplevelElementDecl(QName name) {
    return localToplevelElements.get(name);
  }

  /*
   * Some simple accessors; they do the obvious
   */
  String getTargetNamespaceURI() {
    return targetNamespace.getURI();
  }

  String getTargetNamespacePrefix() {
    return targetNamespace.getPrefix();
  }

  Namespace getTargetNamespace() {
    return targetNamespace;
  }

  /*
   * Establish the type derivation hierarchy We will want to extend this to
   * generate and store XSDType objects, as well as adding the builtin ditto. We
   * will also want to take one more sweep that stuffs derivations into complex
   * types (to provide 2 way navigation).
   */
  void initSchemaTypeHierarchy(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
    /*
     * Iteration over all types, even originally anonymous ones. This should fix
     * up two way references betwwen supertypes and subtypes.
     */
    for (Map.Entry<QName, XSDAbstractType> e : localTypedefsByRef.entrySet()) {
      XSDAbstractType type = e.getValue();
      if (type == null)
        throw new XSLToolsSchemaException("INTERNAL ERROR: Type name "
            + Dom4jUtil.clarkName(e.getKey())
            + " was in type map, but with null value");

      /*
       * At the time of the typedef's creation, it was given the QName of the
       * type it derived from. Now, all typedefs have been created, and they can
       * (if they want to) resolve the names into real references to XSDType
       * objects.
       */
      type.fixDerivedFrom(schema);

      /*
       * If complex, let the type resolve the declarations of its attributes (if
       * it wants to; that's only an offer)
       */
      type.cookAttributeModel(schema);

      /*
       * Fixup the derivation references the other way: From "supertypes" to
       * "subtypes". That is necessary when we have to find, for a type t, all
       * types that may substitute t (the xsi:type thing).
       */
      QName styperef = type.getDerivedFromQName();

      XSDAbstractType stype = (XSDAbstractType) schema.getTypedef(styperef);

      if (stype == null)
        throw new XSLToolsSchemaException("Could not resolve typedef for: "
            + Dom4jUtil.clarkName(styperef));
      stype.addDerivative(e.getValue(), type.getDerivationMethod());
    }
  }

  /*
   * Apart from the declared type of every element, which is already known, also
   * add all partial types derived from the declared type by extension to the
   * type set of each element. Done by simply searching the type hierarchy. No
   * longer relevant.
   */
  /*
   * void initElementDerivedTypes() { for (ElementDecl e : allElementDecls) {
   * XSDElementDecl xe = (XSDElementDecl) e; for (Map.Entry<QName,
   * TypeDerivation> me : typeHierarchy.entrySet()) {
   * 
   * Object o = getTypeDef(me.getKey()); // fusk: See if the type is an element
   * (that is, not built in) and if // that element // is at top level (that is,
   * originally a toplevel type def)
   * 
   * if (o instanceof Element) { Element xo = (Element) o; if (xo.getParent() !=
   * null && xo.getParent().getQName().equals(
   * XSDSchemaConstants.ELEM_SCHEMA_QNAME)) { if
   * (derivesFrom(xe.declaredTypeName, me.getKey())) { TypeDerivation td =
   * me.getValue(); if (td.derivationKind == TypeDerivation.DK_EXTENDED)
   * xe.typeDeclNames.add(me.getKey()); } } } } } }
   */

  void checkDocumentCompatibility(XSDAbstractDocument other) {
    Set<QName> sniff = new HashSet<QName>();

    sniff.addAll(localTypedefsByRef.keySet());
    sniff.retainAll(other.localTypedefsByRef.keySet());
    if (!sniff.isEmpty()) {
      System.err.println("Documents at " + systemId + " and " + other.systemId
          + ":");
      System.err
          .println("Type conflict at include / import (or unhandled redefine)");
      for (QName name : sniff) {
        System.err.println(Dom4jUtil.clarkName(name));
      }
    }
    sniff.clear();

    sniff.addAll(localElementDeclsByRef.keySet());
    sniff.retainAll(other.localElementDeclsByRef.keySet());

    if (!sniff.isEmpty()) {
      System.err.println("Documents at " + systemId + " and " + other.systemId
          + ":");
      System.err
          .println("Element conflict at include / import (or unhandled redefine)");
      for (QName name : sniff) {
        System.err.println(Dom4jUtil.clarkName(name));
      }
    }
    sniff.clear();

    sniff.addAll(localAttributeDeclsByRef.keySet());
    sniff.retainAll(other.localAttributeDeclsByRef.keySet());
    if (!sniff.isEmpty()) {
      System.err.println("Documents at " + systemId + " and " + other.systemId
          + ":");
      System.err
          .println("Attribute conflict at include / import (or unhandled redefine)");
      for (QName name : sniff) {
        System.err.println(Dom4jUtil.clarkName(name));
      }
    }
  }

  /*
   * Never used for anything else than paranoid diags etc. Not of importance..
   */
  QName getRefNameFor(XSDElementDecl d) {

    for (Map.Entry<QName, XSDElementDecl> d2 : localElementDeclsByRef
        .entrySet()) {
      if (d == d2.getValue())
        return d2.getKey();
    }
    return null;
    /*
     * for (Map.Entry<QName, SetXSDElementDecl>> m :
     * localElementDeclsByRef.entrySet()) { for (XSDElementDecl d2 :
     * m.getValue()) { if (d == d2) return m.getKey(); } } return null;
     */
  }

  abstract void makeTypeDefs(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException;

  abstract void prepareDecls() throws XSLToolsXPathUnresolvedNamespaceException;

  abstract void makeDecls(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException;

  abstract Element getAttributeGroupDef(QName name);

  abstract Element getModelGroupDef(QName name);

  abstract void killGroupDefs();

  abstract void prepareDOM4Flattening(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException;

  abstract void flatten(UniqueNameGenerator names)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException;

  void cookCM(XSDSchema schema)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
  }
}
