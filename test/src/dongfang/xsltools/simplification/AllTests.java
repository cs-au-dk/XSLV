/*
 * dongfang M. Sc. Thesis
 * Created on 2005-05-02
 */
package dongfang.xsltools.simplification;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import dongfang.xsltools.simplification.resolver.attribute_set.AttributeSetResolutionTest;
import dongfang.xsltools.simplification.resolver.var_par.ImportPrecedenceOverrideTest;
import dongfang.xsltools.simplification.resolver.var_par.MeanScopingTest;
import dongfang.xsltools.simplification.resolver.var_par.OutOfScopeTest;
import dongfang.xsltools.simplification.resolver.var_par.RTFResolutionTest;

/**
 * @author dongfang
 */
public class AllTests extends TestSuite {
  public AllTests(String name) {
    super(name);

    addTestSuite(AttributeSetResolutionTest.class);
    addTestSuite(ImportPrecedenceOverrideTest.class);
    addTestSuite(MeanScopingTest.class);
    addTestSuite(OutOfScopeTest.class);
    addTestSuite(RTFResolutionTest.class);

    addTestSuite(CallTemplatesSimplificationTest.class);
    addTestSuite(CopyOfTest.class);
    addTestSuite(LiteralResultsTest.class);
    addTestSuite(LiteralBugTest.class);
    addTestSuite(LoaderTest.class);
  }

  public static void main(String[] args) {
    Test all = new AllTests("simplification");
    TestRunner.run(all);
  }
}
