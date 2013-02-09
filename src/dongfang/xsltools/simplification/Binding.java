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
import dongfang.xsltools.model.StylesheetModule;

/**
 * This interface describes values that may be bound and retrieved by a
 * Resolver. In order to support complete resolution of bound values, even if
 * the bound values themselves contain names references, it is necessary to be
 * able to retrieve the context in which these once again were bound. The bound
 * values can implement that behaviour in the resolveBehaviour method. The bound
 * elements all support dumping their value into a dom4j attribute or attaching
 * it to an element as a child element.
 * 
 * @author dongfang
 */
public abstract class Binding {
  private StylesheetModule bindingStylesheetModule;

  private boolean contentIsResolved;

  private short bindingScope;

  Binding(StylesheetModule bindingStylesheetModule) {
    this.bindingStylesheetModule = bindingStylesheetModule;
  }

  /**
   * Implements the bind time behaviour of bound values: For variables and
   * parameters, that includes checking that the same name is not bound in the
   * same scope (given) by supplied <code>Resolver</code>) already. For
   * attribute sets, this could include merging. The
   * <code>Resolver</resolver> will call this method prior to storing the binding 
   * in its binding structure (Resolver). 
   * The caller is expected to be exception-safe: If an exception is thrown from here,
   * no binding will be stored, and data structures are left in or reverted to the
   * state they had before the bind call.
   * @param name - name that new <code>Binding</code> is to be bound under.
   * @param resolver - the resolution context in which this binding is made
   */
  abstract Binding bindBehaviour(QName name, Resolver resolver)
      throws XSLToolsException;

  /**
   * A hack pretty much for the sake of ResolutionSimplifier alone
   * 
   * @param resolver
   * @param simplifier
   * @throws XSLToolsException
   */
  public abstract void removeAllVariableRefs(Resolver resolver,
      ResolutionSimplifierBase simplifier) throws XSLToolsException;

  /**
   * Implements further resolution of any variable references that may be found
   * in the bound value. The <code>Resolver</code> provided will represent the
   * binding context in whics this binding was made originally.
   * 
   * @param name
   * @param resolver -
   *          the resolution context in which this binding was made
   * @return - a variable reference free value
   * @throws XSLSimplificationException
   */
  abstract Binding resolveBehaviour(QName name, Resolver resolver)
      throws XSLToolsException;

  abstract void resolutionDiagnostics(Branch parent, DocumentFactory fac);

  /**
   * Returns the symbol space index of the binding: There is one for parameters
   * and variables, another for attribute sets, etc. Bindings under the same
   * name but in different symbol spaces do not interfere.
   */
  abstract short getSymbolSpace();

  /**
   * A hack pretty much for the sake of ResolutionSimplifier alone
   * 
   * @param resolver
   * @param simplifier
   * @throws XSLToolsException
   */
  StylesheetModule getBindingStylesheetModule() {
    return bindingStylesheetModule;
  }

  /**
   * Flag: There is nothing to be resolved (variable refs etc.) in the contents
   * of this binding.
   * 
   * @return
   */
  boolean contentIsResolved() {
    return contentIsResolved;
  }

  /**
   * Flag: There is nothing to be resolved (variable refs etc.) in the contents
   * of this binding.
   * 
   * @return
   */
  void setContentResolved() {
    contentIsResolved = true;
  }

  void setBindingScope(short value) {
    bindingScope = value;
  }

  short getBindingScope() {
    return bindingScope;
  }

  /**
   * A special binding that is used for binding a name to nothing, thus hiding
   * bindings of the same name in outer or top level scopes. It is used by
   * ForEachResolverShim when simplifying for-each.
   */
  static Binding SHOULD_NOT_RESOLVE = new Binding(null) {

    @Override
	public Binding bindBehaviour(QName name, Resolver resolver) {
      return null;
    }

    @Override
	public Binding resolveBehaviour(QName name, Resolver resolver) {
      return null;
    }

    @Override
	public void removeAllVariableRefs(Resolver resolver,
        ResolutionSimplifierBase simplifier) {
    }

    @Override
	public void resolutionDiagnostics(Branch parent, DocumentFactory fac) {
      Element e = fac.createElement("should-not-resolve");
      parent.add(e);
    }

    @Override
	public short getSymbolSpace() {
      return Resolver.UNDEFINED_SYMBOLSPACE;
    }
  };
}