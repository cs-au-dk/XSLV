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
 * Interface for a variable and parameter resolver. Currently, this interface
 * represents both an object for binding and resolution, and a lexical scope for
 * binding and resolution.
 * 
 * @author dongfang
 */
public interface Resolver {
  short PARAMETER_AND_VARIABLE_SYMBOLSPACE = 0;

  short ATTRIBUTE_SET_SYMBOLSPACE = 1;

  short TEMPLATE_SYMBOLSPACE = 2;

  short KEY_SYMBOLSPACE = 3;

  short LOCAL_PARAMDECL_SET_SYMBOLSPACE = 4;

  short UNDEFINED_SYMBOLSPACE = 5;

  short SYMBOLSPACE_COUNT = 5;

  short TOPLEVEL_SCOPE = 0;

  short LOCAL_SCOPE = 1;

  short UNDEFINED_SCOPE = 2;

  Class[] SYMBOLSPACE_CLASSES = { ParameterOrVariableBinding.class,
      AttributeSetBinding.class, TemplateBinding.class, KeyBinding.class,
      LocalParameterSet.class };

  /**
   * Introduce a new name->element binding. It is left to the bound value to
   * decide whether a binding to a name already bound is an error, or, if not,
   * which binding that will have precedence.
   * 
   * @param name -
   *          a name
   * @param e -
   *          en element to bind the name to
   * @throws XSLSimplificationException
   */
  void bind(QName name, Binding e) throws XSLToolsException;

  /**
   * Internal method: Pass a resolver for use by resolveBehavior.
   * 
   * @param name
   * @param scope
   * @param base
   * @return
   * @throws XSLSimplificationException
   */
  Binding resolve(QName name, short symbolSpace, Resolver base)
      throws XSLToolsException;

  /**
   * Resolve the element for the name (or return null if there is none).
   * 
   * @param name
   * @return
   */
  Binding resolve(QName name, short symbolSpace) throws XSLToolsException;

  /**
   * Resolve in the lexical scope represented by this Resolver only. Useful for
   * clash detects at binding time, etc.
   * 
   * @param name
   * @return - the value bound in the lexical scope represented by this
   *         Resolver, or null if nothing is bound
   * @throws XSLSimplificationException
   */
  Binding resolveLocalScope(QName name, short symbolSpace);

  /**
   * Enter a new resolution scope (that will correspond to lexical scope for
   * XSL). <em>Users must update their reference to the resolver to point to
   * the one returned from here !</em>
   * 
   * @return - the updated <code>Resolver</code>.
   */
  Resolver enterScope();

  short resolverScope();

  void resolutionDiagnostics(Branch parent, DocumentFactory fac);
}