/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.diagnostics;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.VisitorSupport;

import dongfang.XMLConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.simplification.StylesheetProcessor;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * Converts simplified stylesheet modules back to legal XSL, that is, match
 * pattern axes are abbreviated.
 * 
 * @author dongfang
 */
public class XPathCollector implements StylesheetProcessor {

  static boolean removeDFAttributes = true;

  private static XPathCollector instance;

  public static XPathCollector getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    if (instance == null)
      instance = new XPathCollector();
    return instance;
  }

  List<String> xpathTemplatePatternList = new LinkedList<String>();

  List<String> xpathTemplateInvokerSelectionList = new LinkedList<String>();

  List<String> xpathOtherSelectionList = new LinkedList<String>();

  class Blah extends VisitorSupport {
    @Override
    public void visit(Element element) {

      for (Iterator attributes = element.attributeIterator(); attributes
          .hasNext();) {
        Attribute attribute = (Attribute) attributes.next();
        if (attribute.getQName().equals(XSLConstants.ATTR_MATCH_QNAME)) {
          xpathTemplatePatternList.add(attribute.getValue());
        } else if (attribute.getQName().equals(XSLConstants.ATTR_SELECT_QNAME)) {
          if (element.getQName()
              .equals(XSLConstants.ELEM_APPLY_TEMPLATES_QNAME))
            xpathTemplateInvokerSelectionList.add(attribute.getValue());
          else
            xpathOtherSelectionList.add(attribute.getValue());
        } else if (removeDFAttributes
            && attribute.getNamespaceURI().equals(XMLConstants.DONGFANG_URI)) {
        }
      }
    }
  }

  private Blah vs = new Blah();

  private void process(StylesheetModule module) {
    module.getDocument(StylesheetModule.CORE).accept(vs);
  }

  private void process(StylesheetLevel level) {
    for (StylesheetModule module : level.contents()) {
      process(module);
    }
    for (StylesheetLevel imported : level.imports()) {
      process(imported);
    }
  }

  public void process(Stylesheet stylesheet) {
    xpathTemplatePatternList.clear();
    xpathTemplateInvokerSelectionList.clear();
    xpathOtherSelectionList.clear();
    process((StylesheetModule) stylesheet);
    process(stylesheet.getPrincipalLevel());
    try {
      URI n = new URI(stylesheet.getSystemId());
      String fn = n.getRawSchemeSpecificPart();
      int slashIdx = fn.lastIndexOf("/");
      if (slashIdx >= 0)
        fn = fn.substring(slashIdx + 1);
      saveXPath("tmp/" + fn + ".matchpatterns.xpath", xpathTemplatePatternList);
      saveXPath("tmp/" + fn + ".invocation-selections.xpath",
          xpathTemplateInvokerSelectionList);
      saveXPath("tmp/" + fn + ".other-selections.xpath",
          xpathOtherSelectionList);
    } catch (IOException ex) {
      System.err.println("XPathCollector had trouble saving: "
          + ex.getMessage());
    } catch (URISyntaxException ex) {
      System.err.println("XPathCollector had trouble: " + ex.getMessage());
    }
  }

  public void saveXPath(String filename, List<String> explist)
      throws IOException {
    PrintStream of = new PrintStream(filename);
    boolean needsSep = false;
    for (String s : explist) {
      if (needsSep) {
        of.println("-");
      } else
        needsSep = true;
      of.println(s);
    }
    of.close();
  }
}