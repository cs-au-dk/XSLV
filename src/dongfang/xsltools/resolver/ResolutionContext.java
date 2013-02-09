/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.resolver;

import java.io.IOException;
import java.util.logging.Logger;

import org.xml.sax.InputSource;

import dongfang.xsltools.experimental.progresslogging.ProgressLogger;

/**
 * Basically, this is like an EntityResolver, plus some user help text features,
 * minus URI resolution (maybe it should really be included here; lost track of
 * that).
 * 
 * @author dongfang
 */
public interface ResolutionContext {
  /*
   * String INPUT_SCHEMA_ROOT_ELEMENT_NAME = "input-root-element"; String
   * INPUT_DTD_NAMESPACE_URI = "dtd-input-namespace"; String
   * OUTPUT_SCHEMA_ROOT_ELEMENT_NAME = "output-root-element"; String
   * OUTPUT_DTD_NAMESPACE_URI = "dtd-output-namespace"; String
   * INPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER = "input-schema"; String
   * OUTPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER = "output-schema"; String
   * STYLESHEET_PRINCIPAL_MODULE_IDENTIFIER = "principal-module"; String UNKNOWN =
   * "Unknown kind";
   */

  short INPUT = 0;

  short OUTPUT = 1;

  short UNKNOWN_RESOURCE = 0;

  short INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY = 1;

  short OUTPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY = 2;

  short INPUT_DTD_NAMESPACE_URI_KEY = 3;

  short OUTPUT_DTD_NAMESPACE_URI_KEY = 4;

  short INPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY = 5;

  short OUTPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY = 6;

  short STYLESHEET_PRINCIPAL_MODULE_IDENTIFIER_KEY = 7;

  short STYLESHEET_MODULE_IDENTIFIER_KEY = 8;

  short XSD_DOCUMENT_IDENTIFIER_KEY = 9;

  short XMLINSTANCE_MODULE_IDENTIFIER_KEY = 10;

  short SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY = 11;

  short SCHEMA_SECONDARY_COMPONENT_IDENTIFIER_KEY = 12;

  short RELAXNG_MODULE_IDENTIFIER_KEY = 13;

  short INPUT_SCHEMA_FQ_ROOT_ELEMENT_NAME_KEY = 14;

  short OUTPUT_SCHEMA_FQ_ROOT_ELEMENT_NAME_KEY = 15;

  String[] SystemInterfaceStrings = {
  /* 0 */"unknown-kind",
  /* 1 */"input-root-element",
  /* 2 */"output-root-element",
  /* 3 */"dtd-input-namespace",
  /* 4 */"dtd-output-namespace",
  /* 5 */"input-schema",
  /* 6 */"output-schema",
  /* 7 */"principal-module.xsl",
  /* 8 */"module",
  /* 9 */"xsd",
  /* 10 */"xmlinstance",
  /* 11 */"schema-primary-component",
  /* 12 */"schema-secondary-component",
  /* 13 */"rng-module",
  /* 14 */"fq-input-root-element",
  /* 15 */"fq-output-root-element" };

  String[] HUMAN_INTERFACE_STRINGS = {
      /* 0 */"unknown resource",
      /* 1 */"input schema root element name",
      /* 2 */"output schema root element name",
      /* 3 */"input namespace URI (for DTD)",
      /* 4 */"output namespace URI (for DTD)",
      /* 5 */"input schema",
      /* 6 */"output schema for validation",
      /* 7 */"principal stylesheet module",
      /* 8 */"stylesheet module",
      /* 9"Relax NG Module", */
      /* 9 */"XML Schema document",
      /* 10 */"XML instance document",
      /* 11 */"schema document",
      /* 12 */"schema document",
      /* 13 */"RNG module",
      /* 14 */"qualified name of input root element, ({namespace-uri}elementname, or just elementname if no namespace used)",
      /* 15 */"qualified name of output root element, ({namespace-uri}elementname, or just elementname if no namespace used)" };

  String MSG_FAIL = "Failed to load";

  String MSG_UNKNOWN_KIND_INPUT_SCHEMA = "The transform input schema (DTD, XML or Restricted Relax NG)";

  String MSG_UNKNOWN_KIND_OUTPUT_SCHEMA = "The transform output schema (DTD, XML or Restricted Relax NG)";

  String MSG_UNKNOWN_KIND_SCHEMA = "A schema document: DTD, XML Schema or Restricted Relax NG are accepted.";

  String MSG_DTD = "DTD";

  String MSG_RNG = "Relax NG";

  String MSG_XSD = "XML Schema";

  String MSG_XSL_PRINCIPAL = "The XSL stylesheet to analyze";

  String MSG_XSL_ANY = "An XSL Stylesheet module";

  Logger logger = ProgressLogger.getThreadLocalLogger();//("xslv.resolver");

  /**
   * Get a stream resource.
   * 
   * @param systemId -
   *          a stringed URI, identifying the resource. This is often a pure
   *          identifer (URI), and not a locator (URL) (in fact, an URN could
   *          have been used...): It is up to the implementation what to make of
   *          it.
   * @param userExplanation -
   *          a string to help a human user understand what is being requested
   *          (like "the input schema" etc.).
   * @param humanKey
   * @return
   * @throws IOException
   */
  InputSource resolveStream(String systemId, String userExplanation,
      int humanKey) throws IOException;

  /*
   * Not used; obsolete. Purpose was to let give a resolver a chance to prepare
   * in advance for handling requests.
   */
  void earlyStreamRequest(String systemId, String userExplanation, int humanKey)
      throws IOException;
}
