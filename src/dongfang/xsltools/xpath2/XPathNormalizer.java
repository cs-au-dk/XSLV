package dongfang.xsltools.xpath2;

public class XPathNormalizer {
  public static XPathExpr toplevelStuntz(XPathExpr exp) {
    if (exp instanceof XPathRelativePathExpr) {
      XPathPathExpr exp2 = new XPathRelativePathExpr();
      exp2.addStepsFrom((XPathPathExpr) exp);
      return exp2;
    }

    String e2s = exp.toString();
    if (e2s.equals("current()"))
      exp = new XPathAbsolutePathExpr();
    else if (e2s.equals("position()") || e2s.equals("last()")) {
      exp = new XPathNumericLiteral();
      ((XPathNumericLiteral) exp).setContent("1");
    } else if (e2s.equals("string()") || e2s.equals("string(.)")
        || e2s.equals("string(self::node())")) {
      /*
       * exp = new StringCast(); exp.subExpressions.add(new
       * NodeSetAbsLocationPath());
       */
      throw new AssertionError("TODO! Normalize string(.) in root context");
    } else if (e2s.equals("local-name()") || e2s.equals("namespace-uri()")
        || e2s.equals("name()"))
      exp = new XPathStringLiteral();
    return exp;
  }
}
