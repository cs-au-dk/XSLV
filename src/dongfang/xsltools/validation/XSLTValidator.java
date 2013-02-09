/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.validation;

import java.io.IOException;

import dk.brics.xmlgraph.XMLGraph;
import dk.brics.xmlgraph.converter.XMLGraphReducer;
import dk.brics.xmlgraph.validator.Validator;
import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.controlflow.ControlFlowConfiguration;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.controlflow.DeathFollowerUpper;
import dongfang.xsltools.controlflow.FastValidationFlowAnalyzer;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.simplification.ApproximativeSimplifier;
import dongfang.xsltools.simplification.DefaultSimplifier;
import dongfang.xsltools.simplification.SemanticsPreservingSimplifier;
import dongfang.xsltools.util.UniqueNameGenerator;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

/**
 * This is the program that drives a ValidationRun.
 * @author dongfang
 */
public class XSLTValidator {

  /**
   * Get a simplified stylesheet.
   * @param systemId
   * @param context
   * @param cesspool
   * @return
   * @throws XSLToolsException
   */
  static Stylesheet getSemanticsPreservedStylesheet(String systemId,
      ValidationContext context, ErrorReporter cesspool)
      throws XSLToolsException {

    UniqueNameGenerator names = new UniqueNameGenerator();

    SemanticsPreservingSimplifier defaultSimplifier = SemanticsPreservingSimplifier.getInstance(
        context, cesspool, names);

    Stylesheet stylesheet = defaultSimplifier.getStylesheet(systemId, context,
        cesspool);

    if (cesspool.hasErrors())
      return null;

    return stylesheet;
  }

  /**
   * Get an approximated stylesheet.
   * This is a little ugly, as it circumvents the logging, timing etc. of DefaultSimplifier.
   * @param systemId
   * @param context
   * @param cesspool
   * @return
   * @throws XSLToolsException
   */
  static void approximateStylesheet(Stylesheet stylesheet, ErrorReporter cesspool)
      throws XSLToolsException {
    UniqueNameGenerator names = new UniqueNameGenerator();

    ApproximativeSimplifier defaultSimplifier = ApproximativeSimplifier.getInstance(
       cesspool, names);

    defaultSimplifier.process(stylesheet);

    //if (cesspool.hasErrors())
    //  return null;
  }

  /**
   * Get a simplified stylesheet.
   * @param systemId
   * @param context
   * @param cesspool
   * @return
   * @throws XSLToolsException
   */
  static Stylesheet getStylesheet(String systemId,
      ValidationContext context, ErrorReporter cesspool)
      throws XSLToolsException {

    UniqueNameGenerator names = new UniqueNameGenerator();

    DefaultSimplifier defaultSimplifier = DefaultSimplifier.getInstance(
        context, cesspool, names);

    Stylesheet stylesheet = defaultSimplifier.getStylesheet(systemId, context,
        cesspool);

    // if (cesspool.hasErrors())
    //  return null;

    return stylesheet;
  }

  /**
   * Once, a long time ago, there was an idea of requesting ALL the definitely 
   * needed resources from the beginning. This is where it happened. 
   * @param context
   * @throws IOException
   */
  public static void prepareValidation(ValidationContext context)
      throws IOException {
    // context.earlyStreamRequest(context.getInputSchemaIdentifier(""),
    // ResolutionContext.HUMAN_INPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY);
    // context.earlyStreamRequest(context.getOutputSchemaIdentifier(""),
    // ResolutionContext.HUMAN_OUTPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY);
    // context.earlyStreamRequest(context.getStylesheetIdentifier(),
    // ResolutionContext.HUMAN_STYLESHEET_PRINCIPAL_MODULE_IDENTIFIER_KEY);

    // assume DTD
    // context.earlyStringRequest(ResolutionContext.INPUT_DTD_NAMESPACE_URI,
    // ResolutionContext.HUMAN_INPUT_DTD_NAMESPACE_URI_KEY);
    // context.earlyStringRequest(
    // ResolutionContext.INPUT_SCHEMA_ROOT_ELEMENT_NAME,
    // ResolutionContext.HUMAN_INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY);
  }

  /**
   * Get a control flow graph, given a stylesheet.
   * The internals will pull an input schema out of the validation context.
   * @param systemId
   * @param stylesheet
   * @param context
   * @param cesspool
   * @return
   * @throws XSLToolsException
   */
  static ControlFlowGraph getCFG(String systemId, Stylesheet stylesheet,
      ValidationContext context, ErrorReporter cesspool)
      throws XSLToolsException {
    if (stylesheet == null)
      return null;
    ControlFlowGraph xcfg;
    /*
     * This analyzer is now only around for historical purposes. It is obsolete.
     * 
     * if (context.doUseAutomata()) xcfg =
     * ValidationFlowAnalyzer.getInstance().analyze(stylesheet, context,
     * cesspool); else
     */
    xcfg = FastValidationFlowAnalyzer.getInstance().analyze(stylesheet,
        context, cesspool);
    if (ControlFlowConfiguration.current
        .sanityCheckContextSetsWithContextFlows())
      xcfg.sanityCheckFlows();
    if (cesspool.hasErrors())
      return null;
    return xcfg;
  }

  /**
   * Given a control flow graph (which references to everything it needs in the stylesheet),
   * return an XMLGraph of the control flow. This will involve the input schema, pulled out
   * of the ValidationContext. 
   * @param xcfg
   * @param context
   * @return
   * @throws XSLToolsException
   */
  static XMLGraph getControlFlowSummaryGraph(ControlFlowGraph xcfg,
      ValidationContext context) throws XSLToolsException {
    PerformanceLogger pa = DiagnosticsConfiguration.current
        .getPerformanceLogger();
    pa.startTimer("Construction", "SGConstruction");
    XMLGraph xcfgSG = xcfg.constructSummaryGraph(context
        .getInputType(context.getSchemaIdentifier("", ResolutionContext.INPUT)));
    pa.stopTimer("Construction", "SGConstruction");
    if (ControlFlowConfiguration.current.reduceSummaryGraph()) {
      pa.startTimer("Reduction", "SGConstruction");
      new XMLGraphReducer().reduce(xcfgSG);
      pa.stopTimer("Reduction", "SGConstruction");
    }
    if (ControlFlowConfiguration.current.checkSummaryGraph()) {
      pa.startTimer("Check", "SGConstruction");
      xcfgSG.check(System.err);
      pa.stopTimer("Check", "SGConstruction");
    }
    return xcfgSG;
  }

  /**
   * Given a flow and an output XML Graph, perform validation.
   * @param outputType
   * @param xcfgSG
   * @return
   * @throws XSLToolsException
   */
  static ValidationResult validate(XMLGraph outputType,
      XMLGraph xcfgSG) throws XSLToolsException {

    XMLErrorHandler handler = new XMLErrorHandler();

    Validator validator = new Validator(handler);
    validator.validate(xcfgSG, outputType, -1);
    /*
     * logger.info("Done validating SG."); / * WebGenerator wg = new
     * WebGenerator("presentation/simple-annotated-xml-to-xhtml.xsl");
     * 
     * wg.process(clone, Stylesheet.ORIGINAL, new
     * File(ConfigurationFactory.getDumpConfiguration().generatedHTMLSensitivePrefix()));
     * wg.process(clone2, Stylesheet.ORIGINAL, new
     * File(ConfigurationFactory.getDumpConfiguration().generatedHTMLInsensitivePrefix())); /
     * handler.originalize(stylesheet, StylesheetModule.ORIGINAL);
     * 
     */
    return handler;
  }

  /**
   * Drive a ValidationRun thru validation.
   * @param systemId - System ID of principal module
   * @param context - Validation context (will provide schemas and other resources)
   * @param cesspool - where non-validation errors go.
   * @param story - Validation results go here (as do other results and reports).
   * @throws XSLToolsException
   */
  public static void validate(String systemId, ValidationContext context, ErrorReporter cesspool, ValidationRun story) throws XSLToolsException {
    /*
     * If we want to tell our envorinment about resources we want to fetch, go ahead.
     * This should really be the job of the ValidationRun.
     */
    try {
      prepareValidation(context);
    } catch (IOException ex) {
      throw new XSLToolsException(ex);
    }

    PerformanceLogger pa = DiagnosticsConfiguration.current.getPerformanceLogger();
    pa.startTimer("Validation", "Root");
    pa.startTimer("Simplification", "Validation");
    Stylesheet ss = getStylesheet(systemId, context, cesspool);
    pa.stopTimer("Simplification", "Validation");
    story.setSemPreservingSimplifiedStylesheet(ss);
    approximateStylesheet(ss, cesspool);
   
    story.setApproxSimplifiedStylesheet(ss);
    
    if (ss != null && story.relaxateProgress(ValidationRun.STYLESHEET_LOADED)) {
      pa.startTimer("InputSchema", "Validation");
      SingleTypeXMLClass it = context.getInputType(context
          .getSchemaIdentifier("", ResolutionContext.INPUT));
      pa.stopTimer("InputSchema", "Validation");
      story.setInputType(it);
      // it = null;
      pa.startTimer("ValidationFlowAnalyzer", "Validation");
      ControlFlowGraph xcfg = getCFG(systemId, ss, context, cesspool);

      if (ControlFlowConfiguration.current.generateFlowDeathReport()) {
        // WebGenerator wg = new WebGenerator(DiagnosticsConfiguration.current
        // .getGeneratedHTMLStylesheetName());
        // DiagnosticsConfiguration.current.getGeneratedHTMLPrefix()
        // wg.process(ss);
      }

      // ss = null;

      pa.stopTimer("ValidationFlowAnalyzer", "Validation");
      story.setXcfg(xcfg);
      if (xcfg != null
          && story.relaxateProgress(ValidationRun.XCFG_CONSTRUCTED)) {
        pa.startTimer("SGConstruction", "Validation");
        XMLGraph sg = getControlFlowSummaryGraph(xcfg, context);
        // xcfg = null;
        pa.stopTimer("SGConstruction", "Validation");
        if (ControlFlowConfiguration.current.runCodeAssistAlgorithms()) {
          DeathFollowerUpper.getInstance().analyze(xcfg, context, null, null);
        }

        story.setControlFlowSG(sg);
        if (story.relaxateProgress(ValidationRun.SG_CONSTRUCTED)) {
          pa.startTimer("OutputSchema", "Validation");
          XMLGraph outputType = context.getOutputType(context
              .getSchemaIdentifier("", ResolutionContext.OUTPUT));
          pa.stopTimer("OutputSchema", "Validation");
          story.setOutputType(outputType);
          pa.startTimer("SGValidation", "Validation");
          ValidationResult vres = validate(outputType, sg);
          story.setValidationResult(vres);
          pa.stopTimer("SGValidation", "Validation");
          story.relaxateProgress(ValidationRun.VALIDATED);
        }
      }
    }
    pa.stopTimer("Validation", "Root");
  }

  /**
   * Drive a ValidationRun thru validation.
   * @param systemId - System ID of principal module
   * @param context - Validation context (will provide schemas and other resources)
   * @param cesspool - where non-validation errors go.
   * @param story - Validation results go here (as do other results and reports).
   * @throws XSLToolsException
   */
  public static ValidationResult validate
  (ValidationContext context, ErrorReporter cesspool, ResultListener rl, boolean giveUpOnSemanticErrors) throws XSLToolsException {
    /*
     * If we want to tell our envoronment about resources we want to fetch, go ahead.
     * This should really be the job of the ValidationRun.
     */
    PerformanceLogger pa = DiagnosticsConfiguration.current.getPerformanceLogger();
    pa.startTimer("Validation", "Root");
    pa.startTimer("Simplification", "Validation");
    Stylesheet ss = getStylesheet(context.getStylesheetIdentifier(), context, cesspool);
    if (ss!=null) {
      rl.setSemPreservingSimplifiedStylesheet(ss);
      approximateStylesheet(ss, cesspool);
      rl.setApproxSimplifiedStylesheet(ss);
    }
    pa.stopTimer("Simplification", "Validation");
    if (ss != null && (!giveUpOnSemanticErrors || !cesspool.hasErrors())) {
      pa.startTimer("InputSchema", "Validation");
      SingleTypeXMLClass it = context.getInputType(context
          .getSchemaIdentifier("", ResolutionContext.INPUT));
      pa.stopTimer("InputSchema", "Validation");
      rl.setInputType(it);
      pa.startTimer("ValidationFlowAnalyzer", "Validation");
      ControlFlowGraph xcfg = getCFG(context.getStylesheetIdentifier(), ss, context, cesspool);
      pa.stopTimer("ValidationFlowAnalyzer", "Validation");
      rl.setXcfg(xcfg);
      if (xcfg != null && (!giveUpOnSemanticErrors || !cesspool.hasErrors())) {
        pa.startTimer("SGConstruction", "Validation");
        XMLGraph sg = getControlFlowSummaryGraph(xcfg, context);
        pa.stopTimer("SGConstruction", "Validation");
        if (ControlFlowConfiguration.current.runCodeAssistAlgorithms()) {
          DeathFollowerUpper.getInstance().analyze(xcfg, context, null, null);
        }
        rl.setControlFlowSG(sg);
          pa.startTimer("OutputSchema", "Validation");
          XMLGraph outputType = context.getOutputType(context
              .getSchemaIdentifier("", ResolutionContext.OUTPUT));
          pa.stopTimer("OutputSchema", "Validation");
          rl.setOutputType(outputType);
          pa.startTimer("SGValidation", "Validation");
          ValidationResult vres = validate(outputType, sg);
          rl.setValidationResult(vres);
          pa.stopTimer("SGValidation", "Validation");
          return vres;
      }
    }
    pa.stopTimer("Validation", "Root");
    return null;
  }

  /**
   * Same as above, except that no validation is done, only flow analysis.
   * There is really no need for this...
   * @param systemId
   * @param context
   * @param cesspool
   * @param story -- TODO: Make some superinterface of ValidationRun, or something like that.
   * @throws XSLToolsException
   */
  public static void analyzeFlow(String systemId, ValidationContext context,
      ErrorReporter cesspool, ValidationRun story) throws XSLToolsException {
    try {
      prepareValidation(context);
    } catch (IOException ex) {
      throw new XSLToolsException(ex);
    }

    PerformanceLogger pa = DiagnosticsConfiguration.current.getPerformanceLogger();

    pa.startTimer("Analysis", "Root");
    pa.startTimer("Simplification", "Analysis");
    Stylesheet ss = getStylesheet(systemId, context, cesspool);
    pa.stopTimer("Simplification", "Analysis");
    story.setApproxSimplifiedStylesheet(ss);
    if (ss != null && story.relaxateProgress(ValidationRun.STYLESHEET_LOADED)) {
      pa.startTimer("InputSchema", "Analysis");
      SingleTypeXMLClass it = context.getInputType(context.getSchemaIdentifier("", ResolutionContext.INPUT));
      pa.stopTimer("InputSchema", "Analysis");
      story.setInputType(it);
      ControlFlowGraph xcfg = getCFG(systemId, ss, context, cesspool);
      //xcfg.annotateContextInsensitiveFlows(ss, StylesheetModule.ORIGINAL);
      xcfg.annotateContextSensitiveFlows(ss, StylesheetModule.ORIGINAL);
      WebGenerator wg = new WebGenerator(DiagnosticsConfiguration.current.getGeneratedHTMLStylesheetName());
        // DiagnosticsConfiguration.current.getGeneratedHTMLPrefix()
         wg.process(ss);
    }
    pa.stopTimer("Analysis", "Root");
  }
}
