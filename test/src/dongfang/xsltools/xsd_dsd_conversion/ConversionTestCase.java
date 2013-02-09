/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.xsd_dsd_conversion;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;

import dk.brics.dsd.Schema;
import dk.brics.dsd.ValidationErrorHandler;
import dk.brics.dsd.Validator;
import dk.brics.jwig.analysis.summarygraph.Node;
import dongfang.xsltools.util.XSDValidator;

/**
 * @author dongfang
 */
public class ConversionTestCase extends TestCase {

  static Logger stinkdyr = Logger.getLogger("xslv");
  static {
    stinkdyr.setLevel(Level.INFO);
  }

  private static final String TEST_PATH = "test/resources/xsd-dsd-converter";

  private final String dirName;

  private File[] positiveCases;

  private File[] negativeCases;

  private File xsd;

  private boolean dsdValidate(URL documentLocation, URL schemaLocation)
      throws Exception {
    final boolean[] errors = new boolean[1];
    ValidationErrorHandler handler = new ValidationErrorHandler() {
      public boolean error(Node node, Element element, String message,
          Element dsd) {
        System.err.println(message + "(node:" + node.toString() + ") (element:"
            + element.getQualifiedName() + ")");
        errors[0] = true;
        return true;
      }
    };

    Validator valle = new Validator(handler);

    Schema schema = new Schema(Validator.makeDocument(schemaLocation));
    Document instance = Validator.makeDocument(documentLocation);

    valle.process(instance, schema);

    return !errors[0];
  }

  protected ConversionTestCase(String testName, String dirName) {
    super(testName);
    this.dirName = dirName;
  }

  public void setUp() {
    File totalDir = new File(TEST_PATH, dirName);
    File[] xsds = totalDir.listFiles(new FilenameFilter() {
      public boolean accept(File f, String name) {
        return name.endsWith(".xsd");
      }
    });

    if (xsds.length < 1)
      stinkdyr.warning("no xsd file!");
    else if (xsds.length > 1)
      stinkdyr.warning("more than one xsd file!");
    else
      this.xsd = xsds[0];

    xsds = totalDir.listFiles(new FilenameFilter() {
      public boolean accept(File f, String name) {
        return !name.startsWith("anti") && name.endsWith(".xml");
      }
    });

    this.positiveCases = xsds;

    xsds = totalDir.listFiles(new FilenameFilter() {
      public boolean accept(File f, String name) {
        return name.startsWith("anti") && name.endsWith(".xml");
      }
    });

    this.negativeCases = xsds;
  }

  public void testPositiveCases() throws Exception {

    URI schemaFilenameURI = this.xsd.toURI();
    File dsd = // File.createTempFile("temp", "dsd");
    new File("temp.dsd");

    XSDToDSDConverter.translate(this.xsd, dsd);

    for (int i = 0; i < this.positiveCases.length; i++) {

      File casef = this.positiveCases[i];
      URI caseFilenameURI = casef.toURI();

      boolean xsdResult = XSDValidator.validate(caseFilenameURI.toString(),
          schemaFilenameURI.toString());

      if (!xsdResult)
        stinkdyr
            .warning("Positive expectation yielded negative result -- does the test case work at all?- "
                + caseFilenameURI.toString());

      boolean dsdResult = true;

      try {
        dsdResult = dsdValidate(casef.toURI().toURL(), dsd.toURI().toURL());
      } catch (Exception ex) {
        System.err.println(ex.getMessage());
        dsdResult = false;
      } finally {
        // dsdResult = false;
      }

      assertEquals("XSD and DSD validation differed", xsdResult, dsdResult);

    }
  }

  public void testNegativeCases() throws Exception {
    URI schemaFilenameURI = this.xsd.toURI();
    File dsd = // File.createTempFile("temp", "dsd");
    new File("temp.dsd");

    XSDToDSDConverter.translate(this.xsd, dsd);

    for (int i = 0; i < this.negativeCases.length; i++) {

      File casef = this.negativeCases[i];
      URI caseFilenameURI = casef.toURI();

      try {

        boolean xsdResult = XSDValidator.validate(caseFilenameURI.toString(),
            schemaFilenameURI.toString());

        if (xsdResult)
          stinkdyr
              .warning("Negative expectation yielded positive result -- does the test case work at all?- "
                  + caseFilenameURI.toString());

        boolean dsdResult = dsdValidate(casef.toURI().toURL(), dsd.toURI()
            .toURL());

        assertEquals("XSD and DSD validation differed", xsdResult, dsdResult);

      } catch (Exception ex) {
        fail("An exception occured: " + ex);
      }
    }
  }
}