package dongfang.xsltools.xmlclass.schemaside;

import java.util.Set;

import org.dom4j.QName;

import dk.brics.automaton.State;
import dk.brics.misc.Origin;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.xslside.CharNameResolver;

public interface AttributeDecl extends AncestorLangDecl {

  QName getQName();

  // Necessary? Ok, can of course go via self uses...
  // Set<? extends ElementUse> getParentUses();

  Set<QName> getOwnerElementNames();

  //void addSelfUse(AttributeUse use);

//  Set<? extends AttributeUse> getAllUses();

  boolean typeMayDeriveFrom(QName typeName) throws XSLToolsSchemaException;

  void constructATSPathAutomaton(State myState);

  Object getIdentifier();

  /*
   * boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz) throws
   * XSLToolsSchemaException;
   */

  Origin getOrigin();
  
  char getCharRepresentation(CharNameResolver resolver);

  String toLabelString();
}
