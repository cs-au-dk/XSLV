package dongfang.xsltools.validation;

import java.io.IOException;
import java.io.PrintStream;

import dongfang.xsltools.diagnostics.PerformanceLogger;

/**
 * A ValidationRun that dumps only the performance analyzer into a file.
 * @author dongfang
 */

public class LightValidationRunDumper extends ValidationRunDecoratorBase {

  String prefix;

  /**
   * @param decorated
   * @param prefix -- the file names will be <code>prefix</code>.performance-timers.xml" and
   * <code>prefix</code>.performance-counters.xml
   */
  public LightValidationRunDumper(ValidationRun decorated, String prefix) {
    super(decorated);
    this.prefix = prefix;
  }

  static void dumpPerformanceAnalyzer(PerformanceLogger pa, String prefix) {
    try {
      PrintStream ps = new PrintStream(prefix + ".performance-timers.xml");
      ps.print(pa.getTimerStats());
      ps.close();

      ps = new PrintStream(prefix + ".performance-counters.xml");
      ps.print(pa.getCounterStats());
      ps.close();

    } catch (IOException ex) {
      System.err.println("Verdammt, IOException " + ex);
    }
  }

  @Override
public void setPerformanceLogger(PerformanceLogger pa) {
    super.setPerformanceLogger(pa);
    dumpPerformanceAnalyzer(pa, prefix);
  }
}
