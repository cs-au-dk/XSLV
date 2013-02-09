package dongfang.xsltools.model;

import org.dom4j.Namespace;
import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;

public class DummyNamespaceExpander implements NamespaceExpander {

  public Namespace getNamespaceFromString(String attvalue,
      String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {
    if (attvalue.contains(":")) {
      String pre = attvalue.substring(0, attvalue.indexOf(':'));
      return Namespace.get(pre, "dummy://bound-by-" + pre);
    }
    if (noPrefixBehaviour == BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE)
      return Namespace.get("foo", "dummy://default");
    if (noPrefixBehaviour == BIND_PREFIXLESS_TO_NO_NAMESPACE)
      return Namespace.NO_NAMESPACE;
    return Namespace.get("foo", noPrefixBehaviour);
  }

  public String getNamespaceURIFromString(String attvalue,
      String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {
    if (attvalue.contains(":")) {
      String pre = attvalue.substring(0, attvalue.indexOf(':'));
      return "dummy://bound-by-" + pre;
    }
    if (noPrefixBehaviour == BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE)
      return "dummy://default";
    if (noPrefixBehaviour == BIND_PREFIXLESS_TO_NO_NAMESPACE)
      return "";
    return noPrefixBehaviour;
  }

  public QName getQNameFromString(String qname, String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {
    if (qname.contains(":")) {
      int i = qname.indexOf(':');
      String pre = qname.substring(0, i);
      String suf = qname.substring(i + 1);
      return QName.get(suf, pre, "dummy://bound-by-" + pre);
    }
    if (noPrefixBehaviour == BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE)
      return QName.get(qname, "foo", "dummy://default");
    if (noPrefixBehaviour == BIND_PREFIXLESS_TO_NO_NAMESPACE)
      return QName.get(qname, Namespace.NO_NAMESPACE);
    return QName.get(qname, "foo", noPrefixBehaviour);
  }
}
