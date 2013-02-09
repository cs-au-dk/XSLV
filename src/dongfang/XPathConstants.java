/*
 * dongfang M. Sc. Thesis
 * Created on 2005-05-01
 */
package dongfang;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import dongfang.xsltools.model.ModelConfiguration;
import dongfang.xsltools.model.ParseLocationDocumentFactory;
import dongfang.xsltools.model.StylesheetModule;

/**
 * @author dongfang
 */
public class XPathConstants {
  public static final String FUNC_UNKNOWN_STRING = "unknownString";

  // public static final QName FUNC_UNKNOWN_STRING_QNAME = QName.get(
  // FUNC_UNKNOWN_STRING, XMLConstants.VALIDATOR_NAMESPACE);

  public static final String FUNC_UNKNOWN_NUMBER = "unknownNumber";

  // public static final QName FUNC_UNKNOWN_NUMBER_QNAME = QName.get(
  // FUNC_UNKNOWN_NUMBER, XMLConstants.VALIDATOR_NAMESPACE);

  public static final String FUNC_UNKNOWN_BOOLEAN = "unknownBoolean";

  // public static final QName FUNC_UNKNOWN_BOOLEAN_QNAME = QName.get(
  // FUNC_UNKNOWN_BOOLEAN, XMLConstants.VALIDATOR_NAMESPACE);

  public static final String FUNC_UNKNOWN_RTF = "unknownRTF";

  // public static final QName FUNC_UNKNOWN_RTF_QNAME = QName.get(
  // FUNC_UNKNOWN_RTF, XMLConstants.VALIDATOR_NAMESPACE);

  public static boolean isNameFunction(String nameValTemplate) {
    return isQNameFunction(nameValTemplate)
        || isLocalNameFunction(nameValTemplate)
        || isNamespaceURIFunction(nameValTemplate);
  }

  public static boolean isQNameFunction(QName functionName) {
    return functionName.getName().equals("name");
  }

  public static boolean isQNameFunction(String nameValTemplate) {
    return nameValTemplate.equals("{name()}")
        || nameValTemplate.equals("{name(.)}")
        || nameValTemplate.equals("{name(self::node())");
  }

  public static boolean isLocalNameFunction(String nameValTemplate) {
    return nameValTemplate.equals("{local-name()}")
        || nameValTemplate.equals("{local-name(.)}")
        || nameValTemplate.equals("{local-name(self::node())}");
  }

  public static boolean isLocalNameFunction(QName functionName) {
    return functionName.getName().equals("local-name");
  }

  public static boolean isNamespaceURIFunction(String nameValTemplate) {
    return nameValTemplate.equals("{namespace-uri()}")
        || nameValTemplate.equals("{namespace-uri(.)}")
        || nameValTemplate.equals("{namespace-uri(self::node())}");
  }

  // TODO: Proper function lib
  public static boolean isNamespaceURIFunction(QName functionName) {
    return functionName.getName().equals("namespace-uri");
  }

  public static final Namespace XPathFunctionNamespace = Namespace.get("fn",
      "http://www.w3.org/2005/xpath-functions");

  public static final Namespace XPathDatatypeNamespace = Namespace.get("xdt",
      "http://www.w3.org/2005/xpath-datatypes");

  public static final Namespace XPathErrorNamespace = Namespace.get("err",
      "http://www.w3.org/2005/xqt-errors");

  private static DocumentFactory fac = ModelConfiguration.current
      .getDocumentFactory();

  // XPath level quote string used for denoting string literals.
  // We do not bother here with how XML attributes are quoted.
  public static String quoteStr = "\'";

  public static Element createSimplifiedTextLiteral(StylesheetModule module,
      Element where, String nonQuotedText, boolean quote) {

    Element value_of;

    if (fac instanceof ParseLocationDocumentFactory && where != null) {
      value_of = ((ParseLocationDocumentFactory) fac).cloneElement(where);
      value_of.setQName(XSLConstants.ELEM_VALUE_OF_QNAME);
    } else
      value_of = fac.createElement(XSLConstants.ELEM_VALUE_OF_QNAME);

    module.addCoreElementId(value_of);

    if (quote) {
      nonQuotedText = escape(nonQuotedText);
      String quotedText = quoteStr + nonQuotedText + quoteStr;
      value_of.addAttribute(XSLConstants.ATTR_SELECT, quotedText);
    } else
      value_of.addAttribute(XSLConstants.ATTR_SELECT, nonQuotedText);
    return value_of;
  }

  public static String escape(String content) {
    return content.replaceAll(quoteStr, quoteStr + quoteStr);
  }

  public static String unescape(String escaped) {
    String result = escaped.replaceAll(quoteStr + quoteStr, quoteStr);
    return result;
  }

  public static String addLeadingTrailingQuotes(String s) {
    return quoteStr + s + quoteStr;
  }

  public static String stripLeadingTrailingQuotes(String s) {
    if (s.length() < 2)
      return s;
    return s.substring(1, s.length() - 1);
  }
}
