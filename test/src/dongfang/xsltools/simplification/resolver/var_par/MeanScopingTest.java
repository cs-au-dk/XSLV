/*
 * dongfang M. Sc. Thesis
 * Created on 2005-05-02
 */
package dongfang.xsltools.simplification.resolver.var_par;

import junit.textui.TestRunner;
import dongfang.xsltools.simplification.LoaderTest;
import dongfang.xsltools.simplification.SimplificationTestCase;

/**
 * This also tests a great deal of the attribute set stuff... (but that is good --
 * escaping attribute sets are treated separately) in the resolution -- no
 * simple trick)
 * 
 * @author dongfang
 */
public class MeanScopingTest extends SimplificationTestCase {
  /**
   * 
   */
  public MeanScopingTest() throws Exception {
    super(LoaderTest.class.getSimpleName(), TEST_PATH
        + "resolver/var-par/mean-scoping-error.xsl");
    super.loadStylesheet();
  }

  public void testNothing() {
    // the xsl should fail when loading! -- variable decls get resolved into
    // foreign scope
  }

  public static void main(String[] args) {
    TestRunner.run(MeanScopingTest.class);
  }
}
