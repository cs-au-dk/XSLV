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

import dongfang.xsltools.simplification.LoaderTest;
import dongfang.xsltools.simplification.SimplificationTestCase;

/**
 * @author dongfang
 */
public class ImportPrecedenceOverrideTest extends SimplificationTestCase {
  /**
   * 
   */
  public ImportPrecedenceOverrideTest() throws Exception {
    super(LoaderTest.class.getSimpleName(), TEST_PATH
        + "resolver/var-par/sub.xsl");
    super.loadStylesheet();
  }

  public void test1() {
    Document primary = getPrimarySimplifiedDocument();
    List list = primary
        .selectNodes("//xsl:element[@name='test1']/xsl:value-of/@select");
    assertEquals("Expected 2 results", 2, list.size());
    for (Iterator iter = list.iterator(); iter.hasNext();) {
      Attribute next = (Attribute) iter.next();
      assertEquals("test1 wrong result", "'MZ1401'", next.getValue());
    }
  }

  public void testOverride() {
    Document primary = getPrimarySimplifiedDocument();
    List list = primary
        .selectNodes("//xsl:element[@name='override-test']/xsl:value-of/@select");
    assertEquals("Expected 1 result", 1, list.size());
    for (Iterator iter = list.iterator(); iter.hasNext();) {
      Attribute next = (Attribute) iter.next();
      assertEquals("test1 wrong result", "'principal'", next.getValue());
    }
  }

  public void testContinuedResolution() {
    Document primary = getPrimarySimplifiedDocument();
    List list = primary
        .selectNodes("//xsl:element[@name='continued-resolution-test']/xsl:value-of/@select");
    assertEquals("Expected 2 results", 2, list.size());
    for (Iterator iter = list.iterator(); iter.hasNext();) {
      Attribute next = (Attribute) iter.next();
      assertEquals("test1 wrong result", "'principal'", next.getValue());
    }
  }

  public void testNamespaces() {
    Document primary = getPrimarySimplifiedDocument();
    List list = primary
        .selectNodes("//xsl:element[@name='namespace-test']/xsl:value-of/@select");
    assertEquals("Expected 2 results", 2, list.size());
    int i = 1;
    for (Iterator iter = list.iterator(); iter.hasNext();) {
      Attribute next = (Attribute) iter.next();
      assertEquals("test1 wrong result", Integer.toString(i++), next.getValue());
    }
  }

  public static void main(String[] args) {
    TestRunner.run(ImportPrecedenceOverrideTest.class);
  }
}
