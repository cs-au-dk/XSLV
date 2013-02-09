/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-21
 */
package dongfang.xsltools.simplification;

import java.util.Iterator;
import java.util.ListIterator;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.QName;

import dongfang.XMLConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.UniqueNameGenerator;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathPathExpr;
import dongfang.xsltools.xpath2.XPathUnionExpr;

/**
 * TODO: Consider whether this could be moved into an other Simplifier (should
 * be possible!) TODO: Consider whether generated templates can be safely
 * inserted into the main Stylesheet, possibly with altered priorities
 * (combining this Simplifier with the template extractor)
 * 
 * Simplifications #12, 18: - Copies named AND matched templates into one of
 * each - Splits templates with union match patterns into templates each with
 * one subpattern of the union () - Binds named templates in top level resolver
 * 
 * @author dongfang
 */

public class TemplateSimplifier implements StylesheetProcessor {
  private ErrorReporter cesspool;

  private UniqueNameGenerator names;

  public static TemplateSimplifier getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new TemplateSimplifier(cesspool, names);
  }

  private TemplateSimplifier(ErrorReporter cesspool, UniqueNameGenerator names) {
    this.cesspool = cesspool;
    this.names = names;
  }

  private QName getFreshModeName() {
    String localName = names.getFreshId(XMLConstants.MODE_ENUMERATION_FORMAT);
    QName qname = QName.get("id" + localName,
        XMLConstants.TEMPLATE_MODE_NAMESPACE);
    // The default rule is not needed -- it is never hit anyway.
    // stylesheet.addDefaultModedTemplate(qname);
    return qname;
  }

  protected void splitUnionMatch(StylesheetModule module, Element element,
      ListIterator iter) throws XSLToolsXPathException {

    // If match expressions is a union, split template:
    Attribute matchAtt = element.attribute(XSLConstants.ATTR_MATCH_QNAME);

    if (matchAtt == null) {
      element.addAttribute(XSLConstants.ATTR_PRIORITY, "0");
      return;
    }

    try {

      XPathExpr xPathExp = module.getCachedXPathExp(XSLConstants.ATTR_MATCH,
          matchAtt.getValue(), element, element);

      if (xPathExp instanceof XPathPathExpr)
        Dom4jUtil.computePriority(element, xPathExp);
      else {

        if (xPathExp instanceof XPathUnionExpr) {
          // LinkedList<XPathExp> subExps = ((NodeSetUnion)
          // xPathExp).deUnionize();
          Iterator<XPathExpr> subExps = ((XPathUnionExpr) xPathExp).iterator();

          xPathExp = subExps.next();// getFirst();
          matchAtt.setValue(xPathExp.toString());

          Element clone = (org.dom4j.Element) element.clone();

          Dom4jUtil.computePriority(element, xPathExp);

          module.cacheXPathExpression(element, XSLConstants.ATTR_MATCH,
              xPathExp);

          // Insert copies of the template for the rest of the union
          // expressions:
          // if (subExps.size() > 1) {
          if (subExps.hasNext()) {
            // Iterator<XPathExpr> iter2 = subExps.iterator();
            // iter2.next(); // First expression has already been handled.
            Element clone2 = null;

            while (subExps.hasNext()) {
              xPathExp = subExps.next();// iter2.next();
              // Clone template and set match expression:
              module.reassignCoreIDs(clone);
              clone.addAttribute(XSLConstants.ATTR_MATCH, xPathExp.toString());
              module.cacheXPathExpression(clone, XSLConstants.ATTR_MATCH,
                  xPathExp);
              // if (iter2.hasNext())
              if (subExps.hasNext())
                clone2 = (org.dom4j.Element) clone.clone();
              Dom4jUtil.computePriority(clone, xPathExp);
              if (SimplifierConfiguration.current.addTransformOriginComments()) {
                clone.addProcessingInstruction("simplification",
                    "union-match-split");
              }
              iter.add(clone);
              clone = clone2;
            }
          }
        } else {
          throw new XSLToolsXPathException(
              "Match pattern is neither a NodeSetLocationPath nor a Union");
        }
      }
    } catch (XSLToolsXPathException ex) {
      try {
        cesspool.reportError(module, element, ParseLocation.Extent.TAG, ex);
      } catch (XSLToolsException ex2) {
        throw (XSLToolsXPathException) ex2;
      }
    }
  }

  protected void process(StylesheetModule module, Resolver resolver)
      throws XSLToolsException {
    org.dom4j.Element documentElement = module
        .getDocumentElement(StylesheetModule.CORE);

    /*
     * Simplification #8 could go here....
     */

    /**
     * Top level iteration: Copy template rules with both <code>name</code>
     * and <code>match</code> attributes. Original loses name attribute and
     * copy loses match attribute.
     */
    for (ListIterator it = documentElement.elements().listIterator(); it
        .hasNext();) {

      org.dom4j.Element element = (Element) it.next();

      if (element.getQName().equals(XSLConstants.ELEM_TEMPLATE_QNAME)) {
        try {
          Attribute matchAtt = element.attribute(XSLConstants.ATTR_MATCH_QNAME);
          Attribute nameAtt = element.attribute(XSLConstants.ATTR_NAME_QNAME);

          if (nameAtt != null) // it's got to go in all cases
            nameAtt.detach();

          if (matchAtt != null) // split match
            splitUnionMatch(module, element, it);

          if (nameAtt != null) {
            QName bindName = ElementNamespaceExpander
                .qNameForXSLAttributeValue(nameAtt.getValue(), element,
                    NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
            QName freshMode = getFreshModeName();
            resolver.bind(bindName, new TemplateBinding(module, freshMode));

            if (matchAtt != null) { // the original element is already
              // used once in the document; we have to clone it for more usage
              element = (Element) element.clone();
              if (SimplifierConfiguration.current.addTransformOriginComments()) {
                element.addProcessingInstruction("simplification",
                    "call-template-copy");
              }
              module.reassignCoreIDs(element);
              it.add(element);
            }

            // make new moded template that matches anything that self::node()
            // could be..
            // that is children, attrributes and root.
            element.addAttribute(XSLConstants.ATTR_MATCH,
                "child::node()|attribute::*|/");
            Dom4jUtil.transferAttributeValue(freshMode, XSLConstants.ATTR_MODE,
                element);
            element.addAttribute(XSLConstants.ATTR_PRIORITY, "0");
            splitUnionMatch(module, element, it);
          }
        } catch (XSLToolsException ex) {
          cesspool.reportError(module, element, ParseLocation.Extent.TAG, ex);
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see dongfang.xsltools.simplification.StylesheetProcessor#process(dongfang.xsltools.simplification.ImportPrecedenceGroup)
   */
  public void process(Stylesheet stylesheet, StylesheetLevel level)
      throws XSLToolsException {
    for (StylesheetModule m : level.contents()) {
      process(m, level);
    }
    for (StylesheetLevel g : level.imports()) {
      process(stylesheet, g);
    }
  }

  public void process(Stylesheet stylesheet) throws XSLToolsException {
    process(stylesheet, stylesheet.getPrincipalLevel());
    if (simplifyStylesheetProper)
      process(stylesheet, stylesheet);
  }
}
