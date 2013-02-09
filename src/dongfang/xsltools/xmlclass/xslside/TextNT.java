package dongfang.xsltools.xmlclass.xslside;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Automata;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.ChoiceNode;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.ControlFlowConfiguration;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.Declaration;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xpath2.XPathAxisStep;

/**
 * TODO! For string analysis, consider to make this parameterized!!!
 * 
 * @author dongfang
 */
public class TextNT extends AbstractNodeType implements DeclaredNodeType,
    UndeclaredNodeType, Declaration {

  /*
   * Simple element typed return a TextNT colored with their declared text type
   * ... hehehe
   */
  private Automaton contentAutomaton;

  private Origin declarationOrigin;
  
  /*
   * This instance is called the chameleon, because it will compare true by
   * equals() with any color TextNT, and any color TextNT will compare true by
   * equals() with it.
   */
  public static final TextNT chameleonInstance = new TextNT() {
    @Override
	public boolean equals(Object o) {
      return o instanceof TextNT;
    }
  };

  public TextNT() {
    this.contentAutomaton = Automata.get("string");
    this.declarationOrigin = new Origin("[pcdata]",0,0);
  }

  public TextNT(Automaton content, Origin declarationOrigin) {
    if (!ControlFlowConfiguration.current.useColoredContextTypes())
      throw new AssertionError(
          "Somebody instantiated a colored text node, but the current configuration does not use colors");
    this.contentAutomaton = content;
    this.declarationOrigin = declarationOrigin;
  }

  //@Override
  public void runParentAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    result.addAll(clazz.getPCDataParents());
  }

  @Override
  public void runReverseChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    runParentAxis(clazz, result);
  }

  public Node constructParentFM(
      SGFragment fraggle, SingleTypeXMLClass clazz, Set<DeclaredNodeType> weCare) {
    
    Set<? extends ElementUse> parents = clazz.getPCDataParents();
    
    List<Integer> contents = new LinkedList<Integer>();

    for (DeclaredNodeType type2 : weCare) {
      if (parents.contains(type2.getOriginalDeclaration())) {
        contents.add(fraggle.createPlaceholder(type2).getIndex());
      }
    }
    /*
    for (ElementUse parent : parents) {
      if (typeSet.contains(parent))
        contents.add(fraggle.createPlaceholder(parent).getIndex());
    }
    */
    if (contents.size() == 0)
      return fraggle.createEpsilonNode();
    if (contents.size() == 1)
      return fraggle.getNodeAt(contents.get(0));
    ChoiceNode goose = fraggle.createChoiceNode(contents, null);
    return goose;
  }

  // @Override
  @Override
public char getCharRepresentation(CharNameResolver clazz) {
    return '_';
  }

  public boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz) {
    return s.accept(this);
  }

  @Override
public boolean equals(Object val) {
    return (val == chameleonInstance || (val instanceof TextNT && contentAutomaton
        .equals(((TextNT) val).contentAutomaton)));
  }

  @Override
public int hashCode() {
    return 1;
  }

  @Override
public String toString() {
    String ex = contentAutomaton.getShortestExample(true);
    if (ex == null)
      ex = "null";
    return "[pcdata](" + ex + ')';
  }

  public void expandLanguageBy(Automaton addition) {
    contentAutomaton = contentAutomaton.union(addition);
  }

  public void expandLanguageBy(TextNT addition) {
    contentAutomaton = contentAutomaton.union(addition.contentAutomaton);
  }

  public Automaton getContentAutomaton() {
    return contentAutomaton;
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz) {
    return getContentAutomaton();
  }

  public Automaton getATSAutomaton(SingleTypeXMLClass clazz) {
    return clazz.getTextNodeTypeAutomaton();
  }
  
  public Node constructInstantiationFM(SGFragment fraggle, int content, SingleTypeXMLClass clazz) throws XSLToolsSchemaException {
    return fraggle.createTextNode(getContentAutomaton(), getDeclarationOrigin());
  }

  public Origin getDeclarationOrigin() {
    return declarationOrigin;
  }
  
  public DeclaredNodeType getOriginalDeclaration() {
    return this;
  }
}
