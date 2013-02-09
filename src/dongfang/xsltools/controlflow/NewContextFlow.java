package dongfang.xsltools.controlflow;

import java.util.Set;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;

class NewContextFlow extends NewFlow {
  final DeclaredNodeType contextType;

  NewContextFlow(DeclaredNodeType contextType, TemplateRule target,
      ContextMode contextMode) {
    super(target, contextMode);
    this.contextType = contextType;
  }

  @Override
public String toString() {
    return "contextType: " + contextType + " rule: " + target;
  }

  @Override
  public void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration) {
    contextType.diagnostics(me, fac, configuration);
  }
}