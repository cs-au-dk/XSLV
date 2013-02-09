package dongfang.xsltools.experimental.progresslogging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


public class ProgressLogger {
	private static String LOGDIR = System
			.getProperty("dongfang.xsltools.progresslogdir");
	static {
		if (LOGDIR == null)
		LOGDIR = System.getProperty("java.io.tmpdir", "/tmp");
	}

	private static ThreadLocal<Logger> sessionLogger = new ThreadLocal<Logger>();
	// private static Set<Logger> alreadyIssutedLoggers = new HashSet<Logger>();
	
	/*
	 * Get a session specific logger, appending to 2 files: Coarse and detailed.
	 * An attempt is made to avoid overwriting old files when session names are
	 * reused.
	 */
	public static synchronized Logger getLogger(String sessionName) {
		Logger logger = Logger.getLogger("progress-" + sessionName);
		// We can recognize already configured loggers on this feature.
	
		if (!logger.getUseParentHandlers())
			return logger;
		logger.setUseParentHandlers(false);

		// if (alreadyIssutedLoggers.contains(logger)) return logger;
		// alreadyIssutedLoggers.add(logger);
		
		logger.setLevel(Level.ALL);
		String dateString = new Date().toString();
		dateString = dateString.replace(':', '_');
		dateString = dateString.replace(' ', '_');

		Formatter fmt = new ProgressLoggerFormatter();

		try {
			// Make the stream an appending one
			OutputStream summaryStream = new FileOutputStream(LOGDIR
					+ File.separatorChar + sessionName + "." + dateString + ".summary.log", true);
			StreamHandler summaryHandler = new StreamHandler(summaryStream, fmt);
			summaryHandler.setLevel(Level.INFO);

			// Make the stream an appending one
			OutputStream detailStream = new FileOutputStream(LOGDIR
					+ File.separatorChar + sessionName + "." + dateString + ".detail.log", true);
			StreamHandler detailHandler = new StreamHandler(detailStream, fmt);
			detailHandler.setLevel(Level.ALL);
			
			logger.addHandler(summaryHandler);
			logger.addHandler(detailHandler);
			
			// TODO: We need to take care to close the files!!
			// TODO: And we need to avoid collisions.
		} catch (IOException ex) {
			System.err.println(ex);
		}
		
		return logger;
	}

	public static void setThreadLocal(Logger logger) {
		sessionLogger.set(logger);
	}
	
	public static synchronized Logger getThreadLocalLogger() {
		Logger l = sessionLogger.get();
		if (l==null) {
			l = Logger.getLogger("NoSessionLoggerAvailable");
		}
		return l;
	}
	
	/*
	 * Take care to close the logger's output streams (so output is flushed and resources are released).
	 * We want to have this foolproof: Can be done any number of times. A handed-back logger can be
	 * reused without loss of function.
	 */
	public static synchronized void handback(Logger logger) {
		Handler[] handlers = logger.getHandlers();
		if (logger.getUseParentHandlers()) { // !alreadyIssutedLoggers.contains(logger)|| handlers.length!= 2) {
			// not one of ours.
			return;
		}
		// Mark logger as not managed by us... This will make the logger get re-inited in above getLogger method.
		logger.setUseParentHandlers(true);
		// alreadyIssutedLoggers.remove(logger);
		StreamHandler h = (StreamHandler)handlers[0];
		h.close();
		h = (StreamHandler)handlers[1];
		h.close();
	}
}
