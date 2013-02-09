/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import org.dom4j.QName;

import dongfang.xslt.XSLT;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.StylesheetModule;

/**
 * A binding of an xsl parameter or an xsl variable.
 * @author dongfang
 */
public abstract class ParameterOrVariableBinding extends Binding {
  public static final short UNDEFINED_BINDING_TYPE = 0;

  public static final short PARAMETER_BINDING_TYPE = 1;

  public static final short VARIABLE_BINDING_TYPE = 2;

  // public static final short KEY_BINDING_TYPE = 3;

  public static final short XPATH_VALUE_TYPE = 0;

  public static final short RTF_VALUE_TYPE = 1;

  public static final short SYMBOLSPACE = Resolver.PARAMETER_AND_VARIABLE_SYMBOLSPACE;

  private short scope;

  /*
   * If any reason comes up, by all means get rid of this and replace by
   * subclasses. We have 3 axes of binding refinement: Parameter/variable
   * Local/global (can be omitted) RTF/simpleType value
   */
  private short bindingType;

  protected ParameterOrVariableBinding(
      StylesheetModule bindingStylesheetModule, short scope, short bindingType) {
    super(bindingStylesheetModule);
    this.bindingType = bindingType;
  }

  /*
   * This will be set to true for template-local parameter and variable
   * bindings, and false for global ones. Purpose: Resolving variables
   * referenced from within a for-each instruction, this will tell if the
   * variable is template-defined and thus should be forwarded as a parameter to
   * the template created in Rule ###. Other purpose: Sanity check (at resolve
   * time): Variables bound in a global scope (ImportPrecedenceGroup) should
   * have this set to false. Variables bound locally (LexicalScope) should have
   * this set to true. A read accessor should be defined on Binding, to allow
   * the same check on attribute-sets. BTW: If sanity testing is OK, maybe this
   * whole operation of determining whether a binding is global or local, in the
   * for-each deconstruction, is not necessary: References to globally defined
   * variables could simply be forwarded, too. (let's try that first)
   */
  // private boolean isLocal;
  /*
   * public short getScoop() { return scope; }
   */
  @Override
public Binding bindBehaviour(QName name, Resolver resolver)
      throws XSLToolsException {
    Binding oldBindable = resolver.resolveLocalScope(name, SYMBOLSPACE);
    if (oldBindable != null) {
      if (scope == Resolver.LOCAL_SCOPE) {
        XSLT.getInstance().localVariablesOrParametersNameClash();
      } else {
        XSLT.getInstance().toplevelVariablesOrParametersNameClash();
      }
      // install resolver bomb:
      // this will go off at resolution time.
      return new DoubleBindingBomb(this, oldBindable);
    }
    return this;
  }

  public abstract short getValueType();

  public boolean isVariableBinding() {
    return bindingType == VARIABLE_BINDING_TYPE;
  }

  public boolean isParameterBinding() {
    return bindingType == PARAMETER_BINDING_TYPE;
  }

  @Override
public short getSymbolSpace() {
    return SYMBOLSPACE;
  }

  @Override
public Binding resolveBehaviour(QName name, Resolver resolver) {
    return this;
  }
}