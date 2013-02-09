/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.context;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.xml.sax.InputSource;

import dk.brics.relaxng.Grammar;
import dk.brics.relaxng.converter.Data2Automaton;
import dk.brics.relaxng.converter.NameClass2Automaton;
import dk.brics.relaxng.converter.RNGParser;
import dk.brics.relaxng.converter.RestrRelaxNG2XMLGraph;
import dk.brics.relaxng.converter.SchemaReducer;
import dk.brics.relaxng.converter.StandardDatatypes;
import dk.brics.relaxng.converter.xmlschema.XMLSchema2RestrRelaxNG;
import dk.brics.xmlgraph.XMLGraph;
import dk.brics.xmlgraph.converter.XMLGraphReducer;
import dk.brics.xmlgraph.validator.ValidationErrorHandler;
import dk.brics.xmlgraph.validator.Validator;
import dongfang.xsltools.controlflow.ControlFlowConfiguration;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.controlflow.DeathFollowerUpper;
import dongfang.xsltools.controlflow.FastValidationFlowAnalyzer;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ShowStopperErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.experimental.progresslogging.ProgressLogger;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.simplification.DefaultSimplifier;
import dongfang.xsltools.util.UniqueNameGenerator;
import dongfang.xsltools.validation.ValidationResult;
import dongfang.xsltools.xmlclass.SchemaKindSniffer;
import dongfang.xsltools.xmlclass.dtd.DTD;
import dongfang.xsltools.xmlclass.relaxng.RelaxNGFactory;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xsd.XSDSchemaFactory;

/**
 * @author dongfang
 */
public abstract class ValidationContextImpl implements ValidationContext {
  private static final String OUTPUT_SCHEMA_RESOLUTION_METHOD = "DF";

  private ResolutionContext resolver;



  /*
   * A BRICS Schematools thing.
   */
  private StandardDatatypes datatypes = new StandardDatatypes();

  /*
   * Input type cache (belongs here??)
   */
  private Map<String, SingleTypeXMLClass> inputTypeCache = new HashMap<String, SingleTypeXMLClass>();

  // This will be a singleton...
  private Set<String> taintedStylesheets = new HashSet<String>();

  private Map<String, Set<String>> knownStylesheetResources = new HashMap<String, Set<String>>();

  private Map<String, Stylesheet> stylesheetCache = new HashMap<String, Stylesheet>();

  private Set<String> taintedInputTypes = new HashSet<String>();

  private Map<String, Set<String>> knownInputResources = new HashMap<String, Set<String>>();

  private Map<String, XMLGraph> outputTypeCache = new HashMap<String, XMLGraph>();

  private Set<String> taintedOutputTypes = new HashSet<String>();

  private Map<String, Set<String>> knownOutputResources = new HashMap<String, Set<String>>();

  private String recordingInputType;

  private String recordingOutputType;

  private String recordingStylesheetName;

  protected boolean isStylesheetTainted(String name) {
    boolean taintedtt = taintedStylesheets.contains(name);
    boolean noinputset = !knownStylesheetResources.containsKey(name);
    boolean emptyinputset = noinputset ? false : knownStylesheetResources.get(
        name).isEmpty();

    boolean result = false;

    if (taintedtt) {
      logger.fine("Stylesheet " + name
          + " is tainted because tainted set contains it");
      result = true;
    }
    if (noinputset) {
      logger.fine("Stylesheet " + name
          + " is tainted because there is no input set for it");
      result = true;
    }
    if (emptyinputset) {
      logger.fine("Stylesheet " + name
          + " is tainted because there is an empty input set for it");
      result = true;
    }
    return result;
  }

  private void untaintStylesheet(String name) {
    logger.fine("Untainting stylesheet: " + name);
    taintedOutputTypes.remove(name);
  }

  /*
   * Type is the name of the type. When validating w a single input schema,
   * there will be only one.
   */
  protected boolean isInputTypeTainted(String type) {
    boolean taintedtt = taintedInputTypes.contains(type);
    boolean noinputset = !knownInputResources.containsKey(type);
    boolean emptyinputset = noinputset ? false : knownInputResources.get(type)
        .isEmpty();

    boolean result = false;

    if (taintedtt) {
      logger.fine("Input type " + type
          + " is tainted because tainted set contains it");
      result = true;
    }
    if (noinputset) {
      logger.fine("Input type " + type
          + " is tainted because there is no input set for it");
      result = true;
    }
    if (emptyinputset) {
      logger.fine("Input type " + type
          + " is tainted because there is an empty input set for it");
      result = true;
    }
    return result;
  }

  private void untaintInputType(String type) {
    logger.fine("Untainting input type: " + type);
    taintedInputTypes.remove(type);
  }

  /*
   * Type is the name of the type. When validating w a single output schema,
   * there will be only one.
   */
  protected boolean isOutputTypeTainted(String type) {
    boolean taintedtt = taintedOutputTypes.contains(type);
    boolean noinputset = !knownOutputResources.containsKey(type);
    boolean emptyinputset = noinputset ? false : knownOutputResources.get(type)
        .isEmpty();

    boolean result = false;

    if (taintedtt) {
      logger.fine("Output type " + type
          + " is tainted because tainted set contains it");
      result = true;
    }
    if (noinputset) {
      logger.fine("Output type " + type
          + " is tainted because there is no input set for it");
      result = true;
    }
    if (emptyinputset) {
      logger.fine("Output type " + type
          + " is tainted because there is an empty input set for it");
      result = true;
    }
    return result;
  }

  private void untaintOutputType(String type) {
    logger.fine("Untainting output type: " + type);
    taintedOutputTypes.remove(type);
  }

  public void setResolver(ResolutionContext resolver) {
    this.resolver = resolver;
  }

  public void earlyStreamRequest(String systemId, String userExplanation,
      int humanKey) throws IOException {
  }

  /**
   * Load the input schema (a luxury method that loads the real schema, not just
   * the document representations)
   */
  public SingleTypeXMLClass getInputType(String systemId)
      throws XSLToolsLoadException {
    long time = System.currentTimeMillis();
    try {
      if (isInputTypeTainted(systemId)) {
        logger.fine("Input type " + systemId
            + " is tainted; loading from below");
        if (!knownInputResources.containsKey(systemId)) {
          logger.fine("Adding map for " + systemId
              + " to known input resources.");
          knownInputResources.put(systemId, new HashSet<String>());
        }
        logger.fine("Recording input type: " + systemId);
        recordingInputType = systemId;

        /*
         * logger.fine("b knownInputResources: " + knownInputResources);
         * logger.fine("b taintedInputResources: " + taintedInputTypes);
         * logger.fine("b InputTypeCache: " + inputTypeCache);
         */

        try {
          /*
           * Try detect the schema kind (DTD, XSD, etc...)
           */
          String sniff = SchemaKindSniffer.sniffSchemaKind(this, systemId,
              ResolutionContext.INPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY);
          SingleTypeXMLClass c = null;
          /*
           * Now call the right schema generator for the kind
           */
          if (sniff == MSG_XSD)
            c = XSDSchemaFactory.createSchema(systemId,
                ResolutionContext.INPUT, this, new ShowStopperErrorReporter());
          else if (sniff == MSG_DTD) {
            InputSource dtdin = resolveStream(systemId,
                ResolutionContext.MSG_UNKNOWN_KIND_INPUT_SCHEMA,
                INPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY);
            if (dtdin == null)
              throw new NullPointerException("Input identifier "
                  + getSchemaIdentifier(systemId, INPUT) + " resolved to null");
            if (dtdin.getCharacterStream() == null)
              throw new NullPointerException("Input identifier "
                  + getSchemaIdentifier(systemId, INPUT)
                  + "resolved to a worthless InputSource");
            c = new DTD(dtdin, this, systemId, ResolutionContext.INPUT);
          } else if (sniff == MSG_RNG) {
            c = RelaxNGFactory.getSimplifiedRelaxNG(this, systemId);
          } else {
            throw new XSLToolsLoadException("Failed to load schema at: "
                + systemId + " (kind detected: " + sniff + ")");
          }
          inputTypeCache.put(systemId, c);
          untaintInputType(systemId);
          return c;
        } catch (Exception ex) {
          Throwable ex2 = ex;
          while (ex2 != null) {
            ex2.printStackTrace();
            ex2 = ex2.getCause();
          }
          throw new XSLToolsLoadException(ex);
        } finally {
          recordingInputType = null;
          /*
           * logger.info("a knownInputResources: " + knownInputResources);
           * logger.info("a taintedInputResources: " + taintedInputTypes);
           * logger.info("a InputTypeCache: " + inputTypeCache);
           */
        }
      } else {
        logger.fine("*** used the input type cache!");
        return inputTypeCache.get(systemId);
      }
    } finally {
      logger.fine("Got input type in " + (System.currentTimeMillis() - time)
          + " millis");
    }
  }

  public XMLGraph getOutputType(String systemId) throws XSLToolsLoadException {
    long time = System.currentTimeMillis();
    try {
      XMLGraph sg;
      if (isOutputTypeTainted(systemId)) {
        logger.fine("Output type " + systemId
            + " is tainted; loading from below");
        if (!knownOutputResources.containsKey(systemId)) {
          logger.fine("Adding map for " + systemId
              + " to known output resources.");
          knownOutputResources.put(systemId, new HashSet<String>());
        }
        logger.fine("Recording output: " + systemId);
        recordingOutputType = systemId;
        try {
          String sniff = SchemaKindSniffer.sniffSchemaKind(this, systemId,
              ResolutionContext.OUTPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY);
          // logger.info("Loading input schema: " + systemId + ", kind=" +
          // sniff);
          // setStage(INPUT_STAGE);
          if (sniff == MSG_XSD) {
            // the BRICS way

            // InputSource source = resolveStream(systemId,
            // ResolutionContext.MSG_UNKNOWN_KIND_OUTPUT_SCHEMA,
            // ResolutionContext.OUTPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY);
            if ("BRICS".equals(OUTPUT_SCHEMA_RESOLUTION_METHOD)) {
              Grammar g = null;

              String _systemId = getRootElementNameIdentifier(
                  ResolutionContext.SystemInterfaceStrings[ResolutionContext.INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY
                      + OUTPUT], OUTPUT);

              URL url = new URL(systemId);

              XMLSchema2RestrRelaxNG xs2rrnc = new XMLSchema2RestrRelaxNG(
                  new StandardDatatypes());

              String documentElementNameFromContext = resolveString(_systemId,
                  "Namespace-qualified name of designated root element, on the form {namespace-uri}elementname, " +
                  "or just elementname if no namespace is used", "Auto-detect",
                  ResolutionContext.INPUT_SCHEMA_FQ_ROOT_ELEMENT_NAME_KEY + OUTPUT);
              
              org.jdom.Document rrng = xs2rrnc.convert(url, documentElementNameFromContext);

              RNGParser rng_parser = new RNGParser();
              // dette har et problem .. hvad??
              g = rng_parser.parse(rrng, url);

              // g = rng_parser.parse(source);
              if (!g.check(System.err)) {
                throw new XSLToolsLoadException(
                    "schema is not Restricted RELAX NG, aborting");
              }
              if (g != null) {
                SchemaReducer schema_reducer = new SchemaReducer(
                    new NameClass2Automaton(), new Data2Automaton(g, datatypes));
                schema_reducer.reduce(g);
                RestrRelaxNG2XMLGraph converter = new RestrRelaxNG2XMLGraph(
                    null, datatypes);
                sg = converter.convert(g);
                outputTypeCache.put(systemId, sg);
                untaintOutputType(systemId);
                return sg;
              }
            } else {
              // the dongfang way

              ErrorReporter cesspool = new ShowStopperErrorReporter();
              SingleTypeXMLClass c = null;
              c = XSDSchemaFactory.createSchema(systemId,
                  ResolutionContext.OUTPUT, this, cesspool);
              sg = c.createSG(systemId);
              outputTypeCache.put(systemId, sg);
              untaintOutputType(systemId);
              return sg;
            }
          } else if (sniff == MSG_DTD) {
            SingleTypeXMLClass c = null;
            InputSource dtdin = resolveStream(systemId,
                ResolutionContext.MSG_UNKNOWN_KIND_OUTPUT_SCHEMA,
                OUTPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY);
            if (dtdin == null)
              throw new NullPointerException("Input identifier "
                  + getSchemaIdentifier(systemId, INPUT) + " resolved to null");

            if (dtdin.getCharacterStream() == null)
              throw new NullPointerException("Input identifier "
                  + getSchemaIdentifier(systemId, INPUT)
                  + "resolved to a worthless InputSource");
            c = new DTD(dtdin, this, systemId, ResolutionContext.OUTPUT);
            sg = c.createSG(systemId);
            outputTypeCache.put(systemId, sg);
            untaintOutputType(systemId);
            return sg;
          }

          InputSource source = resolveStream(systemId,
              ResolutionContext.MSG_UNKNOWN_KIND_OUTPUT_SCHEMA,
              ResolutionContext.OUTPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY);
          Grammar g = null;
          RNGParser rng_parser = new RNGParser();
          InputStream is = source.getByteStream();
          // String sid = source.getSystemId();
          URL url = new URL(systemId);
          // dette har et problem .. hvad??
          g = rng_parser.parse(is, url);
          // g = rng_parser.parse(source);
          if (!g.check(System.err)) {
            throw new XSLToolsLoadException(
                "schema is not Restricted RELAX NG, aborting");
          }
          if (g != null) {
            SchemaReducer schema_reducer = new SchemaReducer(
                new NameClass2Automaton(), new Data2Automaton(g, datatypes));
            schema_reducer.reduce(g);
            RestrRelaxNG2XMLGraph converter = new RestrRelaxNG2XMLGraph(null,
                datatypes);
            sg = converter.convert(g);
            outputTypeCache.put(systemId, sg);
            untaintOutputType(systemId);
            return sg;
          }
          throw new XSLToolsLoadException("Graph loaded was null");
          // break;
          // case XML_SCHEMA:
          // g = rng_parser.parse(xsd2rrng.convert(url), url);
          // break;
        } catch (Exception ex) {
          throw new XSLToolsLoadException(ex);
        } finally {
          recordingOutputType = null;
        }
      } else {
        logger.fine("*** used the output type cache!");
        return outputTypeCache.get(systemId);
      }
    } finally {
      logger.fine("Got output type in " + (System.currentTimeMillis() - time)
          + " millis");
    }
  }

  protected void taintInputSource(String systemId) {
    logger.fine("Delivering InputSource (TaintMatic): " + systemId);
    // streamCache.put(systemId, stream);
    Set<String> ss = knownStylesheetResources.get(recordingStylesheetName);
    if (ss != null) {
      logger.fine("InputSource input schema recording is for "
          + recordingStylesheetName + "; recording " + systemId);
      ss.add(systemId); // record
    }
    for (Map.Entry<String, Set<String>> sss : knownStylesheetResources
        .entrySet()) {
      if (sss.getValue().contains(systemId)) {
        logger.fine("Tainting input schema: " + sss.getKey());
        taintedStylesheets.add(sss.getKey());
      }
    }

    ss = knownInputResources.get(recordingInputType);
    if (ss != null) {
      logger.fine("InputSource input schema recording is for "
          + recordingInputType + "; recording " + systemId);
      ss.add(systemId); // record
    }
    for (Map.Entry<String, Set<String>> sss : knownInputResources.entrySet()) {
      if (sss.getValue().contains(systemId)) {
        logger.fine("Tainting input schema: " + sss.getKey());
        taintedInputTypes.add(sss.getKey());
      }
    }

    ss = knownOutputResources.get(recordingOutputType);
    if (ss != null) {
      logger.fine("InputSource output schema recording is for "
          + recordingInputType + "; recording " + systemId);
      ss.add(systemId); // record
    }
    for (Map.Entry<String, Set<String>> sss : knownOutputResources.entrySet()) {
      if (sss.getValue().contains(systemId)) {
        logger.fine("Tainting output schema: " + sss.getKey());
        taintedOutputTypes.add(sss.getKey());
      }
    }
  }

  public InputSource resolveStream(String systemId, String userExplanation,
      int humanReadableKey) throws IOException {
    InputSource source = resolver.resolveStream(systemId, userExplanation,
        humanReadableKey);
    taintInputSource(systemId);
    return source;
  }

  public ControlFlowGraph getControlFlowGraph(ErrorReporter cesspool)
      throws XSLToolsException {
    /*
     * This analyzer is now only around for historical purposes. It is obsolete.
     * 
     * if (context.doUseAutomata()) xcfg =
     * ValidationFlowAnalyzer.getInstance().analyze(stylesheet, context,
     * cesspool); else
     */

    ControlFlowGraph xcfg = FastValidationFlowAnalyzer.getInstance().analyze(
        getStylesheet(cesspool), this, cesspool);

    if (ControlFlowConfiguration.current.runCodeAssistAlgorithms()) {
      DeathFollowerUpper.getInstance().analyze(xcfg, this, null, null);
    }

    if (ControlFlowConfiguration.current
        .sanityCheckContextSetsWithContextFlows())
      xcfg.sanityCheckFlows();

    return xcfg;
  }

  protected Stylesheet getStylesheet(String systemId, ErrorReporter cesspool)
      throws XSLToolsException {
    // long time = System.currentTimeMillis();
    try {
      if (isStylesheetTainted(systemId)) {
        if (!knownStylesheetResources.containsKey(systemId)) {
          logger.fine("Adding map for " + systemId
              + " to known stylesheet resources.");
          knownStylesheetResources.put(systemId, new HashSet<String>());
        }
        logger.fine("Recording stylesheet name: " + systemId);
        recordingStylesheetName = systemId;
        UniqueNameGenerator names = new UniqueNameGenerator();
        DefaultSimplifier defaultSimplifier = DefaultSimplifier.getInstance(
            this, cesspool, names);
        Stylesheet stylesheet = defaultSimplifier.getStylesheet(systemId, this,
            cesspool);
        if (!cesspool.hasErrors()) {
          stylesheetCache.put(systemId, stylesheet);
          untaintStylesheet(systemId);
        }
      }
    } catch (XSLToolsException ex) {
      throw ex;
    } finally {
      recordingStylesheetName = null;
    }
    return stylesheetCache.get(systemId);
  }

  public Stylesheet getStylesheet(ErrorReporter cesspool)
      throws XSLToolsException {
    return getStylesheet(getStylesheetIdentifier(), cesspool);
  }

  public XMLGraph getControlFlowXMLGraph(ErrorReporter cesspool)
      throws XSLToolsException {
    XMLGraph xcfgSG = getControlFlowGraph(cesspool).constructSummaryGraph(
        getInputType(getSchemaIdentifier("", ResolutionContext.INPUT)));

    if (ControlFlowConfiguration.current.reduceSummaryGraph()) {
      new XMLGraphReducer().reduce(xcfgSG);
    }

    if (ControlFlowConfiguration.current.checkSummaryGraph()) {
      xcfgSG.check(System.err);
    }

    return xcfgSG;
  }

  public void validate(ErrorReporter cesspool, ValidationResult result)
      throws XSLToolsException {
    XMLGraph xformType = getControlFlowXMLGraph(cesspool);
    XMLGraph outputType = getOutputType(getSchemaIdentifier("", OUTPUT));

    Validator validator = new Validator((ValidationErrorHandler) result);
    validator.validate(xformType, outputType, -1);
  }

  public void reset() {
    inputTypeCache.clear();
    taintedInputTypes.clear();
    knownInputResources.clear();
    outputTypeCache.clear();
    taintedOutputTypes.clear();
    knownOutputResources.clear();
    stylesheetCache.clear();
  }
}
