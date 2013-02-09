/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.exceptions;

/**
 * @author dongfang
 */
public class XSLToolsWebException extends XSLToolsException {

  /**
	 * 
	 */
	private static final long serialVersionUID = -6342641391296141406L;

/**
   * 
   */
  public XSLToolsWebException() {
    super();
  }

  /**
   * @param message
   */
  public XSLToolsWebException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public XSLToolsWebException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public XSLToolsWebException(Throwable cause) {
    super(cause);
  }
}
