s/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.xsd_dsd_conversion;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import dongfang.xsltools.util.XSDValidator;

/**
 * @author dongfang
 */
public class MetaTest extends TestCase {

  static Logger stinkdyr = Logger.getLogger("xslv");
  static {
    stinkdyr.setLevel(Level.INFO);
  }

  private static final String TEST_PATH = "test/resources/xsd-dsd-converter";

  // private final String dirName;

  public MetaTest(String testName) {
    super(testName);
    // this.dirName = dirName;
  }

  /**
   * Test that there is one .xsd file in given directory. Test that all the xml
   * test cases named ....xml do validate against the xsd file Test that none of
   * the xml test cases named anti....xml do validate against the xsd file
   */
  private void _testTestSet(File totalDir, String testSetName) {
    File[] positiveCases;
    File[] negativeCases;

    File xsd = null;

    // File totalDir = + File.pathSeparator + testCaseDirName);

    File[] xsds = totalDir.listFiles(new FilenameFilter() {
      public boolean accept(File f, String name) {
        return name.endsWith(".xsd");
      }
    });

    if (xsds.length < 1)
      fail("no xsd file in " + testSetName);
    else if (xsds.length > 1)
      fail("more than one xsd file in " + testSetName);
    else
      xsd = xsds[0];

    xsds = totalDir.listFiles(new FilenameFilter() {
      public boolean accept(File f, String name) {
        return !name.startsWith("anti") && name.endsWith(".xml");
      }
    });

    positiveCases = xsds;

    if (positiveCases.length == 0)
      stinkdyr.warning("No positive cases in " + testSetName);

    xsds = totalDir.listFiles(new FilenameFilter() {
      public boolean accept(File f, String name) {
        return name.startsWith("anti") && name.endsWith(".xml");
      }
    });

    negativeCases = xsds;

    if (negativeCases.length == 0)
      stinkdyr.warning("No negative cases in " + testSetName);

    URI schemaFilenameURI = xsd.toURI();

    for (int i = 0; i < positiveCases.length; i++) {

      File casef = positiveCases[i];
      URI caseFilenameURI = casef.toURI();

      boolean xsdResult = false;

      try {
        xsdResult = XSDValidator.validate(caseFilenameURI.toString(),
            schemaFilenameURI.toString());
      } catch (Exception e) {
        fail("Exception: " + e.getMessage() + " in test set " + testSetName
            + " in test case file " + positiveCases[i].getName());
      }

      assertTrue("Positive expectations, negative results in test set "
          + testSetName + ", test case file " + positiveCases[i].getName(),
          xsdResult);
    }

    for (int i = 0; i < negativeCases.length; i++) {

      File casef = negativeCases[i];
      URI caseFilenameURI = casef.toURI();

      boolean xsdResult = true;

      try {
        xsdResult = XSDValidator.validate(caseFilenameURI.toString(),
            schemaFilenameURI.toString());
        assertFalse("Negative expectations, positive results in test set "
            + testSetName + ", test case file " + negativeCases[i], xsdResult);
      } catch (Exception e) {
        fail("Exception: " + e.getMessage() + " in test set " + testSetName
            + " in test case file " + negativeCases[i].getName());
      }
    }

    File[] xsd_imports = totalDir.listFiles(new FilenameFilter() {
      public boolean accept(File f, String name) {
        return name.endsWith(".xsdi");
      }
    });

    File[] allFiles = totalDir.listFiles(new FileFilter() {
      public boolean accept(File f) {
        return f.isFile();
      }
    });

    int noTestRelevantFiles = 1 + xsd_imports.length + positiveCases.length
        + negativeCases.length;

    assertEquals(
        "Some file in "
            + testSetName
            + " was not recognized as a schema, a positive test case or a negative test case",
        noTestRelevantFiles, allFiles.length);
  }

  public void testTestCases() throws Exception {
    File testCasesSuperDir = new File(TEST_PATH);

    File[] testcases = testCasesSuperDir.listFiles(new FilenameFilter() {
      public boolean accept(File f, String name) {
        return f.isDirectory() && !(name.equalsIgnoreCase("CVS"));
      }
    });

    for (int i = 0; i < testcases.length; i++) {
      _testTestSet(testcases[i], testcases[i].getName());
    }
  }

  public static void main(String[] args) {
    TestRunner.run(MetaTest.class);
  }
}