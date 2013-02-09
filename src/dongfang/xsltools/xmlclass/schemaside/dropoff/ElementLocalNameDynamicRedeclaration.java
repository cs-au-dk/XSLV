package dongfang.xsltools.xmlclass.schemaside.dropoff;

import org.dom4j.QName;

import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NamedNodeType;
import dongfang.xsltools.xpath2.XPathComparisonExpr;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathFunctionCallExpr;
import dongfang.xsltools.xpath2.XPathStringLiteral;

public class ElementLocalNameDynamicRedeclaration extends
    ElementUseDynamicRedeclaration {

  ElementLocalNameDynamicRedeclaration() {
    super(null);
  }

  ElementLocalNameDynamicRedeclaration(ElementUse type) {
    super(type);
  }

  ElementLocalNameDynamicRedeclaration instantiate(DeclaredNodeType type) {
    return new ElementLocalNameDynamicRedeclaration((ElementUse) type);
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

      // reject if different name.
      return CardinalMatch.NEVER_ARG_TYPE;
    }
    if (val.equals(""))
      // ooooo kay, some types are defined to be empty named...
      return CardinalMatch.ALWAYS_ARG_TYPE;
    return CardinalMatch.NEVER_ARG_TYPE;
  }

  @Override
  public CardinalMatch transform(SingleTypeXMLClass clazz,
      DeclaredNodeType candidate, XPathExpr predicate) {
    //if (!(candidate instanceof ElementUse)) return CardinalMatch.MAYBE_ARG_TYPE;
    return _accept(clazz, candidate, predicate);
  }
}
