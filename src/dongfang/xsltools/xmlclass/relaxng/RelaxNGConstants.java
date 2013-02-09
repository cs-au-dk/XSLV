/*
 * dongfang M. Sc. Thesis
 * Created on 2005-09-26
 */
package dongfang.xsltools.xmlclass.relaxng;

import org.dom4j.Namespace;
import org.dom4j.QName;

/**
 * @author dongfang
 */
public class RelaxNGConstants {
  public static final String NAMESPACE_URI = "http://relaxng.org/ns/structure/1.0";

  public static final Namespace NAMESPACE = Namespace.get("rng", NAMESPACE_URI);

  public static final String ELEM_DATA = "data";

  public static final QName ELEM_DATA_QNAME = QName.get(ELEM_DATA, NAMESPACE);

  public static final String ELEM_ELEMENT = "element";

  public static final QName ELEM_ELEMENT_QNAME = QName.get(ELEM_ELEMENT,
      NAMESPACE);

  public static final String ELEM_ATTRIBUTE = "attribute";

  public static final String ELEM_OPTIONAL = "optional";

  public static final QName ELEM_OPTIONAL_QNAME = QName.get(ELEM_OPTIONAL,
      NAMESPACE);

  public static final String ELEM_TEXT = "text";

  public static final QName ELEM_TEXT_QNAME = QName.get(ELEM_TEXT, NAMESPACE);

  public static final String ELEM_GROUP = "group";

  public static final QName ELEM_GROUP_QNAME = QName.get(ELEM_GROUP, NAMESPACE);

  // public static final String ELEM_ZERO_OR_MORE = "zeroOrMore";

  // public static final QName ELEM_ZERO_OR_MORE_QNAME =
  // QName.get(ELEM_ZERO_OR_MORE, NAMESPACE);

  public static final String ELEM_ONE_OR_MORE = "oneOrMore";

  public static final QName ELEM_ONE_OR_MORE_QNAME = QName.get(
      ELEM_ONE_OR_MORE, NAMESPACE);

  public static final String ELEM_CHOICE = "choice";

  public static final QName ELEM_CHOICE_QNAME = QName.get(ELEM_CHOICE,
      NAMESPACE);

  public static final String ELEM_EMPTY = "empty";

  public static final String ELEM_EXCEPT = "except";

  public static final String NSNAME = "nsName";

  public static final String ELEM_GRAMMAR = "grammar";

  public static final QName ELEM_GRAMMAR_QNAME = QName.get(ELEM_GRAMMAR,
      NAMESPACE);

  public static final String ELEM_START = "start";

  public static final QName ELEM_START_QNAME = QName.get(ELEM_START, NAMESPACE);

  public static final String ELEM_DEFINE = "define";

  public static final QName ELEM_DEFINE_QNAME = QName.get(ELEM_DEFINE,
      NAMESPACE);

  public static final String ELEM_REF = "ref";

  public static final QName ELEM_REF_QNAME = QName.get(ELEM_REF, NAMESPACE);

  public static final String ATTR_NAME = "name";

  public static final QName ATTR_NAME_QNAME = QName.get(ATTR_NAME);

  public static final String ATTR_NS = "ns";

  public static final QName ATTR_NS_QNAME = QName.get(ATTR_NS);
}
