package dongfang.xsltools.model;

import org.dom4j.Namespace;
import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;

public class NullNamespaceExpander implements NamespaceExpander {

  public Namespace getNamespaceFromString(String attvalue,
      String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {
    if (attvalue.contains(":"))
      throw new XSLToolsXPathUnresolvedNamespaceException(
          "This expander cannot expand any prefixes at all");
    return Namespace.NO_NAMESPACE;
  }

  public String getNamespaceURIFromString(String attvalue,
      String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {
    if (attvalue.contains(":"))
      throw new XSLToolsXPathUnresolvedNamespaceException(
          "This expander cannot expand any prefixes at all");
    return "";
  }

  public QName getQNameFromString(String qname, String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {
    if (qname.contains(":"))
      throw new XSLToolsXPathUnresolvedNamespaceException(
          "This expander cannot expand any prefixes at all");
    return QName.get(qname, Namespace.NO_NAMESPACE);
  }
}
