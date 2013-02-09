/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.model;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

/**
 * DocumentFactoru that spews out the special parse location elements.
 * 
 * @author dongfang
 */
public class ParseLocationDocumentFactory extends DocumentFactory {
  /**
	 * 
	 */
	private static final long serialVersionUID = -5841669497898987319L;
private static ParseLocationDocumentFactory instance = new ParseLocationDocumentFactory();

  public static ParseLocationDocumentFactory getPLInstance() {
    return instance;
  }

  @Override
  public Element createElement(QName qname) {
    return new ParseLocationElement(qname);
  }

  @Override
  public Element createElement(String qualifiedName, String namespaceURI) {
    return createElement(createQName(qualifiedName, namespaceURI));
  }

  @Override
  public Element createElement(String name) {
    return createElement(createQName(name));
  }

  public Element cloneElement(Element original) {
    QName qname = original.getQName();
    if (original instanceof ParseLocationElement) {
      ParseLocationElement po = (ParseLocationElement) original;
      ParseLocationElement result = new ParseLocationElement(qname);
      result.elementEndTagEndColumn = po.elementEndTagEndColumn;
      result.elementEndTagEndLine = po.elementEndTagEndLine;
      result.elementStartTagBeginningColumn = po.elementStartTagBeginningColumn;
      result.elementStartTagBeginningLine = po.elementStartTagBeginningLine;
      result.elementStartTagEndColumn = po.elementStartTagEndColumn;
      result.elementStartTagEndLine = po.elementStartTagEndLine;
      return result;
    }
    return createElement(qname);
  }
}
