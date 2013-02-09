package dongfang.xsltools.xmlclass.xslside;

import java.util.Collection;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xpath2.XPathAxisStep;

public interface DeclaredNodeType extends NodeType {

  boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException;

  /*
   * Ancestor language automaton
   */
  Automaton getATSAutomaton(SingleTypeXMLClass clazz);

  /**
   * Get an automaton representing the possible outcome of an value-of, with the
   * node as argument. Attributes will want to return their type, as will
   * simple-typed elements. Text nodes will want to return their text (if they
   * have a clue)(wait...they don't. They are not declared cowboy). Complex type
   * elements may want to try to approximate, or just give up and return the any
   * string automaton.
   * 
   * @return
   */
  Automaton getValueOfAutomaton(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException;

  // ide: Automaton getTestAutomaton...

  /*
   * Return an object that uniquely (by Equals) references this node type, but
   * is still useable as a weak key (candidate: a good enough toString()
   * result...)
   */
  Object getIdentifier();

  void runChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result)
      throws XSLToolsSchemaException;

  void runParentAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result);

  void runAttributeAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result);

  void runReverseChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result);

  void runReverseAttributeAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result);

 /*
  * Return the original declaration of a dynamic redeclaration.
  * Implmentations should recurse thru dynamic redeclarations
  * of dynamic redeclarations.
  */ 
  DeclaredNodeType getOriginalDeclaration();
  
  Collection<DeclaredNodeType> getMyTypes(Collection<DeclaredNodeType> choices);
  
  Node constructChildFM(
      SGFragment fraggle, SingleTypeXMLClass clazz,
      boolean maxOne, 
      Set<DeclaredNodeType> typeSet, 
      boolean allowInterleave, ContentOrder order)
      throws XSLToolsSchemaException;

  Node constructAttributeFM(SGFragment fraggle, SingleTypeXMLClass clazz,
      Set<DeclaredNodeType> typeSet) throws XSLToolsSchemaException;

  Node constructParentFM(
      SGFragment fraggle, SingleTypeXMLClass clazz,
      Set<DeclaredNodeType> typeSet) throws XSLToolsSchemaException;

  Node constructAncestorFM(SGFragment fraggle, SingleTypeXMLClass clazz,
      boolean maxOne, Set<DeclaredNodeType> typeSet)
      throws XSLToolsSchemaException;

  Node constructDescendantFM(SGFragment fraggle, SingleTypeXMLClass clazz,
      boolean maxOne, Set<DeclaredNodeType> typeSet)
      throws XSLToolsSchemaException;

  Node constructSelfFM(SGFragment fraggle, DeclaredNodeType selfType)
      throws XSLToolsSchemaException;

  Node constructAncestorOrSelfFM(SGFragment fraggle, SingleTypeXMLClass clazz,
      boolean maxOne, 
      Set<DeclaredNodeType> typeSet, DeclaredNodeType selfType,
      ContentOrder order)
      throws XSLToolsSchemaException;

  Node constructDescendantOrSelfFM(
      SGFragment fraggle, SingleTypeXMLClass clazz, 
      boolean maxOne,
      Set<DeclaredNodeType> typeSet,
      DeclaredNodeType selfType, ContentOrder order)
      throws XSLToolsSchemaException;

  Node constructInstantiationFM(SGFragment fraggle, int content, SingleTypeXMLClass clazz) throws XSLToolsSchemaException;
  // char getCharRepresentation();
  // void getGuaranteedDescendants(SingleTypeXMLClass clazz, Set<? super
  // DeclaredNodeType> tanker);
  
  Origin getDeclarationOrigin();
}
