package dongfang.xsltools.exceptions;

public class XSLToolsRuntimeException extends RuntimeException {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1694433414099267319L;

public XSLToolsRuntimeException() {
    super();
  }

  public XSLToolsRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public XSLToolsRuntimeException(String message) {
    super(message);
  }

  public XSLToolsRuntimeException(Throwable cause) {
    super(cause);
  }
}
