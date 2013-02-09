/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.diagnostics;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.dom4j.Element;

import dongfang.xsltools.diagnostics.ParseLocation.Extent;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.experimental.progresslogging.NullFormatter;
import dongfang.xsltools.model.StylesheetModule;

/**
 * @author dongfang
 */
public class LoggingErrorReporter implements ErrorReporter {
  private Logger logger = Logger.getLogger("xslv.errors");

  private boolean hadErrors;

  public LoggingErrorReporter(String filename) throws IOException {
    Handler handler = new FileHandler(filename);
    handler.setFormatter(new NullFormatter());
    logger.addHandler(handler);
  }

  public void reportError(XSLToolsException e) {
    hadErrors = true;
    logger.severe(e.toString());
    StackTraceElement[] st = e.getStackTrace();
    for (int i = 0; i < st.length; i++) {
      logger.info(st[i].toString());
    }
  }

  public void reportError(StylesheetModule module, Element source,
      Extent extent, String message) throws XSLToolsException {
    hadErrors = true;
    logger.severe("In module: " + module + " at element " + source + ": "
        + message);
  }

  public void reportError(StylesheetModule module, Element source,
      Extent extent, Throwable cause) throws XSLToolsException {
    hadErrors = true;
    logger.severe("In module: " + module + " at element " + source + ": "
        + cause);
  }

  public void reportError(String systemId, int line, int col, String message)
      throws XSLToolsException {
    hadErrors = true;
    logger.severe("In module: " + systemId + " at line " + line + ", col "
        + col + ": " + message);
  }

  public void reportError(String systemId, int line, int col, Throwable cause)
      throws XSLToolsException {
    hadErrors = true;
    logger.severe("In module: " + systemId + " at line " + line + ", col "
        + col + ": " + cause);
  }

  public boolean hasErrors() {
    return hadErrors;
  }

  public void reset() {
    hadErrors = false;
  }
}