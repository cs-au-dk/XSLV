/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.resolver;

import java.io.IOException;
import java.io.Reader;

import org.xml.sax.InputSource;

import dongfang.xsltools.context.ValidationContext;

/**
 * @author dongfang
 */
public class CachedWebResource implements Comparable<CachedWebResource> {
  String abstractName;

  InputSource inputSource;

  String inputReference = "";

  String inputMethod;

  public CachedWebResource(InputSource inputSource) {
    this.inputSource = inputSource;
    this.inputMethod = WebConstants.TEXT;
  }

  public CachedWebResource(String referenceName, InputSource inputSource, String inputReference, String inputMethod) {
    this.abstractName = referenceName;
    this.inputSource = inputSource;
    this.inputReference = inputReference;
    this.inputMethod = inputMethod;
  }

  public InputSource getInputSource() {
    return inputSource;
  }

  public String getInputMethod() {
    return inputMethod;
  }

  public String getContents() {
    StringBuilder sbuf = new StringBuilder();
    char[] buf = new char[1024];
    Reader reader = inputSource.getCharacterStream();
    int i;
    try {
      while ((i = reader.read(buf)) > 0) {
        sbuf.append(buf, 0, i);
      }
      reader.close();
    } catch (IOException ex) {
    	return ex.toString();
    }
    return sbuf.toString();
  }

  public String getInputReference() {
    return inputReference;
  }

  public String getSystemId() {
    return getInputSource().getSystemId();
  }

  public String getAbstractName() {
    return abstractName;
  }

  int getSpecialNameScore() {
    if (abstractName
        .contains(ResolutionContext.HUMAN_INTERFACE_STRINGS[ResolutionContext.STYLESHEET_PRINCIPAL_MODULE_IDENTIFIER_KEY]))
      return -100;

    if (abstractName
        .contains(ResolutionContext.HUMAN_INTERFACE_STRINGS[ResolutionContext.INPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY]))
    return -80;

    if (abstractName
        .contains(ResolutionContext.HUMAN_INTERFACE_STRINGS[ResolutionContext.OUTPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY]))
    return -60;
    
    return 0;
  }

  /*
   * Simple implementation: Just use names.
   * 1) Anything with "principal-module" in it is advanced before anything else.
   * 2) Anything with "input-schema" in it is advanced before anything else except 1)
   * 3) Anything with "input-schema" in it is advanced before anything else except 1), 2)
   */
  public int compareTo(CachedWebResource o) {
    int mySpecial = getSpecialNameScore();
    int hisSpecial = o.getSpecialNameScore();
    if (mySpecial != hisSpecial)
      return mySpecial - hisSpecial;
    
    return abstractName.compareTo(o.abstractName);
  }
}
