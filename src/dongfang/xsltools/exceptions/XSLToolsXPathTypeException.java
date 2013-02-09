/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-13
 */
package dongfang.xsltools.exceptions;

/**
 * @author dongfang
 */
public class XSLToolsXPathTypeException extends XSLToolsXPathException {

  /**
	 * 
	 */
	private static final long serialVersionUID = 371428022533346698L;

/**
   * 
   */
  public XSLToolsXPathTypeException() {
    super();
  }

  /**
   * @param message
   */
  public XSLToolsXPathTypeException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public XSLToolsXPathTypeException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public XSLToolsXPathTypeException(Throwable cause) {
    super(cause);
  }
}