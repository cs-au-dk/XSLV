package dongfang.xsltools.controlflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.xmlgraph.Node;
import dk.brics.xmlgraph.XMLGraph;
import dongfang.XMLConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.diagnostics.ParseLocationUtil;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsUnhandledNodeTestException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.Util;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.schemaside.dropoff.DynamicRedeclaration;
import dongfang.xsltools.xmlclass.xslside.AttributeNT;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.ElementNT;
import dongfang.xsltools.xmlclass.xslside.NodeType;
import dongfang.xsltools.xmlclass.xslside.TextNT;
import dongfang.xsltools.xmlclass.xslside.UndeclaredNodeType;
import dongfang.xsltools.xpath2.XPathPathExpr;

/**
 * Information available from TemplateRule: - context set; a Set of NodeTypes. -
 * match path - instructions, a list of XSLTInstruction They again have the
 * information: - Type; as runtime type o' objects - Of the instructions, the
 * ApplyTemplatesInst ones have a List selections. The Selection has: Map
 * <NodeType, ContextFlow> flows: (NodeType, ContextFlow) pairs. Describes
 * context node flow for each source context node. The ContextFlow's each hold a
 * map of <TemplateRule, Set <NodeType>>, oops .. messed up <:3 )-- as well as a
 * set of possible targets from the insensitive analysis (so sensitive -
 * insensitive is the difference betw possible targets and keySet of the context
 * flow map.) Set <TemplateRule>contextInsensitiveTargets; Contains a rough
 * narrowing of the possible targets. Found through context insensitive
 * CONCAT(match,select) analysis. Map <TemplateRule, Set
 * <NodeType>>contextInsensitiveEdgeFlows;
 * 
 * @author dongfang
 */
public class TemplateRule implements Diagnoseable, Comparable<TemplateRule> {
  /*
   * Some diags o' death constants.
   */
  private static final byte RENDER_ABOVE = 0;

  private static final byte RENDER_WITHIN = 1;

  private static final byte RENDER = RENDER_WITHIN;

  public static final int NO_OVERRIDE = 0;

  public static final int OVERRIDDEN_ON_PRIORITY = 1;

  public static final int OVERRIDDEN_ON_PRECEDENCE = 2;

  /*
   * An artificial template rule, for getting it all started. It selects the 
   * /-matching rule(s) in the stylesheet.
   * Shold be replaced by some mechanishm for selecting initial template
   * (as per XSLT2).
   */
  static Element parsedBootstrapTemplateRule = Dom4jUtil.parse("<template xmlns='" + XSLConstants.NAMESPACE_URI + "' "
      + "xmlns:xslv='" + XMLConstants.VALIDATOR_NAMESPACE_URI + "' " + "xmlns:" + XMLConstants.DONGFANG_PREFIX + "='"
      + XMLConstants.DONGFANG_URI + "' " + "match='.' priority='0' " + XMLConstants.ELEMENT_ID_QNAME.getQualifiedName()
      + "='nowhere' " + XMLConstants.ELEMENT_CORE_ID_QNAME.getQualifiedName() + "='nowhere'>"
      + "<apply-templates select='/'/>" + "</template>");

  /*
   * We could also have extended the element and have gotten created by a custom
   * DocumentFactory. Just an idea...
   */

  private final CompositeXSLTInstruction translation;

  /*
   * A map of the locally constructed SG nodes for the template. There is one
   * for each node type.
   */
  private final Map<ContextMode, Map<DeclaredNodeType, SGFragment>> modedSgFragments = new HashMap<ContextMode, Map<DeclaredNodeType, SGFragment>>();

  /*
   * Names of incoming params. To be killed.
   */
  private Set<QName> inputParameters;

  /*
   * Parsed match pattern
   */
  final dongfang.xsltools.xpath2.XPathPathExpr match;

  /*
   * Match regexp (over char-mapped names). Used for fending off some incoming
   * flow (that doesn't match). Only used with automata (but the field takes up
   * so little space that it is just left here).
   */
  private String matchRegExp;

  /*
   * Automaton for above, lazily constructed.
   * Only used with automata (but the field takes up
   * so little space that it is just left here).
   */
  private Automaton matchAutomaton;

  /*
   * eid and cid for this template rule
   */
  private final String[] templateRuleIdentifiers = new String[2];

  public String getId(int version) {
    return templateRuleIdentifiers[version];
  }

  /*
   * Home module of the rule
   */
  private final StylesheetModule module;

  /*
   * Mode, or empty if absent
   */
  final TemplateMode mode;

  /*
   * Priority
   */
  final double priority;

  /*
   * Context set as simple NTs.
   */
  private final Set<UndeclaredNodeType> schemalessContextSet = new HashSet<UndeclaredNodeType>();

  /*
   * Context set as simple NTs. All keys map to themselves (for alias nodes in
   * the set to be retreiveable)
   */
  // private final Map<DeclaredNodeType, DeclaredNodeType> contextSet = new
  // HashMap<DeclaredNodeType, DeclaredNodeType>();
  // private final Map<DeclaredNodeType, Set<ContextMode>> contextModeMap = new
  // HashMap<DeclaredNodeType, Set<ContextMode>>();
  private final Map<ContextMode, Map<DeclaredNodeType, DeclaredNodeType>> contextMNodes = new HashMap<ContextMode, Map<DeclaredNodeType, DeclaredNodeType>>();

  //private final Map<ContextMode, Set<DeclaredNodeType>> contextMNodes = 
  //  new HashMap<ContextMode, Set<DeclaredNodeType>>();

  private final Set<ContextMode> knownModes = new HashSet<ContextMode>();

  final LinkedList<ApplyTemplatesInst> applies = new LinkedList<ApplyTemplatesInst>();

  /*
   * Human readable identity (not stractly needed, could use object identity)
   */
  final int index;

  boolean hasLocalOutputInstructions = false;

  /*
   * Whether some analyzer has determined that this template carries no flow
   * whatsoever.
   */
  boolean pronouncedDead;

  // Rule index. For creating Dot edges,
  // and unique SG gap names.
  // flow set merging experiment!!!

  private Set<? extends DeclaredNodeType> definitelyAcceptableTypes;

  Map<Set<DeclaredNodeType>, Set<DeclaredNodeType>> flowSetCanonicalizer = new HashMap<Set<DeclaredNodeType>, Set<DeclaredNodeType>>();

  Set<TemplateRule> inflows;

  // maps target mode to
  // a map that maps target types to
  // the source context (mode, type, template)
  Map<ContextMode, Map<DeclaredNodeType, Set<NewContextFlow>>> sensitiveInflows;

  Map<ContextMode, Set<DeclaredNodeType>> haveOutputContexts;

  // for some death analysis
  Map<DeclaredNodeType, Set<ApplyTemplatesInst>> invokersByType;

  private final ParseLocation originalParseLocation;

  TemplateRule(StylesheetModule module, Element element, int index) throws XSLToolsException {

    // this.coreTemplateElement
    Element coreTemplateElement = element;
    this.index = index;
    this.module = module;

    // this.moduleURI = moduleURI;
    // Parse match expression:
    Attribute matchAtt = coreTemplateElement.attribute(XSLConstants.ATTR_MATCH);

    // String elementIdentifier = coreTemplateElement
    // .attributeValue(XMLConstants.ELEMENT_ID_QNAME);
    String attributeName = matchAtt.getName();

    // XPathExp xpathExp = null;
    dongfang.xsltools.xpath2.XPathExpr xpathExp = null;
    // String xpathExpAsString = null;

    if (module != null) {
      xpathExp = module.getCachedXPathExp(attributeName, matchAtt.getValue(), element, element);
    }

    else {
      // xpathExp = XPathExp.parse(matchAtt.getValue(), coreTemplateElement,
      // coreTemplateElement);
      xpathExp = dongfang.xsltools.xpath2.XPathParser.parse(matchAtt.getValue());
    }

    match = (XPathPathExpr) xpathExp;

    // Fetch mode:
    Attribute modeAtt = coreTemplateElement.attribute(XSLConstants.ATTR_MODE_QNAME);
    if (modeAtt != null) {
      String sModeAtt = modeAtt.getValue().trim();
      if (sModeAtt.contains(" ")) {
        mode = new CompositeTemplateMode(sModeAtt, coreTemplateElement);
      } else if ("#all".equals(sModeAtt))
        mode = AllTemplateMode.instance;
      else if ("#default".equals(sModeAtt))
        mode = DefaultTemplateMode.instance;
      else {
        QName qname = ElementNamespaceExpander.qNameForXSLAttributeValue(modeAtt.getValue(), coreTemplateElement,
            NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
        mode = new QNameTemplateMode(qname);
      }
    } else
      mode = DefaultTemplateMode.instance;

    // Fetch priority:
    Attribute priorityAtt = coreTemplateElement.attribute(XSLConstants.ATTR_PRIORITY_QNAME);

    priority = Double.parseDouble(priorityAtt.getValue());

    Attribute identifierAtt = coreTemplateElement.attribute(XMLConstants.ELEMENT_CORE_ID_QNAME);
    if (identifierAtt != null) {
      templateRuleIdentifiers[StylesheetModule.CORE] = identifierAtt.getValue();
    } else
      throw new AssertionError("Template w/o simplified id: " + module.toString() + ", "
          + coreTemplateElement.toString());

    identifierAtt = coreTemplateElement.attribute(XMLConstants.ELEMENT_ID_QNAME);
    if (identifierAtt == null) {
      throw new AssertionError("Template w/o original id: " + module.toString() + ", " + coreTemplateElement.toString());
    }

    templateRuleIdentifiers[StylesheetModule.ORIGINAL] = identifierAtt.getValue();

    String simplifiedId = coreTemplateElement.attributeValue(XMLConstants.ELEMENT_CORE_ID_QNAME);
    String originalId = coreTemplateElement.attributeValue(XMLConstants.ELEMENT_ID_QNAME);

    translation = new CompositeXSLTInstruction(module, simplifiedId, originalId);
    locateXSLT(coreTemplateElement.content(), new LinkedList<ChooseInst>(), new LinkedList<OtherwiseInst>(), translation, module);

    if (ControlFlowConfiguration.current.generateFlowDeathReport())
      invokersByType = new HashMap<DeclaredNodeType, Set<ApplyTemplatesInst>>();

    String eid = element.attributeValue(XMLConstants.ELEMENT_ID_QNAME);
    if (eid != null && module != null) {
      Element original = module.getElementById(eid, StylesheetModule.ORIGINAL);
      originalParseLocation = ParseLocationUtil.getParseLocation(original);
    } else
      originalParseLocation = null;
  }

  public ParseLocation getOriginalLocation() {
    return originalParseLocation;
  }

  public int getIndex() {
    return index;
  }

  static TemplateRule getBootstrapTemplateRule() {
    // if (bootstrapTemplateRule == null) {
    // Static did not work: Object gets altered during analysis.
    // Can't clean it .. too thread-unsafe.
    try {
      // Dom4jUtil.debugPrettyPrint(parsedBootstrapTemplateRule);
      return new TemplateRule(null, (Element) parsedBootstrapTemplateRule.clone(), -1);
    } catch (XSLToolsException ex) {
      throw new AssertionError("Parsing the bootstrap template rule should never fail!");
    }
  }

  /*
   * For one experiment in the report.
   */
  void initDefinitelyAcceptableTypes(SingleTypeXMLClass clazz) throws XSLToolsUnhandledNodeTestException,
      XSLToolsSchemaException {

    String isDefault = "";
    int n = getModuleLevelNumber();
    if (n > 100000)
      isDefault = ".inBuiltinRules";

    definitelyAcceptableTypes = clazz.possibleTargetNodes(match);

    if (definitelyAcceptableTypes.isEmpty()) {
      if (!(mode.toString().contains("dongfang"))) {
        PerformanceLogger pa = DiagnosticsConfiguration.current.getPerformanceLogger();
        pa.incrementCounter("AbsurdPatterns" + isDefault, "Death");
      }
    }
  }

  Set<? extends DeclaredNodeType> getDefinitelyAcceptableTypes() {
    return definitelyAcceptableTypes;
  }

  public int getModuleLevelNumber() {
    return module.getlLevelNumber();
  }

  public int getModuleSublevelUpperBound() {
    return module.getSublevelUpperBound();
  }

  /*
   * For the sanity test
   */
  void inferContextSets(ContextMode mode, Map<ContextMode, Map<TemplateRule, Set<DeclaredNodeType>>> inferredContextSets) {
    for (ApplyTemplatesInst ati : applies) {
      ati.inferContextSets(mode, inferredContextSets);
    }
  }

  /**
   * Generate the intra-template XMLGraph fragments, given
   * @param mode - a mode
   * @param contextType - a context type
   * @param clazz - the input schema
   * @param sg - to dump the output
   * @param force - ??
   * @throws XSLToolsException
   */
  void constructSGFragments(ContextMode mode, DeclaredNodeType contextType, SingleTypeXMLClass clazz, XMLGraph sg,
      boolean force) throws XSLToolsException {

    if (!force && !hasOutputterFor(mode, contextType)) {
      return;
    }

    SGFragment sgFragment = new SGFragment(sg, "for tr " + this.index + "(match: " + match + ") @mode" + mode);

    Node entry = translation.createSGSubgraph(clazz, mode, contextType, sgFragment, false);

    sgFragment.setEntryNode(entry);

    // Try fetch mode --> (context type --> fragment)
    Map<DeclaredNodeType, SGFragment> m = modedSgFragments.get(mode);

    if (m == null) {
      m = new HashMap<DeclaredNodeType, SGFragment>();
      modedSgFragments.put(mode, m);
    }

    // store fragment -- used in next method.
    m.put(contextType, sgFragment);

    for (ApplyTemplatesInst ati : applies) {
      ati.constructSGFragments(mode, contextType, clazz, sg);
    }
  }

  void hookupFragments(ContextMode newMode, SGFragment maddafokka, Map<ContextMode, Set<TemplateRule>> done)
      throws XSLToolsException {

    // assert (mode.accepts(newMode));
    // assert (contextMNodes.keySet().contains(newMode));

    for (ApplyTemplatesInst api : applies) {
      api.hookupFragments(newMode, maddafokka, done);
    }
  }

  SGFragment getSGFragment(ContextMode mode, DeclaredNodeType contextType) {
    Map<DeclaredNodeType, SGFragment> map = modedSgFragments.get(mode);
    if (map == null)
      return null;
    SGFragment f = map.get(contextType);
    
    return f;
  }

  boolean addMode(ContextMode mode) {
    boolean b = knownModes.add(mode);
    return b;
  }

  Set<? extends ContextMode> getContextModeSet() {
    return knownModes;
  }

  Set<? extends UndeclaredNodeType> getSchemalessContextSet() {
    return schemalessContextSet;
  }

  void addSchemalessContextType(Set<? extends UndeclaredNodeType> addition) {
    schemalessContextSet.addAll(addition);

    // clean up: If e* is there, remove all other element types
    if (schemalessContextSet.contains(NodeType.ONE_ANY_NAME_ELEMENT_NT)) {
      Set<UndeclaredNodeType> reduced = new HashSet<UndeclaredNodeType>(schemalessContextSet);
      schemalessContextSet.clear();
      for (UndeclaredNodeType type : reduced) {
        if (!(type instanceof ElementNT))
          schemalessContextSet.add(type);
      }
      schemalessContextSet.add(NodeType.ONE_ANY_NAME_ELEMENT_NT);
    }

    // clean up: If a* is there, remove all other attribute types
    if (schemalessContextSet.contains(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT)) {
      Set<UndeclaredNodeType> reduced = new HashSet<UndeclaredNodeType>(schemalessContextSet);
      schemalessContextSet.clear();
      for (UndeclaredNodeType type : reduced) {
        if (!(type instanceof AttributeNT))
          schemalessContextSet.add(type);
      }
      schemalessContextSet.add(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT);
    }
  }

  /*
   * void addToSchemalessContextSet(Set<? extends UndeclaredNodeType>
   * setAddition) { schemalessContextSet.addAll(setAddition); }
   */

  Map<ContextMode, Map<DeclaredNodeType, DeclaredNodeType>> getModedContextSet() {
    return contextMNodes;
  }

  //Set<DeclaredNodeType> getTypesForMode(ContextMode mode) {
  //  return contextMNodes.get(mode);
  //}

  Collection<DeclaredNodeType> getContextSet(ContextMode mode) {
    Map<DeclaredNodeType, DeclaredNodeType> s = contextMNodes.get(mode);
    return s.values();
  }

  public Set<ApplyTemplatesInst> getInvokersForType(DeclaredNodeType type) {
    return invokersByType.get(type);
  }

  boolean addContextType(ContextMode mode, DeclaredNodeType addition, ApplyTemplatesInst invoker) {

    if (ControlFlowConfiguration.current.generateFlowDeathReport()) {
      Set<ApplyTemplatesInst> invokersForType = invokersByType.get(addition);
      if (invokersForType == null) {
        invokersForType = new HashSet<ApplyTemplatesInst>();
        invokersByType.put(addition, invokersForType);
      }
      invokersForType.add(invoker);
    }

    Map<DeclaredNodeType, DeclaredNodeType> contextSet = contextMNodes.get(mode);

    if (contextSet == null) {
      contextSet = new HashMap<DeclaredNodeType, DeclaredNodeType>();
      contextMNodes.put(mode, contextSet);
    }

    if (addition instanceof TextNT && ControlFlowConfiguration.current.useColoredContextTypes()) {
      assert (addition != TextNT.chameleonInstance) : "Don't add chameleonInstance to a colored context set;"
          + " it doesn't behave well there (it is chamelonic, kicking out any color node)";

      boolean blackAndWhite = !contextSet.containsKey(TextNT.chameleonInstance);

      Object colored = contextSet.put(addition, addition);

      if (blackAndWhite)
        return true;

      if (ControlFlowConfiguration.current.useColoredFlowPropagation())
        return colored == null;
    }

    //return contextSet.put(addition, addition) == null;
    DeclaredNodeType already = contextSet.get(addition);

    if (already == null) {
      contextSet.put(addition, addition);
      return true;
    }

    if (!(already instanceof DynamicRedeclaration))
      return false;

    if (addition instanceof DynamicRedeclaration)
      // Pladder; de skal da bevare farven ellers er det hele omsonst.
      // contextSet.put(addition, ((DropOffDecorator) addition).getDecorated());
      if (addition.equals(already))
        return false;
    return true;
  }

  public Set<DeclaredNodeType> getAllModesContextSet() {
    Set<DeclaredNodeType> result = new HashSet<DeclaredNodeType>();
    for (Map.Entry<ContextMode, Map<DeclaredNodeType, DeclaredNodeType>> e : contextMNodes.entrySet()) {
      if (e.getValue() != null)
        result.addAll(e.getValue().keySet());
    }
    return result;
  }

  /**
   * Recursive descent. Locates all content in the XSLT namespace and constructs
   * appropriate instruction objects.
   */
  private void locateXSLT(List<org.dom4j.Node> content, LinkedList<ChooseInst> chooses, LinkedList<OtherwiseInst> filters, XSLTInstruction parent,
      StylesheetModule todoRemoveMeFromHere) throws XSLToolsException {
    // Iterate through and recurse on content:
    ListIterator iter = content.listIterator();

    while (iter.hasNext()) {
      Object o = iter.next();

      if (o instanceof Element) {
        Element element = (Element) o;

        String simplifiedId = element.attributeValue(XMLConstants.ELEMENT_CORE_ID_QNAME);
        String originalId = element.attributeValue(XMLConstants.ELEMENT_ID_QNAME);

        // Identify XSLT elements:
        if (XSLConstants.NAMESPACE_URI.equals(element.getNamespaceURI())) {
          // Add appropriate instruction to list:
          XSLTInstruction xlation;
          if (element.getName().equals(XSLConstants.ELEM_APPLY_TEMPLATES)) {
            xlation = new ApplyTemplatesInst(module, this, element, filters, simplifiedId, originalId);
            if (todoRemoveMeFromHere != null)
              todoRemoveMeFromHere.addInvokerParseLocation((ApplyTemplatesInst) xlation);
            applies.add((ApplyTemplatesInst) xlation);
          } else if (element.getName().equals(XSLConstants.ELEM_APPLY_IMPORTS)) {
            xlation = new ApplyImportsInst(module, this, element, filters, simplifiedId, originalId);
            if (todoRemoveMeFromHere != null)
              todoRemoveMeFromHere.addInvokerParseLocation((ApplyTemplatesInst) xlation);
            applies.add((ApplyTemplatesInst) xlation);
          } else if (element.getName().equals(XSLConstants.ELEM_NEXT_MATCH)) {
            xlation = new NextMatchInst(module, this, element, filters, simplifiedId, originalId);
            if (todoRemoveMeFromHere != null)
              todoRemoveMeFromHere.addInvokerParseLocation((ApplyTemplatesInst) xlation);
            applies.add((ApplyTemplatesInst) xlation);
          } else if (element.getName().equals(XSLConstants.ELEM_VALUE_OF)) {
            Attribute a = element.attribute(XSLConstants.ATTR_SELECT_QNAME);
            dongfang.xsltools.xpath2.XPathExpr select = module.getCachedXPathExp(XSLConstants.ATTR_SELECT,
                a.getValue(), element, element);
            xlation = new ValueOfInst(module, simplifiedId, originalId, select);
            hasLocalOutputInstructions = true;
          } else if (element.getName().equals(XSLConstants.ELEM_CHOOSE)) {
            xlation = new ChooseInst(module, element, simplifiedId, originalId);
            chooses.add((ChooseInst)xlation);
          } else if (element.getName().equals(XSLConstants.ELEM_ATTRIBUTE)) {
            xlation = new AttributeInst(module, element, simplifiedId, originalId);
            hasLocalOutputInstructions = true;
          } else if (element.getName().equals(XSLConstants.ELEM_ELEMENT)) {
            xlation = new ElementInst(module, element, simplifiedId, originalId);
            hasLocalOutputInstructions = true;
          } else if (element.getName().equals(XSLConstants.ELEM_WHEN)) {
            xlation = new WhenInst(module, element, chooses.getLast(), simplifiedId, originalId);
            filters.add((WhenInst) xlation);
          } else if (element.getName().equals(XSLConstants.ELEM_OTHERWISE)) {
            xlation = new OtherwiseInst(module, chooses.getLast(), simplifiedId, originalId);
            filters.add((OtherwiseInst) xlation);
          } else if (element.getName().equals(XSLConstants.ELEM_SORT)) {
            xlation = new SortInst(module, simplifiedId, originalId);
          } else if (element.getName().equals(XSLConstants.ELEM_PARAM)) {
            // Store the parameter name.
            if (inputParameters == null)
              inputParameters = new HashSet<QName>();
            inputParameters.add(ElementNamespaceExpander.qNameForXSLAttributeValue(XSLConstants.ATTR_NAME, element,
                NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE));
            xlation = null;
          } else if (element.getName().equals(XSLConstants.ELEM_WITH_PARAM)) {
            xlation = new WithParamInst(module, element, simplifiedId, originalId);
          } else if (element.getName().equals(XSLConstants.ELEM_COPY_OF)) {
            xlation = new CopyOfInst(module, simplifiedId, originalId);
            hasLocalOutputInstructions = true;
          } else if (element.getName().equals(XSLConstants.ELEM_COPY)) {
            xlation = new CopyInst(module, simplifiedId, originalId);
            hasLocalOutputInstructions = true;
          } else
            throw new AssertionError("Unhandled XSLT instruction: " + element.getName());
          // Recurse:
          if (xlation != null) {
            parent.addSubInstruction(xlation);
            locateXSLT(element.content(), chooses, filters, xlation, todoRemoveMeFromHere);

            if (element.getName().equals(XSLConstants.ELEM_WHEN)) {
              filters.removeLast();
            } else if (element.getName().equals(XSLConstants.ELEM_OTHERWISE)) {
              filters.removeLast();
            }
            } else if (element.getName().equals(XSLConstants.ELEM_CHOOSE)) {
              chooses.removeLast();
            } else
            throw new AssertionError("Non-XSLT element: " + element.getName());
        }
      }
    }
  }

  String lazyMakeMatchRegExp(SingleTypeXMLClass inputType) throws XSLToolsSchemaException,
      XSLToolsUnhandledNodeTestException {
    if (matchRegExp == null) {
      // assert (xmlclass instanceof RegularTreeXMLClass) : "Unsupported XML
      // class";
      matchRegExp = inputType.constructXPathRegExp(match);
    }
    return matchRegExp;
  }

  Automaton lazyMakeMatchAutomaton(SingleTypeXMLClass inputType) throws XSLToolsSchemaException,
      XSLToolsUnhandledNodeTestException {
    PerformanceLogger pa = DiagnosticsConfiguration.current.getPerformanceLogger();
    pa.incrementCounter("Match", "Automata");
    if (matchAutomaton == null) {
      pa.incrementCounter("MatchMiss", "Match");
      String matchExp = lazyMakeMatchRegExp(inputType);
      matchAutomaton = new RegExp(matchExp).toAutomaton().intersection(inputType.getSchemaATSPathAutomaton());
    } else {
      pa.incrementCounter("MatchHit", "Match");
    }
    return matchAutomaton;
  }

  public void pronounceDead() {
    if (!pronouncedDead) {
      int n = getModuleLevelNumber();
      String isBuiltin = "";
      if (n > 100000) {
        isBuiltin = ".early";
      }
      PerformanceLogger pa = DiagnosticsConfiguration.current.getPerformanceLogger();
      if (!(mode.toString().contains("dongfang"))) {
        pa.incrementCounter("DeadRules" + isBuiltin, "Death");
      }
      pronouncedDead = true;
    }
  }

  @Override
public String toString() {
    return "<rule match=\"" + match + "\" mode=\"" + mode + "\" index=\"" + index + "\" hasLocalOutput=\""
        + Boolean.toString(hasLocalOutputInstructions)
        + "\" schemaLessContextSet=\"{" + decentString(schemalessContextSet, 1000) + "}\" contextSet=\"{"
        + decentString(contextMNodes, 1000) + "}\" "
        + "'/>";
  }

  public String toLabelString() {
    return "rule(" + index + ") match=" + match + " mode=" + mode;
  }

  /**
   * For internal use.
   */
  public static String decentString(Object o) {
    return decentString(o, 200);
  }

  /**
   * For internal use.
   */
  public static String decentString(Object o, int maxLength) {
    String str = o.toString();
    if (str.length() > maxLength)
      return str.substring(0, maxLength) + "...";
    return str;
  }

  /**
   * Return all <outflowing node type, Set <targets of that>> pairs for a
   * particular context node type. Non instantiating version.
   * 
   * @param contextNode
   * @param table
   */
  public void getTargetTable(DeclaredNodeType contextType, Map<DeclaredNodeType, Set<TemplateRule>> table) {
    for (ApplyTemplatesInst ati : applies) {
      ati.getTargetTable(contextType, table);
    }
  }

  /**
   * Return all <outflowing node type, Set <targets of that>> pairs for a
   * particular context node type. Map instantiating version.
   * 
   * @param contextNode
   * @return
   */
  public Map<DeclaredNodeType, Set<TemplateRule>> getTargetTable(DeclaredNodeType contextNode) {
    Map<DeclaredNodeType, Set<TemplateRule>> result = new HashMap<DeclaredNodeType, Set<TemplateRule>>();
    getTargetTable(contextNode, result);
    return result;
  }

  /**
   * Return all <outflowing node type, Set <targets of that>> pairs for all
   * selections of the TemplateRule. Useful for template-level flow graphing.
   * 
   * @return
   */
  public Map<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>> getTargetTable() {
    Map<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>> result = new HashMap<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>>();
    for (Map.Entry<ContextMode, Map<DeclaredNodeType, DeclaredNodeType>> e : contextMNodes.entrySet()) {
      for (DeclaredNodeType context : e.getValue().keySet()) {
        Map<DeclaredNodeType, Set<TemplateRule>> table = getTargetTable(context);
        result.put(context, table);
      }
    }
    return result;
  }

  Set<TemplateRule> getAllTargets() {
    Set<TemplateRule> buffer = new HashSet<TemplateRule>();
    for (ApplyTemplatesInst ati : applies)
      ati.getAllTargets(buffer);
    return buffer;
  }

  // has-reachable-outputter analysis stage 1: let all successors know that we
  // call them
  void updateInflowSetOfTargets() {
    Set<TemplateRule> buffer = new HashSet<TemplateRule>();
    for (ApplyTemplatesInst ati : applies) {
      ati.getAllTargets(buffer);
    }
    for (TemplateRule r : buffer) {
      r.inflows.add(this);
    }
  }

  void addSensitiveInflow(ContextMode targetMode, DeclaredNodeType targetType, NewContextFlow inflow) {
    if (sensitiveInflows == null)
      sensitiveInflows = new HashMap<ContextMode, Map<DeclaredNodeType, Set<NewContextFlow>>>();

    Map<DeclaredNodeType, Set<NewContextFlow>> inflowForMode = sensitiveInflows.get(targetMode);
    if (inflowForMode == null) {
      inflowForMode = new HashMap<DeclaredNodeType, Set<NewContextFlow>>();
      sensitiveInflows.put(targetMode, inflowForMode);
    }
    Set<NewContextFlow> inflowForType = inflowForMode.get(targetType);

    if (inflowForType == null) {
      inflowForType = new HashSet<NewContextFlow>();
      inflowForMode.put(targetType, inflowForType);
    }

    inflowForType.add(inflow);
  }

  boolean hasOutputterFor(ContextMode mode, DeclaredNodeType contextType) {
    if (!ControlFlowConfiguration.current.useNoOutputSGTruncation())
      return true;
    if (haveOutputContexts == null)
      return false;
    Set<DeclaredNodeType> types = haveOutputContexts.get(mode);
    if (types == null)
      return false;
    return types.contains(contextType);
  }

  void addSensitiveOutputtersReachable(ContextMode mode, DeclaredNodeType contextType, LinkedList<NewContextFlow> work) {
    if (!hasOutputterFor(mode, contextType)) {
      if (haveOutputContexts == null) {
        haveOutputContexts = new HashMap<ContextMode, Set<DeclaredNodeType>>();
      }
      Set<DeclaredNodeType> types = haveOutputContexts.get(mode);
      if (types == null) {
        types = new HashSet<DeclaredNodeType>();
        haveOutputContexts.put(mode, types);
      }
      types.add(contextType);

      // Map<ContextMode, Map<DeclaredNodeType, Set<NewContextFlow>>>
      if (sensitiveInflows != null) {
        for (Map.Entry<ContextMode, Map<DeclaredNodeType, Set<NewContextFlow>>> upstream0 : sensitiveInflows.entrySet()) {
          for (Map.Entry<DeclaredNodeType, Set<NewContextFlow>> upstream1 : upstream0.getValue().entrySet()) {
            // for (NewContextFlow flow : upstream1.getValue()) {
            // TemplateRule source = flow.target;
            // if (!flow.target.hasOutputterFor(flow.contextMode,
            // flow.contextType))
            // work.add(flow);
            // }
            work.addAll(upstream1.getValue());
          }
        }
      } else {
        // System.err.println("This template had null inflows but was reached
        // even so: " + this);
      }
    }
  }

  void initSensitiveOutputtersReachable(LinkedList<NewContextFlow> work) {
    // PerformanceAnalyzer pa =
    // DiagnosticsConfiguration.current.getPerformanceAnalyzer();
    for (ApplyTemplatesInst ati : applies) {
      ati.makeSensitiveReverseFlowTable();
    }
    if (hasLocalOutputInstructions) {
      for (Map.Entry<ContextMode, Map<DeclaredNodeType, DeclaredNodeType>> e : contextMNodes.entrySet()) {
        for (DeclaredNodeType type : e.getValue().keySet()) {
          work.add(new NewContextFlow(type, this, e.getKey()));
          // pa.incrementCounter("ReachingOutput", "SGFragments");
        }
      }
    }
  }

  public int compareTo(TemplateRule other) {
    /*
     * Returning something negative means that other is overriding this.
     * Returning zero means same precedence and priority Returning something
     * positive means that this is overriding other.
     */

    // Really just returns: other.levelnumber - module.levelnumber
    // that is, if the other guy has a higher levelnumber, then module wins.
    int diff = module.compareTo(other.module);

    if (diff != 0)
      return diff < 0 ? -OVERRIDDEN_ON_PRECEDENCE : OVERRIDDEN_ON_PRECEDENCE;

    double prioDiff = priority - other.priority;
    return prioDiff < 0 ? -OVERRIDDEN_ON_PRIORITY : prioDiff > 0 ? OVERRIDDEN_ON_PRIORITY : NO_OVERRIDE;
  }

  private static Element createXMLReference(TemplateRule target, DocumentFactory fac, String docname) {
    Element targetDiag = fac.createElement(XMLConstants.CONTEXT_FLOW_TARGET_QNAME);
    targetDiag.addAttribute(XMLConstants.SYSTEM_ID_QNAME, target.module.getSystemId());
    targetDiag.addAttribute(XMLConstants.DOCUMENT_ID_QNAME, docname);
    targetDiag.addAttribute(XMLConstants.ELEMENT_ID_QNAME,
        target.templateRuleIdentifiers[StylesheetModule.ORIGINAL] == null ? "null"
            : target.templateRuleIdentifiers[StylesheetModule.ORIGINAL]);
    targetDiag.addAttribute(XMLConstants.ELEMENT_CORE_ID_QNAME,
        target.templateRuleIdentifiers[StylesheetModule.CORE] == null ? "null"
            : target.templateRuleIdentifiers[StylesheetModule.CORE]);
    return targetDiag;
  }

  public Element createXMLReference(DocumentFactory fac) {
    return createXMLReference(this, fac, module.getHierarchialName());
  }

  private Element createCanonicalChild(Element parent, QName name, DocumentFactory fac) {
    for (Iterator it = parent.elementIterator(); it.hasNext();) {
      Element child = (Element) it.next();
      if (child.getQName().equals(name))
        return child;
    }
    Element result = fac.createElement(name);
    parent.add(result);

    return result;
  }

  /**
   * currently hogs up the xml like a .. well like a hog.
   */
  public void annotateContextSensitiveFlows(Stylesheet stylesheet, int version, DocumentFactory fac) {
    StylesheetModule container = stylesheet.getModule(module);

    String key = templateRuleIdentifiers[version];

    Element coreTemplateElement = container.getElementById(key, version);

    assert (coreTemplateElement != null) : "Blast! An element(" + templateRuleIdentifiers[version] + " from "
        + container + ") found no original";

    Element parent = coreTemplateElement.getParent();
    int index = parent.elements().indexOf(coreTemplateElement);

    Element templateDecorator = RENDER == RENDER_ABOVE ? index == 0 ? null : (Element) parent.elements().get(index - 1)
        : coreTemplateElement.elements().size() == 0 ? null : (Element) coreTemplateElement.elements().get(0);
    if (templateDecorator == null || !templateDecorator.getQName().equals(XMLConstants.FLOWANNOTATION_QNAME)) {
      templateDecorator = fac.createElement(XMLConstants.FLOWANNOTATION_QNAME);
      if (RENDER == RENDER_ABOVE)
        parent.elements().add(index, templateDecorator);
      else
        coreTemplateElement.elements().add(0, templateDecorator);
    }

    /*
     * Context sets
     */
    Element contextSetDiag = createCanonicalChild(templateDecorator, XMLConstants.CONTEXTSET_QNAME, fac);

    for (Map.Entry<ContextMode, Map<DeclaredNodeType, DeclaredNodeType>> e : contextMNodes.entrySet()) {
      for (NodeType context : e.getValue().keySet()) {
        Element contextNTDiag = fac.createElement(XMLConstants.CONTEXT_TYPE_QNAME);
        contextSetDiag.add(contextNTDiag);
        contextNTDiag.addAttribute(XMLConstants.CONTEXT_TYPE_ATTR_QNAME, context.toLabelString());
      }
    }

    for (ApplyTemplatesInst ati : applies) {

      Map<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>> flows = ati.getAllFlowsForAllModes();

      // assert (ati.containingRule == this) : "An apply-templates instruction
      // seemed to belong to a different template rule!";

      key = ati.originalElementIds[version];
      if (key == null) {
        key = ati.originalElementIds[1 - version];
      }

      Element atiSrc = container.getElementById(key, version);

      /*
       * assert (atiSrc != null) : "Blast! An apply-templates (" + ati + " from " +
       * stylesheet.getModule(ati.containingRule.module) + ") inst with the orig
       * id " + ati.originalElementIds[version] + " found no original";
       */
      parent = atiSrc.getParent();
      index = parent.elements().indexOf(atiSrc);

      Element atiDecorator = RENDER == RENDER_ABOVE ? index == 0 ? null : (Element) parent.elements().get(index - 1)
          : atiSrc.elements().size() == 0 ? null : (Element) atiSrc.elements().get(0);

      if (atiDecorator == null || !atiDecorator.getQName().equals(XMLConstants.FLOWANNOTATION_QNAME)) {
        atiDecorator = fac.createElement(XMLConstants.FLOWANNOTATION_QNAME);
        if (RENDER == RENDER_ABOVE)
          parent.elements().add(index, atiDecorator);
        else
          atiSrc.elements().add(0, atiDecorator);
      }

      contextSetDiag = createCanonicalChild(atiDecorator, XMLConstants.CONTEXTSET_QNAME, fac);

      Attribute derived = atiSrc.attribute(XMLConstants.KNOCKEDOUT);
      if (derived == null || version == StylesheetModule.CORE) {
        for (DeclaredNodeType context : flows.keySet()) {
          Map<DeclaredNodeType, Set<TemplateRule>> flowSuccessors = flows.get(context);
          Element contextNTDiag = fac.createElement(XMLConstants.CONTEXT_TYPE_QNAME);
          contextNTDiag.addAttribute("type", context.toLabelString());
          contextSetDiag.add(contextNTDiag);

          // Element outflowsDiag =
          // fac.createElement(XMLConstants.OUTFLOWS_QNAME);
          // contextNTDiag.add(outflowsDiag);

          for (Map.Entry<DeclaredNodeType, Set<TemplateRule>> outflow : flowSuccessors.entrySet()) {
            Element outflowDiag = fac.createElement(XMLConstants.OUTFLOW_QNAME);
            outflowDiag.addAttribute("type", outflow.getKey().toLabelString());
            contextNTDiag.add(outflowDiag);

            Set<TemplateRule> targets = outflow.getValue();

            for (TemplateRule target : targets) {
              outflowDiag
                  .add(createXMLReference
                      (target, fac, stylesheet.getModule(target.module).getHierarchialName()));
            }
          }
        }
        // logger.warning(atiDecorator.getDocument().getName());
        if (!ati.getDeathCauses().isEmpty()) {
          Element deadTargetDecorator = createCanonicalChild(atiDecorator, XMLConstants.DEAD_TARGETS_QNAME, fac);
          for (DeadContextFlow cause : ati.getDeathCauses()) {
            deadTargetDecorator.add(cause.describeDeathCause(fac));
          }
        }
      }
    }
  }

  /**
   * currently hogs up the xml like a .. well like a hog.
   */
  public void annotateContextInsensitiveFlows(Stylesheet stylesheet, int version, DocumentFactory fac) {

    StylesheetModule container = stylesheet.getModule(module);
    String key = templateRuleIdentifiers[version];

    Element coreTemplateElement = container.getElementById(key, version);

    assert (coreTemplateElement != null) : "Blast! An element(" + templateRuleIdentifiers[version] + " from "
        + container + ") found no original";

    Element parent = coreTemplateElement.getParent();
    int index = parent.elements().indexOf(coreTemplateElement);
    for (ApplyTemplatesInst ati : applies) {
      // zwin den til...
      // Map<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>> flows =
      // ati
      // .getAllFlows();
      // assert (ati.containingRule == this) : "An apply-templates instruction
      // seemed to belong to a different template rule!";

      key = ati.originalElementIds[version];
      if (key == null) {
        key = ati.originalElementIds[1 - version];
      }

      Element atiSrc = container.getElementById(key, version);

      /*
       * assert (atiSrc != null) : "Blast! An apply-templates (" + ati + " from " +
       * stylesheet.getModule(ati.containingRule.module) + ") inst with the orig
       * id " + ati.originalElementIds[version] + " found no original";
       */
      parent = atiSrc.getParent();
      index = parent.elements().indexOf(atiSrc);

      Element atiDecorator = RENDER == RENDER_ABOVE ? index == 0 ? null : (Element) parent.elements().get(index - 1)
          : atiSrc.elements().size() == 0 ? null : (Element) atiSrc.elements().get(0);

      if (atiDecorator == null || !atiDecorator.getQName().equals(XMLConstants.INSENSITIVE_CONTROLFLOW_QNAME)) {
        atiDecorator = fac.createElement(XMLConstants.INSENSITIVE_CONTROLFLOW_QNAME);
        if (RENDER == RENDER_ABOVE)
          parent.elements().add(index, atiDecorator);
        else
          atiSrc.elements().add(0, atiDecorator);
      }

      Element currentTargetDecorator = fac.createElement(XMLConstants.CURRENT_TARGETS_QNAME);
      atiDecorator.add(currentTargetDecorator);

      /*
       * skal paa igen men vil ikke compilere!!!!!! List<TemplateRule> targets =
       * ati.getContextInsensitiveTargets(); for (TemplateRule target : targets) {
       * currentTargetDecorator.add(createXMLReference(target, fac, stylesheet
       * .getModule(target.module).getHierarchialName())); }
       */

      Element deadTargetDecorator = fac.createElement(XMLConstants.DEAD_TARGETS_QNAME);
      atiDecorator.add(deadTargetDecorator);

      // if(ati.diagnosticsContextInsensitiveFlow!=null) {
      // for (TemplateRule target:ati.diagnosticsContextInsensitiveFlow) {
      // deadTargetDecorator.add(createXMLReference(target, fac,
      // stylesheet.getModule(target.module).getHierarchialName()));
      // }
      // for (CauseOfDeath death : ati.deathCauses) {
      // }
    }
  }

  Element briefIdDiagnostics(Branch parent, String name, DocumentFactory fac) {
    Element me = fac.createElement(name);
    parent.add(me);
    // me.addAttribute("home-module", module == null ? "bootstrap-rule" : module
    // .getSystemId());
    me.addAttribute("index", Integer.toString(index));
    me.addAttribute("mode", mode == null ? "null" : mode.toString());
    me.addAttribute("match", match == null ? "null" : match.toString());
    me.addAttribute("priority", Double.toString(priority));
    me.addAttribute("hasLocalOutput", Boolean.toString(hasLocalOutputInstructions));
    return me;
  }

  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = briefIdDiagnostics(parent, Util.capitalizedStringToHyphenString(TemplateRule.class), fac);

    if (pronouncedDead) {
      me.add(fac.createElement("pronouncedDead"));
      return;
    }

    if (DiagnosticsConfiguration.outputAutomataInControlFlowGraph(configuration)) {
      me.addAttribute("matchRegExp", matchRegExp == null ? "null" : matchRegExp);
      if (DiagnosticsConfiguration.outputAncestorStringsInControlFlowGraph(configuration))
        me.addAttribute("ancestor-string-example", matchAutomaton == null ? "null" : matchAutomaton
            .getShortestExample(true));
    }

    if (DiagnosticsConfiguration.outputSchemalessStuffInControlFlowGraph(configuration))
      Dom4jUtil.collectionDiagnostics(me, schemalessContextSet, "schemaless-context-set", fac, configuration);

    Set<DeclaredNodeType> contextSet = new HashSet<DeclaredNodeType>();

    for (Map.Entry<ContextMode, Map<DeclaredNodeType, DeclaredNodeType>> e : contextMNodes.entrySet())
      contextSet.addAll(e.getValue().keySet());

    List<DeclaredNodeType> sorted = new ArrayList<DeclaredNodeType>(contextSet);
    Collections.sort(sorted);

    // Dom4jUtil.collectionDiagnostics(me, sorted,
    // "schema-context-set", fac);

    Element mcs = fac.createElement("contextSet");
    me.add(mcs);

    for (Map.Entry<ContextMode, Map<DeclaredNodeType, DeclaredNodeType>> e : contextMNodes.entrySet()) {
      Element mode = fac.createElement("under-mode");
      mcs.add(mode);
      mode.addAttribute("mode", e.getKey().toString());
      for (DeclaredNodeType t : e.getValue().keySet()) {
        t.diagnostics(mode, fac, configuration);
      }
    }

    // Dom4jUtil.collectionDiagnostics(me, coreTemplateElement.content(),
    // "contents", fac);

    Dom4jUtil.collectionDiagnostics(me, translation.subInstructions, "instructions", fac, configuration);

    Element inputParametersDiag = fac.createElement("input-parameters");
    if (inputParameters != null)
      for (QName inputParameter : inputParameters) {
        Element inputParameterDiag = fac.createElement("input-parameter");
        inputParametersDiag.add(inputParameterDiag);
        inputParameterDiag.add(fac.createText(Dom4jUtil.clarkName(inputParameter)));
      }
  }
}
