/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-16
 */
package dongfang.xsltools.simplification;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dongfang.xslt.XSLT;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Util;

/**
 * A binding representing two local bindings under the same name and in the same
 * symbol space. Resolution of this should fail in XSLT2. Modelled by failing
 * upon resolution.
 * 
 * @author dongfang
 */
public class DoubleBindingBomb extends Binding {
  private Binding primary;

  private Binding secondary;

  protected DoubleBindingBomb(Binding primary, Binding secondary) {
    super(null);
    this.primary = primary;
    this.secondary = secondary;
  }

  @Override
public Binding bindBehaviour(QName name, Resolver resolver)
      throws XSLToolsException {
    return primary;
  }

  @Override
public void removeAllVariableRefs(Resolver resolver,
      ResolutionSimplifierBase simplifier) {
    // simplifier.simplify(this, resolver);
    throw new AssertionError("This should never be called");
  }

  @Override
public Binding resolveBehaviour(QName name, Resolver resolver)
      throws XSLToolsException {
    XSLT.getInstance().toplevelVariablesOrParametersNameClash();
    return primary.resolveBehaviour(name, resolver);
  }

  @Override
public short getSymbolSpace() {
    return primary.getSymbolSpace();
  }

  @Override
public StylesheetModule getBindingStylesheetModule() {
    return primary.getBindingStylesheetModule();
  }

  @Override
public void resolutionDiagnostics(Branch parent, DocumentFactory fac) {
    Element me = fac.createElement(Util.capitalizedStringToHyphenString(this
        .getClass()));
    parent.add(me);
    Element primary = fac.createElement("primary");
    me.add(primary);
    this.primary.resolutionDiagnostics(primary, fac);
    Element secondary = fac.createElement("secondary");
    me.add(secondary);
    this.secondary.resolutionDiagnostics(secondary, fac);
  }
}