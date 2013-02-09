/*
 * dongfang M. Sc. Thesis
 * Created on 2005-02-26
 */
package dongfang.xsltools.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.InvalidXPathException;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.VisitorSupport;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import dongfang.XMLConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.DiagnosticsConfigurationOptions;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathPathExpr;

/**
 * General attempt to make up for what was missing in Dom4J.
 * @author dongfang
 */
public class Dom4jUtil {
  private static DocumentFactory dfac = new DocumentFactory();

  /**
   * Assuming that a's owner elemenet is in the XSL namespace, returns whether
   * the attribute a in the XSL namespace (no, getNamespace() on a is not to be
   * trusted!)
   * 
   * @param a
   * @return
   * @deprecated - no need for it really. Use QNames.
   */
  @Deprecated
public static boolean isXSLAttribute(Attribute a) {
    /*
     * return "".equals(a.getNamespacePrefix()) ||
     * XSLConstants.NAMESPACE_URI.equals(a.getNamespaceURI());
     */
    return Namespace.NO_NAMESPACE.getURI().equals(a.getNamespaceURI());
  }

  public static boolean isXPathAttribute(QName attname) {
    boolean namespaceOk = Namespace.NO_NAMESPACE.equals(attname.getNamespace());
    if (namespaceOk) {
      String localName = attname.getName();
      return (XSLConstants.ATTR_MATCH.equals(localName)
          || XSLConstants.ATTR_SELECT.equals(localName)
          || XSLConstants.ATTR_TEST.equals(localName) || XSLConstants.ATTR_USE
          .equals(localName));
    }
    return false;
  }

  /*
   * public static boolean hasXSLAttribute(Element element, String attname) {
   * for (int i = 0; i < element.attributeCount(); i++) { Attribute a =
   * element.attribute(i); if (attname.equals(a.getName()) && isXSLAttribute(a)) {
   * return true; } } return false; }
   */
  /*
   * public static Attribute getXSLAttribute(Element element, String attname) {
   * for (int i = 0; i < element.attributeCount(); i++) { Attribute a =
   * element.attribute(i); if (attname.equals(a.getName()) && isXSLAttribute(a)) {
   * return a; } } return null; }
   */
  /**
   * 
   * @param element
   * @param attname
   * @return
   * @deprecated -- not necessary, but all right only harm is confusion.
   */
  @Deprecated
public static String getXSLAttributeValue(Element element, String attname) {
    for (int i = 0; i < element.attributeCount(); i++) {
      Attribute a = element.attribute(i);
      if (attname.equals(a.getName()) && isXSLAttribute(a)) {
        return a.getValue();
      }
    }
    return null;
  }

  public static void prettyPrint(Node n, OutputStream os) {
    try {
      OutputFormat fmt = new OutputFormat(UtilConfiguration.current
          .getIndentationString(), UtilConfiguration.current.addNewlines(),
          "utf-8");
      fmt.setNewLineAfterDeclaration(!UtilConfiguration.current.addNewlines());
      XMLWriter wr = new XMLWriter(os, fmt);
      wr.write(n);
      wr.flush();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  /*
   * public static void prettyPrint(Node n, Writer w) { try { OutputFormat fmt =
   * new OutputFormat(" ", false, "utf-8");
   * fmt.setNewLineAfterDeclaration(false); XMLWriter wr = new XMLWriter(w,
   * fmt); wr.write(n); wr.flush(); } catch (IOException e) { throw new
   * AssertionError(e); } }
   */

  public static void prettyPrint(Node n) {
    prettyPrint(n, System.out);
  }

  public static void debugPrettyPrint(Node n, OutputStream os) {
    try {
      OutputFormat fmt = new OutputFormat("  ", true, "utf-8");
      fmt.setNewLineAfterDeclaration(false);
      XMLWriter wr = new XMLWriter(os, fmt);
      wr.write(n);
      wr.flush();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  public static void debugPrettyPrint(Node n) {
    debugPrettyPrint(n, System.out);
  }

  public static void toString(Node n, Writer wr) {
    try {
      OutputFormat fmt = new OutputFormat(UtilConfiguration.current
          .getIndentationString(), UtilConfiguration.current.addNewlines(),
          "utf-8");
      fmt.setNewLineAfterDeclaration(!UtilConfiguration.current.addNewlines());
      XMLWriter xwr = new XMLWriter(wr, fmt);
      xwr.write(n);
      xwr.flush();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  public static void toDebugString(Node n, Writer wr) {
    try {
      OutputFormat fmt = new OutputFormat("    ", true, "utf-8");
      fmt.setNewLineAfterNTags(1);
      fmt.setNewLineAfterDeclaration(false);
      XMLWriter xwr = new XMLWriter(wr, fmt);
      xwr.write(n);
      wr.flush();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  public static String toString(Node n) {
    StringWriter sw = new StringWriter();
    toString(n, sw);
    return sw.toString();
  }

  public static String toDebugString(Node n) {
    StringWriter sw = new StringWriter();
    toDebugString(n, sw);
    return sw.toString();
  }

  public static String diagnostics(Diagnoseable diag, Set<Object> configuration) {
    Document d = dfac.createDocument();
    diag.diagnostics(d, dfac, configuration);
    return toDebugString(d);
  }
  
  public static String diagnostics(Diagnoseable diag) {
    return diagnostics(diag, DiagnosticsConfiguration.current.getDefaultDiagnosticsConfiguration());
  }

  public static String diagnostics(Diagnoseable diag, DiagnosticsConfigurationOptions options) {
    Set<Object> configuration = options.getSelectedOptions();
    return diagnostics(diag, configuration);
  }

  public static String diagnostics(Collection coll, String containerElementName, Set<Object> configuration) {
    Document d = dfac.createDocument();
    collectionDiagnostics(d, coll, containerElementName, dfac, configuration);
    return toDebugString(d);
  }

  /*
  public static String diagnostics(Collection coll, String containerElementName) {
    Document d = dfac.createDocument();
    collectionDiagnostics(d, coll, containerElementName, dfac);
    return toDebugString(d);
  }
  */
  
  public static boolean compareStrictly(Document a, Document b) {
    boolean result = false;
    try {
      ByteArrayOutputStream osa = new ByteArrayOutputStream();
      debugPrettyPrint(a, osa);

      ByteArrayOutputStream osb = new ByteArrayOutputStream();
      debugPrettyPrint(b, osb);

      byte[] ba = osa.toByteArray();
      byte[] bb = osa.toByteArray();

      if (!Arrays.equals(ba, bb)) {
        System.err.println("First Argument: ");
        // OutputStreamWriter w=new
        // OutputStreamWriter(System.out,"utf-8");
        System.out.write(ba);

        System.out.println();

        System.err.println("Second Argument: ");
        System.out.write(bb);
        System.out.println();
      } else
        result = true;

    } catch (Exception e) {
    }
    return result;
  }

  public static void collectionDiagnostics(Branch parent, Collection coll,
      String containerElementName, DocumentFactory fac, Set<Object> configuration) {
    Element containerElement = fac.createElement(containerElementName);
    parent.add(containerElement);
    if (coll != null) {
      for (Object diag : coll) {
        if (diag instanceof Diagnoseable)
          ((Diagnoseable) diag).diagnostics(containerElement, fac, configuration);
        else if (diag instanceof Node)
          containerElement.add((Node) ((Node) diag).clone());
        else {
          if (diag instanceof QName) {
            Element e = fac.createElement("qname");
            e.addAttribute("value", clarkName((QName) diag));
            containerElement.add(e);
          } else {
            Element e = fac.createElement("non-diagnoseable-something");
            if (diag == null) {
              e.addAttribute("class", "null");
              e.addAttribute("toString", "null");
            } else {
              e.addAttribute("class", diag.getClass().getSimpleName());
              e.addAttribute("toString", diag.toString());
            }
            containerElement.add(e);
          }
        }
      }
    } else
      containerElement.addAttribute("collection-null", "true");
  }

  public static void collectionDiagnostics(Branch parent, Map coll,
      String containerElementName, DocumentFactory fac, Set<Object> configuration) {
    Element containerElement = fac.createElement(containerElementName);
    parent.add(containerElement);
    if (coll != null) {
      for (Object diag : coll.keySet()) {
        Element key = fac.createElement("key");
        containerElement.add(key);
        if (diag instanceof Diagnoseable)
          ((Diagnoseable) diag).diagnostics(key, fac, configuration);
        else if (diag instanceof Node)
          key.add((Node) ((Node) diag).clone());
        else {
          if (diag instanceof QName) {
            Element e = fac.createElement("qname");
            e.addAttribute("value", clarkName((QName) diag));
            key.add(e);
          } else {
            Element e = fac.createElement("non-diagnoseable-something");
            e.addAttribute("class", diag.getClass().getSimpleName());
            e.addAttribute("toString", diag.toString());
            key.add(e);
          }
        }

        Element value = fac.createElement("value");
        containerElement.add(value);

        diag = coll.get(diag);
        if (diag == null) {
          value.addText("null");
        } else if (diag instanceof Diagnoseable)
          ((Diagnoseable) diag).diagnostics(value, fac, configuration);
        else if (diag instanceof Node)
          value.add((Node) ((Node) diag).clone());
        else {
          if (diag instanceof QName) {
            Element e = fac.createElement("qname");
            e.addAttribute("value", ((QName) diag).getQualifiedName());
            value.add(e);
          } else {
            Element e = fac.createElement("non-diagnoseable-something");
            e.addAttribute("class", diag.getClass().getName());
            value.add(e);
          }
        }
      }
    } else
      containerElement.addAttribute("collection-null", "true");
  }

  /*
   * public static boolean idempotencyRegressionTest(StylesheetModule m,
   * Simplifier simplifier) { boolean result = true; try { StylesheetModule m2 =
   * simplifier.simplify(m, m, new BackingUpCesspool()); StylesheetModule m3 =
   * simplifier.simplify(m2, m2, new BackingUpCesspool()); result =
   * compareStrictly(m2.getDocument(), m3.getDocument()); } catch
   * (XSLToolsException e) { e.printStackTrace(); result = false; } return
   * result; }
   */

  /*
   * @deprecated - no need for it really. Use QNames.
   */
  /*
   * public static boolean isXSLElement(String XSLElementName, Element e) {
   * return (XSLConstants.NAMESPACE_URI.equals(e.getNamespaceURI()) &&
   * XSLElementName .equals(e.getName())); }
   */

  public static String stringno(int i) {
    int totalBits = 32;
    int bitsPerChar = 4;
    StringBuilder result = new StringBuilder();
    for (int j = totalBits / bitsPerChar - 1; j >= 0; j--) {
      long mask = (long) (((1 << bitsPerChar) - 1)) << (j * bitsPerChar);
      char digit = (char) ((i & mask) >>> (j * bitsPerChar));
      char digitval = (char) (digit + 'a');
      result.append(digitval);
    }
    return result.toString();
  }

  public static String generateSafePrefix(Element e, String preferred,
      String uri) {
    int i = 0;
    Namespace ns;
    while ((ns = e.getNamespaceForPrefix(preferred)) != null
        && !ns.getURI().equals(uri)) {
      preferred = stringno(i++);
    }
    return preferred;
  }

  public static String getPrefixFor(Element e, String preferred, String uri) {
    Namespace ns = e.getNamespaceForURI(uri);
    if (ns != null)
      return ns.getPrefix();
    String result = generateSafePrefix(e, preferred, uri);
    e.addNamespace(result, uri);
    return result;
  }

  public static String getPrefixFor(Element e, Namespace ns) {
    return getPrefixFor(e, ns.getPrefix(), ns.getURI());
  }

  /**
   * @deprecated - use the explicit no-prefix bind-behaviour version instead !!!
   */
  @Deprecated
public static void transferAttributeValue(String attvalue, Element from,
      String attname, Element to)
      throws XSLToolsXPathUnresolvedNamespaceException {
    QName qname = ElementNamespaceExpander.qNameForXSLAttributeValue(attvalue,
        from, NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE);
    transferAttributeValue(qname, attname, to);
  }

  public static void transferAttributeValue(String attvalue, Element from,
      String attname, Element to, String noPrefixBehaviour)
      throws XSLToolsXPathUnresolvedNamespaceException {
    QName qname = ElementNamespaceExpander.qNameForXSLAttributeValue(attvalue,
        from, noPrefixBehaviour);
    transferAttributeValue(qname, attname, to);
  }

  /*
   * public static void transferAttributeValue ( QName qname, String attname,
   * Element to) { transferAttributeValue(qname, attname, to, true); }
   */

  public static String makeAttributeValue(QName qname, Element to
  /* ,boolean debug */) {

    // Element tryOnMe = null;
    // if (debug) tryOnMe = (Element)to.clone();

    String prefix = getPrefixFor(to, qname.getNamespace());
    String attvalue = ("".equals(prefix)) ? qname.getName() : prefix + ":"
        + qname.getName();
    return attvalue;

    // if (debug)
    // testSemanticDifferenceWithTransferAttributeValue2(qname, attname, to,
    // tryOnMe);
  }

  public static void transferAttributeValue(QName qname, String attname,
      Element to) {
    String attvalue = makeAttributeValue(qname, to);
    to.addAttribute(attname, attvalue);
  }

  /*
   * private static void testSemanticDifferenceWithTransferAttributeValue2 (
   * QName qname, String attname, Element newmethod, Element tryOnMe ) {
   * addQNameAttribute(tryOnMe, attname, qname, false);
   * 
   * String s_oldmethod = toString(tryOnMe); String s_newmethod =
   * toString(newmethod);
   * 
   * if (!s_oldmethod.equals(s_newmethod)) { System.err.println("Results from
   * addQNameAttribute differed from that of transferAttributeValue!");
   * System.err.println("QName: " + qname.getQualifiedName());
   * System.err.println("attname: " + attname);
   * System.err.println("addQNameAttribute result: " + s_oldmethod);
   * System.err.println("transferAttributeValue result: " + s_newmethod); } }
   */

  /**
   * 
   * @param element
   * @param attname
   * @param qname
   * @deprecated - Replace by transferAttributeValue(qname, attname, element);
   *             should do the same thing.
   */
  /*
   * public static void addQNameAttribute(Element element, String attname, QName
   * qname) { addQNameAttribute(element, attname, qname, true); }
   */
  /*
   * private static void _addQNameAttribute(Element element, String attname,
   * QName qname) { // to be removed! // Element before = null; // if (debug) //
   * before = (Element)element.clone();
   * 
   * Namespace bindingForPrefix = element.getNamespaceForPrefix(qname
   * .getNamespacePrefix());
   * 
   * if (bindingForPrefix != null) { if
   * (!bindingForPrefix.getURI().equals(qname.getNamespaceURI())) { String
   * prefix = generateSafePrefix(element, qname.getNamespacePrefix(),
   * qname.getNamespaceURI()); qname = QName.get(qname.getName(), prefix,
   * qname.getNamespaceURI()); } } element.addAttribute(attname,
   * qname.getQualifiedName()); element.addNamespace(qname.getNamespacePrefix(),
   * qname.getNamespaceURI()); // to be removed! // if (debug) //
   * testSemanticDifferenceWithTransferAttributeValue1(qname, attname, //
   * element, before); }
   */

  /*
   * private static void testSemanticDifferenceWithTransferAttributeValue1 (
   * QName qname, String attname, Element oldmethod, Element tryOnMe ) {
   * transferAttributeValue(qname, attname, tryOnMe, false);
   * 
   * String s_oldmethod = toString(oldmethod); String s_newmethod =
   * toString(tryOnMe);
   * 
   * if (!s_oldmethod.equals(s_newmethod)) { System.err.println("Results from
   * addQNameAttribute differed from that of transferAttributeValue!");
   * System.err.println("QName: " + qname.getQualifiedName());
   * System.err.println("attname: " + attname);
   * System.err.println("addQNameAttribute result: " + s_oldmethod);
   * System.err.println("transferAttributeValue result: " + s_newmethod); } }
   */

  @Deprecated
public static ParseLocation getParsePosition(Element source) {
    // dummy
    return null;
  }

  public static Element parse(String contents) {
    try {
      Document tempDoc = new SAXReader().read(new StringReader(contents));
      Element rootElement = tempDoc.getRootElement();
      return rootElement;
    } catch (Exception e) {
      throw new AssertionError("Check your literal; it won't parse!: "
          + e.toString());
    }
  }

  public static Element undecoratedClone(Element e) {
    Element eclone = (Element) e.clone();
    final Set<Attribute> deathRow = new HashSet<Attribute>();
    eclone.accept(new VisitorSupport() {
      @Override
	public void visit(Attribute node) {
        if (node.getNamespace().getURI().equals(XMLConstants.DONGFANG_URI))
          deathRow.add(node);
      }
    });
    for (Attribute att : deathRow) {
      att.detach();
    }
    return eclone;
  }

  /*
   * public static void computePriority(Element element, XPathExp matchPattern)
   * throws InvalidXPathException { // DONE: Check that the priority att for
   * union patterns really has // precedence. if
   * (element.attribute(XSLConstants.ATTR_PRIORITY) != null) return; //
   * XPathPattern pattern = new XPathPattern(matchPattern.toString()); //
   * pattern.getUnionPatterns(); // double d4jpriority = pattern.getPriority();
   * 
   * double homeGrownPriority = ((NodeSetLocationPath) matchPattern)
   * .defaultPriority(); // if (Math.abs(d4jpriority - homeGrownPriority) >
   * 0.00001) // System.err.println("Priority algorithms r' fightin' : " +
   * matchPattern + // " Dom4j thinks it is " + d4jpriority + ", we think " + //
   * homeGrownPriority);
   * 
   * element.addAttribute(XSLConstants.ATTR_PRIORITY, String
   * .valueOf(homeGrownPriority)); }
   */

  public static void computePriority(Element element, XPathExpr matchPattern)
      throws InvalidXPathException {
    // TODO: Check that the priority att for union patterns really has
    // precedence.
    if (element.attribute(XSLConstants.ATTR_PRIORITY) != null)
      return;
    // XPathPattern pattern = new XPathPattern(matchPattern.toString());
    // pattern.getUnionPatterns();

    // double d4jpriority = pattern.getPriority();

    double homeGrownPriority = ((XPathPathExpr) matchPattern).defaultPriority();

    // if (Math.abs(d4jpriority - homeGrownPriority) > 0.00001)
    // System.err.println("Priority algorithms r' fightin' : " + matchPattern +
    // " Dom4j thinks it is " + d4jpriority + ", we think " +
    // homeGrownPriority);

    element.addAttribute(XSLConstants.ATTR_PRIORITY, String
        .valueOf(homeGrownPriority));
  }

  public static String clarkName(QName name) {
    if (name == null)
      return "null";
    if ("".equals(name.getNamespaceURI()))
      return name.getName();
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    sb.append(name.getNamespaceURI());
    sb.append('}');
    sb.append(name.getName());
    return sb.toString();
  }
}
