/*
 * dongfang M. Sc. Thesis
 * Created on 2005-02-24
 */
package dongfang.xsltools.simplification;

import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Element;

import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.UniqueNameGenerator;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathNormalizer;

/**
 * Carry out simplifications that do not need any context information, and do
 * not restructure the DOM tree. The simplifications are done just by writing
 * alterations into the tree (OK - addition of an attribute is not considered a
 * restructuring!) - Adds default select to
 * apply-templates if missing (and also to apply-imports -- ewww) 
 * - Expands simplified XPath and overwrites 
 * 
 * @author dongfang
 */
public class ContextFreeSimplifier /* extends StructuralSimplifierBase */
implements StylesheetProcessor {
  
  private ErrorReporter cesspool;
  private boolean insideToplevelVarOrPar;

  public static ContextFreeSimplifier getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new ContextFreeSimplifier(cesspool, names);
  }

  private ContextFreeSimplifier(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    this.cesspool = cesspool;
  }

  private void simplifyBelow(Element originalElement, Stylesheet stylesheet,
      StylesheetModule module, int depth) throws XSLToolsException {
    for (Iterator it = originalElement.elementIterator(); it.hasNext();) {
      Element childElement = (Element) it.next();
      simplify(childElement, stylesheet, module, depth + 1);
    }
  }

  protected void simplify(Element element, Stylesheet stylesheet,
      StylesheetModule module, int depth) throws XSLToolsException {

    if (depth == 1)
      insideToplevelVarOrPar = XSLConstants.ELEM_VARIABLE_QNAME.equals(element
          .getQName())
          || XSLConstants.ELEM_PARAM_QNAME.equals(element.getQName());

    simplifyBelow(element, stylesheet, module, depth);

    for (int i = 0; i < element.attributeCount(); i++) {
      Attribute a = element.attribute(i);

      if (Dom4jUtil.isXPathAttribute(a.getQName())) {
        /*
         * Do the top-level var or par XPath normalization: . becomes /, etc.
         */
        try {
          XPathExpr exp = module.getCachedXPathExp(a.getName(), a.getValue(),
              element, element);
          if (insideToplevelVarOrPar) {
            exp = XPathNormalizer.toplevelStuntz(exp);
            module.cacheXPathExpression(element, a.getName(), exp);
          }
          a.setValue(exp.toString());
        } catch (XSLToolsXPathException ex) {
          cesspool.reportError(module, element, ParseLocation.Extent.TAG, ex);
        }
      }
    }

    if (element.getNamespaceURI().equals(XSLConstants.NAMESPACE_URI)) {

      /*
       * run simplification #3 -- dump default select attribute
       */
      if (element.getName().equals(XSLConstants.ELEM_APPLY_TEMPLATES)) {
        if (element.attribute(XSLConstants.ATTR_SELECT_QNAME) == null)
          element.addAttribute(XSLConstants.ATTR_SELECT, "child::node()");
        if (element.attribute(XSLConstants.ATTR_MODE_QNAME) == null)
          element.addAttribute(XSLConstants.ATTR_MODE, "#default");

        // TODO : Adding a select to an apply-imports actually perverts the simplified XSLT.
        // Should not be here.
      } else if (element.getName().equals(XSLConstants.ELEM_APPLY_IMPORTS)) {
        element.addAttribute(XSLConstants.ATTR_SELECT, "self::node()");
      }
      if (element.getName().equals(XSLConstants.ELEM_NEXT_MATCH)) {
        element.addAttribute(XSLConstants.ATTR_SELECT, "self::node()");
      }
    }

    if (depth == 1)
      insideToplevelVarOrPar = false;
  }

  private void simplify(Stylesheet stylesheet, StylesheetModule module)
      throws XSLToolsException {
    simplify(module.getDocumentElement(StylesheetModule.CORE),
        stylesheet, module, 0);
  }

  private void process(Stylesheet stylesheet, StylesheetLevel level)

  throws XSLToolsException {
    for (StylesheetModule module : level.contents()) {
      // currentModule = module;
      simplify(stylesheet, module);
    }
    for (StylesheetLevel imported : level.imports())
      process(stylesheet, imported);
  }

  /*
   * Simplifier implementation
   */
  public void process(Stylesheet stylesheet) throws XSLToolsException {
    process(stylesheet, stylesheet.getPrincipalLevel());
    if (simplifyStylesheetProper)
      simplify(stylesheet, stylesheet);
  }
}
