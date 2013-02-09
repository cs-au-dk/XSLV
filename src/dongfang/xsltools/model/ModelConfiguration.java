package dongfang.xsltools.model;

import org.dom4j.DocumentFactory;

public class ModelConfiguration {

  public static enum ParseLocationConfig {
    NO_PARSELOCATIONS, PARSELOCATIONS_AS_ATTRIBUTES, PARSELOCATIONS_IN_SPECIAL_ELEMENT_CLASS
  };

  public static enum XPathCacheConfig {
    // Reparse again and again...
    NO_XPATH_CACHE,
    // Test cached exp toString with attribute value
    // and reparse (+issue warning) if different
    SELFCHECK_XPATH_CACHE,
    // use cache and do not test if attribute expression
    // has changed w r to toString of cachec expression.
    // There might still be update bugs, so using it might
    // be dangerous.
    BOLD_XPATH_CACHE
  };

  public static ModelConfiguration current = new ModelConfiguration();

  private ParseLocationConfig parseLocationConfig = ParseLocationConfig.PARSELOCATIONS_IN_SPECIAL_ELEMENT_CLASS;

  private XPathCacheConfig xPathCacheConfig = XPathCacheConfig.NO_XPATH_CACHE;

  private boolean schemaValidateXSLTSource = false;

  private boolean schemaValidateXMLSchema = false;

  private boolean continueAfterXSLTParseOrValidationError = false;

  private boolean parserMergesAdjacentTextNodes = true;

  private boolean threadUnsafelyReuseReaders = true;

  private ModelConfiguration() {
  }

  public ModelConfiguration(ParseLocationConfig parseLocationConfig,
      XPathCacheConfig xPathCacheConfig, boolean validateXSLT,
      boolean continueAfterXSLTParseOrValidationError) {
    this.parseLocationConfig = parseLocationConfig;
    this.xPathCacheConfig = xPathCacheConfig;
    this.schemaValidateXSLTSource = validateXSLT;
    this.continueAfterXSLTParseOrValidationError = continueAfterXSLTParseOrValidationError;
  }

  /*
   * Add parse locations or not, and implementation style.
   */
  ParseLocationConfig getParseLocationConfig() {
    return parseLocationConfig;
  }

  /*
   * How to use XPath cache -- still in test phase
   */
  XPathCacheConfig getXPathCacheConfig() {
    return xPathCacheConfig;
  }

  /*
   * Validate input stylesheet module with XML Schema
   */
  boolean schemaValidateXSLTSource() {
    return schemaValidateXSLTSource;
  }

  boolean schemaValidateXMLSchema() {
    return schemaValidateXMLSchema;
  }

  /*
   * If false, stylesheet module factories will return an empty stylesheet if
   * there was an error in parsing or in validation (if enabled)
   */
  boolean continueAfterXSLTParseOrValidationError() {
    return continueAfterXSLTParseOrValidationError;
  }

  /*
   * Whether to log every operation on cache (good for finding miss sources
   * anyway)
   */
  public boolean debugXPathCache() {
    return false;// getXPathCacheConfig() == SELFCHECK_XPATH_CACHE;
  }

  /*
   * Whether to tell Dom4J to merge adjacent text nodes when parsing.
   */
  public boolean parserMergesAdjacentTextNodes() {
    return parserMergesAdjacentTextNodes;
  }

  /*
   * Get a stylesheet module factory. TODO: Can instances be reused thread
   * safely?
   */
  public StylesheetModuleFactory getStylesheetModuleFactory() {
    /*
     * KILL this line and the next if no parse locations desired
     */
    if (parseLocationConfig != ParseLocationConfig.NO_PARSELOCATIONS)
      return new ParseLocationStylesheetModuleFactory();

    return new NoParseLocationStylesheetModuleFactory();
  }

  public boolean threadUnsafelyReuseReaders() {
    return threadUnsafelyReuseReaders;
  }

  public DocumentFactory getDocumentFactory() {
    if (parseLocationConfig == ParseLocationConfig.PARSELOCATIONS_IN_SPECIAL_ELEMENT_CLASS)
      return ParseLocationDocumentFactory.getPLInstance();
    return DocumentFactory.getInstance();
  }
}
