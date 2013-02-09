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
import java.util.Map.Entry;

import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dk.brics.xmlgraph.XMLGraph;
import dongfang.XMLConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.diagnostics.ParseLocationUtil;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.Util;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xpath2.XPathAbsolutePathExpr;
import dongfang.xsltools.xpath2.XPathAxisStep;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathMultiNameTest;
import dongfang.xsltools.xpath2.XPathNameTest;
import dongfang.xsltools.xpath2.XPathNodeTest;
import dongfang.xsltools.xpath2.XPathParser;
import dongfang.xsltools.xpath2.XPathPathExpr;
import dongfang.xsltools.xpath2.XPathRelativePathExpr;
import dongfang.xsltools.xpath2.XPathUnionExpr;

public class ApplyTemplatesInst extends XSLTInstruction {
	
  /*
   * The XPath expression in the selections, with all steps (up, down, whatever)
   * present. This is an expression, not merely a location path, because it
   * might be a union (and that's why the Selection class exists -- there's one
   * instance of that for each element in the union, and each gets a pure
   * location path).
   */
  final XPathExpr originalSelectPath;

  /*
   * Flow death diagnostics. Should be updated when it is determined that a flow
   * did not exist, after all. That happens when: - The RawTAG analyzer found a
   * flow, but the sensitive analyzers determined that there was none. - A
   * priority override test determined that a flow was stolen.
   */
  private List<DeadContextFlow> deathCauses;

  /*
   * Might go out, dunno.
   */
  final Map<QName, Element> withParameters = new HashMap<QName, Element>();

  /*
   * Representations of the individual location steps in the select expression
   * of this apply-templates instructions. In most cases, there is only one.
   */
  final LinkedList<Selection> selections = new LinkedList<Selection>();

  /*
   * Particularly for the downward only attribute hack.
   */
  LinkedList<Selection> attributeAxisSelections;// = new

  // LinkedList<Selection>();

  LinkedList<Selection> otherAxisSelections;// = new LinkedList<Selection>();

  /*
   * Value of mode attribute of the apply-templates instr.
   */
  private final TemplateInvokerMode mode;

  /*
   * True iff there are sort instructions nested.
   */
  boolean containsSorts;

  /*
   * Whether a hack at SG composition for union selection expressions is
   * allowed: If everything is just child and attribute axis, we can assume the
   * attributes to come before the children in doc order, and thus put the
   * fragment repr. the attributes first.
   */
  private boolean onlyDownwardAxesAttributeHack;

  final TemplateRule containingRule;

  private final Map<ContextMode, Set<TemplateRule>> modeCompatibleRules = new HashMap<ContextMode, Set<TemplateRule>>();

  private LinkedList<OtherwiseInst> filters;

  private final ParseLocation originalLocation;

  /*
   * For an apply-templates instruction, translate the context type of the containing template rule
   * to a dynamic redeclaration. The kind of redeclaration depends on the when/otherwise instructions that
   * are in between the template rule and the apply-templates instruction in the containment hierarchy. 
   * This context stuff was collected at the time the apply-templates inst was created, and is in the list 
   * <code>filters</code>.
   */
  DeclaredNodeType applyFilters(SingleTypeXMLClass clazz, DeclaredNodeType type)
      throws XSLToolsSchemaException {
	  // WhenInst is implemented as a subclass of OtherwiseInst...
    for (OtherwiseInst ow : filters) {
      type = ow.transformChoose(clazz, type);
      if (type == null)
    	  break;
    }
    return type;
  }

  /*
   * inst. from TemplateRule
   */
  protected ApplyTemplatesInst(StylesheetModule module, TemplateRule rule,
      Element coreApplyElement, LinkedList<OtherwiseInst> filters,
      String simplifiedElementId, String originalElementId)
      throws XSLToolsXPathException, XSLToolsXPathUnresolvedNamespaceException {
    // Initialize:
    super(module, simplifiedElementId, originalElementId);
    this.containingRule = rule;
    this.filters = (LinkedList<OtherwiseInst>) filters.clone();
    // Fetch select XPath:
    Attribute selectAtt = coreApplyElement
        .attribute(XSLConstants.ATTR_SELECT_QNAME);

    XPathExpr select;

    if (module != null)
      select = module.getCachedXPathExp(XSLConstants.ATTR_SELECT, selectAtt
          .getValue(), coreApplyElement, coreApplyElement);
    else
      select = XPathParser.parse(selectAtt.getValue());

    originalSelectPath = select;

    // Extract selections from the expression (split unions):
    splitUnions(rule, select);

    // Fetch mode:
    Attribute modeAtt = coreApplyElement
        .attribute(XSLConstants.ATTR_MODE_QNAME);

    if (modeAtt != null) {
      String sModeAtt = modeAtt.getValue().trim();
      if (sModeAtt.contains(" ")) {
        throw new AssertionError("Multiple modes not implemented!");
      }
      if ("#current".equals(sModeAtt))
        mode = CurrentContextMode.instance;
      else if ("#default".equals(sModeAtt))
        mode = DefaultTemplateMode.instance;
      else {
        QName qname = ElementNamespaceExpander.qNameForXSLAttributeValue(
            modeAtt.getValue(), coreApplyElement,
            NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
        mode = new QNameTemplateMode(qname);
      }
    } else
      mode = DefaultTemplateMode.instance;

    // Examine content:
    List content = coreApplyElement.content();

    Iterator contentIter = content.iterator();

    while (contentIter.hasNext()) {
      Object childNode = contentIter.next();

      if (childNode instanceof Element) {
        Element child = (Element) childNode;

        if (child.getNamespace().getURI().equals(XSLConstants.NAMESPACE_URI)) {
          if (child.getName().equals(XSLConstants.ELEM_SORT))
            containsSorts = true;
        }
      }
    }

    String eid = coreApplyElement.attributeValue(XMLConstants.ELEMENT_ID_QNAME);
    if (eid != null) {
      Element original = module.getElementById(eid, StylesheetModule.ORIGINAL);
      originalLocation = ParseLocationUtil.getParseLocation(original);
    } else
      originalLocation = null;
  }

  public ParseLocation getOriginalLocation() {
    return originalLocation;
  }

  /*
   * Cuts a select exp of the union kind ( a | b ).. into separate Selection's
   */
  private void splitUnions(TemplateRule rule, XPathExpr exp)
      throws XSLToolsXPathException {
    // Extract all location paths:
    if (exp instanceof XPathUnionExpr) {
      // NodeSetUnion union = (NodeSetUnion) exp;
      XPathUnionExpr union = (XPathUnionExpr) exp;

      LinkedList<Selection> selections = new LinkedList<Selection>();
      LinkedList<Selection> attributeAxisSelections = new LinkedList<Selection>();
      LinkedList<Selection> otherAxisSelections = new LinkedList<Selection>();

      Set<QName> theNames = new HashSet<QName>();

      short axis = -1;
      boolean allAbsolute = false;
      boolean allAxesIdenticalAndAllTestsNameTests = true;
      boolean downwardAttributeHack = true;

      // Iterate and extract:
      Iterator<XPathExpr> iter = union.iterator();

      while (iter.hasNext()) {
        XPathExpr subExp = iter.next();

        if (subExp instanceof XPathPathExpr) {
          // Store path:
          XPathPathExpr path = (XPathPathExpr) subExp;

          Selection current = new Selection(containingRule, path);

          selections.add(current);

          if (path.getStepCount() != 1) {
            allAxesIdenticalAndAllTestsNameTests = false;
          }

          if (axis == -1)
            allAbsolute = subExp instanceof XPathAbsolutePathExpr;

          if (allAbsolute != subExp instanceof XPathAbsolutePathExpr) {
            allAxesIdenticalAndAllTestsNameTests = false;
          }

          if (path.getStepCount() >= 1) {

            XPathExpr firstStep = path.getFirstStep();

            if (!(firstStep instanceof XPathAxisStep)) {
              allAxesIdenticalAndAllTestsNameTests = false;
              continue;
            }

            XPathAxisStep firstAxisStep = (XPathAxisStep) firstStep;

            if (axis != -1 && firstAxisStep.getAxis() != axis) {
              allAxesIdenticalAndAllTestsNameTests = false;
            }

            axis = firstAxisStep.getAxis();

            if (!XPathAxisStep.isStrictlyDownward(axis)) {
              downwardAttributeHack = false;
            } else {
              if (axis == XPathAxisStep.ATTRIBUTE)
                attributeAxisSelections.add(current);
              else
                otherAxisSelections.add(current);
            }

            if (!(firstAxisStep.getNodeTest() instanceof XPathNameTest)) {
              allAxesIdenticalAndAllTestsNameTests = false;
            } else {
              theNames.add(((XPathNameTest) firstAxisStep.getNodeTest())
                  .getQName());
            }
          } else
            downwardAttributeHack = false;
        } else if (subExp instanceof XPathUnionExpr) {
          throw new AssertionError(
              "Parser returned nested union exprs!?!?! Damn.");
        } else {
          allAxesIdenticalAndAllTestsNameTests = false;

          // Emergency, in case of non path selections!
          // really ugly thing to do -- incorrect basically.
          selections
              .add(new Selection(containingRule, (XPathPathExpr) XPathParser
                  .parse("/descendant-or-self::node()")));
          System.err.println("Warning: Apply-templates select exp " + exp
              + " was not a pathexp, or a union. Approximated it.");
          // Throw new XSLToolsXPathTypeException(
          // "apply-templates select must be of type node-set: " + subExp);
        }
      }

      if (allAxesIdenticalAndAllTestsNameTests) {
        XPathPathExpr unified = allAbsolute ? new XPathAbsolutePathExpr()
            : new XPathRelativePathExpr();
        XPathNodeTest nt = new XPathMultiNameTest(theNames);
        XPathAxisStep onlyStep = new XPathAxisStep(axis, nt);
        unified.addStepAtHead(onlyStep);
        this.selections.add(new Selection(rule, unified));
      } else {
        this.selections.addAll(selections);
        if (downwardAttributeHack && !attributeAxisSelections.isEmpty()
            && !otherAxisSelections.isEmpty()) {
          this.onlyDownwardAxesAttributeHack = true;
          this.attributeAxisSelections = attributeAxisSelections;
          this.otherAxisSelections = otherAxisSelections;
        }
      }
    } else if (exp instanceof XPathPathExpr) {
      // Store path:
      selections.add(new Selection(containingRule, (XPathPathExpr) exp));
    } else {
      selections.add(new Selection(containingRule, (XPathPathExpr) XPathParser
          .parse("/descendant-or-self::node()")));
      System.err
          .println("Warning: Apply-templates select exp was not a pathexp, or a union. Approximated it.");
      // throw new XSLToolsXPathTypeException(
      // "apply-templates select must be of type node-set: " + exp);
    }
  }

  /*
   * A mighty bunch of convenience methods, for later use of the analysis
   * results.
   */

  /**
   * Produces a table of (NodeType, Set) pairs. Given the source context node,
   * the table maps each output context node to the possible targets of that
   * particular node-to-node flow.
   */

  public Map<DeclaredNodeType, Set<TemplateRule>> getTargetTable(
      DeclaredNodeType contextType,
      Map<DeclaredNodeType, Set<TemplateRule>> table) { // For each selection:
    for (Selection sel : selections) {
      sel.getTargetTableForAllModes(contextType, table);
    }
    return table;
  }

  /**
   * Returns all context flows and their targets for this instruction
   * 
   * @return
   */

  public void getAllFlowsForAllModes(
      Map<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>> table) {
    for (Selection sel : selections) {
      sel.getAllContextFlowsForAllModes(table);
    }
  }

  public Map<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>> getAllFlowsForAllModes() {
    Map<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>> result = new HashMap<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>>();
    getAllFlowsForAllModes(result);
    return result;
  }

  public void getAllContextTypes(Set<DeclaredNodeType> dumper) {
    if (containingRule != null)
      dumper.addAll(containingRule.getAllModesContextSet());
  }

  public void getAllFlowsForAllModesOtherWayAround(
      Map<DeclaredNodeType, Map<TemplateRule, Set<DeclaredNodeType>>> table) {
    for (Selection sel : selections) {
      sel.getAllContextFlowsForAllModesOtherWayAround(table);
    }
  }

  public Map<DeclaredNodeType, Map<TemplateRule, Set<DeclaredNodeType>>> getAllFlowsForAllModesOtherWayAround() {
    Map<DeclaredNodeType, Map<TemplateRule, Set<DeclaredNodeType>>> result = new HashMap<DeclaredNodeType, Map<TemplateRule, Set<DeclaredNodeType>>>();
    getAllFlowsForAllModesOtherWayAround(result);
    return result;
  }

  void getAllTargets(Set<TemplateRule> buffer) {
    for (Selection sel : selections) {
      sel.getAllTargets(buffer);
    }
  }

  Set<TemplateRule> getAllTargets() {
    Set<TemplateRule> buffer = new HashSet<TemplateRule>();
    getAllTargets(buffer);
    return buffer;
  }

  TemplateInvokerMode getMode() {
    return mode;
  }

  Map<ContextMode, Set<TemplateRule>> getModeCompatibleRules() {
    return modeCompatibleRules;
  }

  Set<TemplateRule> getModeInsensitiveTargetSet() {
    Collection<Set<TemplateRule>> allModedTargets = modeCompatibleRules
        .values();
    Set<TemplateRule> pladder = new HashSet<TemplateRule>();
    for (Set<TemplateRule> s : allModedTargets) {
      pladder.addAll(s);
    }
    return pladder;
  }

  List<ContextMode> getContextModesFor(TemplateRule target) {
    List<ContextMode> result = new LinkedList<ContextMode>();
    for (Map.Entry<ContextMode, Set<TemplateRule>> e : modeCompatibleRules
        .entrySet()) {
      if (e.getValue().contains(target) && !result.contains(e.getKey())) {
        result.add(e.getKey());
      }
    }
    return result;
  }

  void makeSensitiveReverseFlowTable() {
    for (Selection sel : selections) {
      sel.makeSensitiveReverseFlowTable(this);
    }
  }

  void contextualize(List<ContextMode> l) {
    for (ListIterator<ContextMode> it = l.listIterator(); it.hasNext();) {
      it.set(mode.contextualize(it.next()));
    }
  }

  void reportDeath(DeadContextFlow cause) {
    if (!ControlFlowConfiguration.current.generateFlowDeathReport())
      return;
    if (deathCauses == null) {
      deathCauses = new LinkedList<DeadContextFlow>();
    }
    if (!deathCauses.contains(cause))
      deathCauses.add(cause);
    else {// just to be sure, merge
      int oldi = deathCauses.indexOf(cause);
      DeadContextFlow old = deathCauses.get(oldi);
      old.addLostNodeTypes(cause.lostNodeTypes);
    }
  }

  public List<DeadContextFlow> getDeathCauses() {
    if (deathCauses == null)
      return Collections.emptyList();
    return deathCauses;
  }

  boolean templateInvocationCompatible(TemplateRule target) {
    return true;
  }

  /*
   * When containing rule is first visited with a new mode, fetch the list of
   * new targets that might give rise to.
   */
  protected void locateModeCompatibleEdges(
      List<? extends TemplateRule> templateRules, ContextMode contextMode,
      LinkedList<NewFlow> result, PerformanceLogger pa) {

    // Let #current have a chance to imitate the context, and all the others a
    // chance to override

    Set<TemplateRule> targets = modeCompatibleRules.get(contextMode);
    assert (targets == null) : "Funny, an apply-templates inst was visited with same mode the 2nd time! (in rule "
        + containingRule
        + " with mode "
        + contextMode
        + " map: "
        + modeCompatibleRules;
    if (targets == null)
      targets = new HashSet<TemplateRule>();
    modeCompatibleRules.put(contextMode, targets);

    contextMode = this.mode.contextualize(contextMode);

    for (TemplateRule target : templateRules) {
      pa.incrementCounter("InsensitiveEdgesConsidered",
          "ModeAndApplyFlowGrapher");
      if (target.mode.accepts(contextMode)
          && templateInvocationCompatible(target)) {
        targets.add(target);
        NewFlow nf = new NewFlow(target, contextMode);
        result.add(nf);
        pa.incrementCounter("InsensitiveEdgesSurvived",
            "InsensitiveEdgesConsidered");
      } else {
        pa.incrementCounter("InsensitiveEdgesKilled",
            "InsensitiveEdgesConsidered");
      }
    }
  }

  /**
   * Simply adds every template rule that has the same mode is the mode of this --
   * including the empty mode. In fact, for each tuple (selection, template
   * rule) adds an edge if the mode is the same. This is probably the place at
   * which to add the Dong and Bailey improvement (that, independently of any
   * schema information finds seletion, match pairs that at least are not
   * completely hopeless from the outset). Size of the output is in the order of
   * total#selections * total #template rules.
   */
  protected void locateCandidateEdges(ContextMode mode,
      LinkedList<SourcedNewFlow> result) {
    // For each selection:
    int selIndex = 0;

    for (Selection sel : selections) {
      Set<TemplateRule> rules = modeCompatibleRules.get(mode);
      List<TemplateRule> dirtyHack = new ArrayList<TemplateRule>(rules);
      Collections.sort(dirtyHack);
      ListIterator<TemplateRule> it = dirtyHack.listIterator(dirtyHack.size());
      while (it.hasPrevious()) {
        TemplateRule target = it.previous();
        SourcedNewFlow nf = new SourcedNewFlow(target, mode, this, sel);
        result.addLast(nf);
      }
      selIndex++;
    }
  }

  @Override
  void addSubInstruction(XSLTInstruction subInstruction) {
    if (subInstruction instanceof WithParamInst) {

    } else if (subInstruction instanceof SortInst) {
      containsSorts = true;
    }
  }

  private Node createUglyRepeater(SGFragment global, List<Integer> idxx) {
    if (idxx.size() == 1)
      return global.getNodeAt(idxx.get(0));
    Origin o = new Origin(getClass().getSimpleName(), 0, 0);
    // TODO: Det her duer ikke. Nu goer det ! Vistnok.
    // det er endnu mere forkert at interleave!
    return global.createOneOrMoreNode(global.createChoiceNode(idxx, o)
        .getIndex(), o);
  }

  @Override
  dk.brics.xmlgraph.Node createSGSubgraph(SingleTypeXMLClass clazz,
      ContextMode mode, DeclaredNodeType contextType, SGFragment global,
      boolean allowInterleave) throws XSLToolsException {
    List<Integer> idxx = new LinkedList<Integer>();
    // Node n = null;
    // TODO: det her duer ikke (er i stor grad fixet nu...)
    // Det kunne faktisk være lidt smart at hakke alle selection
    // paths op til max længde 1.. og så indføre mellem-templates
    // for hvert skridt -- det kunne forenkle mange ting groft!!!
    // (og der er vist intet tab af præcision) .. jaja men ortogonale,
    // lange paths smutter...

    if (onlyDownwardAxesAttributeHack) {

      for (Selection sel : attributeAxisSelections) {
        Node n = sel.constructSGSubgraph(clazz, mode, contextType, global,
            (containsSorts ? ContentOrder.RANDOM : ContentOrder.FORWARD),
            false, containingRule.index);
        idxx.add(n.getIndex());
      }

      Node allTheAttributes = createUglyRepeater(global, idxx);
      idxx = new LinkedList<Integer>();

      for (Selection sel : otherAxisSelections) {
        Node n = sel.constructSGSubgraph(clazz, mode, contextType, global,
            (containsSorts ? ContentOrder.RANDOM : ContentOrder.FORWARD),
            false, containingRule.index);
        idxx.add(n.getIndex());
      }

      Node allTheOthers = createUglyRepeater(global, idxx);
      idxx = new ArrayList<Integer>(2);

      idxx.add(allTheAttributes.getIndex());
      idxx.add(allTheOthers.getIndex());

      return global.createSequenceNode(idxx, null);
    }

    for (Selection sel : selections) {
      Node n = sel.constructSGSubgraph(clazz, mode, contextType, global,
          (containsSorts ? ContentOrder.RANDOM : ContentOrder.FORWARD),
          allowInterleave, containingRule.index);
      idxx.add(n.getIndex());
    }
    return createUglyRepeater(global, idxx);
  }

  void inferContextSets(
      ContextMode mode,
      Map<ContextMode, Map<TemplateRule, Set<DeclaredNodeType>>> inferredContextSets) {
    for (Selection sel : selections) {
      sel.inferContextSets(mode, inferredContextSets, this);
    }
  }

  void constructSGFragments(ContextMode mode, DeclaredNodeType contextType,
      SingleTypeXMLClass clazz, XMLGraph sg) throws XSLToolsException {
    for (Selection sel : selections) {
      sel.constructSGFragments(mode, contextType, clazz, sg, this);
    }
  }

  void hookupFragments(ContextMode mode, SGFragment global,
      Map<ContextMode, Set<TemplateRule>> done) throws XSLToolsException {
    for (Selection sel : selections) {
      sel.hookupFragments(mode, global, this, done);
    }
  }

  /*
  public void diagnostics(Branch parent, DocumentFactory fac) {
    Set<String> params = new HashSet<String>();
    if (DiagnosticsConfiguration.current.outputDeadFlowDiagsInControlFlowGraph())
      params.add("outputDeadFlowDiagsInControlFlowGraph");
    diagnostics(parent, fac, params);
  }
  */
  @Override
public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement(Util
        .capitalizedStringToHyphenString(getClass()));
    parent.add(me);
    me.addAttribute("mode", mode == null ? "null" : mode.toString());
    me.addAttribute("containsSorts", Boolean.toString(containsSorts));
    // Element selectionsDiag = fac.createElement("selections");
    // me.add(selectionsDiag);
    for (Selection selection : selections) {
      selection.diagnostics(me, fac, configuration);
    }
    Element withParametersDiag = fac.createElement("with-params");
    me.add(withParametersDiag);
    for (Entry<QName, Element> e : withParameters.entrySet()) {
      Element mapping = fac.createElement(XSLConstants.ELEM_WITH_PARAM);
      withParametersDiag.add(mapping);
      mapping.addAttribute(XSLConstants.ATTR_NAME, Dom4jUtil.clarkName(e
          .getKey()));
      mapping.add((Element) e.getValue().clone());
    }
    if (DiagnosticsConfiguration.outputDeadFlowDiagsInControlFlowGraph(configuration))
      Dom4jUtil.collectionDiagnostics(me, deathCauses, "deadFlows", fac, configuration);
    moreDiagnostics(me, fac);
  }

  public String toLabelString() {
    StringBuilder result = new StringBuilder();
    result.append("apply-templates mode=\"");
    result.append(mode.toString());
    result.append("\" select=\"");
    /*
    boolean needsPipe = false;
    for (Selection sel : selections) {
      if (needsPipe)
        result.append('|');
      else
        needsPipe = true;
      result.append(sel.originalPath.toString());
    }
    */
    result.append(originalSelectPath.toString());
    result.append('\"');
    if (containingRule == null || containingRule.index < 0) {
      result.append(" (* ENTRY*)");
    } else {
      result.append(" in " + containingRule.toLabelString());
    }
    return result.toString();
  }

  @Override
public String toString() {
    return "apply-templates mode=\"" + mode + "\" selections=\"" + selections
        + "\"";
  }
}
