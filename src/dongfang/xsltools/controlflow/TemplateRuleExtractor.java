/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-19
 */
package dongfang.xsltools.controlflow;

import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import dongfang.XMLConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.simplification.StylesheetProcessor;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * Extract template rules.
 * @author dongfang
 */
public class TemplateRuleExtractor implements StylesheetProcessor {

  private int templateRuleIndex;

  private ErrorReporter cesspool;

  protected TemplateRuleExtractor(ErrorReporter cesspool) {
    this.cesspool = cesspool;
  }

  private List<TemplateRule> allTemplateRules = new LinkedList<TemplateRule>();

  protected List<TemplateRule> getAllTemplateRules() {
    return allTemplateRules;
  }

  private void process(StylesheetModule dcon, int precedence)
      throws XSLToolsException {

    Element documentElement = dcon.getDocument(StylesheetModule.CORE)
        .getRootElement();

    for (Object o : documentElement.elements()) {
      Element template = (Element) o;
      if (template.getQName().equals(XSLConstants.ELEM_TEMPLATE_QNAME)) {
        try {
          TemplateRule tr = new TemplateRule(dcon, template, templateRuleIndex);
          allTemplateRules.add(tr);
          // TODO: OUTTA HERE!!!
          {
        	  dcon.addRuleParseLocation(tr);
              String templateKey = UniqueNameGenerator.getFreshId(templateRuleIndex, XMLConstants.CORE_ELEMENT_ENUMERATION_FORMAT);
        	  template.addAttribute(XMLConstants.TEMPLATE_IDENTIFIER_QNAME, templateKey);
          }
          templateRuleIndex++;
        } catch (XSLToolsXPathException ex) {
          cesspool.reportError(dcon, template, ParseLocation.Extent.TAG, ex);
        }
      }
    }
  }

  private int process(StylesheetLevel group, int precedence)
      throws XSLToolsException {

    for (StylesheetModule module : group.contents()) {
      process(module, precedence);
    }

    for (StylesheetLevel imported : group.imports()) {
      precedence = process(imported, precedence - 1);
    }

    return precedence;
  }

  public static TemplateRuleExtractor getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new TemplateRuleExtractor(cesspool);
  }

  public void process(StylesheetLevel group) throws XSLToolsException {
    process(group, 0);
  }

  public void process(Stylesheet stylesheet) throws XSLToolsException {
    int precedence = 0;
    if (stylesheet.getPrincipalLevel() != null)
      precedence = process(stylesheet.getPrincipalLevel(), 0);
    process(stylesheet, precedence - 1);
  }
}
