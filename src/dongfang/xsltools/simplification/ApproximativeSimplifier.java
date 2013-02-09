/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-20
 */
package dongfang.xsltools.simplification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;

import dongfang.XMLConstants;
import dongfang.XPathConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLocatableException;
import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.model.DummyNamespaceExpander;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.UniqueNameGenerator;
import dongfang.xsltools.xpath2.KeyKiller;
import dongfang.xsltools.xpath2.XPathBase;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathParser;
import dongfang.xsltools.xpath2.XPathStringLiteral;

/**
 * The approximative simplifier performs simplifications that
 * affect semantics of XSLT transforms, but retain any validity
 * errors.
 * It includes stripping away instructions that we do not go
 * into detail with (such as xsl:processing-instruction,
 * xsl:message and more), replacing xsl:number by xsl:value-of
 * with an unknown-string selection, and simplification of
 * complex selections in value-of to an unknown string function.
 * (the latter step is really not necessary for anything else
 * than nice descriptions in prose of the simplification process,
 * and it could be removed).
 * @author dongfang
 */

public class ApproximativeSimplifier extends StructureCopyingSimplifierBase {

  /*
   * Ooold code for the same purpose (and more). 
   * 
   * else if (element.getName().equals("value-of")) { if
   * (!xpathExpressions.containsKey(element)) { // Fetch select expression:
   * Attribute selectAtt = element.getAttribute("select");
   * 
   * if (selectAtt == null) { // Error: xsl:value-of must have a select
   * attribute. reportError("xsl:value-of is missing a select attribute"); //
   * Set some replacement select: selectAtt = new Attribute("select",
   * "self::node()"); element.setAttribute(selectAtt); } // Parse select
   * expression: selectAtt = element.getAttribute("select"); XPathExp selectExp =
   * null;
   * 
   * currentAttribute = selectAtt;
   * 
   * //validator.log("\nvalue-of/@select unparsed= \"" + // selectAtt.getValue() +
   * "\""); selectExp = XPathExp.parse(selectAtt.getValue(),
   * XPathExp.TYPE_STRING, this); //validator.log("value-of/@select parsed= \"" +
   * selectExp.toString() // + "\""); selectExp =
   * XPathExp.applyVisitor(selectExp, new SimplifyXPath());
   * //validator.log("value-of/@select simplified=\"" + // selectExp.toString() +
   * "\"");
   * 
   * currentAttribute = null; // Set normalized select expression: String
   * oldValue = selectAtt.getValue(); selectAtt.setValue(selectExp.toString());
   * 
   * xpathExpressions.put(element, selectExp);
   * 
   * validator.log("xsl:value-of: select=\"" + oldValue + "\" -> select=\"" +
   * selectAtt.getValue() + "\""); } //if (xpathExpressions.get(element) ==
   * null)
   * 
   * 
   * The value-of select to unknownString stunt // Fetch select expression:
   * XPathExp selectExp = (XPathExp)xpathExpressions.get(element); // Unless
   * select is "simple", replace the element with <value-of //
   * select="xslv:unknownString()"/>. boolean simpleSelect = false; // TODO:
   * parse UNKNOWN_STRING only once at construction like the others. if
   * (selectExp.similarTo(XPathExp.parse(UNKNOWN_STRING, XPathExp.TYPE_STRING,
   * this))) simpleSelect = true;
   * 
   * if (selectExp instanceof StringConstant) simpleSelect = true;
   * 
   * if (selectExp.similarTo(simpleExp1)) simpleSelect = true;
   * 
   * if (selectExp.similarTo(simpleExp2)) simpleSelect = true; // Is it simple
   * then? if (!simpleSelect) { // Replace. ListIterator parentIter =
   * (ListIterator)iterStack.peek(); // Remove: parentIter.previous(); // Must
   * do previous and next if anything has // been added, else we cant remove.
   * parentIter.next(); parentIter.remove(); xpathExpressions.remove(element); //
   * Insert value-of: Element valueof = new Element("value-of", xslNS);
   * valueof.setAttribute(new Attribute("select", UNKNOWN_STRING));
   * 
   * parentIter.add(valueof); // Rewind: parentIter.previous();
   * 
   * Remove disable-output-excaping attribute
   * 
   * validator.log("xsl:value-of: Aproximated select: \"" + selectExp + "\" ->
   * \"" + valueof.getAttribute("select").getValue() + "\""); } else { // Keep. //
   * disable-output-escaping: Attribute escapingAtt =
   * element.getAttribute("disable-output-escaping");
   * 
   * if (escapingAtt != null) { if (escapingAtt.getValue().equals("no")) { //
   * Remove attribute: element.removeAttribute(escapingAtt);
   * 
   * See if the disable output escaping can be removed anyway
   * 
   * validator.log("xsl:value-of: Redundant disable-output-escaping=\"no\"
   * attribute removed."); } else { // Check if the disabled output escaping has
   * any effect: // [14] CharData ::= [^<&]* - ([^<&]* ']]>' [^<&]*) / *
   * disable-output-escaping HAS an effect if: 1. It contains ' <' or '&'. 2. It
   * contains "]]>". (Note: ' <' captures all tags and CDATA sections) / if
   * (selectExp instanceof StringConstant) { String value =
   * ((StringConstant)selectExp).getValue();
   * 
   * if (value.indexOf("<") < 0 && value.indexOf("&") < 0 &&
   * value.indexOf("]]>") < 0) { // Disable escaping has no effect. Remove the
   * attribute: element.removeAttribute(escapingAtt);
   * 
   * validator.log("xsl:value-of: disable-output-escaping has no effect.
   * Attribute removed."); } } } } // Replace string constant value-of with text
   * if no // disable-output-escaping: escapingAtt =
   * element.getAttribute("disable-output-escaping");
   * 
   * Replace value-of by pure text (?????)
   * 
   * if (escapingAtt == null && selectExp instanceof StringConstant) { // Fetch
   * string constant: String str = ((StringConstant)selectExp).getValue(); //
   * Remove value-of: ListIterator parentIter = (ListIterator)iterStack.peek();
   * parentIter.previous(); // Must do previous and next if anything has // been
   * added, else we cant remove. parentIter.next(); parentIter.remove();
   * xpathExpressions.remove(element); // Insert text: parentIter.add(new
   * Text(str));
   * 
   * Replace tests by unknown booleans
   * 
   * validator.log("xsl:value-of: Replaced with plain text '"+str+"'."); } } }
   * else if (element.getName().equals("when") ||
   * element.getName().equals("otherwise")) { if
   * (element.getName().equals("when")) { Attribute testAtt =
   * element.getAttribute("test"); // Check if already simplified: if
   * (!testAtt.getValue().equals(UNKNOWN_BOOLEAN)) { // Set test to
   * "xslv:unknownBoolean()": testAtt.setValue(UNKNOWN_BOOLEAN);
   * 
   * validator.log("xsl:when: test=\"" + testAtt.getValue() + "\""); } } //
   * Check if already simplified: if (element.getAttribute("extracted",
   * validatorNS) == null) { // Gather defined parameters: LinkedList
   * withParamList = new LinkedList(); LinkedList paramList = new LinkedList();
   * 
   * generateParamForwardingLists(withParamList, paramList); // Move content to
   * separate template rule: Attribute selectAtt = new Attribute("select",
   * "self::node()"); String mode = SIMPLIFYER_MODE_PREFIX + "_" + newModeIndex +
   * "_" + element.getName(); Attribute modeAtt = new Attribute("mode", mode); //
   * Generate apply: Element apply = new Element("apply-templates", xslNS);
   * apply.setAttribute(selectAtt); apply.setAttribute(modeAtt);
   * apply.getContent().addAll(withParamList); // Mark mode as processed:
   * apply.setAttribute(new Attribute("modeProcessed", "", validatorNS)); //
   * Find match pattern: String matchStr;
   * 
   * if (currentForEach == null && currentTemplate.getAttribute("match") !=
   * null) matchStr = currentTemplate.getAttribute("match").getValue(); else
   * matchStr = "child::node()|/|attribute::*"; // Generate template: Element
   * template = new Element("template", xslNS); template.setAttribute(new
   * Attribute("match", matchStr));
   * template.setAttribute((Attribute)modeAtt.clone());
   * template.setAttribute(new Attribute("label", getContainingTemplateLabel(),
   * validatorNS)); template.getContent().addAll(paramList); // Mark mode as
   * processed: template.setAttribute(new Attribute("modeProcessed", "",
   * validatorNS)); // Move the content to the new template: Iterator
   * contentIter = element.getContent().iterator(); List templateContent =
   * template.getContent();
   * 
   * while (contentIter.hasNext()) { Object o = contentIter.next();
   * contentIter.remove(); templateContent.add(o); } // Insert template at
   * top-level: ListIterator toplevelIter = (ListIterator)iterStack.get(1);
   * toplevelIter.add(new Text("\n")); toplevelIter.add(template);
   * toplevelIter.previous(); toplevelIter.previous(); // Insert the apply:
   * element.addContent(apply); // Mark the template as made by the
   * simplification process: autoTemplates.add(template); // Increment mode
   * index: newModeIndex++; // Mark sub-template as extracted:
   * element.setAttribute(new Attribute("extracted", "", validatorNS));
   * 
   * validator.log("xsl:"+element.getName()+": Content reduced to
   * xsl:apply-templates and xsl:template. Mode is '" + modeAtt.getValue() +
   * "'."); } } else if (element.getName().equals("attribute")) { // Replace
   * complex templates with "xslv:unknownString()": //LinkedList content =
   * element.getContent(); } else if (element.getName().equals("message")) { //
   * Remove the element: ListIterator parentIter =
   * (ListIterator)iterStack.peek(); parentIter.remove(); } else if
   * (element.getName().equals("stylesheet") ||
   * element.getName().equals("transform") || element.getName().equals("import") ||
   * element.getName().equals("sort") || element.getName().equals("choose") ||
   * element.getName().equals("output") || element.getName().equals("key") ||
   * element.getName().equals("namespace-alias") ||
   * element.getName().equals("param") ||
   * element.getName().equals("with-param")) { validator.log("xsl:" +
   * element.getName() + ": Skipped."); } else { // Unhandled XSL element. This
   * is an error. reportError("Invalid XSL element: '" + element.getName() +
   * "'"); // Remove element: ListIterator parentIter =
   * (ListIterator)iterStack.peek(); parentIter.previous(); // Must do previous
   * and next if anything has been // added, else we cant remove.
   * parentIter.next(); parentIter.remove(); } } //if
   * (element.getNamespace().equals(xslNS)) else { // --** Literal
   * simplification **-- //validator.log(" Literal: " + element.getName()); //
   * Attribute set uses. // IMPORTANT: To ensure correct attribute order,
   * attribute sets must be // handled before the attribute value templates. if
   * (element.getAttribute("use-attribute-sets", xslNS) != null) { // Resolve
   * and insert used attribute sets. Attribute attribute =
   * element.getAttribute("use-attribute-sets", xslNS); String usesStr =
   * attribute.getValue(); LinkedList newContent =
   * cloneContent(AttributeSet.resolveUses(this, usesStr)); // Convert ALL
   * literal attributes (not XSL attributes) to xsl:attribute // instructions.
   * List attributes = element.getAttributes(); Iterator iter =
   * attributes.iterator();
   * 
   * while (iter.hasNext()) { Attribute att = (Attribute)iter.next();
   * 
   * if (!att.getNamespace().equals(xslNS)) { // Construct attribute
   * instruction: Element attElement = new Element("attribute", xslNS);
   * 
   * attElement.setAttribute("name", att.getName());
   * attElement.setContent(attValueTemplate2Content(att.getValue())); // Add to
   * content list: newContent.add(new Text("\n")); newContent.add(attElement); //
   * Remove literal attribute: iter.remove();
   * 
   * validator.log("Literal @xsl:use-attribute-sets=\"" + usesStr + "\":
   * De-literalized " + att.getName() + "=\"" + att.getValue() + "\"."); } } //
   * Transfer old element content and set new: List elementContent =
   * detachContent(element); newContent.addAll(elementContent);
   * element.setContent(newContent); // Remove use-attribute-sets attribute:
   * element.removeAttribute(attribute);
   * 
   * validator.log("Literal @xsl:use-attribute-sets=\"" + usesStr + "\":
   * Inserted attribute set '" + usesStr + "'."); } / * Quote W3C-XSLT:
   * Attribute sets can also be used by specifying an xsl:use-attribute-sets
   * attribute on a literal result element. The value of the
   * xsl:use-attribute-sets attribute is a whitespace-separated list of names of
   * attribute sets. The xsl:use-attribute-sets attribute has the same effect as
   * the use-attribute-sets attribute on xsl:element with the additional rule
   * that attributes specified on the literal result element itself are treated
   * as if they were specified by xsl:attribute elements before any actual
   * xsl:attribute elements but after any xsl:attribute elements implied by the
   * xsl:use-attribute-sets attribute. / // Check for attribute value templates: //
   * Note - It can now be assumed that if any literal attributes // remain, then
   * there was no xsl:use-attribute-sets attribute on // the literal. List
   * newContent = new LinkedList();
   * 
   * List attributes = element.getAttributes(); ListIterator iter =
   * attributes.listIterator();
   * 
   * while (iter.hasNext()) { Attribute att = (Attribute)iter.next();
   * 
   * if (att.getValue().indexOf('{') != -1) { validator.log("Found attribute
   * value template."); // Convert the rest of the attributes to <xsl:attribute>
   * instructions: iter.previous();
   * 
   * while (iter.hasNext()) { Attribute attribute = (Attribute)iter.next();
   * 
   * validator.log("Converting attribute " + attribute); // Construct attribute
   * instruction: Element attElement = new Element("attribute", xslNS);
   * 
   * attElement.setAttribute("name", attribute.getName());
   * attElement.setContent(attValueTemplate2Content(attribute.getValue())); //
   * Add to content list: newContent.add(new Text("\n"));
   * newContent.add(attElement); // Remove literal attribute: iter.previous(); //
   * Must do previous and next if anything has been // added, else we cant
   * remove. iter.next(); iter.remove();
   * 
   * validator.log("Literal: De-literalized " + attribute.getName() + "=\"" +
   * attribute.getValue() + "\"."); } } //else // validator.log("NOT an
   * attribute value template: " + att); } // Transfer old element content and
   * set new: List elementContent = detachContent(element);
   * newContent.addAll(elementContent); element.setContent(newContent); } //if
   * (element.getNamespace().equals(xslNS)) ELSE
   * 
   */

  private static final XPathBase STRING_SELF_NODE;

  private static final XPathBase STRING_NAMED_ATTRIBUTE;

  private static final XPathBase STRING_UNKNOWN_STRING;

  private static final XPathBase SELF_NODE;

  private static final XPathBase NAMED_ATTRIBUTE;

  private static final XPathBase UNKNOWN_STRING;

  private final ElementNamespaceExpander killex = new ElementNamespaceExpander();

  private final KeyKiller keyKiller = new KeyKiller(killex);

  static {
    try {
      
      DummyNamespaceExpander dummyNamespaceExpander = new DummyNamespaceExpander();
      
      STRING_SELF_NODE = (XPathBase) XPathParser.parse("string(self::node())",
          dummyNamespaceExpander);
      
      STRING_NAMED_ATTRIBUTE = (XPathBase) XPathParser.parse(
          "string(attribute::a)", dummyNamespaceExpander);
      
      STRING_UNKNOWN_STRING = (XPathBase) XPathParser.parse("string("
          + XPathConstants.FUNC_UNKNOWN_STRING + "())", dummyNamespaceExpander);
      
      SELF_NODE = (XPathBase) XPathParser.parse("self::node()",
          dummyNamespaceExpander);
      
      NAMED_ATTRIBUTE = (XPathBase) XPathParser.parse("attribute::a",
          dummyNamespaceExpander);
      
      UNKNOWN_STRING = (XPathBase) XPathParser.parse(
          XPathConstants.FUNC_UNKNOWN_STRING + "()", dummyNamespaceExpander);
      
    } catch (XSLToolsException ex) {
      throw new AssertionError(
          "Error in static initializer -- should never happen: " + ex);
    }
  }

  public static ApproximativeSimplifier getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new ApproximativeSimplifier(cesspool);
  }

  private ApproximativeSimplifier(ErrorReporter cesspool) {
    this.cesspool = cesspool;
  }

  private ErrorReporter cesspool;

  /*
   * private XPathNormalizer normal_normalizer = new XPathNormalizer(
   * XPathExp.TYPE_ANY, cesspool);
   */

  @Override
protected Node simplify(Node node, StylesheetModule module,
      Resolver resolver, int depth, boolean lastNode) throws XSLToolsException {
    switch (node.getNodeType()) {
    case Node.ELEMENT_NODE:
      Element originalElement = (Element) node;

      /*
       * Zap out MESSAGE, OUTPUT, FALLBACK, etc.
       * Also kill: NAMESPACE_ALIAS (could really keep this, it would be cool to support)
       * Also kill: Key (processed already)
       * Alst kill: comment, processing-instruction -- their output is never really
       * significant for the analysis (when disregarding the ridiculous fact that
       * DTDs may prohibit them in EMPTY elements -- we don't care).
       */
      if (originalElement.getNamespaceURI().equals(XSLConstants.NAMESPACE_URI)) {
        if (originalElement.getName().equals(XSLConstants.ELEM_MESSAGE)
            || originalElement.getName().equals(XSLConstants.ELEM_OUTPUT)
            || originalElement.getName().equals(XSLConstants.ELEM_FALLBACK)
            || originalElement.getName().equals(
                XSLConstants.ELEM_NAMESPACE_ALIAS)
            || originalElement.getName().equals(XSLConstants.ELEM_STRIP_SPACE)
            || originalElement.getName().equals(
                XSLConstants.ELEM_PRESERVE_SPACE)
            || originalElement.getName().equals(
                XSLConstants.ELEM_DECIMAL_FORMAT)
            || originalElement.getName().equals(XSLConstants.ELEM_KEY)
            || originalElement.getName().equals(XSLConstants.ELEM_COMMENT)
            || originalElement.getName().equals(
                XSLConstants.ELEM_PROCESSING_INSTRUCTION))
          return null;

        Element substituteElement = simplifyBelow(originalElement, module,
            resolver, depth);

        /*
         * Turn number into value-of
         */
        if (substituteElement.getName().equals(XSLConstants.ELEM_NUMBER)) {
          substituteElement.setQName(XSLConstants.ELEM_VALUE_OF_QNAME);
          // All count-specific attributes go out
          for (Iterator it = substituteElement.attributeIterator(); it
              .hasNext();) {
            Attribute att = (Attribute) it.next();
            if (att.getNamespaceURI().equals(Namespace.NO_NAMESPACE.getURI())) {
              it.remove();
            }
          }

          // substituteElement.addAttribute(XSLConstants.ATTR_SELECT_QNAME,
          // XPathConstants.FUNC_UNKNOWN_STRING);
          String unknownStringFunc = /*
                                       * Dom4jUtil.makeAttributeValue(
                                       * XPathConstants.FUNC_UNKNOWN_STRING_QNAME,
                                       * substituteElement);
                                       */
          XPathConstants.FUNC_UNKNOWN_STRING;
          substituteElement.addAttribute(XSLConstants.ATTR_SELECT_QNAME,
              unknownStringFunc + "()");
          return substituteElement;
        }

        // keyKiller.setNamespaceResolver(originalElement);

        for (int i = 0; i < substituteElement.attributeCount(); i++) {
          Attribute attribute = substituteElement.attribute(i);
          /*
           * if (attribute.getQName().equals(XSLConstants.ATTR_TEST_QNAME)) {
           * module.unCacheXPathExpression(substituteElement,
           * attribute.getName());
           * 
           * String unknownBooleanFunc =
           * Dom4jUtil.makeAttributeValue(XPathConstants.FUNC_UNKNOWN_STRING_QNAME,
           * substituteElement);
           * substituteElement.addAttribute(XSLConstants.ATTR_SELECT_QNAME,
           * unknownBooleanFunc + "()"); } else
           */
          if (Dom4jUtil.isXPathAttribute(attribute.getQName())) {

            try {
              XPathExpr xPathExpr = // XPathExp.parse(attribute.getValue(),
              // originalElement);
              module.getCachedXPathExp(attribute.getName(), attribute
                  .getValue(), originalElement, substituteElement);

              String oldToString = xPathExpr.toString();

              if (oldToString.toString().contains("key(")) {

                // TODO: Killer paa igen
                XPathExpr newExpr;// 

                killex.setElement(originalElement);
                newExpr = (XPathExpr) xPathExpr.accept(keyKiller);

                if (newExpr.toString().contains("key(")) {
                  System.err
                      .println("Key killer failed to eradicate a key: From "
                          + oldToString + " to " + newExpr);
                  // System.err.print(Dom4jUtil.diagnostics(xPathExpr));
                }

                if (!newExpr.toString().equals(xPathExpr.toString())) {
                  // System.err.println("Plamm! Killed a key: From " + xPathExpr
                  // + " to " + newExpr);
                  module.cacheXPathExpression(substituteElement, attribute
                      .getName(), newExpr);

                  attribute.setValue(newExpr.toString());
                }

                /*
                 * DISABLED for now. What good does it do anyway??
                 */
                /*
                 * int xPathContextDeterminedType = XPathExp.TYPE_ANY;
                 * 
                 * if
                 * (attribute.getQName().equals(XSLConstants.ATTR_MATCH_QNAME)) //
                 * attribute.getQName().equals(XSLConstants.ATTR_SELECT_QNAME))
                 * xPathContextDeterminedType = XPathExp.TYPE_NODESET;
                 * 
                 * else if (attribute.getQName()
                 * .equals(XSLConstants.ATTR_TEST_QNAME))
                 * xPathContextDeterminedType = XPathExp.TYPE_BOOL;
                 * 
                 * else if (attribute.getQName().equals(
                 * XSLConstants.ATTR_SELECT_QNAME) &&
                 * attribute.getParent().getQName().equals(
                 * XSLConstants.ELEM_VALUE_OF_QNAME)) xPathContextDeterminedType =
                 * XPathExp.TYPE_STRING;
                 * 
                 * newExp = XPathExp.applyVisitor(newExp, new
                 * XPathNormalizer(xPathContextDeterminedType, cesspool, depth ==
                 * TOPLEVEL_DEPTH)); // String elementIdentifier = //
                 * substituteElement.attributeValue(XMLConstants.ELEMENT_ID_QNAME); //
                 * module.cacheXPathExpression(elementIdentifier, //
                 * attribute.getName(), xPathExp); / * if
                 * (!attribute.getValue().equals(xPathExp.toString())) {
                 * System.err.println("Back-mutation!!! : " + xPathExp + " " +
                 * attribute.getValue()); }
                 */
              }
            } catch (XSLToolsException ex) {
              cesspool.reportError(module, substituteElement,
                  ParseLocation.Extent.TAG, ex);
            }
          }
        }

        // Now everything that should be converted to value-of is a value-of,
        // and XPath is
        // normalized (context determined type coercion made explicit)

        // For now, make very rough string approximations...
        // String elementIdentifier = substituteElement
        // .attributeValue(XMLConstants.ELEMENT_ID_QNAME);

        if (substituteElement.getName().equals(XSLConstants.ELEM_VALUE_OF)) {

          String select = substituteElement
              .attributeValue(XSLConstants.ATTR_SELECT_QNAME);

          try {

            XPathExpr xPathExp = module.getCachedXPathExp(
                XSLConstants.ATTR_SELECT, select, originalElement,
                substituteElement);

            /*
             * These are the types that we support in static analysis.
             * All others are crushed (replaced by unknownString())
             */
            if (xPathExp.getType() == XPathExpr.TYPE_STRING) {
              if (!(xPathExp instanceof XPathStringLiteral)
                  && !(xPathExp.similarTo(SELF_NODE))
                  && !(xPathExp.similarTo(NAMED_ATTRIBUTE))
                  && !(xPathExp.similarTo(UNKNOWN_STRING))
                  && !(xPathExp.similarTo(STRING_SELF_NODE))
                  && !(xPathExp.similarTo(STRING_NAMED_ATTRIBUTE))
                  && !(xPathExp.similarTo(STRING_UNKNOWN_STRING))) {
                
                QName qselect = QName.get(XPathConstants.FUNC_UNKNOWN_STRING,
                    XMLConstants.VALIDATOR_NAMESPACE);
                
                String attval = Dom4jUtil.makeAttributeValue(qselect,
                    substituteElement);
                // fusk!
                substituteElement.addAttribute(XSLConstants.ATTR_SELECT_QNAME,
                    attval + "()");

                module.unCacheXPathExpression(substituteElement,
                    XSLConstants.ATTR_SELECT);
              }
            }
          } catch (XSLToolsXPathException ex) {
            cesspool.reportError(module, substituteElement,
                ParseLocation.Extent.TAG, ex);
          }
        } else if (substituteElement.getName().equals(XSLConstants.ELEM_WHEN)) {
          /*
           * when has its test made unknown
           */
          if (substituteElement.attribute(XSLConstants.ATTR_TEST_QNAME)==null) {
            QName qtest = QName.get(XPathConstants.FUNC_UNKNOWN_BOOLEAN,
              XMLConstants.VALIDATOR_NAMESPACE);
            String attval = Dom4jUtil
                .makeAttributeValue(qtest, substituteElement);
            // fusk!
            substituteElement.addAttribute(XSLConstants.ATTR_TEST_QNAME, attval + "()");
          }
        } else
        // #30
        if (substituteElement.getName().equals(XSLConstants.ELEM_ATTRIBUTE)) {
          boolean preserve = true;
          for (int i = 0; i < substituteElement.elements().size() && preserve; i++) {
            Element child = (Element) substituteElement.elements().get(i);
            /*
             * These are the attribute text generation constructs that we support in static analysis...
             */
            if (!child.getQName().equals(XSLConstants.ELEM_VALUE_OF_QNAME)
                && !child.getQName().equals(XSLConstants.ELEM_CHOOSE_QNAME)
                && !child.getQName().equals(XSLConstants.ELEM_APPLY_TEMPLATES_QNAME))
              preserve = false;
          }
          if (!preserve) {
            /*
             * Kill the child exps
             */
            List children = new ArrayList(substituteElement.elements());
            for (Iterator it = children.iterator(); it.hasNext();) {
              Element child = (Element) it.next();
              child.detach();
            }
            Element valueOf = fac
                .createElement(XSLConstants.ELEM_VALUE_OF_QNAME);
            module.addCoreElementId(valueOf);
            String unknownStringFunction = /*
                                             * Dom4jUtil.makeAttributeValue(
                                             * XPathConstants.FUNC_UNKNOWN_STRING_QNAME,
                                             * valueOf);
                                             */
            XPathConstants.FUNC_UNKNOWN_STRING;
            valueOf.addAttribute(XSLConstants.ATTR_SELECT,
                unknownStringFunction + "()");
            substituteElement.add(valueOf);
          }
        }
        // #31 --- OUT!!!!
        /*
         * if (substituteElement.getName().equals(XSLConstants.ELEM_ATTRIBUTE) ||
         * substituteElement.getName().equals(XSLConstants.ELEM_ELEMENT)) {
         * Attribute nameAtt = substituteElement
         * .attribute(XSLConstants.ATTR_NAME_QNAME); if
         * (nameAtt.getValue().indexOf('{') < 0 || // TODO: Wrong. Is that att
         * val templates here or are they gone?
         * XPathConstants.isNameFunction(nameAtt.getValue().trim())) { } else {
         * String unknownStringFunction = XPathConstants.FUNC_UNKNOWN_STRING;
         * substituteElement.addAttribute(XSLConstants.ATTR_NAME,
         * unknownStringFunction + "()"); } }
         */
        return substituteElement;
      }
      throw new AssertionError("Non-xsl element remaining!");
    }
    // return null;
    return (Node) node.clone();
  }

  public void process(StylesheetLevel level) throws XSLToolsException {
    for (StylesheetModule module : level.contents()) {
      try {
        simplify(level, module);
      } catch (XSLToolsLocatableException ex) {
        cesspool.reportError(ex);
      }
    }

    for (StylesheetLevel _import : level.imports()) {
      try {
        process(_import);
      } catch (XSLToolsLocatableException ex) {
        cesspool.reportError(ex);
      }
    }
  }

  public void process(Stylesheet stylesheet) throws XSLToolsException {
    keyKiller.setResolver(stylesheet);
    process(stylesheet.getPrincipalLevel());
  }
}