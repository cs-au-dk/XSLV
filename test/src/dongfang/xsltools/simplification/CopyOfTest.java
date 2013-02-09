/*
 * dongfang M. Sc. Thesis
 * Created on 2005-05-02
 */
package dongfang.xsltools.simplification;

import java.util.Iterator;
import java.util.List;

import junit.textui.TestRunner;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;

import dongfang.XSLConstants;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;

/**
 * @author snk
 */
public class CopyOfTest extends SimplificationTestCase {
  public CopyOfTest() throws Exception {
    super(CopyOfTest.class.getSimpleName(), TEST_PATH + "literals/literals.xsl");
    // super(CopyOfTest.class.getSimpleName(), TEST_PATH +
    // "copy-of/copy-of.xsl");
    super.loadStylesheet();
  }

  public void testTextSimplification() {
    Document primary = getPrimarySimplifiedDocument();
    // List textContainers =
    // primary.selectNodes("//xsl:value-of[@select=\"'TEXT'\"]");
    List textContainers = primary
        .selectNodes("//xsl:value-of[@select=\"'TEXT'\"]");
    assertEquals(2, textContainers.size());
  }

  public void testAttributeContentsIsValueOf() {
    Document primary = getPrimarySimplifiedDocument();
    // List textContainers =
    // primary.selectNodes("//xsl:value-of[@select=\"'TEXT'\"]");
    List textContainers = primary
        .selectNodes("//xsl:value-of[@select=\"'barattvalue'\"]");
    assertEquals(2, textContainers.size());
  }

  // TODO: Check that this value-of should not be processed!
  public void testVariableRefValueOf() {
    Document primary = getPrimarySimplifiedDocument();
    // List textContainers =
    // primary.selectNodes("//xsl:value-of[@select=\"'TEXT'\"]");
    List textContainers = primary
        .selectNodes("//xsl:value-of[@select=\"$test_seq_constructor_var_contents_simplification\"]");
    assertEquals(1, textContainers.size());

    Element e = (Element) textContainers.get(0);
    String sel = e.attributeValue(XSLConstants.ATTR_SELECT_QNAME);

    if (sel.indexOf("$") >= 0) {
      List variableDecls = primary.selectNodes("//xsl:variable");
      assertFalse("Variable used but not declared: " + sel, variableDecls
          .isEmpty());
    }
  }

  // Not really a literals test, but in some sense maybe .. anyway just test it.
  public void testCopyOfGone() {
    Document primary = getPrimarySimplifiedDocument();
    // List textContainers =
    // primary.selectNodes("//xsl:value-of[@select=\"'TEXT'\"]");
    List copy_ofs = primary.selectNodes("//xsl:copy-of");
    assertTrue("Copy-of remaining", copy_ofs.isEmpty());
  }

  // in fact there are also things in element and attribute names that are
  // interpreted
  // as attribute value templates -- these are not tested here.
  public void testAttributeValueTemplatesInLiterals() {
    Document primary = getPrimarySimplifiedDocument();
    // List textContainers =
    // primary.selectNodes("//xsl:value-of[@select=\"'TEXT'\"]");
    List copy_ofs = primary
        .selectNodes("//xsl:attribute[@name='barattr']/xsl:value-of");
    assertEquals("Unexpected number of baratt attribute", 2, copy_ofs.size());
    for (Iterator<Element> elements = copy_ofs.iterator(); elements.hasNext();) {
      Element element = elements.next();
      String sele = element.attributeValue(XSLConstants.ATTR_SELECT_QNAME);
      assertEquals(
          "Unexpected att val -- att val template resolution failed??",
          "'barattvalue'", sele);
    }
  }

  public void testElementNamespacing() throws Exception {
    Document primary = getPrimarySimplifiedDocument();
    List copy_ofs = primary
        .selectNodes("//xsl:element[@name='fooelem' or @name='foons:fooelem']");
    assertEquals("Unexpected number fo fooelem element constructors", 5,
        copy_ofs.size());

    String ns = "";

    for (Iterator<Element> elementcstrs = copy_ofs.iterator(); elementcstrs
        .hasNext();) {
      Element cstr = elementcstrs.next();

      String name = cstr.attributeValue(XSLConstants.ATTR_NAME_QNAME);

      QName qualName = ElementNamespaceExpander.qNameForXSLAttributeValue(name, cstr,
          NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);

      String uri = qualName.getNamespaceURI();

      if (cstr.attributeValue(XSLConstants.ATTR_NAMESPACE_QNAME) != null)
        uri = cstr.attributeValue(XSLConstants.ATTR_NAMESPACE_QNAME);

      if ("http://dongfang.dk/default".equals(uri))
        ns += "d";
      else if ("http://dongfang.dk/foo".equals(uri))
        ns += "o";
      else
        ns += "?";
    }
    assertEquals("Some fooelem element ended up in wrong NS", "ddodd", ns);
  }

  public void testAttributeNamespacing() throws Exception {
    Document primary = getPrimarySimplifiedDocument();
    List copy_ofs = primary
        .selectNodes("//xsl:attribute[@name='fooattr' or @name='foons:fooattr']");
    assertEquals("Unexpected number fo fooattr attribute constructors", 6,
        copy_ofs.size());

    String ns = "";

    for (Iterator<Element> attributecstrs = copy_ofs.iterator(); attributecstrs
        .hasNext();) {
      Element cstr = attributecstrs.next();

      String name = cstr.attributeValue(XSLConstants.ATTR_NAME_QNAME);

      QName qualName = ElementNamespaceExpander.qNameForXSLAttributeValue(name, cstr,
          NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);

      String uri = qualName.getNamespaceURI();

      if (cstr.attributeValue(XSLConstants.ATTR_NAMESPACE_QNAME) != null)
        uri = cstr.attributeValue(XSLConstants.ATTR_NAMESPACE_QNAME);

      if ("".equals(uri))
        ns += "n";
      else if ("http://dongfang.dk/foo".equals(uri))
        ns += "f";
      else
        ns += "?";
    }
    assertEquals("Some fooattr attribute ended up in wrong NS", "nnfnfn", ns);
  }

  public static void main(String[] args) {
    TestRunner.run(CopyOfTest.class);
  }
}
