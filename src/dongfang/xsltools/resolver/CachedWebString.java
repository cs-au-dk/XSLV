/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.resolver;

/**
 * @author dongfang
 */
public class CachedWebString {
  String string = "";
  String defaultValue;

  public CachedWebString(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public CachedWebString(String string, String defaultValue) {
    this(defaultValue);
    this.string = string;
  }

  public String getString() {
    return toString();
  }

  @Override
public String toString() {
    return string;
  }
}
