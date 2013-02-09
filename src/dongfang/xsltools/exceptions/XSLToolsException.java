/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.exceptions;

/**
 * @author dongfang
 */
public class XSLToolsException extends Exception {

  /**
	 * 
	 */
	private static final long serialVersionUID = 2760363114646250441L;

/**
   * 
   */
  public XSLToolsException() {
    super();
  }

  /**
   * @param message
   */
  public XSLToolsException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public XSLToolsException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public XSLToolsException(Throwable cause) {
    super(cause);
  }
}