package dongfang.xsltools.model;

import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.util.Dom4jUtil;

public class ElementNamespaceExpander implements NamespaceExpander {
  Element element;

  public ElementNamespaceExpander() {
  }

  public ElementNamespaceExpander(Element e) {
    setElement(e);
  }

  public void setElement(Element e) {
    this.element = e;
  }

  public QName getQNameFromString(String attvalue, String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {
    return qNameForXSLAttributeValue(attvalue, element, noPrefixBehaviour);
  }

  public Namespace getNamespaceFromString(String attvalue,
      String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {
    return getNamespaceFromString(attvalue, element, noPrefixBehaviour);
  }

  public String getNamespaceURIFromString(String attvalue,
      String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {
    return getNamespaceURIFromString(attvalue, element, noPrefixBehaviour);
  }

  public static QName qNameForXSLAttributeValue(String attvalue,
      Element element, String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {
    int colonIndex = attvalue.indexOf(':');

    QName bindName;

    if (colonIndex >= 0) {
      String prefix = attvalue.substring(0, colonIndex);
      String suffix = attvalue.substring(colonIndex + 1);

      Namespace templateNameNS = element.getNamespaceForPrefix(prefix);

      if (templateNameNS == null) {
        // attempt to make a summary of known NSs
        StringBuilder known = new StringBuilder();
        Element e = element;
        while (e != null) {
          for (Iterator<Node> iter = e.nodeIterator(); iter.hasNext();) {
            Node n = iter.next();
            if (n.getNodeType() == Node.NAMESPACE_NODE) {
              Namespace nn = (Namespace) n;
              if (known.length() > 0)
                known.append(", ");
              known.append(nn.getPrefix());
              known.append("-->");
              known.append(nn.getURI());
            }
          }
          e = e.getParent();
        }

        throw new XSLToolsXPathUnresolvedNamespaceException(prefix
            + ", known bindings are: " + known);
      }

      bindName = QName.get(suffix, templateNameNS);
    } else // bindName = QName.get(attvalue, Namespace.NO_NAMESPACE);
    {
      Namespace defaultNS;

      if (noPrefixBehaviour == NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE)
        defaultNS = Namespace.NO_NAMESPACE;
      else if (noPrefixBehaviour == NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE)
        defaultNS = element.getNamespaceForPrefix("");
      else {
        String prefix = Dom4jUtil.getPrefixFor(element, "xpath",
            noPrefixBehaviour);
        defaultNS = Namespace.get(prefix, noPrefixBehaviour);
      }

      bindName = QName.get(attvalue, defaultNS);
    }
    return bindName;
  }

  /*
   * public QName qNameForXSLAttributeValue(String attvalue, Element element)
   * throws XSLToolsXPathUnresolvedNamespaceException { return
   * qNameForXSLAttributeValue(attvalue, element, defaultNoPrefixNamespace); }
   */

  public static Namespace getNamespaceFromString(String attvalue,
      Element element, String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {

    int colonIndex = attvalue.indexOf(':');
    Namespace templateNameNS;

    if (colonIndex >= 0) {
      String prefix = attvalue.substring(0, colonIndex);

      templateNameNS = element.getNamespaceForPrefix(prefix);

      if (templateNameNS == null) {
        throw new XSLToolsXPathUnresolvedNamespaceException(prefix);
      }
    } else // bindName = QName.get(attvalue, Namespace.NO_NAMESPACE);
    {
      if (noPrefixBehaviour == NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE)
        templateNameNS = Namespace.NO_NAMESPACE;
      else if (noPrefixBehaviour == NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE)
        templateNameNS = element.getNamespaceForPrefix("");
      else {
        String prefix = Dom4jUtil.getPrefixFor(element, "xpath",
            noPrefixBehaviour);
        templateNameNS = Namespace.get(prefix, noPrefixBehaviour);
      }
    }
    return templateNameNS;
  }

  public static String getNamespaceURIFromString(String attvalue,
      Element element, String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {
    return getNamespaceFromString(attvalue, element, noPrefixBehaviour)
        .getURI();
  }
}