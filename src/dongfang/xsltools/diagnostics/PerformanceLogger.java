package dongfang.xsltools.diagnostics;

/**
 * Analyzer is almost too much of a name; all we really do is measure some times and dump them.
*/
public interface PerformanceLogger {

  /**
   * Start a timer.
   * @param processName - name of the timer
   * @param parentProcessName - name of the parent timer. The parent timer
   * will make a breakdown of the timing results of all its child timers.
   */
  void startTimer(String processName, String parentProcessName);

  /**
   * Stop a timer.
   * @param processName - name of the timer
   * @param parentProcessName - name of the parent timer. The parent timer
   * will make a breakdown of the timing results of all its child timers.
   */
  void stopTimer(String processName, String parentProcessName);

  /**
   * Increment a counter by 1.
   * @param itemName - name of the counter
   * @param parentItemName - name of the parent counter. The parent counter
   * will make a breakdown of the count results of all its child counters.
   */
  void incrementCounter(String itemName, String parentItemName);

  /**
   * Increment a counter.
   * @param itemName - name of the counter
   * @param parentItemName - name of the parent counter. The parent counter
   * will make a breakdown of the count results of all its child counters.
   */
  void incrementCounter(String itemName, String parentItemName, int increment);

  /**
   * Set a counter to a value.
   * @param itemName - name of the counter
   * @param parentItemName - name of the parent counter. The parent counter
   * will make a breakdown of the count results of all its child counters.
   */
  void setValue(String itemName, String parentItemName, int value);

  /**
   * Reset the timers.
   *
   */
  void resetTimers();

  /**
   * Reset the counters.
   *
   */
  void resetCounters();

  /**
   * Get a document with the timer status
   * @return
   */
  String getTimerStats();

  /**
   * Get a document with the counter status
   * @return
   */
  String getCounterStats();
}
