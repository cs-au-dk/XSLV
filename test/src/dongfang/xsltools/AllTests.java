/*
 * dongfang M. Sc. Thesis
 * Created on 2005-05-02
 */
package dongfang.xsltools;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * @author dongfang
 */
public class AllTests extends TestSuite {
  public AllTests(String name) {
    super(name);

    addTestSuite(dongfang.xsltools.simplification.AllTests.class);
    addTestSuite(dongfang.xsltools.xsd_dsd_conversion.AllTests.class);

  }

  public static void main(String[] args) {
    Test all = new AllTests("");
    TestRunner.run(all);
  }
}
