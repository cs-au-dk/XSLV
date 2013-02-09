/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-16
 */
package dongfang.xsltools.exceptions;

/**
 * @author dongfang
 */
public class XSL2ErrorException extends XSLErrorException {

  /**
	 * 
	 */
	private static final long serialVersionUID = -2470796987598970256L;

/**
   * @param XSLRecommendationErrorNumber
   */
  public XSL2ErrorException(String XSLRecommendationErrorNumber) {
    super(XSLRecommendationErrorNumber);
  }
}