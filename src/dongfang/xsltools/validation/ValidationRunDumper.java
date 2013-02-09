package dongfang.xsltools.validation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import dk.brics.xmlgraph.XMLGraph;
import dk.brics.xmlgraph.converter.Serializer;
import dk.brics.xmlgraph.converter.XMLGraph2Dot;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

/**
 * A ValidationRun that dumps temporary structures into files.
 * This dumps lots of stuff:
 *
 * The control flow DOT files, as <code>prefix</code>.xcfg.modecompatibility.dot
 * , <code>prefix</code>.xcfg.schemaless.dot and 
 * and <code>prefix</code>.xcfg.dot
 * 
 * The control flow XML file, as <code>prefix</code>.xcfg.xml
 * The control flow XML Graph in XML, as <code>prefix</code>.xcfg-xmlgraph.xml

 * The output XML Graph in XML, as <code>prefix</code>.output-schema.xmlgraph.dot
 * The output XML Graph in XML, as <code>prefix</code>.output-schema-xmlgraph.xml
 */
public class ValidationRunDumper extends LightValidationRunDumper {

  /**
   * @param decorated
   * @param prefix
   */
  public ValidationRunDumper(ValidationRun decorated, String prefix) {
    super(decorated, prefix);
  }

  static void dumpControlFlowDots(ControlFlowGraph xcfg, String prefix) {
    if (xcfg == null) {
      System.err.println("Cannot dump xcfg; it is null");
    } else {
      try {
        xcfg.saveModeCompatibilityDot(prefix + ".xcfg.modecompatibility.dot");
      } catch (IOException ex) {
        System.err.println("Verdammt, IOException " + ex);
      }

      try {
        xcfg.saveSchemalessDot(new FileWriter(prefix + ".xcfg.schemaless.dot"));
      } catch (IOException ex) {
        System.err.println("Verdammt, IOException " + ex);
      }

      try {
        xcfg.saveDot(new FileWriter(prefix + ".xcfg.dot"));
      } catch (IOException ex) {
        System.err.println("Verdammt, IOException " + ex);
      }
    }
  }

  static void dumpInputTypeDiagnostics(SingleTypeXMLClass it, String filename) {
    if (it == null) {
      System.err.println("Cannot dump input type diags; it is null");
    } else {
      String itDump = Dom4jUtil.diagnostics(it);
      try {
        Writer dumpFileW = new FileWriter(filename);
        dumpFileW.write(itDump);

        dumpFileW.close();
      } catch (IOException ex) {
        System.err.println("Verdammt, IOException " + ex);
      }
    }
  }

  static void dumpControlFlowDiagnostics(ControlFlowGraph xcfg, String filename) {
    if (xcfg == null) {
      System.err.println("Cannot dump xcfg diags; it is null");
    } else {
      String xcfgDump = Dom4jUtil.diagnostics(xcfg);
      try {
        Writer dumpFileW = new FileWriter(filename);
        dumpFileW.write(xcfgDump);
        dumpFileW.close();
      } catch (IOException ex) {
        System.err.println("Verdammt, IOException " + ex);
      }
    }
  }

  public static void dumpSummaryGraphDot(XMLGraph sg, String filename) {
    if (sg == null) {
      System.err.println("Cannot dump sg; it is null");
    } else {
      try {
        PrintWriter ps = new PrintWriter(filename);
        XMLGraph2Dot dotter = new XMLGraph2Dot(ps);
        dotter.print(sg);
        ps.close();
      } catch (IOException ex) {
        System.err.println("Verdammt, IOException " + ex);
      }
    }
  }

  static void dumpSummaryGraphXML(XMLGraph sg, String dirname) {
    if (sg == null) {
      System.err.println("Cannot dump sg; it is null");
    } else {
      File f = new File(dirname);
      f.mkdir();
      Serializer toXML = new Serializer();
      // Document out = toXML.convert(sg, true);
      try {
        toXML.store(sg, dirname, true);
        /*
         * XMLOutputter putte = new XMLOutputter(Format.getPrettyFormat());
         * OutputStream os = new FileOutputStream(filename); putte.output(out,
         * os); os.close();
         */
      } catch (IOException ex) {
        System.err.println("Verdammt, Exception " + ex);
      }
    }
  }

  @Override
  public void setControlFlowSG(XMLGraph controlFlowSG) {
    super.setControlFlowSG(controlFlowSG);
    dumpSummaryGraphDot(controlFlowSG, prefix + ".xmlgraph.dot");
    dumpSummaryGraphXML(controlFlowSG, prefix + ".xcfg-xmlgraph.xml");
  }

  @Override
  public void setInputType(SingleTypeXMLClass inputType) {
    super.setInputType(inputType);
    dumpInputTypeDiagnostics(inputType, prefix
        + ".input-schema-diagnostics.xml");
  }

  @Override
  public void setOutputType(XMLGraph outputType) {
    super.setOutputType(outputType);
    dumpSummaryGraphDot(outputType, prefix + ".output-schema.xmlgraph.dot");
    dumpSummaryGraphXML(outputType, prefix + ".output-schema-xmlgraph.xml");
  }

  @Override
  public void setXcfg(ControlFlowGraph xcfg) {
    super.setXcfg(xcfg);
    dumpControlFlowDiagnostics(xcfg, prefix + ".xcfg.xml");
    dumpControlFlowDots(xcfg, prefix);
  }
}
