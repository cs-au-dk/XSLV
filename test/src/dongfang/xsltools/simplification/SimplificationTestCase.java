/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Text;
import org.dom4j.Visitor;
import org.dom4j.VisitorSupport;
import org.xml.sax.InputSource;

import dk.brics.dsd.Schema;
import dongfang.XSLConstants;
import dongfang.xsltools.configuration.FactoryFactory;
import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.diagnostics.ShowStopperErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.model.StylesheetModuleFactory;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.resolver.URLResolutionContext;
import dongfang.xsltools.util.UniqueNameGenerator;
import dongfang.xsltools.util.Util;
import dongfang.xsltools.xmlclass.XMLClass;

/**
 * @author dongfang
 */
public class SimplificationTestCase extends TestCase {

  static {
    Logger stinkdyr = Logger.getLogger("xslv");
    stinkdyr.setLevel(Level.SEVERE);
  }

  public static final String TEST_PATH = "test/resources/";

  public static final File TEST_OUTPUT_PATH = new File("tmp");

  final String fileName;

  Stylesheet afterDefaultSimplificationStylesheet;

  protected SimplificationTestCase(String testName, String filename) {
    super(testName);
    this.fileName = filename;
  }

  protected Stylesheet loadStylesheet() throws Exception {

    ErrorReporter cesspool = new ShowStopperErrorReporter();
    UniqueNameGenerator gen = new UniqueNameGenerator();

    StylesheetModuleFactory fac = FactoryFactory.instance
        .getStylesheetModuleFactory();

    /*
     * Stylesheet stylesheet = fac.createStylesheet (fileName, new
     * LocalResolutionContext());
     */
    SemanticsPreservingSimplifier sim = SemanticsPreservingSimplifier
        .getInstance(new URLResolutionContext(),
            new ShowStopperErrorReporter(), new UniqueNameGenerator());
    // weird .. to contexts...
    // will fail at runtime !!!

    ValidationContext context = new ValidationContext() {
      public String getInputSchemaIdentifier() {
        return null;
      }

      public String getOutputSchemaIdentifier() {
        return null;
      }

      public String getStylesheetIdentifier() {
        return null;
      }

      public String resolveString(String id) throws IOException {
        return null;
      }

      public void earlyStringRequest(String id, short humanReadableKey) {
      }

      public void dropConstructedResources() {
      }

      public XMLClass getInputType() throws XSLToolsException {
        return null;
      }

      public Schema getOutputType() throws XSLToolsException {
        return null;
      }

      public void setStage(int stage) {
      }

      public int getStage() {
        return 0;
      }

      public void earlyStreamRequest(String s1, short key) {
      }

      public InputSource resolveStream(String systemId) throws IOException {
        ResolutionContext context = new URLResolutionContext();
        String urlString = Util.toUrlString(fileName);
        return context.resolveStream(urlString);
      }

      public InputSource resolveStream(String systemId, short humanReadableKey)
          throws IOException {
        return resolveStream(systemId);
      }

      public void reportError(String systemId, String message,
          ParseLocation location) {
        // TODO Auto-generated method stub

      }

      public void reportError(String systemId, String message) {
        // TODO Auto-generated method stub

      }
    };

    String urlString = Util.toUrlString(fileName);
    Stylesheet stylesheet = sim.getStylesheet(urlString, context, cesspool);

    ExternalModuleSaver saver = new ExternalModuleSaver(getName(),
        TEST_OUTPUT_PATH, cesspool);
    saver.process(stylesheet, StylesheetModule.SIMPLIFIED);
    afterDefaultSimplificationStylesheet = stylesheet;
    return stylesheet;
  }

  public Stylesheet getLoadedStylesheet() {
    return afterDefaultSimplificationStylesheet;
  }

  class Stinger extends VisitorSupport {
    Element currentElement;

    public void visit(Attribute node) {
      // check that selects are not unions
      if (currentElement.getQName().equals(
          XSLConstants.ELEM_APPLY_TEMPLATES_QNAME)
          && node.getQName().equals(XSLConstants.ATTR_SELECT_QNAME)) {
        assertFalse(node.getValue().indexOf("|") >= 0);
      }
      assertFalse("use-attribute-sets still there", node.getName().equals(
          XSLConstants.ATTR_USE_ATTRIBUTE_SETS));
    }

    public void visit(Element node) {
      currentElement = node;
      assertTrue("Non-XSL-namespace element: " + node, node.getNamespaceURI()
          .equals(XSLConstants.NAMESPACE_URI));

      assertFalse("call-template still there", node.getName().equals(
          XSLConstants.ELEM_CALL_TEMPLATE));
      assertFalse("decimal-format still there", node.getName().equals(
          XSLConstants.ELEM_DECIMAL_FORMAT));
      assertFalse("fallback still there", node.getName().equals(
          XSLConstants.ELEM_FALLBACK));
      assertFalse("for-each still there", node.getName().equals(
          XSLConstants.ELEM_FOR_EACH));
      assertFalse("if still there", node.getName().equals(XSLConstants.ELEM_IF));
      assertFalse("message still there", node.getName().equals(
          XSLConstants.ELEM_MESSAGE));
      assertFalse("output still there", node.getName().equals(
          XSLConstants.ELEM_OUTPUT));
      assertFalse("number still there", node.getName().equals(
          XSLConstants.ELEM_NUMBER));
      assertFalse("preserve-space still there", node.getName().equals(
          XSLConstants.ELEM_PRESERVE_SPACE));
      assertFalse("strip-space still there", node.getName().equals(
          XSLConstants.ELEM_STRIP_SPACE));

      if (node.getName().equals(XSLConstants.ELEM_TEMPLATE)) {
        assertTrue("Missing priority attr", node
            .attribute(XSLConstants.ATTR_PRIORITY_QNAME) != null);
        // assertTrue("Name attr still there",
        // node.attribute(XSLConstants.ATTR_NAME_QNAME)==null);
      }
      if (node.getName().equals(XSLConstants.ELEM_APPLY_TEMPLATES)) {
        assertTrue("Missing select attribute", node
            .attribute(XSLConstants.ATTR_SELECT_QNAME) != null);
      }
      if (node.getName().equals(XSLConstants.ELEM_CHOOSE)) {
        List hasOtherwise = node.selectNodes("xsl:otherwise");
        assertEquals("choose without one otherwise", 1, hasOtherwise.size());
      }
    }

    public void visit(Text node) {
      if (node.getText().length() == 0)
        System.err.println("Empty text %&¤%###¤%¤!!! node");
    }
  }

  class AllModulesIterator implements Iterator<StylesheetModule> {
    StylesheetLevel currentGroup;

    Iterator<StylesheetModule> currentModules;

    Iterator<StylesheetLevel> allGroups;

    AllModulesIterator(final Stylesheet stylesheet) {
      currentGroup = stylesheet.getPrincipalLevel();
      currentModules = currentGroup.contents().iterator();
      List<StylesheetLevel> temp = new LinkedList<StylesheetLevel>();
      suck(temp, currentGroup);
      allGroups = temp.iterator();
    }

    private void suck(List<StylesheetLevel> list, StylesheetLevel group) {
      list.add(group);
      for (StylesheetLevel imported : group.imports())
        suck(list, imported);
    }

    public boolean hasNext() {
      return currentModules.hasNext() || allGroups.hasNext();
    }

    public StylesheetModule next() {
      if (!currentModules.hasNext())
        currentModules = allGroups.next().contents().iterator();
      return currentModules.next();
    }

    public void remove() {// nixxe}
    }
  }

  protected Iterator<StylesheetModule> allModules(Stylesheet stylesheet) {
    return new AllModulesIterator(stylesheet);
  }

  protected Iterator<Document> allDocuments(final Stylesheet stylesheet,
      final int version) {
    final Iterator<StylesheetModule> source = new AllModulesIterator(stylesheet);
    return new Iterator<Document>() {
      public boolean hasNext() {
        return source.hasNext();
      }

      public Document next() {
        StylesheetModule module = source.next();
        return module.getDocument(version);
      }

      public void remove() {
      }
    };
  }

  protected Document getPrimarySimplifiedDocument() {
    Document primary = afterDefaultSimplificationStylesheet
        .getPrincipalModule().getDocument(StylesheetModule.SIMPLIFIED);
    return primary;
  }

  public void testAssertionsOnSimplifiedStylesheets() {
    Visitor stinger = new Stinger();
    for (Iterator<Document> allDocs = allDocuments(
        afterDefaultSimplificationStylesheet, StylesheetModule.SIMPLIFIED); allDocs
        .hasNext();) {
      allDocs.next().accept(stinger);
    }
  }
}
