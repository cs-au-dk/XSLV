/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import java.util.Iterator;
import java.util.List;

import junit.textui.TestRunner;

import org.dom4j.Document;
import org.dom4j.Element;

import dongfang.XSLConstants;
import dongfang.xsltools.model.StylesheetModule;

/**
 * @author dongfang
 */
public class CallTemplatesSimplificationTest extends SimplificationTestCase {

  public CallTemplatesSimplificationTest() throws Exception {
    super(CallTemplatesSimplificationTest.class.getSimpleName(), TEST_PATH
        + "call-template/simple.xsl");
    super.loadStylesheet();
  }

  public void testCallTemplatesLinking() {
    // pick up all call-templates with a mode
    Document primary = afterDefaultSimplificationStylesheet
        .getPrincipalModule().getDocument(StylesheetModule.SIMPLIFIED);
    List applys = primary
        .selectNodes("//xsl:template//xsl:apply-templates[@mode]");
    for (Iterator<Element> apply = applys.iterator(); apply.hasNext();) {
      Element a = apply.next();
      String mode = a.attributeValue(XSLConstants.ATTR_MODE);
      List targets = primary.selectNodes("//xsl:template[@mode=\"" + mode
          + "\"]");
      assertEquals("Bad match count for converted call-template", 1, targets
          .size());
    }
  }

  public static void main(String[] args) {
    TestRunner.run(CallTemplatesSimplificationTest.class);
  }
}
