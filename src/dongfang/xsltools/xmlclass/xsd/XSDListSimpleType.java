package dongfang.xsltools.xmlclass.xsd;

import java.util.List;
import java.util.Set;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Origin;
import dk.brics.relaxng.Param;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.util.Dom4jUtil;

class XSDListSimpleType extends XSDAbstractSimpleType {

  // hehe... lists deriving from lists are represented
  // not by a ListSimpleType, but by a SchemaDefinedSimpleType
  // that adds the new facets.

  QName itemTypeRef;

  public XSDListSimpleType(QName name, QName itemTypeRef, List facets,
      Origin origin) throws XSLToolsSchemaException {
    super(name, itemTypeRef, DER_LIST, facets, origin);
    this.itemTypeRef = itemTypeRef;
  }

  // TODO: Proper facets, also pattern and enumeration.
  public Automaton makeValueOfAutomaton(XSDSchema schema, List<Param> facets)
      throws XSLToolsSchemaException {
    // imitate that of the ListPattern thingy..
    XSDType gItemType = schema.getTypedef(itemTypeRef);
    XSDAbstractSimpleType itemType = (XSDAbstractSimpleType) gItemType;
    // right ... we do NOT mix the item type facets with the list facets!
    Automaton itemTypeAutomaton = itemType.getValueOfAutomaton(schema);

    int min = 0;
    String smin = getFacet(facets, "minLength");
    if (smin != null)
      min = Integer.parseInt(smin);

    int max = Integer.MAX_VALUE;
    String smax = getFacet(facets, "maxLength");
    if (smax != null)
      min = Integer.parseInt(smax);

    if (max == Integer.MAX_VALUE) {
      if (min == 0) {
        // TODO: This is approx. Bad whitespace.
        return itemTypeAutomaton.concatenate((Automaton.makeString(" ")
            .concatenate(itemTypeAutomaton)).repeat());
      }
      return itemTypeAutomaton.concatenate(
          Automaton.makeString(" ").concatenate(itemTypeAutomaton)).repeat(
          min - 1).concatenate(
          (Automaton.makeString(" ").concatenate(itemTypeAutomaton)).repeat());
    }

    if (max == 0)
      return Automaton.makeString("");

    if (min == 0)
      return itemTypeAutomaton.concatenate(
          Automaton.makeString(" ").concatenate(itemTypeAutomaton)).repeat(
          max - 1);

    return itemTypeAutomaton.concatenate(
        Automaton.makeString(" ").concatenate(itemTypeAutomaton)).repeat(
        max - 1);

  }

  @Override
void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration) {
    me.addAttribute("kind", "list");
    Element emember = fac.createElement("itemType");
    me.add(emember);
    emember.addAttribute("itemTypeRef", Dom4jUtil.clarkName(itemTypeRef));
  }
}
