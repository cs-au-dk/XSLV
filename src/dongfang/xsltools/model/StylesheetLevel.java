/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-03
 */
package dongfang.xsltools.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.simplification.Binding;
import dongfang.xsltools.simplification.ResolutionSimplifierBase;
import dongfang.xsltools.simplification.Resolver;
import dongfang.xsltools.simplification.ResolverBase;
import dongfang.xsltools.util.Util;

/**
 * @author dongfang
 */
public class StylesheetLevel extends ResolverBase implements Cloneable,
    Diagnoseable {
  private List<StylesheetModule> contents = new ArrayList<StylesheetModule>();

  private List<StylesheetLevel> imports = new ArrayList<StylesheetLevel>();

  /**
   * This is not normally used ... removes everything. Used with the external
   * module loader.
   */
  public void clear() {
    contents.clear();
    imports.clear();
  }

  public void addContent(StylesheetModule module) {
    contents.add(module);
  }

  public void addImport(StylesheetLevel level) {
    imports.add(level);
  }

  public List<StylesheetModule> contents() {
    return contents;
  }

  public List<StylesheetLevel> imports() {
    return imports;
  }

  @Override
protected Binding continueResolution(QName name, short scope, Resolver starter)
      throws XSLToolsException {
    ListIterator<StylesheetLevel> it = imports.listIterator(imports.size());
    while (it.hasPrevious()) {
      Binding b = it.previous().resolve(name, scope, starter);
      if (b != null) {
        return b;
      }
    }
    return null;
  }

  /*
   * protected Resolver continueBindingResolver(QName name, short scope) throws
   * XSLToolsException { ListIterator<ImportPrecedenceGroup> it =
   * imports.listIterator(imports.size()); while(it.hasPrevious()) { Resolver r =
   * it.previous().bindingResolver(name, scope); if (r!=null) { return r; } }
   * return null; }
   */

  protected StylesheetModule getPrincipalModule() {
    return contents().get(contents().size() - 1);
  }

  /**
   * Runs the simplifier on the bound par/var and attribute-set values.
   * 
   * @param resolver
   * @param simplifier
   * @throws XSLToolsException
   */
  public void removeAllVariableRefs(Resolver resolver,
      ResolutionSimplifierBase simplifier) throws XSLToolsException {
    if (symbolSpaces[ATTRIBUTE_SET_SYMBOLSPACE] != null) {
      for (Iterator varbindings = symbolSpaces[ATTRIBUTE_SET_SYMBOLSPACE]
          .values().iterator(); varbindings.hasNext();) {
        Binding binding = (Binding) varbindings.next();
        binding.removeAllVariableRefs(resolver, simplifier);
      }
    }
    if (symbolSpaces[PARAMETER_AND_VARIABLE_SYMBOLSPACE] != null) {
      for (Iterator varbindings = symbolSpaces[PARAMETER_AND_VARIABLE_SYMBOLSPACE]
          .values().iterator(); varbindings.hasNext();) {
        Object binding = varbindings.next();
        ((Binding) binding).removeAllVariableRefs(resolver, simplifier);
      }
    }
  }

  public void setSublevelNumberUpperBound(int i) {
    for (StylesheetModule module : contents) {
      module.setSublevelNumberUpperBound(i);
    }
  }

  public short resolverScope() {
    return TOPLEVEL_SCOPE;
  }

  @Override
public void resolutionDiagnostics(Branch context, DocumentFactory fac) {
    Element t = fac.createElement(Util
        .capitalizedStringToHyphenString(getClass()));
    context.add(context = t);
    t = fac.createElement("toplevel-bindings");
    context.add(t);
    super.resolutionDiagnostics(t, fac);
    context.add(t = fac.createElement("imports"));
    for (StylesheetLevel level : imports) {
      level.resolutionDiagnostics(t, fac);
    }
  }

  public void diagnostics(Branch result, DocumentFactory fac, Set<Object> configuration) {
    Element e1 = fac.createElement(Util
        .capitalizedStringToHyphenString(getClass()));
    result.add(e1);
    Element e2 = fac.createElement("contents");
    e1.add(e2);
    for (StylesheetModule module : contents) {
      module.diagnostics(e2, fac, configuration);
    }
    e2 = fac.createElement("imports");
    e1.add(e2);
    for (StylesheetLevel level : imports()) {
      level.diagnostics(e2, fac, configuration);
    }
  }

  /**
   * Clones all contents and imports, but throws out all bindings.
   */
  @Override
public Object clone() {
    StylesheetLevel clone = new StylesheetLevel();

    for (StylesheetModule m : contents) {
      clone.addContent((StylesheetModule) m.clone());
    }

    for (StylesheetLevel g : imports) {
      clone.addImport((StylesheetLevel) g.clone());
    }

    return clone;
  }

  public StylesheetModule getModule(String uri) {
    for (StylesheetModule module : contents) {
      if (uri.equals(module.getSystemId()))
        return module;
    }

    StylesheetModule module = null;
    for (StylesheetLevel level : imports) {
      if ((module = level.getModule(uri)) != null)
        return module;
    }

    return null;
  }

  public void getAllModules(List<StylesheetModule> result) {
    result.addAll(contents);
    for (ListIterator<StylesheetLevel> iter = imports.listIterator(imports
        .size()); iter.hasPrevious();) {
      iter.previous().getAllModules(result);
    }
  }

  @Override
public String toString() {
    return "Contents: " + contents.toString() + ", Imports: "
        + imports.toString() + ", Resolver:" + super.toString();
  }
}
