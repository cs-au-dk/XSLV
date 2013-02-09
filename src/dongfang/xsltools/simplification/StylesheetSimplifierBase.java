/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;

/**
 * This class supports simplification of <code>Stylesheet</code>s, by
 * applying the simplification defined in a subclass to the principal group of
 * the <code>Stylesheet</code>.
 * 
 * @author dongfang
 */
public abstract class StylesheetSimplifierBase implements StylesheetProcessor {

  protected abstract void process(StylesheetLevel group)
      throws XSLToolsException;

  public void process(Stylesheet stylesheet) throws XSLToolsException {
    process(stylesheet.getPrincipalLevel());
  }
}