/*
 * dongfang M. Sc. Thesis
 * Created on 07-02-2005
 */
package dongfang;

import org.dom4j.Namespace;
import org.dom4j.QName;

/**
 * @author dongfang Constants go here
 */
public class XMLConstants {
  public static final String DONGFANG_PREFIX = "df";

  public static final String DONGFANG_URI = "http://dongfang.dk/XSLV";

  public static final Namespace DONGFANG_NAMESPACE = Namespace.get(
      DONGFANG_PREFIX, DONGFANG_URI);

  public static final Namespace PARSELOCATION_DECORATION_NAMESPACE = DONGFANG_NAMESPACE;

  public static final QName PARSELOCATION_DECORATION_QNAME = QName.get("parse",
      PARSELOCATION_DECORATION_NAMESPACE);

  public static final QName ELEMENT_ID_QNAME = QName.get("eid",
      PARSELOCATION_DECORATION_NAMESPACE);

  public static final QName ELEMENT_CORE_ID_QNAME = QName.get("cid",
      PARSELOCATION_DECORATION_NAMESPACE);

  public static final QName ELEMENT_DESUGAR_ID_QNAME = QName.get("eid",
      PARSELOCATION_DECORATION_NAMESPACE);

  public static final QName SYSTEM_ID_QNAME = QName.get("sid",
      PARSELOCATION_DECORATION_NAMESPACE);

  public static final QName DOCUMENT_ID_QNAME = QName.get("mid",
      PARSELOCATION_DECORATION_NAMESPACE);

  public static final QName TEMPLATE_IDENTIFIER_QNAME = QName.get("tid",
	      PARSELOCATION_DECORATION_NAMESPACE);
  
  public static final QName DERIVED_FLAG_QNAME = QName.get("der",
      PARSELOCATION_DECORATION_NAMESPACE);

  public static final QName KNOCKEDOUT = QName.get("ko",
      PARSELOCATION_DECORATION_NAMESPACE);

  // public static final String ATTR_PARSELOCATION = "parseposition";
  /*
   * public static final String PARSELOCATION_FIRST_LINE_ATT_NAME = "firstline";
   * public static final String PARSELOCATION_FIRST_COL_ATT_NAME = "firstcol";
   * public static final String PARSELOCATION_LAST_LINE_ATT_NAME = "lastline";
   * public static final String PARSELOCATION_LAST_COL_ATT_NAME = "lastcol";
   */
  /*
   * public static final String CONFLICT_RESOLUTION_PREFIX = "dongfang"; public
   * static final String CONFLICT_RESOLUTION_NAMESPACE_URI=DONGFANG +
   * CONFLICT_RESOLUTION_PREFIX; public static final Namespace
   * CONFLICT_RESOLUTION_NAMESPACE = Namespace.get(CONFLICT_RESOLUTION_PREFIX,
   * CONFLICT_RESOLUTION_NAMESPACE_URI); public static final String
   * CONFLICT_RESOLUTION_ID_ATT_NAME = "id";
   * 
   * public static final String CONFLICT_RESOLUTION_SET_ATT_NAME =
   * "conflict-set";
   */
  /*
   * public static final String TEST_DECORATION_PREFIX = "dongfang"; public
   * static final String TEST_DECORATION_NAMESPACE_URI = DONGFANG +
   * TEST_DECORATION_PREFIX;
   */

  public static final Namespace ORIGINAL_URI_NAMESPACE = DONGFANG_NAMESPACE;

  public static final QName ORIGINAL_URI_ATT_QNAME = QName.get("original-uri",
      ORIGINAL_URI_NAMESPACE);

  public static final Namespace NS_DECLARATION_NAMESPACE = DONGFANG_NAMESPACE;

  public static final QName NS_PREFIX_ATTR_QNAME = QName.get("ns-prefixes",
      NS_DECLARATION_NAMESPACE);

  public static final QName NS_URI_ATTR_QNAME = QName.get("ns-uris",
      NS_DECLARATION_NAMESPACE);

  public static final Namespace TEMPLATE_MODE_NAMESPACE = DONGFANG_NAMESPACE;

  public static final Namespace TEMPLATE_DECORATOR_NAMESPACE = DONGFANG_NAMESPACE;

  public static final String XSD_ATTR_REFNAME = "refname";

  public static final QName XSD_ATTR_REFNAME__QNAME = QName.get(
      XSD_ATTR_REFNAME, DONGFANG_NAMESPACE);

  public static final QName DEATH_CAUSE_QNAME = QName.get("flowdeath",
      TEMPLATE_DECORATOR_NAMESPACE);

  public static final QName DEATH_CAUSE_ATTR_QNAME = QName.get("cause",
      Namespace.NO_NAMESPACE);

  public static final QName TEMPLATE_DECORATOR_QNAME = QName.get("annotation",
      TEMPLATE_DECORATOR_NAMESPACE);

  public static final QName ROBBER_QNAME = QName.get("robber",
      TEMPLATE_DECORATOR_NAMESPACE);

  public static final QName VICTIM_QNAME = QName.get("victim",
      TEMPLATE_DECORATOR_NAMESPACE);

  public static final QName INSENSITIVE_CONTROLFLOW_QNAME = QName.get(
      "control-flow", TEMPLATE_DECORATOR_NAMESPACE);

  public static final QName FLOWANNOTATION_QNAME = QName.get("flows",
      TEMPLATE_DECORATOR_NAMESPACE);

  public static final QName CONTEXTSET_QNAME = QName.get("context-set",
      TEMPLATE_DECORATOR_NAMESPACE);

  public static final QName CONTEXT_TYPE_QNAME = QName.get("context-type",
      TEMPLATE_DECORATOR_NAMESPACE);

  /*
   * public static final QName CONTEXT_TYPE_ATTR_QNAME = QName.get("type",
   * TEMPLATE_DECORATOR_NAMESPACE);
   */
  public static final QName CONTEXT_TYPE_ATTR_QNAME = QName.get("type",
      Namespace.NO_NAMESPACE);

  public static final QName CURRENT_TARGETS_QNAME = QName.get(
      "current-targets", TEMPLATE_DECORATOR_NAMESPACE);

  public static final QName DEAD_TARGETS_QNAME = QName.get("dead-targets",
      TEMPLATE_DECORATOR_NAMESPACE);

  public static final QName APPLY_TEMPLATES_DECORATOR_QNAME = QName.get(
      "annotation", TEMPLATE_DECORATOR_NAMESPACE);

  public static final QName OUTFLOWS_QNAME = QName.get("outflows",
      TEMPLATE_DECORATOR_NAMESPACE);

  public static final QName OUTFLOW_QNAME = QName.get("outflow",
      TEMPLATE_DECORATOR_NAMESPACE);

  public static final QName CONTEXT_FLOW_TARGET_QNAME = QName.get("target",
      TEMPLATE_DECORATOR_NAMESPACE);

  public static final String SUMMARY_GRAPH_PREFIX = "sg";

  public static final String SUMMARY_GRAPH_NAMESPACE_URI = "http://www.brics.dk/jwig/gap";

  public static final Namespace SUMMARY_GRAPH_NAMESPACE = Namespace.get(
      SUMMARY_GRAPH_PREFIX, SUMMARY_GRAPH_NAMESPACE_URI);

  public static final String DSD_NS = "http://www.brics.dk/DSD/2.0";

  public static final String DSD_META_NS = "http://www.brics.dk/DSD/2.0/meta";

  public static final String DSD_ERROR_NS = "http://www.brics.dk/DSD/2.0/error";

  // temporary, for legacy compatibility
  public static final String VALIDATOR_NAMESPACE_PREFIX = "xslv";

  public static final String VALIDATOR_NAMESPACE_URI = "urn:XSLTValidator";

  public static final Namespace VALIDATOR_NAMESPACE = Namespace.get(
      VALIDATOR_NAMESPACE_PREFIX, VALIDATOR_NAMESPACE_URI);

  public static final String ELEMENT_ENUMERATION_FORMAT = "el-%1S";

  public static final String CORE_ELEMENT_ENUMERATION_FORMAT = "el-%1S";

  public static final String TEMPLATE_INDEX_ENUMERATION_FORMAT = "te-%1S";

  // public static final String DESUGAR_ELEMENT_ENUMERATION_FORMAT = "de-%1S";

  public static final String MODE_ENUMERATION_FORMAT = "mo-%1S";

  public static final String XSD_ELEMENTDECL_ENUMERATION_FORMAT = "el-%1S";

  public static final String XSD_TYPEDEF_ENUMERATION_FORMAT = "ty-%1S";

  public static final Namespace XSD_ELEMENTDECL_NAMESPACE = DONGFANG_NAMESPACE;

  public static final Namespace XSD_TYPEDEF_NAMESPACE = DONGFANG_NAMESPACE;

  public static final String DTD_NAMESPACE_PREFIX = "dtdns";

  public static final String NAMESPACE_NAMESPACE_URI = "http://www.w3.org/XML/1998/namespace";

  public static final String NAMESPACE_NAMESPACE_PREFIX = "xml";

  public static final Namespace NAMESPACE_NAMESPACE = Namespace.get(
      NAMESPACE_NAMESPACE_PREFIX, NAMESPACE_NAMESPACE_URI);

  /*
   * xs: bound to http://www.w3.org/2001/XMLSchema
   * 
   * xsi: bound to http://www.w3.org/2001/XMLSchema-instance
   * 
   * xdt: bound to http://www.w3.org/2005/xpath-datatypes
   * 
   * fn: bound to http://www.w3.org/2005/xpath-functions
   */
}
