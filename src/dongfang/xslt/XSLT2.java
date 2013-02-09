/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-16
 */
package dongfang.xslt;

import dongfang.xsltools.exceptions.XSL2ErrorException;
import dongfang.xsltools.exceptions.XSLErrorException;

/**
 * @author dongfang
 */
public class XSLT2 extends XSLT {

  private static final XSLT instance = new XSLT2();

  public static final XSLT getInstance() {
    return instance;
  }

  @Override
public void localVariablesOrParametersNameClash() throws XSLErrorException {
    // no problem -- see section 9.7 of Recommendation
  }

  @Override
public void ambigousVariableSuppliedValue() throws XSLErrorException {
    throw new XSL2ErrorException(ERR_XT0620);
  }

  public static String ERR_XT0620 = "It is a static error if a variable-binding element has a select attribute and has non-empty contents";

  public static String ERR_XT0630 = "It is a static error if a stylesheet contains more than one binding for a global variable with the same name and same import precedence, unless it also contains another binding with the same name and higher import precedence";
}