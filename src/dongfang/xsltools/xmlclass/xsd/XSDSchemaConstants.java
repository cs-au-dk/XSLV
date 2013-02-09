/*
 * dongfang M. Sc. Thesis
 * Created on Jun 4, 2005
 */
package dongfang.xsltools.xmlclass.xsd;

import java.util.HashSet;
import java.util.Set;

import org.dom4j.Namespace;
import org.dom4j.QName;

import dongfang.XMLConstants;

/**
 * @author Soren Kuula
 */
public class XSDSchemaConstants {
  public static final String NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";

  public static final String INSTANCE_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";

  public static final String DATATYPE_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-datatypes";

  public static final Namespace NAMESPACE = Namespace.get(NAMESPACE_URI);

  public static final Namespace INSTANCE_NAMESPACE = Namespace
      .get(INSTANCE_NAMESPACE_URI);

  public static final String ELEM_ALL = "all";

  public static final QName ELEM_ALL_QNAME = QName.get(ELEM_ALL, NAMESPACE);

  public static final String ELEM_ANNOTATION = "annotation";

  public static final QName ELEM_ANNOTATION_QNAME = QName.get(ELEM_ANNOTATION,
      NAMESPACE);

  public static final String ELEM_ATTRIBUTE_GROUP = "attributeGroup";

  public static final QName ELEM_ATTRIBUTE_GROUP_QNAME = QName.get(
      ELEM_ATTRIBUTE_GROUP, NAMESPACE);

  public static final String ELEM_CHOICE = "choice";

  public static final QName ELEM_CHOICE_QNAME = QName.get(ELEM_CHOICE,
      NAMESPACE);

  public static final String ELEM_LIST = "list";

  public static final QName ELEM_LIST_QNAME = QName.get(ELEM_LIST, NAMESPACE);

  public static final String ELEM_UNION = "union";

  public static final QName ELEM_UNION_QNAME = QName.get(ELEM_UNION, NAMESPACE);

  public static final String ELEM_SEQUENCE = "sequence";

  public static final QName ELEM_SEQUENCE_QNAME = QName.get(ELEM_SEQUENCE,
      NAMESPACE);

  public static final String ELEM_GROUP = "group";

  public static final QName ELEM_GROUP_QNAME = QName.get(ELEM_GROUP, NAMESPACE);

  public static final String ELEM_ELEMENT = "element";

  public static final QName ELEM_ELEMENT_QNAME = QName.get(ELEM_ELEMENT,
      NAMESPACE);

  public static final String ELEM_ATTRIBUTE = "attribute";

  public static final QName ELEM_ATTRIBUTE_QNAME = QName.get(ELEM_ATTRIBUTE,
      NAMESPACE);

  public static final String ELEM_SIMPLE_TYPE = "simpleType";

  public static final QName ELEM_SIMPLE_TYPE_QNAME = QName.get(
      ELEM_SIMPLE_TYPE, NAMESPACE);

  public static final String ELEM_SIMPLE_CONTENT = "simpleContent";

  public static final QName ELEM_SIMPLE_CONTENT_QNAME = QName.get(
      ELEM_SIMPLE_CONTENT, NAMESPACE);

  public static final String ELEM_COMPLEX_TYPE = "complexType";

  public static final QName ELEM_COMPLEX_TYPE_QNAME = QName.get(
      ELEM_COMPLEX_TYPE, NAMESPACE);

  public static final String ELEM_COMPLEX_CONTENT = "complexContent";

  public static final QName ELEM_COMPLEX_CONTENT_QNAME = QName.get(
      ELEM_COMPLEX_CONTENT, NAMESPACE);

  public static final String ELEM_IMPORT = "import";

  public static final QName ELEM_IMPORT_QNAME = QName.get(ELEM_IMPORT,
      NAMESPACE);

  public static final String ELEM_INCLUDE = "include";

  public static final QName ELEM_INCLUDE_QNAME = QName.get(ELEM_INCLUDE,
      NAMESPACE);

  public static final String ELEM_RESTRICTION = "restriction";

  public static final QName ELEM_RESTRICTION_QNAME = QName.get(
      ELEM_RESTRICTION, NAMESPACE);

  public static final String ELEM_EXTENSION = "extension";

  public static final QName ELEM_EXTENSION_QNAME = QName.get(ELEM_EXTENSION,
      NAMESPACE);

  public static final String ELEM_SCHEMA = "schema";

  public static final QName ELEM_SCHEMA_QNAME = QName.get(ELEM_SCHEMA,
      NAMESPACE);

  public static final String ELEM_MIN_EXCLUSIVE = "minExclusive";

  public static final String ELEM_MIN_INCLUSIVE = "minInclusive";

  public static final String ELEM_MAX_ExCLUSIVE = "maxExclusive";

  public static final String ELEM_MAX_INCLUSIVE = "maxInclusive";

  public static final String ELEM_TOTAL_DIGITS = "totalDigits";

  public static final String ELEM_FRACTION_DIGITS = "fractionDigits";

  public static final String ELEM_LENGTH = "length";

  public static final String ELEM_MIN_LENGTH = "minLength";

  public static final String ELEM_MAX_LENGTH = "maxLength";

  public static final String ELEM_ENUMERATION = "enumeration";

  public static final String ELEM_WHITESPACE = "whiteSpace";

  public static final String ELEM_PATTERN = "pattern";

  public static final Set<String> FACET_NAMES = new HashSet<String>();
  static {
    FACET_NAMES.add(ELEM_MIN_EXCLUSIVE);
    FACET_NAMES.add(ELEM_MIN_INCLUSIVE);
    FACET_NAMES.add(ELEM_MAX_ExCLUSIVE);
    FACET_NAMES.add(ELEM_MAX_INCLUSIVE);
    FACET_NAMES.add(ELEM_TOTAL_DIGITS);
    FACET_NAMES.add(ELEM_FRACTION_DIGITS);
    FACET_NAMES.add(ELEM_LENGTH);
    FACET_NAMES.add(ELEM_MIN_LENGTH);
    FACET_NAMES.add(ELEM_MAX_LENGTH);
    FACET_NAMES.add(ELEM_ENUMERATION);
    FACET_NAMES.add(ELEM_WHITESPACE);
    FACET_NAMES.add(ELEM_PATTERN);
  }

  public static final String ATTR_BLOCK = "block";

  public static final QName ATTR_BLOCK_QNAME = QName.get(ATTR_BLOCK,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_FIXED = "fixed";

  public static final QName ATTR_FIXED_QNAME = QName.get(ATTR_FIXED,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_BLOCK_DEFAULT = "blockDefault";

  public static final QName ATTR_BLOCK_DEFAULT_QNAME = QName.get(
      ATTR_BLOCK_DEFAULT, Namespace.NO_NAMESPACE);

  public static final String ATTR_MEMBER_TYPES = "memberTypes";

  public static final QName ATTR_MEMBER_TYPES_QNAME = QName.get(
      ATTR_MEMBER_TYPES, Namespace.NO_NAMESPACE);

  public static final String ATTR_NILLABLE = "nillable";

  public static final QName ATTR_NILLABLE_QNAME = QName.get(ATTR_NILLABLE,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_ABSTRACT = "abstract";

  public static final QName ATTR_ABSTRACT_QNAME = QName.get(ATTR_ABSTRACT,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_TARGET_NAMESPACE = "targetNamespace";

  public static final QName ATTR_TARGET_NAMESPACE_QNAME = QName.get(
      ATTR_TARGET_NAMESPACE, Namespace.NO_NAMESPACE);

  public static final String ATTR_NAMESPACE = "namespace";

  public static final QName ATTR_NAMESPACE_QNAME = QName.get(ATTR_NAMESPACE,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_TYPE = "type";

  public static final QName ATTR_TYPE_QNAME = QName.get(ATTR_TYPE,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_NAME = "name";

  public static final QName ATTR_NAME_QNAME = QName.get(ATTR_NAME,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_REF = "ref";

  public static final QName ATTR_REF_QNAME = QName.get(ATTR_REF,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_BASE = "base";

  public static final QName ATTR_BASE_QNAME = QName.get(ATTR_BASE,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_MIN_OCCURS = "minOccurs";

  public static final QName ATTR_MIN_OCCURS_QNAME = QName.get(ATTR_MIN_OCCURS,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_MAX_OCCURS = "maxOccurs";

  public static final QName ATTR_MAX_OCCURS_QNAME = QName.get(ATTR_MAX_OCCURS,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_ELEMENT_FORM_DEFAULT = "elementFormDefault";

  public static final QName ATTR_ELEMENT_FORM_DEFAULT_QNAME = QName.get(
      ATTR_ELEMENT_FORM_DEFAULT, Namespace.NO_NAMESPACE);

  public static final String ATTR_ATTRIBUTE_FORM_DEFAULT = "attributeFormDefault";

  public static final QName ATTR_ATTRIBUTE_FORM_DEFAULT_QNAME = QName.get(
      ATTR_ATTRIBUTE_FORM_DEFAULT, Namespace.NO_NAMESPACE);

  public static final String ATTR_FORM = "form";

  public static final QName ATTR_FORM_QNAME = QName.get(ATTR_FORM,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_SCHEMA_LOCATION = "schemaLocation";

  public static final QName ATTR_SCHEMA_LOCATION_QNAME = QName.get(
      ATTR_SCHEMA_LOCATION, Namespace.NO_NAMESPACE);

  public static final String ATTR_ITEM_TYPE = "itemType";

  public static final QName ATTR_ITEM_TYPE_QNAME = QName.get(ATTR_ITEM_TYPE,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_SUBSTITUTION_GROUP = "substitutionGroup";

  public static final QName ATTR_SUBSTITUTION_GROUP_QNAME = QName.get(
      ATTR_SUBSTITUTION_GROUP, Namespace.NO_NAMESPACE);

  public static final String ATTR_MIXED = "mixed";

  public static final QName ATTR_MIXED_QNAME = QName.get(ATTR_MIXED,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_USE = "use";

  public static final QName ATTR_USE_QNAME = QName.get(ATTR_USE,
      Namespace.NO_NAMESPACE);

  public static final String ATTR_VALUE = "value";

  public static final QName ATTR_VALUE_QNAME = QName.get(ATTR_VALUE,
      Namespace.NO_NAMESPACE);

  public static final String ELEM_ANY = "any";

  public static final QName ELEM_ANY_QNAME = QName.get(ELEM_ANY, NAMESPACE);

  public static final String ELEM_ANY_ATTRIBUTE = "anyAttribute";

  public static final QName ELEM_ANY_ATTRIBUTE_QNAME = QName.get(
      ELEM_ANY_ATTRIBUTE, NAMESPACE);

  public static final String ANYTYPE = "anyType";

  public static final QName ANYTYPE_QNAME = QName.get(ANYTYPE, NAMESPACE);

  public static final String ANYSIMPLETYPE = "anySimpleType";

  public static final QName ANYSIMPLETYPE_QNAME = QName.get(ANYSIMPLETYPE,
      NAMESPACE);

  // Placeholders meaning: Nothing was specified about the type,
  // but at least we are able to see whether type was simple or
  // complex...
  public static final String UNSPECIFIED_TYPE = "unspecifiedType";

  public static final QName UNSPECIFIED_TYPE_QNAME = QName.get(
      UNSPECIFIED_TYPE, XMLConstants.DONGFANG_NAMESPACE);

  public static final String UNSPECIFIED_SIMPLETYPE = "unspecifiedSimpleType";

  public static final QName UNSPECIFIED_SIMPLETYPE_QNAME = QName.get(
      UNSPECIFIED_SIMPLETYPE, XMLConstants.DONGFANG_NAMESPACE);

  public static final String STRINGTYPE = "string";

  public static final QName STRINGTYPE_QNAME = QName.get(STRINGTYPE, NAMESPACE);

  public static final String NIL = "nil";

  public static final QName NIL_QNAME = QName.get(NIL, INSTANCE_NAMESPACE);

  public static final String TYPE = "type";

  public static final QName TYPE_QNAME = QName.get(TYPE, INSTANCE_NAMESPACE);
}
