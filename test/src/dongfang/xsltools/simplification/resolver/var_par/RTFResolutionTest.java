/*
 * dongfang M. Sc. Thesis
 * Created on 2005-05-02
 */
package dongfang.xsltools.simplification.resolver.var_par;

import java.util.Iterator;
import java.util.List;

import junit.textui.TestRunner;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import dongfang.xsltools.simplification.SimplificationTestCase;

/**
 * This also tests a great deal of the attribute set stuff... (but that is good --
 * escaping attribute sets are treated separately) in the resolution -- no
 * simple trick)
 * 
 * @author dongfang
 */
public class RTFResolutionTest extends SimplificationTestCase {
  /**
   * 
   */
  public RTFResolutionTest() throws Exception {
    super(RTFResolutionTest.class.getSimpleName(), TEST_PATH
        + "resolver/var-par/rtf.xsl");
    super.loadStylesheet();
  }

  public void testRTFInCopyOfResolved() {
    Document primary = getPrimarySimplifiedDocument();
    List list = primary.selectNodes("//xsl:element[@name='elem-04']/*");
    assertEquals("Expected 3 subelements", 3, list.size());
  }

  public void testRTFCopy() {
    Document primary = getPrimarySimplifiedDocument();
    List list = primary
        .selectNodes("//xsl:element[@name='elem-04']/xsl:element/xsl:attribute");
    assertEquals("Expected 2 attr constructors", 2, list.size());

    int i = 0;

    for (Iterator<Element> iter = list.iterator(); iter.hasNext();) {
      Element atcstr = iter.next();

      List list2 = atcstr.selectNodes("text()");

      if (!list2.isEmpty()) {
        for (Iterator<org.dom4j.Node> titer = list2.iterator(); titer.hasNext();) {
          org.dom4j.Node n = titer.next();
          if (n.getText().trim().length() > 0)
            fail("Escaping string literal:" + n);
        }
      } else {
        list2 = atcstr.selectNodes("xsl:value-of/@select");
        assertEquals("Attribute constructor contents not xsl:value-of", 1,
            list2.size());
        Attribute select = (Attribute) list2.get(0);
        if (i == 0)
          assertTrue(select.getValue().equals("'right-for-foo'"));
        else
          assertTrue(select.getValue().equals(
              "'should-override-in-variable-foo'"));
      }
      i++;
    }
  }

  public void testRTFCopyDelarationAfter() {
    Document primary = getPrimarySimplifiedDocument();
    List list = primary
        .selectNodes("//xsl:element[@name='elem-05']/xsl:element/xsl:attribute");
    assertEquals("Expected 2 attr constructors", 2, list.size());

    int i = 0;

    for (Iterator<Element> iter = list.iterator(); iter.hasNext();) {
      Element atcstr = iter.next();

      List list2 = atcstr.selectNodes("text()");

      if (!list2.isEmpty()) {
        for (Iterator<org.dom4j.Node> titer = list2.iterator(); titer.hasNext();) {
          org.dom4j.Node n = titer.next();
          if (n.getText().trim().length() > 0)
            fail("Escaping string literal:" + n);
        }
      } else {
        list2 = atcstr.selectNodes("xsl:value-of/@select");
        assertEquals("Attribute constructor contents not xsl:value-of", 1,
            list2.size());
        Attribute select = (Attribute) list2.get(0);
        if (i == 0)
          assertTrue(select.getValue().equals("'right-for-foo'"));
        else
          assertTrue(select.getValue().equals(
              "'should-override-in-variable-foo'"));
      }
      i++;
    }
  }

  public static void main(String[] args) {
    TestRunner.run(RTFResolutionTest.class);
  }
}
