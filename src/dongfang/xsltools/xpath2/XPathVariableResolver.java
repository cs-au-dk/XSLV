package dongfang.xsltools.xpath2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Namespace;
import org.dom4j.QName;

import dongfang.XPathConstants;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsRuntimeException;
import dongfang.xsltools.simplification.Binding;
import dongfang.xsltools.simplification.ParameterOrVariableBinding;
import dongfang.xsltools.simplification.Resolver;
import dongfang.xsltools.simplification.XPathExpressionParameterOrVariableBinding;
import dongfang.xsltools.util.ListStack;

public class XPathVariableResolver extends XPathVisitor {
  private final Resolver resolver;

  private ListStack<Set<QName>> env = new ListStack<Set<QName>>();

  private final short target;

  private boolean everythingResolved = true;

  public XPathVariableResolver(Resolver resolver, short target) {
    this.resolver = resolver;
    this.target = target;
  }

  @Override
public XPathBase visit(XPathPathExpr expr) throws XSLToolsException {

    // do NOT visit children... visit((XPathBase)expr);
    if (expr.getStepCount() == 0)
      return expr;
    if (expr.getFirstStep() instanceof XPathVarRef) {
      XPathVarRef vryf = (XPathVarRef) expr.getFirstStep();
      XPathBase resolved = visit(vryf);

      if (!(resolved instanceof XPathPathExpr))
        return resolved;

      XPathPathExpr result = (XPathPathExpr) resolved;

      boolean skipFirst = true;
      for (XPathStepExpr step : expr) {
        if (skipFirst) {
          skipFirst = false;
        } else {
          result.addLastStep((XPathBase) step.clone());
        }
      }
      return result;
    }
    return expr;
  }

  @Override
public XPathBase visit(XPathVarRef vref) throws XSLToolsException {
    visit((XPathBase) vref);
    if (boundLocally(vref.getQName()))
      return super.visit(vref);
    try {
      Binding binding = resolve(vref, resolver);
      XPathBase exp = vref;
      if (binding != null) {
        // Variable or parameter?
        if (binding instanceof ParameterOrVariableBinding) {
          ParameterOrVariableBinding pvBinding = (ParameterOrVariableBinding) binding;
          if (target == ParameterOrVariableBinding.VARIABLE_BINDING_TYPE
              && pvBinding.isVariableBinding()) {
            ParameterOrVariableBinding binding2 = (ParameterOrVariableBinding) binding;
            /*
             * if (binding2.getValueType()!=
             * ParameterOrVariableBinding.XPATH_VALUE_TYPE) throw new
             * XSLSimplificationException ("Not an XPath type! Me confused!");
             */

            /*
             * If not an XPath type, then is must be RTF type. Do not play with
             * those in here!
             */
            if (binding2.getValueType() == ParameterOrVariableBinding.XPATH_VALUE_TYPE) {
              XPathExpr varXPath = ((XPathExpressionParameterOrVariableBinding) binding)
                  .getExpression();
              // Replace reference with declaration:
              varXPath.setArtifact("VariableResolver");
              exp = (XPathBase) varXPath;
            } else {
              everythingResolved = false;

              // emergency hack !!!
              // Make it UnknownRTF().
              // HMM.
              String unknownRTFFunction = XPathConstants.FUNC_UNKNOWN_RTF;
              // RTFHackVictimElement.addAttribute(RTFHackAttName,
              // unknownRTFFunction);
              XPathFunctionCallExpr exp2 = new XPathFunctionCallExpr(QName.get(
                  unknownRTFFunction, Namespace.NO_NAMESPACE));

              exp2.addEmptyPredicateList();

              // shold not be necessary?!?!?
              // RTFHackVictimModule.cacheXPathExpression(RTFHackVictimElement,
              // RTFHackAttName, exp2);
              exp = exp2;
            }
          } else if (target == ParameterOrVariableBinding.PARAMETER_BINDING_TYPE
              && pvBinding.isParameterBinding()) {
            System.err
                .println("Got one paramter, got one .. what am I going to do with it???");
          }
        }
      } else {
        throw new XSLToolsRuntimeException("Unresolved variable reference: "
            + vref.getQName());
      }
      return exp;
    } catch (XSLToolsException ex) {
      throw new XSLToolsRuntimeException(ex);
    }
  }

  @Override
  XPathBase visit(XPathForExpr e) throws XSLToolsException {
    addLocalVariable(e.getQName());
    return super.visit(e);
  }

  @Override
  XPathBase visit(XPathQuantifiedExpr e) throws XSLToolsException {
    addLocalVariable(e.getQName());
    return super.visit(e);
  }

  @Override
  XPathBase visit(XPathSimpleForClause2 e) throws XSLToolsException {
    addLocalVariable(e.getQName());
    return super.visit(e);
  }

  /*
   * Leave a local scope, forgetting about it.
   */
  @Override
  protected void ascend() {
    env.pop();
  }

  /*
   * Introduce new local scope
   */
  @Override
  protected void descend() {
    Set<QName> es = Collections.emptySet();
    env.push(es);
  }

  private void addLocalVariable(QName name) {
    if (env.isEmpty())
      // just a hack in case a variable binding expression is the root of the
      // XPathExpr visited.
      // Harmless, just makes the environment one too high throughout.
      env.push(new HashSet<QName>());

    Set<QName> top = env.peek();

    if (top == Collections.EMPTY_SET) {
      // get rid of it, we want a writeable set
      env.pop();
      env.push(top = new HashSet<QName>());
    }
    top.add(name);
  }

  private boolean boundLocally(QName name) {
    for (Set<QName> scope : env) {
      if (scope.contains(name))
        return true;
    }
    return false;
  }

  public static Binding resolve(XPathVarRef vref, Resolver resolver)
      throws XSLToolsException {
    // String qname_prefix = vref.getPrefix();
    // Implementation of 2.4: Qualified Names, in XSL spec: Default namespace
    // NOT used.
    // For XSLT 2.0 behaviour, assign default namespace instead !! (maybe not
    // that either ... see later draft)
    // QName name = QName.get(qualified_name, ns.getURI());
    QName name = vref.getQName();
    return resolver.resolve(name, Resolver.PARAMETER_AND_VARIABLE_SYMBOLSPACE);
  }

  public boolean isEverythingResolved() {
    return everythingResolved;
  }
}
