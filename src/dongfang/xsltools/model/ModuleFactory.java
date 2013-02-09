/*
 * dongfang M. Sc. Thesis
 * Created on 03-10-2005
 */
package dongfang.xsltools.model;

import org.dom4j.Document;

import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.resolver.ResolutionContext;

public interface ModuleFactory {
  Document read(String systemId, String userExplanation, int humanKey, Object expectedKind,
      ResolutionContext context, ErrorReporter cesspool)
      throws XSLToolsLoadException;
}
