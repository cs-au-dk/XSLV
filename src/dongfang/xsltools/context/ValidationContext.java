/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-28
 */
package dongfang.xsltools.context;

import java.io.IOException;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.validation.ValidationResult;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

/**
 * @author dongfang
 */
public interface ValidationContext extends ResolutionContext {
  /*
   * We need to have dynamic names because of session to name mapping: A
   * validator, which should not concern itself with things like sessions,
   * can not know otherwise.
   */
  String getSchemaIdentifier(String documentIdentifier, short io);

  /**
   * Get the string identifying the string resource that tells us the name of the root element.
   * @param s - the systemId of the schema in question
   * @param io - do we mean input or output side?
   * @return
   */
  String getRootElementNameIdentifier(String s, short io);
  
  /**
   * Get the string identifying the string resource that tells us the namespace of the (DTD) schema.
   * @param s - the systemId of the schema in question
   * @param io - do we mean input or output side?
   * @return
   */
  String getNamespaceURIIdentifier(String s, short io);

  /**
   * Get the string identifying the principal stylesheet module.
   * @return
   */
  String getStylesheetIdentifier();

  /**
   * Get a string resource, such as a namespace URI or a root element name. This
   * method may block the caller thread until resource is obtained.
   * There is no particular reason that that went here and not in ResolutionContext
   * -- or that it should go there, for that matter.
   * 
   * @param id
   * @return
   * @throws IOException
   */
  String resolveString(String systemId, String user, String none, int humanKey) throws IOException;

  /**
   * Let the context know in advance that the string resource will be requested
   * eventually. This method does not block the caller thread.
   * Largely unused / ignored / obsolete. 
   * @param id
   * @param humanReadable
   */
  void earlyStringRequest(String id, String user, String none, int humanReadableKey) throws IOException;

  /*
   * To be replaces by something better....
   */
  // void dropConstructedResources();

  /**
   * Get the complete input type. This may be implemented to depend on further
   * resource resolution (using methods in the superinterface)
   * 
   * @return
   * @throws XSLToolsException
   */
  SingleTypeXMLClass getInputType(String systemId)
      throws XSLToolsLoadException;
  
  /**
   * Get the complete output type. This may be implemented to depend on further
   * resource resolution (using methods in the superinterface)
   * 
   * @return
   * @throws XSLToolsException
   */
  XMLGraph getOutputType(String systemId) throws XSLToolsLoadException;

  /*
   * Needs a system ID? Ever uses anything else than the here given?
   */
  Stylesheet getStylesheet(ErrorReporter cesspool) throws XSLToolsException;

  ControlFlowGraph getControlFlowGraph(ErrorReporter cesspool) throws XSLToolsException;

  XMLGraph getControlFlowXMLGraph(ErrorReporter cesspool) throws XSLToolsException;

  /*
   * Could be made to return the result instead -- nicer???
   */
  void validate(ErrorReporter cesspool, ValidationResult result) throws XSLToolsException;
  
  void pushMessage(String target, String message);
  
  void setResolver(ResolutionContext resolver);
  
  void reset();
}
