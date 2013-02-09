package dongfang.xsltools.xmlclass.xsd;

import java.util.Set;

interface XSDComplexType extends XSDType {
  /*
   * Get set of types that can substitute this type. We don't bother with
   * restriction (so they are not included). The set is of immediate substitutes
   * (not transitive). Substitutions that are block'ed are, of course, not
   * included in the returned set.
   */
  Set<XSDComplexType> immediateExtensionSubstitutableTypes(short holderBlock);

  Set<XSDComplexType> reflexiveImmediateExtensionSubstitutableTypes(
      short holderBlock);

  Set<XSDComplexType> transitiveExtensionSubstitutableTypes(short holderBlock);

  Set<XSDComplexType> reflexiveTransitiveExtensionSubstitutableTypes(
      short holderBlock);
}
