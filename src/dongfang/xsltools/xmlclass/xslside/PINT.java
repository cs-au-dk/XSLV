package dongfang.xsltools.xmlclass.xslside;

import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Automata;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.ControlFlowConfiguration;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.Declaration;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xpath2.XPathAxisStep;

public class PINT extends AbstractNodeType implements DeclaredNodeType,
    UndeclaredNodeType, Declaration {

  String target;

  private static final Origin origin = new Origin("[pi]",0,0);

  public PINT(String target) {
    if (target != null
        && !ControlFlowConfiguration.current.useColoredContextTypes())
      throw new AssertionError(
          "Somebody instantiated a colored PI node, but the current configuration does not use colors");
    this.target = target;
  }

  public boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz) {
    return s.accept(this);
  }

  // @Override
  @Override
public char getCharRepresentation(CharNameResolver clazz) {
    return '2';
  }

  public Automaton getATSAutomaton(SingleTypeXMLClass clazz) {
    return clazz.getPINodeTypeAutomaton();
  }

  public String getTarget() {
    return target;
  }

  // @Override
  public void runParentAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    result.add(RootNT.instance);
    result.addAll(clazz.getCommentPIParentElements());
  }

  @Override
  public void runReverseChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    runParentAxis(clazz, result);
  }

  public Node constructParentFM(
      SGFragment fraggle, SingleTypeXMLClass clazz,
      Set<DeclaredNodeType> typeSet) {
    return CommentNT.instance
        .constructParentFM(fraggle, clazz, typeSet);
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz) {
    // TODO: No ws at head and tail (norml'ze)
    return Automata.get("string");
  }

  @Override
public boolean equals(Object val) {
    if (!(val instanceof PINT))
      return false;

    PINT other = (PINT) val;

    // it's the chameleon
    if (other == chameleonInstance)
      return true;

    // provoke nullpointerexception if more than one chameleon made!
    return other.target.equals(target);
  }

  @Override
public int hashCode() {
    return 3;
  }

  @Override
public String toString() {
    if (target == null)
      return "[pi]";
    return "[pi(" + target + ")]";
  }

  public static final PINT chameleonInstance = new PINT(null) {
    @Override
	public boolean equals(Object o) {
      return o instanceof PINT;
    }
  };

  public Node constructInstantiationFM(SGFragment fraggle, int content, SingleTypeXMLClass clazz) throws XSLToolsSchemaException {
    throw new XSLToolsSchemaException("Cannot instantiate a PI");
  }
  
  public Origin getDeclarationOrigin() {
    return origin;
  }

  public DeclaredNodeType getOriginalDeclaration() {
    return this;
  }
}