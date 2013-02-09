/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-16
 */
package dongfang.xslt;

import dongfang.xsltools.exceptions.XSL1ErrorException;
import dongfang.xsltools.exceptions.XSLErrorException;

/**
 * @author dongfang
 */
public class XSLT1 extends XSLT {
  private static final XSLT instance = new XSLT1();

  public static XSLT getInstance() {
    return instance;
  }

  @Override
public void localVariablesOrParametersNameClash() throws XSLErrorException {
    throw new XSL1ErrorException(
        "XSL Error: Local parameter or value shadowed local parameter or value",
        11.5f);
  }

  @Override
public void toplevelVariablesOrParametersNameClash() throws XSLErrorException {
    throw new XSL1ErrorException(
        "XSL Error: It is an error if a stylesheet contains more than one binding ofa top-level variable with the same name and same import precedence",
        11.4f);
  }

  @Override
public void ambigousVariableSuppliedValue() throws XSLErrorException {
    throw new XSLErrorException("Todo: Section");
  }
}