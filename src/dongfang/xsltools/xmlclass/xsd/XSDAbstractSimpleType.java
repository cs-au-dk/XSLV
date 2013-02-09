package dongfang.xsltools.xmlclass.xsd;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.schemaside.AbstractAttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.TextNT;

/**
 * Abstract base class for XML Schema simple types.
 * @author dongfang
 */

abstract class XSDAbstractSimpleType extends XSDAbstractType implements
    XSDSimpleType {
  
  final List<Param> facets = new LinkedList<Param>();

  /*
   * "Content" is a slight misnomer: Normally that is used about elements.
   * The content here is text.
   */
  Automaton cachedContentAutomaton;

  XSDAbstractSimpleType(QName refName, QName derivedFromRef,
      short derivationMethod, List facets, Origin origin)
      throws XSLToolsSchemaException {
    super(refName, derivedFromRef, derivationMethod, origin);
    makeFacetList(facets);
  }

  void makeFacetList(List facets) throws XSLToolsSchemaException {
    for (Iterator facetIter = facets.iterator(); facetIter.hasNext();) {
      Element f = (Element) facetIter.next();
      if (f.getNamespaceURI().equals(XSDSchemaConstants.NAMESPACE_URI)) {
        String facetName = f.getName();
        // just skip past nonfacets, such as simpleType decls and attribute
        // stuff
        if (XSDSchemaConstants.FACET_NAMES.contains(facetName)) {
          String facetValue = f
              .attributeValue(XSDSchemaConstants.ATTR_VALUE_QNAME);
          if (facetValue == null)
            throw new XSLToolsSchemaException("Facet without value: "
                + f.getName());
          Param param = new Param(facetName, facetValue);
          this.facets.add(param);
        }
      }
    }
  }

  // @Override
  public Node constructChildFM(SGFragment fraggle,
      XSDSchema schema, boolean maxOne,
      Set<DeclaredNodeType> types, 
      ContentOrder order, boolean allowInterleave)
      throws XSLToolsSchemaException {
    Node result;
    if (types.contains(TextNT.chameleonInstance)) {
      TextNT addition;
      if (ControlFlowConfiguration.current.useColoredContextTypes())
        // addition = new TextNT(getValueOfAutomaton(schema));
        addition = new TextNT(languageOfTextNodes(schema), getOrigin());
      else
        addition = TextNT.chameleonInstance;
      result = fraggle.createPlaceholder(addition);
    } else
      result = fraggle.createEpsilonNode();
    return fraggle.wrapInCommentPIConstruct(result, types
        .contains(CommentNT.instance), null);
  }

  public short getKind() {
    return KIND_SIMPLE;
  }

  public Automaton getValueOfAutomaton(XSDSchema schema)
      throws XSLToolsSchemaException {
    if (cachedContentAutomaton == null)
      cachedContentAutomaton = makeValueOfAutomaton(schema,
          new LinkedList<Param>());
    return cachedContentAutomaton;
  }

  //abstract Automaton makeValueOfAutomaton(XSDSchema schema, List<Param> facets);

  @Override
public void addDerivative(XSDType type, short derivationMethod) {
    // We don't bother to save it. We never use it anyway.
  }

  String getFacet(List<Param> facets, String name) {
    for (ListIterator<Param> rev = facets.listIterator(facets.size()); rev
        .hasPrevious();) {
      Param p = rev.previous();
      if (name.equals(p.getName()))
        return p.getValue();
    }
    return null;
  }

  /*
   * This is really an approximation: Some simple types may be empty altogether.
   * But, hey, who would ever want to do that?
   */
  public boolean mayContainTextNodes() {
    return true;
  }

  /*
   * For simple types, the entire value-of language is just the same as the
   * language of the simple content.
   */
  public Automaton languageOfTextNodes(XSDSchema schema)
      throws XSLToolsSchemaException {
    return getValueOfAutomaton(schema);
  }

  public boolean mayHaveChildElements() {
    return false;
  }

  abstract void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration);

  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    if (skipDiagnostics())
      return;
    Element me = fac.createElement(getClass().getSimpleName());
    parent.add(me);
    me.addAttribute("refName", Dom4jUtil.clarkName(name));
    moreDiagnostics(me, fac, configuration);
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
    Element efacets = fac.createElement("facets");
    me.add(efacets);
    for (Param facet : facets) {
      Element efacet = fac.createElement(facet.getName());
      efacets.add(efacet);
      efacet.addAttribute("value", facet.getValue());
    }
  }

  public boolean hasAttributeWildcard() {
    return false;
  }

  /*
   * Simple types don't have attributes.
   */
  public Set<AbstractAttributeUse> getLocalAndInheritedAttributeUses() {
    return Collections.emptySet();
  }
}
