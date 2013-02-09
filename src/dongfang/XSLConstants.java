/*
 * Created on 2005-02-19
 */
package dongfang;

import org.dom4j.Namespace;
import org.dom4j.QName;

/**
 * @author dongfang
 */
public class XSLConstants {
  /*
   * URI of this name space
   */
  public static final String NAMESPACE_SAMPLE_PREFIX = "xsl";

  public static final String NAMESPACE_URI = "http://www.w3.org/1999/XSL/Transform";

  public static final Namespace NAMESPACE = Namespace.get(
      NAMESPACE_SAMPLE_PREFIX, NAMESPACE_URI);

  /*
   * public static final Set<String> ELEMENT_NAMES = new HashSet<String>();
   * 
   * public static final Set<String> ATTRIBUTE_NAMES = new HashSet<String>();
   * 
   * static { try { Class me = XSLConstants.class.getClass(); Field[] consts =
   * me.getDeclaredFields(); for (int i = 0; i < consts.length; i++) { int m =
   * consts[i].getModifiers(); if (Modifier.isStatic(m) && Modifier.isFinal(m) &&
   * Modifier.isPublic(m)) { Object o = consts[i].get(null); String fn =
   * consts[i].getName(); String s = o.toString(); if (fn.startsWith("ELEM"))
   * ELEMENT_NAMES.add(s); else if (fn.startsWith("ATTR"))
   * ATTRIBUTE_NAMES.add(s); } } } catch (IllegalAccessException e) { throw new
   * AssertionError( "Error in static initializer -- should never happen:" + e); } }
   */
  /*
   * private static String marshall(String elename, String attname) { return
   * elename + "/" + attname; }
   */
  /*
   * Instructions
   */
  public static final String ELEM_APPLY_TEMPLATES = "apply-templates";

  public static final QName ELEM_APPLY_TEMPLATES_QNAME = QName.get(
      ELEM_APPLY_TEMPLATES, NAMESPACE);

  public static final String ELEM_APPLY_IMPORTS = "apply-imports";

  public static final QName ELEM_APPLY_IMPORTS_QNAME = QName.get(
      ELEM_APPLY_IMPORTS, NAMESPACE);

  public static final String ELEM_NEXT_MATCH = "next-match";

  public static final QName ELEM_NEXT_MATCH_QNAME = QName.get(ELEM_NEXT_MATCH,
      NAMESPACE);

  public static final String ELEM_ATTRIBUTE = "attribute";

  public static final QName ELEM_ATTRIBUTE_QNAME = QName.get(ELEM_ATTRIBUTE,
      NAMESPACE);

  public static final String ELEM_CALL_TEMPLATE = "call-template";

  public static final QName ELEM_CALL_TEMPLATE_QNAME = QName.get(
      ELEM_CALL_TEMPLATE, NAMESPACE);

  public static final String ELEM_CHOOSE = "choose";

  public static final QName ELEM_CHOOSE_QNAME = QName.get(ELEM_CHOOSE,
      NAMESPACE);

  public static final String ELEM_COMMENT = "comment";

  public static final QName ELEM_COMMENT_QNAME = QName.get(ELEM_COMMENT,
      NAMESPACE);

  public static final String ELEM_COPY = "copy";

  public static final QName ELEM_COPY_QNAME = QName.get(ELEM_COPY, NAMESPACE);

  public static final String ELEM_COPY_OF = "copy-of";

  public static final QName ELEM_COPY_OF_QNAME = QName.get(ELEM_COPY_OF,
      NAMESPACE);

  public static final String ELEM_ELEMENT = "element";

  public static final QName ELEM_ELEMENT_QNAME = QName.get(ELEM_ELEMENT,
      NAMESPACE);

  public static final String ELEM_FALLBACK = "fallback";

  // public static final QName ELEM_FALLBACK

  public static final String ELEM_FOR_EACH = "for-each";

  public static final QName ELEM_FOR_EACH_QNAME = QName.get(ELEM_FOR_EACH,
      NAMESPACE);

  public static final String ELEM_IF = "if";

  public static final QName ELEM_IF_QNAME = QName.get(ELEM_IF, NAMESPACE);

  public static final String ELEM_MESSAGE = "message";

  // public static final QName ELEM_MESSAGE

  public static final String ELEM_NUMBER = "number";

  // public static final QName ELEM_NUMBER

  public static final String ELEM_PROCESSING_INSTRUCTION = "processing-instruction";

  public static final QName ELEM_PROCESSING_INSTRUCTION_QNAME = QName.get(
      ELEM_PROCESSING_INSTRUCTION, NAMESPACE);

  public static final String ELEM_TEXT = "text";

  public static final QName ELEM_TEXT_QNAME = QName.get(ELEM_TEXT, NAMESPACE);

  public static final String ELEM_VALUE_OF = "value-of";

  public static final QName ELEM_VALUE_OF_QNAME = QName.get(ELEM_VALUE_OF,
      NAMESPACE);

  /*
   * Top-level elements
   */
  public static final String ELEM_ATTRIBUTE_SET = "attribute-set";

  public static final QName ELEM_ATTRIBUTE_SET_QNAME = QName.get(
      ELEM_ATTRIBUTE_SET, NAMESPACE);

  public static final String ELEM_DECIMAL_FORMAT = "decimal-format";

  // public static final QName ELEM_DECIMAL_FORMAT

  public static final String ELEM_KEY = "key";

  public static final QName ELEM_KEY_QNAME = QName.get(ELEM_KEY, NAMESPACE);

  public static final String ELEM_NAMESPACE_ALIAS = "namespace-alias";

  // public static final QName ELEM_NAMESPACE_ALIAS

  public static final String ELEM_OUTPUT = "output";

  // public static final QName ELEM_OUTPUT

  public static final String ELEM_PARAM = "param";

  public static final QName ELEM_PARAM_QNAME = QName.get(ELEM_PARAM, NAMESPACE);

  public static final String ELEM_PRESERVE_SPACE = "preserve-space";

  // public static final QName ELEM_PRESERVE_SPACE

  public static final String ELEM_STRIP_SPACE = "preserve-space";

  // public static final QName ELEM_STRIP_SPACE

  public static final String ELEM_TEMPLATE = "template";

  public static final QName ELEM_TEMPLATE_QNAME = QName.get(ELEM_TEMPLATE,
      NAMESPACE);

  public static final String ELEM_VARIABLE = "variable";

  public static final QName ELEM_VARIABLE_QNAME = QName.get(ELEM_VARIABLE,
      NAMESPACE);

  /*
   * Others
   */
  public static final String ELEM_IMPORT = "import";

  public static final QName ELEM_IMPORT_QNAME = QName.get(ELEM_IMPORT,
      NAMESPACE);

  public static final String ELEM_INCLUDE = "include";

  public static final QName ELEM_INCLUDE_QNAME = QName.get(ELEM_INCLUDE,
      NAMESPACE);

  public static final String ELEM_OTHERWISE = "otherwise";

  public static final QName ELEM_OTHERWISE_QNAME = QName.get(ELEM_OTHERWISE,
      NAMESPACE);

  public static final String ELEM_SORT = "sort";

  public static final QName ELEM_SORT_QNAME = QName.get(ELEM_SORT, NAMESPACE);

  public static final String ELEM_STYLESHEET = "stylesheet";

  public static final QName ELEM_STYLESHEET_QNAME = QName.get(ELEM_STYLESHEET,
      NAMESPACE);

  public static final String ELEM_TRANSFORM = "transform";

  public static final QName ELEM_TRANSFORM_QNAME = QName.get(ELEM_TRANSFORM,
      NAMESPACE);

  public static final String ELEM_WHEN = "when";

  public static final QName ELEM_WHEN_QNAME = QName.get(ELEM_WHEN, NAMESPACE);

  public static final String ELEM_WITH_PARAM = "with-param";

  public static final QName ELEM_WITH_PARAM_QNAME = QName.get(ELEM_WITH_PARAM,
      NAMESPACE);

  /*
   * Attributes
   */
  public static final String ATTR_DISABLE_OUTPUT_ESCAPING = "disable-output-escaping";

  public static final QName ATTR_DISABLE_OUTPUT_ESCAPING_QNAME = QName.get(
      ATTR_DISABLE_OUTPUT_ESCAPING, Namespace.NO_NAMESPACE);

  public static final String ATTR_HREF = "href";

  public static final QName ATTR_HREF_QNAME = QName.get(ATTR_HREF,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_MATCH = "match";

  public static final QName ATTR_MATCH_QNAME = QName.get(ATTR_MATCH,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_MODE = "mode";

  public static final QName ATTR_MODE_QNAME = QName.get(ATTR_MODE,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_NAME = "name";

  public static final QName ATTR_NAME_QNAME = QName.get(ATTR_NAME,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_NAMESPACE = "namespace";

  public static final QName ATTR_NAMESPACE_QNAME = QName.get(ATTR_NAMESPACE,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_PRIORITY = "priority";

  public static final QName ATTR_PRIORITY_QNAME = QName.get(ATTR_PRIORITY,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_TEST = "test";

  public static final QName ATTR_TEST_QNAME = QName.get(ATTR_TEST,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_SELECT = "select";

  public static final QName ATTR_SELECT_QNAME = QName.get(ATTR_SELECT,
      Namespace.NO_NAMESPACE);

  // does this exist at all???
  // public static final String ATTR_URI = "uri";

  public static final String ATTR_USE = "use";

  public static final QName ATTR_USE_QNAME = QName.get(ATTR_USE,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_USE_ATTRIBUTE_SETS = "use-attribute-sets";

  public static final QName ATTR_USE_ATTRIBUTE_SETS_QNAME = QName.get(
      ATTR_USE_ATTRIBUTE_SETS, Namespace.NO_NAMESPACE);

  public static final QName RTF_ATTRIBUTE_SET_QNAME = QName.get(
      ATTR_USE_ATTRIBUTE_SETS, NAMESPACE);

  public static final String ATTR_VERSION = "version";

  public static final QName ATTR_VERSION_QNAME = QName.get(ATTR_VERSION,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_LEVEL = "level";

  public static final QName ATTR_LEVEL_QNAME = QName.get(ATTR_LEVEL,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_COUNT = "count";

  public static final QName ATTR_COUNT_QNAME = QName.get(ATTR_COUNT,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_FROM = "from";

  public static final QName ATTR_FROM_QNAME = QName.get(ATTR_FROM,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_VALUE = "value";

  public static final QName ATTR_VALUE_QNAME = QName.get(ATTR_VALUE,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_FORMAT = "format";

  public static final QName ATTR_FORMAT_QNAME = QName.get(ATTR_FORMAT,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_LETTER_VALUE = "letter-value";

  public static final QName ATTR_LETTER_VALUE_QNAME = QName.get(
      ATTR_LETTER_VALUE, Namespace.NO_NAMESPACE);

  public static final String ATTR_GROUPING_SEPARATOR = "grouping-separator";

  public static final QName ATTR_GROUPING_SEPARATOR_QNAME = QName.get(
      ATTR_GROUPING_SEPARATOR, Namespace.NO_NAMESPACE);

  public static final String ATTR_GROUPING_SIZE = "grouping-size";

  public static final QName ATTR_GROUPING_SIZE_QNAME = QName.get(
      ATTR_GROUPING_SIZE, Namespace.NO_NAMESPACE);

  public static String WHITESPACE = " \n";
}
