/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification.resolver.var_par;

import java.util.Iterator;
import java.util.List;

import junit.textui.TestRunner;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import dongfang.xsltools.simplification.LoaderTest;
import dongfang.xsltools.simplification.SimplificationTestCase;

/**
 * @author dongfang
 */
public class OutOfScopeTest extends SimplificationTestCase {
  public OutOfScopeTest() throws Exception {
    super(LoaderTest.class.getSimpleName(), TEST_PATH
        + "resolver/var-par/out-of-scope.xsl");
    super.loadStylesheet();
  }

  public void testAttributeValues() {
    Document primary = getPrimarySimplifiedDocument();
    List list = primary.selectNodes("//xsl:attribute/xsl:value-of/@select");
    assertEquals("Wrong count of 'right' attributes", 7, list.size());
    for (Iterator<Attribute> attrs = list.iterator(); attrs.hasNext();) {
      Attribute attr = attrs.next();
      assertEquals("Wrong (scope shift problem?) attribute value", "'right'",
          attr.getValue());
    }
  }

  public void testAliasing() {
    Document primary = getPrimarySimplifiedDocument();
    List list = primary
        .selectNodes("//xsl:element[position()=last() or position()=last()-1]");

    Element e0 = (Element) list.get(0);
    Element e1 = (Element) list.get(1);

    assert (e1.toString().equals(e1.toString()));
  }

  public static void main(String[] args) {
    TestRunner.run(OutOfScopeTest.class);
  }
}
