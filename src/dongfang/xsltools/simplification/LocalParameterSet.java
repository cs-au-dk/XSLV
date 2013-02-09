package dongfang.xsltools.simplification;

import java.util.LinkedList;
import java.util.List;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.StylesheetModule;

public class LocalParameterSet extends Binding {

  private List<Binding> bindings = new LinkedList<Binding>();

  public LocalParameterSet(StylesheetModule bindingStylesheetModule,
      Binding start) {
    super(bindingStylesheetModule);
    bindings.add(start);
  }

  @Override
  Binding bindBehaviour(QName name, Resolver resolver) throws XSLToolsException {
    Binding b = resolver
        .resolve(name, Resolver.LOCAL_PARAMDECL_SET_SYMBOLSPACE);
    if (b != null) {
      List<Binding> bs = ((LocalParameterSet) b).bindings;

      // hack
      for (int i = 0; i < bs.size(); i++) {
        Binding b2 = bs.get(i);
        boolean doit = true;
        for (int j = 0; j < bindings.size() && doit; j++) {
          Binding b3 = bindings.get(j);
          if (b2.getClass() == b3.getClass()
              && b2.toString().equals(b3.toString())) {
            doit = false;
          }
        }
        if (doit)
          bindings.add(b2);
      }
    }
    return this;
  }

  @Override
  short getSymbolSpace() {
    return Resolver.LOCAL_PARAMDECL_SET_SYMBOLSPACE;
  }

  @Override
  public void removeAllVariableRefs(Resolver resolver,
      ResolutionSimplifierBase simplifier) throws XSLToolsException {
  }

  @Override
  void resolutionDiagnostics(Branch parent, DocumentFactory fac) {
  }

  @Override
  Binding resolveBehaviour(QName name, Resolver resolver)
      throws XSLToolsException {
    return this;
  }

  List<Binding> getBindings() {
    return bindings;
  }

  @Override
public String toString() {
    return bindings.toString();
  }
}
