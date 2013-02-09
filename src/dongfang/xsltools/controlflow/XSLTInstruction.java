package dongfang.xsltools.controlflow;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Automata;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.XPathConstants;
import dongfang.XSLConstants;
import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.ParseLocationUtil;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsMalformedCoreXSLException;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsUnhandledNodeTestException;
import dongfang.xsltools.exceptions.XSLToolsXPathParseException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.Util;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.schemaside.dropoff.CardinalMatch;
import dongfang.xsltools.xmlclass.schemaside.dropoff.DynamicRedeclaration;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NamedNodeType;
import dongfang.xsltools.xmlclass.xslside.RootNT;
import dongfang.xsltools.xmlclass.xslside.TextNT;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathFunctionCallExpr;
import dongfang.xsltools.xpath2.XPathParser;
import dongfang.xsltools.xpath2.XPathPathExpr;
import dongfang.xsltools.xpath2.XPathStringLiteral;
import dongfang.xsltools.xpath2.XPathUnionExpr;

public abstract class XSLTInstruction implements Diagnoseable {

  final String[] originalElementIds = new String[2];

  final StylesheetModule originalModule;

  protected void moreDiagnostics(Element me, DocumentFactory fac) {
  }

  /*
   * Do a standard thing -- make an element of the class name -- and then call
   * moreDiagnostics.
   */
  /*
   * public void diagnostics(Branch parent, DocumentFactory fac) { Set<String>
   * params = new HashSet<String>(); if
   * (DiagnosticsConfiguration.current.outputAllInstructionsInControlFlowGraph()) {
   * params.add("outputAllInstructionsInControlFlowGraph"); } }
   */

  public void diagnostics(Branch parent, DocumentFactory fac,
      Set<Object> configuration) {
    if (DiagnosticsConfiguration
        .outputAllInstructionsInControlFlowGraph(configuration)) {
      Element me = fac.createElement(Util
          .capitalizedStringToHyphenString(getClass()));
      parent.add(me);
      moreDiagnostics(me, fac);
    }
  }

  XSLTInstruction(StylesheetModule originalModule, String simplifiedId,
      String originalId) {
    this.originalModule = originalModule;
    this.originalElementIds[StylesheetModule.CORE] = simplifiedId;
    this.originalElementIds[StylesheetModule.ORIGINAL] = originalId;
  }

  void addSubInstruction(XSLTInstruction subInstruction)
      throws XSLToolsMalformedCoreXSLException {
    throw new XSLToolsMalformedCoreXSLException("This kind of instruction ("
        + getClass().getSimpleName() + ") does not support sub-instructions");
  }

  Node createSGSubgraph(SingleTypeXMLClass clazz, ContextMode mode,
      DeclaredNodeType contextType, SGFragment sgFragment,
      boolean allowInterleave) throws XSLToolsException {
    throw new AssertionError("createSGFragment called on "
        + getClass().getSimpleName());
  }

  enum NameBehavior {
    LOCALNAME, NAMESPACE, QNAME
  };

  /*
   * Make automaton for simple stringy exprs.
   */
  Automaton cook(SingleTypeXMLClass clazz, XPathExpr exp,
      DeclaredNodeType contextType, NameBehavior nb,
      String noNamespaceBehavior, Automaton panicPatch)
      throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException {

    /*
     * Automaton of union expr is union automaton of each expr.
     */
    if (exp instanceof XPathUnionExpr) {
      Automaton result = new Automaton();
      for (Iterator<XPathExpr> subexp = ((XPathUnionExpr) exp).iterator(); subexp
          .hasNext();) {
        result = result.union(cook(clazz, subexp.next(), contextType, nb,
            noNamespaceBehavior, panicPatch));
      }
      return result;
    } else if (exp instanceof XPathStringLiteral) {
      String s = ((XPathStringLiteral) exp).getSweetContent();
      // DONE: String already unescaped when pulled out of literal. Don't do
      // twice.
      return Automaton.makeString((s));
    } else if (exp instanceof XPathPathExpr) {
      Automaton result = new Automaton();
      // attempt at generalizing...

      Set<? extends DeclaredNodeType> types = clazz.possibleTargetNodes(
          (XPathPathExpr) exp, contextType);

      /*
       * Some ugly diagnostics side effecting... TODO: make nicer. Get away from
       * the schema clazz.
       */
      clazz.addValueOfTouchedTypes(types, (XPathPathExpr) exp, contextType);

      // now get the text model of each and unionize .. for now, we just give
      // upfor anything
      // else than attributes... elements should not be hard to approximate,
      // however.
      for (DeclaredNodeType type : types) {
        Automaton part = type.getValueOfAutomaton(clazz);
        result = result.union(part);
      }
      return result;
    } else if (exp instanceof XPathFunctionCallExpr) {
      XPathFunctionCallExpr fce = (XPathFunctionCallExpr) exp;
      QName funcName = fce.getQName();

      if (funcName.getName().equals(XPathConstants.FUNC_UNKNOWN_STRING))
        return panicPatch;

      // try cook the name function...
      boolean argIsSelfNode = false;

      if (fce.arity() == 0)
        argIsSelfNode = true;
      else if (fce.arity() == 1) {
        XPathExpr arg = fce.getArgument(0);
        if (arg.toString().equals(".") || arg.toString().equals("self::node()"))
          argIsSelfNode = true;
      }

      if (contextType instanceof NamedNodeType) {
        QName name = ((NamedNodeType) contextType).getQName();
        if (XPathConstants.isLocalNameFunction(funcName)) {
          if (nb != NameBehavior.NAMESPACE) {
            return argIsSelfNode ? Automaton.makeString(name.getName())
                : panicPatch;
          }
          return Automaton.makeEmptyString();
          // might still be bugs around here...
        } else if (XPathConstants.isNamespaceURIFunction(funcName)) {
          return argIsSelfNode ? Automaton.makeString(name.getNamespaceURI())
              : panicPatch;
        } else if (XPathConstants.isQNameFunction(funcName)) {
          if (!argIsSelfNode)
            return panicPatch;
          switch (nb) {
          case LOCALNAME:
            return Automaton.makeString(name.getName());
          case NAMESPACE:
            return Automaton.makeString(name.getNamespaceURI());
          default:
            return Automaton.makeString(name.getQualifiedName());
          }
        }
      } else {
        if (XPathConstants.isLocalNameFunction(funcName)) {
          return argIsSelfNode ? Automaton.makeEmptyString() : panicPatch;
        } else if (XPathConstants.isNamespaceURIFunction(funcName)) {
          return argIsSelfNode ? Automaton.makeEmptyString() : panicPatch;
        } else if (XPathConstants.isQNameFunction(funcName)) {
          return argIsSelfNode ? Automaton.makeEmptyString() : panicPatch;
        }
      }

      // try cook the concat function...
      if (fce.getQName().getName().equals("concat")) {
        Automaton result = Automaton.makeEmptyString();
        for (int i = 0; i < fce.arity(); i++) {
          XPathExpr arg = fce.getArgument(i);
          Automaton part = cook(clazz, arg, contextType, nb,
              noNamespaceBehavior, panicPatch);
          result = result.concatenate(part);
        }
        return result;
      }

      if (fce.getQName().getName().equals("position")) {
        return Automaton.makeInterval(1, Integer.MAX_VALUE, 0);
      }

      if (fce.getQName().getName().equals("last")) {
        return Automaton.makeInterval(1, Integer.MAX_VALUE, 0);
      }
    }

    // ARGH the end of our imagination has come! Return an any-string spewing
    // monster
    return panicPatch;
  }

  @Override
public String toString() {
    return "???";
  }
}

class CompositeXSLTInstruction extends XSLTInstruction {
  List<XSLTInstruction> subInstructions = new LinkedList<XSLTInstruction>();

  CompositeXSLTInstruction(StylesheetModule originalModule,
      String simplifiedId, String originalId) {
    super(originalModule, simplifiedId, originalId);
  }

  @Override
void addSubInstruction(XSLTInstruction subInstruction)
      throws XSLToolsMalformedCoreXSLException {
    subInstructions.add(subInstruction);
  }

  @Override
  Node createSGSubgraph(SingleTypeXMLClass clazz, ContextMode mode,
      DeclaredNodeType contextType, SGFragment sgFragment,
      boolean allowInterleave) throws XSLToolsException {
    List<Integer> idxx = new LinkedList<Integer>();
    int i = 0;
    for (XSLTInstruction sub : subInstructions) {
      // if there is one subinst, then we do not make a sequence, and allow
      // interleave if we are allowed ourselves.
      idxx.add(i = sub.createSGSubgraph(clazz, mode, contextType, sgFragment,
          allowInterleave && subInstructions.size() == 1).getIndex());
    }
    if (idxx.size() == 1) {
      return sgFragment.getNodeAt(i);
    }
    return sgFragment.createSequenceNode(idxx, new Origin(
        "sequence-constructor", 0, 0));
  }

  @Override
public void diagnostics(Branch parent, DocumentFactory fac,
      Set<Object> configuration) {
    if (DiagnosticsConfiguration
        .outputAllInstructionsInControlFlowGraph(configuration)) {
      Element me = fac.createElement(Util
          .capitalizedStringToHyphenString(getClass()));
      parent.add(me);
      for (XSLTInstruction sub : subInstructions) {
        sub.diagnostics(me, fac, configuration);
      }
      moreDiagnostics(me, fac);
    } else {
      for (XSLTInstruction sub : subInstructions) {
        sub.diagnostics(parent, fac, configuration);
      }
    }
  }
}

class SortInst extends XSLTInstruction {
  SortInst(StylesheetModule originalModule, String simplifiedId,
      String originalId) {
    super(originalModule, simplifiedId, originalId);
  }
}

class OtherwiseInst extends CompositeXSLTInstruction {
  ChooseInst myParent;

  OtherwiseInst(StylesheetModule originalModule, ChooseInst myParent,
      String simplifiedId, String originalId) {
    super(originalModule, simplifiedId, originalId);
    this.myParent = myParent;
  }

  /*
   * What is the chance of the context type passing the test of this inst?
   * 100%, because there is no test.
   */
  CardinalMatch transform(SingleTypeXMLClass clazz, DeclaredNodeType candidate)
      throws XSLToolsSchemaException {
    return CardinalMatch.ALWAYS_ARG_TYPE;
  }

  /*
   * Make a redeclaration, where case recognized. Delegates to containing choose instruction.
   */
  DeclaredNodeType transformChoose(SingleTypeXMLClass clazz,
      DeclaredNodeType candidate) throws XSLToolsSchemaException {
    return myParent.flowReaches(clazz, candidate, this);
  }
  
  @Override
  public String toString() {
	  return "override";
  }
}

class WhenInst extends OtherwiseInst {
  XPathExpr test;

  WhenInst(StylesheetModule originalModule, Element globle,
      ChooseInst myParent, String simplifiedId, String originalId)
      throws XSLToolsXPathParseException {
    super(originalModule, myParent, simplifiedId, originalId);
    String tests = globle.attributeValue(XSLConstants.ATTR_TEST_QNAME);
    if (tests != null)
      this.test = originalModule.getCachedXPathExp(XSLConstants.ATTR_TEST,
          tests, globle, globle);
    else
      System.err.println("Wattu? Choose uden test @ " + originalId);
  }

  /*
   * What is the chance of the context type passing the test of this inst?
   * Depends on the test: Try all redeclaration prototypes until one seems to get the idea, 
   * if never, just return the "use the arg type" thing.
   */
  @Override
  CardinalMatch transform(SingleTypeXMLClass clazz, DeclaredNodeType candidate)
      throws XSLToolsSchemaException {
    Iterator<DynamicRedeclaration> it = DynamicRedeclaration.prototypes.iterator();
    CardinalMatch sniff = CardinalMatch.MAYBE_ARG_TYPE;
    if (it.hasNext())
      do {
        DynamicRedeclaration deco = it.next();
        sniff = deco.transform(clazz, candidate, test);
      } while (it.hasNext() && sniff.cannotDetermine());
    return sniff;
  }

  @Override
Node createSGSubgraph(SingleTypeXMLClass clazz, ContextMode mode,
      DeclaredNodeType contextType, SGFragment sgFragment,
      boolean allowInterleave) throws XSLToolsException {

    return super.createSGSubgraph(clazz, mode, contextType, sgFragment,
        allowInterleave);
  }

  @Override
  protected void moreDiagnostics(Element me, DocumentFactory fac) {
    me.addAttribute("test", test.toString());
  }
  
  @Override
  public String toString() {
	  return "when test=\"" + test.toString() + "\"";
  }
}

class WithParamInst extends XSLTInstruction {
  QName paramName;

  public WithParamInst(StylesheetModule originalModule, Element e,
      String simplifiedId, String originalId)
      throws XSLToolsXPathUnresolvedNamespaceException {
    super(originalModule, simplifiedId, originalId);
    String name = e.attributeValue(XSLConstants.ATTR_NAME_QNAME);
    paramName = ElementNamespaceExpander.qNameForXSLAttributeValue(name, e,
        NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
  }

  @Override
void addSubInstruction(XSLTInstruction subInstruction) {
    // throw new XSLToolsMalformedCoreXSLException("This kind of instruction ("
    // + getClass().getSimpleName() + ") does not support sub-instructions");
    System.err.println(getClass().getSimpleName() + " ignored subinstruction: "
        + subInstruction);
  }
}

class ValueOfInst extends XSLTInstruction {
  private final dongfang.xsltools.xpath2.XPathExpr select;

  /*
   * ValueOfInst(String simplifiedElementId, String originalElementId) {
   * super(simplifiedElementId, originalElementId); }
   */
  public ValueOfInst(StylesheetModule originalModule,
      String simplifiedElementId, String originalElementId,
      dongfang.xsltools.xpath2.XPathExpr exp) {
    super(originalModule, simplifiedElementId, originalElementId);
    this.select = exp;
  }

  @Override
  Node createSGSubgraph(SingleTypeXMLClass clazz, ContextMode mode,
      DeclaredNodeType contextType, SGFragment sgFragment,
      boolean allowInterleave) throws XSLToolsUnhandledNodeTestException,
      XSLToolsSchemaException {

    Automaton a = cook(clazz, select, contextType, NameBehavior.QNAME,
        NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE, Automata
            .get("string"));

    Node n = sgFragment.createTextNode(a, ParseLocationUtil.getOrigin(
        originalModule, originalElementIds[StylesheetModule.ORIGINAL],
        getClass().getSimpleName()));
    return n;
  }

  @Override
protected void moreDiagnostics(Element me, DocumentFactory fac) {
    me.addAttribute("select", select == null ? "null" : select.toString());
  }

  @Override
public String toString() {
    return "value-of exp=\"" + select + "\"";
  }
}

class ChooseInst extends CompositeXSLTInstruction {
  public ChooseInst(StylesheetModule originalModule, Element guffer,
      String simplifiedElementId, String originalElementId) {
    super(originalModule, simplifiedElementId, originalElementId);

    // we want to include the following brances:
    // Everything after an acceptor is not included
    // Rejectors are not included
    // Everything else is included (acceptors not after an acceptor, maybes not
    // after an acceptor,
    // no-tests not after an acceptor)
    
  }

  @Override
  Node createSGSubgraph(SingleTypeXMLClass clazz, ContextMode mode,
      DeclaredNodeType contextType, SGFragment sgFragment,
      boolean allowInterleave) throws XSLToolsException {
    List<Integer> idxx = new LinkedList<Integer>();

    List<OtherwiseInst> include = new LinkedList<OtherwiseInst>();
    List<DeclaredNodeType> typesToUse = new LinkedList<DeclaredNodeType>();

    boolean handled = false;

    Iterator<XSLTInstruction> subs = subInstructions.iterator();
    while (subs.hasNext()) {
      OtherwiseInst sub = (OtherwiseInst) subs.next();

      if (!subs.hasNext())
        // case Otherwise for sure for sure
        break;

      CardinalMatch schnapp = sub.transform(clazz, contextType);
      if (!schnapp.argTypeNeverPasses()) {// if null, rejected; then this branch
        // is not run.
        include.add(sub);
        if (schnapp.isSplit()) {
          typesToUse.add(schnapp.getPassAlternative());
          contextType = schnapp.getFailAlternative();
        } else {
          typesToUse.add(contextType);
        }
        if (schnapp.argTypeAlwaysPasses()) {
          handled = true;
          break;
        }
      }
    }

    Iterator<OtherwiseInst> insts = include.iterator();
    Iterator<DeclaredNodeType> typesToUseI = typesToUse.iterator();

    while (insts.hasNext()) {
      OtherwiseInst inst = insts.next();
      DeclaredNodeType type = typesToUseI.next();
      idxx.add(inst.createSGSubgraph(clazz, mode, type, sgFragment, false)
          .getIndex());
    }

    if (!handled) {
      XSLTInstruction otherwiseSmatso = subInstructions.get(subInstructions
          .size() - 1);
      idxx.add(otherwiseSmatso.createSGSubgraph(clazz, mode, contextType,
          sgFragment, false).getIndex());
    }

    Node n = sgFragment.createChoiceNode(idxx, ParseLocationUtil
        .getOrigin(originalModule,
            originalElementIds[StylesheetModule.ORIGINAL], "choose"));
    return n;
  }

  DeclaredNodeType flowReaches(SingleTypeXMLClass clazz,
      DeclaredNodeType contextType, OtherwiseInst child)
      throws XSLToolsSchemaException {
    Iterator<XSLTInstruction> subs = subInstructions.iterator();
    while (subs.hasNext()) {
      OtherwiseInst sub = (OtherwiseInst) subs.next();
      boolean thisIsIt = sub == child;
      CardinalMatch schnapp = sub.transform(clazz, contextType);
      if (schnapp.argTypeAlwaysPasses()) {
        if (thisIsIt) {
          // all flow left gets gobbled up by the child we are looking after
          return contextType;
        // all flow left gets gobbled up by something before the child we
        // are looking after; we can safely say it gets noting.
        } else {
          return null;
        }
      }
      else if (schnapp.isSplit()) {
        if (thisIsIt)
          return schnapp.getPassAlternative();
        else contextType = schnapp.getFailAlternative();
      }
      else if (schnapp.argTypeNeverPasses()) {
        if (thisIsIt) {
          return null;
        } else {
          // do nothing.
        }
      } else {
        if (thisIsIt) {
          return null;
        }
      }
    }
    throw new AssertionError("flowReached iteration reached end of list before exiting. Should not happen.");
  }

  @Override
void addSubInstruction(XSLTInstruction subInstruction)
      throws XSLToolsMalformedCoreXSLException {
    if (subInstruction instanceof WhenInst
        || subInstruction instanceof OtherwiseInst)
      super.addSubInstruction(subInstruction);
    else
      throw new XSLToolsMalformedCoreXSLException(
          "The choose instruction can only contain when and otherwise instructions ("
              + subInstruction.getClass() + ")");
  }

  @Override
public void diagnostics(Branch parent, DocumentFactory fac,
      Set<Object> configuration) {
    if (DiagnosticsConfiguration
        .outputAllInstructionsInControlFlowGraph(configuration)) {
      Element me = fac.createElement(Util
          .capitalizedStringToHyphenString(getClass()));
      parent.add(me);
      Dom4jUtil.collectionDiagnostics(me, subInstructions,
          "whens-and-otherwise", fac, configuration);
    } else {
      for (XSLTInstruction sub : subInstructions) {
        sub.diagnostics(parent, fac, configuration);
      }
    }
  }

  @Override
public String toString() {
    return "choose";
  }
}

class AttributeInst extends CompositeXSLTInstruction {

  final String nameAVT;

  final String namespaceAVT;

  final Element originalConstructorElement;

  Automaton cachedConstantNameAutomaton = null;

  AttributeInst(StylesheetModule originalModule, Element e,
      String simplifiedElementId, String originalElementId) {
    super(originalModule, simplifiedElementId, originalElementId);
    this.originalConstructorElement = e;
    this.nameAVT = e.attributeValue(XSLConstants.ATTR_NAME_QNAME);
    this.namespaceAVT = e.attributeValue(XSLConstants.ATTR_NAMESPACE_QNAME);
  }

  /*
   * void handleNameStinker() { { {
   * 
   * String nameValTemplate = e.attribute(XSLConstants.ATTR_NAME).getValue(); //
   * TO DO: Exception instead assert (!nameValTemplate.equals("{" +
   * XPathConstants.FUNC_UNKNOWN_STRING_QNAME.getQualifiedName() + "()}")) :
   * "Any element and attribute not yet implemented"; // XSL element
   * constructors would already have been converted to the // element they
   * construct, so XSL NS parent means that the attribute // is dangling if
   * (((Element) e.getParent()).getNamespaceURI().equals(
   * XSLConstants.NAMESPACE_URI)) { // Report error: logger .warning("Cannot yet
   * handle attribute instructions at the top level of a rule: " + e); // Remove
   * instruction: // cesspool.... ... iter.remove(); // Stop recursion into this
   * element: continue; } else { if
   * (XPathConstants.isNameFunction(nameValTemplate)) { if (contextNode
   * instanceof ElementNT) { // nameValTemplate = ((ElementNT)
   * contextNode).name; nameValTemplate = ((ElementNT) contextNode).getName()
   * .getQualifiedName(); } else if (contextNode instanceof AttributeNT) { //
   * nameValTemplate = ((AttributeNT) contextNode).name; nameValTemplate =
   * ((AttributeNT) contextNode).getName() .getQualifiedName(); } else if
   * (contextNode instanceof RootNT || contextNode instanceof CommentNT ||
   * contextNode instanceof TextNT) { // assert(false) : "" + contextNode + "
   * has no expanded name! // The name() test results in empty string, which is
   * not a // QName"; cesspool .reportError( module, e,
   * ParseLocation.IN_START_TAG, "A name()/local-name() function in an attribute
   * constructor appeared where the context node has no name"); } else throw new
   * AssertionError("Unhandled name() test on context: " + contextNode); // Set
   * input namespace in case of {name()} name: if
   * (XPathConstants.isQNameFunction(nameValTemplate)) { if
   * (e.attribute(XSLConstants.ATTR_NAMESPACE) == null) { if (contextNode
   * instanceof ElementNT) { ElementNT ent = (ElementNT) contextNode;
   * e.addAttribute(XSLConstants.ATTR_NAMESPACE, ent.getName()
   * .getNamespaceURI()); } if (contextNode instanceof AttributeNT) {
   * AttributeNT ant = (AttributeNT) contextNode;
   * e.addAttribute(XSLConstants.ATTR_NAMESPACE, ant.getName()
   * .getNamespaceURI()); } } }
   */
  /*
   * It has one or more xsl instructions in body ... Could well be an
   * apply-templates among 'em. Technique: Make a sequence node, plug in node /
   * fragment of each instruction.
   * 
   * Handling of name is a separate matter...
   */

  Node createNode(SGFragment fragment, Automaton nameAutomaton, Node content) {
    return fragment.createAttributeNode(nameAutomaton, content.getIndex(),
        ParseLocationUtil.getOrigin(originalModule,
            originalElementIds[StylesheetModule.ORIGINAL], getClass()
                .getSimpleName()));
  }

  String getNoPrefixBehaviour() {
    return NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE;
  }

  static XPathExpr parseNameAVT(String avt) throws XSLToolsXPathParseException {
    List<XPathExpr> result = new LinkedList<XPathExpr>();
    parseNameAVT(avt, result);
    if (result.isEmpty())
      return null;
    XPathFunctionCallExpr concat = new XPathFunctionCallExpr(QName
        .get("concat"));
    for (XPathExpr expr : result) {
      concat.addArgument(expr);
    }
    concat.addEmptyPredicateList();
    return concat;
  }

  static void parseNameAVT(String avt, List<XPathExpr> result)
      throws XSLToolsXPathParseException {
    // ....{}..{{...{}..}}...
    if ("".equals(avt))
      return;

    int left = avt.indexOf('{');
    int right = avt.indexOf('}');

    if (left != -1 && right != -1) {

      if (left > right) {
        // this should really only be possible in the situation ...}}...{{...,
        // so we treat it as such
        if (avt.length() < left + 2 || avt.charAt(left + 1) != '{'
            || avt.charAt(right + 1) != '}') // ..{
          throw new XSLToolsXPathParseException(
              "Bad AVT: ..}...{.. was not in the form ..}}..{{..");
        String lit = "'" + avt.substring(0, right)
            + avt.substring(right + 1, left + 1) + "'";
        XPathExpr xlit = XPathParser.parse(lit);
        result.add(xlit);
        parseNameAVT(avt.substring(left + 2), result);
      }

      else if (left < right) {
        // well then right > left, we are in a ...{...}...{..}... or a
        // {...}...{..}... situation
        if (left == 0) {
          if (avt.charAt(left + 1) == '{') {
            String lit = "'{'";
            XPathExpr xlit = XPathParser.parse(lit);
            result.add(xlit);
            parseNameAVT(avt.substring(2), result);
          }
          // ok it's XPath all right .. ignore that right may be }}, nobody
          // should do within XPath.
          String exp = avt.substring(1, right);
          XPathExpr xexp = XPathParser.parse(exp);
          result.add(xexp);
          parseNameAVT(avt.substring(right + 1), result); // kill the }
        } else { // there is a literal before the XPath
          String lit = "'" + avt.substring(0, left) + "'";
          XPathExpr xlit = XPathParser.parse(lit);
          result.add(xlit);
          parseNameAVT(avt.substring(left), result);
        }
      }
    }
    // ok, one or both is minus one...
    else if (left == right) {
      // String lit = "'" + avt + "'";
      // XPathExpr xlit = XPathParser.parse(lit);
      // result.add(xlit);
    } else if (left == -1) {
      if (avt.length() < right + 2 || avt.charAt(right + 1) != '}')
        throw new XSLToolsXPathParseException(
            "Bad AVT: Only a } with no accompanying {, and it's not a }}");
      String lit = "'" + avt.substring(0, right + 1) + "'";
      XPathExpr xlit = XPathParser.parse(lit);
      result.add(xlit);
      parseNameAVT(avt.substring(right + 2), result);
    } else {
      if (avt.length() < left + 2 || avt.charAt(left + 1) != '{')
        throw new XSLToolsXPathParseException(
            "Bad AVT: Only a { and no accompanying }, and it's not a {{");
      String lit = "'" + avt.substring(0, left + 1) + "'";
      XPathExpr xlit = XPathParser.parse(lit);
      result.add(xlit);
      parseNameAVT(avt.substring(left + 2), result);
    }
  }

  /*
   * Complex Bastard!!! Do not try to understand. It will map like this: <foo/>
   * --> default-ns-binding:foo <input:bar/> --> binding-of-input-prefix:bar
   * <xsl:element name="foo" namespace=""/> --> :foo <xsl:element
   * name="{name()}" namespace="{namespace-uri()}"/> -->
   * namespace-uri-of-context:local-name-of-context <xsl:element
   * name="{local-name()}" namespace="{namespace-uri()}"/> -->
   * namespace-uri-of-context:local-name-of-context <xsl:element
   * name="{name()}"/> --> namespace-uri-of-context:local-name-of-context
   * 
   * If the context node is needed in the computation and it is not a named node
   * type (element / attribute), then an ANYSTRING automaton is returned.
   * 
   * If an unbound prefix is used, an ANYSTRING automaton is returned (throw
   * exception instead!)
   */
  Automaton getQNameAutomaton(SingleTypeXMLClass clazz,
      DeclaredNodeType contextType) throws XSLToolsException {
    if (cachedConstantNameAutomaton != null)
      return cachedConstantNameAutomaton;

    Automaton localNameAuto;

    // Parse text in {}s, and everything in between is inserted as string
    // literals. If no {} appear just return null
    XPathExpr boble = parseNameAVT(nameAVT);

    if (boble != null) {
      localNameAuto = cook(clazz, boble, contextType, NameBehavior.LOCALNAME,
          getNoPrefixBehaviour(), Automata.get("NCName2"));
    } else {
      QName qName = ElementNamespaceExpander.qNameForXSLAttributeValue(nameAVT,
          originalConstructorElement, getNoPrefixBehaviour());
      String localName = qName.getName();
      localNameAuto = Automaton.makeString(localName);
    }

    Automaton namespaceURIAuto;

    if (namespaceAVT != null) {
      // Parse text in {}s, and everything in between is inserted as string
      // literals. If no {} appear just return null
      boble = parseNameAVT(namespaceAVT);

      if (boble != null) {
        namespaceURIAuto = cook(clazz, boble, contextType,
            NameBehavior.LOCALNAME, getNoPrefixBehaviour(), Automata
                .get("NCName2"));
      } else {
        // we STRONGLY assumer there is no :
        String localName = namespaceAVT;
        namespaceURIAuto = Automaton.makeString(localName);
      }
    } else {
      if (boble != null) {
        namespaceURIAuto = cook(clazz, boble, contextType,
            NameBehavior.NAMESPACE, getNoPrefixBehaviour(), Automata.get("URI"));
      } else {
        QName qName = ElementNamespaceExpander.qNameForXSLAttributeValue(
            nameAVT, originalConstructorElement, getNoPrefixBehaviour());
        String localName = qName.getNamespaceURI();
        namespaceURIAuto = Automaton.makeString(localName);
      }
    }

    Automaton alsoEmpty = Automaton.makeEmpty();
    if (namespaceURIAuto.run("")) {
      alsoEmpty = Automaton.makeEmptyString();
      namespaceURIAuto = namespaceURIAuto.intersection(alsoEmpty.complement());
    }
    namespaceURIAuto = alsoEmpty.union(Automaton.makeChar('{').concatenate(
        namespaceURIAuto.concatenate(Automaton.makeChar('}'))));

    return namespaceURIAuto.concatenate(localNameAuto);
  }

  Node localCreateSGSubgraph(SingleTypeXMLClass clazz, ContextMode mode,
      DeclaredNodeType contextType, SGFragment sgFragment,
      boolean allowInterleave) throws XSLToolsException {
    List<Integer> idxx = new LinkedList<Integer>();
    for (XSLTInstruction sub : subInstructions) {
      idxx.add(sub.createSGSubgraph(clazz, mode, contextType, sgFragment,
          allowInterleave).getIndex());
    }
    Node content;
    if (idxx.size() == 1) {
      content = sgFragment.getNodeAt(idxx.get(0));
    } else {
      content = sgFragment.createSequenceNode(idxx, ParseLocationUtil
          .getOrigin(originalModule,
              originalElementIds[StylesheetModule.ORIGINAL], getClass()
                  .getSimpleName()));
    }

    Automaton nameAutomaton = getQNameAutomaton(clazz, contextType);

    // String tasteHer = nameAutomaton.getShortestExample(true);
    // System.err.println("Cooked n element with name " + tasteHer);

    Node result = createNode(sgFragment, nameAutomaton, content);

    return result;
  }

  @Override
  Node createSGSubgraph(SingleTypeXMLClass clazz, ContextMode mode,
      DeclaredNodeType contextType, SGFragment sgFragment,
      boolean allowInterleave) throws XSLToolsException {
    return localCreateSGSubgraph(clazz, mode, contextType, sgFragment,
        allowInterleave && subInstructions.size() == 1);
  }

  @Override
protected void moreDiagnostics(Element me, DocumentFactory fac) {
    me.addAttribute("nameAVT", nameAVT);
    me.addAttribute("namespaceAVT", namespaceAVT);
  }

  @Override
public String toString() {
    return "attribute";
  }
}

class ElementInst extends AttributeInst {
  public ElementInst(StylesheetModule originalModule, Element e,
      String simplifiedElementId, String originalElementId) {
    // Initialize:
    super(originalModule, e, simplifiedElementId, originalElementId);
  }

  @Override
String getNoPrefixBehaviour() {
    return NamespaceExpander.BIND_PREFIXLESS_TO_DEFAULT_NAMESPACE;
  }

  @Override
Node createNode(SGFragment fragment, Automaton nameAutomaton, Node content) {
    return fragment.createElementNode(nameAutomaton, content.getIndex(),
        ParseLocationUtil.getOrigin(originalModule,
            originalElementIds[StylesheetModule.ORIGINAL], getClass()
                .getSimpleName()));
  }

  @Override
  Node createSGSubgraph(SingleTypeXMLClass clazz, ContextMode mode,
      DeclaredNodeType contextType, SGFragment sgFragment,
      boolean allowInterleave) throws XSLToolsException {
    // interleavers are unconditionally true here, and only ever here...
    return localCreateSGSubgraph(clazz, mode, contextType, sgFragment, true);
  }

  @Override
public String toString() {
    return "element";
  }
}

class CopyInst extends CompositeXSLTInstruction {

  CopyInst(StylesheetModule originalModule, String simplifiedId,
      String originalId) {
    super(originalModule, simplifiedId, originalId);
  }

  @Override
  Node createSGSubgraph(SingleTypeXMLClass clazz, ContextMode mode,
      DeclaredNodeType contextType, SGFragment sgFragment,
      boolean allowInterleave) throws XSLToolsException {

    Set<? extends DeclaredNodeType> types = // clazz.possibleTargetNodes(expr,
    // contextType);
    Collections.singleton(contextType);

    List<Integer> gedeMums = new LinkedList<Integer>();

    Origin origin = ParseLocationUtil.getOrigin(originalModule,
        originalElementIds[StylesheetModule.ORIGINAL], getClass()
            .getSimpleName());

    for (DeclaredNodeType t : types) {

      if (t instanceof ElementUse) {
        ElementUse ed = (ElementUse) t;
        Node content = super.createSGSubgraph(clazz, mode, t, sgFragment,
            allowInterleave);
        gedeMums.add(sgFragment.createElementNode(ed.getClarkNameAutomaton(),
            content.getIndex(), origin).getIndex());
      } else

      if (t instanceof AttributeUse) {
        AttributeUse ed = (AttributeUse) t;
        Node content = sgFragment.createTextNode(ed.getValueOfAutomaton(clazz),
            origin);
        Node more = super.createSGSubgraph(clazz, mode, t, sgFragment,
            allowInterleave);
        List<Integer> allList = new LinkedList<Integer>();
        allList.add(content.getIndex());
        allList.add(more.getIndex());
        Node all = sgFragment.createSequenceNode(allList, origin);
        gedeMums.add(sgFragment.createAttributeNode(ed.getClarkNameAutomaton(),
            all.getIndex(), origin).getIndex());
      } else if (t instanceof TextNT) {
        Automaton aut = ((TextNT) t).getContentAutomaton();
        gedeMums.add(sgFragment.createTextNode(aut, origin).getIndex());
      } else if (t == RootNT.instance) {
        Node more = super.createSGSubgraph(clazz, mode, t, sgFragment,
            allowInterleave);
        gedeMums.add(more.getIndex());
      }
    }

    if (gedeMums.size() == 1)
      return sgFragment.getNodeAt(gedeMums.get(0));

    if (gedeMums.size() != 0)
      return sgFragment.createChoiceNode(gedeMums, origin);

    // we ignore comment and PI, and namespace...
    List<Integer> content = Collections.emptyList();
    return sgFragment.createSequenceNode(content, origin);
  }
}

/**
 * Describes a <xsl:copy-of select="$p"/> instruction, where p is a parameter.
 * These are the only copy-of instructions left after simplification.
 */
class CopyOfInst extends XSLTInstruction {
  // public String parameterName;

  public CopyOfInst(StylesheetModule originalModule,
      String simplifiedElementId, String originalElementId) {
    // Initialize:
    super(originalModule, simplifiedElementId, originalElementId);
  }

  @Override
  Node createSGSubgraph(SingleTypeXMLClass clazz, ContextMode mode,
      DeclaredNodeType contextType, SGFragment sgFragment,
      boolean allowInterleave) {
    return null;
  }

  @Override
public String toString() {
    return "copy-of";
  }
}
