package dongfang.xsltools.controlflow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;

public class CompositeFlowAnalyzer implements FlowAnalyzer {
  protected List<FlowAnalyzer> analyzers = new ArrayList<FlowAnalyzer>();

  /**
   * Add a FlowAnalyzer to the left hand side of the composite FlowAnalyzer
   * function (so the new analyzer is run on the output of the previous one
   * added etc.)
   * 
   * @param clazz -
   *          a FlowAnalyzer class object
   */
  public void addAnalyzer(Class<? extends FlowAnalyzer> clazz) {
    analyzers.add(instantiate(clazz));
  }

  /**
   * Add a FlowAnalyzer to the left hand side of the composite FlowAnalyzer
   * function (so the new analyzer is run on the output of the previous one
   * added etc.)
   * 
   * @param processor -
   *          a FlowAnalyzer object
   */
  public void addAnalyzer(FlowAnalyzer analyzer) {
    analyzers.add(analyzer);
  }

  /**
   * Run the processor on the stylesheet. Useful for overriding in subclasses
   * (add timers, dumping code etc)
   * 
   * @param proc
   * @param stylesheet
   * @throws XSLToolsException
   */
  protected void analyze(FlowAnalyzer analyzer, /*List<TemplateRule> liveRules,*/
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode bootstrapMode) throws XSLToolsException {
    analyzer.analyze(/*liveRules,*/xcfg, validationContext, bootstrapTemplateRule,
        bootstrapMode);
  }

  protected FlowAnalyzer instantiate(Class<? extends FlowAnalyzer> clazz) {
    try {
      Method getInstance = clazz.getDeclaredMethod("getInstance",
          new Class[] {});
      return (FlowAnalyzer) getInstance.invoke(null, new Object[] {});
    } catch (NoSuchMethodException ex) {
      throw new AssertionError("reflection add FlowAnalyzer: " + ex.toString());
    } catch (InvocationTargetException ex) {
      throw new AssertionError("reflection add FlowAnalyzer: " + ex.toString()
          + " " + clazz);
    } catch (IllegalAccessException ex) {
      throw new AssertionError("reflection add FlowAnalyzer: " + ex.toString());
    }
  }

  /**
   * Analyze.
   */
  public void analyze(
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode bootstrapMode) throws XSLToolsException {

    /*
     * Don't charge one particular analyzer the cost of loading the input
     * schema; prepare it here. Remove if not using input schema.
    validationContext.getInputType(validationContext
        .getSchemaIdentifier("", ValidationContext.INPUT));
     */

    for (FlowAnalyzer analyzer : analyzers) {
      analyze(analyzer, xcfg, validationContext, bootstrapTemplateRule,
          bootstrapMode);
    }
  }

  /**
   * First get the xcfg, then analyze.
   */
  public ControlFlowGraph analyze(Stylesheet stylesheet,
      ValidationContext validationContext, ErrorReporter cesspool)
      throws XSLToolsException {

    ControlFlowGraph result = new ControlFlowGraph(validationContext,
        stylesheet, cesspool);

    TemplateRule bootstrap = TemplateRule.getBootstrapTemplateRule();
    ContextMode bootstrapMode = DefaultTemplateMode.instance;

    analyze(result, validationContext, bootstrap, bootstrapMode);
    result.setRootSelection((bootstrap.applies.get(0)).selections.get(0));
    return result;
  }
}
