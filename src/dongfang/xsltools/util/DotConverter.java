package dongfang.xsltools.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.logging.Logger;

import dongfang.xsltools.experimental.progresslogging.ProgressLogger;

public class DotConverter {

  /*
   * The max. time the servlet thread should wait for our manager thread 
   */
  private static final int INPUT_STAGE_PATIENCE = 30000;

  /*
   * The max. time it should take to finish the dot converter with data (until all is accepted)
   */
  private static final int TOTAL_PROCESS_PATIENCE = 60000; 
  
  /*
   * Where is dot executable; where will it work 
   */
  private static String DOT_EXECUTABLE_NAME = System.getProperty("dongfang.xsltools.dot.exe");
  static {
    if (DOT_EXECUTABLE_NAME == null)
      DOT_EXECUTABLE_NAME = "/usr/bin/dot";
  }
  
  private static String DOT_WORK_DIR = System.getProperty("dongfang.xsltools.dot.work");
  static {
    if (DOT_WORK_DIR == null)
    	DOT_WORK_DIR = System.getProperty("java.io.tmpdir", "/tmp");
  }
  
  // private static final Logger logger = Logger.getLogger(DotConverter.class.getSimpleName());

  private PipedOutputStream post = new PipedOutputStream();

  private Thread manager;
  
  private boolean success;

  // for diags / debugging, nothing else.
  private boolean feederIsRunning;
  
  // for diags / debugging, nothing else.
  private boolean eaterIsRunning;
  
  public OutputStream getOutputStream() {
    return post;
  }

  public void startGraphvizProcess(final OutputStream to, String format) throws IOException {
	final Logger logger = ProgressLogger.getThreadLocalLogger();
	  
    ProcessBuilder pb = new ProcessBuilder(new String[] { DOT_EXECUTABLE_NAME, "-T" + format.toLowerCase() });
    pb.directory(new File(DOT_WORK_DIR));

    final PipedInputStream pist = new PipedInputStream(post);

    final Process proc = pb.start();

    final InputStream pris = proc.getInputStream();
    final InputStream prie = proc.getErrorStream();
    final OutputStream pros = proc.getOutputStream();

    final Thread feeder = new Thread() {
      @Override
	public void run() {
        feederIsRunning = true;
        try {
          int ptr;
          byte[] buf = new byte[1024];
          while ((ptr = pist.read(buf)) > 0) {
            pros.write(buf, 0, ptr);
          }
        } catch (IOException ex) {
          logger.warning("IO from us to DOT process threw an excption: " + ex);
          //System.err.println("IO from us to DOT process threw an excption: " + ex);
        } finally {
          try {
            pros.close();
          } catch (IOException ex2) {
            logger.warning("Closing IO from us to DOT process threw an excption: " + ex2);
            //System.err.println("Closing IO from us to DOT process threw an excption: " + ex2);
          } finally {
            feederIsRunning = false;
          }
        }
      }
    };

    final Thread eater = new Thread() {
      @Override
	public void run() {
        eaterIsRunning = true;
        try {
          int ptr;
          byte[] buf = new byte[1024];
          while ((ptr = pris.read(buf)) > 0) {
            to.write(buf, 0, ptr);
          }
          to.flush();
        } catch (IOException ex) {
          logger.warning("IO from DOT process to us threw an excption: " + ex);
          //System.err.println("IO from DOT process to us threw an excption: " + ex);
        } finally {
          eaterIsRunning = false;
        }
      }
    };

    final Thread error = new Thread() {
      @Override
	public void run() {
        try {
          int ptr;
          byte[] buf = new byte[1024];
          while ((ptr = prie.read(buf)) > 0) {
            String msg = new String(buf, 0, ptr);
            logger.warning("DOT spewed out an error message: " + msg);
            //System.err.println("DOT spewed out an error message: " + msg);
          }          
        } catch (IOException ex) {
          logger.warning("stderr watcher threw an IO excption: " + ex);
          //System.err.println("stderr wathcer threw an IO excption: " + ex);
        }
      }
    };

    error.setDaemon(true);
    eater.setDaemon(true);
    feeder.setDaemon(true);

    error.start();
    eater.start();
    feeder.start();

    manager = new Thread() {
      @Override
	public void run() {
        try {
          // yield();
          // Before waiting for feeder thread to die, give it a chance to start. Otherwise, it might 
          // (apparently; debugging experience) be considered dead before it even started.
          sleep(500);
          long time = System.currentTimeMillis();
          feeder.join(INPUT_STAGE_PATIENCE);
          // Feeder is done; wait for process to terminate iff feeder did not take too long.
          if (System.currentTimeMillis() - time < INPUT_STAGE_PATIENCE + 1000) {
            eater.join(TOTAL_PROCESS_PATIENCE -  (System.currentTimeMillis() - time));
            // We do not wait for the process to finish -- it might be hung / take too long.
            // proc.waitFor();
          }
        } catch (InterruptedException ex) {
          logger.info("We had an InterruptedException");
          //System.err.println("We had an InterruptedException");
        }
        try {
          int exit = proc.exitValue();
          if (exit == 0) {
            logger.info("DOT terminated OK");
            //System.err.println("DOT terminated OK");
            success = true;
          }
          else {
            logger.warning("DOT termintated with exit code: " + exit);
            //System.err.println("DOT termintated with exit code: " + exit);
            success = false;
          }
        } catch (IllegalThreadStateException ex2) {
          // System.err.println("IllegalThreadStateException is: " + ex2);
          logger.warning("DOT had not terminated as we lost patience with it; killing it!");
          // System.err.println("DOT had not terminated as we lost patience with it; killing it!");
          proc.destroy();
          success = false;
        }
        finally {
          logger.info("Feeder still running: " + feederIsRunning);
          logger.info("Eater still running: " + eaterIsRunning);
        }
      }
    };
    
    manager.setDaemon(true);
    manager.start();
    
    logger.info("Started a DOT converter.");
    //System.err.println("Started a DOT converter.");
  }
  
  public boolean waitToComplete() {
	final Logger logger = ProgressLogger.getThreadLocalLogger();
    try {
      // Let manager terminate.
      manager.join(TOTAL_PROCESS_PATIENCE + 2000);
    } catch (InterruptedException ex) {
      logger.info("We had an InterruptedException");
      //System.err.println("We had an InterruptedException");
    } finally {
      return success;
    }
  }
}
