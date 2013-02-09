/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsResolverAlreadyBoundException;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;

/**
 * A representation of a named xsl:template.
 * 
 * @author dongfang
 */
public class TemplateBinding extends Binding {
  private QName mode;

  /**
   * Create an instance. All xsl:attribute children of <code>element</code>
   * are saved in a map, as is the element itself, for namespace prefix
   * resolution.
   * 
   * @param attribute_set
   */
  protected TemplateBinding(StylesheetModule bindingStylesheetModule, QName mode) {
    super(bindingStylesheetModule);
    this.mode = mode;
  }

  @Override
public short getSymbolSpace() {
    return Resolver.TEMPLATE_SYMBOLSPACE;
  }

  /*
   * Resolver needed here is the resolver bound to! NOTE that the binding order
   * now is significant -- we MUST bind from the top down.
   */
  @Override
public Binding bindBehaviour(QName name, Resolver resolver)
      throws XSLToolsException {
    Binding that = resolver.resolve(name, Resolver.TEMPLATE_SYMBOLSPACE);
    if (that != null)
      throw new XSLToolsResolverAlreadyBoundException(name);
    return this;
  }

  @Override
public void removeAllVariableRefs(Resolver resolver,
      ResolutionSimplifierBase simplifier) {
  }

  @Override
public Binding resolveBehaviour(QName name, Resolver resolver) {
    return this;
  }

  protected QName getMode() {
    return mode;
  }

  @Override
public void resolutionDiagnostics(Branch parent, DocumentFactory fac) {
    Element e = fac.createElement(dongfang.xsltools.util.Util
        .capitalizedStringToHyphenString(getClass()));
    parent.add(parent = e);
    e.addAttribute("mode", Dom4jUtil.clarkName(mode));
  }
}
