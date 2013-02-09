package dongfang.xsltools.controlflow;

import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.util.Util;

public class NewFlow implements Diagnoseable {
  final TemplateRule target;

  ContextMode contextMode;

  NewFlow(TemplateRule target, ContextMode contextMode) {
    this.target = target;
    this.contextMode = contextMode;
  }

  @Override
public String toString() {
    return "contextMode: " + contextMode + " rule: " + target;
  }

  void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration) {
  }

  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement(Util
        .capitalizedStringToHyphenString(getClass()));
    parent.add(me);
    me.addAttribute("mode", contextMode.toString());
    moreDiagnostics(me, fac, configuration);
    target.diagnostics(me, fac, configuration);
  }
}
