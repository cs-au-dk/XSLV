/*
 * dongfang M. Sc. Thesis
 * Created on Jun 4, 2005
 */
package dongfang.xsltools.xmlclass.xsd;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Document;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.model.ModuleFactory;
import dongfang.xsltools.model.ParseLocationStylesheetModuleFactory;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.util.ListStack;

/**
 * @author dongfang
 */
public class XSDSchemaFactory {
  public static XSDSchemaDocument createSchemaDocument(String systemId,
      int humanKey, ResolutionContext context, String targetOverride,
      ErrorReporter cesspool) throws XSLToolsException {
    Document doc = getDocument(systemId, humanKey, context, cesspool);
    return createSchemaDocument(systemId, doc, targetOverride, cesspool);
  }

  static Document getDocument(String systemId, int humanKey,
      ResolutionContext context, ErrorReporter cesspool)
      throws XSLToolsException {
    ModuleFactory fac = new ParseLocationStylesheetModuleFactory();
    Document doc = fac.read
    (systemId, "An " + ResolutionContext.HUMAN_INTERFACE_STRINGS
        [ResolutionContext.XMLINSTANCE_MODULE_IDENTIFIER_KEY], 
        humanKey, ResolutionContext.MSG_XSD, context,
        cesspool);
    return doc;
  }

  static XSDSchemaDocument createSchemaDocument(String systemId, Document doc,
      String targetOverride, ErrorReporter cesspool) throws XSLToolsException {
    XSDSchemaDocument schdoc = new XSDSchemaDocument(systemId);
    schdoc.setDocument(doc, targetOverride, cesspool);
    return schdoc;
  }

  static class LoadingSchema {
    String url;

    String targetOverride;

    LoadingSchema(String url, String targetOverride) {
      this.url = url;
      this.targetOverride = targetOverride;
    }
  }

  public static XSDSchema createSchema(
      String systemId, short io,
      ValidationContext context, ErrorReporter cesspool)
      throws XSLToolsException {

    ListStack<String> dupeStack = new ListStack<String>();
    XSDSchema result = new XSDSchema();

    _foo(result, new LoadingSchema(systemId, null), dupeStack, true, context, cesspool);

    result.init(context, io);

    return result;
  }

  private static void _foo(XSDSchema result, LoadingSchema next,
      ListStack<String> dupeStack, boolean primary, ValidationContext context,
      ErrorReporter cesspool) throws XSLToolsException {
    try {
      String nextId = next.url;
      // System.err.println("Trying: "+nextId);
      if (dupeStack.contains(nextId)) {
        System.err.println("Cyclic, or multiple include / import of " + nextId
            + ". Skipping. Dupe stack is: " + dupeStack);
        return;
      }

      dupeStack.push(nextId);

      String nextNS = next.targetOverride;
      
      int type = 
        primary ? ResolutionContext.SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY
            :
              ResolutionContext.SCHEMA_SECONDARY_COMPONENT_IDENTIFIER_KEY;
      
      XSDSchemaDocument sdocument = createSchemaDocument(nextId,
          type, context,
          nextNS, cesspool);

      result.addSchemaDocument(nextId, sdocument);

      Set<LoadingSchema> loadQueue = new HashSet<LoadingSchema>();

      loadQueue.addAll(sdocument.resolveImportReferences(nextId));
      loadQueue.addAll(sdocument.resolveIncludeReferences(nextId));

      for (LoadingSchema s : loadQueue) {
        _foo(result, s, dupeStack, false, context, cesspool);
      }

      dupeStack.pop();
    } catch (URISyntaxException ex) {
      throw new XSLToolsSchemaException(ex);
    }
  }
}
