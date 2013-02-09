/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Visitor;
import org.dom4j.VisitorSupport;

import dongfang.XMLConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * Put unique df:cid on each element. Supposed to be run on core XSLT documents
 * only (possibly before approximative simplification)
 * 
 * Not needed anymore!
 * 
 * @author dongfang
 */
public class CoreTemplateIdAnnotator implements StylesheetProcessor {
  private UniqueNameGenerator names;

  static CoreTemplateIdAnnotator getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new CoreTemplateIdAnnotator(names);
  }

  CoreTemplateIdAnnotator(UniqueNameGenerator names) {
    this.names = names;
  }

  void annotate(final StylesheetModule module) {
    // TODO: Should the generator not be reset???
    // names.reset();

    Document doc = module.getDocument(StylesheetModule.CORE);

    Visitor annotatorVisitor = new VisitorSupport() {
      @Override
	public void visit(Element element) {
        if (element.getNamespaceURI().equals(XSLConstants.NAMESPACE_URI)) {

          // Attribute at =
          // element.attribute(XMLConstants.ELEMENT_CORE_ID_QNAME);
          // if (at!=null)
          // throw new RuntimeException("Already had core id! " +
          // element);

          if (element.getName().equals(XSLConstants.ELEM_TEMPLATE)
              || element.getName().equals(XSLConstants.ELEM_APPLY_TEMPLATES)) {
            String coreKey = element.attributeValue(XMLConstants.ELEMENT_CORE_ID_QNAME);
            if (coreKey == null) {
              coreKey = names.getFreshId(XMLConstants.CORE_ELEMENT_ENUMERATION_FORMAT);
              element.addAttribute(XMLConstants.ELEMENT_CORE_ID_QNAME, coreKey);
            }
            module.addElementById(coreKey, element, StylesheetModule.CORE);
          }
        }
      }
    };
    doc.accept(annotatorVisitor);
  }

  public void process(StylesheetLevel group) {
    for (StylesheetModule module : group.contents()) {
      annotate(module);
    }
    for (StylesheetLevel imported : group.imports()) {
      process(imported);
    }
  }

  public void process(Stylesheet stylesheet) {
    throw new AssertionError("Outdated code");
  }
}
