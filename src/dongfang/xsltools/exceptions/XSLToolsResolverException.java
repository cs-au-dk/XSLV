/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.exceptions;

import org.dom4j.QName;

/**
 * @author dongfang
 */
public class XSLToolsResolverException extends XSLToolsException {
  /**
	 * 
	 */
	private static final long serialVersionUID = -3471358933026335772L;

QName qname;

  short symbolSpace;

  public XSLToolsResolverException(QName qname, short symbolSpace) {
    this.qname = qname;
    this.symbolSpace = symbolSpace;
  }

  /**
   * 
   */
  public XSLToolsResolverException() {
    super();
  }

  /**
   * @param message
   */
  public XSLToolsResolverException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public XSLToolsResolverException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause
   */
  public XSLToolsResolverException(Throwable cause) {
    super(cause);
  }
}