/*
 * dongfang M. Sc. Thesis
 * Created on 2005-05-02
 */
package dongfang.xsltools.xsd_dsd_conversion;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author dongfang
 */
public class AllTests extends TestSuite {
  public AllTests(String name) {
    super(name);

    addTestSuite(BadTest.class);
    addTestSuite(ConversionTestCase.class);
    addTestSuite(DownwardTest.class);
    addTestSuite(MegaDownwardTest.class);
    addTestSuite(QualificationTest.class);
    addTestSuite(ScopingTest.class);
    addTestSuite(TyperefTest.class);
  }

  public static void main(String[] args) {
    Test all = new AllTests("");
    TestRunner.run(all);
  }
}
