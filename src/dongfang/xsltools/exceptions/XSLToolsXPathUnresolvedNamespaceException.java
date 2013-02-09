/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.exceptions;

/**
 * @author dongfang
 */
public class XSLToolsXPathUnresolvedNamespaceException extends
    XSLToolsXPathException {

  /**
	 * 
	 */
	private static final long serialVersionUID = 8885768709202710820L;

/**
   * 
   */
  public XSLToolsXPathUnresolvedNamespaceException() {
    super();
  }

  /**
   * @param message
   */
  public XSLToolsXPathUnresolvedNamespaceException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public XSLToolsXPathUnresolvedNamespaceException(String message,
      Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public XSLToolsXPathUnresolvedNamespaceException(Throwable cause) {
    super(cause);
  }
}