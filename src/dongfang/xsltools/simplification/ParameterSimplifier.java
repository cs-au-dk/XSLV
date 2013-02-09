/*
 * Created on 2005-03-10
 */
package dongfang.xsltools.simplification;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;

import dongfang.XPathConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.DummyNamespaceExpander;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.UniqueNameGenerator;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathParser;
import dongfang.xsltools.xpath2.XPathVarRef;
import dongfang.xsltools.xpath2.XPathVariableResolver;

/**
 * Currently, variable refs inside parameter decls, and the problems of parameter
 * refs inside variable decls that are moved out of their scope on variable
 * resolution, are ignored !!
 * 
 * @author dongfang
 */
public class ParameterSimplifier extends ResolutionSimplifierBase {
  private ErrorReporter cesspool;

  protected static ParameterSimplifier getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new ParameterSimplifier(cesspool, names);
  }

  private ParameterSimplifier(ErrorReporter cesspool, UniqueNameGenerator names) {
    this.cesspool = cesspool;
  }

  void simplify(TemplateBinding binding, Resolver resolver) {
    throw new AssertionError("This method should not be called.");
  }

  @Override
boolean simplify(XPathExpressionParameterOrVariableBinding binding,
      Resolver resolver) throws XSLToolsException {
    XPathExpr e2 = (XPathExpr) binding.getExpression().accept(
        new XPathVariableResolver(resolver,
            ParameterOrVariableBinding.PARAMETER_BINDING_TYPE));
    binding.setExpression(e2);
    return true;// FIXME
  }

  /*
  private void substituteRTF(RTFParameterOrVariableBinding binding,
      StylesheetModule module) {
    Element substituteElement = binding.getCarrierClone();
    for (Iterator substs = substituteElement.elementIterator(); substs
        .hasNext();) {
      Element subst = (Element) substs.next();
      module.addCoreElementId(subst);
      subst.detach();
      locallevelAdditionQueue.add(subst);
    }
  }
  */

  /**
   * This very very complicated and a little fuct, too...
   */
  private Element resolveParRefs(StylesheetModule module,
      Element originalElement, Element substituteElement, Resolver resolver,
      int depth) throws XSLToolsException {

    if (substituteElement.getQName().equals(XSLConstants.ELEM_PARAM_QNAME))
      return null;

    if (substituteElement.getQName().equals(XSLConstants.ELEM_WITH_PARAM_QNAME))
      return null;

    if (XSLConstants.NAMESPACE_URI.equals(substituteElement.getNamespaceURI())) {
      for (int i = 0; i < substituteElement.attributeCount(); i++) {
        Attribute a = substituteElement.attribute(i);
        if (a.getQName().equals(XSLConstants.ATTR_SELECT_QNAME)) {
          try {
            String attributeName = a.getName();
            XPathExpr xpathExp = module.getCachedXPathExp(attributeName, a
                .getValue(), originalElement, substituteElement);

            String s = xpathExp.toString();

            if (s.indexOf('$') >= 0) {
              if (!(xpathExp instanceof XPathVarRef)) {

                /*
                 * Hack: See that the dollar sign is not in predicate
                 */
                int j = s.indexOf('$');
                int lklam = s.indexOf('[', 0);
                int rklam = s.lastIndexOf(']', j);
                if (lklam > j && rklam < j) {

                  // panic, give up
                  // make it an unknownString()...
                  String unknownStringFunction = /*
                                                   * Dom4jUtil.makeAttributeValue(
                                                   * XPathConstants.FUNC_UNKNOWN_STRING_QNAME,
                                                   * substituteElement)
                                                   */
                  XPathConstants.FUNC_UNKNOWN_STRING + "()";
                  a.setValue(unknownStringFunction);
                }
              } else {
                // redo me -- quirk and dirty fix...
                // Set<String> alreadyUsedExpressions = new HashSet<String>();

                QName varName = ((XPathVarRef) xpathExp).getQName();
                Binding splatter = resolver.resolve(varName,
                    Resolver.LOCAL_PARAMDECL_SET_SYMBOLSPACE);
                if (splatter == null
                    || !(splatter instanceof LocalParameterSet)) {
                  System.err.println("Fucking underligt");
                } else {
                  Element goose = fac
                      .createElement(XSLConstants.ELEM_CHOOSE_QNAME);
                  module.addCoreElementId(goose);
                  boolean first = true;
                  for (Iterator<Binding> iter = ((LocalParameterSet) splatter)
                      .getBindings().iterator(); iter.hasNext();) {
                    Element cse = null;
                    Binding binding = iter.next();
                    if (iter.hasNext()) {
                      cse = fac.createElement(XSLConstants.ELEM_WHEN_QNAME);
                      module.addCoreElementId(cse);
                      String unknownBooleanFunction = /*
                                                       * Dom4jUtil
                                                       * .makeAttributeValue(
                                                       * XPathConstants.FUNC_UNKNOWN_BOOLEAN_QNAME,
                                                       * cse)
                                                       */
                      XPathConstants.FUNC_UNKNOWN_BOOLEAN + "()";
                      cse.addAttribute(XSLConstants.ATTR_TEST,
                          unknownBooleanFunction);
                      module.cacheXPathExpression(cse, XSLConstants.ATTR_TEST,
                          XPathParser.parse(unknownBooleanFunction,
                              new DummyNamespaceExpander()));
                      goose.add(cse);
                      first = false;
                    } else if (first) {
                      cse = goose = substituteElement;
                    } else {
                      cse = fac
                          .createElement(XSLConstants.ELEM_OTHERWISE_QNAME);
                      module.addCoreElementId(cse);
                      goose.add(cse);
                    }

                    if (binding instanceof RTFParameterOrVariableBinding) {
                      RTFParameterOrVariableBinding rtf = (RTFParameterOrVariableBinding) binding;
                      Element cc = (Element) rtf.getCarrierClone().elements()
                          .get(0);
                      cc.detach();
                      module.addCoreElementId(cc);
                      if (first && !iter.hasNext())
                        goose = cc;
                      else
                        cse.add(cc);
                    } else if (binding instanceof XPathExpressionParameterOrVariableBinding) {
                      XPathExpressionParameterOrVariableBinding xp = (XPathExpressionParameterOrVariableBinding) binding;

                      // String s2 = xp.toString();
                      // if (alreadyUsedExpressions.add(s2)) {

                      Element vof = fac
                          .createElement(XSLConstants.ELEM_VALUE_OF_QNAME);
                      module.addCoreElementId(vof);
                      vof.addAttribute(XSLConstants.ATTR_SELECT_QNAME, xp
                          .toString());
                      module.cacheXPathExpression(vof,
                          XSLConstants.ATTR_SELECT, xp.getExpression());
                      if (first && !iter.hasNext())
                        goose = vof;
                      else
                        cse.add(vof);
                      // }
                    }
                  }
                  return goose;
                }
              }
            }
          } catch (XSLToolsException ex) {
            System.err.println(getClass().getSimpleName() + "OUCH: "
                + a.getValue() + " : " + ex.getMessage() + ", module is: "
                + module);
            ex.printStackTrace();
            cesspool.reportError(module, substituteElement,
                ParseLocation.Extent.TAG, ex);
          }
        }
      }
    }
    return substituteElement;
  }

  /**
   * Simplify nodes
   */
  @Override
protected Node simplify(Node node, StylesheetModule module,
      Resolver resolver, int depth, boolean lastNode) throws XSLToolsException {
    // Element proper first, and then contents.. no problem that a local
    // var or par definition will not appear in its own scope; this is not
    // allowed anyway.
    // if a var ref -- resolve it!

    switch (node.getNodeType()) {
    case (Node.ELEMENT_NODE):
      Element originalElement = (Element) node;

      /*
       * Now, simplify all children .. note that is does not matter if the
       * current element is a variable or parameter definition and it has not
       * been bound yet -- the binding is invisible from its children anyway.
       */
      Element substituteElement = simplifyBelow(originalElement, module,
          resolver.enterScope(), depth + 1);

      /*
       * Now, eradicate any variable reference and bind any declarations TODO:
       * Do I mean the shim, or the resolver below here -- or both?
       */
      substituteElement = resolveParRefs(module, originalElement,
          substituteElement, resolver, depth);

      if (substituteElement == null)
        return null;
      /*
       * Resolve any use-atttribute-sets Bind any xsl:attribute-set's
       */

      return substituteElement;
    default:
      return (Node) node.clone();
    }
  }

  protected void simplify(StylesheetModule module, Stylesheet stylesheet,
      Resolver resolver) throws XSLToolsException {
    // resolver er faktisk stylesheetet.
    Document d2 = fac.createDocument(simplifyBelow(module
        .getDocumentElement(StylesheetModule.CORE), module, resolver, 0));
    module.setDocument(d2, StylesheetModule.CORE);
  }

  protected StylesheetLevel simplify(Stylesheet stylesheet,
      StylesheetLevel level) throws XSLToolsException {
    for (StylesheetModule module : level.contents()) {
      simplify(module, stylesheet, stylesheet);
    }

    for (StylesheetLevel imported : level.imports()) {
      simplify(stylesheet, imported);
    }
    return level;
  }

  /**
   * Track down all local parameter declarations and bind sets of parameter
   * declarations under same name.
   */
  protected void bindParameterDecls(StylesheetModule module, Element before,
      Resolver stylesheet, Set<QName> cheetah) throws XSLToolsException {

    if (before.getQName().equals(XSLConstants.ELEM_WITH_PARAM_QNAME)) {
      String nameAttVal = before.attributeValue(XSLConstants.ATTR_NAME);

      QName qname = ElementNamespaceExpander
          .qNameForXSLAttributeValue(nameAttVal, before,
              NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
      cheetah.add(qname);

      Binding binding = makeBinding(module, before, before,
          Resolver.LOCAL_SCOPE,
          ParameterOrVariableBinding.PARAMETER_BINDING_TYPE, cesspool);

      Binding bindings = new LocalParameterSet(module, binding);

      stylesheet.bind(qname, bindings);
    } else if (before.getQName().equals(XSLConstants.ELEM_PARAM_QNAME)) {
      String nameAttVal = before.attributeValue(XSLConstants.ATTR_NAME);

      QName qname = ElementNamespaceExpander
          .qNameForXSLAttributeValue(nameAttVal, before,
              NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
      cheetah.add(qname);

      Binding binding = makeBinding(module, before, before,
          Resolver.LOCAL_SCOPE,
          ParameterOrVariableBinding.PARAMETER_BINDING_TYPE, cesspool);

      Binding bindings = new LocalParameterSet(module, binding);

      stylesheet.bind(qname, bindings);
    } else {
      for (Iterator<Element> children = before.elementIterator(); children
          .hasNext();) {
        Element child = children.next();
        bindParameterDecls(module, child, stylesheet, cheetah);
      }
    }
  }

  protected void bindParameterDecls(StylesheetModule module,
      Stylesheet stylesheet, Resolver resolver, Set<QName> cheetah)
      throws XSLToolsException {
    // resolver er faktisk stylesheetet.
    bindParameterDecls(module, module
        .getDocumentElement(StylesheetModule.CORE), resolver, cheetah);

    /*
     * Document d2 = fac.createDocument
     * (simplifyBelow(module.getDocumentElement(StylesheetModule.SIMPLIFIED),
     * module, resolver, 0)); module.setDocument(d2,
     * StylesheetModule.SIMPLIFIED);
     */
  }

  protected StylesheetLevel bindParameterDecls(Stylesheet stylesheet,
      StylesheetLevel level, Set<QName> cheetah) throws XSLToolsException {
    for (StylesheetModule module : level.contents()) {
      bindParameterDecls(module, stylesheet, stylesheet, cheetah);
    }

    for (StylesheetLevel imported : level.imports()) {
      bindParameterDecls(stylesheet, imported, cheetah);
    }
    return level;
  }

  public void process(Stylesheet stylesheet) throws XSLToolsException {
    Set<QName> cheetah = new HashSet<QName>();
    bindParameterDecls(stylesheet, stylesheet.getPrincipalLevel(), cheetah);
    for (QName n : cheetah) {
      /*Binding b = */stylesheet.resolve(n,
          Resolver.LOCAL_PARAMDECL_SET_SYMBOLSPACE);
      // LocalParameterSet s = (LocalParameterSet) b;
    }
    simplify(stylesheet, stylesheet.getPrincipalLevel());
  }
}
