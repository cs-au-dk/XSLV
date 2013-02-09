package dongfang.xsltools.xmlclass.schemaside;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.CharGenerator;
import dongfang.xsltools.xmlclass.xslside.CharNameResolver;
import dongfang.xsltools.xmlclass.xslside.ElementNT;

/*
 * The side towards the automata, schema .. etc
 */
public abstract class ElementDeclImpl extends ElementNT implements
    ElementDecl {

// all automaton related shit goes here.
  private boolean automatonTunedUp;
  private Automaton myATSPathAutomaton;
  protected State myState;
  protected State myCommentState;
  protected State myPIState;
  protected State myTextState;

  private char elementChar;

  private String shortestExample;

  // Set of ElementDecl objects representing possible parent elements.
  // These are Uses because ... ta daaa ... we may be the child of
  // some but not all uses of a declaration (namely, only those
  // that mention us in their contents)
  private final Set<ElementUse> parentUses = new HashSet<ElementUse>();

  // Set of AttributeDecl objects representing possible attributes.
  // protected final Map<QName, AttributeUse> attributeUses = new HashMap<QName,
  // AttributeUse>();

  @Override
public Object getIdentifier() {
    if (shortestExample == null) {
      if (myATSPathAutomaton != null) {
        shortestExample = myATSPathAutomaton.getShortestExample(true);
      }
      if (shortestExample == null) {
        shortestExample = Dom4jUtil.clarkName(getQName());
        shortestExample += shortestExample.hashCode();
      }
    }
    return new String(shortestExample);
  }

  protected ElementDeclImpl(QName name) {
    super(name);
  }

  /*
   * An XML Schema thing. Not used by others.
   */
  protected void constructATSPathAutomatonForLocalFeatures(State parentState,
      State commentParentState, State commentAcceptState, State PIParentState,
      State PIAcceptState, State textParentState, State textAcceptState) {
  }

  public void constructATSPathAutomaton(State parentState,
      State commentParentState, State commentAcceptState, State PIParentState,
      State PIAcceptState, State textParentState, State textAcceptState) {

    Transition trans;

    if (myState == null) {

      myState = new State();

      myPIState = new State();
      myCommentState = new State();
      myTextState = new State();

      if (!getCommentsPIAcceptingVariations().isEmpty()) {
        trans = new Transition(CharGenerator.getCommentChar(),
            commentAcceptState);
        myCommentState.addTransition(trans);

        trans = new Transition(CharGenerator.getPIChar(), PIAcceptState);
        myPIState.addTransition(trans);
      }

      if (!getTextAcceptingVariations().isEmpty()) {
        trans = new Transition(CharGenerator.getPCDATAChar(), textAcceptState);
        myTextState.addTransition(trans);
      }

      for (ElementDecl child : getWidenedChildElementDecls()) {
        child.constructATSPathAutomaton(myState, myCommentState,
            commentAcceptState, myPIState, PIAcceptState, myTextState,
            textAcceptState);
      }

      // det er nok at goere det med erklaeringen faktisk ... een gang.
      for (AttributeUse a : getAllVariationAttributeUses().values()) {
        a.getAttributeDeclaration().constructATSPathAutomaton(myState);
      }

      constructATSPathAutomatonForLocalFeatures(parentState,
          commentParentState, commentAcceptState, PIParentState, PIAcceptState,
          textParentState, textAcceptState);
    }

    char myChar = getCharRepresentation();
    trans = new Transition(myChar, myState);
    parentState.addTransition(trans);

    trans = new Transition(getCharRepresentation(), myCommentState);
    commentParentState.addTransition(trans);

    trans = new Transition(getCharRepresentation(), myPIState);
    PIParentState.addTransition(trans);

    trans = new Transition(getCharRepresentation(), myTextState);
    textParentState.addTransition(trans);
  }

  public void fixupCharacterNames(SingleTypeXMLClass clazz,
      CharGenerator charGen) throws XSLToolsSchemaException {
    if (clazz.elementQNameToCharMap.containsKey(getQName()))
      elementChar = clazz.elementQNameToCharMap.get(getQName());
    else {
      char ec = charGen.nextElementChar();
      elementChar = ec;
      clazz.elementQNameToCharMap.put(getQName(), ec);
    }

    // TODO: Not here !!
    for (AttributeUse ad : getAllVariationAttributeUses().values()) {
      ad.getAttributeDeclaration().fixupCharacterNames(clazz, charGen);
    }
  }

  // assume myState is in the automaton .. and that no harm is done in fooling
  // with it.
  public void snapshootATSPathAutomaton(SingleTypeXMLClass clazz) {
    if (myState == null) {
      System.err
          .println("Declaration for element "
              + Dom4jUtil.clarkName(getQName())
              + " was unreachable from the root. That could be becauce the schema is not single-type (I traverse children by unique names).");
      if (SingleTypeXMLClass.REMOVE_UNREACHABLE_DECLARATIONS) {
        clazz.removeElementDecl(this);
      } else
        myATSPathAutomaton = new Automaton();
    } else {
      // make the automaton accept exactly this type
      myState.setAccept(true);

      // keep a copy
      Automaton allElementsAutomaton = clazz.getSchemaATSPathAutomaton();
      myATSPathAutomaton = (Automaton) allElementsAutomaton.clone();

      // clean up the mess
      myState.setAccept(false);

      for (AttributeUse att : getAllVariationAttributeUses().values()) {
        att.getAttributeDeclaration().snapshootATSPathAutomaton(clazz);
      }

      // forget about state in foreign automaton
      myState = null;
    }
  }

  private void tuneATSPathAutomaton(SingleTypeXMLClass clazz) {
    if (!automatonTunedUp) {
      PerformanceLogger pa = DiagnosticsConfiguration.current
          .getPerformanceLogger();

      pa.startTimer("CompleteElementAutomata", "InputSchema");

      if (SingleTypeXMLClass.DO_RESTORE_INVARIANT)
        myATSPathAutomaton.restoreInvariant();

      myATSPathAutomaton.setDeterministic(false);

      if (SingleTypeXMLClass.DO_REMOVE_DEAD_TRANSITIONS)
        myATSPathAutomaton.removeDeadTransitions();

      pa.stopTimer("CompleteElementAutomata", "InputSchema");
    }
    automatonTunedUp = true;
  }

  public Automaton getATSAutomaton(SingleTypeXMLClass clazz) {
    if (myATSPathAutomaton == null)
      return null;
    tuneATSPathAutomaton(clazz);
    return myATSPathAutomaton;
  }

  public Set<? extends ElementUse> getParentUses() {
    return parentUses;
  }

  public void addParentUse(ElementUse parentUse) {
    parentUses.add(parentUse);
  }

  // public abstract Map<QName, ElementUse> getChildElementUseMap();
  // interface declare instead, implement here
  // public Collection<? extends AttributeUse> getAttributeUses() {
  // return attributeUses.values();
  // }
  // public AttributeUse getAttributeUse(QName name) {
  // return attributeUses.get(name);
  // }

  @Override
  public char getCharRepresentation(CharNameResolver clazz) {
    return elementChar;
  }

  public char getCharRepresentation() {
    return elementChar;
  }

  public abstract Automaton languageOfTextNodes(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException;

  // Error? Wrong direction? What's the use of checking that this element
  // has a supertype of some other type?
  public boolean typeMayDeriveTo(QName typeName, SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    throw new XSLToolsSchemaException(
        "Cannot do type based selection with other input schema kinds than XML Schema");
  }

  public boolean typeMayDeriveFrom(QName typeName)
      throws XSLToolsSchemaException {
    throw new XSLToolsSchemaException(
        "Cannot do type based selection with other input schema kinds than XML Schema");
  }

  public boolean isNillable() throws XSLToolsSchemaException {
    throw new XSLToolsSchemaException(
        "Cannot do nill property based selection with other input schema kinds than XML Schema");
  }

  /**
   * Default impl. foo all else than XML Schema, without any consideration for -
   * derived type contents - substitution groups - abstract elements
   * 
   * @param clazz
   * @param result
   * @param includeDerivedTypeContents
   * @throws XSLToolsSchemaException
   */

  @Override
public String toString() {
    // Make parents string list:

    /*
     * String parentsStr = ""; Iterator<QName> numse =
     * parents.keySet().iterator();
     * 
     * while (numse.hasNext()) { QName parentName = numse.next(); parentsStr +=
     * parentName.getQualifiedName(); if (numse.hasNext()) parentsStr += " "; }
     */
    // Make children string list:
    String result = "element name=" + Dom4jUtil.clarkName(getQName());

    try {
      String childrenStr = "";

      Iterator<? extends ElementDecl> numse = getWidenedChildElementDecls()
          .iterator();// getChildElementMap().keySet().iterator();

      while (numse.hasNext()) {
        ElementDecl d = numse.next();
        QName childName = d.getQName();
        childrenStr += Dom4jUtil.clarkName(childName);
        if (numse.hasNext())
          childrenStr += " ";
      }
      // return "<E name=\"" + getName().getQualifiedName() + "\" parents=\"[" +
      // parentsStr + "]\" children=\"["
      // + childrenStr + "]\">";
      // return "<E name=\"" + getName().getQualifiedName() + "\" children=\"["
      // +
      // childrenStr + "]\">";
      if (SingleTypeXMLClass.VERBOSE_AUTOMATA_INFO)
        result += " char="
            + getCharRepresentation()
            + " ancestor-string-example="
            + (getATSAutomaton(null) == null ? "automation-is-null"
                : getIdentifier());
    } catch (Exception ex) {
      result += ex.getMessage();
    }
    return result;
  }

  @Override
public String toLabelString() {
    return Dom4jUtil.clarkName(getQName());
  }

  @Override
  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    diagnostics(parent, fac, configuration, false);
  }

  protected void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration,
      boolean verbose) {
  }

  public Node constructInstantiationFM(SGFragment fraggle, int content, SingleTypeXMLClass clazz) throws XSLToolsSchemaException {
    return fraggle.createElementNode(getClarkNameAutomaton(), content, getOrigin());
  }
  
  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration, boolean verbose) {
    Element me = fac.createElement(getClass().getSimpleName());
    parent.add(me);
    me.addAttribute("name", Dom4jUtil.clarkName(getQName()));
    if (DiagnosticsConfiguration.outputCharNamesInControlFlowGraph(configuration))
      me.addAttribute("char", getCharRepresentation() + "("
          + Integer.toString(getCharRepresentation()) + ")");
    if (DiagnosticsConfiguration
        .outputAncestorStringsInControlFlowGraph(configuration))
      me.addAttribute("ancestor-string-example", getIdentifier().toString());
    if (verbose) {
      Element declaredAttributes = fac.createElement("DeclaredAttributes");
      me.add(declaredAttributes);
      for (AttributeUse at : getAllVariationAttributeUses().values()) {
        at.diagnostics(declaredAttributes, fac, configuration);
      }
      Element declaredChildren = fac.createElement("DeclaredChildren");
      me.add(declaredChildren);
      for (ElementDecl ch : getWidenedChildElementDecls()) {
        ch.diagnostics(declaredChildren, fac, configuration, false);
      }
      if (!getTextAcceptingVariations().isEmpty())
        me.addAttribute("textContentDeclared", "true");
    }

    moreDiagnostics(me, fac, configuration, verbose);
  }

  public abstract Origin getOrigin();

  /*
   * Schema-declared element types are canonical.
   */
  @Override
public boolean equals(Object o) {
    /*
     * Experiment: A declaration and its dropoff are not the same.
    if (o instanceof DropOffDecorator) {
      return equals(((DropOffDecorator)o).getDecorated());
    }
    */
    return this == o;
  }
}
