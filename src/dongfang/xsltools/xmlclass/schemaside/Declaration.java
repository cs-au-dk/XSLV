package dongfang.xsltools.xmlclass.schemaside;

import dk.brics.automaton.Automaton;
import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;

public interface Declaration extends Diagnoseable {
  // Ancestor path automata related ztuff
  Automaton getATSAutomaton(SingleTypeXMLClass clazz);

  // xsl:value-of -- FEUER!!
  Automaton getValueOfAutomaton(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException;

  // Automaton for name(.)

  String toLabelString();

  // Set<? extends DeclaredNodeType> getAllUses();
}
