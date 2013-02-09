/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.model;

import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.resolver.ResolutionContext;

/**
 * @author dongfang
 */
public interface StylesheetModuleFactory extends ModuleFactory {
  /**
   * @param systemId -
   *          URL or other ID that the context will understand
   * @param context -
   *          a resolution context (kind of entity resolver)
   * @param levelnumber -
   *          level number to assign
   * @param modulenumber -
   *          module number (intra-level) to assign
   * @param errorReporter -
   *          error reports go here
   * @return
   * @throws XSLToolsLoadException
   */
  StylesheetModule createStylesheetModule(String systemId,
      String userExplanation,
      ResolutionContext context, int groupnumber, int modulenumber,
      ErrorReporter errorReporter) throws XSLToolsLoadException;

  /**
   * Loads the stylesheet module referred to by the URI and creates an
   * <code>StylesheetLevel</code> with the module as its sole contents
   * 
   * @param systemId -
   *          URL or other ID that the context will understand
   * @param context -
   *          a resolution context (kind of entity resolver)
   * @param levelnumber -
   *          level number to assign
   * @param errorReporter -
   *          error reports go here
   */
  StylesheetLevel createStylesheetLevel(String systemId,
      String userExplanation,
      ResolutionContext context, int levelnumber, ErrorReporter errorReporter)
      throws XSLToolsLoadException;

  /**
   * Loads the stylesheet principal module from the URL given and creates a
   * containing <code>Stylesheet</code>
   * 
   * @param systemId -
   *          URL or other ID that the context will understand
   * @param context -
   *          a resolution context (kind of entity resolver)
   * @param errorReporter -
   *          error reports go here
   */
  Stylesheet createStylesheet(String systemId, String userExplanation, ResolutionContext context,
      ErrorReporter errorReporter) throws XSLToolsLoadException;
}