package dongfang.xsltools.xmlclass.xslside;

import org.dom4j.Namespace;
import org.dom4j.QName;

import dongfang.xsltools.diagnostics.Diagnoseable;

public interface NodeType extends Diagnoseable, Comparable<NodeType> {
  /*
   * MODIFIED from "*" prefix to "" prefix, as the wildcard "*:*" is not legal
   * XPath anyway (it is just written "*"), AND because the prefix just
   * complicated things.
   */
  Namespace ANY_NAMESPACE = Namespace.get("", "*");

  QName ANY_NAME = QName.get("*", ANY_NAMESPACE);

  ElementNT ONE_ANY_NAME_ELEMENT_NT = new ElementNT(ANY_NAME);

  AttributeNT ONE_ANY_NAME_ATTRIBUTE_NT = new AttributeNT(ANY_NAME);

  char getCharRepresentation(CharNameResolver resolver);

  String toLabelString();
}
