package dongfang.xsltools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.xsltools.validation.TestTripleRunner;

public class ResourceMover {
  public static void main(String[] args) {
    TestTripleRunner r = new TestTripleRunner();

    File base = new File(args[0]);

    File out = new File(args[1]);

    File[] files = base.listFiles();

    for (File f : files) {
      if (f.getName().toLowerCase().endsWith("xml")) {
        if (r.isGoodTriple(f)) {
          try {
            TestTriple t = TestTriple.makeTriple(f.toURL());
            // System.out.println(t);
            // String i = t.getName();
            // System.out.print(f.getName());
            // System.out.print("\t");
            String n = f.getName().substring(0, f.getName().length() - 4);
            // System.out.println(n);
            t.setName(n);
            File lout = new File(out, n);
            lout.mkdir();

            String absStm = new URL(t.getStylesheetPrimaryModuleURI())
                .getFile();
            String relStm = new File(absStm).getName();
            t.setStylesheetPrimaryModuleURI(relStm);

            String absIn = new URL(t.getInputSchemaURI()).getFile();
            String relIn = new File(absIn).getName();
            t.setInputSchemaURI(relIn);

            String absOut = new URL(t.getOutputSchemaURI()).getFile();
            String relOut = new File(absOut).getName();
            t.setOutputSchemaURI(relOut);

            File llout = new File(lout, "triple.xml");
            Element e = t.toDOM();
            Document d = new DocumentFactory().createDocument(e);
            OutputStream os = new FileOutputStream(llout);
            // System.out.println(t.getStylesheetPrimaryModuleURI());
            // System.out.println(f.getParentFile().toURL());

            Dom4jUtil.prettyPrint(d, os);

            os.close();

            copy(absStm, lout, relStm);

            copy(absIn, lout, relIn);

            copy(absOut, lout, relOut);

          } catch (Exception ex) {
            System.out.println(ex);
          }
        }
      }
    }
  }

  static void copy(String in, File outDir, String out) throws IOException {
    byte[] buf = new byte[512];
    InputStream is = new FileInputStream(in);
    File outf = new File(outDir, out);
    OutputStream outs = new FileOutputStream(outf);
    int i;
    while ((i = is.read(buf)) > 0) {
      outs.write(buf, 0, i);
    }
    is.close();
    outs.close();
  }
}
