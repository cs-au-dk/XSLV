/*
 * dongfang M. Sc. Thesis
 * Created on 2005-05-02
 */
package dongfang.xsltools.simplification;

import junit.textui.TestRunner;

/**
 * Konklusionen paa denne test er at opfoerslen -- xsl:value-of( en RTF typet
 * variabel ) bliver staaende er korrekt -- men den approksimerende forenkling
 * bagefter mangler.
 * 
 * @author snk
 */
public class CopyOfBugTest extends SimplificationTestCase {
  public CopyOfBugTest() throws Exception {
    super(CopyOfBugTest.class.getSimpleName(), TEST_PATH
        + "copy-of/copy-of-bug-test.xsl");
    super.loadStylesheet();
  }

  public void testNada() {
  }

  public static void main(String[] args) {
    TestRunner.run(CopyOfBugTest.class);
  }
}
