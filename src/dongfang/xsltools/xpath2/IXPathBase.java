package dongfang.xsltools.xpath2;

import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.exceptions.XSLToolsException;

public interface IXPathBase extends Diagnoseable {
  abstract IXPathBase accept(XPathVisitor v) throws XSLToolsException;

  // void acceptChildren(XPathVisitor v) throws XSLToolsException;
  boolean similarTo(XPathBase that);
}
