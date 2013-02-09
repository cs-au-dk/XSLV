/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.exceptions;

/**
 * @author dongfang
 */
public class XSLToolsXPathParseException extends XSLToolsXPathException {

  /**
	 * 
	 */
	private static final long serialVersionUID = -4669914640108566836L;

/**
   * 
   */
  public XSLToolsXPathParseException() {
    super();
  }

  /**
   * @param message
   */
  public XSLToolsXPathParseException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public XSLToolsXPathParseException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public XSLToolsXPathParseException(Throwable cause) {
    super(cause);
  }
}