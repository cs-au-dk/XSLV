package dongfang.xsltools.xmlclass.schemaside.dropoff;

import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NamedNodeType;
import dongfang.xsltools.xpath2.XPathComparisonExpr;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathFunctionCallExpr;
import dongfang.xsltools.xpath2.XPathStringLiteral;

public abstract class LocalNameDynamicRedeclaration extends NamedTypeDynamicRedeclaration {

  LocalNameDynamicRedeclaration() {
    super(null);
  }
  
  public LocalNameDynamicRedeclaration(DeclaredNodeType decorated) {
    super(decorated);
  }

  abstract LocalNameDynamicRedeclaration instantiate(DeclaredNodeType type) ;
  
  DeclaredNodeType _accept(SingleTypeXMLClass clazz,
      DeclaredNodeType candidate, XPathExpr predicate)
      throws XSLToolsSchemaException {
    // not our predicate, leave
    if (!(predicate instanceof XPathComparisonExpr))
      return candidate;

    XPathComparisonExpr p = (XPathComparisonExpr) predicate;

    if (!(p.getLHS() instanceof XPathFunctionCallExpr)
        && !(p.getRHS() instanceof XPathFunctionCallExpr))
      return candidate;

    if (!(p.getLHS() instanceof XPathStringLiteral)
        && !(p.getRHS() instanceof XPathStringLiteral))
      return candidate;

    XPathFunctionCallExpr function;
    XPathStringLiteral nameLit;

    if (p.getLHS() instanceof XPathFunctionCallExpr) {
      nameLit = (XPathStringLiteral) p.getRHS();
      function = (XPathFunctionCallExpr) p.getLHS();
    } else {
      nameLit = (XPathStringLiteral) p.getLHS();
      function = (XPathFunctionCallExpr) p.getRHS();
    }

    if (!function.getQName().getName().equals("local-name"))
      return candidate;

    String val = nameLit.getSweetContent();

    if (candidate instanceof NamedNodeType) {
    
    NamedNodeType nnt = (NamedNodeType) candidate;

    QName name = nnt.getQName();

    if (name.getName().equals(val))
      return instantiate(candidate);

    return null;
  } else {
    if (val.equals(""))
    return instantiate(candidate);
    return null;
  }
  }
}
