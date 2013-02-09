package dongfang.xsltools.xmlclass.xslside;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Automata;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.Declaration;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xpath2.XPathAxisStep;

public class CommentNT extends AbstractNodeType implements DeclaredNodeType,
    UndeclaredNodeType, Declaration {

  public static final CommentNT instance = new CommentNT();
  
  private static final Origin origin = new Origin("[comment]",0,0);

  private CommentNT() {
  }

  public boolean matches(XPathAxisStep step, SingleTypeXMLClass clazz) {
    return step.accept(this);
  }

  // @Override
  @Override
public char getCharRepresentation(CharNameResolver clazz) {
    return '1';
  }

  public Automaton getATSAutomaton(SingleTypeXMLClass clazz) {
    return clazz.getCommentNodeTypeAutomaton();
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
      SGFragment fraggle, 
      SingleTypeXMLClass clazz,
      Set<DeclaredNodeType> typeSet) {
    Set<? extends ElementUse> parents = clazz.getCommentPIParentElements();
    List<Integer> contents = new LinkedList<Integer>();
    if (typeSet.contains(RootNT.instance))
      contents.add(fraggle.createPlaceholder(RootNT.instance).getIndex());
    for (ElementUse parent : parents) {
      if (typeSet.contains(parent))
        contents.add(fraggle.createPlaceholder(parent).getIndex());
    }
    if (contents.size() == 0)
      return fraggle.createEpsilonNode();
    Node goose;
    if (contents.size() == 1)
       goose = fraggle.getNodeAt(contents.get(0));
    else goose = fraggle.createChoiceNode(contents, null);
    return goose;
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz) {
    // TODO: Return an automaton without ws at head and tail
    return Automata.get("string");
  }

  @Override
public boolean equals(Object val) {
    return val == this;
  }

  @Override
public int hashCode() {
    return 2;
  }

  @Override
public String toString() {
    return "[comment]";
  }
  
  public Node constructInstantiationFM(SGFragment fraggle, int content, SingleTypeXMLClass clazz) throws XSLToolsSchemaException {
    throw new XSLToolsSchemaException("Cannot instantiate a comment");
  }

  public Origin getDeclarationOrigin() {
    return origin;
  }
  
  public DeclaredNodeType getOriginalDeclaration() {
    return this;
  }
}