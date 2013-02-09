package dongfang.xsltools.controlflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.xmlgraph.ChoiceNode;
import dk.brics.xmlgraph.Node;
import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsUnhandledNodeTestException;
import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.util.Util;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.schemaside.dropoff.DynamicRedeclaration;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NodeType;
import dongfang.xsltools.xmlclass.xslside.PINT;
import dongfang.xsltools.xmlclass.xslside.RootNT;
import dongfang.xsltools.xmlclass.xslside.TextNT;
import dongfang.xsltools.xmlclass.xslside.UndeclaredNodeType;
import dongfang.xsltools.xpath2.XPathAbsolutePathExpr;
import dongfang.xsltools.xpath2.XPathAnyNodeTest;
import dongfang.xsltools.xpath2.XPathAxisStep;
import dongfang.xsltools.xpath2.XPathBase;
import dongfang.xsltools.xpath2.XPathCommentTest;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathNameTest;
import dongfang.xsltools.xpath2.XPathNodeTest;
import dongfang.xsltools.xpath2.XPathPITest;
import dongfang.xsltools.xpath2.XPathParser;
import dongfang.xsltools.xpath2.XPathPathExpr;
import dongfang.xsltools.xpath2.XPathRelativePathExpr;
import dongfang.xsltools.xpath2.XPathStepExpr;
import dongfang.xsltools.xpath2.XPathTextTest;
import dongfang.xsltools.xpath2.XPathUnionExpr;

public class Selection implements Diagnoseable {

  private static final XPathPathExpr slash;

  static {
    try {
      slash = (XPathPathExpr) XPathParser.parse("/");
    } catch (Exception ex) {
      throw new AssertionError(
          "Error in static initializer -- should never happen");
    }
  }

  // eksperiment -- skal ud igen !!!
  // public static Automaton totalSlammer = null;

  // private static final boolean usingNoOutputSGTruncation =
  // ControlFlowConfiguration.current.useNoOutputSGTruncation();

  final XPathPathExpr containingRuleMatchPattern;

  final boolean wasApproximated;

  final XPathPathExpr originalPath;

  String _filterExprFreeRepr;

  final XPathPathExpr downwardizedPathPart;

  final XPathPathExpr waywardPathPart;

  private XPathPathExpr contextInsensitiveMatchSelectPath;

  Set<DeclaredNodeType> allTypesEverSelected = new HashSet<DeclaredNodeType>();

  // private Automaton contextInsensitiveMatchSelectAutomaton;
  // private Map<Object, Automaton> alphaAutomatonCache = new
  // WeakHashMap<Object, Automaton>();

  /*
   * An M --> T --> 2^Sigma gadget, all in all S --> M --> T --> 2^Sigma
   */
  private Map<ContextMode, SchemalessFlow> contextInsensitiveEdgeFlows = new HashMap<ContextMode, SchemalessFlow>();

  // private Map<DeclaredNodeType, ContextFlow> contextFlowCache = new
  // HashMap<DeclaredNodeType, ContextFlow>();

  private Map<ContextMode, ModedContextMap> contextMaps = new HashMap<ContextMode, ModedContextMap>();

  private Map<ContextMode, Map<DeclaredNodeType, SGFragment>> myModedFragments = new HashMap<ContextMode, Map<DeclaredNodeType, SGFragment>>();

  private static PerformanceLogger spa = DiagnosticsConfiguration.current
      .getPerformanceLogger();

  // private static final boolean useAbstractFlowAlgoritm =
  // ControlFlowConfiguration.current
  // .candidateFilterAlgorithm() ==
  // ControlFlowConfiguration.ControlFlowAlgorithm.ABSTRACT_EVALUATION;

  private static final boolean usePrefilterCache = ControlFlowConfiguration.current
      .usePrefilterCache();

  private static final boolean cacheAlphaBeta = ControlFlowConfiguration.current
      .useAlphaBetaResultCache();

  private static final boolean cacheAlpha = ControlFlowConfiguration.current
      .useAlphaAutomatonCache();

  public Selection(TemplateRule rule, XPathPathExpr originalPath)
      throws XSLToolsXPathException {
    this(rule.index == -1 ? slash : rule.match, originalPath);
  }

  Selection(XPathPathExpr match, XPathPathExpr originalPath)
      throws XSLToolsXPathException {

    // originalPath = zapLeadingSlashSlash(originalPath);
    this.containingRuleMatchPattern = match;
    this.originalPath = originalPath;
    this.downwardizedPathPart = new XPathRelativePathExpr();
    this.waywardPathPart = new XPathRelativePathExpr();

    /*
     * Make the famous location path containing all steps of the original,
     * except the rightmost nondownward, and everything on its left. The steps
     * not written to the downward part, are written to another part, called the
     * wayward part (the XPath abstract eval function, Delta, will feed on
     * this).
     */

    boolean wasApproximated = false;

    for (Iterator it = originalPath.reverseSteps(); it.hasNext();) {
      Object o = it.next();
      if (o instanceof XPathAxisStep) {
        XPathAxisStep step = (XPathAxisStep) o;

        short axis = step.getAxis();

        // if (XPathAxisStep.isDownward(axis) && !wasApproximated) {
        if (XPathAxisStep.isStrictlyDownwardOrSelf(axis) && !wasApproximated) {
          // if (axis == XPathAxisStep.SELF && step.getNodeTest() instanceof
          // XPathAnyNodeTest)
          // ;
          // else
          downwardizedPathPart.addStepAtHead(step);
        } else {
          wasApproximated = true;
          waywardPathPart.addStepAtHead(step);
        }
      }
    }
    this.wasApproximated = wasApproximated;

    if (usePrefilterCache)
      _filterExprFreeRepr = originalPath.toFilterExprFreeString();
  }

  /**
   * Shorts out child steps followed by parent steps. This is a dangerous thing
   * to do unless we are upper approximating - predicates might become lost; so
   * may the the test that the child exists at all.
   * 
   * @param expr
   * @return
   * @throws XSLToolsXPathException
   */
  private XPathPathExpr foldChildParent(XPathPathExpr expr)
      throws XSLToolsXPathException {
    short la = -1;
    XPathAxisStep ls = null;
    for (Iterator<XPathStepExpr> stepIter = expr.iterator(); stepIter.hasNext();) {
      XPathStepExpr step = stepIter.next();
      if (step instanceof XPathAxisStep) {
        XPathAxisStep as = (XPathAxisStep) step;
        if (as.getAxis() == XPathAxisStep.PARENT && la == XPathAxisStep.CHILD) {
          XPathPathExpr result = (XPathPathExpr) expr.clone();
          while (result.getStepCount() > 0 && result.getLastStep() != ls)
            result.removeLastStep();
          result.removeLastStep();
          while (stepIter.hasNext()) {
            result.addLastStep((XPathBase) stepIter.next().clone());
          }
          if (result.getStepCount() == 0)
            return expr;
          return foldChildParent(result);
        }
        la = as.getAxis();
        ls = as;
      }
    }
    return expr;
  }

  /*
   * Just an idea .. avoid huge SG fanouts for descendant-or-self::node()
   * followed by something. and no predicates in between... but there will be
   * trouble with the compatibility test in using it. The idea is to replace
   * /descendant-or-self::node()/a:t by /descendant::t which is the same.
   * 
   * For the future...
   * 
   * private XPathPathExpr zapLeadingSlashSlash(XPathPathExpr originalPath) { if (!
   * (originalPath instanceof XPathAbsolutePathExpr)) return originalPath;
   * 
   * if (originalPath.getStepCount()<2) return originalPath;
   * 
   * XPathStepExpr stetson = originalPath.getFirstStep();
   * 
   * if (!(stetson instanceof XPathAxisStep)) return originalPath;
   * 
   * XPathAxisStep ass = (XPathAxisStep)stetson;
   * 
   * if (ass.getAxis() != XPathAxisStep.DESCENDANT_OR_SELF) return originalPath;
   * 
   * if (!(ass.getNodeTest() instanceof XPathAnyNodeTest)) return originalPath;
   * 
   * XPathPathExp }
   */

  private XPathPathExpr _zapSelfNode(XPathPathExpr expr)
      throws XSLToolsXPathException {
    if (expr.getStepCount() <= 1)
      return expr;

    XPathPathExpr buildingClone = null;
    Object ls = null;

    for (Iterator<XPathStepExpr> stepIter = expr.iterator(); stepIter.hasNext();) {
      XPathStepExpr step = stepIter.next();
      if (step instanceof XPathAxisStep) {
        XPathAxisStep as = (XPathAxisStep) step;
        if (as.getAxis() == XPathAxisStep.SELF
            && as.getNodeTest() instanceof XPathAnyNodeTest) {
          buildingClone = (XPathPathExpr) expr.clone();
          while (buildingClone.getStepCount() > 0
              && buildingClone.getLastStep() != ls)
            buildingClone.removeLastStep();
        } else {
          if (buildingClone != null)
            buildingClone.addLastStep((XPathBase) as.clone());
        }
      }
      ls = step;
    }
    if (buildingClone != null)
      return buildingClone;
    return expr;
  }

  XPathPathExpr getInsensitiveMatchSelectPath() {
    if (originalPath instanceof XPathAbsolutePathExpr) {
      return originalPath;
    }
    // Concatenate match and select unless it is a root selection:
    if (contextInsensitiveMatchSelectPath == null) {
      // if (wasApproximated)
      // contextInsensitiveMatchSelectPath =
      // containingRuleMatchPattern.concatenation(originalPath);
      // else
      // if (!isTrivial || wasApproximated)
      contextInsensitiveMatchSelectPath = containingRuleMatchPattern
          .concatenation(originalPath);
      // else
      // contextInsensitiveMatchSelectPath = containingRuleMatchPattern;
      // contextInsensitiveMatchSelectPath.trimRedundancies();
    }
    return contextInsensitiveMatchSelectPath;
  }

  /*
   * Make a composite XPath exp: matchexp/self::context-type/select. In reality,
   * the path will look different -- often, the final step of the match pattern
   * is tightened up to have the effect of the self::context-type.
   */
  private XPathPathExpr sharpenMatchToType(XPathPathExpr match,
      DeclaredNodeType contextType) throws XSLToolsXPathException {
    XPathPathExpr result = null;
    // Otherwise: Merge all three.
    // Merge match and context node:
    if (contextType instanceof AttributeUse) {
      AttributeUse attributeDecl = (AttributeUse) contextType;

      // Alter the node test if necessary:
      // Change to: Just add self::name at end.??
      // OOPS: Will that work with attributes??? No....
      XPathAxisStep lastStep = (XPathAxisStep) match.getLastStep();
      assert (lastStep.getAxis() == XPathAxisStep.ATTRIBUTE);

      // principal = axis::*, allnodes = axis::node(), and axis is attribute
      if ((lastStep.getNodeTest() instanceof XPathNameTest && ((XPathNameTest) lastStep
          .getNodeTest()).isMultiple())
          || lastStep.getNodeTest() instanceof XPathAnyNodeTest) {
        // Alter to NameStep:
        XPathPathExpr matchClone = (XPathPathExpr) match.clone();
        // LocationStep oldStep = matchClone.getSteps().removeLast();
        // alter last step to attribute::<contextname>
        XPathStepExpr newStep = new XPathAxisStep(XPathAxisStep.ATTRIBUTE,
            new XPathNameTest(attributeDecl.getQName()));
        // newStep.getPredicates().addAll(oldStep.getPredicates());
        matchClone.replaceLastStep((XPathBase) newStep); // here it is.
        result = matchClone;
      }

      // the match exp ends in attribute::<name>; is the context node used
      // here a real context node (shown to be in the template's context set)?
      else if (lastStep.getNodeTest() instanceof XPathNameTest) {
        XPathNameTest nameTest = (XPathNameTest) lastStep.getNodeTest();
        // Names can not be anything but the same.
        if (!(nameTest.getQName().equals(attributeDecl.getQName()))) {
          System.err
              .println("A context type was seen that could not possibly have been matched by the match pattern (pattern is:"
                  + this + ", type is " + attributeDecl + ")");
        }
        result = (XPathPathExpr) match.clone();
      } else
        throw new AssertionError(
            "Context node was attribute-type, but last step of match was not a principal/all/name step!?!");

      // if (result == null)
      // System.out.println("HALT!!!");

      // Tweak: Add a step before too if absent:
      // If attributes shared among elements, this will become
      // impossible (so just remove it).
      if (result.getStepCount() == 1) {
        // Insert child step before to take advantage of element knowledge:
        // dongfang: That is, turn
        // attribute::<aname> into child::<ename(attr)>/attribute<aname>.
        QName ons = attributeDecl.getOwnerElementQName();
        // Set<? extends ElementDecl> ons =
        // attributeDecl.getOwnerElementDecls();
        XPathNodeTest newTest;
        // if (ons.size() != 1)
        // newTest = new XPathMultiNameTest(ons);
        // else
        newTest = new XPathNameTest(ons);
        XPathAxisStep newStep = new XPathAxisStep(XPathAxisStep.CHILD, newTest);
        result.addStepAtHead(newStep);
      }
    } else { // context NT is some element or text/comment/PI type
      // Alter the node test if necessary:
      XPathAxisStep lastStep = (XPathAxisStep) match.getLastStep();
      // child is the only alternative to attribute, so we should be sure...
      // WELL! Maybe self is allowed in, too.
      assert (lastStep.getAxis() == XPathAxisStep.CHILD);

      // principal = axis::*, allnodes = axis::node(), and axis is child
      if ((lastStep.getNodeTest() instanceof XPathNameTest && ((XPathNameTest) lastStep
          .getNodeTest()).isMultiple())
          || lastStep.getNodeTest() instanceof XPathAnyNodeTest) {
        // Alter to NameStep/TextNodeStep/CommentNodeStep/PINodeStep:
        result = (XPathPathExpr) match.clone();
        result.removeLastStep();

        XPathNodeTest newTest = null;

        // tighten up last match step
        if (contextType instanceof ElementUse)
          newTest = new XPathNameTest(((ElementUse) contextType).getQName());
        // tighten up last match step
        else if (contextType instanceof TextNT)
          newTest = new XPathTextTest();
        // tighten up last match step
        else if (contextType instanceof CommentNT)
          newTest = new XPathCommentTest();
        // tighten up last match step
        else if (contextType instanceof PINT)
          newTest = new XPathPITest();
        else
          throw new AssertionError("Unexpected node type : " + contextType);

        XPathAxisStep newStep = new XPathAxisStep(XPathAxisStep.CHILD, newTest);

        newStep.copyPredicatesFrom(lastStep);
        result.addLastStep(newStep);
      } else if (lastStep.getNodeTest() instanceof XPathNameTest) {
        // dongfang: it's ok already.
        XPathNameTest nameStep = (XPathNameTest) lastStep.getNodeTest();
        // Names can not be anything but the same.
        assert (nameStep.getQName().equals(((ElementUse) contextType)
            .getQName())) : "A context type was seen that could not possibly have been matched by the match pattern";
        result = (XPathPathExpr) match.clone(); // BUMMM
        // DONE: Dangerous or not???: Code coverage inspection found nothing
      } // else throw new AssertionError("sharpenMatchToType @ " + this + "
      // failed (no case applied) for match expr " + match + " type " +
      // contextType);
    } // else throw new AssertionError("sharpenMatchToType @ " + this + " failed
    // (no case applied) for match expr " + match + " type " + contextType);
    return result;
  }

  /*
   * Some of these should really be /descendant-of-self::node()/child::foo, but
   * become descendant-or-self::foo. Unless foo ever matched the root node type
   * (it never does), or there are any predicates involved (there ain't), the
   * two are equivalent.
   */
  private XPathAbsolutePathExpr mergeTypeSelect(DeclaredNodeType contextType) {

    XPathAbsolutePathExpr result = new XPathAbsolutePathExpr();
    result.addStepAtHead(new XPathAxisStep(XPathAxisStep.DESCENDANT_OR_SELF,
        new XPathAnyNodeTest()));

    if (contextType instanceof AttributeUse) {
      // cook a /descendant-or-self::node()/child::DNTs(parents(t))/attribute::t

      AttributeUse attNT = (AttributeUse) contextType;
      // If sharing attributes, this will become looser.
      XPathAxisStep newElementNameStep = new XPathAxisStep(XPathAxisStep.CHILD,
          new XPathNameTest(attNT.getOwnerElementQName())/* attNT.getOwnerElementDecls()) */);
      XPathAxisStep newAttributeNameStep = new XPathAxisStep(
          XPathAxisStep.ATTRIBUTE, new XPathNameTest(attNT.getQName()));
      result.addLastStep(newElementNameStep);
      result.addLastStep(newAttributeNameStep);

    } else if (contextType == RootNT.instance) {
      // cook a /
      result = new XPathAbsolutePathExpr();
    } else {// context NT is some element or text/comment/PI/root type
      XPathAxisStep newStep = null;
      if (contextType instanceof ElementUse)
        // cook a /descendant-or-self::node()/child::mu(t)
        newStep = new XPathAxisStep(XPathAxisStep.CHILD, new XPathNameTest(
            ((ElementUse) contextType).getQName()));
      else if (contextType instanceof TextNT)
        // cook a /descendant-or-self::node()/text()
        newStep = new XPathAxisStep(XPathAxisStep.CHILD, new XPathTextTest());
      else if (contextType == CommentNT.instance)
        // cook a /descendant-or-self::node()/comment()
        newStep = new XPathAxisStep(XPathAxisStep.CHILD, new XPathCommentTest());
      else if (contextType instanceof PINT) {
        // cook a /descendant-or-self::node()/processing-instruction()
        newStep = new XPathAxisStep(XPathAxisStep.CHILD, new XPathPITest());
        String t = ((PINT) contextType).getTarget();
        if (t != null && !("".equals(t)))
          ((XPathPITest) newStep.getNodeTest()).setTarget(t);
      } else
        assert (false) : "Unexpected node type: " + contextType;
      result.addStepAtHead(newStep);
    }
    result.addStepsFrom(downwardizedPathPart);

    return result;
  }

  private XPathExpr makeUnion(LinkedList<XPathPathExpr> subExps) {
    if (subExps.isEmpty())
      return null;
    if (subExps.size() == 1)
      return subExps.getFirst();
    XPathUnionExpr result = new XPathUnionExpr();
    for (XPathPathExpr e : subExps) {
      result.add(e);
    }
    return result;
  }

  /**
   * Return insensitive target set for a mode
   * 
   * @param mode
   * @return
   */
  Set<TemplateRule> getContextInsensitiveTargetSet(ContextMode mode) {
    SchemalessFlow flow = contextInsensitiveEdgeFlows.get(mode);
    if (flow == null)
      return Collections.emptySet();
    return flow.keySet();
  }

  /**
   * F_u function
   * 
   * @param mode
   * @param target
   * @return
   */
  Set<UndeclaredNodeType> getContextInsensitiveEdgeFlow(ContextMode mode,
      TemplateRule target) {
    return contextInsensitiveEdgeFlows.get(mode).get(target);
  }

  void putContextInsensitiveEdgeFlow(ContextMode mode, TemplateRule target,
      Set<UndeclaredNodeType> contexts) {
    SchemalessFlow slf = contextInsensitiveEdgeFlows.get(mode);
    if (slf == null) {
    	slf = new SchemalessFlow();
      contextInsensitiveEdgeFlows.put(mode, slf);
    }
    slf.put(target, contexts);
  }

  Set<ContextMode> getAllModes() {
    return contextMaps.keySet();
  }

  // ref fra Selection selv, getTargetTableForAllModes.
  private ContextFlow getAllModesContextFlow(DeclaredNodeType type) {
    ContextFlow result = new ContextFlow();
    for (Map.Entry<ContextMode, ModedContextMap> e : contextMaps.entrySet()) {
      ContextFlow f = e.getValue().get(type);
      if (f != null)
        result.putAll(f);
    }
    return result;
  }

  ContextFlow getContextFlow(ContextMode mode, DeclaredNodeType type) {
    ModedContextMap map = contextMaps.get(mode);
    if (map == null)
      return null;
    return map.get(type);
  }

  void putContextFlow(ContextMode mode, DeclaredNodeType contextNode,
      TemplateRule target, Set<DeclaredNodeType> flow) {

    ModedContextMap map = contextMaps.get(mode);

    if (map == null) {
      map = new ModedContextMap();
      contextMaps.put(mode, map);
    }

    ContextFlow cfl = map.get(contextNode);

    if (cfl == null) {
      spa.incrementCounter("ContextFlowCount", "ContextFlow");
      map.put(contextNode, cfl = new ContextFlow());
    }

    /*
     * This count is a nice sanity tester -- see that some modification leaves
     * it the same as that of a a known-to-be-good build.
     */
    spa.incrementCounter("ContextFlowTargetContextCount", "ContextFlow", flow
        .size());

    Set<DeclaredNodeType> cf = cfl.get(target);

    if (cf != null) {
      // System.err.println("OOPS, same context flow 2nd time...");
      cf.addAll(flow);
    } else
      cfl.put(target, flow);
  }

  /**
   * Fill up a map with possible flows for given context node. Map keys are
   * outflowing context nodes, and elements are sets of those template rules
   * that the outflowing nodes may flow to.
   * 
   * @param contextNode
   * @param table
   */

  void getTargetTableForAllModes(DeclaredNodeType contextType,
      Map<DeclaredNodeType, Set<TemplateRule>> table) { // Get flow for the
    // NodeType
    ContextFlow flow = getAllModesContextFlow(contextType);
    for (TemplateRule target : flow.keySet()) { // for each target found, get
      // thenode types that might hit
      // it
      Set<DeclaredNodeType> contextFlow = flow.get(target); // reverse mapping:
      // from map from
      // template rules to
      // node types,
      // make a mapping from node types to template rules
      for (DeclaredNodeType targetContext : contextFlow) { // Add to table:
        Set<TemplateRule> targetSet = table.get(targetContext);
        if (targetSet == null) {
          targetSet = new HashSet<TemplateRule>();
          table.put(targetContext, targetSet);
        }
        targetSet.add(target);
      }
    }
  }

  void getAllTargetsForMode(ContextMode mode, Set<TemplateRule> buffer) {
    ModedContextMap map = contextMaps.get(mode);
    if (map != null) {
      for (ContextFlow f : map.values()) {
        // result.addAll(f.keySet());
        for (Map.Entry<TemplateRule, Set<DeclaredNodeType>> realos : f
            .entrySet()) {
          if (realos.getValue() == null) {
            // System.err.println("YEAH RIGHT! Null.");
          } else if (realos.getValue().isEmpty()) {
            // System.err.println("YEAH RIGHT! Null.");
          } else {
            buffer.add(realos.getKey());
          }
        }
      }
    }
  }

  Set<TemplateRule> getAllTargetsForMode(ContextMode mode) {
    Set<TemplateRule> result = new HashSet<TemplateRule>();
    getAllTargetsForMode(mode, result);
    return result;
  }

  void getAllTargets(Set<TemplateRule> buffer) {
    for (ContextMode m : contextMaps.keySet()) {
      getAllTargetsForMode(m, buffer);
    }
  }

  Set<TemplateRule> getAllTargets() {
    Set<TemplateRule> result = new HashSet<TemplateRule>();
    getAllTargets(result);
    return result;
  }

  void getTargetsForContextType(DeclaredNodeType contextType,
      ModedContextMap input, Map<DeclaredNodeType, Set<TemplateRule>> dumper) { // Get
    // flow
    // for
    // the
    // NodeType
    ContextFlow flow = input.get(contextType);
    for (TemplateRule target : flow.keySet()) { // for each target found, get
      // thenode types that might hit
      // it
      Set<DeclaredNodeType> contextFlow = flow.get(target); // reverse mapping:
      // from map from
      // template rules to
      // node types,
      // make a mapping from node types to template rules
      for (DeclaredNodeType targetContext : contextFlow) { // Add to table:
        Set<TemplateRule> targetSet = dumper.get(targetContext);
        if (targetSet == null) {
          targetSet = new HashSet<TemplateRule>();
          dumper.put(targetContext, targetSet);
        }
        targetSet.add(target);
      }
    }
  }

  /**
   * Same as above, except that we do it for all context nodes (as opposed to
   * just one), and fill up a map with context node-->map&lt;outflow node for
   * context node, Set &lt;template rules for the outflow&gt;&gt;
   * 
   * @param table
   */
  void getAllContextFlows(ContextMode mode,
      Map<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>> table) {

    ModedContextMap map = contextMaps.get(mode);

    if (map == null)
      return;

    for (DeclaredNodeType context : map.keySet()) {

      Map<DeclaredNodeType, Set<TemplateRule>> forThisContext = table
          .get(context);

      if (forThisContext == null) {
        forThisContext = new HashMap<DeclaredNodeType, Set<TemplateRule>>();
      }

      getTargetsForContextType(context, map, forThisContext);
      if (!forThisContext.isEmpty()) {
        table.put(context, forThisContext);
      }
    }
  }

  void getAllContextFlowsOtherWayAround(ContextMode mode,
      Map<DeclaredNodeType, Map<TemplateRule, Set<DeclaredNodeType>>> table) {

    ModedContextMap map = contextMaps.get(mode);

    if (map == null)
      return;

    for (DeclaredNodeType context : map.keySet()) {

      Map<TemplateRule, Set<DeclaredNodeType>> forThisContext = table
          .get(context);

      if (forThisContext == null) {
        forThisContext = new HashMap<TemplateRule, Set<DeclaredNodeType>>();
      }

      Map<TemplateRule, Set<DeclaredNodeType>> cf = map.get(context);

      if (cf != null) {
        for (Map.Entry<TemplateRule, Set<DeclaredNodeType>> e : cf.entrySet()) {
          Set<DeclaredNodeType> there = forThisContext.get(e.getKey());
          if (there != null) {
            there.addAll(e.getValue());
          } else {
            forThisContext.put(e.getKey(), e.getValue());
          }
        }
      }

      if (!forThisContext.isEmpty()) {
        table.put(context, forThisContext);
      }
    }
  }

  void getAllContextFlowsForAllModesOtherWayAround(
      Map<DeclaredNodeType, Map<TemplateRule, Set<DeclaredNodeType>>> table) {
    for (ContextMode mode : getAllModes()) {
      getAllContextFlowsOtherWayAround(mode, table);
    }
  }

  void getAllContextFlowsForAllModes(
      Map<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>> table) {
    for (ContextMode mode : getAllModes()) {
      getAllContextFlows(mode, table);
    }
  }

  /*
   * Makes the MCS automaton for the context type.
   */
  Automaton doMakeAlphaIntersectInputAutomaton(DeclaredNodeType contextNode,
      XPathExpr alphaNodeSetExp, SingleTypeXMLClass inputType,
      Map<String, Automaton> globalAlphaCache, String alphaKey)
      throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException {

    String alphaRE = inputType.constructXPathRegExp(alphaNodeSetExp);
    Automaton alphaAutomatic = new RegExp(alphaRE).toAutomaton();
    Automaton schemaATS = inputType.getSchemaATSPathAutomaton();

    alphaAutomatic = alphaAutomatic.intersection(schemaATS);

    if (cacheAlpha) {
      globalAlphaCache.put(alphaKey, alphaAutomatic);
    }

    return alphaAutomatic;
  }

  /**
   * Return alpha exp for automata use; ie. nondownward steps are removed in
   * approximation. Self steps are retained; they only sharpen.
   * 
   * @param contextNode
   * @param inputType
   * @return
   * @throws XSLToolsUnhandledNodeTestException
   * @throws XSLToolsXPathException
   * @throws XSLToolsSchemaException
   */
  XPathExpr makeAlphaNodeSetExpForAutomata(DeclaredNodeType contextNode,
      SingleTypeXMLClass inputType) throws XSLToolsUnhandledNodeTestException,
      XSLToolsXPathException, XSLToolsSchemaException {

    XPathExpr result;

    // Handle root selection:
    if (containingRuleMatchPattern == slash) {
      // CANONICAL RootRoot selection. There is no containing template rule.
      return slash;
    }

    // Only special case for [root]: Absolutify the select expression: The
    // context type is root
    // anyway, and match can only be / (otherwise, error, we might want to
    // assert that)
    // If the select exp is not all-downward then we cannot do this.
    // If the select exp is absolute, then the normal cases will handle it OK:
    // Approximated case, will just seed the simulation test with [root].
    // Nonapproximated case, then the select is absolute and will be returned
    // anyway (OK)
    if (!wasApproximated && !(originalPath instanceof XPathAbsolutePathExpr)) {
      // case: Not approximated and not absolute
      if (contextNode == RootNT.instance) {
        // this always clones.
        result = slash.concatenation(downwardizedPathPart);
      } else {
        XPathPathExpr resultIsPath = sharpenMatchToType(
            containingRuleMatchPattern, contextNode);
        resultIsPath = resultIsPath.concatenation(originalPath);
        // resultIsPath.trimRedundancies();
        result = resultIsPath;
      }
    }

    else if (wasApproximated) {
      // case: Approximated, maybe absolute
      Set<? extends DeclaredNodeType> approximatedEvaluationResult = inputType
          .possibleTargetNodes(waywardPathPart, contextNode);

      LinkedList<XPathPathExpr> temp = new LinkedList<XPathPathExpr>();

      for (DeclaredNodeType nt : approximatedEvaluationResult) {
        temp.add(mergeTypeSelect(nt));
      }

      result = makeUnion(temp);
    }

    // Handle absolute selection path, where there are only downward steps:
    // else if (originalPath instanceof NodeSetAbsLocationPath) {
    else {
      // case: Not approximated, absolute.
      result = originalPath;
    }
    return result;
  }

  XPathExpr makeAlphaNodeSetExpForMills(DeclaredNodeType contextNode,
      SingleTypeXMLClass inputType) throws XSLToolsUnhandledNodeTestException,
      XSLToolsXPathException {

    XPathPathExpr result;

    // Handle root selection:
    if (containingRuleMatchPattern == slash) {
      // CANONICAL RootRoot selection. There is no containing template rule.
      return slash;
    }

    if (!(originalPath instanceof XPathAbsolutePathExpr)) {
      // case: Not approximated and not absolute
      if (contextNode == RootNT.instance) {
        // this always clones.
        result = slash.concatenation(originalPath);
      } else {
        XPathPathExpr resultIsPath = sharpenMatchToType(
            containingRuleMatchPattern, contextNode);
        resultIsPath = resultIsPath.concatenation(originalPath);
        // resultIsPath.trimRedundancies();
        XPathPathExpr almostresult = foldChildParent(_zapSelfNode(resultIsPath));
        // result = new XPathAbsolutePathExpr();
        // result.addStepAtHead(new
        // XPathAxisStep(XPathAxisStep.DESCENDANT_OR_SELF, new
        // XPathAnyNodeTest()));
        // result.addStepsFrom(almostresult);
        result = almostresult;
      }
    } else {
      result = originalPath;
    }
    return result;
  }

  /**
   * Get key for alpha caching
   * 
   * @param alphaNodeSetExp
   * @param contextNode
   * @return
   */
  String getAlphaKey(XPathExpr alphaNodeSetExp, DeclaredNodeType contextNode) {
    String exps = null;
    if (alphaNodeSetExp instanceof XPathPathExpr) {
      exps = ((XPathPathExpr) alphaNodeSetExp).toFilterExprFreeString();
    } else
      exps = alphaNodeSetExp.toString();
    String key = exps + contextNode.getIdentifier();
    return key;
  }

  Automaton getAlphaAutomaton(DeclaredNodeType contextNode,
      SingleTypeXMLClass inputType, XPathExpr alphaNodeSetExp,
      Map<String, Automaton> globalAlphaCache, String alphaKey)
      throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException,
      XSLToolsXPathException {
    spa.incrementCounter("AlphaAutomatonRequests", "Automata");
    Automaton a = null;

    spa.startTimer("AlphaAutomatonCreation", "AlphaBetaTest");

    if (cacheAlpha)
      a = globalAlphaCache.get(alphaKey);
    // System.out.println(key + "-->" + (a == null ? "null" :
    // a.getShortestExample(true)));
    if (a != null) {
      /*
       * Automaton compare = doMakeAlphaIntersectInputAutomaton(contextNode,
       * alphaNodeSetExp, inputType, globalAlphaCache); if (!a.equals(compare))
       * System.err.println("OUCH!!! Cached automaton not equal to on-the-fly
       * constructed!");
       */
      spa.incrementCounter("AlphaHit", "AlphaAutomatonRequests");
      spa.stopTimer("AlphaAutomatonCreation", "AlphaBetaTest");
      return a;
    }
    spa.incrementCounter("AlphaMiss", "AlphaAutomatonRequests");
    a = doMakeAlphaIntersectInputAutomaton(contextNode, alphaNodeSetExp,
        inputType, globalAlphaCache, alphaKey);
    spa.stopTimer("AlphaAutomatonCreation", "AlphaBetaTest");
    return a;
  }

  Automaton getAlphaIntersectInputAutomaton(DeclaredNodeType contextNode,
      SingleTypeXMLClass inputType, Map<String, Automaton> globalAlphaCache)
      throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException,
      XSLToolsXPathException {
    XPathExpr alphaExp = makeAlphaNodeSetExpForAutomata(contextNode, inputType);
    return getAlphaAutomaton(contextNode, inputType, alphaExp,
        globalAlphaCache, getAlphaKey(alphaExp, contextNode));
  }

  private Set<DeclaredNodeType> _makeMCSContextFlowUsingAutomata(
      CandidateEdge ce, SingleTypeXMLClass inputType,
      Map<String, Automaton> globalAlphaMap)
      throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException,
      XSLToolsXPathException {

    spa.startTimer("AlphaBetaTest", "CandidateAnalysis");

    XPathExpr alphaExp = makeAlphaNodeSetExpForAutomata(ce.contextType,
        inputType);
    String alphaKey = getAlphaKey(alphaExp, ce.contextType);

    Automaton alphaAutomaton = getAlphaAutomaton(ce.contextType, inputType,
        alphaExp, globalAlphaMap, alphaKey);

    spa.startTimer("AlphaAutomatonEmptyCheck", "AlphaBetaTest");
    if (alphaAutomaton.isEmpty()) {
      spa.incrementCounter("AlphaEmpty", "AlphaBetaTest");
      spa.stopTimer("AlphaAutomatonEmptyCheck", "AlphaBetaTest");
      spa.stopTimer("AlphaBetaTest", "ContextFlowDerivation");
      return Collections.emptySet();
    }
    spa.stopTimer("AlphaAutomatonEmptyCheck", "AlphaBetaTest");

    Set<DeclaredNodeType> flow;

    spa.startTimer("BetaAutomatonCreation", "AlphaBetaTest");
    Automaton betaAutomatic = ce.target.lazyMakeMatchAutomaton(inputType);
    // int targetNo = ce.target.index;
    spa.stopTimer("BetaAutomatonCreation", "AlphaBetaTest");

    spa.startTimer("AlphaBetaIntersection", "AlphaBetaTest");

    Automaton total = null;

    String key = null;

    if (cacheAlphaBeta) {
      // String matchPattern = ce.target.match.toFilterExprFreeString();
      // key = alphaKey + "#" + matchPattern;
      total = globalAlphaMap.get(key);
      if (total == null) {
        total = alphaAutomaton.intersection(betaAutomatic);
        // globalAlphaMap.put(key, total);

        // if (totalSlammer != null)
        // totalSlammer = totalSlammer.union(total);

      }
    } else {
      total = alphaAutomaton.intersection(betaAutomatic);
    }
    spa.stopTimer("AlphaBetaIntersection", "AlphaBetaTest");

    if (alphaExp instanceof XPathPathExpr) {
      spa.startTimer("Mills", "AlphaBetaTest");
      Set<? extends DeclaredNodeType> foo = inputType
          .possibleTargetNodes((XPathPathExpr) alphaExp);
      Set<? extends DeclaredNodeType> bar = inputType
          .possibleTargetNodes(ce.target.match);
      spa.stopTimer("Mills", "AlphaBetaTest");

      foo.retainAll(bar);

      spa.startTimer("Search", "AlphaBetaTest");
      flow = inputType.testDeclarationsFor(total, foo, key);
      spa.stopTimer("Search", "AlphaBetaTest");
    } else {
      spa.startTimer("StupidSearch", "AlphaBetaTest");
      // flow = inputType.getDeclarationsFor
      // (total, getContextInsensitiveEdgeFlow(ce.contextMode, ce.target));

      flow = new HashSet<DeclaredNodeType>();

      // Set<BackendDecl> decls = inputType.getDeclarationsFor
      // (total, getContextInsensitiveEdgeFlow(ce.contextMode, ce.target));

      // for (BackendDecl decl : decls) {
      // flow.addAll(decl.getAllUses());
      // }
      flow.addAll(inputType.getUsesFor(total, getContextInsensitiveEdgeFlow(
          ce.contextMode, ce.target)));

      spa.stopTimer("StupidSearch", "AlphaBetaTest");
    }
    spa.stopTimer("AlphaBetaTest", "CandidateAnalysis");

    // SNOWMAN! There it is !!
    if (ControlFlowConfiguration.current.useColoredContextTypes()) {
      Set<? extends DeclaredNodeType> limiter = inputType.possibleTargetNodes(
          ce.sourceSelect.originalPath, ce.contextType);
      flow.retainAll(limiter);
    }
    return flow;
  }

  Set<DeclaredNodeType> getContextFlow(CandidateEdge ce,
      SingleTypeXMLClass inputType, Map<String, Automaton> globalAlphaMap)
      throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException,
      XSLToolsXPathException {

    Set<DeclaredNodeType> flow = _makeMCSContextFlowUsingAutomata(ce,
        inputType, globalAlphaMap);

    return flow;
  }

  void makeSensitiveReverseFlowTable(ApplyTemplatesInst sourceApply) {
    for (ContextMode sourceMode : getAllModes()) {
      ContextMode targetMode = sourceMode;
      if (sourceApply != null)
        targetMode = sourceApply.getMode().contextualize(sourceMode);
      ModedContextMap map = contextMaps.get(sourceMode);
      for (Map.Entry<DeclaredNodeType, ContextFlow> e : map.entrySet()) {
        for (Map.Entry<TemplateRule, Set<DeclaredNodeType>> e2 : e.getValue()
            .entrySet()) {
          for (DeclaredNodeType targetType : e2.getValue()) {
            DeclaredNodeType sourceType = e.getKey();
            TemplateRule source = sourceApply.containingRule;
            TemplateRule target = e2.getKey();
            NewContextFlow toSource = new NewContextFlow(sourceType, source,
                sourceMode);
            target.addSensitiveInflow(targetMode, targetType, toSource);
          }
        }
      }
    }
  }

  XPathPathExpr getOriginalPath() {
    return originalPath;
  }

  /**
   * Here is the base of most optimizations! Running the alpha-beta test on each
   * and every combination of (source template, source context, target template)
   * is slow beyond all hope (but for the patient, or for the paranoid, it could
   * be done, just to be sure...). Instead, some earlier analysis results, as
   * well as some researced facts, are used for reducing the number of candidate
   * flows that must be examined in details. It works two-way: Some flows can be
   * shown not to exit. Among these are the flows that are not represented in
   * the insensitive flow graph -- these are not even considered here. Among
   * those are flows from apply-templates instructions to templates of a
   * different mode. Other kinds include those that fail the insensitive regexp
   * test, or fail the parallel end-to-end abstract XPath evaluation test. Some
   * flows can be shown already here to pass the context sensitive regexp alpha
   * beta test. These are flows through selection and match expressions so
   * simple that it can be determined through a reverse step-thru-step abstract
   * XPath evaluation test that they will pass the regexp test too. These are
   * marked as "will survive, don't need to test".
   */

  void locateCandidateEdgesForAutomata(DeclaredNodeType contextType,
      SingleTypeXMLClass inputType, LinkedList<CandidateEdge> work,
      Map<String, Map<DeclaredNodeType, Set<DeclaredNodeType>>> _skunk,
      ApplyTemplatesInst apply, ContextMode mode, int selIndex)
      throws XSLToolsException {

    PerformanceLogger pa = DiagnosticsConfiguration.current
        .getPerformanceLogger();

    XPathExpr alphaExprForMills = null;

    Set<DeclaredNodeType> s1 = null;
    Set<DeclaredNodeType> s2 = null;

    Map<DeclaredNodeType, Set<DeclaredNodeType>> _subSkunk = null;

    // if (useAbstractFlowAlgoritm) {
    if (usePrefilterCache) {
      _subSkunk = _skunk.get(_filterExprFreeRepr);
      if (_subSkunk == null) {
        _subSkunk = new HashMap<DeclaredNodeType, Set<DeclaredNodeType>>();
        _skunk.put(_filterExprFreeRepr, _subSkunk);
      }
      s1 = _subSkunk.get(contextType);
    }

    if (s1 == null) {
      s1 = inputType.possibleTargetNodes(originalPath, contextType);
      if (usePrefilterCache) {
        _subSkunk.put(contextType, s1);
      }
    }
    // }

    // For possible target in the RawTAG flow graph:
    for (TemplateRule target : getContextInsensitiveTargetSet(mode)) {
      // assert (apply.templateInvocationCompatible(target)) : "Funny, this flow
      // should have been gone by now...";

      pa
          .incrementCounter("EdgesConsideredAsCandidates",
              "SensitiveFlowGrapher");

      // if the abstract evaluation algorithm is not used, we consider the test
      // passed.

      boolean abstractTestPassed = false;// !useAbstractFlowAlgoritm;
      boolean incompatiblePathsTestPassed = true;
      boolean prefilterTestPassed = true;

      /*
       * The end-to-end abstract XPath evaluation kill prediction test
       */
      if (!abstractTestPassed) {
        if (usePrefilterCache) {
          Set<? extends DeclaredNodeType> x = new HashSet<DeclaredNodeType>(
              target.getDefinitelyAcceptableTypes());
          x.retainAll(s1);
          prefilterTestPassed = !x.isEmpty();
        }

        if (prefilterTestPassed) { // why the heck repeat after pass?
          pa.startTimer("MillDeathTest", "CandidateSelection");
          s2 = inputType.possibleTargetNodes(target.match);
          s2.retainAll(s1);
          abstractTestPassed = !s2.isEmpty();
          if (abstractTestPassed
              && ControlFlowConfiguration.current.useOrthogonalAbstractTest()) {
            pa.startTimer("orthogonal", "MillDeathTest");

            alphaExprForMills = makeAlphaNodeSetExpForMills(contextType,
                inputType);

            if (inputType.incompatiblePaths((XPathPathExpr) alphaExprForMills,
                target.match, s2) > 0) {

              Set<DeclaredNodeType> death = new HashSet<DeclaredNodeType>(s2);
              pa.incrementCounter("xxxMillKill", "SensitiveFlowGrapher", death
                  .size());

              incompatiblePathsTestPassed = false;
              abstractTestPassed = false;
            }
            pa.stopTimer("orthogonal", "MillDeathTest");
          }
          pa.stopTimer("MillDeathTest", "CandidateSelection");
        }
      }

      /*
       * Comprehensive kill prediction
       */
      boolean shouldBeKilled = !abstractTestPassed;

      /*
       * Log the credits for the kill
       */
      if (shouldBeKilled) {
        if (!abstractTestPassed) {
          pa
              .incrementCounter("KilledByMillTest",
                  "EdgesConsideredAsCandidates");
          if (!incompatiblePathsTestPassed)
            pa.incrementCounter("KilledByOrthogonalMillTest",
                "KilledByMillTest");
          else if (!prefilterTestPassed)
            pa.incrementCounter("KilledByPrefilter", "KilledByMillTest");
          else
            pa.incrementCounter("KilledByStraightMillTest", "KilledByMillTest");
        }
      }

      if (!shouldBeKilled) {
        CandidateEdge newEdge = new CandidateEdge(apply, selIndex, this,
            contextType, target, mode);

        newEdge.maybeSurviveableTypes = s2;

        newEdge.alphaExprForMills = alphaExprForMills;

        /*
         * This assignment is for diagnostics only (when soft-killing)
         */
        newEdge.wasPredictedToDie = shouldBeKilled;

        /*
         * Make survival prediction. In the nasty case that it becomes a union
         * exp, just chicken out and don't attempt to determine survival.
         */
        work.addLast(newEdge);
        pa.incrementCounter("SurvivedCandidateEdges",
            "EdgesConsideredAsCandidates");
      } else {
      }
    }
  }

  boolean hasEmStinkingDecorators(Set<? extends DeclaredNodeType> typeset) {
    for (DeclaredNodeType type : typeset)
      if (type instanceof DynamicRedeclaration)
        return true;
    return false;
  }

  void locateCandidateEdgesForMills(DeclaredNodeType contextType,
      SingleTypeXMLClass inputType, LinkedList<CandidateEdge> work,
      Map<String, Map<DeclaredNodeType, Set<DeclaredNodeType>>> cache,
      ApplyTemplatesInst apply, ContextMode mode, int selIndex)
      throws XSLToolsException {

    if (apply != null) {
      contextType = apply.applyFilters(inputType, contextType);
    }

    if (contextType == null)
      return;

    PerformanceLogger pa = DiagnosticsConfiguration.current
        .getPerformanceLogger();

    XPathExpr alphaExprForMills = null;

    Set<DeclaredNodeType> selected = null;
    Set<? extends DeclaredNodeType> matched = null;

    Map<DeclaredNodeType, Set<DeclaredNodeType>> cached = null;

    if (usePrefilterCache) {
      cached = cache.get(_filterExprFreeRepr);
      if (cached == null) {
        cached = new HashMap<DeclaredNodeType, Set<DeclaredNodeType>>();
        cache.put(_filterExprFreeRepr, cached);
      }
      selected = cached.get(contextType);
    }

    if (selected == null) {
      pa.startTimer("MillLiveTest", "CandidateSelection");
      selected = inputType.possibleTargetNodes(originalPath, contextType);
      if (usePrefilterCache) {
        cached.put(contextType, selected);
      }
      pa.stopTimer("MillLiveTest", "CandidateSelection");
    }

    if (selected.isEmpty()) {
      pa.incrementCounter("AbsurdSelectionsInContext", "Death");
    }

    boolean selectedHasDO = hasEmStinkingDecorators(selected);

    // For possible target in the RawTAG flow graph:
    for (TemplateRule target : getContextInsensitiveTargetSet(mode)) {

      int incompatiblePathsKills = 0;

      pa
          .incrementCounter("EdgesConsideredAsCandidates",
              "SensitiveFlowGrapher");

      matched = /* new HashSet<DeclaredNodeType>( */target
          .getDefinitelyAcceptableTypes()/* ) */;

      pa.startTimer("MillLiveTest", "CandidateSelection");

      boolean matchedHasDO = hasEmStinkingDecorators(matched);

      //if (selectedHasDO || matchedHasDO) {
      //  System.out.println("we have a situation here.");
      //}

      // First a simple intersection .. left over in matched.
      // The semantics should be:
      /*
       * Two identical node types: Keep. Two identical dropoffs: Keep. Two
       * compatible dropoffs: Ooops. One node type, one dropoff: Keep the
       * dropoff, iff it decorates the type.
       */
      Set<DeclaredNodeType> clumsy = new HashSet<DeclaredNodeType>();
      /*
       * At most the match has DRs. We can iterate thru them, and remove those
       * that are not contained in select, or whose decorated value is not
       * contained in select.
       */
      for (DeclaredNodeType type : matched) {
        if (selected.contains(type)
            || selected.contains(type.getOriginalDeclaration()))
          clumsy.add(type);
      }

      if (selectedHasDO) {
        /*
         * The select may have DOs. We have to retain all in match that: if s is
         * raw node, m is the same node, m. if s is decorator, m is decorator,
         * either if they are compatible and neither if not if s is raw node, m
         * is decorator over s, m. if s is raw node, m is different raw node,
         * nothing.
         */
        for (Iterator<DeclaredNodeType> typei = selected.iterator(); typei
            .hasNext();) {
          DeclaredNodeType type = typei.next();
          if (matched.contains(type.getOriginalDeclaration()))
            clumsy.add(type);
        }
      }

      pa.stopTimer("MillLiveTest", "CandidateSelection");
      alphaExprForMills = makeAlphaNodeSetExpForMills(contextType, inputType);
      pa.startTimer("MillDeathTest", "CandidateSelection");
      for (Iterator<DeclaredNodeType> iq = clumsy.iterator(); iq.hasNext();) {

        DeclaredNodeType q = iq.next();

        short causeOfDeath = inputType.incompatiblePaths(
            (XPathPathExpr) alphaExprForMills, target.match, q);

        if (causeOfDeath > 0) {
          iq.remove();
          incompatiblePathsKills++;
        }
      }

      pa.stopTimer("MillDeathTest", "CandidateSelection");

      /*
       * Log the credits for the kill
       */

      if (clumsy.isEmpty())
        pa.incrementCounter("KilledByMillTest", "EdgesConsideredAsCandidates");

      pa.incrementCounter("KilledByOrthogonalMillTest", "KilledByMillTest",
          incompatiblePathsKills);
      /*
       * Make the candidate edge, if not predicted to die
       */
      if (!clumsy.isEmpty()) {
        CandidateEdge newEdge = new CandidateEdge(apply, selIndex, this,
            contextType, target, mode);

        this.allTypesEverSelected.addAll(clumsy);

        newEdge.maybeSurviveableTypes = clumsy;
        newEdge.alphaExprForMills = alphaExprForMills;

        /*
         * Make survival prediction. In the nasty case that it becomes a union
         * exp, just chicken out and don't attempt to determine survival.
         */
        work.addLast(newEdge);
        pa.incrementCounter("SurvivedCandidateEdges",
            "EdgesConsideredAsCandidates");
      } else {
      }
    }
  }

  /*
   * Construct fragment for context. Don't assemble just construct.
   */
  Node constructSGSubgraph(SingleTypeXMLClass clazz, ContextMode mode,
      DeclaredNodeType contextType, SGFragment global, ContentOrder order,
      boolean allowInterleave, int index)
      throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException {

    Set<DeclaredNodeType> limiter = new HashSet<DeclaredNodeType>();

    // Map<DeclaredNodeType,DeclaredNodeType> limiter = new
    // IdentityHashMap<DeclaredNodeType,DeclaredNodeType>();

    ContextFlow cf = getContextFlow(mode, contextType);

    if (cf != null) {
      cf.getContextFlowSuccessorsForAllTargets(limiter);
    }

    SGFragment fraggle;

    if (index >= 0)
      fraggle = clazz.constructSGFragment(global, originalPath, contextType,
          limiter, order, allowInterleave);
    else
      fraggle = global;

    Map<DeclaredNodeType, SGFragment> forMode = myModedFragments.get(mode);
    if (forMode == null) {
      forMode = new HashMap<DeclaredNodeType, SGFragment>();
      myModedFragments.put(mode, forMode);
    }

    forMode.put(contextType, fraggle);

    if (fraggle == null)
      fraggle = global;
    return fraggle.getEntryNode();
  }

  /*
   * For the sanity test
   */
  void inferContextSets(
      ContextMode mode,
      Map<ContextMode, Map<TemplateRule, Set<DeclaredNodeType>>> inferredContextSets,
      ApplyTemplatesInst containingApply) {

    Map<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>> cfxxx = new HashMap<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>>();
    getAllContextFlows(mode, cfxxx);

    ContextMode newMode = mode;

    if (containingApply != null)
      newMode = containingApply.getMode().contextualize(mode);

    for (Map.Entry<DeclaredNodeType, Map<DeclaredNodeType, Set<TemplateRule>>> e : cfxxx
        .entrySet()) {
      Map<DeclaredNodeType, Set<TemplateRule>> cfx = e.getValue();
      for (Map.Entry<DeclaredNodeType, Set<TemplateRule>> e2 : cfx.entrySet()) {
        for (TemplateRule t : e2.getValue()) {
          if (!t.getContextSet(newMode).contains(e2.getKey())) {
            System.err
                .println("Edge flow type missing in context set (maybe the edge flow should have been removed...)");
            System.err.println("Source template of edge flow: "
                + containingApply.containingRule);
            System.err.println("Source select of flow: " + this);
            System.err
                .println("Source context type of flow (derived from sel edges, not the template): "
                    + e.getKey());
            System.err.println("Target context type: " + e2.getKey());
            System.err.println("Target template rule: " + t);
          }

          Map<TemplateRule, Set<DeclaredNodeType>> inferredForMode = inferredContextSets
              .get(newMode);
          if (inferredForMode == null) {
            inferredForMode = new HashMap<TemplateRule, Set<DeclaredNodeType>>();
            inferredContextSets.put(newMode, inferredForMode);
          }

          Set<DeclaredNodeType> simcon = inferredForMode.get(t);
          if (simcon == null) {
            simcon = new HashSet<DeclaredNodeType>();
            inferredForMode.put(t, simcon);
            t.inferContextSets(newMode, inferredContextSets);
          }
          simcon.add(e2.getKey());
        }
      }
    }
  }

  void constructSGFragments(ContextMode mode, DeclaredNodeType contextType,
      SingleTypeXMLClass clazz, XMLGraph sg, ApplyTemplatesInst containingApply)
      throws XSLToolsException {

    ContextMode newMode = mode;
    if (containingApply != null) {
      newMode = containingApply.getMode().contextualize(mode);
      contextType = containingApply.applyFilters(clazz, contextType);
    }
    
    ContextFlow cf = getContextFlow(mode, contextType);
    if (cf != null) {
      for (Map.Entry<TemplateRule, Set<DeclaredNodeType>> e : cf.entrySet()) {
        TemplateRule target = e.getKey();
        for (DeclaredNodeType targetType : e.getValue()) {
          SGFragment fraggle = target.getSGFragment(newMode, targetType);
          if (fraggle == null)
            target.constructSGFragments(newMode, targetType, clazz, sg, false);
        }
      }
    }
  }

  void hookupFragments(ContextMode mode, SGFragment maddafokka,
      ApplyTemplatesInst containingApply,
      Map<ContextMode, Set<TemplateRule>> done) throws XSLToolsException {

    PerformanceLogger pa = DiagnosticsConfiguration.current
        .getPerformanceLogger();

    ContextMode newMode = mode;

    if (containingApply != null)
      newMode = containingApply.getMode().contextualize(mode);

    /*
     * For each context type: Targets of each outflowing context type
     */
    Map<DeclaredNodeType, Set<TemplateRule>> reversed = new HashMap<DeclaredNodeType, Set<TemplateRule>>();

    // for (Map.Entry<ContextMode, ModedContextMap> e : contextMaps.entrySet())
    // {

    ModedContextMap cm = contextMaps.get(mode);

    if (cm == null) {
      // System.err.println("cm null for mode " + mode + " @ " + this + " @ "
      // + containingApply + " @ " + containingApply.containingRule.index);
      cm = new ModedContextMap();
    }

    /*
     * For each (income to the template rule of this selection) context type:
     * Grab the fragment
     */
    for (Map.Entry<DeclaredNodeType, ContextFlow> contextFlowMapping : cm
        .entrySet()) {
      DeclaredNodeType contextType = contextFlowMapping.getKey();

      // fusk: Hvis der ikke er en outputter for denne sel, mode, type, saa smut
      // udenom...
      // den rigtige loesning ville vaere slet ikke at rekursere hertil.
      if (containingApply != null
          && !containingApply.containingRule.hasOutputterFor(mode, contextType)) {
        pa.incrementCounter("LiveButNotReachingOutput", "SGFragments");
        continue;
      }

      pa.incrementCounter("LiveAndReachingOutput", "SGFragments");

      SGFragment fraggle = null;
      try {
        fraggle = myModedFragments.get(mode).get(contextType);
      } catch (RuntimeException ex) {
        System.err.println("Oops, got an exception");
        System.err.println("Selection: " + this);
        System.err.println("Context type: " + contextType);
        System.err.println("Mode: " + mode);
      }

      if (fraggle == null) {
        System.err.println("Got null for context type " + contextType + " at " + this);
        System.err.println("Had: " + myModedFragments.get(mode));
      }

      ContextFlow contextFlow = contextFlowMapping.getValue();

      /*
       * Each incoming context type has a function {template
       * rules}-->Set{context types going the that rule}
       */
      for (Map.Entry<TemplateRule, Set<DeclaredNodeType>> flowForContextType : contextFlow
          .entrySet()) {

        TemplateRule target = flowForContextType.getKey();
        Set<DeclaredNodeType> outflowTypesForContextTypeAndTarget = flowForContextType
            .getValue();
        /*
         * For each context type going to a rule
         */
        for (DeclaredNodeType outflowType : outflowTypesForContextTypeAndTarget) {
          /*
           * except these, which we ignore
           */
          if ((outflowType == CommentNT.instance || outflowType instanceof PINT)
              && !ControlFlowConfiguration.current.useCommentPIPropagation()) {
            // continue;
          }
          /*
           * Add their target to reversed map
           */
          Set<TemplateRule> targetsForTargetType = reversed.get(outflowType);
          if (targetsForTargetType == null) {
            targetsForTargetType = new HashSet<TemplateRule>();
            reversed.put(outflowType, targetsForTargetType);
          }
          targetsForTargetType.add(target);
        }
      }

      /*
       * For each outflowing context type
       */
      for (Map.Entry<DeclaredNodeType, Set<TemplateRule>> e2 : reversed
          .entrySet()) {
        /*
         * Get the node for the type in the SG fragment
         */

        Set<Integer> flows = new HashSet<Integer>();
        /*
         * Now make an edge to each target
         */

        int schwamm = -1;
        for (TemplateRule target : e2.getValue()) {
          // assert (target.mode.accepts(newMode));

          SGFragment his = target.getSGFragment(newMode, e2.getKey());

          if (his == null) {
            if (target.hasOutputterFor(newMode, e2.getKey())) {
              System.err.println("Ouch!");
              System.err.println("Was asked for: " + e2.getKey());
              System.err.println("On rule: " + target);
              System.err
                  .println("That's a serious indication of a too-small context set, or discrepancy between flow on edges vs. context sets.");
              System.err
                  .println("Context set contains the so called inflowing node type: "
                      + target.getContextSet(newMode).contains(e2.getKey()));
            }
            if (schwamm < 0) {
              schwamm = maddafokka.createEpsilonNode().getIndex();
            }
            flows.add(schwamm);
          } else
            flows.add(his.getEntryNode().getIndex());
        }

        if (fraggle == null) {
          System.out.println(contextType);
          /*
          ChoiceNode root = (ChoiceNode) maddafokka.getEntryNode();
          Set<Integer> idxx = new HashSet<Integer>(root.getContents());
          for (int i : flows) {
            idxx.add(i);
          }
          root.setContent(idxx, maddafokka.getXMLGraph());
          */
        } else {
          DeclaredNodeType key = e2.getKey();
          ChoiceNode cn = fraggle.get(key);
          if (cn == null) {
            if ((key != CommentNT.instance && !(key instanceof PINT))
                || ControlFlowConfiguration.current.useCommentPIPropagation()) {
              throw new XSLToolsException(
                  "Internal error: Missing placeholder for type: " + key
                      + " missing @hookupFragments, fragment: " + fraggle
                      + ", @ selection: " + this + ", @ apply: "
                      + containingApply + ", @ rule: "
                      + containingApply.containingRule.index);
            }
          } else {
            if (!cn.getContents().isEmpty()) {
              System.err.println("Double-d...");
              System.err.println(fraggle);
            }
            cn.setContent(flows, fraggle.getXMLGraph());
          }
        }
      }
      reversed.clear();
    }

    for (TemplateRule target : getAllTargetsForMode(mode)) {
      Set<TemplateRule> d = done.get(newMode);
      if (d == null) {
        d = new HashSet<TemplateRule>();
        done.put(newMode, d);
      }
      if (d.add(target)) {
        target.hookupFragments(newMode, maddafokka, done);
      }
    }
  }

  /*
   * public void diagnostics(Branch parent, DocumentFactory fac) { Set<String>
   * params = new HashSet<String>(); if
   * (DiagnosticsConfiguration.current.outputSchemalessStuffInControlFlowGraph())
   * params.add("outputSchemalessStuffInControlFlowGraph"); }
   */

  public void diagnostics(Branch parent, DocumentFactory fac,
      Set<Object> configuration) {
    Element me = fac.createElement(Util
        .capitalizedStringToHyphenString(getClass()));
    parent.add(me);
    /*
     * me.addAttribute("containing-template-match", containingRuleMatchPattern ==
     * null ? "null" : containingRuleMatchPattern.toString());
     */
    me.addAttribute("original-path", originalPath == null ? "null"
        : originalPath.toString());

    me.addAttribute("downward-path-part", downwardizedPathPart == null ? "null"
        : downwardizedPathPart.toString());

    me.addAttribute("wayward-path-part", waywardPathPart == null ? "null"
        : waywardPathPart.toString());

    List<TemplateRule> keyList2;

    if (DiagnosticsConfiguration
        .outputSchemalessStuffInControlFlowGraph(configuration)) {

      Element contextInsensitiveEdgeFlowsDiag = fac
          .createElement("schemaless-flows");
      me.add(contextInsensitiveEdgeFlowsDiag);

      if (contextInsensitiveEdgeFlows != null) {
        for (ContextMode mode : contextInsensitiveEdgeFlows.keySet()) {
          keyList2 = new ArrayList<TemplateRule>(
              getContextInsensitiveTargetSet(mode));

          Collections.sort(keyList2, ControlFlowFunctions
              .getTemplateDiagnosticsOrdering());

          Element hundeprut = fac.createElement("under-mode");
          hundeprut.addAttribute("mode", mode.toString());
          contextInsensitiveEdgeFlowsDiag.add(hundeprut);

          for (TemplateRule target : keyList2) {
            // Element targetEdgeFlowDiag = fac.createElement("context-flow");
            // targetEdgeFlowDiag.addAttribute("target-rule-index",
            // Integer.toString(target.index));
            Element targetx = target.briefIdDiagnostics(hundeprut,
                "target-template", fac);
            // hundeprut.add(targetEdgeFlowDiag);
            for (NodeType nt : getContextInsensitiveEdgeFlow(mode, target)) {
              Element ntDiag = fac.createElement("undeclared-node-type");
              ntDiag.addAttribute("type", nt.toString());
              targetx.add(ntDiag);
            }
          }
        }
      }
    }

    Element flowsDiag = fac.createElement("schema-context-flows");
    me.add(flowsDiag);

    for (Map.Entry<ContextMode, ModedContextMap> e : contextMaps.entrySet()) {

      Element mode = fac.createElement("under-mode");
      mode.addAttribute("mode", e.getKey().toString());
      flowsDiag.add(mode);

      List<DeclaredNodeType> sorted = new ArrayList<DeclaredNodeType>(e
          .getValue().keySet());

      Collections.sort(sorted);

      for (DeclaredNodeType nt : sorted) {
        Element flowDiag = fac.createElement("under-context-type");
        flowDiag.addAttribute("type", nt.toLabelString());

        mode.add(flowDiag);
        ContextFlow cf = e.getValue().get(nt);

        /*
         * nt.diagnostics(mode, fac, configuration);
         * 
         * Element goddammit = (Element) mode.elements().get(
         * mode.elements().size() - 1); boolean munchmunch =
         * cf._diagnostics(mode, fac, configuration);
         */
        // nt.diagnostics(flowDiag, fac, configuration);
        // Element goddammit = (Element) flowDiag.elements().get(
        // flowDiag.elements().size() - 1);
        /* boolean munchmunch = */cf._diagnostics(flowDiag, fac, configuration);
        // if (!munchmunch)
        // goddammit.detach();
      }
    }
  }

  public String toString() {
    // try {
    return "<select originalPath=\"" + originalPath + "\" downwardizedPath=\""
        + downwardizedPathPart + "\" waywardPath=\"" + waywardPathPart
        // + "\" zapdDownPath=\"" + _zapSelfNode(downwardizedPathPart)
        + "\" containingMatch=\"" + containingRuleMatchPattern + "\"/>";
  }
}
