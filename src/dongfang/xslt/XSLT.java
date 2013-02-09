/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-16
 */
package dongfang.xslt;

import dongfang.xsltools.exceptions.XSLErrorException;

/**
 * @author dongfang
 */
public class XSLT {
  private static double version = 1.0;

  public static XSLT getInstance() {
    if (version == 1.0)
      return XSLT1.getInstance();
    return XSLT2.getInstance();
  }

  public void localVariablesOrParametersNameClash() throws XSLErrorException {
  }

  public void toplevelVariablesOrParametersNameClash() throws XSLErrorException {
  }

  public void ambigousVariableSuppliedValue() throws XSLErrorException {
  }
}