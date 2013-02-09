/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.exceptions;

/**
 * @author dongfang
 */
public class XSLToolsLoadException extends XSLToolsException {

  /**
	 * 
	 */
	private static final long serialVersionUID = -8666860566243130657L;

/**
   * 
   */
  public XSLToolsLoadException() {
    super();
  }

  /**
   * @param message
   */
  public XSLToolsLoadException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public XSLToolsLoadException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public XSLToolsLoadException(Throwable cause) {
    super(cause);
  }
}