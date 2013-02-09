/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-21
 */
package dongfang.xsltools.simplification;

/**
 * A CompositeProcessor that does performance timing as an aside.
 * 
 * @author dongfang
 */
/*
 * public class TimingCompositeProcessor extends CompositeProcessor { private
 * int noRuns = 1;
 * 
 * protected static TimingCompositeProcessor getInstance(ErrorReporter cesspool,
 * UniqueNameGenerator names) { return new TimingCompositeProcessor(cesspool,
 * names); }
 * 
 * public TimingCompositeProcessor(ErrorReporter cesspool, UniqueNameGenerator
 * names) { super(cesspool, names); }
 * 
 * protected void process(StylesheetProcessor proc, Stylesheet stylesheet)
 * throws XSLToolsException { PerformanceAnalyzer pa =
 * DiagnosticsConfiguration.current .getPerformanceAnalyzer();
 * 
 * if (!(proc instanceof CompositeProcessor))
 * pa.startTimer("SimplificationProper", proc.getClass().getSimpleName());
 * super.process(proc, stylesheet); if (!(proc instanceof CompositeProcessor))
 * pa.stopTimer("SimplificationProper", proc.getClass().getSimpleName()); //
 * String key = proc.getClass().getSimpleName(); }
 * 
 * public void process(Stylesheet stylesheet) throws XSLToolsException { for
 * (int i = 0; i < noRuns - 1; i++) { super.process((Stylesheet)
 * stylesheet.clone()); } super.process(stylesheet); } }
 */