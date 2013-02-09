package dongfang.xsltools.xmlclass.xsd;

import java.util.List;

import dk.brics.automaton.Automaton;
import dk.brics.relaxng.Param;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;

interface XSDSimpleType extends XSDType {
  Automaton makeValueOfAutomaton(XSDSchema clazz, List<Param> facets)throws XSLToolsSchemaException;
}
