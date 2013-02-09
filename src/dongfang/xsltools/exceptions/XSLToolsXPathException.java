/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.exceptions;

/**
 * @author dongfang
 */
public class XSLToolsXPathException extends XSLToolsException {

  /**
	 * 
	 */
	private static final long serialVersionUID = -4220206620281225063L;

/**
   * 
   */
  public XSLToolsXPathException() {
    super();
  }

  /**
   * @param message
   */
  public XSLToolsXPathException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public XSLToolsXPathException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public XSLToolsXPathException(Throwable cause) {
    super(cause);
  }
}