/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.diagnostics;

import org.dom4j.Element;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.StylesheetModule;

/**
 * Alternative candidate for naming of this class: CrapEater.
 * 
 * @author dongfang
 */
public interface ErrorReporter {
  void reportError(XSLToolsException e) throws XSLToolsException;

  // This is generally not possible, as bindings will be static ---
  // if we write ... } catch (XSLToolsException ex) {cesspool.reportError(ex)}
  // the variant typed XSLToolsException will be called.
  // void reportError(XSLToolsLocalizableException e)
  // throws XSLToolsLocalizableException;

  void reportError(StylesheetModule module, Element source,
      ParseLocation.Extent extent, String message) throws XSLToolsException;

  void reportError(StylesheetModule module, Element source,
      ParseLocation.Extent extent, Throwable cause) throws XSLToolsException;

  void reportError(String systemId, int line, int col, String message)
      throws XSLToolsException;

  void reportError(String systemId, int line, int col, Throwable cause)
      throws XSLToolsException;

  boolean hasErrors();

  void reset();
}