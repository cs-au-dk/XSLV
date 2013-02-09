/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-15
 */
package dongfang.xsltools.validation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Visitor;
import org.dom4j.VisitorSupport;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;

import dongfang.XMLConstants;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.simplification.StylesheetProcessor;
import dongfang.xsltools.util.Dom4jUtil;

/**
 * @author dongfang
 */
public class WebGenerator implements StylesheetProcessor {

  private static TransformerFactory tfac;

  private Transformer trafo;

  static {
    System.setProperty("javax.xml.transform.TransformerFactory",
        "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
    tfac = TransformerFactory.newInstance();
  }

  private static Visitor namespaceDeclarationShunter = new VisitorSupport() {

    @Override
	public void visit(Element node) {
      StringBuilder pre = new StringBuilder();
      StringBuilder uri = new StringBuilder();

      boolean virgin = true;

      for (Iterator it = node.nodeIterator(); it.hasNext();) {
        Object o = it.next();
        if (o instanceof Namespace) {
          Namespace ns = (Namespace) o;
          if (!virgin) {
            pre.append(",");
            uri.append(",");
          }
          virgin = false;
          pre.append(ns.getPrefix());
          uri.append(ns.getURI());
        }
      }
      if (!virgin) {
        Element element = node;
        element.addAttribute(XMLConstants.NS_PREFIX_ATTR_QNAME, pre.toString());
        element.addAttribute(XMLConstants.NS_URI_ATTR_QNAME, uri.toString());
      }
    }
  };

  public WebGenerator() {
    // does NOT intialize a stylesheet for presentation transform!
  }
  
  WebGenerator(String stylesheetName) throws XSLToolsException {
    try {
      init(stylesheetName);
    } catch (TransformerException ex) {
      throw new XSLToolsException(ex);
    }
  }

  void init(String stylesheetName) throws TransformerConfigurationException {
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        stylesheetName);
    Source stylesheetSource = new StreamSource(is);
    trafo = tfac.newTransformer(stylesheetSource);
  }

  /*
   * Transform a single module and dump result to stream. Do not close.
   */
  public void process(StylesheetModule module, int version, OutputStream os) throws XSLToolsException {
    try {
      Document d = transform(module, version);
      Dom4jUtil.debugPrettyPrint(d, os);
    } catch (Exception ex) {
      throw new XSLToolsException(ex);
    }
  }

  /*
   * Transform a single module and save result in a file
   */
  void process(StylesheetModule module, int version, File basefile)
      throws XSLToolsException {
    try {
      String filename = module.getHierarchialName() + ".html";
      File total = new File(basefile, filename);
      OutputStream os = new FileOutputStream(total);
      process(module, version, os);
      os.close();
    } catch (Exception ex) {
      throw new XSLToolsException(ex);
    }
  }

  /*
   * Transform all modules in a level and save result in a file
   */
  public void process(StylesheetLevel group, int version, File basefile)
      throws XSLToolsException {
    for (StylesheetModule module : group.contents()) {
      process(module, version, basefile);
    }

    for (StylesheetLevel imported : group.imports()) {
      process(imported, version, basefile);
    }
  }

  /*
   * Same.
   */
  public void process(StylesheetLevel group, int version)
      throws XSLToolsException {
    process(group, version, new File(DiagnosticsConfiguration.current
        .getGeneratedHTMLPrefix()));
  }

  /*
   * Same.
   */
  public void process(StylesheetLevel group) throws XSLToolsException {
    process(group, StylesheetModule.ORIGINAL);
  }

  /*
   * Transform all modules in a stylesheet and save all in files.
   */
  void process(Stylesheet stylesheet, int version, File basefile)
      throws XSLToolsException {
    process(stylesheet.getPrincipalLevel(), version, basefile);
    process((StylesheetModule) stylesheet, version, basefile);
  }

  /*
   * Same.
   */
  public void process(Stylesheet stylesheet, int version)
      throws XSLToolsException {
    process(stylesheet, version, new File(DiagnosticsConfiguration.current
        .getGeneratedHTMLPrefix()));
  }

  /*
   * Same.
   */
  public void process(Stylesheet stylesheet) throws XSLToolsException {
    process(stylesheet, StylesheetModule.ORIGINAL);
  }

  /*
   * Transform one stylesheet module; get one document.
   */
  Document transform(StylesheetModule module, int version)
      throws TransformerException {
    Document doc = module.getDocument(version);
    doc.accept(namespaceDeclarationShunter);
    Source source = new DocumentSource(doc);
    DocumentResult result = new DocumentResult();

    trafo.setParameter("this-mid", module.getHierarchialName());
    trafo.transform(source, result);

    Document resultD = result.getDocument();
    return resultD;
  }

  /*
   * Transform a stylesheet level; add all modules in level to result.
   */
  void transform(StylesheetLevel level, int version,
      Map<String, Document> result) throws TransformerException, IOException {
    for (StylesheetModule module : level.contents()) {
      result.put(module.getSystemId(), transform(module, version));
    }

    for (StylesheetLevel imported : level.imports()) {
      transform(imported, version, result);
    }
  }

  /*
   * Transform a stylesheet; return map of result
   */
  public Map<String, Document> transform(Stylesheet stylesheet, int version) 
  throws TransformerException, IOException {
    Map<String, Document> result = new HashMap<String, Document>();
    transform(stylesheet.getPrincipalLevel(), version, result);
    result.put(stylesheet.getSystemId(), transform((StylesheetModule)stylesheet, version));
    return result;
  }
}
