/*
 * dongfang M. Sc. Thesis
 * Created on 2005-02-28
 */
package dongfang.xsltools.simplification;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;

import dongfang.XSLConstants;
import dongfang.xslt.XSLT;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.model.ParseLocationDocumentFactory;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.xpath2.XPathAbsolutePathExpr;
import dongfang.xsltools.xpath2.XPathAxisStep;
import dongfang.xsltools.xpath2.XPathBase;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathPathExpr;
import dongfang.xsltools.xpath2.XPathStepExpr;
import dongfang.xsltools.xpath2.XPathStringLiteral;

/**
 * Recursion support base class for copying tree traversing simplifiers.
 * 
 * @author dongfang
 */
public abstract class StructureCopyingSimplifierBase implements
    StylesheetProcessor {

  /*
   * Whenever copying elements, the factory or clone (for subtrees) should be
   * used. Somehow I have the impression that recursive copying simplifications
   * are to be preferred ofer clone -- with clone, it is hard to keep track of
   * which chunks of xsl that are passed unprocessed.
   */

  static ParseLocationDocumentFactory fac = ParseLocationDocumentFactory
      .getPLInstance();

  static final int TOPLEVEL_DEPTH = 1;

  static final XPathExpr EMPTYSTRING;

  static {
    try {
      // EMPTYSTRING = XPathExp.parse("''");
      EMPTYSTRING = new XPathStringLiteral();
    } catch (Exception e) {
      throw new AssertionError(
          "Error in static initializer -- should never happen");
    }
  }

  List<Node> toplevelAdditionQueue = new LinkedList<Node>();

  List<Node> locallevelAdditionQueue = new LinkedList<Node>();

  Node textAdditionQueue = null;

  protected void copyAttributes(Element e, Element e2) {
    for (int i = 0; i < e.attributeCount(); i++) {
      Attribute a = e.attribute(i);
      Attribute a2 = (Attribute) a.clone();// fac.createAttribute(e,
      // a.getQName(), a.getValue());
      e2.add(a2);
    }
  }

  /**
   * Simplify a single Node, returning the simplified Node. It must be a clone
   * or a fresh node -- detaching the original from its tree and returning it is
   * not permitted (we do not want to risk any complexity issues stemming from
   * altering the source structure which we traverse)
   * 
   * @param node -
   *          node to be simplified
   * @param m -
   *          stylesheet module of the node (for error reporting)
   * @param resolver -
   *          resolver holding current bindings
   * @param depth -
   *          current recursion depth (in source)
   * @return - fresh, simplified node
   * @throws XSLSimplificationException
   */
  protected abstract Node simplify(Node node, StylesheetModule module,
      Resolver resolver, int depth, boolean lastNode) throws XSLToolsException;

  /**
   * Create a clone of an element, with all descendants simplified. The type of
   * the primary object of processing here is Element, not Node, because Nodes
   * in general have no descendants anyway.
   * 
   * @param originalElement -
   *          element to be cloned
   * @param m -
   *          stylesheet module (for error reporting)
   * @param resolver -
   *          resolver holding anything in scope
   */
  protected Element simplifyBelow(Element originalElement,
      StylesheetModule module, Resolver resolver, int depth)
      throws XSLToolsException {
    /*
     * Clone element
     */
    Element substituteElement = fac.cloneElement(originalElement);
    copyAttributes(originalElement, substituteElement);

    /*
     * Recurse
     */
    ListIterator it = originalElement.content().listIterator();
    while (it.hasNext()) {
      Node childNode = (Node) it.next();
      Node simp = simplify(childNode, module, resolver, depth + 1, !it
          .hasNext());
      clearAdditionQueues(substituteElement, simp, depth);
    }
    return substituteElement;
  }

  void clearAdditionQueues(Element simplifiedParent, Node simplifiedNullable,
      int depth) {
    if (textAdditionQueue != null) {
      simplifiedParent.add(textAdditionQueue);
      textAdditionQueue = null;
    }
    if (simplifiedNullable != null) {
      simplifiedParent.add(simplifiedNullable);
    }
    if (!toplevelAdditionQueue.isEmpty() && depth == 0) {
      for (Node addOn : toplevelAdditionQueue) {
        simplifiedParent.add(addOn);
      }
      toplevelAdditionQueue.clear();
    }
    if (!locallevelAdditionQueue.isEmpty()) {
      for (Node addOn : locallevelAdditionQueue) {
        simplifiedParent.add(addOn);
      }
      locallevelAdditionQueue.clear();
    }
  }

  Binding makeBinding(StylesheetModule module, Element element,
      Element namespaceBinder,
      // Resolver binder,
      short globalorlocal, short parorvar, ErrorReporter cesspool)
      throws XSLToolsException {
    ParameterOrVariableBinding binding = null;
    try {
      Attribute value = element.attribute(XSLConstants.ATTR_SELECT_QNAME);
      // String elementIdentifier =
      // element.attributeValue(XMLConstants.ELEMENT_ID_QNAME);
      XPathExpr exp = null;
      if (value != null) {
        String svalue = value.getValue();
        exp = module.getCachedXPathExp(XSLConstants.ATTR_SELECT, svalue,
            namespaceBinder, element);
        if (exp != null) {
          // there is a select, element contents should be ignored (and empty).
          // TODO: Error if both select and contents present.

          if (globalorlocal == Resolver.TOPLEVEL_SCOPE) {
            if (exp instanceof XPathPathExpr) {
              XPathPathExpr pexp = (XPathPathExpr) exp;
              if (pexp.getStepCount() > 1) {
                XPathExpr fstep = pexp.getFirstStep();
                if (fstep instanceof XPathAxisStep) {
                  XPathPathExpr nexp = new XPathAbsolutePathExpr();
                  for (XPathStepExpr se : pexp) {
                    nexp.addLastStep((XPathBase) se.clone());
                  }
                  exp = nexp;
                  value.setValue(exp.toString());
                  module.cacheXPathExpression(element,
                      XSLConstants.ATTR_SELECT, exp);
                }
              }
            }
          }

          binding = new XPathExpressionParameterOrVariableBinding(module, exp,
              namespaceBinder, globalorlocal, parorvar);
        }
      }
      if (!element.elements().isEmpty()) {
        if (exp != null) {
          // complain!
          XSLT.getInstance().ambigousVariableSuppliedValue();
        } else {
          binding = new RTFParameterOrVariableBinding(module, element,
              globalorlocal, parorvar);
        }
      } else if (exp == null) {
        // model of the empty node sequence
        binding = new XPathExpressionParameterOrVariableBinding(module,
            EMPTYSTRING, namespaceBinder, globalorlocal, parorvar);
      }
      // binder.bind(name, binding);
      return binding;
    } catch (XSLToolsException ex) {
      cesspool.reportError(module, element, ParseLocation.Extent.TAG, ex);
    }
    return binding;
  }

  Binding bind(StylesheetModule module, Element element,
      Element namespaceBinder, Resolver binder, short globalorlocal,
      short parorvar, ErrorReporter cesspool) throws XSLToolsException {

    Binding binding = Binding.SHOULD_NOT_RESOLVE;

    try {
      Binding binding2 = makeBinding(module, element, namespaceBinder,
          globalorlocal, parorvar, cesspool);
      if (binding2 != null)
        binding = binding2;
      String name_s = element.attributeValue(XSLConstants.ATTR_NAME_QNAME);
      if (name_s == null) {
        cesspool.reportError(module, element, ParseLocation.Extent.TAG,
            "Parameter or variable declaration had no name");
        return Binding.SHOULD_NOT_RESOLVE;
      }
      QName name = ElementNamespaceExpander.qNameForXSLAttributeValue(name_s,
          namespaceBinder, NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
      binder.bind(name, binding);
    } catch (XSLToolsException ex) {
      cesspool.reportError(module, element, ParseLocation.Extent.TAG, ex);
    }
    return binding;
  }

  protected StylesheetModule simplify(StylesheetLevel level,
      StylesheetModule original) throws XSLToolsException {
    Document result = fac.createDocument();
    result.setName(original.getDocument(StylesheetModule.CORE).getName());

    for (Iterator it = original.getDocument(StylesheetModule.CORE)
        .nodeIterator(); it.hasNext();) {
      Node n = (Node) it.next();
      Node simplified = simplify(n, original, level, 0, !it.hasNext());
      result.add(simplified);
    }
    original.setDocument(result, StylesheetModule.CORE);
    // original. remake element map... if using xpath cache...
    // DONE: Only necessary if using xpath cache...
    // original.rebuildIdToElementMaps();
    return original;
  }
}