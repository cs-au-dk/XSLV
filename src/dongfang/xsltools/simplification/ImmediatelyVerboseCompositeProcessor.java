/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-21
 */
package dongfang.xsltools.simplification;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.dom4j.Document;

import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * A CompositeProcessor that dumps the primary stylesheet module into files
 * after each single simplification step. DONE: Controllable by configuration,
 * or merge with CompositeProcessor
 * 
 * @author dongfang
 */
public class ImmediatelyVerboseCompositeProcessor extends CompositeProcessor {
  private int simplifier_no = 0;

  protected static CompositeProcessor getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new ImmediatelyVerboseCompositeProcessor(cesspool, names);
  }

  /**
   * @param cesspool
   * @param names
   */
  public ImmediatelyVerboseCompositeProcessor(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    super(cesspool, names);
  }

  /*
   * (non-Javadoc)
   * 
   * @see dongfang.xsltools.simplification.CompositeSimplifier#process(dongfang.xsltools.simplification.StylesheetProcessor,
   *      dongfang.xsltools.simplification.Stylesheet)
   */
  @Override
protected void process(StylesheetProcessor proc, Stylesheet stylesheet)
      throws XSLToolsException {

    String path = SimplifierConfiguration.current.intermediateDumpPath();

    String _prefix = SimplifierConfiguration.current.intermediateDumpPrefix();

    String processorname = _prefix + simplifier_no + "("
        + proc.getClass().getSimpleName() + ")";

    boolean dumpOriginal = SimplifierConfiguration.current
        .alsoDumpOriginalDocument();

    Document document = stylesheet.getPrincipalModule().getDocument(
        StylesheetModule.CORE);

    if (proc.getClass() != ElementIdentityChecker.class) {

      save(document, path, processorname, "simplified", "before");

      if (dumpOriginal) {
        document = stylesheet.getPrincipalModule().getDocument(
            StylesheetModule.ORIGINAL);
        save(document, path, processorname, "original", "before");
      }
    }

    super.process(proc, stylesheet);

    if (proc.getClass() != ElementIdentityChecker.class) {

      document = stylesheet.getPrincipalModule().getDocument(
          StylesheetModule.CORE);
      save(document, path, processorname, "simplified", "post");

      if (dumpOriginal) {
        document = stylesheet.getPrincipalModule().getDocument(
            StylesheetModule.ORIGINAL);
        save(document, path, processorname, "original", "post");

      }
      simplifier_no++;
    }
  }

  private void save(Document document, String prefix, String processorname,
      String type, String ba) {

    try {
      String filename = prefix + File.separatorChar + processorname + "."
          + type + "." + ba + ".xml";
      OutputStream os = new FileOutputStream(filename);
      Dom4jUtil.debugPrettyPrint(document, os);
      os.close();
    } catch (IOException ex) {
      System.err
          .println(getClass().getSimpleName()
              + ": Oops, immediate verbose save failed. Not of significance to semantics ("
              + ex.getMessage() + ")");
    }
  }
}
