/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-15
 */
package dongfang.xsltools.experimental.homemade_xpath;

import java.util.Iterator;

import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathParser;
import dongfang.xsltools.xpath2.XPathPathExpr;
import dongfang.xsltools.xpath2.XPathStepExpr;

/**
 * @author snk
 */
public class NormalizeExperiment {
  static void bumm() {
    new Thread() {
      @Override
	public void run() {
        try {
          XPathExpr exp = XPathParser.parse("..");
          XPathPathExpr pa = (XPathPathExpr) exp;
          Iterator<XPathStepExpr> i = pa.reverseSteps();
          while (i.hasNext()) {
            System.out.println(i.next());
          }

        } catch (Exception ex) {
          System.err.println(ex);
        }
      }
    }.start();
  }

  public static void main(String[] args) throws Exception {
    // XPathExp exp = XPathExp.parse("//a/b/[@c=$x]"); // smutter: Denne
    // syntaktiske fejl giver en NullpointerException
    // XPathExpr exp = XPathParser.parse("string(self::node())");
    // String fido = exp.toString();
    // System.out.println(fido);
    // System.out.println(exp.getClass());
    bumm();
    for (long i = 0; i < 100000000; i++)
      ;
  }
}