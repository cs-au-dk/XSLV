/*
 * dongfang M. Sc. Thesis
 * Created on 2005-02-27
 */
package dongfang.xsltools.simplification;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;

/**
 * General representation of a functional stylesheet processor.
 * 
 * @author dongfang
 */
public interface StylesheetProcessor {
  /**
   * Simplify a <code>Stylesheet</code>
   * 
   * @param stylesheet
   * @param cesspool
   * @throws XSLSimplificationException
   */
  void process(Stylesheet stylesheet) throws XSLToolsException;

  /**
   * Experiment: The default rules in the stylesheet proper should not need to
   * be re-simplified. Wonder if it works ? :)
   */
  boolean simplifyStylesheetProper = false;
}