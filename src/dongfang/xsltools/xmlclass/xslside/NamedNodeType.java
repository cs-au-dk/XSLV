package dongfang.xsltools.xmlclass.xslside;

import org.dom4j.QName;

import dk.brics.automaton.Automaton;

public interface NamedNodeType {
  QName getQName();

  Automaton getClarkNameAutomaton();
}
