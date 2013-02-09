package dongfang.xsltools.experimental.controlflow;

import dongfang.xsltools.context.ValidationContextImpl;
import dongfang.xsltools.diagnostics.ShowStopperErrorReporter;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.resolver.URLResolutionContext;
import dongfang.xsltools.simplification.ExternalModuleSaver;
import dongfang.xsltools.validation.FlowAnalysisValidationRun;
import dongfang.xsltools.validation.XSLTValidator;

public class FlowAnnotationExperiment {
  public static void main (String [] args) throws Exception {
    ValidationContextImpl ctx = new ValidationContextImpl() {
      public void earlyStringRequest(String id, String user, String none, int humanReadableKey)
      {
      }

      public String getSchemaIdentifier(String s, short io) {
        return io == INPUT ? "file:web/examples/salesreport/input/fooinc1.xsd" :
          null;
      }

      public String getStylesheetIdentifier() {
        return "file:web/examples/triples/salesreport/xslt/fooinc1.xsl";
      }
      
      public String getRootElementNameIdentifier(String s, short io) {
        return s;
      }
      
      public String getNamespaceURIIdentifier(String s, short io) {
        return s;
      }

      public String resolveString(String id, String user, String none, int humanKey) {
//        if (humanKey == ResolutionContext.INPUT_DTD_NAMESPACE_URI_KEY) {
//          return "";
//        } else if (humanKey == ResolutionContext.INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY) {
//          return "collection";
//        } else
          return null;
      }

      @Override
	public void earlyStreamRequest(String systemId, String user, int humanKey) {
      }

      public void pushMessage(String target, String massage) {
        System.err.println(massage + "-->" + target);
      }
    };

    ctx.setResolver(new URLResolutionContext());
    
    FlowAnalysisValidationRun story = new FlowAnalysisValidationRun();
    
    XSLTValidator.analyzeFlow
    ("file:web/examples/salesreport/xslt/fooinc1.xsl", 
        ctx, new ShowStopperErrorReporter(), story);
    
    ExternalModuleSaver saver = new ExternalModuleSaver
    ("gedefims", new ShowStopperErrorReporter());
    
    saver.process(story.getStylesheet(), StylesheetModule.ORIGINAL);
  }
}
