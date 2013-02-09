/*
 * dongfang M. Sc. Thesis
 * Created on 2005-09-30
 */
package dongfang.xsltools.simplification;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSL1ErrorException;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.xpath2.XPathExpr;

/**
 * A binding for an xsl:key
 * 
 * @author dongfang
 */
public class KeyBinding extends Binding {
  public static final short SYMBOLSPACE = Resolver.KEY_SYMBOLSPACE;

  private XPathExpr use;

  private XPathExpr match;

  public KeyBinding(StylesheetModule bindingStylesheetModule, XPathExpr match,
      XPathExpr use) {
    super(bindingStylesheetModule);
    // this.name = name;
    this.match = match;
    this.use = use;
  }

  @Override
  Binding bindBehaviour(QName name, Resolver resolver) throws XSLToolsException {
    Binding oldBindable = resolver.resolveLocalScope(name, SYMBOLSPACE);
    if (oldBindable != null) {
      // TODO: Is there a version difference??
      throw new XSL1ErrorException(
          "XSL Error: Local parameter or value shadowed local parameter or value",
          0.0f);
    }
    return this;
  }

  @Override
  short getSymbolSpace() {
    return SYMBOLSPACE;
  }

  @Override
  public void removeAllVariableRefs(Resolver resolver,
      ResolutionSimplifierBase simplifier) throws XSLToolsException {
    /*
     * Fortunately, variable refs in the match and use expressions are not
     * allowed.
     */
  }

  @Override
  void resolutionDiagnostics(Branch parent, DocumentFactory fac) {
    Element container = fac.createElement("key-binding");
    parent.add(container);
    container.addAttribute("match", match.toString());
    container.addAttribute("use", use.toString());
  }

  @Override
  Binding resolveBehaviour(QName name, Resolver resolver)
      throws XSLToolsException {
    return this;
  }

  public XPathExpr getMatchPattern() {
    return match;
  }

  public XPathExpr getUseExp() {
    return use;
  }
}
