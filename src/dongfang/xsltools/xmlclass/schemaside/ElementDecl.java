package dongfang.xsltools.xmlclass.schemaside;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.QName;

import dk.brics.automaton.State;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;

public interface ElementDecl extends AncestorLangDecl {

  QName getQName();

  /*
   * Widening get children: Traverse all subtypes, add in their declared
   * children. That is, we go up the type hierarchy, and we fan-down.
   */
  // void getWidenedChildElementDecls(Set<? super BackendElementDecl> dumper);
  Collection<? extends ElementDecl> getWidenedChildElementDecls();

  /*
   * Just substitute by SGRP mechanism. For any schema lang that does not have
   * an SGRP mechanism, element declarations just add themselves.
   */
  // void getSGRPSubstituteableElementUses(Set<? super ElementUse> dumper);
  // Set<? extends ElementUse> getSGRPSubstituteableElementUses();
  Collection<? extends ElementDecl> getSGRPSubstituteableElementDecls();

  /*
   * Return one use that throws in an attribute xsi:type=Optional([declared
   * type's name]) xsi:nil=Optional([false])
   * 
   * and a load of other uses for everything implicit: nillable from the backend
   * decl proper type subst from the type, restricted by decl proper Armored
   * cars n tanks n jeeps and rigs of every size...
   * 
   * For DTD, a singleton of the decl itself will do...
   */
  void getAllUses(Set<? super ElementUse> dumper);

  Set<? extends ElementUse> getAllUses();

  /*
   * Register an ElementUse with us (a MUST-do).
   */
  void addSelfUse(ElementUse use);

  /*
   * Return all registered uses.
   */
  // Set<? extends ElementUse> getAllExplicitAndImplicitUses();
  /*
   * Called at construction time automaton build recursion. Maybe more than
   * once.
   */
  void constructATSPathAutomaton(State myState, State myCommentState,
      State commentAcceptState, State myPIState, State PIAcceptState,
      State myTextState, State textAcceptState);

  /*
   * True for abstract-declared XSD elements, false for everything else.
   */
  boolean isAbstract();

  /*
   * Get the subset of getSelfUses (hopefully!) that accept comments and PIs.
   * That will be all of them, except for an EMPTY dtd decl.
   */
  Set<ElementUse> getCommentsPIAcceptingVariations();

  /*
   * Get the subset of getSelfUses (hopefully!) that accept text. How was that
   * thing with XSD derivation and mixed...?
   */
  Set<ElementUse> getTextAcceptingVariations();

  // This is to be used only for automaton, attribute map xtruction
  // for flow analysis --- NOT SG models. Reason: May clash declarations
  // of xsi:type
  Map<QName, AttributeUse> getAllVariationAttributeUses();

  Set<? extends ElementUse> getParentUses();
  
  Node constructInstantiationFM(SGFragment fraggle, int content, SingleTypeXMLClass clazz) 
    throws XSLToolsSchemaException;

  void processContentModel(SingleTypeXMLClass clazz,
      Map<QName, ElementDecl> tempNameToElementMap)
      throws XSLToolsSchemaException;

  void addParentUse(ElementUse parentUse);

  Origin getOrigin();
  // void fixupParentReferences();

  void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration, boolean recurse);
}
