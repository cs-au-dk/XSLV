/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-16
 */
package dongfang.xsltools.simplification;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.xpath2.XPathExpr;

/**
 * A parameter or variable binding of an XPath expression (the only alternative
 * is RTF).
 * 
 * @author dongfang
 */
public class XPathExpressionParameterOrVariableBinding extends
    ParameterOrVariableBinding {

  // private XPathExp expression;
  private XPathExpr expression;

  XPathExpressionParameterOrVariableBinding(
      StylesheetModule bindingStylesheetModule, XPathExpr expression,
      Element namespaceBinder, short scope, short bindingType) {
    super(bindingStylesheetModule, scope, bindingType);
    // this.namespaceBinder = namespaceBinder;
    setExpression(expression);
  }

  @Override
public short getValueType() {
    return XPATH_VALUE_TYPE;
  }

  @Override
public void removeAllVariableRefs(Resolver resolver,
      ResolutionSimplifierBase simplifier) throws XSLToolsException {
    if (!contentIsResolved()) {
      boolean res = simplifier.simplify(this, resolver);
      if (res)
        setContentResolved();
    }
  }

  @Override
public void resolutionDiagnostics(Branch parent, DocumentFactory fac) {
    ((Element) parent).addAttribute("value", expression.toString());
  }

  void setExpression(XPathExpr expression) {
    this.expression = expression;
  }

  public XPathExpr getExpression() {
    return expression;
  }

  @Override
public String toString() {
    return expression.toString();
  }
}