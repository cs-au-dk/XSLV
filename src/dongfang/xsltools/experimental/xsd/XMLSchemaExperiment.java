package dongfang.xsltools.experimental.xsd;

import java.io.IOException;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.context.ValidationContextImpl;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.resolver.URLResolutionContext;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

public class XMLSchemaExperiment {
  public void testMCS(SingleTypeXMLClass inputType) {
    System.out.println(Dom4jUtil.diagnostics(inputType));
  }

  SingleTypeXMLClass getInputType() throws XSLToolsException {
    ResolutionContext ctx = new URLResolutionContext();

    ValidationContextImpl impesen = new ValidationContextImpl() {

      public void earlyStringRequest(String id, String user, String none, int humanReadableKey)
      {
      }

      public String getSchemaIdentifier(String s, short io) {
        if (io == INPUT)
          return "file:experimental-data/recipeml.xsd";
        return null;
      }

      public String getStylesheetIdentifier() {
        return null;
      }
      
      public String getRootElementNameIdentifier(String s, short io) {
        return s;
      }
      
      public String getNamespaceURIIdentifier(String s, short io) {
        return s;
      }

      public String resolveString(String id, String user, String none, int humanKey) {
        if (humanKey == ResolutionContext.INPUT_DTD_NAMESPACE_URI_KEY) {
          return "";
        } else if (humanKey == ResolutionContext.INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY) {
          return "collection";
        } else
          return null;
      }

      @Override
	public void earlyStreamRequest(String systemId, String user, int humanKey)
          throws IOException {
      }

      public void pushMessage(String target, String massage) {
        System.err.println(massage + "-->" + target);
      }
    };

    impesen.setResolver(ctx);

    return impesen.getInputType(impesen.getSchemaIdentifier("", ResolutionContext.INPUT));
  }

  public static void main(String[] args) throws Exception {
    XMLSchemaExperiment e = new XMLSchemaExperiment();
    e.testMCS(e.getInputType());
  }
}
