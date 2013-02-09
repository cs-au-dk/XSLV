/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.exceptions;

import org.dom4j.QName;

import dongfang.xsltools.util.Dom4jUtil;

/**
 * @author dongfang
 */
public class XSLToolsResolverAlreadyBoundException extends
    XSLToolsResolverException {
  /**
	 * 
	 */
	private static final long serialVersionUID = 7359177806367048822L;

public XSLToolsResolverAlreadyBoundException(QName qname) {
    super("Variable or parameter name already bound in the same scope: "
        + Dom4jUtil.clarkName(qname));
    this.qname = qname;
  }
}