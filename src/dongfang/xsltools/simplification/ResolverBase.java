/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.util.Dom4jUtil;

/**
 * Common base class for the ImportPrecedenceGroup and LexicalScopeResolver
 * classes.
 * 
 * @author dongfang
 */
public abstract class ResolverBase implements Resolver {

  /*
   * The maps where bindings are stored. Different stuff (eg. vars and pars vs.
   * attribute sets) goes into different namespaces->different maps.
   */
  protected Map parvars;

  protected Map attsets;

  /*
   * all of them
   */
  protected Map[] symbolSpaces = new Map[SYMBOLSPACE_COUNT];

  public static String getSymbolSpaceName(short symbolSpace) {
    switch (symbolSpace) {
    case PARAMETER_AND_VARIABLE_SYMBOLSPACE:
      return "parameter of variable";
    case ATTRIBUTE_SET_SYMBOLSPACE:
      return "attribute set";
    case TEMPLATE_SYMBOLSPACE:
      return "named template";
    case KEY_SYMBOLSPACE:
      return "key";
    default:
      return "unknown";
    }
  }

  protected ResolverBase() {
    this.symbolSpaces[PARAMETER_AND_VARIABLE_SYMBOLSPACE] = parvars;
    this.symbolSpaces[ATTRIBUTE_SET_SYMBOLSPACE] = attsets;
  }

  /*
   * Resolve in local scope - do not propagate
   */
  public Binding resolveLocalScope(QName name, short symbolSpace) {
    Map<QName, Binding> scopebindings = symbolSpaces[symbolSpace];
    if (scopebindings == null)
      return null;
    return scopebindings.get(name);
  }

  /*
   * Called after a failed local scope resolve, or as part of bind or resolve
   * behaviour.
   */
  protected abstract Binding continueResolution(QName name, short scope,
      Resolver starter) throws XSLToolsException;

  /*
   * protected abstract Resolver continueBindingResolver(QName name, short
   * scope) throws XSLToolsException;
   */

  public Binding resolve(QName name, short symbolSpace)
      throws XSLToolsException {
    return resolve(name, symbolSpace, this);
  }

  public Binding resolve(QName name, short symbolSpace, Resolver base)
      throws XSLToolsException {
    Binding b = resolveLocalScope(name, symbolSpace);
    if (b != null) {
      /*
       * if (b.getScoop() != getScoop()); throw new
       * XSLToolsInternalErrorException ("A binding resolved from a different
       * Resolver than the one expected!");
       */
      b.resolveBehaviour(name, base);
      return b;
    }
    return continueResolution(name, symbolSpace, base);
  }

  /*
   * public Resolver bindingResolver(QName name, short scope) throws
   * XSLToolsException { if (resolveLocalScope(name, scope)!=null) return this;
   * return continueBindingResolver(name, scope); }
   */
  protected void doBind(QName name, short symbolSpace, Binding b) {
    if (symbolSpaces[symbolSpace] == null)
      symbolSpaces[symbolSpace] = new HashMap();
    symbolSpaces[symbolSpace].put(name, b);
  }

  public void bind(QName name, Binding b) throws XSLToolsException {
    b = b.bindBehaviour(name, this);
    b.setBindingScope(resolverScope());
    doBind(name, b.getSymbolSpace(), b);
  }

  public Resolver enterScope() {
    return new LexicalScopeResolver(this);
  }

  public void resolutionDiagnostics(Branch context, DocumentFactory fac) {
    for (int i = 0; i < Resolver.SYMBOLSPACE_COUNT; i++) {
      fac.createElement(Resolver.SYMBOLSPACE_CLASSES[i].getName());
      Map<QName, Binding> m = symbolSpaces[i];
      if (m != null)
        for (Iterator<QName> it = m.keySet().iterator(); it.hasNext();) {
          Element e = fac.createElement("binding");
          context.add(e);
          QName k = it.next();
          Binding b = (Binding) symbolSpaces[i].get(k);
          e.addAttribute("name", Dom4jUtil.clarkName(k));
          b.resolutionDiagnostics(e, fac);
        }
    }
  }

  @Override
public String toString() {
    return "par-var: "
        + (symbolSpaces[PARAMETER_AND_VARIABLE_SYMBOLSPACE] == null ? "null"
            : symbolSpaces[PARAMETER_AND_VARIABLE_SYMBOLSPACE].toString())
        + " attsets: "
        + (symbolSpaces[ATTRIBUTE_SET_SYMBOLSPACE] == null ? "null"
            : symbolSpaces[ATTRIBUTE_SET_SYMBOLSPACE].toString());
  }
}
