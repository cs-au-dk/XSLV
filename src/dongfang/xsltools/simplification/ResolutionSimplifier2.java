/*
 */
package dongfang.xsltools.simplification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;

import dongfang.XMLConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.UniqueNameGenerator;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathPathExpr;
import dongfang.xsltools.xpath2.XPathVarRef;
import dongfang.xsltools.xpath2.XPathVariableResolver;

/**
 * Supposed to do the following: - Resolution of all variables - Resolution of
 * all attribute sets - Transformation of all for-each instructions into
 * separate templates, with appropriate parameter+variable forwarding
 * 
 * under the assumptions: - All top level variables (and parameters?) have been
 * captured and bound - All top level attribute sets have been captured and
 * bound
 * 
 * Caveats to remember: - When an attribute set is resolved, remember to
 * simplify the contents bound in the resolver. This was solved by resolving
 * variables in-depth before binding attribute sets. - When for-each contents is
 * flung into a separate template rule, remember to simplify it.
 * 
 * All in all, this code is COMPLEX and lots of bugs have been found here.
 * There are probably more.
 * 
 * @author dongfang
 */
public class ResolutionSimplifier2 extends ResolutionSimplifierBase {
  private ErrorReporter cesspool;

  private UniqueNameGenerator names;

  protected static ResolutionSimplifier2 getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new ResolutionSimplifier2(cesspool, names);
  }

  private ResolutionSimplifier2(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    this.cesspool = cesspool;
    this.names = names;
  }

  /*
  private StylesheetModule RTFHackModule;
  private Element RTFHackElement;
  private String RTFHackAttName;
  private boolean simplifyCopy = false;
  */
  
  @Override
boolean simplify(XPathExpressionParameterOrVariableBinding binding,
      Resolver resolver) throws XSLToolsException {
    XPathVariableResolver suppe = new XPathVariableResolver(resolver,
        ParameterOrVariableBinding.VARIABLE_BINDING_TYPE);

    XPathExpr e2 = (XPathExpr) binding.getExpression().accept(suppe);

    binding.setExpression(e2);
    return suppe.isEverythingResolved();
  }

  private QName generateFreshMode() {
    QName mode = QName.get(names
        .getFreshId(XMLConstants.MODE_ENUMERATION_FORMAT),
        XMLConstants.TEMPLATE_MODE_NAMESPACE);
    // The default rule has no significance -- it is never hit anyway.
    // stylesheet.addDefaultXSLT1ModedTemplate(mode);
    return mode;
  }

  /**
   * Generate a template as part of call-template or ... simplification. All new
   * templates generated will be put in the Stylesheet document (never on any
   * document in an ImportPrecedenceGroup)
   * 
   * @param substituteForEach
   * @param forwards
   * @param mode
   * @return
   */
  Element generateForEachSubstitute(Element originalForEach,
      Element substituteForEach, Set<QName> forwards, QName mode,
      StylesheetModule module, Resolver resolver) throws XSLToolsException {

    /*
     * Make replacement element and set its mode, select elements Cache-wise:
     * Element ID and attribute name preserved. Expression the same as before.
     */
    Element replacement = fac
        .createElement(XSLConstants.ELEM_APPLY_TEMPLATES_QNAME);

    if (SimplifierConfiguration.current.addTransformOriginComments()) {
      replacement.addProcessingInstruction("simplification", "for-each");
    }

    module.addCoreElementId(replacement);
    // make substitute backtrack its origin to the contents carrier, the
    // for-each instruction that is replaced by an apply-template instruction
    replacement.addAttribute(XMLConstants.ELEMENT_ID_QNAME, substituteForEach
        .attributeValue(XMLConstants.ELEMENT_ID_QNAME));

    // Tag it as derived
    // replacement.addAttribute
    // (XMLConstants.DERIVED_FLAG_QNAME,
    // contents.attributeValue(XMLConstants.ELEMENT_ID_QNAME));

    // Dom4jUtil.addQNameAttribute(replacement, XSLConstants.ATTR_MODE, mode);
    Dom4jUtil.transferAttributeValue(mode, XSLConstants.ATTR_MODE, replacement);

    Attribute select = substituteForEach
        .attribute(XSLConstants.ATTR_SELECT_QNAME);
    if (select != null) {
      substituteForEach.remove(select);
      replacement.add(select);
    }

    // Transfer sorts .. they should all come before the template, so we may
    // stop when no more are in sight
    for (Iterator<Element> sorts = substituteForEach.elementIterator(); sorts
        .hasNext();) {
      Element maybeSort = sorts.next();
      if (maybeSort.getQName().equals(XSLConstants.ELEM_SORT_QNAME)) {
        maybeSort.detach();
        replacement.add(maybeSort);
      } else
        break;
    }

    /*
     * use a list instead of a set, to keep consistent order
     */
    List<QName> brokenScopeVariables = new ArrayList<QName>();
    brokenScopeVariables.addAll(forwards);

    /*
     * Add with-params to replacement
     */
    ListIterator<QName> lit = brokenScopeVariables
        .listIterator(brokenScopeVariables.size());
    while (lit.hasPrevious()) {
      // TODO: Safer prefixing (may conflict w others)
      QName name = lit.previous();
      Element element = fac.createElement(XSLConstants.ELEM_WITH_PARAM_QNAME);
      // TODO: Safer prefixing (may move out of scope)
      // module.addDesugaredElementId(element);

      module.addCoreElementId(element);
      replacement.add(element);

      String newqname = Dom4jUtil.makeAttributeValue(name, element);

      element.addAttribute(XSLConstants.ATTR_NAME_QNAME, newqname);
      // element.addAttribute(XSLConstants.ATTR_SELECT, "$xxx" + newqname);
      ParameterOrVariableBinding b = (ParameterOrVariableBinding) resolver
          .resolve(name, Resolver.PARAMETER_AND_VARIABLE_SYMBOLSPACE);

      if (b instanceof XPathExpressionParameterOrVariableBinding) {
        //this.RTFHackAttName = XSLConstants.ATTR_SELECT;
        //this.RTFHackElement = element;
        //this.RTFHackModule = module;
        XPathExpr subst = substituteXPath(module,
            ((XPathExpressionParameterOrVariableBinding) b).getExpression(),
            originalForEach, substituteForEach, resolver);
        module.cacheXPathExpression(substituteForEach,
            XSLConstants.ATTR_SELECT, subst);
        element.addAttribute(XSLConstants.ATTR_SELECT, subst.toString());
      } else if (b instanceof RTFParameterOrVariableBinding) {
        // TODO: This should never happen after Lambda-stunt introduced.
        RTFParameterOrVariableBinding rb = (RTFParameterOrVariableBinding) b;
        substituteRTF(rb, module);
      }
    }

    /*
     * Rename original element to xsl:template and fix mode, select attributes.
     * Tag as derived (the new template element is derived from for-each)
     */
    substituteForEach.setQName(QName.get(XSLConstants.ELEM_TEMPLATE,
        XSLConstants.NAMESPACE));
    if (SimplifierConfiguration.current.addTransformOriginComments()) {
      substituteForEach.addProcessingInstruction("simplification",
          "for-each-target");
    }
    Dom4jUtil.transferAttributeValue(mode, XSLConstants.ATTR_MODE,
        substituteForEach);
    substituteForEach.addAttribute(XSLConstants.ATTR_MATCH,
        "child::node()|attribute::*|/");
    substituteForEach.addAttribute(XSLConstants.ATTR_PRIORITY, "0");
    // contents.addAttribute(XMLConstants.DERIVED_FLAG_QNAME,
    // contents.attributeValue(XMLConstants.ELEMENT_ID_QNAME));

    String eid = substituteForEach
        .attributeValue(XMLConstants.ELEMENT_ID_QNAME);
    Element myOriginal = module.getElementById(eid, StylesheetModule.ORIGINAL);
    myOriginal.addAttribute(XMLConstants.KNOCKEDOUT, "for-each");

    /*
     * Prepend params to original element child list
     */
    for (QName name : brokenScopeVariables) {
      Element element = fac.createElement(QName.get(XSLConstants.ELEM_PARAM,
          XSLConstants.NAMESPACE));
      module.addCoreElementId(element);
      Dom4jUtil.transferAttributeValue(name, XSLConstants.ATTR_NAME, element);
      substituteForEach.elements().add(0, element);
    }
    toplevelAdditionQueue.add(substituteForEach);
    return replacement;
  }

  /*
  Element generateCopySubstitute(StylesheetModule module, Element copy,
      Set<QName> forwards, QName mode) {
    // #21

    if (!simplifyCopy)
      return copy;

    List<QName> brokenScopeVariables = new ArrayList<QName>();
    brokenScopeVariables.addAll(forwards);

    / *
     * create common base for first two template rules Ged rid of any
     * use-attribute-sets
     * /
    Attribute useAttSetsAtt = copy
        .attribute(XSLConstants.ATTR_USE_ATTRIBUTE_SETS_QNAME);

    if (useAttSetsAtt != null)
      copy.remove(useAttSetsAtt);

    / *
     * add params
     * /
    for (QName name : brokenScopeVariables) {
      Element element = fac.createElement(XSLConstants.ELEM_PARAM_QNAME);
      module.addCoreElementId(element);
      // element.addAttribute(XSLConstants.ATTR_NAME,
      // name.getQualifiedName());
      // Dom4jUtil.addQNameAttribute(element, XSLConstants.ATTR_NAME, name);
      Dom4jUtil.transferAttributeValue(name, XSLConstants.ATTR_NAME, element);
      copy.elements().add(0, element);
    }

    Element copyCopy = (Element) copy.clone();
    module.reassignCoreIDs(copyCopy);

    copy.setQName(XSLConstants.ELEM_TEMPLATE_QNAME);
    copy.addAttribute(XSLConstants.ATTR_MATCH, "/");
    copy.addAttribute(XSLConstants.ATTR_PRIORITY, "0");
    copy.addAttribute(XMLConstants.KNOCKEDOUT, "copy");

    Dom4jUtil.transferAttributeValue(mode, XSLConstants.ATTR_MODE, copy);

    toplevelAdditionQueue.add(copy);

    if (SimplifierConfiguration.current.addTransformOriginComments()) {
      copyCopy.addProcessingInstruction("simplification", "copy");
    }

    / *
     * create second template
     * /
    copyCopy.setQName(QName.get(XSLConstants.ELEM_ELEMENT,
        XSLConstants.NAMESPACE));
    copyCopy.addAttribute(XSLConstants.ATTR_NAME, "{name()}");

    Element template2 = fac.createElement(XSLConstants.ELEM_TEMPLATE_QNAME);
    module.addCoreElementId(template2);

    if (SimplifierConfiguration.current.addTransformOriginComments()) {
      template2.addProcessingInstruction("simplification", "copy");
    }

    template2.add(copyCopy);
    template2.addAttribute(XSLConstants.ATTR_MATCH, "child::*");
    template2.addAttribute(XSLConstants.ATTR_PRIORITY, "0");
    // Dom4jUtil.addQNameAttribute(template2, XSLConstants.ATTR_MODE, mode);
    Dom4jUtil.transferAttributeValue(mode, XSLConstants.ATTR_MODE, template2);

    String eid = copy.attributeValue(XMLConstants.ELEMENT_ID_QNAME);
    template2.addAttribute(XMLConstants.ELEMENT_ID_QNAME, eid);

    toplevelAdditionQueue.add(template2);

    Element template3 = Dom4jUtil
        .parse("<template xmlns='"
            + XSLConstants.NAMESPACE_URI
            + "' match='attribute::*' priority='0'>"
            + "<attribute name='{name()}'><value-of select='string(self::node())'/></attribute></template>");
    module.reassignCoreIDs(template3);
    Dom4jUtil.transferAttributeValue(mode, XSLConstants.ATTR_MODE, template3);
    template3.addAttribute(XMLConstants.ELEMENT_ID_QNAME, eid);
    if (SimplifierConfiguration.current.addTransformOriginComments()) {
      template3.addProcessingInstruction("simplification", "copy");
    }

    toplevelAdditionQueue.add(template3);

    Element template4 = Dom4jUtil.parse("<template xmlns='"
        + XSLConstants.NAMESPACE_URI + "' match='child::text()' priority='0'>"
        + "<value-of select='string(self::node())'/></template>");
    module.reassignCoreIDs(template4);
    Dom4jUtil.transferAttributeValue(mode, XSLConstants.ATTR_MODE, template4);
    template4.addAttribute(XMLConstants.ELEMENT_ID_QNAME, eid);
    if (SimplifierConfiguration.current.addTransformOriginComments()) {
      template4.addProcessingInstruction("simplification", "copy");
    }
    toplevelAdditionQueue.add(template4);

    Element template5 = Dom4jUtil
        .parse("<template xmlns='"
            + XSLConstants.NAMESPACE_URI
            + "' match='child::comment()' priority='0'>"
            + "<comment><value-of select='string(self::node())'/></comment></template>");
    module.reassignCoreIDs(template5);
    Dom4jUtil.transferAttributeValue(mode, XSLConstants.ATTR_MODE, template5);
    template5.addAttribute(XMLConstants.ELEMENT_ID_QNAME, eid);
    if (SimplifierConfiguration.current.addTransformOriginComments()) {
      template5.addProcessingInstruction("simplification", "copy");
    }
    toplevelAdditionQueue.add(template5);

    Element template6 = Dom4jUtil
        .parse("<template xmlns='"
            + XSLConstants.NAMESPACE_URI
            + "' match='child::processing-instruction()' priority='0'>"
            + "<processing-instruction name='{local-name()}'><value-of select='string(self::node())'/>"
            + "</processing-instruction></template>");
    module.reassignCoreIDs(template6);
    Dom4jUtil.transferAttributeValue(mode, XSLConstants.ATTR_MODE, template6);
    template6.addAttribute(XMLConstants.ELEMENT_ID_QNAME, eid);
    if (SimplifierConfiguration.current.addTransformOriginComments()) {
      template6.addProcessingInstruction("simplification", "copy");
    }
    toplevelAdditionQueue.add(template6);

    Element applyTemplates = Dom4jUtil.parse("<apply-templates xmlns='"
        + XSLConstants.NAMESPACE_URI + "' select='self::node()'/>");
    module.addCoreElementId(applyTemplates);
    Dom4jUtil.transferAttributeValue(mode, XSLConstants.ATTR_MODE,
        applyTemplates);
    if (SimplifierConfiguration.current.addTransformOriginComments()) {
      applyTemplates.addProcessingInstruction("simplification", "copy");
    }
    applyTemplates.addAttribute(XMLConstants.ELEMENT_ID_QNAME, eid);

    / *
     * Add with-params to replacement
     * /
    ListIterator<QName> lit = brokenScopeVariables
        .listIterator(brokenScopeVariables.size());
    while (lit.hasPrevious()) {
      QName name = lit.previous();
      Element withParam = fac.createElement(QName.get(
          XSLConstants.ELEM_WITH_PARAM, XSLConstants.NAMESPACE));
      module.addCoreElementId(withParam);
      applyTemplates.add(withParam);
      String newQName = Dom4jUtil.makeAttributeValue(name, withParam);
      withParam.addAttribute(XSLConstants.ATTR_NAME, newQName);
      withParam.addAttribute(XSLConstants.ATTR_SELECT, "$" + newQName);
    }
    return applyTemplates;
  }
*/

  Element generateCopyOfSubstitute(StylesheetModule module,
      XPathExpr xpathExpr, Element substituteElement) {
    // if (xpathExpr.getType() == XPathExp.TYPE_NODESET) {
    if (xpathExpr instanceof XPathPathExpr) { // TODO: this is NOT!!! a valid
      // approximation
      substituteElement.setQName(XSLConstants.ELEM_APPLY_TEMPLATES_QNAME);

      // select expression left unchanged
      QName freshMode = generateFreshMode();

      // Dom4jUtil.addQNameAttribute(substituteElement, XSLConstants.ATTR_MODE,
      // freshMode);
      Dom4jUtil.transferAttributeValue(freshMode, XSLConstants.ATTR_MODE,
          substituteElement);

      Element template = fac.createElement(XSLConstants.ELEM_TEMPLATE_QNAME);
      if (SimplifierConfiguration.current.addTransformOriginComments()) {
        template.addProcessingInstruction("simplification", "copy-of");
      }

      module.addCoreElementId(template);

      template.addAttribute(XSLConstants.ATTR_MATCH_QNAME,
          "child::node()|attribute::*|/");

      template.addAttribute(XSLConstants.ATTR_PRIORITY_QNAME, "0");
      // Dom4jUtil.addQNameAttribute(template, XSLConstants.ATTR_MODE,
      // freshMode);
      Dom4jUtil.transferAttributeValue(freshMode, XSLConstants.ATTR_MODE,
          template);
      template.addAttribute(XMLConstants.ELEMENT_ID_QNAME, substituteElement
          .attributeValue(XMLConstants.ELEMENT_ID_QNAME));

      Element copy = fac.createElement(XSLConstants.ELEM_COPY_QNAME);
      module.addCoreElementId(copy);
      copy.addAttribute(XMLConstants.ELEMENT_ID_QNAME, substituteElement
          .attributeValue(XMLConstants.ELEMENT_ID_QNAME));
      template.add(copy);

      Element apply = fac
          .createElement(XSLConstants.ELEM_APPLY_TEMPLATES_QNAME);
      apply.addAttribute(XMLConstants.ELEMENT_ID_QNAME, substituteElement
          .attributeValue(XMLConstants.ELEMENT_ID_QNAME));
      module.addCoreElementId(apply);

      copy.add(apply);
      apply
          .addAttribute(XSLConstants.ATTR_SELECT, "child::node()|attribute::*");

      Dom4jUtil
          .transferAttributeValue(freshMode, XSLConstants.ATTR_MODE, apply);

      toplevelAdditionQueue.add(template);

    } else {
      // boolean, number or string type
      substituteElement.setQName(XSLConstants.ELEM_VALUE_OF_QNAME);
    }
    return substituteElement;
  }

  private XPathExpr substituteXPath(StylesheetModule module,
      XPathExpr xPathExp, Element originalElement, Element substituteElement,
      Resolver resolver) throws XSLToolsException {

    XPathExpressionParameterOrVariableBinding temp = new XPathExpressionParameterOrVariableBinding(
        module, xPathExp, originalElement, Resolver.UNDEFINED_SCOPE,
        ParameterOrVariableBinding.UNDEFINED_BINDING_TYPE);

    temp.removeAllVariableRefs(resolver, this);

    return temp.getExpression();
  }

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

  /**
   * Resolve all variable references in XPath expressions, and, if element is a
   * variable declaration, bind it.
   * 
   * @param module -
   *          stylesheet module (for error reporting)
   * @param originalElement -
   *          original element (good for resolvong namespace prefixes with!)
   * @param substituteElement -
   *          the element we are working on
   * @param resolver -
   *          resolver to bind to
   * @param depth -
   *          recursion depth
   * @return
   * @throws XSLSimplificationException
   */
  private Element resolveVarRefs(StylesheetModule module,
      Element originalElement, Element substituteElement, Resolver resolver,
      int depth) throws XSLToolsException {

    if (XSLConstants.NAMESPACE_URI.equals(substituteElement.getNamespaceURI())) {
      /*
       * resolve most variables in XPath expressions in attributes
       */
      for (int i = 0; i < substituteElement.attributeCount(); i++) {
        Attribute a = substituteElement.attribute(i);
        // If substituteElement is a copy-of,
        // and the attribute value select is a variable that refers to
        // a RTF, replace substituteElement by the
        // RTF (or maybe a stupid holder). Fortunately there
        // is no useAttributeSet issue, then, there is no such attr.
        //
        // TODO: Take care that RTF variables and parameters are
        // not resolved!!! (should be OK now... ...)
        if (Dom4jUtil.isXPathAttribute(a.getQName())) {
          try {
            boolean copyOfStunt = false;

            String attributeName = a.getName();

            // XPathExp xpathExp = module.getCachedXPathExp(elementIdentifier,
            // attributeName);
            // String xpathExpAsString = module.getCachedXPathExpString(
            // elementIdentifier, attributeName);

            XPathExpr xpathExp = module.getCachedXPathExp(attributeName, a
                .getValue(), originalElement, substituteElement);

            Binding b = null;
            if (substituteElement.getName().equals(XSLConstants.ELEM_COPY_OF)
                && xpathExp instanceof /* VariableReference */XPathVarRef) {
              // Perform simplification #10, copy-of-with-RTF-type-var-par stunt
              // TODO: Check that this worx inside for-each..?
              // Probably does -- it should NOT resolve if captured in shim.
              b = XPathVariableResolver.resolve(
                  (/* VariableReference */XPathVarRef) xpathExp, resolver);
              if (b instanceof RTFParameterOrVariableBinding
                  && ((RTFParameterOrVariableBinding) b).isVariableBinding())
                copyOfStunt = true;
            }
            if (copyOfStunt) {
              RTFParameterOrVariableBinding binding = (RTFParameterOrVariableBinding) b;
              substituteRTF(binding, module);
              return null;
            } // not the #10 copy-of stunt
            /*
             * originalElement used as a namespace resolver. Notice, this
             * binding is not bound anywhere. It is used only for the
             * convenience of having variable resolution as a part of resolution
             * behaviour
             */
            /*
             * XPathExpressionParameterOrVariableBinding temp = new
             * XPathExpressionParameterOrVariableBinding( module, a.getValue(),
             * originalElement, Resolver.UNDEFINED_SCOPE,
             * ParameterOrVariableBinding.UNDEFINED_BINDING_TYPE);
             */

            //this.RTFHackAttName = a.getName();
            //this.RTFHackModule = module;
            //this.RTFHackElement = substituteElement;
            XPathExpr newValue = substituteXPath(module, xpathExp,
                originalElement, substituteElement, resolver);
            a.setValue(newValue.toString());
            module.cacheXPathExpression(substituteElement, attributeName,
                newValue);
          } catch (XSLToolsException ex) {
            cesspool.reportError(module, substituteElement,
                ParseLocation.Extent.TAG, ex);
          }
        }
      }

      /*
       * Bind contents of any xsl:variable elements, except those at top level
       * which have been bound already (in fact, doing it once more does no harm
       * but it is confusing!) originalElement is used as a namespace resolver
       * only.
       */
      if (depth > TOPLEVEL_DEPTH) {

        if (XSLConstants.ELEM_VARIABLE.equals(originalElement.getName())) {

          Binding binding = bind(module, substituteElement, originalElement,
              resolver, Resolver.LOCAL_SCOPE,
              ParameterOrVariableBinding.VARIABLE_BINDING_TYPE, cesspool);

          if (binding != null)
            binding.removeAllVariableRefs(resolver, this);

        } else if (XSLConstants.ELEM_PARAM.equals(originalElement.getName())) {
          // this code NOT final / tested / thought over.

          Binding binding = bind(module, substituteElement, originalElement,
              resolver, Resolver.LOCAL_SCOPE,
              ParameterOrVariableBinding.PARAMETER_BINDING_TYPE, cesspool);

          binding.removeAllVariableRefs(resolver, this);
        }
      }
    }
    return substituteElement;
  }

  /**
   * Bind any local attribute set declarations, and resolve any
   * use-attribute-sets (side effecting copies of the attributes referred to) on
   * any element -- except xsl: attribute-set.
   * 
   * @param module -
   *          stylesheet module (for error reporting)
   * @param originalElement -
   *          useful for namespace prefix resolution
   * @param substituteElement -
   *          the element we are working on
   * @param resolver -
   *          resolver for both resolving and binding att sets
   * @param depth -
   *          recursion depth
   * @return - same as substituteElement, but possibly with resolved
   *         xsl:attribute's prepended to child list.
   * @throws XSLSimplificationException
   */
  private Element resolveAttSetRefs(StylesheetModule module,
      Element originalElement, Element substituteElement, Resolver resolver,
      int depth) throws XSLToolsException {
    if (XSLConstants.ELEM_ATTRIBUTE_SET_QNAME
        .equals(originalElement.getQName())) {
      if (depth > TOPLEVEL_DEPTH) {
        // get the name attribute. We might as well have used
        // the substitute element.
        try {
          String name_s = originalElement
              .attributeValue(XSLConstants.ATTR_NAME_QNAME);
          // use originalElement as namespace resolver
          QName name = ElementNamespaceExpander.qNameForXSLAttributeValue(
              name_s, originalElement,
              NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
          AttributeSetBinding binding = new AttributeSetBinding(module,
              substituteElement, Resolver.LOCAL_SCOPE);
          resolver.bind(name, binding);
          binding.removeAllVariableRefs(resolver, this);
        } catch (XSLToolsException ex) {
          cesspool.reportError(module, substituteElement,
              ParseLocation.Extent.TAG, ex);
        }
      }
      // cut short any use-attribute-set resolution
      // on xsl:attribute-set.
      return substituteElement;
    }

    Attribute useAttSets = originalElement
        .attribute(XSLConstants.ATTR_USE_ATTRIBUTE_SETS_QNAME);
    if (useAttSets != null) {
      try {
        AttributeSetBinding loader = new AttributeSetBinding(module,
            substituteElement, Resolver.UNDEFINED_SCOPE);
        loader.removeAllVariableRefs(resolver, this);
        // he he heeee, this is recursive :)
        loader.resolveBehaviour(null, resolver);
        Element element = loader.getElement();
        module.reassignCoreIDs(element);
        useAttSets = element
            .attribute(XSLConstants.ATTR_USE_ATTRIBUTE_SETS_QNAME);
        useAttSets.detach();
        return element;
      } catch (XSLToolsException ex) {
        cesspool.reportError(module, substituteElement,
            ParseLocation.Extent.TAG, ex);
      }
    }
    return substituteElement;
  }

  /**
   * This method is defined in the superclass, but there is no way we can
   * support it properly without access to a <code>Stylesheet</code> instance.
   * Complain on invocation.
   */
  /*
   * protected Node simplify(Node node, StylesheetModule m, Resolver resolver,
   * int depth) { // TODO Auto-generated method stub throw new
   * AssertionError("Should not use this Simplify!"); }
   */
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
       * If a for-each, insert a shim on resolver (this should never happen at
       * top level; as a for-each has nothing to do in there! The shim will
       * intercept any calls to resolve and return a "do not resolve that
       * variable, we are going to turn it into a parameter in a moment" value,
       * plus record the name of the variable that was attempted resolved.
       * Additionally, the parameter or variable binding that should have been
       * resolved can be recovered by a call to resolveLocalScope on the shim.
       */
      if (XSLConstants.ELEM_FOR_EACH_QNAME.equals(originalElement.getQName())
          || XSLConstants.ELEM_COPY_QNAME.equals(originalElement.getQName())) {
        resolver = new ForEachResolverShim(resolver);
      }

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
      substituteElement = resolveVarRefs(module, originalElement,
          substituteElement, resolver, depth);

      if (substituteElement == null)
        return null;
      /*
       * Resolve any use-atttribute-sets Bind any xsl:attribute-set's
       */

      substituteElement = resolveAttSetRefs(module, originalElement,
          substituteElement, resolver, depth);

      if (XSLConstants.ELEM_FOR_EACH_QNAME.equals(substituteElement.getQName())) {
        ForEachResolverShim shim = (ForEachResolverShim) resolver;

        QName name = generateFreshMode();

        substituteElement = generateForEachSubstitute(originalElement,
            substituteElement, shim
                .getUnresolvables(ParameterOrVariableBinding.SYMBOLSPACE),
            name, module, shim.getParent());
      }

      /*
      if (XSLConstants.ELEM_COPY_QNAME.equals(substituteElement.getQName())) {
        ForEachResolverShim shim = (ForEachResolverShim) resolver;

        QName name = generateFreshMode();

        substituteElement = generateCopySubstitute(module, substituteElement,
            shim.getUnresolvables(ParameterOrVariableBinding.SYMBOLSPACE), name);
      }
      */

      if (XSLConstants.ELEM_COPY_OF_QNAME.equals(substituteElement.getQName())) {
        Attribute a = substituteElement
            .attribute(XSLConstants.ATTR_SELECT_QNAME);

        // String elementIdentifier = substituteElement
        // .attributeValue(XMLConstants.ELEMENT_ID_QNAME);

        // if (a.getValue().startsWith("substring(substring-after("))
        // System.err.println(a.getValue());

        XPathExpr xPathExpr = module.getCachedXPathExp(
            XSLConstants.ATTR_SELECT, a.getValue(), originalElement,
            substituteElement);

        Binding bimmer = null;
        boolean skip = false;
        if (xPathExpr instanceof XPathVarRef) {
          // Perform simplification #10, copy-of stunt
          bimmer = XPathVariableResolver.resolve((XPathVarRef) xPathExpr,
              resolver);
          skip = !(bimmer instanceof ParameterOrVariableBinding) || /* (!context.acceptValidatingWithDefaultedToplevelParameters() */
          /* && */((ParameterOrVariableBinding) bimmer).isParameterBinding() /* ) */;
          if (!skip) {
            // this cast should be safe, as result tree fragment
            // variable bindings are eliminated already.
            XPathExpressionParameterOrVariableBinding xbimmer = (XPathExpressionParameterOrVariableBinding) bimmer;
            xPathExpr = xbimmer.getExpression();
          }
        }
        if (!skip) {
          substituteElement = generateCopyOfSubstitute(module, xPathExpr,
              substituteElement);
        }
      }

      if (XSLConstants.ELEM_VARIABLE_QNAME.equals(substituteElement.getQName())
          && SimplifierConfiguration.current.removeVariableDeclarations())
        return null;

      if (XSLConstants.ELEM_ATTRIBUTE_SET_QNAME.equals(substituteElement
          .getQName())
          && SimplifierConfiguration.current.removeAttributeSetDeclarations())
        return null;

      return substituteElement;
    default:
      return (Node) node.clone();
    }
  }

  protected StylesheetModule simplify(StylesheetModule module,
      Stylesheet stylesheet) throws XSLToolsException {

    Document d2 = fac
        .createDocument(simplifyBelow(module
            .getDocumentElement(StylesheetModule.CORE), module,
            stylesheet, 0));
    module.setDocument(d2, StylesheetModule.CORE);
    return module;
  }

  protected StylesheetLevel simplify(Stylesheet stylesheet,
      StylesheetLevel level) throws XSLToolsException {

    level.removeAllVariableRefs(stylesheet, this);

    for (StylesheetModule module : level.contents()) {
      simplify(module, stylesheet);
    }

    for (StylesheetLevel imported : level.imports()) {
      simplify(stylesheet, imported);
    }
    return level;
  }

  public void process(Stylesheet stylesheet) throws XSLToolsException {
    simplify(stylesheet, stylesheet.getPrincipalLevel());
  }
}
