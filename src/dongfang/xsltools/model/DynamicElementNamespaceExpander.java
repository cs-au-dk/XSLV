package dongfang.xsltools.model;

import org.dom4j.Element;

/**
 * This subclass offers the possibility of replacing the element used for
 * resolution, at the expense of thread safety.
 * 
 * @author dongfang
 * 
 */
public class DynamicElementNamespaceExpander extends ElementNamespaceExpander {
  public DynamicElementNamespaceExpander(Element e) {
    super(e);
  }

  @Override
public void setElement(Element e) {
    this.element = e;
  }
}
