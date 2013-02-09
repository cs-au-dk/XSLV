package dongfang.xsltools.diagnostics;

import java.util.logging.Logger;

import dongfang.xsltools.experimental.progresslogging.ProgressLogger;

public class ProgressDiagnosticsPerformanceLogger implements PerformanceLogger {
	
	Logger logger = ProgressLogger.getThreadLocalLogger();

	public void startTimer(String processName, String parentProcessName) {
		logger.fine(parentProcessName + "/" + processName + ": start");
	}

	public void stopTimer(String processName, String parentProcessName) {
		logger.fine(parentProcessName + "/" + processName + ": end");
	}

	public void incrementCounter(String itemName, String parentItemName) {
	}

	public void incrementCounter(String itemName, String parentItemName,
			int increment) {
	}

	public void setValue(String itemName, String parentItemName, int value) {
		logger.fine("setValue: " + parentItemName + "/" + itemName + " is: " + value);
	}

	public void resetTimers() {
	}

	public void resetCounters() {
	}

	public String getTimerStats() {
		return "This is a dummy PerformanceLogger";
	}

	public String getCounterStats() {
		return "This is a dummy PerformanceLogger";
	}
}
