package dongfang.xsltools.xpath2;

import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsRuntimeException;
import dongfang.xsltools.exceptions.XSLToolsXPathParseException;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.simplification.KeyBinding;
import dongfang.xsltools.simplification.Resolver;

/**
 * Replace occurences of key(qname) by //x, where x is the match pattern of the
 * key referred. (if x is absolute, // is not prepended)
 * 
 * @author dongfang
 */
public class KeyKiller extends XPathVisitor {
  public KeyKiller(NamespaceExpander expander) {
    this.expander = expander;
  }

  private Resolver resolver;

  private NamespaceExpander expander;

  private XPathBase convert(XPathExpr expr) {
    try {
      if (expr instanceof XPathUnionExpr) {
        XPathUnionExpr result = new XPathUnionExpr();
        XPathUnionExpr u = (XPathUnionExpr) expr;
        for (XPathExpr e : u) {
          result.add(convert(e));
        }
        expr = result;
      } else if (expr instanceof XPathRelativePathExpr) {
        String relativeMatchExpStr = "//" + expr.toString();
        XPathPathExpr pe = (XPathPathExpr) XPathParser.parse(
            relativeMatchExpStr, expander);
        pe.setArtifact("keyKillerConvert");
        expr = pe;
      }
      return (XPathBase) expr;
    } catch (XSLToolsXPathParseException ex) {
      throw new AssertionError(
          "Serialized XPath failed to re-parse. Should never happen");
    }
  }

  @Override
XPathBase visit(XPathPathExpr e) throws XSLToolsException {
    // We don't bother to recurse on path expressions: It (key) is bloody first
    // in the path
    // or it is not in the path at all.
    // visit((XPathBase)e);
    if (e.getStepCount() == 0)
      return e;
    if (e.getFirstStep() instanceof XPathFunctionCallExpr) {
      XPathFunctionCallExpr func = (XPathFunctionCallExpr) e.getFirstStep();
      if (func.getQName().getQualifiedName().equals("key")) {

        XPathExpr result = (XPathExpr) visit(func);

        if (result instanceof XPathUnionExpr)
          return (XPathBase) result;

        if (!(result instanceof XPathPathExpr)) {
          throw new XSLToolsException(
              "Key was not a path and not a union expression!");
        }

        boolean skipFirst = true;
        for (XPathStepExpr step : e) {
          if (skipFirst) {
            skipFirst = false;
          } else {
            ((XPathPathExpr) result).addLastStep((XPathBase) step.clone());
          }
        }
        return (XPathBase) result;
      }
    }
    return e;
  }

  @Override
XPathBase visit(XPathFunctionCallExpr expr) throws XSLToolsException {

    visit((XPathBase) expr);
    if (!expr.getQName().getQualifiedName().equals("key")) {
      return expr;
    }
    if (expr.arity() < 2)
      throw new XSLToolsRuntimeException(
          "Missing arguments in key function application");
    XPathExpr arg = expr.getArgument(0);

    if (arg instanceof XPathStringLiteral) {
      String keyName = ((XPathStringLiteral) arg).getSweetContent();
      // try {

      /*
       * old version, non union proof QName keyname =
       * expander.getQNameFromString(keyName,
       * NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE); KeyBinding key =
       * (KeyBinding) resolver.resolve(keyname, Resolver.KEY_SYMBOLSPACE);
       * 
       * XPathExpr matchExp = key.getMatchPattern();
       * 
       * try { if (matchExp instanceof XPathRelativePathExpr) { String
       * relativeMatchExpStr = matchExp.toString(); matchExp = (XPathPathExpr)
       * XPathParser.parse("//" + relativeMatchExpStr, expander); } } catch
       * (XSLToolsXPathParseException ex) { throw new AssertionError(
       * "Serialized XPath failed to re-parse. Should never happen"); } return
       * matchExp;
       */

      QName keyname = expander.getQNameFromString(keyName,
          NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
      KeyBinding key = (KeyBinding) resolver.resolve(keyname,
          Resolver.KEY_SYMBOLSPACE);

      XPathExpr matchExpr = key.getMatchPattern();

      return convert(matchExpr);

      // } catch(XSLToolsException ex) {
      // we don't care to check exceptions in the whole visit
      // throw new XSLToolsRuntimeException(ex);
      // }
      // }
      // throw new XSLToolsRuntimeException(
      // "Argument of key function was not a string constant");
    }
    throw new XSLToolsRuntimeException(
        "First arg of key function was not a string literal");
    // return expr;
  }

  public void setNamespaceExpander(NamespaceExpander expander) {
    this.expander = expander;
  }

  public void setResolver(Resolver resolver) {
    this.resolver = resolver;
  }
}
