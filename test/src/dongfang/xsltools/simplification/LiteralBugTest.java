/*
 * dongfang M. Sc. Thesis
 * Created on 2005-05-02
 */
package dongfang.xsltools.simplification;

import java.util.List;

import junit.textui.TestRunner;

import org.dom4j.Document;

/**
 * @author snk
 */
public class LiteralBugTest extends SimplificationTestCase {
  public LiteralBugTest() throws Exception {
    super(LiteralBugTest.class.getSimpleName(), TEST_PATH
        + "literals/literal-bug-test.xsl");
    super.loadStylesheet();
  }

  public void testTextNodeSimplification() {
    Document primary = getPrimarySimplifiedDocument();
    List textContainers = primary
        .selectNodes("//xsl:value-of[@select=\"'TEXTNODE  \n  '\"]");
    assertTrue(textContainers.size() == 1);
  }

  public void testXSLTextSimplification() {
    Document primary = getPrimarySimplifiedDocument();
    List textContainers = primary
        .selectNodes("//xsl:value-of[@select=\"'XSL:TEXTTEXT'\"]");
    assertTrue(textContainers.size() == 1);
  }

  public void testXSLTextWhitespace() {
    Document primary = getPrimarySimplifiedDocument();
    List textContainers = primary
        .selectNodes("//xsl:value-of[@select=\"' '\"]");
    assertTrue(textContainers.size() == 1);
  }

  public static void main(String[] args) {
    TestRunner.run(LiteralBugTest.class);
  }
}
