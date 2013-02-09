package dongfang.xsltools.diagnostics;

public class DummyPerformanceLogger implements PerformanceLogger {

  public void incrementCounter(String itemName, String parentItemName) {
  }

  public void incrementCounter(String itemName, String parentItemName,
      int increment) {
  }

  public void setValue(String itemName, String parentItemName, int value) {
  }

  public void resetTimers() {
  }

  public void resetCounters() {
  }

  public void startTimer(String processName, String parentProcessName) {
  }

  public void stopTimer(String processName, String parentProcessName) {
  }

  public String getTimerStats() {
    return "Dummy logger in use -- no stats available";
  }

  public String getCounterStats() {
    return "Dummy logger in use -- no stats available";
  }
}
