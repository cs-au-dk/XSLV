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

class XSDRestrictionSimpleType extends XSDAbstractSimpleType {

  XSDRestrictionSimpleType(QName name, QName derivedFromRef,
      short derivationMethod, List facets, Origin origin)
      throws XSLToolsSchemaException {
    super(name, derivedFromRef, derivationMethod, facets, origin);
  }

  public Automaton makeValueOfAutomaton(XSDSchema schema, List<Param> facets)
      throws XSLToolsSchemaException {
    // we collect facets all the way up the derivation hierarchy...
    // this is a schema define type; it must be derived from a builtin
    // at some level. We just pass on the monkey to the next one up the
    // derivation hierarchy -- eventually is must hit the builtin, and
    // from there, hit the builtin ont with all the facets...
    // XSDType gnext = schema.getTypedef(getDerivedFromQName());
    // we boldly assume it's a simple type, as the only type that
    // can derive from a complex type directly is anySimpleType, that
    // is not represented by a XSDSchemaDefinedType anyway.
    // XSDAbstractSimpleType next = (XSDAbstractSimpleType) gnext;

    facets.addAll(this.facets);
    return ((XSDAbstractSimpleType) derivedFrom).makeValueOfAutomaton(schema,
        facets);
  }
  
  @Override
void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration) {
    me.addAttribute("kind", "schema-atomic");
  }
}
