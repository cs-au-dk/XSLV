/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.diagnostics;

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
public class ToLegalXSLConverter implements StylesheetProcessor {

  static boolean removeDFAttributes = true;

  private static final String childAxis = "child::";

  private static final String attrAxis = "attribute::";

  private static final String slashSlashAxis = "/descendant-or-self::node()/";

  private static ToLegalXSLConverter instance;

  public static ToLegalXSLConverter getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    if (instance == null)
      instance = new ToLegalXSLConverter();
    return instance;
  }

  private static String badamm(String xpath) {
    int idx;
    if ((idx = xpath.indexOf(childAxis)) >= 0) {
      return badamm(xpath.substring(0, idx))
          + badamm(xpath.substring(idx + childAxis.length(), xpath.length()));
    }
    if ((idx = xpath.indexOf(attrAxis)) >= 0) {
      return badamm(xpath.substring(0, idx)) + "@"
          + badamm(xpath.substring(idx + attrAxis.length(), xpath.length()));
    }
    if ((idx = xpath.indexOf(slashSlashAxis)) >= 0) {
      return badamm(xpath.substring(0, idx))
          + "//"
          + badamm(xpath.substring(idx + slashSlashAxis.length(), xpath
              .length()));
    }
    return xpath;
  }

  static class Blah extends VisitorSupport {
    List<Attribute> attributeDeathList = new LinkedList<Attribute>();

    /*
     * @Override public void visit(Attribute attribute) { if
     * (attribute.getQName().equals(XSLConstants.ATTR_MATCH_QNAME)) {
     * attribute.setValue(badamm(attribute.getValue())); } else if
     * (removeDFAttributes &&
     * attribute.getNamespaceURI().equals(XMLConstants.DONGFANG_URI)) {
     * attribute.detach(); } }
     */
    @Override
    public void visit(Element element) {

      for (Iterator<Attribute> attributes = element.attributeIterator(); attributes
          .hasNext();) {
        Attribute attribute = attributes.next();
        if (attribute.getQName().equals(XSLConstants.ATTR_MATCH_QNAME)) {
          attribute.setValue(badamm(attribute.getValue()));
        } else if (attribute.getQName().equals(XSLConstants.ATTR_SELECT_QNAME)) {
          // attribute.setValue(unescape(attribute.getValue()));
        } else if (removeDFAttributes
            && attribute.getNamespaceURI().equals(XMLConstants.DONGFANG_URI)) {
          // attributes.remove();
          attributeDeathList.add(attribute);
        }
      }

      if (removeDFAttributes) {
        for (Attribute attribute : attributeDeathList) {
          attribute.detach();
        }
        attributeDeathList.clear();
      }
    }
  }

  static private Blah vs = new Blah();

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
    process(stylesheet.getPrincipalLevel());
    process((StylesheetModule) stylesheet);
  }
}