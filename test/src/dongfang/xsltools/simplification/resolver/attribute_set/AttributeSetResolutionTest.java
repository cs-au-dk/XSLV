/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification.resolver.attribute_set;

import junit.textui.TestRunner;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.simplification.SimplificationTestCase;

/**
 * @author dongfang
 */
public class AttributeSetResolutionTest extends SimplificationTestCase {
  public AttributeSetResolutionTest() throws Exception {
    super(AttributeSetResolutionTest.class.getSimpleName(), TEST_PATH
        + "resolver/attribute-set/completeness-test.xsl");
    // ToplevelBindingDumper.enable(true);
    super.loadStylesheet();
  }

  /*
   * when an att set is redeclared, att valu decls in the (more)local
   * redeclaration should override same-named decls in extended att set
   */
  public void testExtensionOverriding() {
    Document primary = getLoadedStylesheet().getPrincipalModule().getDocument(
        StylesheetModule.SIMPLIFIED);
    Node elem02n = primary.selectSingleNode("//xsl:element[@name='elem-02']");
    assertTrue("Xpath screwup", elem02n instanceof Element);
    Element elem02 = (Element) elem02n;

    Node att0n = elem02
        .selectSingleNode("xsl:attribute[@name='att0']/xsl:value-of/@select");
    assertTrue("Xpath screwup or escaping literal", att0n instanceof Attribute);
    assertTrue("att0 in bar should be 1 (override)", "'1'"
        .equals(((Attribute) att0n).getValue())
        || "1".equals(((Attribute) att0n).getValue()));

    Node att1n = elem02
        .selectSingleNode("xsl:attribute[@name='att1']/xsl:value-of/@select");
    assertTrue("Xpath screwup", att1n instanceof Attribute);
    assertEquals("att1 in bar should be 'local-variable-ok'",
        "'local-variable-ok'", ((Attribute) att1n).getValue());

    Node att2n = elem02
        .selectSingleNode("xsl:attribute[@name='att2']/xsl:value-of/@select");
    assertTrue("Xpath screwup", att2n instanceof Attribute);
    assertEquals("att2 in bar should be 'right'", "'right'",
        ((Attribute) att2n).getValue());

    Node att3n = elem02
        .selectSingleNode("xsl:attribute[@name='att3']/xsl:value-of/@select");
    assertTrue("Xpath screwup", att3n instanceof Attribute);
    assertEquals("att3 in bar should be 'use-site-override'",
        "'use-site-override'", ((Attribute) att3n).getValue());
  }

  public void testScopingBug() {
    Document primary = getLoadedStylesheet().getPrincipalModule().getDocument(
        StylesheetModule.SIMPLIFIED);
    Node elem03n = primary.selectSingleNode("//xsl:element[@name='elem-03']");
    assertTrue("Xpath screwup", elem03n instanceof Element);
    Element elem03 = (Element) elem03n;

    Node att2n = elem03
        .selectSingleNode("xsl:attribute[@name='att2']/xsl:value-of/@select");
    assertTrue("Xpath screwup", att2n instanceof Attribute);
    assertEquals("att2 in bar should be 'right'", "'right'",
        ((Attribute) att2n).getValue());
  }

  public void testEscapingLiteralsBug() {
    Document primary = getLoadedStylesheet().getPrincipalModule().getDocument(
        StylesheetModule.SIMPLIFIED);
    Node elem03n = primary.selectSingleNode("//xsl:element[@name='elem-03']");
    assertTrue("Xpath screwup", elem03n instanceof Element);
    Element elem03 = (Element) elem03n;

    Node att0n = elem03
        .selectSingleNode("xsl:attribute[@name='att0']/xsl:value-of");
    assertTrue("Escaping literal", att0n != null);

    Node att3n = elem03
        .selectSingleNode("xsl:attribute[@name='att3']/xsl:value-of");
    assertTrue("Escaping literal", att3n != null);
  }

  public static void main(String[] args) {
    TestRunner.run(AttributeSetResolutionTest.class);
  }
}
