/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.diagnostics;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLocatableException;
import dongfang.xsltools.model.StylesheetModule;

/**
 * @author dongfang
 */
public class MemoryErrorReporter extends ShowStopperErrorReporter {

  private List<XSLToolsException> errors = new LinkedList<XSLToolsException>();

  @Override
public void reportError(XSLToolsException e) {
    errors.add(e);
  }

  @Override
public void reportError(StylesheetModule module, Element source,
      ParseLocation.Extent extent, String message) {
    try {
      super.reportError(module, source, extent, message);
    } catch (XSLToolsException ex) {
      errors.add(ex);
    }
  }

  @Override
public void reportError(StylesheetModule module, Element source,
      ParseLocation.Extent extent, Throwable cause) {
    try {
      super.reportError(module, source, extent, cause);
    } catch (XSLToolsException ex) {
      errors.add(ex);
    }
  }

  @Override
  public void reportError(String systemId, int line, int col, String message)
      throws XSLToolsException {
    try {
      super.reportError(systemId, line, col, message);
    } catch (XSLToolsException ex) {
      errors.add(ex);
    }
  }

  @Override
  public void reportError(String systemId, int line, int col, Throwable cause)
      throws XSLToolsException {
    try {
      super.reportError(systemId, line, col, cause);
    } catch (XSLToolsException ex) {
      errors.add(ex);
    }
  }

  public Iterator<XSLToolsException> errorIterator() {
    return errors.iterator();
  }

  public List<XSLToolsException> getErrors() {
    return new LinkedList<XSLToolsException>(errors);
  }

  public List<XSLToolsLocatableException> getLocatableErrors() {
    List<XSLToolsLocatableException> result = new LinkedList<XSLToolsLocatableException>();
    for (XSLToolsException ex : getErrors()) {
      if (ex instanceof XSLToolsLocatableException) {
        result.add((XSLToolsLocatableException) ex);
      }
    }
    return result;
  }

  public List<XSLToolsException> getNonLocatableErrors() {
    List<XSLToolsException> result = new LinkedList<XSLToolsException>();
    for (XSLToolsException ex : getErrors()) {
      if (!(ex instanceof XSLToolsLocatableException)) {
        result.add(ex);
      }
    }
    return result;
  }

  @Override
public boolean hasErrors() {
    return !errors.isEmpty();
  }

  @Override
public void reset() {
    errors.clear();
  }
}
