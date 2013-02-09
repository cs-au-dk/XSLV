/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.diagnostics;

/**
 * @author dongfang
 */
public interface ParseLocation {

  public enum Extent {
    POINT, TAG, ELEMENT
  };

  int elementStartTagBeginningLine();

  int elementStartTagBeginningColumn();

  int elementStartTagEndLine();

  int elementStartTagEndColumn();

  int elementEndTagEndLine();

  int elementEndTagEndColumn();

  String toReadableString(Extent extent);
}