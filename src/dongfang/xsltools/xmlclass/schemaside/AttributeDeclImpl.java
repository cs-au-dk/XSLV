package dongfang.xsltools.xmlclass.schemaside;

import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.misc.Origin;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.CharGenerator;
import dongfang.xsltools.xmlclass.xslside.AttributeNT;

public abstract class AttributeDeclImpl extends AttributeNT implements
    AttributeDecl {

  /*
   * Automata related ztuff
   */
  private boolean ATSautomatonTunedUp;

  private Automaton myATSPathAutomaton = Automaton.makeEmpty();
//  protected final Set<AttributeUse> myUses = new HashSet<AttributeUse>();
  protected char attributeChar;

  private State myState = new State();

  private String shortestExample;

  protected AttributeDeclImpl(QName myName) {
    super(myName);
  }

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

  public void fixupCharacterNames(SingleTypeXMLClass clazz,
      CharGenerator charGen) throws XSLToolsSchemaException {
    if (clazz.attributeQNameToCharMap.containsKey(getQName()))
      attributeChar = clazz.attributeQNameToCharMap.get(getQName());
    else {
      char ec = charGen.nextAttributeChar();
      attributeChar = ec;
      clazz.attributeQNameToCharMap.put(getQName(), ec);
    }
  }

  public void constructATSPathAutomaton(State downToHere) {
    Transition trans = new Transition(getCharRepresentation(), myState);
    downToHere.addTransition(trans);
  }

  public void snapshootATSPathAutomaton(SingleTypeXMLClass clazz) {
    myState.setAccept(true);
    Automaton schemaAutomaton = clazz.getSchemaATSPathAutomaton();
    Automaton myATSPathAutomaton = (Automaton) schemaAutomaton.clone();
    myState.setAccept(false);
    this.myATSPathAutomaton = this.myATSPathAutomaton.union(myATSPathAutomaton);
  }

  private void tuneUpATSPathAutomaton(SingleTypeXMLClass clazz) {
    if (!ATSautomatonTunedUp) {
      if (myATSPathAutomaton == null) {
        // unreachable attribute decl
        if (SingleTypeXMLClass.REMOVE_UNREACHABLE_DECLARATIONS) {
          clazz.removeAttributeDecl(this);
        } else {
          myATSPathAutomaton = new Automaton();
        }
      }

      PerformanceLogger pa = DiagnosticsConfiguration.current
          .getPerformanceLogger();

      pa.startTimer("CompleteAttributeAutomata", "InputSchema");

      if (SingleTypeXMLClass.DO_RESTORE_INVARIANT)
        myATSPathAutomaton.restoreInvariant();
      myATSPathAutomaton.setDeterministic(false);
      if (SingleTypeXMLClass.DO_REMOVE_DEAD_TRANSITIONS)
        myATSPathAutomaton.removeDeadTransitions();

      pa.stopTimer("CompleteAttributeAutomata", "InputSchema");
    }
    ATSautomatonTunedUp = true;
  }

  public char getCharRepresentation() {
    return attributeChar;
  }

  public Automaton getATSAutomaton(SingleTypeXMLClass clazz) {
    tuneUpATSPathAutomaton(clazz);
    return myATSPathAutomaton;
  }

  /*
  public void addSelfUse(AttributeUse use) {
    myUses.add(use);
    addOwnerElementQName(use.getOwnerElementQName());
  }
*/
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

  @Override
public boolean equals(Object o) {
    /*
     * Experiment: An attribute and its dropoff are not the same.
    if (o instanceof DropOffDecorator) 
      return equals(((DropOffDecorator)o).getDecorated());
    */
    
    if (!(o instanceof AttributeDecl))
      return false;

    if (o instanceof AttributeUse) {
      // then we are equal if the attribute use is a use of this
      // THAT IS: An attr declaration is equal to all its uses
      // and all its uses are equal to it..
      // Two uses of the same declaration are not always equal.
      throw new AssertionError(
          "Mixup? Attribute decl was compared with attribute use");
      // return o.equals(this);//equals(((AttributeUse)
      // o).getAttributeDeclaration());
    }

    // else, we are canonical!
    return this == o;
  }

  protected abstract void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration);

  @Override
  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement(getClass().getSimpleName());
    parent.add(me);
    me.addAttribute("name", Dom4jUtil.clarkName(getQName()));
    if (DiagnosticsConfiguration.outputCharNamesInControlFlowGraph(configuration))
      me.addAttribute("char", getCharRepresentation() + "("
          + Integer.toString(getCharRepresentation()) + ")");
    if (DiagnosticsConfiguration
        .outputAncestorStringsInControlFlowGraph(configuration))
      me.addAttribute("ancestor-string-example", (getIdentifier().toString()));
    moreDiagnostics(me, fac, configuration);
  }

  @Override
public String toString() {
    StringBuilder res = new StringBuilder();
    res.append("attribute name=");
    res.append(Dom4jUtil.clarkName(getQName()));
//    boolean needsComma = false;
//    res.append('(');
//    for (AttributeUse n : myUses) {
//      if (needsComma)
//        res.append(',');
//      else
//        needsComma = true;
//      res.append(Dom4jUtil.clarkName(n.getOwnerElementQName()));
//    }
//    res.append(')');
    return res.toString();
  }

  @Override
public String toLabelString() {
    return (Dom4jUtil.clarkName(getQName()));
  }

  public abstract Origin getOrigin();
}
