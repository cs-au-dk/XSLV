/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-16
 */
package dongfang.xsltools.simplification;

import java.util.Iterator;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.StylesheetModule;

/**
 * @author dongfang
 */
public class RTFParameterOrVariableBinding extends ParameterOrVariableBinding {
  // An element HOLDING the RTF (it's not the root of it)
  // Not strictly necessary to do like this, as the carrier
  // should always have exactly one child anyway.
  private Element sequenceCarrier;

  public RTFParameterOrVariableBinding(
      StylesheetModule bindingStylesheetModule, Element sequenceCarrier,
      short scope, short bindingType) {
    super(bindingStylesheetModule, scope, bindingType);
    this.sequenceCarrier = sequenceCarrier;
  }

  @Override
public short getValueType() {
    return RTF_VALUE_TYPE;
  }

  @Override
public void resolutionDiagnostics(Branch parent, DocumentFactory fac) {
    Element container = fac.createElement("content-holder");
    parent.add(container);
    container.add((Element) sequenceCarrier.clone());
  }

  @Override
public void removeAllVariableRefs(Resolver resolver,
      ResolutionSimplifierBase simplifier) throws XSLToolsException {
    if (!contentIsResolved()) {
      for (Iterator iter = sequenceCarrier.elementIterator(); iter.hasNext();) {
        Element e = (Element) iter.next();
        // Dom4jUtil.prettyPrint(e);
        e.detach();
        e = (Element) simplifier.simplify(e, getBindingStylesheetModule(),
            resolver, 100, false);
        simplifier.clearAdditionQueues(sequenceCarrier, e, 100);
      }
      setContentResolved();
    }
  }

  Element getCarrierClone() {
    return (Element) sequenceCarrier.clone();
  }
}