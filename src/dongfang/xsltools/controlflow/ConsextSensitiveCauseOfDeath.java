/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-23
 */
package dongfang.xsltools.controlflow;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.XMLConstants;
import dongfang.xsltools.xmlclass.xslside.NodeType;

/**
 * @author dongfang
 */
public class ConsextSensitiveCauseOfDeath extends DeadContextFlow {
  private NodeType context;

  public ConsextSensitiveCauseOfDeath(TemplateRule target, NodeType context,
      int cause, String source) {
    super(target, cause, source);
    this.context = context;
  }

  @Override
public Element describeDeathCause(DocumentFactory fac) {
    Element result = super.describeDeathCause(fac);
    Element flowtype = fac.createElement(XMLConstants.CONTEXT_TYPE_QNAME);
    flowtype.addAttribute(XMLConstants.CONTEXT_TYPE_ATTR_QNAME, context
        .toString());
    result.add(flowtype);
    return result;
  }
}
