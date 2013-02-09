package dongfang.xsltools.xmlclass.xsd;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Origin;
import dk.brics.relaxng.Param;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.ControlFlowConfiguration;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.TextNT;

class XSDSimpleContentType extends XSDAbstractComplexType {
  List<Param> facets = new LinkedList<Param>();

  Automaton cachedContentAutomaton;

  XSDSimpleContentType(QName refName, QName derivedFromRef,
      short derivationMethod, List nonContentModel, String block, Origin origin) {
    super(refName, derivedFromRef, derivationMethod, nonContentModel, block,
        origin);
    makeFacetList(nonContentModel);
  }

  // DONE: have a ref to a simpleType here, or is that too confusing?

  void makeFacetList(List facets) {
    for (Iterator facetIter = facets.iterator(); facetIter.hasNext();) {
      Element f = (Element) facetIter.next();
      if (f.getNamespaceURI().equals(XSDSchemaConstants.NAMESPACE_URI)) {
        String facetName = f.getName();
        // just skip past nonfacets, such as simpleType decls and attribute
        // stuff
        if (XSDSchemaConstants.FACET_NAMES.contains(facetName)) {
          String facetValue = f
              .attributeValue(XSDSchemaConstants.ATTR_VALUE_QNAME);
          Param param = new Param(facetName, facetValue);
          this.facets.add(param);
        }
      }
    }
  }

  public Automaton makeValueOfAutomaton(XSDSchema schema, List<Param> facets)
      throws XSLToolsSchemaException {
    // we collect facets all the way up the derivation hierarchy...
    // this is a schema define type; it must be derived from a builtin
    // at some level. We just pass on the monkey to the next one up the
    // derivation hierarchy -- eventually is must hit the builtin, and
    // from there, hit the builtin ont with all the facets...
    // we boldly assume it's a simple type, as the only type that
    // can derive from a complex type directly is anySimpleType, that
    // is not represented by a XSDSchemaDefinedType anyway.
    XSDSimpleType next = derivedFrom;
    facets.addAll(this.facets);
    return next.makeValueOfAutomaton(schema, facets);
  }

  public Automaton getValueOfAutomaton(XSDSchema schema)
      throws XSLToolsSchemaException {
    if (cachedContentAutomaton == null)
      cachedContentAutomaton = makeValueOfAutomaton(schema,
          new LinkedList<Param>());
    return cachedContentAutomaton;
  }

  // @Override
  public Node constructChildFM(SGFragment fraggle,
      XSDSchema schema, boolean maxOne, Set<DeclaredNodeType> types, 
      ContentOrder order, boolean allowInterleave)
      throws XSLToolsSchemaException {
    Node result;
    if (types.contains(TextNT.chameleonInstance)) {
      TextNT addition;
      if (ControlFlowConfiguration.current.useColoredContextTypes())
        addition = new TextNT(languageOfTextNodes(schema), getOrigin());
      else
        addition = TextNT.chameleonInstance;
      result = fraggle.createPlaceholder(addition);
    } else
      result = fraggle.createEpsilonNode();
    return fraggle.wrapInCommentPIConstruct(result, types
        .contains(CommentNT.instance), getOrigin());
  }

  public boolean mayContainTextNodes() {
    return true;
  }

  public boolean mayHaveChildElements() {
    return false;
  }

  public Automaton languageOfTextNodes(XSDSchema schema)
      throws XSLToolsSchemaException {
    return getValueOfAutomaton(schema);
  }

  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    if (skipDiagnostics())
      return;
    Element me = fac.createElement(getClass().getSimpleName());
    parent.add(me);
    moreDiagnostics(me, fac, configuration);

    Element efacets = fac.createElement("facets");
    me.add(efacets);
    for (Param facet : facets) {
      Element efacet = fac.createElement(facet.getName());
      efacets.add(efacet);
      efacet.addAttribute("value", facet.getValue());
    }
  }
}