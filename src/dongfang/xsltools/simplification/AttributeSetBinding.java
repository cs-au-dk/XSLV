/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dongfang.XSLConstants;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsResolverException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;

/**
 * A representation of an xsl:Attribute-set. The class supports merging of
 * attribute-sets, with observance of the precedence rules (XSL Recommendation).
 * It also supports (<code>bindBehaviour</code>) merging of attribute-sets
 * in the same scope and with the same name. Because of this support, it is
 * sometimes useful to instantiate this class even is no binding is intended.
 * 
 * @author dongfang
 */
public class AttributeSetBinding extends Binding {
  Element element;

  Map<QName, Element> attvals = new HashMap<QName, Element>();

  /**
   * Create an instance. All xsl:attribute children of <code>element</code>
   * are saved in a map, as is the element itself, for namespace prefix
   * resolution.
   * 
   * @param attribute_set
   */
  AttributeSetBinding(StylesheetModule bindingStylesheetModule,
      Element attribute_set, short scope) {
    super(bindingStylesheetModule);
    this.element = attribute_set;
  }

  /*
   * Remove all xsl:attribute children from element and then write all those in
   * attvals back to the element
   */
  Element getElement(/* StylesheetModule module */) {
    Element element2 = (Element) element.clone();
    // module.reassignCoreIDs(element2);

    for (Iterator iter = element2.elementIterator(); iter.hasNext();) {
      Element e = (Element) iter.next();
      if (e.getQName().equals(XSLConstants.ELEM_ATTRIBUTE_QNAME))
        e.detach();
    }

    for (Map.Entry<QName, Element> e : attvals.entrySet()) {
      Element attribute = (Element) e.getValue().clone();
      // module.reassignCoreIDs(attribute);
      element2.elements().add(0, attribute);
    }
    return element2;
  }

  /*
   * Merge attribute sets. The attributes in this AttributeSetBinding instance
   * have precedence in case of name clashes
   */
  private void merge(AttributeSetBinding that) {
    Map<QName, Element> thatc = new HashMap<QName, Element>();
    thatc.putAll(that.attvals);
    thatc.putAll(attvals);
    attvals = thatc;
  }

  @Override
public short getSymbolSpace() {
    return Resolver.ATTRIBUTE_SET_SYMBOLSPACE;
  }

  /*
   * Resolver needed here is the resolver bound to! NOTE that the binding order
   * now is significant -- we MUST bind from the top down.
   */
  @Override
public Binding bindBehaviour(QName name, Resolver resolver)
      throws XSLToolsException {
    AttributeSetBinding that = (AttributeSetBinding) resolver.resolve(name,
        Resolver.ATTRIBUTE_SET_SYMBOLSPACE);
    if (that != null)
      merge(that);
    return this;
  }

  @Override
public void removeAllVariableRefs(Resolver resolver,
      ResolutionSimplifierBase simplifier) throws XSLToolsException {
    if (!contentIsResolved()) {
      for (Iterator iter = element.elementIterator(); iter.hasNext();) {
        Element e = (Element) iter.next();
        if (e.getQName().equals(XSLConstants.ELEM_ATTRIBUTE_QNAME)) {
          e = (Element) simplifier.simplify(e, getBindingStylesheetModule(),
              resolver, 100, false);
          // remove variables and stuff
          if (e != null) {
            String name_s = e.attributeValue(XSLConstants.ATTR_NAME_QNAME);
            QName name = ElementNamespaceExpander.qNameForXSLAttributeValue(
                name_s, e, NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
            attvals.put(name, e);
          }
        }
      }
      setContentResolved();
    }
  }

  /*
   * In here, the Resolver needed is the global resolver. The resolution and
   * merging of the attribute-sets referred to by use-attribute-sets is done
   * here. (useful for other element types than just attribute-set -- for
   * example, xsl:element).
   */
  @Override
public Binding resolveBehaviour(QName name, Resolver resolver)
      throws XSLToolsException {
    String use_attr_sets = element
        .attributeValue(XSLConstants.ATTR_USE_ATTRIBUTE_SETS_QNAME);
    if (use_attr_sets != null)
      for (StringTokenizer st = new StringTokenizer(use_attr_sets,
          XSLConstants.WHITESPACE); st.hasMoreElements();) {
        String thatname_s = st.nextToken();
        QName thatname = ElementNamespaceExpander.qNameForXSLAttributeValue(
            thatname_s, element,
            NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
        AttributeSetBinding that = (AttributeSetBinding) resolver.resolve(
            thatname, Resolver.ATTRIBUTE_SET_SYMBOLSPACE);
        if (that != null) {
          merge(that);
        } else
          throw new XSLToolsResolverException(
              "XSL-??? use-attribute-sets referred unbound QName: "
                  + Dom4jUtil.clarkName(thatname));
      }
    return this;
  }

  @Override
public void resolutionDiagnostics(Branch parent, DocumentFactory fac) {
    Element e = fac.createElement(dongfang.xsltools.util.Util
        .capitalizedStringToHyphenString(getClass()));
    parent.add(parent = e);
    for (Map.Entry<QName, Element> en : attvals.entrySet()) {
      e = fac.createElement("attribute");
      parent.add(e);
      e.addAttribute("name", en.getKey().getQualifiedName());
      Element value = fac.createElement("value");
      e.add(value);
      value.add((Element) en.getValue().clone());
    }
  }
}
