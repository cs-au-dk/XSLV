/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.exceptions;

import org.dom4j.QName;

import dongfang.xsltools.simplification.ResolverBase;

/**
 * @author dongfang
 */
public class XSLToolsResolverNotFoundException extends
    XSLToolsResolverException {
  /**
	 * 
	 */
	private static final long serialVersionUID = 5275692854442204252L;

public XSLToolsResolverNotFoundException(QName qname, short symbolSpace) {
    super("No " + ResolverBase.getSymbolSpaceName(symbolSpace)
        + " binding of the name " + qname.getQualifiedName() + " in scope");
    this.qname = qname;
    this.symbolSpace = symbolSpace;
  }
}