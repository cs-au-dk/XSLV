package dongfang.xsltools.controlflow;

import java.util.LinkedList;

import org.dom4j.Element;

import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.model.StylesheetModule;

public class NextMatchInst extends ApplyTemplatesInst {

  public NextMatchInst(StylesheetModule module, TemplateRule rule,
      Element coreApplyElement, LinkedList<OtherwiseInst> filters,String simplifiedElementId,
      String originalElementId) throws XSLToolsXPathException,
      XSLToolsXPathUnresolvedNamespaceException {
    super(module, rule, coreApplyElement, filters, simplifiedElementId,
        originalElementId);
  }

  /*
   * We want to approve anything that the flow source beats wrt import
   * precedence, as well as normal priority. So that's what we do.
   */
  @Override
  boolean templateInvocationCompatible(TemplateRule target) {
    return containingRule.compareTo(target) > 0;
  }

  @Override
public String toLabelString() {
    return "next-match";
  }

  @Override
TemplateInvokerMode getMode() {
    return CurrentContextMode.instance;
  }
}