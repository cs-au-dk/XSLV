/*
 * dongfang M. Sc. Thesis
 * Created on 2005-05-02
 */
package dongfang.xsltools.simplification;

import junit.textui.TestRunner;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;

/**
 * @author dongfang
 */
public class LoaderTest extends SimplificationTestCase {
  public LoaderTest() throws Exception {
    super(LoaderTest.class.getSimpleName(), TEST_PATH + "loader/A.xsl");
    super.loadStylesheet();
  }

  public void testLoader() {
    Stylesheet ss = afterDefaultSimplificationStylesheet;

    StylesheetLevel Agroup = ss.getPrincipalLevel();
    assertEquals("A group should be just A", 1, Agroup.contents().size());
    assertEquals("A has 2 imports", 2, Agroup.imports().size());

    StylesheetLevel Bgroup = Agroup.imports().get(0);
    assertEquals("B group should be just B", 1, Bgroup.contents().size());
    assertEquals("B group has 1 import", 1, Bgroup.imports().size());

    StylesheetLevel Dgroup = Bgroup.imports().get(0);
    assertEquals("D group should be B and F", 2, Dgroup.contents().size());
    assertEquals("D group has 1 import", 1, Dgroup.imports().size());

    StylesheetLevel Cgroup = Agroup.imports().get(1);
    assertEquals("C group should be C, E and I", 3, Cgroup.contents().size());
    assertEquals("C group has 1 import", 1, Cgroup.imports().size());
  }

  public static void main(String[] args) {
    TestRunner.run(LoaderTest.class);
  }
}
