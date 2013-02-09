/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-16
 */
package dongfang.xsltools.exceptions;

/**
 * @author dongfang
 */
public class XSL2StaticErrorException extends XSLErrorException {

  /**
	 * 
	 */
	private static final long serialVersionUID = -6302174811385896096L;

/**
   * @param XSLRecommendationErrorNumber
   */
  public XSL2StaticErrorException(String XSLRecommendationErrorNumber) {
    super(XSLRecommendationErrorNumber);
  }
}