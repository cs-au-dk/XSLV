/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-16
 */
package dongfang.xsltools.exceptions;

/**
 * @author dongfang
 */
public class XSL1ErrorException extends XSLErrorException {

  /**
	 * 
	 */
	private static final long serialVersionUID = 5301935167816960198L;

/**
   * @param XSLRecommendationErrorNumber
   */
  //private float section;

  public XSL1ErrorException(String XSLRecommendationErrorNumber, float section) {
    super(XSLRecommendationErrorNumber);
    //this.section = section;
  }
}