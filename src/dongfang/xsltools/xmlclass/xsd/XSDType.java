package dongfang.xsltools.xmlclass.xsd;

import java.util.Map;
import java.util.Set;

import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Origin;
import dk.brics.relaxng.converter.StandardDatatypes;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.AbstractAttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;

interface XSDType extends Diagnoseable {
  /*
   * Not deriveable
   */
  short DER_NOT_DERIVEABLE = 0;

  /*
   * Every derivation method is allowed
   */
  short DER_NOTHING_BLOCKED = 0;

  /*
   * Derived by restriction
   */
  short DER_RESTRICTION = 1 << 0;

  /*
   * Derived by extension
   */
  short DER_EXTENSION = 1 << 1;

  /*
   * Derived by list
   */
  short DER_LIST = 1 << 2;

  /*
   * Derived by union
   */
  short DER_UNION = 1 << 3;

  /*
   * The others are type derivation; this describes a declaration derivation:
   * Substitution.
   */
  short DECL_DER_SUBSTITUTION = 1 << 4;

  /*
   * Derived by declaration identity (reflectivity)
   */
  short DER_REFLECT = 1 << 6;

  /*
   * A blocked derivation method set makes no sense, as the derivation path
   * whose types was supposed to define the set, does not exist
   */
  short DER_BLOCK_MEANINGLESS = 1 << 6;

  short KIND_SIMPLE = 0;

  short KIND_COMPLEX = 1;

  // one day it will be able to build again...
  // StandardDatatypes stddt = null;
  StandardDatatypes stddt = new StandardDatatypes();

  /*
   * Return the name that the type is referenced by (for named types, it will be
   * the name. For originally anonymous types, it is a generated name).
   */
  QName getQName();

  /*
   * Return the refName of the type that this type derives from. anyType returns
   * null.
   */
  QName getDerivedFromQName();

  /*
   * Return the XSDType that this XSDType derived from. For the xs:anyType
   * representation, it points to itself.
   */
  XSDType getDerivedFrom();

  /*
   * Return the kind of derivation. anyKind returns DER_RESTRICTION.
   * anySimpleType returns DER_RESTRICTION.
   */
  short getDerivationMethod();

  /*
   * Just to save stupid instance tests: Simple types return K_SIMPLE, complex
   * return K_COMPLEX.
   */
  short getKind();

  /*
   * New stuff!
   */

  Map<QName, XSDElementDecl> getLocalElementContents();

  void getLocalElementContents(Map<QName, XSDElementDecl> dumper);

  Map<QName, XSDElementDecl> getLocalAndInheritedElementContents();

  void getLocalAndInheritedElementContents(Map<QName, XSDElementDecl> dumper);

  Set<XSDComplexType> immediateExtensionSubstitutableTypes();

  Set<XSDComplexType> reflexiveImmediateExtensionSubstitutableTypes();

  Set<XSDComplexType> transitiveExtensionSubstitutableTypes(short block);

  Set<XSDComplexType> reflexiveTransitiveExtensionSubstitutableTypes(short block);

  Origin getOrigin();

  /*
   * Automaton that represents a superlanguage the language of xsl:value-of on
   * instances of this type. That is, for complex types, generally not the same
   * as the language of child text nodes, as value-of for elements has this
   * funny traverse-the-subgraph, collect-everything semantics.
   */
  Automaton getValueOfAutomaton(XSDSchema schema)
      throws XSLToolsSchemaException;

  /*
   * The idea is: If this if deriveable from other, the derivation is
   * backtrackable : this is derived from x_1 by method m_1, x_1 is deriveable
   * from x_2 by method m_2, etc., until x_n which is deriveable from other,
   * because x_n = other. It should then be easy to make a recursive impl. that
   * travels up the type hierarchy and returns the OR sum of each type's
   * derivation method and the next recursion's result. It should stop when
   * anyType is hit, or stop and return DER_REFLECT when the definition of other
   * is hit. If then result & DER_REFLECT = 0, we know that this is NOT
   * deriveable from other, because other was never hit on the upward traversal
   * (anyType is NOT derived from itself by reflection, but rather by
   * restriction!). The valus of the DER_REFLECT bit can be used in a simpler
   * method that just returns whether this is derived from other or not.
   */
  // short derivationMethodsFrom(QName othersName);
  /*
   * Dead easy: Call the above, return result & DER_REFLECT!=0. (implementation
   * simplified: The above no longer exists.)
   */
  boolean deriveableFrom(QName othersName);

  /*
   * Same as above, but caller may add a set of methods that are blocked.
   */
  boolean deriveableFrom(QName othersName, int blockedMethods);

  /*
   * Returns the set of derivation methods that are blocked on at least one step
   * on the derivation path from the type named by othersRefName to this. The
   * XSD rec. specifies (Schema Component Constraint: Substitution Group OK
   * (Transitive)) that the intersection of (all used derivation methods) and
   * (all blocked derivation methods) must be empty for a substitution to be
   * valid ... this method can be used for getting the latter. If this is not
   * deriveable from other, the result will be meaningless (one cold consider
   * reserving a bit for that message, set when banging through anyType...)
   */
  // public short getBlockedDerivationsFrom(QName othersName);
  /*
   * For simple types, this is whether the type is nonempty (or just true...)
   * For complex types, this is whether the type is mixed. TODO: Change type to
   * Automaton and get to business???
   */
  boolean mayContainTextNodes();

  Automaton languageOfTextNodes(XSDSchema schema)
      throws XSLToolsSchemaException;

  /*
   * Return true if there is any chance that the type may have child elements,
   * false if no possibility whatsoever. Useful for determining whether nonmixed
   * complex types may have a value-of different from "".
   */
  boolean mayHaveChildElements();

  /*
   * True iff there is an anyAttribute in the declaration (or in whatever it is
   * derived from). There must obviously be something special about wildcard wrt
   * attributes: If they are inherited automatically in derivation by
   * restriction, then everything derived from anyType would have 'em .. not
   * bloody likely.
   */
  boolean hasAttributeWildcard();

  // void cookCM(XSDSchema schema) throws
  // XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException;

  /*
   * Construct a content model for this type. TODO: What abt attributes?
   * Attributes no issue; there are no attributes on the child axis...
   */

  Node constructChildFM(SGFragment fraggle, XSDSchema schema, 
      boolean maxOne, Set<DeclaredNodeType> types, ContentOrder order,
      boolean allowInterleave) throws XSLToolsSchemaException;

  Set<AbstractAttributeUse> getLocalAndInheritedAttributeUses();

  XSITypeAttr getTypeAttribute();

  /*
   * If true, this type will not be included in the diagnostics of the schema
   * (that could be used for built in types, that get a little boring to look at
   * in the long run)
   */
  boolean skipDiagnostics();
}
