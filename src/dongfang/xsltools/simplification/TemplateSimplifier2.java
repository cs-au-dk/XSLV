/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.QName;

import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsResolverNotFoundException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * Turns named template rules into unnamed ones. Turns call-templates into
 * apply-templates that hit the same templates, usingf fresh modes.
 * 
 * @author dongfang
 */
public class TemplateSimplifier2 extends StructuralSimplifierBase {

  private ErrorReporter cesspool;

  protected static TemplateSimplifier2 getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new TemplateSimplifier2(cesspool, names);
  }

  private TemplateSimplifier2(ErrorReporter cesspool, UniqueNameGenerator names) {
    this.cesspool = cesspool;
  }

  @Override
protected void simplify(Element element, Resolver resolver,
      Resolver groupscope, StylesheetModule module, int depth)
      throws XSLToolsException {

    if (element.getNamespaceURI().equals(XSLConstants.NAMESPACE_URI)) {

      // simplification #19
      if (element.getName().equals(XSLConstants.ELEM_CALL_TEMPLATE)) {
        Attribute templateNameAtt = element
            .attribute(XSLConstants.ATTR_NAME_QNAME);

        if (templateNameAtt == null)
          cesspool.reportError(module, element, ParseLocation.Extent.TAG,
              "A call-template had no name attribute");

        else {
          String templateName = templateNameAtt.getValue();

          try {
            QName bindName = ElementNamespaceExpander
                .qNameForXSLAttributeValue(templateName, element,
                    NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);

            TemplateBinding targetBinding = (TemplateBinding) resolver.resolve(
                bindName, Resolver.TEMPLATE_SYMBOLSPACE);

            if (targetBinding == null)
              throw new XSLToolsResolverNotFoundException(bindName,
                  Resolver.TEMPLATE_SYMBOLSPACE);

            QName freshMode = targetBinding.getMode();

            // ((Stylesheet) resolver).addDefaultXSLT1ModedTemplate(freshMode);

            element.setQName(QName.get(XSLConstants.ELEM_APPLY_TEMPLATES,
                element.getNamespace()));

            templateNameAtt.detach();

            element.addAttribute(XSLConstants.ATTR_SELECT, "self::node()");

            element.addAttribute(XSLConstants.ATTR_MODE, "#current");

            Dom4jUtil.transferAttributeValue(freshMode, XSLConstants.ATTR_MODE,
                element);
          } catch (XSLToolsException ex) {
            cesspool.reportError(module, element, ParseLocation.Extent.TAG, ex);
          }
        }
      }
    }
    simplifyBelow(element, resolver, null, module, depth);
  }

  @Override
protected void simplify(StylesheetModule module, Stylesheet resolver)
      throws XSLToolsException {
    Element documentElement = module
        .getDocumentElement(StylesheetModule.CORE);
    simplify(documentElement, resolver, null, module, 0);
  }

  @Override
public void process(StylesheetLevel level) {
    throw new AssertionError("Don't call me!");
  }

  @Override
public void process(Stylesheet stylesheet) throws XSLToolsException {
    process(stylesheet.getPrincipalLevel(), stylesheet);
  }
}