/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xslt;

import java.io.Reader;
import java.io.StringReader;

/**
 * @author dongfang
 */
public class DefaultXSLT1Rules {
  private static String it = "<?xml version='1.0' encoding='utf-8'?>\n"
      + "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>\n"
      + "<xsl:template match='*|/' mode='#all'>\n"
      + " <xsl:apply-templates mode='#current'/>\n"
      + "</xsl:template>\n"
      + "<xsl:template match='text()|@*' mode='#all'>\n"
      + "  <xsl:value-of select='.'/>\n"
      + "</xsl:template>\n"
      + "<xsl:template match='processing-instruction()|comment()' mode='#all'/>\n"
      + "</xsl:stylesheet>\n";

  public static Reader getReader() {
    return new StringReader(it);
  }
}
