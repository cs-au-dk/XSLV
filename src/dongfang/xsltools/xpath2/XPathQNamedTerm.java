package dongfang.xsltools.xpath2;

import org.dom4j.QName;

import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.model.NamespaceExpander;

public interface XPathQNamedTerm extends Diagnoseable {
  QName getQName();

  void setQName(String qualifiedName, NamespaceExpander nexp,
      String noPrefixBehavior);

  void setQName(Token token, NamespaceExpander nexp, String noPrefixBehavior);
}
