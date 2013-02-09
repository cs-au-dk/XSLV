package dongfang.xsltools.xmlclass.schemaside.dropoff;

import org.dom4j.QName;

import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NamedNodeType;
import dongfang.xsltools.xpath2.XPathComparisonExpr;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathFunctionCallExpr;
import dongfang.xsltools.xpath2.XPathStringLiteral;

public class AttributeLocalNameDynamicRedeclaration extends
    AttributeUseDynamicRedeclaration {

  AttributeLocalNameDynamicRedeclaration() {
    super(null);
  }

  AttributeLocalNameDynamicRedeclaration(AttributeUse type) {
    super(type);
  }

  AttributeLocalNameDynamicRedeclaration instantiate(DeclaredNodeType type) {
    return new AttributeLocalNameDynamicRedeclaration((AttributeUse) type);
  }

  CardinalMatch _accept(SingleTypeXMLClass clazz,
      DeclaredNodeType candidate, XPathExpr predicate) {
    // not our predicate, leave
    if (!(predicate instanceof XPathComparisonExpr))
      return CardinalMatch.MAYBE_ARG_TYPE;

    XPathComparisonExpr p = (XPathComparisonExpr) predicate;

    if (!(p.getLHS() instanceof XPathFunctionCallExpr)
        && !(p.getRHS() instanceof XPathFunctionCallExpr))
      return CardinalMatch.MAYBE_ARG_TYPE;

    if (!(p.getLHS() instanceof XPathStringLiteral)
        && !(p.getRHS() instanceof XPathStringLiteral))
      return CardinalMatch.MAYBE_ARG_TYPE;

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
      return CardinalMatch.MAYBE_ARG_TYPE;

    String val = nameLit.getSweetContent();

    if (candidate instanceof NamedNodeType) {

      NamedNodeType nnt = (NamedNodeType) candidate;

      QName name = nnt.getQName();

      if (name.getName().equals(val))
        return CardinalMatch.ALWAYS_ARG_TYPE;
      return CardinalMatch.NEVER_ARG_TYPE;
    }
    if (val.equals(""))
      return CardinalMatch.ALWAYS_ARG_TYPE;
    return CardinalMatch.NEVER_ARG_TYPE;
  }

  /*
  DeclaredNodeType _reject(SingleTypeXMLClass clazz,
      DeclaredNodeType candidate, XPathExpr predicate) {
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

      if (!name.getName().equals(val))
        return instantiate(candidate);

      return null;
    }
    
    if (!val.equals(""))
      return instantiate(candidate);
    return null;
  }
  */

  @Override
  public CardinalMatch transform(SingleTypeXMLClass clazz,
      DeclaredNodeType candidate, XPathExpr predicate) {
    //if (!(candidate instanceof AttributeUse))
    //  return CardinalMatch.MAYBE_ARG_TYPE;
    return _accept(clazz, candidate, predicate);
  }

  @Override
  protected boolean dynamicRedeclarationPropertiesEquals(DynamicRedeclaration o) {
    return decorated != null && decorated.equals(o.decorated);
  }
}
