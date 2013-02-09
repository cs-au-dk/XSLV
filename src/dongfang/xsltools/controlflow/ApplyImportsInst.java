package dongfang.xsltools.controlflow;

import java.util.LinkedList;

import org.dom4j.Element;

import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetModule;

public class ApplyImportsInst extends ApplyTemplatesInst {

  public ApplyImportsInst(StylesheetModule module, TemplateRule rule,
      Element coreApplyElement, LinkedList<OtherwiseInst> filters, String simplifiedElementId,
      String originalElementId) throws XSLToolsXPathException,
      XSLToolsXPathUnresolvedNamespaceException {
    super(module, rule, coreApplyElement, filters, simplifiedElementId,
        originalElementId);
  }

  /*
   * We want to only consider targets that are import descendants of the source.
   * These will have level number GREATER than that of the source (lower number
   * is higher precedence!) but SMALLER OR EQUAL to the import tree upper bound
   * of the source. Default rules are permitted too.
   */
  @Override
  boolean templateInvocationCompatible(TemplateRule target) {
    int targetPrecedenceIs = target.getModuleLevelNumber();

    if (targetPrecedenceIs == Stylesheet.DEFAULT_RULES_LEVELNUMBER)
      return true;

    int targetPrecedenceMin = containingRule.getModuleLevelNumber();
    int targetPrecedenceMax = containingRule.getModuleSublevelUpperBound();

    return targetPrecedenceIs > targetPrecedenceMin
        && targetPrecedenceIs <= targetPrecedenceMax;
  }

  @Override
public String toLabelString() {
    return "apply-imports";
  }
  
  @Override
TemplateInvokerMode getMode() {
    return CurrentContextMode.instance;
  }
}
