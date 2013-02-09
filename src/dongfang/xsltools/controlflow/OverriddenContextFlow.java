/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-21
 */
package dongfang.xsltools.controlflow;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.XMLConstants;

/**
 * @author dongfang
 */
public class OverriddenContextFlow extends DeadContextFlow {
  TemplateRule newRule;

  public OverriddenContextFlow(TemplateRule newRule, TemplateRule oldRule,
      int cause, String source) {
    super(oldRule, cause, source);
    this.newRule = newRule;
  }

  /*
   * (non-Javadoc)
   * 
   * @see dongfang.xsltools.diagnostics.CauseOfDeath#describeDeathCause(org.dom4j.DocumentFactory)
   */
  @Override
public Element describeDeathCause(DocumentFactory fac) {
    Element result = super.describeDeathCause(fac);
    Element robber = fac.createElement(XMLConstants.ROBBER_QNAME);
    result.add(robber);
    robber.add(newRule.createXMLReference(fac));
    return result;
  }

  @Override
public boolean equals(Object o) {
    if (o instanceof DeadContextFlow) {
      if (o instanceof OverriddenContextFlow) {
        // //OverrideCauseOfDeath r = (OverrideCauseOfDeath) o;
        return newRule.equals(newRule) && super.equals(o);
      }
      return super.equals(o);
    }
    return false;
  }

  @Override
  void moreDiagnostics(Element me, DocumentFactory fac) {
    me.addAttribute("robber", newRule.toLabelString());
  }

  @Override
public int hashCode() {
    return newRule.hashCode() + super.hashCode();
  }
}
