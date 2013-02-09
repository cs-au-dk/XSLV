/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * A dynamic composite Simplifier, that executes the composite simplification
 * function of all the Simplifiers added (in order).
 * 
 * @author dongfang
 */
public class CompositeProcessor implements StylesheetProcessor {
  // protected List<Class<? extends StylesheetProcessor>> simplifiers =
  // new ArrayList<Class<? extends StylesheetProcessor>>();
  // new ArrayList<Class<StylesheetProcessor>>();
  protected List<StylesheetProcessor> processors = new ArrayList<StylesheetProcessor>();

  private ErrorReporter cesspool;

  private UniqueNameGenerator names;

  protected static CompositeProcessor getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new CompositeProcessor(cesspool, names);
  }

  protected CompositeProcessor(ErrorReporter cesspool, UniqueNameGenerator names) {
    this.cesspool = cesspool;
    this.names = names;
  }

  /**
   * Add a Simplifier to the left hand side of the composite Simplifier function
   * (so the new simplifier is run on the output of the previous one added etc.)
   * 
   * @param clazz -
   *          a StylesheetProcessor class object
   */
  public void addSimplifier(Class<? extends StylesheetProcessor> clazz) {
    processors.add(instantiate(clazz));
  }

  /**
   * Add a Simplifier to the left hand side of the composite Simplifier function
   * (so the new simplifier is run on the output of the previous one added etc.)
   * 
   * @param processor -
   *          a StylesheetProcessor object
   */
  public void addSimplifier(StylesheetProcessor processor) {
    processors.add(processor);
  }

  /**
   * Run the processor on the stylesheet. Useful for overriding in subclasses
   * (add timers, dumping code etc)
   * 
   * @param proc
   * @param stylesheet
   * @throws XSLToolsException
   */
  protected void process(StylesheetProcessor proc, Stylesheet stylesheet)
      throws XSLToolsException {
    PerformanceLogger pa = DiagnosticsConfiguration.current
        .getPerformanceLogger();
    boolean skipTimers = proc instanceof DefaultSimplifier
        || proc instanceof SemanticsPreservingSimplifier;
    if (!skipTimers)
      pa.startTimer(proc.getClass().getSimpleName(), "SimplificationProper");
    proc.process(stylesheet);
    if (!skipTimers)
      pa.stopTimer(proc.getClass().getSimpleName(), "SimplificationProper");
  }

  protected StylesheetProcessor instantiate(
      Class<? extends StylesheetProcessor> clazz) {
    try {
      Method getInstance = clazz.getDeclaredMethod("getInstance", new Class[] {
          ErrorReporter.class, UniqueNameGenerator.class });
      return (StylesheetProcessor) getInstance.invoke(null, new Object[] {
          cesspool, names });
    } catch (NoSuchMethodException ex) {
      throw new AssertionError("reflection add Simplifier: " + ex.toString());
    } catch (InvocationTargetException ex) {
      throw new AssertionError("reflection add Simplifier: " + ex.toString()
          + " " + clazz);
    } catch (IllegalAccessException ex) {
      throw new AssertionError("reflection add Simplifier: " + ex.toString());
    }
  }

  /**
   * Subclasses may override this to do their internal stuff prior to
   * processing.
   */
  public void beforeProcessing() {
  }

  /**
   * Subclasses may override this to do their internal stuff after processing.
   */
  public void afterProcessing() {
  }

  public void process(Stylesheet stylesheet) throws XSLToolsException {
    beforeProcessing();
    for (StylesheetProcessor processor : processors) {
      process(processor, stylesheet);
    }
    afterProcessing();
  }
}
