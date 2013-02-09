package dongfang.xsltools.model;

import org.dom4j.Namespace;
import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;

public interface NamespaceExpander {
  public static final String BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE = "default";

  public static final String BIND_PREFIXLESS_TO_NO_NAMESPACE = "none";

  QName getQNameFromString(String qname, String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException;

  Namespace getNamespaceFromString(String attvalue, String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException;

  String getNamespaceURIFromString(String attvalue, String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException;
}
