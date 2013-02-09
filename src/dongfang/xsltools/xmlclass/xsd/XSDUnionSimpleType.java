package dongfang.xsltools.xmlclass.xsd;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Origin;
import dk.brics.relaxng.Param;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.util.Dom4jUtil;

class XSDUnionSimpleType extends XSDAbstractSimpleType {

  Set<QName> memberRefs = new HashSet<QName>();

  // em suckers are nested within the union elements,
  // as simpleTypes.
  public XSDUnionSimpleType(QName refName, QName derivedFromRef,
      List typesnfacets, Set<QName> memberRefs, Origin origin)
      throws XSLToolsXPathUnresolvedNamespaceException, XSLToolsSchemaException {
    super(refName, derivedFromRef, DER_UNION, typesnfacets, origin);
    this.memberRefs.addAll(memberRefs);
    for (Iterator moreMembers = typesnfacets.iterator(); moreMembers.hasNext();) {
      Element element = (Element) moreMembers.next();
      if (element.getQName().equals(XSDSchemaConstants.ELEM_SIMPLE_TYPE_QNAME)) {
        String sref = element.attributeValue(XSDSchemaConstants.ATTR_REF_QNAME);
        QName ref = ElementNamespaceExpander.qNameForXSLAttributeValue(sref,
            element, NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
        this.memberRefs.add(ref);
      }
    }
  }

  // TODO: Proper facets.
  public Automaton makeValueOfAutomaton(XSDSchema schema, List<Param> facets)
      throws XSLToolsSchemaException {
    Automaton result = Automaton.makeEmpty();
    for (QName memberRef : memberRefs) {
      XSDType member = schema.getTypedef(memberRef);
      Automaton memberAutomaton = member.getValueOfAutomaton(schema);
      result = result.union(memberAutomaton);
    }
    return result;
  }

  @Override
void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration) {
    me.addAttribute("kind", "union");
    for (QName name : memberRefs) {
      Element emember = fac.createElement("member");
      me.add(emember);
      emember.addAttribute("ref", Dom4jUtil.clarkName(name));
    }
  }
}
