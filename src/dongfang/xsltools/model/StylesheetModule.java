/*
 * dongfang M. Sc. Thesis
 * Created on 18-02-2005
 */
package dongfang.xsltools.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Visitor;
import org.dom4j.VisitorSupport;

import dongfang.XMLConstants;
import dongfang.xsltools.controlflow.ApplyTemplatesInst;
import dongfang.xsltools.controlflow.TemplateRule;
import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.diagnostics.ParseLocationUtil;
import dongfang.xsltools.exceptions.XSLToolsXPathParseException;
import dongfang.xsltools.util.UniqueNameGenerator;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathParser;

/**
 * Representation of a stylesheet module.
 * 
 * @author dongfang
 */
public class StylesheetModule implements Comparable<StylesheetModule>, Cloneable, Diagnoseable {

  /*
   * For the XPath expression cache thing. @author dongfang
   */
  class XPathExpressionValue {
    XPathExpr expression;

    String expressionAsString;

    XPathExpressionValue(XPathExpr expression) {
      this.expression = expression;
    }

    @Override
	public String toString() {
      if (expressionAsString == null) {
        expressionAsString = expression.toString();
      }
      return expressionAsString;
    }
  }

  /**
   * The original stylesheet, possibly decorated with pase locations
   */
  public static final short ORIGINAL = 0;

  /**
   * The simplified stylesheet, mutilated beyond recognition
   */
  public static final short CORE = 1;

  static final Logger logger = Logger.getLogger("xslv.xpathcache");

  static {
    logger.setLevel(ModelConfiguration.current.debugXPathCache() ? Level.FINE : Level.WARNING);
  }

  /*
   * The original, simplified documents.
   */
  private final Document[] documents = new Document[2];

  final String baseURI;

  final int[] idCounters = new int[2];

  /*
   * Number of the stylesheet level that this module belongs to. High number is
   * low import precedence.
   */
  final int levelnumber;

  /*
   * Number of the stylesheet module within its level
   */
  final int modulenumber;

  /*
   * int sublevelUpperBound is the number after the colon (and levelnumber is
   * the one before).
   */
  int sublevelUpperBound;

  /*
   * Parsed XPath expression cache. Depends on elements being decorated with
   * df:eid to work. TODO: Take care that newly created elements in simplifiers
   * get an eid.
   */
  Map<String, XPathExpressionValue> xPathExpressionCache = new HashMap<String, XPathExpressionValue>();

  /*
   * Map from eids to elements.
   */
  Map[] elementsById = new Map[2];

  /**
   * Only constructor
   * 
   * @param base
   * @param simplified
   * @param original
   * @param levelnumber
   * @param modulenumber
   */
  StylesheetModule(String base, Document simplified, Document original, int levelnumber, int modulenumber) {
    this.documents[CORE] = simplified;
    this.documents[ORIGINAL] = original;
    this.baseURI = base;
    this.levelnumber = levelnumber;
    this.modulenumber = modulenumber;
    elementsById[0] = new HashMap();
    elementsById[1] = new HashMap();
  }

  /**
   * Return ORIGINAL or SIMPLIFIED document.
   * 
   * @param version
   * @return
   */
  public Document getDocument(int version) {
    return documents[version];
  }

  /**
   * Set SIMPLIFIED or ORIGINAL document.
   * 
   * @param document
   * @param version
   */
  public void setDocument(Document document, int version) {
    this.documents[version] = document;
  }

  /**
   * Get the document element of <code>ORIGINAL</code> or
   * <code>SIMPLIFIED</code> stylesheet module document.
   * 
   * @param version
   * @return
   */
  public Element getDocumentElement(int version) {
    return documents[version].getRootElement();
  }

  public String getSystemId() {
    return baseURI;
  }

  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement("stylesheet-module");
    me.addAttribute("systemId", baseURI);
    me.addAttribute("levelnumber", Integer.toString(levelnumber));
    me.addAttribute("modulenumber", Integer.toString(modulenumber));
    // me.addAttribute("precedence", Integer.toString(importPrecedence));
    Element contents = fac.createElement("contents");
    me.add(contents);
    if (getDocument(CORE) != null)
      contents.add((Element) getDocumentElement(CORE).clone());
    contents = fac.createElement("original-contents");
    me.add(contents);
    if (getDocument(ORIGINAL) != null)
      contents.add((Element) getDocumentElement(ORIGINAL).clone());
    parent.add(me);
  }

  /**
   * Obsolete? ParseLocations have no state no more...
   * 
   * @param elementIdentifier
   * @return
   */
  /*
   * ParseLocation newParseLocation(String elementIdentifier) { ParseLocation pp =
   * new ParseLocation(); return pp; }
   */
  /**
   * Update ID of an element in the element ID map.
   * 
   * @param elementIdentifier
   * @param element
   * @param version -
   *          <code>ORIGINAL</code> or <code>SIMPLIFIED</code>
   */
  public void addElementById(String elementIdentifier, Element element, int version) {
    elementsById[version].put(elementIdentifier, element);
  }

  /**
   * Get element referred to by ID
   * 
   * @param elementIdentifier
   * @param version -
   *          <code>ORIGINAL</code> or <code>SIMPLIFIED</code>
   * @return
   */
  public Element getElementById(String elementIdentifier, int version) {
    return (Element) elementsById[version].get(elementIdentifier);
  }

  /**
   * Add id on original element. This will only happen on the original document
   * (simplified will be cloned from original).
   * 
   * @param element
   */
  public void addOriginalElementId(Element element) {
    String elementIdentifier = UniqueNameGenerator.getFreshId(idCounters[ORIGINAL]++,
        XMLConstants.ELEMENT_ENUMERATION_FORMAT);
    addElementById(elementIdentifier, element, ORIGINAL);
    element.addAttribute(XMLConstants.ELEMENT_ID_QNAME, elementIdentifier);
  }

  /**
   * Add core id on core element. This will only happen on the simplified
   * document.
   * 
   * @param element
   */
  public void addCoreElementId(Element element) {
    Attribute a = element.attribute(XMLConstants.ELEMENT_CORE_ID_QNAME);
    String elementIdentifier = UniqueNameGenerator.getFreshId(idCounters[CORE]++,
        XMLConstants.CORE_ELEMENT_ENUMERATION_FORMAT);
    if (a != null) {
      //System.err.println("Warning! Element was reassigned a core ID! 2nd assignment culprit was:");
      //new RuntimeException().printStackTrace();
      a.setValue(elementIdentifier);
    } else {
      element.addAttribute(XMLConstants.ELEMENT_CORE_ID_QNAME, elementIdentifier);
    }
    addElementById(elementIdentifier, element, CORE);
  }

  /**
   * Cache a parsed XPath expression.
   * 
   * @param elementIdentifier
   * @param attributeName
   * @param exp
   */
  private void cacheXPathExpression(String cid, String attributeName, XPathExpr exp) {
    ModelConfiguration.XPathCacheConfig cacheConfig = ModelConfiguration.current.getXPathCacheConfig();
    if (cacheConfig == ModelConfiguration.XPathCacheConfig.NO_XPATH_CACHE || cid == null) {
      if (cacheConfig != ModelConfiguration.XPathCacheConfig.NO_XPATH_CACHE && logger.isLoggable(Level.FINE))
        throw new NullPointerException(baseURI + ": Could not cache expression: " + exp
            + " for eid: null, attribute-name: " + attributeName);
      return;
    }
    xPathExpressionCache.put(cid + ":" + attributeName, new XPathExpressionValue(exp));
    if (logger.isLoggable(Level.FINE))
      logger.fine(baseURI + ": Cached expression: " + exp + " for cid: " + cid + ", attribute-name: " + attributeName);
  }

  public void cacheXPathExpression(Element carrier, String attributeName, XPathExpr exp) {
    String cid = carrier.attributeValue(XMLConstants.ELEMENT_CORE_ID_QNAME);
    if (cid == null)
      throw new NullPointerException("Element used for xpath caching had no cid");
    cacheXPathExpression(cid, attributeName, exp);
  }

  /**
   * Delete an XPath expression from cache.
   * 
   * @param elementIdentifier
   * @param attributeName
   */
  void unCacheXPathExpression(String cid, String attributeName) {
    ModelConfiguration.XPathCacheConfig cacheConfig = ModelConfiguration.current.getXPathCacheConfig();
    if (cacheConfig == ModelConfiguration.XPathCacheConfig.NO_XPATH_CACHE || cid == null)
      return;
    xPathExpressionCache.remove(cid + ":" + attributeName);
    if (logger.isLoggable(Level.FINE))
      logger.fine(baseURI + ": Removed cache value for eid: " + cid + ", attribute-name: " + attributeName);
  }

  public void unCacheXPathExpression(Element carrier, String attributeName) {
    ModelConfiguration.XPathCacheConfig cacheConfig = ModelConfiguration.current.getXPathCacheConfig();
    if (cacheConfig == ModelConfiguration.XPathCacheConfig.NO_XPATH_CACHE)
      return;
    String cid = carrier.attributeValue(XMLConstants.ELEMENT_CORE_ID_QNAME);
    unCacheXPathExpression(cid, attributeName);
  }

  private String getNoPrefixNS(Element context) {
    // the XPath1 thing to do.
    return NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE;
  }

  /**
   * Gets a parsed XPath expression from the cache, checking that the parsed
   * expression still is the one given as a string. If not, it is re-parsed and
   * re-cached. This is the preferred way to use the XPath expression cache. The
   * test is fairly fast, and ensures that no nasty divergence between cached
   * expressions and expressions in the attributes of the XSLT document under
   * processing occur. Of course, it would be even better to keep the cache
   * updated consistently, but the simplification code originally had no XPath
   * expression cache, but lots of changes to XPath attributes. This is safer.
   * 
   * @param elementIdentifier
   * @param attributeName
   * @param asString -
   *          The canonical string representation of the expression
   * @param context -
   *          An element used for resolving namespaces if re-parsing. Typically
   *          (always), <code>asString</code> is an attribute value, and
   *          <code>context</code> is the owner element of that attribute.
   * @return
   */
  public XPathExpr getCachedXPathExp(String attributeName, String attributeValue, Element context, Element _hackVictim)
      throws XSLToolsXPathParseException {

    ModelConfiguration.XPathCacheConfig cacheConfig = ModelConfiguration.current.getXPathCacheConfig();

    XPathExpr cachedExp;

    if (cacheConfig == ModelConfiguration.XPathCacheConfig.NO_XPATH_CACHE) {
      cachedExp = XPathParser.parse(attributeValue, context, getNoPrefixNS(context));
      return cachedExp;
    }

    // String elementIdentifier =
    // context.attributeValue(XMLConstants.ELEMENT_ID_QNAME);
    String cid = context.attributeValue(XMLConstants.ELEMENT_CORE_ID_QNAME);

    if (cid == null)
      // logger.warning(getHierarchialName() + ": Cache lookup failed, null
      // cid");
      throw new NullPointerException("Cache lookup failed, null cid");

    XPathExpressionValue v = null;

    v = xPathExpressionCache.get(cid + ":" + attributeName);

    if (v == null
        || (cacheConfig == ModelConfiguration.XPathCacheConfig.SELFCHECK_XPATH_CACHE && !(v.toString()
            .equals(attributeValue)))) {
      // System.err.println("XPathExp attribute changed relative to cached
      // parsed copy: Cached:" + cachedExp + " parsed:" + xpathExp);
      if (v != null) {
        logger.warning(baseURI + ": XPath expression mutated. Attribute name: " + attributeName + ". Incache value: "
            + v.toString() + ", attribute value: " + attributeValue);
        new RuntimeException().printStackTrace();
      } else if (logger.isLoggable(Level.FINE))
        logger.fine(baseURI + ": Cache miss on eid = " + cid + " attname = " + attributeName);

      try {
        cachedExp = XPathParser.parse(attributeValue, context, getNoPrefixNS(context));
      } catch (XSLToolsXPathParseException ex) {
        System.err.println("Parse error was in module " + getSystemId());
        throw ex;
      }
      // re-cache it
      cacheXPathExpression(cid, attributeName, cachedExp);
    } else {
      cachedExp = v.expression;
      if (logger.isLoggable(Level.FINE))
        logger.fine(baseURI + ": Cache hit on cid = " + cid + " attname = " + attributeName);
    }
    return cachedExp;
  }

  public int getlLevelNumber() {
    return levelnumber;
  }

  public int getModuleNumber() {
    return modulenumber;
  }

  @Override
public String toString() {
    return getClass().getSimpleName() + " : " + baseURI;
  }

  public String getHierarchialName() {
    return "lvl" + levelnumber + "." + "mod" + modulenumber;
  }

  /**
   * Rebuilds the map that locates elements by their identifiers. This map is
   * not used by the simplifiers at all, so it needs not be maintained during
   * simplification. It should be rebuilt before template rule extraction, so
   * that the flow death annotators etc. can find its elements.
   * 
   * @param version
   * @return
   */
  Map<String, Element> refreshIdToElementMap(int version) {
    final Map<String, Element> idsToElements = new HashMap<String, Element>();
    Visitor nameAndParsePosSucker = new VisitorSupport() {
      @Override
	public void visit(Element node) {
        Attribute id = node.attribute(XMLConstants.ELEMENT_ID_QNAME);
        if (id != null)
          idsToElements.put(id.getValue(), node);
        id = node.attribute(XMLConstants.ELEMENT_CORE_ID_QNAME);
        if (id != null)
          idsToElements.put(id.getValue(), node);
      }
    };
    getDocument(version).accept(nameAndParsePosSucker);
    return idsToElements;
  }

  /**
   * Force new core IDs upon all element in the tree. Useful after a clone
   * operation... This has nothing to do with the element Id to elements map
   * (except that the re-Id'd elements will not be possible to find in the map
   * unless added with rebuildIdToElementMaps() or maps rebuild with
   * rebuildIdToElementMaps() )
   * 
   * @param root
   */
  public void reassignCoreIDs(Node root) {
    Visitor v = new VisitorSupport() {
      @Override
	public void visit(Element node) {
        addCoreElementId(node);
      }
    };
    root.accept(v);
  }

  void reassignOriginalIDs() {
    Visitor v = new VisitorSupport() {
      @Override
	public void visit(Element node) {
        addOriginalElementId(node);
      }
    };
    documents[ORIGINAL].accept(v);
  }

  /**
   * Rebuilds the map that locates elements by their identifiers. This map is
   * not used by the simplifiers at all, so it needs not be maintained during
   * simplification. It should be rebuilt before template rule extraction, so
   * that the flow death annotators etc. can find its elements.
   * 
   * @param version
   * @return
   */
  public void rebuildIdToElementMaps() {
    elementsById[CORE] = refreshIdToElementMap(CORE);
    elementsById[ORIGINAL] = refreshIdToElementMap(ORIGINAL);
  }

  // does not clone parse positions... could just copy reference; they are
  // supposed not to be written to anyway.
  @Override
public Object clone() {
    StylesheetModule clone = new StylesheetModule(baseURI, (Document) getDocument(CORE).clone(),
        (Document) getDocument(ORIGINAL).clone(), levelnumber, modulenumber);
    clone.idCounters[CORE] = idCounters[CORE];
    clone.idCounters[ORIGINAL] = idCounters[ORIGINAL];
    clone.rebuildIdToElementMaps();
    ModelConfiguration.XPathCacheConfig cacheConfig = ModelConfiguration.current.getXPathCacheConfig();
    if (cacheConfig != ModelConfiguration.XPathCacheConfig.NO_XPATH_CACHE)
      clone.xPathExpressionCache.clear();
    clone.xPathExpressionCache.putAll(xPathExpressionCache);
    return clone;
  }

  public int getSublevelUpperBound() {
    return sublevelUpperBound;
  }

  public void setSublevelNumberUpperBound(int i) {
    this.sublevelUpperBound = i;
  }

  public int compareTo(StylesheetModule other) {
    int result = other.levelnumber - levelnumber;
    return result;
  }

  /*
   * TODO: Dirty hacks. Should not be public, at least.
   * Containers for parse locations of templates and invokers.
   */

  private Map<TemplateRule, ParseLocation> locationsOfRules = new HashMap<TemplateRule, ParseLocation>();

  private Map<ApplyTemplatesInst, ParseLocation> locationsOfInvokers = new HashMap<ApplyTemplatesInst, ParseLocation>();

  public void addRuleParseLocation(TemplateRule rule) {
    ParseLocation pl = rule.getOriginalLocation();
    if (pl != null)
      locationsOfRules.put(rule, pl);
  }

  public void addInvokerParseLocation(ApplyTemplatesInst invoker) {
    ParseLocation pl = invoker.getOriginalLocation();
    if (pl != null)
      locationsOfInvokers.put(invoker, pl);
  }

  /*
   * For positional search (UI pop up menu or equiv.)
   */
  public Set<TemplateRule> searchForTemplates(int line, int col) {
    Set<TemplateRule> result = new HashSet<TemplateRule>();
    for (Map.Entry<TemplateRule, ParseLocation> e : locationsOfRules.entrySet()) {
      if (ParseLocationUtil.insideStartTag(line, col, e.getValue()))
        result.add(e.getKey());
    }
    return result;
  }

  /*
   * For positional search (UI pop up menu or equiv.)
   */
  public Set<ApplyTemplatesInst> searchForInvokers(int line, int col) {
    Set<ApplyTemplatesInst> result = new HashSet<ApplyTemplatesInst>();
    for (Map.Entry<ApplyTemplatesInst, ParseLocation> e : locationsOfInvokers.entrySet()) {
      if (ParseLocationUtil.inside(line, col, e.getValue()))
        result.add(e.getKey());
    }
    return result;
  }
}