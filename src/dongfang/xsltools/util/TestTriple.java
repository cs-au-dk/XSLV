package dongfang.xsltools.util;

import java.net.URI;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLoadException;

/**
 * A representation of a triple; what goes into a static XSLT validation run:
 * - an input schema
 * - an XSLT stylesheet
 * - an output schema
 * 
 * See any "triple.xml" file for the XML format (I SHOULD have written a
 * schema, but ... well ...)
 * @author dongfang
 */
public class TestTriple {
  public static final String DTD = "DTD";

  public static final String RNG = "RelaxNG";

  public static final String XSD = "XSD";

  private String defaultBase;

  private boolean isEnabled = true;

  private boolean shouldValidate = true;

  private String inputSchemaURI;

  private String inputType = DTD;

  private String inputDTDNamespaceURI = "";

  private String inputRootElementName = null;

  private String outputSchemaURI;

  private String outputType = DTD;

  private String outputDTDNamespaceURI = "";

  private String outputRootElementName = null;

  private String stylesheetPrimaryModuleURI;

  private String name = "unnamed";

  private Element notesAsXHTMLHedge;

  private String info = "";

  public void applyBase(String URIBase) {
    try {

      URI baseURI = new URI(URIBase);
      URI result = baseURI.resolve(this.inputSchemaURI);
      this.inputSchemaURI = result.toString();

      result = baseURI.resolve(this.outputSchemaURI);
      this.outputSchemaURI = result.toString();

      result = baseURI.resolve(this.stylesheetPrimaryModuleURI);
      this.stylesheetPrimaryModuleURI = result.toString();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public String getName() {
    return name;
  }

  public String getBase() {
    return defaultBase;
  }

  public String getInputType() {
    return inputType;
  }

  public String getInputDTDNamespaceURI() {
    return inputDTDNamespaceURI;
  }

  public String getInputRootElementName() {
    return inputRootElementName;
  }

  public String getOutputType() {
    return outputType;
  }

  public String getOutputDTDNamespaceURI() {
    return outputDTDNamespaceURI;
  }

  public String getOutputRootElementName() {
    return outputRootElementName;
  }

  public String getStylesheetPrimaryModuleURI() {
    return stylesheetPrimaryModuleURI;
  }

  public String getInputSchemaURI() {
    return inputSchemaURI;
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public String getNotes() {
    if (notesAsXHTMLHedge==null)
      return "";
    StringBuilder result = new StringBuilder();
    for (Object n : notesAsXHTMLHedge.content()) {
      result.append(Dom4jUtil.toString((Node)n));
    }
    return result.toString();
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public boolean shouldValidate() {
    return shouldValidate;
  }

  public void setInputSchemaURI(String inputSchemaURI) {
    this.inputSchemaURI = inputSchemaURI;
  }

  public String getOutputSchemaURI() {
    return outputSchemaURI;
  }

  public void setOutputSchemaURI(String outputSchemaURI) {
    this.outputSchemaURI = outputSchemaURI;
  }

  public void setInputDTDNamespaceURI(String inputDTDNamespaceURI) {
    this.inputDTDNamespaceURI = inputDTDNamespaceURI;
  }

  public void setInputRootElementName(String inputRootElementName) {
    this.inputRootElementName = inputRootElementName;
  }

  public void setInputType(String inputType) {
    this.inputType = inputType;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setOutputDTDNamespaceURI(String outputDTDNamespaceURI) {
    this.outputDTDNamespaceURI = outputDTDNamespaceURI;
  }

  public void setOutputRootElementName(String outputRootElementName) {
    this.outputRootElementName = outputRootElementName;
  }

  public void setOutputType(String outputType) {
    this.outputType = outputType;
  }

  public void setStylesheetPrimaryModuleURI(String stylesheetPrimaryModuleURI) {
    this.stylesheetPrimaryModuleURI = stylesheetPrimaryModuleURI;
  }

  public void setEnabled(boolean b) {
    isEnabled = b;
  }

  public void setShouldValidate(boolean b) {
    shouldValidate = b;
  }

  public void checkResourcesPresent() throws XSLToolsLoadException {
    URL url;
    try {
      url = new URL(getInputSchemaURI());
      url.openStream();
    } catch (Exception ex) {
      throw new XSLToolsLoadException(ex);
    }
    // throw new RuntimeException("Cannot find input schema resource: " +
    // getInputSchemaURI() + " (resolved)");

    try {
      url = new URL(getOutputSchemaURI());
      url.openStream();
    } catch (Exception ex) {
      throw new XSLToolsLoadException(ex);
    }
    // throw new RuntimeException("Cannot find input schema resource: " +
    // getInputSchemaURI() + " (resolved)");

    try {
      url = new URL(getStylesheetPrimaryModuleURI());
      url.openStream();
    } catch (Exception ex) {
      throw new XSLToolsLoadException(ex);
    }
  }

  public void read(Element testTriple) {
    Element input = (Element) testTriple.selectSingleNode("input");

    String name = testTriple.attributeValue("name");
    if (name != null)
      this.name = name;

    if ("false".equals(testTriple.attributeValue("enabled")))
      isEnabled = false;

    if ("false".equals(testTriple.attributeValue("shouldValidate")))
      shouldValidate = false;

    if (input == null)
      throw new RuntimeException("Input-schema spec missing!");
    inputType = input.attributeValue("type");
    inputRootElementName = input.attributeValue("RootElementName");
    inputDTDNamespaceURI = input.attributeValue("DTDNamespaceURI");
    if (inputDTDNamespaceURI == null)
      inputDTDNamespaceURI = "";
    inputSchemaURI = input.getTextTrim();

    Element output = (Element) testTriple.selectSingleNode("output");
    if (output == null)
      throw new RuntimeException("Output-schema spec missing!");
    outputType = output.attributeValue("type");
    outputRootElementName = output.attributeValue("RootElementName");
    outputDTDNamespaceURI = output.attributeValue("DTDNamespaceURI");
    if (outputDTDNamespaceURI == null)
      outputDTDNamespaceURI = "";
    outputSchemaURI = output.getTextTrim();

    Element xslt = (Element) testTriple.selectSingleNode("xslt");
    if (xslt == null)
      throw new RuntimeException("Stylesheet primary module (xslt) spec missing!");
    stylesheetPrimaryModuleURI = xslt.getTextTrim();

    Element notes = (Element) testTriple.selectSingleNode("notes");
      this.notesAsXHTMLHedge = notes;

    Element info = (Element) testTriple.selectSingleNode("info");
    if (info != null) {
      this.info = info.getTextTrim();
    }
  }

  public Element toDOM() {
    DocumentFactory fac = new DocumentFactory();
    Element root = fac.createElement("triple");
    root.addAttribute("name", name);
    root.addAttribute("enabled", Boolean.toString(isEnabled));

    Element input = fac.createElement("input");
    root.add(input);
    if (inputType != null)
      input.addAttribute("type", inputType);
    if (inputDTDNamespaceURI != null)
      input.addAttribute("DTDNamespaceURI", inputDTDNamespaceURI);
    if (inputRootElementName != null)
      input.addAttribute("RootElementName", inputRootElementName);
    input.add(fac.createText(inputSchemaURI));

    Element output = fac.createElement("output");
    root.add(output);
    if (outputType != null)
      output.addAttribute("type", outputType);
    if (outputDTDNamespaceURI != null)
      output.addAttribute("DTDNamespaceURI", outputDTDNamespaceURI);
    if (outputRootElementName != null)
      output.addAttribute("RootElementName", outputRootElementName);
    output.add(fac.createText(outputSchemaURI));

    Element xslt = fac.createElement("xslt");
    root.add(xslt);
    xslt.add(fac.createText(stylesheetPrimaryModuleURI));

    if (!"".equals(this.info.trim())) {
      Element info = fac.createElement("info");
      root.add(info);
      info.setText(this.info);
    }

    return root;
  }

  public void parseArgs(String[] args) {
    if (args.length >= 3) {
      stylesheetPrimaryModuleURI = Util.isURL(args[0]) ? args[0] : Util.toUrlString(args[0]);
      inputSchemaURI = Util.isURL(args[1]) ? args[1] : Util.toUrlString(args[1]);
      outputSchemaURI = Util.isURL(args[2]) ? args[2] : Util.toUrlString(args[2]);
    }

    if (args.length == 5) {
      inputRootElementName = args[3];
      inputDTDNamespaceURI = args[4];
    }
  }

  public static TestTriple makeTriple(URL url) throws XSLToolsException {
    TestTriple triple = new TestTriple();
    SAXReader r = new SAXReader();
    Document d = null;
    try {
      d = r.read(url);
    } catch (DocumentException ex) {
      throw new XSLToolsLoadException(ex);
    }
    Element e = d.getRootElement();
    triple.read(e);
    triple.applyBase(url.toString());
    triple.checkResourcesPresent();
    return triple;
  }

  public static TestTriple makeTriple(String systemId) throws XSLToolsException {
    try {
      if (!Util.isURL(systemId))
        systemId = Util.toUrlString(systemId);
      URL url = new URL(systemId);
      return makeTriple(url);
    } catch (Exception ex) {
      throw new XSLToolsException(ex);
    }
  }

  @Override
public String toString() {
    return "<triple principalStylesheetModuleURI=\"" + stylesheetPrimaryModuleURI + "\"" + " inputSchemaURI=\""
        + inputSchemaURI + "\"" + " outputSchemaURI=\"" + outputSchemaURI + "\"/>";
  }

  public static TestTriple readTriple(URL url) throws XSLToolsException {
    TestTriple triple = new TestTriple();
    SAXReader r = new SAXReader();
    Document d = null;
    try {
      d = r.read(url);
    } catch (Exception ex) {
      throw new XSLToolsLoadException(ex);
    }
    Element e = d.getRootElement();
    triple.read(e);
    triple.applyBase(url.toString());
    triple.checkResourcesPresent();
    return triple;
  }
}
