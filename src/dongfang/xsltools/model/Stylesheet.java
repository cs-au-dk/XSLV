/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xml.sax.InputSource;

import dongfang.XMLConstants;
import dongfang.XSLConstants;
import dongfang.xslt.DefaultXSLT1Rules;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.diagnostics.ShowStopperErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.resolver.URLResolutionContext;
import dongfang.xsltools.simplification.Binding;
import dongfang.xsltools.simplification.Resolver;
import dongfang.xsltools.simplification.SemanticsPreservingSimplifier;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * Representation of an XSL style sheet. Basically just a wrapper around an
 * StylesheetLevel that: - forwards all resolution stuff - returns (and
 * records?) special "open parameter" values for resolution - implements the
 * default template rule addition - accepts any new template rules generated in
 * simplification
 * 
 * @author dongfang
 */
public class Stylesheet extends StylesheetModule implements Resolver {
  public static final int DEFAULT_RULES_LEVELNUMBER = 1000000000;

  public static Stylesheet xslt1BuiltInRuleStylesheet;

  private static DocumentFactory fac = ModelConfiguration.current
      .getDocumentFactory();

  static {
    final String DEFAULTRULES_SYSTEM_ID = "xslt1.0.defaults";

    try {
      ResolutionContext ct = new URLResolutionContext() {
        @Override
		public InputSource resolveStream(String systemId) throws IOException {
          if (systemId.equals(DEFAULTRULES_SYSTEM_ID)) {
            InputSource so = new InputSource(systemId);
            // InputStream is = ClassLoader.getSystemResourceAsStream(systemId);
            // logger.info("Loading over classloader from " + systemId + " : " +
            // (is==null ? "failure(null)" : "success"));
            // so.setByteStream(is);
            // so.setCharacterStream(new InputStreamReader(is, "utf-8"));
            so.setCharacterStream(DefaultXSLT1Rules.getReader());
            return so;
          }
          return super.resolveStream(systemId);
        }
      };

      xslt1BuiltInRuleStylesheet = ModelConfiguration.current
          .getStylesheetModuleFactory().createStylesheet(
              DEFAULTRULES_SYSTEM_ID, null, ct, new ShowStopperErrorReporter());

      // just in case clone() is run on the stylesheet (as in the timing
      // composite simplifier),
      // provide the original document in advance.
      xslt1BuiltInRuleStylesheet.setDocument(xslt1BuiltInRuleStylesheet
          .getPrincipalModule().getDocument(ORIGINAL), ORIGINAL);

      // Stylesheet debug = xslt1BuiltInRuleStylesheet;

      // TODO: Why not move over the stylesheet before processing??

      PerformanceLogger pa = DiagnosticsConfiguration.current
          .getPerformanceLogger();

      pa.startTimer("SimplificationProper", "Simplification");

      SemanticsPreservingSimplifier.getInstance(ct,
          new ShowStopperErrorReporter(), new UniqueNameGenerator()).process(
          xslt1BuiltInRuleStylesheet);

      pa.stopTimer("SimplificationProper", "Simplification");

      // plunder contents from principal module...
      xslt1BuiltInRuleStylesheet.setDocument(xslt1BuiltInRuleStylesheet
          .getPrincipalModule().getDocument(CORE), CORE);
      xslt1BuiltInRuleStylesheet.setDocument(xslt1BuiltInRuleStylesheet
          .getPrincipalModule().getDocument(ORIGINAL), ORIGINAL);
      xslt1BuiltInRuleStylesheet.idCounters[ORIGINAL] = xslt1BuiltInRuleStylesheet
          .getPrincipalModule().idCounters[ORIGINAL];
      xslt1BuiltInRuleStylesheet.idCounters[CORE] = xslt1BuiltInRuleStylesheet
          .getPrincipalModule().idCounters[CORE];
      xslt1BuiltInRuleStylesheet.elementsById = xslt1BuiltInRuleStylesheet
          .getPrincipalModule().elementsById;
      ModelConfiguration.XPathCacheConfig xPathCacheConfig = ModelConfiguration.current
          .getXPathCacheConfig();
      if (xPathCacheConfig != ModelConfiguration.XPathCacheConfig.NO_XPATH_CACHE)
        xslt1BuiltInRuleStylesheet.xPathExpressionCache = xslt1BuiltInRuleStylesheet
            .getPrincipalModule().xPathExpressionCache;
      // and even kill it afterwards
      xslt1BuiltInRuleStylesheet.principalLevel = null;
      // xslt1BuiltInRuleStylesheet.reassignCoreIDs(xslt1BuiltInRuleStylesheet.getDocument(SIMPLIFIED));
      // xslt1BuiltInRuleStylesheet.baseURI = new URI("xslt1:builtin-rules");
      // Dom4jUtil.debugPrettyPrint(xslt1BuiltInRuleStylesheet.getDocument(ORIGINAL));
      // Dom4jUtil.debugPrettyPrint(xslt1BuiltInRuleStylesheet.getDocument(SIMPLIFIED));
    } catch (Exception e) {
      // this should never fail.
      e.printStackTrace();
      throw new AssertionError(e.getCause());
    }
  }

  private StylesheetLevel principalLevel;

  /*
   * This Document will contain the built in template rules (for XSLT 1.0),
   * along with some (although not all) template rules created through the
   * simplification process (DONE: Decision on this!)
   */

  private Element includerBootstrap;

  public Stylesheet() {
    super(XMLConstants.VALIDATOR_NAMESPACE_URI, null, null,
        DEFAULT_RULES_LEVELNUMBER, 0);
    // if we are not at the static initializer of the default rules....

    Element rootElement = fac.createElement(XSLConstants.ELEM_STYLESHEET_QNAME);
    includerBootstrap = fac.createElement(XSLConstants.ELEM_INCLUDE_QNAME);
    includerBootstrap.addAttribute(XSLConstants.ATTR_HREF_QNAME, "");
    addCoreElementId(rootElement);
    addCoreElementId(includerBootstrap);

    rootElement.addText("\n  ");
    rootElement.add(includerBootstrap);
    rootElement.addText("\n");

    setDocument(fac.createDocument(rootElement), CORE);

    if (xslt1BuiltInRuleStylesheet == null) {
      // do nothing .. the document element
    } else {
      /*
       * We overwrite the stylesheet element of the principal module, so we
       * might as well give the overwrite the overwritten's cid, zero
       */
      idCounters[CORE] = 0;
      addCoreElementId(rootElement);
      /*
       * Let cid start from beyond last of default rules cid
       */
      idCounters[CORE] = xslt1BuiltInRuleStylesheet.idCounters[CORE];
      idCounters[ORIGINAL] = xslt1BuiltInRuleStylesheet.idCounters[ORIGINAL] + 100;

      addCoreElementId(includerBootstrap);

      setDocument((Document) xslt1BuiltInRuleStylesheet.getDocument(ORIGINAL)
          .clone(), ORIGINAL);
      /*
       * Copy in the rules from the default-rules stylesheet
       */
      for (Iterator iter = xslt1BuiltInRuleStylesheet.getDocumentElement(
          CORE).elementIterator(); iter.hasNext();) {
        Element toplevel = (Element) iter.next();
        addTemplate((Element) toplevel.clone());
        // NOTE: Much easier to just reassign core IDs here.
      }

      /* Stylesheet debug = xslt1BuiltInRuleStylesheet; */

      // rebuildIdToElementMaps();
      // reassignCoreIDs(rootElement);
      ModelConfiguration.XPathCacheConfig xPathCacheMode = ModelConfiguration.current
          .getXPathCacheConfig();
      if (xPathCacheMode != ModelConfiguration.XPathCacheConfig.NO_XPATH_CACHE) {
        xPathExpressionCache = new HashMap<String, XPathExpressionValue>(
            xslt1BuiltInRuleStylesheet.xPathExpressionCache);
      }
    }
  }

  /*
   * Just for cloning really.
   */
  Stylesheet(String systemId, Document simplified, Document original,
      int levelnumber, int modulenumber) {
    super(systemId, simplified, original, levelnumber, modulenumber);
  }

  @Override
  public String getSystemId() {
    return XMLConstants.VALIDATOR_NAMESPACE_URI;
  }

  /**
   * This implementation will make the top level resolver an
   * ImportPrecedenceGroup, not a Stylesheet, which may have looked a little
   * nicer in debugging
   */
  public Resolver enterScope() {
    return principalLevel.enterScope();
  }

  public Binding resolve(QName name, short scope) throws XSLToolsException {
    return resolve(name, scope, this);
  }

  public Binding resolve(QName name, short scope, Resolver base)
      throws XSLToolsException {
    Binding binding = principalLevel.resolve(name, scope);
    // if (binding == null); //do something;
    return binding;
  }

  public Binding resolveLocalScope(QName name, short scope) {
    throw new AssertionError("ResolveLocalScope called on Stylesheet");
  }

  public void bind(QName name, Binding value) throws XSLToolsException {
    principalLevel.bind(name, value);
  }

  public short resolverScope() {
    return TOPLEVEL_SCOPE;
  }

  public void resolutionDiagnostics(Branch parent, DocumentFactory fac) {
    Element e = fac.createElement("resolution-diagnostics");
    parent.add(e);
    principalLevel.resolutionDiagnostics(e, fac);
  }

  public short getScoop() {
    return Resolver.TOPLEVEL_SCOPE;
  }

  /**
   * Return a document describing the structure the the <code>Stylesheet</code>
   * 
   * @param parent
   * @param fac
   */
  public void structureDiagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element e = fac.createElement("structure-diagnostics");
    parent.add(e);
    principalLevel.diagnostics(e, fac, configuration);
  }

  public StylesheetModule getPrincipalModule() {
    if (principalLevel == null)
      return null;
    return principalLevel.getPrincipalModule();
  }

  public StylesheetLevel getPrincipalLevel() {
    return principalLevel;
  }

  void setPrincipalGroup(StylesheetLevel group) {
    principalLevel = group;
    Attribute href = includerBootstrap.attribute(XSLConstants.ATTR_HREF);
    href.setValue(getPrincipalModule().getSystemId());
  }

  /*
  private void addDefaultXSLT1ModedTemplate(QName mode, String matchPattern) {
    Element template = fac.createElement(XSLConstants.ELEM_TEMPLATE_QNAME);
    addOriginalElementId(template);
    template.addAttribute(XSLConstants.ATTR_MATCH, matchPattern);
    String template_modestr = Dom4jUtil.makeAttributeValue(mode, template);
    template.addAttribute(XSLConstants.ATTR_MODE, template_modestr);
    template.addAttribute(XSLConstants.ATTR_PRIORITY, "0");

    Element apply = fac.createElement(XSLConstants.ELEM_APPLY_TEMPLATES_QNAME);
    addOriginalElementId(apply);
    apply.addAttribute(XSLConstants.ATTR_SELECT, "child::node()");
    String apply_mode = Dom4jUtil.makeAttributeValue(mode, apply);
    apply.addAttribute(XSLConstants.ATTR_MODE, apply_mode);

    Element coreTemplate = (Element) template.clone();
    Element coreApply = (Element) apply.clone();

    template.add(apply);

    // yup!! not joking, we do alter the original document.
    getDocumentElement(ORIGINAL).add(template);
    // addElementById(template_eid, template, ORIGINAL);
    // addElementById(apply_eid, apply, ORIGINAL);

    reassignCoreIDs(coreTemplate);
    reassignCoreIDs(coreApply);

    coreTemplate.add(coreApply);
    addTemplate(coreTemplate);
  }
  
  public void addDefaultXSLT1ModedTemplate(QName mode) {
    addDefaultXSLT1ModedTemplate(mode, "child::*");
    addDefaultXSLT1ModedTemplate(mode, "/");
  }
  */

  void addTemplate(Element template) {
    getDocumentElement(CORE).add(template);
  }

  public StylesheetModule getModule(StylesheetModule image) {
    return getModule(image.getSystemId());
  }

  public StylesheetModule getModule(String uri) {
    if (uri.equals(baseURI))
      return this;
    if (principalLevel != null)
      return principalLevel.getModule(uri);
    return null;
  }

  @Override
public String getHierarchialName() {
    return "default-rules";
  }

  public List<StylesheetModule> getAllModules() {
    List<StylesheetModule> result = new LinkedList<StylesheetModule>();
    getPrincipalLevel().getAllModules(result);
    result.add(this);
    return result;
  }

  @Override
public Object clone() {
    Document simplified = (Document) getDocument(CORE).clone();
    Document original = (Document) getDocument(ORIGINAL).clone();
    Stylesheet clone = new Stylesheet(baseURI, simplified, original,
        levelnumber, modulenumber);
    if (getPrincipalLevel() != null)
      clone.principalLevel = (StylesheetLevel) getPrincipalLevel().clone();
    clone.idCounters[CORE] = idCounters[CORE];
    clone.idCounters[ORIGINAL] = idCounters[ORIGINAL];
    clone.rebuildIdToElementMaps();
    ModelConfiguration.XPathCacheConfig xPathCacheMode = ModelConfiguration.current
        .getXPathCacheConfig();
    if (xPathCacheMode != ModelConfiguration.XPathCacheConfig.NO_XPATH_CACHE)
      clone.xPathExpressionCache = new HashMap<String, XPathExpressionValue>(
          xPathExpressionCache);
    return clone;
  }
}
