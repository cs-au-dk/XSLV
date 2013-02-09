/*
 * dongfang M. Sc. Thesis
 * Created on 2005-02-27
 */
package dongfang.xsltools.simplification;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.model.ModelConfiguration;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * Just an idea ... a Simplifier that takes a singular StylesheetModule, and
 * recursively eats up the whole inclusion and import tree. This could, of
 * course, not be made as a subclass of RecursiveSimplifier, as this presupposes
 * that the include and import resolution is already done... (well some fancy
 * proxy thing COULD be made, if there is the need...)
 * 
 * It might also be argued that this is loading, not simplification ;)
 * @author dongfang
 */
public class ExternalModuleLoader extends StylesheetSimplifierBase {
  private ErrorReporter cesspool;

  private ResolutionContext context;

  protected static ExternalModuleLoader getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    ExternalModuleLoader loader = new ExternalModuleLoader(cesspool);
    return loader;
  }

  /**
   * The resolver we will use for loading things.
   * @param context
   */
  void setContext(ResolutionContext context) {
    this.context = context;
  }

  private ExternalModuleLoader(ErrorReporter cesspool) {
    this.cesspool = cesspool;
  }

  /**
   * Resolve URIs and load stylesheet module
   * @param sbaseURI
   * @param relURI
   * @param groupnumber
   * @param modulenumber
   * @return
   * @throws XSLToolsLoadException
   */
  StylesheetModule load(String s_baseURI, String s_relURI, int groupnumber,
      int modulenumber) throws XSLToolsLoadException {
    try {
      
      URI baseURI = new URI(s_baseURI);
      URI importURI = new URI(s_relURI);
      URI total = baseURI.resolve(importURI);

      /*
       * We don't know which module (principal or not) this is, so ResolutionContext.MSG_XSL_ANY.
       */
      StylesheetModule id = ModelConfiguration.current
          .getStylesheetModuleFactory().createStylesheetModule(
              total.toString(), ResolutionContext.MSG_XSL_ANY, context, groupnumber, modulenumber, cesspool);

      return id;
    } catch (URISyntaxException e) {
      throw new XSLToolsLoadException("Invalid URI: " + s_relURI + " or " + s_baseURI + " at load", e);
    }
  }

  /**
   * Tear up xsl:include and xsl:import
   * @param source
   * @param level
   * @param levelnumber
   * @param modulenumber
   * @return
   * @throws XSLToolsException
   */
  int resolveIncludes(StylesheetModule source, StylesheetLevel level,
      int levelnumber, int modulenumber) throws XSLToolsException {
    Element documentRoot = source
        .getDocumentElement(StylesheetModule.CORE);

    /*
     * If document element is in a different NS than XSL, then we assume that
     * the stylesheet is simplified syntax (and then there are no
     * includes/imports to resolve)
     */
    if (!XSLConstants.NAMESPACE_URI.equals(documentRoot.getNamespaceURI())) {
      DocumentFactory fac = ModelConfiguration.current.getDocumentFactory();
      Element newTemplate = fac.createElement(XSLConstants.ELEM_TEMPLATE_QNAME);
      source.addOriginalElementId(newTemplate);
      source.addCoreElementId(newTemplate);
      newTemplate.addAttribute(XSLConstants.ATTR_MATCH, "/");
      documentRoot.detach();
      newTemplate.add(documentRoot);
      Element newStylesheet = fac
          .createElement(XSLConstants.ELEM_STYLESHEET_QNAME);
      source.addCoreElementId(newStylesheet);
      // TODO: 1.0??
      newStylesheet.addAttribute(XSLConstants.ATTR_VERSION, "1.0");
      newStylesheet.add(newTemplate);
      source.getDocument(StylesheetModule.CORE).add(newStylesheet);
    } else {
      for (Iterator it = source.getDocumentElement(StylesheetModule.CORE)
          .elementIterator(); it.hasNext();) {
        Element element = (Element) it.next();
        if (element.getQName().equals(XSLConstants.ELEM_INCLUDE_QNAME)) {
          String iURI_s = element.attributeValue(XSLConstants.ATTR_HREF_QNAME);
          try {
            modulenumber++;
            StylesheetModule included = load(source.getSystemId(), iURI_s,
                levelnumber, modulenumber);
            resolveIncludes(included, level, levelnumber, modulenumber);
          } catch (XSLToolsLoadException ex) {
            cesspool.reportError(ex);
          }
        }
      }
    }

    for (Iterator it = source.getDocumentElement(StylesheetModule.CORE)
        .elementIterator(); it.hasNext();) {
      Element element = (Element) it.next();
      if (element.getQName().equals(XSLConstants.ELEM_IMPORT_QNAME)) {
        String iURI_s = element.attributeValue(XSLConstants.ATTR_HREF_QNAME);
        try {
          levelnumber++;
          StylesheetModule imported = load(source.getSystemId(), iURI_s,
              levelnumber, 0);
          StylesheetLevel level2 = new StylesheetLevel();
          level.addImport(level2);
          levelnumber = resolveIncludes(imported, level2, levelnumber, 0);
        } catch (XSLToolsLoadException ex) {
          cesspool.reportError(source, element, ParseLocation.Extent.TAG, ex);
        }
      }
    }
    level.addContent(source);
    level.setSublevelNumberUpperBound(levelnumber);
    return levelnumber;
  }

  @Override
public void process(StylesheetLevel level) throws XSLToolsException {
    StylesheetModule source = level.contents().get(0);
    level.clear();
    resolveIncludes(source, level, 0, source.getModuleNumber());
  }
}
