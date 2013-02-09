package dongfang.xsltools.xmlclass.schemaside;

import dk.brics.automaton.Automaton;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.CharGenerator;
import dongfang.xsltools.xmlclass.xslside.CharNameResolver;

public interface AncestorLangDecl extends Declaration {
  void fixupCharacterNames(SingleTypeXMLClass clazz, CharGenerator charGen)
      throws XSLToolsSchemaException;

  void snapshootATSPathAutomaton(SingleTypeXMLClass clazz);

  Automaton getClarkNameAutomaton();

  char getCharRepresentation(CharNameResolver resolver);

  Object getIdentifier();

//  Set<? extends ElementUse> getParentUses();
}
