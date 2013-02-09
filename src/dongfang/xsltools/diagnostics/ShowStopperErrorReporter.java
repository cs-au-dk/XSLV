/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.diagnostics;

import org.dom4j.Element;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLocatableException;
import dongfang.xsltools.model.StylesheetModule;

/**
 * @author dongfang
 */
public class ShowStopperErrorReporter implements ErrorReporter {
  public void reportError(XSLToolsException e) throws XSLToolsException {
    throw e;
  }

  public void reportError(StylesheetModule module, Element source,
      ParseLocation.Extent extent, String message) throws XSLToolsException {
    throw new XSLToolsLocatableException(module.getSystemId(), source, extent,
        message);
  }

  public void reportError(StylesheetModule module, Element source,
      ParseLocation.Extent extent, Throwable cause) throws XSLToolsException {
    throw new XSLToolsLocatableException(module.getSystemId(), source, extent,
        cause);
  }

  public void reportError(String systemId, int line, int col, String message)
      throws XSLToolsException {
    throw new XSLToolsLocatableException(systemId, line, col, message);
  }

  public void reportError(String systemId, int line, int col, Throwable cause)
      throws XSLToolsException {
    throw new XSLToolsLocatableException(systemId, line, col, cause);
  }

  public boolean hasErrors() {
    return false;
  }

  public void reset() {
  }
}