/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.exceptions;

/**
 * @author dongfang
 */
public class XSLToolsMalformedXMLException extends XSLToolsException {

  /**
	 * 
	 */
	private static final long serialVersionUID = 162246228034508711L;

/**
   * 
   */
  public XSLToolsMalformedXMLException() {
    super();
  }

  /**
   * @param message
   */
  public XSLToolsMalformedXMLException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public XSLToolsMalformedXMLException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public XSLToolsMalformedXMLException(Throwable cause) {
    super(cause);
  }
}