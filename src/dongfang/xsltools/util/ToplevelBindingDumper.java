/*
 * dongfang M. Sc. Thesis
 * Created on 2005-05-03
 */
package dongfang.xsltools.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;

import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.simplification.SimplifierConfiguration;
import dongfang.xsltools.simplification.StylesheetProcessor;

/**
 * For diagnostics. Saves top level bindings to a file.
 * @author dongfang
 */
public class ToplevelBindingDumper implements StylesheetProcessor {

  private static ToplevelBindingDumper instance = new ToplevelBindingDumper();

  public static ToplevelBindingDumper getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return instance;
  }

  public void process(Stylesheet stylesheet) throws XSLToolsException {
    DocumentFactory fac = new DocumentFactory();
    Document doc = fac.createDocument();
    stylesheet.resolutionDiagnostics(doc, fac);
    String path = SimplifierConfiguration.current.intermediateDumpPath();
    String _prefix = SimplifierConfiguration.current.intermediateDumpPrefix();
    String name = _prefix + "toplevelvars.xml";

    save(doc, path, name);

    // Dom4jUtil.debugPrettyPrint(doc);
  }

  private void save(Document doc, String path, String name) {
    try {
      String filename = path + File.separatorChar + name;
      OutputStream os = new FileOutputStream(filename);
      Dom4jUtil.debugPrettyPrint(doc, os);
      os.close();
    } catch (IOException ex) {
      System.err
          .println("Oops, top level var dump failed. Not of significance to semantics.");
    }
  }
}
