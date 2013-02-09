/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsException;

/**
 * A lexical scope Resolver, used for binding of local variables / attribute
 * sets. Lexical scope resolvers are arranged in singly linked lists; if the
 * resolver at the head of the list does not have a binding for some name, the
 * resolution request is passed to its successor ("parent") .. etc, until the
 * name is resolved, or the last resolver did not have a binding. This
 * implements more local bindings shadowing less local ones. The resolver at the
 * tail of the list will usually be the one representing the top level bindings.
 * 
 * @author dongfang
 */
public class LexicalScopeResolver extends ResolverBase {

  private Resolver parent;

  protected LexicalScopeResolver(Resolver parent) {
    super();
    this.parent = parent;
  }

  /*
   * Continued resolution here goes to the parent.
   */
  @Override
protected Binding continueResolution(QName name, short symbolSpace,
      Resolver wueeeee) throws XSLToolsException {
    return parent.resolve(name, symbolSpace, wueeeee);
  }

  public short resolverScope() {
    return LOCAL_SCOPE;
  }

  @Override
public void resolutionDiagnostics(Branch eparent, DocumentFactory fac) {
    super.resolutionDiagnostics(eparent, fac);
    if (parent != null)
      parent.resolutionDiagnostics(eparent, fac);
  }
}