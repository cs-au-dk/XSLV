/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import java.util.Iterator;

import org.dom4j.Element;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;

/**
 * Recursion support base class for non copying tree traversing simplifiers.
 * 
 * @author dongfang
 */
public abstract class StructuralSimplifierBase extends StylesheetSimplifierBase {

  protected void simplifyBelow(Element originalElement, Resolver resolver,
      Resolver groupscope, StylesheetModule module, int depth)
      throws XSLToolsException {
    for (Iterator it = originalElement.elementIterator(); it.hasNext();) {
      Element childElement = (Element) it.next();
      simplify(childElement, resolver, groupscope, module, depth + 1);
    }
  }

  void process(StylesheetLevel level, Stylesheet resolver)
      throws XSLToolsException {
    for (StylesheetModule module : level.contents())
      simplify(module, resolver);

    for (StylesheetLevel imported : level.imports()) {
      process(imported, resolver);
    }
  }

  protected abstract void simplify(StylesheetModule module, Stylesheet resolver)
      throws XSLToolsException;

  protected abstract void simplify(Element element, Resolver resolver,
      Resolver groupscope, StylesheetModule module, int depth)
      throws XSLToolsException;
}