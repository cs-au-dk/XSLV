/*
 *  dongfang M. Sc. Thesis
 * Created on 2005-02-25
 */
package dongfang.xsltools.simplification;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;

import dongfang.XMLConstants;
import dongfang.XPathConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLocatableException;
import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.UniqueNameGenerator;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathStringLiteral;

/**
 * Currently, this works by duplicating. The original is not modified. This
 * could be altered to just manipulate the original .. but not until after this
 * works :) (there is some danger of complexity lurking!) (in fact, terrible)
 * (and if performs great even with copying).
 * 
 * Presently, this simplifier does the following: - Restructure if to choose -
 * Add empty otherwise branches to choose - Merge adjacent text nodes. If at
 * least one contains some non-whitespace, instead output an xsl:value-of with
 * the merged string. - Convert xsl:text into xsl:value-of. Merging of
 * consecutive value-of nodes (Achtung! remember disable-output-escaping) is
 * better done later. - Binds top level declarations in environment - Binds top
 * level attribute set declarations in environment - Cooks attribute value
 * templates to sequence constructors (?) - Converts literal elements to
 * xsl:element constructors
 * 
 * @author dongfang
 */
public class RestructuringSimplifier extends StructureCopyingSimplifierBase {

  protected static RestructuringSimplifier getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new RestructuringSimplifier(cesspool);
  }

  private RestructuringSimplifier(ErrorReporter cesspool) {
    this.cesspool = cesspool;
  }

  private ErrorReporter cesspool;

  private Element fuskLastElement;

  private static Element simplifyIf(StylesheetModule module,
      Element originalElement, Element substituteElement) {
    // Cache-wise: Name changed, but ID and expression unchenged
    substituteElement.setQName(QName.get(XSLConstants.ELEM_WHEN,
        originalElement.getNamespace()));
    // make "choose" parent for when
    // Cache-wise: No expressions on new element.
    Element wedge = fac.createElement(QName.get(XSLConstants.ELEM_CHOOSE,
        originalElement.getNamespace()));
    module.addCoreElementId(wedge);
    wedge.add(substituteElement);
    return simplifyChoose(module, originalElement, wedge);
  }

  private static Element simplifyChoose(StylesheetModule module,
      Element originalElement, Element substituteElement) {
    // TODO: testen skal ogsÃ¥ omfatte NS
    boolean hasOtherwise = false;
    for (Iterator iter = substituteElement.elementIterator(); iter.hasNext()
        && !hasOtherwise;) {
      Element n = (Element) iter.next();
      if (n.getQName().equals(XSLConstants.ELEM_OTHERWISE_QNAME))
        hasOtherwise = true;
    }
    if (!hasOtherwise) {
      // There is no otherwise branch. Add one.
      // This goes post recursion in order to get the
      // otherwise child node last in the list.
      Element ow = fac.createElement(XSLConstants.ELEM_OTHERWISE_QNAME);
      module.addCoreElementId(ow);
      substituteElement.add(ow);
    }
    return substituteElement;
  }

  private Element bindToplevelParVarDecl(Element originalElement,
      Element substituteElement, short type, StylesheetModule module,
      Resolver resolver) throws XSLToolsException {
    bind(module, substituteElement, originalElement, resolver,
        Resolver.TOPLEVEL_SCOPE, type, cesspool);
    return substituteElement;
  }

  private Element bindToplevelAttributeSetDecl(Element originalElement,
      Element substituteElement, StylesheetModule module, Resolver resolver)
      throws XSLToolsException {
    String name_s = originalElement
        .attributeValue(XSLConstants.ATTR_NAME_QNAME);
    try {
      QName name = ElementNamespaceExpander.qNameForXSLAttributeValue(name_s,
          originalElement, NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
      AttributeSetBinding binding = new AttributeSetBinding(module,
          substituteElement, Resolver.TOPLEVEL_SCOPE);
      resolver.bind(name, binding);
    } catch (XSLToolsException ex) {
      cesspool.reportError(module, substituteElement, ParseLocation.Extent.TAG,
          ex);
    }
    return substituteElement;
  }

  private Element bindToplevelKeyDecl(Element originalElement,
      Element substituteElement, StylesheetModule module, Resolver resolver)
      throws XSLToolsException {

    String s_match = substituteElement
        .attributeValue(XSLConstants.ATTR_MATCH_QNAME);
    String s_use = substituteElement
        .attributeValue(XSLConstants.ATTR_USE_QNAME);
    String name = substituteElement
        .attributeValue(XSLConstants.ATTR_NAME_QNAME);

    QName qname = ElementNamespaceExpander.qNameForXSLAttributeValue(name,
        originalElement, NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);

    XPathExpr match = module.getCachedXPathExp(XSLConstants.ATTR_MATCH,
        s_match, originalElement, substituteElement);

    XPathExpr use = module.getCachedXPathExp(XSLConstants.ATTR_USE, s_use,
        originalElement, substituteElement);

    KeyBinding binding = new KeyBinding(module, match, use);

    resolver.bind(qname, binding);

    return substituteElement;
  }

  protected Element simplifyTextElement(StylesheetModule module,
      Element textElement) {
    String text = textElement.getText(); // without trim !!!

    Element substituteElement =
    // fac.createElement(XSLConstants.ELEM_VALUE_OF_QNAME);
    XPathConstants.createSimplifiedTextLiteral(module, textElement, text, true);

    module.addCoreElementId(substituteElement);

    // substituteElement.addAttribute(XSLConstants.ATTR_SELECT, "'" +
    // text.replaceAll("'", "&apos;").replaceAll("\"", "&quot;") + "'");
    if ((text = textElement
        .attributeValue(XSLConstants.ATTR_DISABLE_OUTPUT_ESCAPING_QNAME)) != null)
      substituteElement.addAttribute(XSLConstants.ATTR_DISABLE_OUTPUT_ESCAPING,
          text);
    return substituteElement;
  }

  /**
   * A simple way this could have been done would be: Trigger XPath mode on {.
   * At beginning of XPath mode parse, emit a value-of select='{' if string
   * begins w }.
   * 
   * @param value
   * @return
   */
  private List<Element> attValueTemplate2Content(StylesheetModule module,
      Element element, Element originalElement, String value)
      throws XSLToolsException {
    LinkedList<Element> content = new LinkedList<Element>();
    while (value.length() > 0) {
      boolean hasXPathExpression = false;
      int templateIndex = value.indexOf('{');
      // found some curly brace
      if (templateIndex >= 0) {
        if (value.length() > templateIndex + 1
            && value.charAt(templateIndex + 1) == '{') {
          // it's double, do not trigger expression start but replace by
          // single...
          // content.add(fac.createText(value.substring(0, templateIndex+1)));
          Element valueof = XPathConstants.createSimplifiedTextLiteral(module,
              element, value.substring(0, templateIndex + 1), true);
          module.addCoreElementId(valueof);
          content.add(valueof);
          value = value.substring(templateIndex + 2, value.length());
        } else { // it's an expression start, first get rid of the text before
          // it
          hasXPathExpression = true;
          if (templateIndex == 0) {
            value = value.substring(1, value.length());
          } else if (templateIndex > 0) {
            // content.add(fac.createText(value.substring(0, templateIndex)));
            Element valueof = XPathConstants.createSimplifiedTextLiteral(
                module, element, value.substring(0, templateIndex), true);
            module.addCoreElementId(valueof);
            content.add(valueof);
            value = value.substring(templateIndex + 1, value.length());
          }
        }
      }
      if (!hasXPathExpression) {
        int doubleend = value.indexOf("}}");
        if (doubleend < 0) {
          Element valueof = XPathConstants.createSimplifiedTextLiteral(module,
              element, value, true);
          module.addCoreElementId(valueof);
          content.add(valueof);
          value = "";
        } else {
          Element valueof = XPathConstants.createSimplifiedTextLiteral(module,
              element, value.substring(0, doubleend + 1), true);
          content.add(valueof);
          module.addCoreElementId(valueof);
          value = value.substring(doubleend + 2);
        }
      }
      // Convert template XPath to xsl:value-of instruction:
      else if (value.length() > 0) {
        // Fetch xpath:
        int templateEndIndex = value.indexOf('}');
        if (templateEndIndex >= 0) {

          String xpath = value.substring(0, templateEndIndex);

          value = value.substring(templateEndIndex + 1, value.length());
          /*
           * XPathExp asXPath = XPathExp.parse(xpath);
           * XPathExp.applyVisitor(asXPath, new
           * XPathNormalizer(XPathExp.TYPE_STRING, cesspool)); // Add
           * <xsl:value-of>:
           */
          // DONE: Cache XPath.
          // valueOfElm.addAttribute(XSLConstants.ATTR_SELECT, xpath);
          // TODO: Simplify XPath
          // TODO: Might buggi here.
          // Something with cache inconsistency.
          //
          Element valueof = XPathConstants.createSimplifiedTextLiteral(module,
              element, xpath, false);
          module.addCoreElementId(valueof);
          content.add(valueof);

          XPathExpr p = module.getCachedXPathExp(XSLConstants.ATTR_SELECT,
              xpath, originalElement, valueof);

          valueof.addAttribute(XSLConstants.ATTR_SELECT, p.toString());

          String eid = element.attributeValue(XMLConstants.ELEMENT_ID_QNAME);

          if (eid != null) // should never be null in fact...
            valueof.addAttribute(XMLConstants.ELEMENT_ID_QNAME, eid);

        }
      } else {
        Element valueof = XPathConstants.createSimplifiedTextLiteral(module,
            element, value, true);
        module.addCoreElementId(valueof);
        content.add(valueof);
        value = "";
      }
    }
    return content;
  }

  private List<Element> cookDesugaredAttributeValueTemplates(
      Element originalElement, StylesheetModule module, List<Element> avts)
      throws XSLToolsXPathException {
    List<Element> result = new LinkedList<Element>();
    String glob = "";
    for (Element element : avts) {
      Attribute select = element.attribute(XSLConstants.ATTR_SELECT_QNAME);
      XPathExpr exp = module.getCachedXPathExp(XSLConstants.ATTR_SELECT, select
          .getValue(), originalElement, element);
      if (exp instanceof XPathStringLiteral) {
        XPathStringLiteral sconst = (XPathStringLiteral) exp;
        glob += sconst.getSweetContent();
      } else {
        // flush out the glob and the expression here
        if (!glob.equals("")) {
          Element valueof = XPathConstants.createSimplifiedTextLiteral(module,
              element, glob, true);
          module.addCoreElementId(valueof);
          result.add(valueof);
          glob = "";
        }
        result.add(element);
      }
    }
    if (!glob.equals("")) {
      Element valueof = XPathConstants.createSimplifiedTextLiteral(module,
          fuskLastElement, glob, true);
      module.addCoreElementId(valueof);
      result.add(valueof);
    }
    return result;
  }

  /**
   * Basically we need just to make sure it won't crash on attribute value
   * templates. Right now, this code does (almost) no good -- it merely strips
   * off prefixes, if there is no namespace attribute. Due to tolerance in
   * Dom4J, AVTs get through this alive to tell the tale. Naaaah -- kill it off.
   * Bang.
   * 
   * @param module
   * @param elementConstructor
   * @return
   */
  protected void simplifyElementConstructorElement(StylesheetModule module,
      Element elementConstructor, Element nsResolver)
      throws XSLToolsXPathUnresolvedNamespaceException {
    /*
     * String qname_s = elementConstructor
     * .attributeValue(XSLConstants.ATTR_NAME_QNAME); QName qname =
     * Dom4jUtil.qNameForXSLAttributeValue(qname_s, nsResolver,
     * Dom4jUtil.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE); if
     * (elementConstructor.attribute(XSLConstants.ATTR_NAMESPACE_QNAME) != null)
     * ;// elementConstructor.addAttribute(XSLConstants.ATTR_NAME, //
     * qname.getName()); else
     * {elementConstructor.addAttribute(XSLConstants.ATTR_NAMESPACE, qname
     * .getNamespaceURI()); }
     * elementConstructor.addAttribute(XSLConstants.ATTR_NAME, qname.getName());
     */
  }

  protected Element simplifyLiteralElement(StylesheetModule module,
      Element literalElement, Element originalElement) throws XSLToolsException {

    Element substituteElement = fac.createElement(XSLConstants.ELEM_ELEMENT_QNAME);

    substituteElement.addAttribute(XSLConstants.ATTR_NAME, literalElement
        .getName());/* Qualified */
    substituteElement.addAttribute(XSLConstants.ATTR_NAMESPACE, literalElement
        .getNamespaceURI());

    for (Iterator it = literalElement.attributeIterator(); it.hasNext();) {
      Attribute attribute = (Attribute) it.next();
      // do not make attribute constructors for own decoration (parse
      // location etc.) attributes
      if (!attribute.getNamespaceURI().equals(
          XMLConstants.PARSELOCATION_DECORATION_NAMESPACE.getURI())) {
        if (attribute.getNamespaceURI().equals(XSLConstants.NAMESPACE_URI)) {
          // xsl NS attributes on element .. do not translate; add on result
          // directly, though
          // stripping off the XSL NS!
          substituteElement.addAttribute(attribute.getName(), attribute
              .getValue());
        } else {
          Element attributeAsElement = fac
              .createElement(XSLConstants.ELEM_ATTRIBUTE_QNAME);
          module.addCoreElementId(attributeAsElement);
          // getQualifiedName should work too, and be a little nicer (can hint a
          // prefix)
          // attributeAsElement.addAttribute(XSLConstants.ATTR_NAME,
          // attribute.getName());
          attributeAsElement.addAttribute(XSLConstants.ATTR_NAME, attribute
              .getQualifiedName());
          // quirk in XSL specification: Attribute in no-NS country <==> no
          // expansion by default NS binding.
          if (!(attribute.getNamespaceURI().equals(Namespace.NO_NAMESPACE
              .getURI())))
            attributeAsElement.addAttribute(XSLConstants.ATTR_NAMESPACE,
                attribute.getNamespaceURI());
          String value = attribute.getValue();
          // value = value.replaceAll("'", "&apos;").replaceAll("\"", "&quot;");

          // int curlyIndex;

          try {
            if ((/*curlyIndex = */value.indexOf('{')) >= 0) {
              attributeAsElement
                  .setContent(cookDesugaredAttributeValueTemplates(
                      originalElement, module, attValueTemplate2Content(module,
                          literalElement, originalElement, value)));
            } else {
              // Cache-wise: New uncached expression
              Element valueof = XPathConstants.createSimplifiedTextLiteral(
                  module, literalElement, value, true);
              module.addCoreElementId(valueof);
              attributeAsElement.add(valueof);
            }
            substituteElement.add(attributeAsElement);
          } catch (XSLToolsException ex) {
            cesspool.reportError(module, literalElement,
                ParseLocation.Extent.TAG, ex);
          }
        }
      } else
        substituteElement.add((Attribute) attribute.clone());
    }

    module.addCoreElementId(substituteElement);

    for (Iterator it = literalElement.nodeIterator(); it.hasNext();) {
      Node node = (Node) it.next();
      substituteElement.add((Node) node.clone());
    }
    simplifyElementConstructorElement(module, substituteElement, literalElement);
    return substituteElement;
  }

  private StringBuilder allTextNodesContents = new StringBuilder();

  @Override
protected Node simplify(Node originalNode, StylesheetModule module,
      Resolver resolver, int depth, boolean lastNode) throws XSLToolsException {

    Node rabbit = null;

    if (originalNode.getNodeType() == Node.TEXT_NODE) {
      allTextNodesContents.append(originalNode.getText());
    }
    /*
     * if at last child node, or at a non-text node (element, comment, whatever)
     * check status of the sequence of text nodes we have picked up so far. If
     * all-whitespace, insert at result -- before normal translation result -- a
     * text node with the concatenation of it all. If some non-whitespace,
     * insert a value-of instruction.
     */
    if (lastNode || originalNode.getNodeType() != Node.TEXT_NODE) {

      String s_allTextNodesContents = allTextNodesContents.toString();
      allTextNodesContents = new StringBuilder();

      String zapd = s_allTextNodesContents.trim();

      if (zapd.equals("")) {
        // no non-whitespace
        if (!s_allTextNodesContents.equals("")) // not empty
          if (SimplifierConfiguration.current
              .getWhitespaceOnlyTextNodeBevaviour() == SimplifierConfiguration.WhitespaceNodeBehaviour.PRESERVE)
            textAdditionQueue = fac.createText(s_allTextNodesContents);
        // as an alternative, one could kill them altogether, and format all
        // serialized output.
      } else {
        // Cache-wise: new uncached expression
        Element vof = XPathConstants.createSimplifiedTextLiteral(module,
            fuskLastElement, s_allTextNodesContents, true);
        module.addCoreElementId(vof);
        rabbit = vof;
      }
    }

    if (lastNode && rabbit != null) {
      textAdditionQueue = rabbit;
      rabbit = null;
    }

    switch (originalNode.getNodeType()) {
    case Node.ELEMENT_NODE:
      Element originalElement = (Element) originalNode;

      fuskLastElement = originalElement;

      // this should simplify "everything below"

      Element substituteElement = simplifyBelow(originalElement, module,
          resolver, depth);

      if (rabbit != null) {
        textAdditionQueue = rabbit;
        rabbit = null;
      }

      if (XSLConstants.ELEM_TEXT_QNAME.equals(originalElement.getQName()))
        return simplifyTextElement(module, originalElement);

      if (XSLConstants.ELEM_IF_QNAME.equals(substituteElement.getQName()))
        return simplifyIf(module, originalElement, substituteElement);

      if (XSLConstants.ELEM_CHOOSE_QNAME.equals(substituteElement.getQName()))
        return simplifyChoose(module, originalElement, substituteElement);

      if (depth == TOPLEVEL_DEPTH
          && XSLConstants.ELEM_VARIABLE_QNAME.equals(substituteElement
              .getQName())) {
        try {
          // Dom4jUtil.prettyPrint(substituteElement);
          return bindToplevelParVarDecl(originalElement, substituteElement,
              ParameterOrVariableBinding.VARIABLE_BINDING_TYPE, module,
              resolver);
        } catch (XSLToolsException ex) {
          cesspool.reportError(module, originalElement,
              ParseLocation.Extent.TAG, ex);
          return substituteElement;
        }
      } else if (depth == TOPLEVEL_DEPTH
          && XSLConstants.ELEM_PARAM_QNAME.equals(substituteElement.getQName())) {
        try {
          return bindToplevelParVarDecl(originalElement, substituteElement,
              ParameterOrVariableBinding.PARAMETER_BINDING_TYPE, module,
              resolver);
        } catch (XSLToolsException e) {
          cesspool.reportError(e);
          return substituteElement;
        }
      } else if (depth == TOPLEVEL_DEPTH
          && XSLConstants.ELEM_ATTRIBUTE_SET_QNAME.equals(substituteElement
              .getQName())) {
        try {
          return bindToplevelAttributeSetDecl(originalElement,
              substituteElement, module, resolver);
        } catch (XSLToolsLocatableException e) {
          cesspool.reportError(e);
          return substituteElement;
        }
      } else if (depth == TOPLEVEL_DEPTH
          && XSLConstants.ELEM_KEY_QNAME.equals(substituteElement.getQName())) {
        try {
          return bindToplevelKeyDecl(originalElement, substituteElement,
              module, resolver);
        } catch (XSLToolsLocatableException e) {
          cesspool.reportError(e);
          return substituteElement;
        }
      }
      /*
       * else if (depth == TOPLEVEL_DEPTH &&
       * XSLConstants.ELEM_TEMPLATE_QNAME.equals(substituteElement .getQName())) {
       * String s_modes =
       * substituteElement.attributeValue(XSLConstants.ATTR_MODE_QNAME); boolean
       * first = true; if (s_modes!=null) s_modes = s_modes.trim(); else s_modes =
       * "#default"; for (StringTokenizer st = new StringTokenizer(s_modes, "
       * \n\t"); st.hasMoreTokens();) { String s_mode = st.nextToken(); if
       * (first) { substituteElement.addAttribute(XSLConstants.ATTR_MODE,
       * s_mode); //toplevelAdditionQueue.add(substituteElement); first = false; }
       * else { Element clone = (Element)substituteElement.clone();
       * clone.addAttribute(XSLConstants.ATTR_MODE, s_mode);
       * module.reassignCoreIDs(clone); toplevelAdditionQueue.add(clone); } }
       * return substituteElement; }
       */
      else if (!XSLConstants.NAMESPACE_URI.equals(substituteElement
          .getNamespaceURI())) {
        return simplifyLiteralElement(module, substituteElement,
            originalElement);
      } else if (XSLConstants.ELEM_ELEMENT_QNAME.equals(substituteElement
          .getQName())) {
        simplifyElementConstructorElement(module, substituteElement,
            originalElement);
      }
      // nada to do
      return substituteElement;
    case Node.TEXT_NODE:
      // text nodes are processed by mechanism above
      return null;
    case Node.NAMESPACE_NODE:
      Namespace n2 = (Namespace) originalNode;
      Node n3 = fac.createNamespace(n2.getPrefix(), n2.getURI());
      return n3;
    case Node.PROCESSING_INSTRUCTION_NODE:
    case Node.COMMENT_NODE:
      return (Node) originalNode.clone();
    case Node.CDATA_SECTION_NODE:
      Node tn = fac.createText(originalNode.getStringValue());
      return simplify(tn, module, resolver, depth, lastNode);
    default:
      return (Node) originalNode.clone();
    }
  }

  public void process(StylesheetLevel original) throws XSLToolsException {
    // ImportPrecedenceGroup result = new ImportPrecedenceGroup();
    for (StylesheetModule module : original.contents()) {
      try {
        simplify(original, module);
      } catch (XSLToolsLocatableException ex) {
        cesspool.reportError(ex);
      }
    }

    for (StylesheetLevel group : original.imports()) {
      try {
        process(group);
      } catch (XSLToolsLocatableException ex) {
        cesspool.reportError(ex);
      }
    }
  }

  public void process(Stylesheet stylesheet) throws XSLToolsException {
    process(stylesheet.getPrincipalLevel());
    if (simplifyStylesheetProper)
      simplify(null, stylesheet);
  }
}
