package dongfang.xsltools.diagnostics;

import dk.brics.misc.Origin;

/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */

/**
 * @author dongfang
 */
public class ParseLocationBean implements ParseLocation {

  int elementStartTagBeginningLine;

  int elementStartTagBeginningColumn;

  int elementStartTagEndLine;

  int elementStartTagEndColumn;

  int elementEndTagEndLine;

  int elementEndTagEndColumn;

  public ParseLocationBean() {
  }

  public ParseLocationBean(Origin o) {
    this.elementStartTagBeginningLine = o.getLine();
    this.elementStartTagEndColumn = o.getColumn();
  }

  public ParseLocationBean(int elementStartTagBeginningLine,
      int elementStartTagBeginningColumn) {
    this.elementStartTagBeginningLine = elementStartTagBeginningLine;
    this.elementStartTagBeginningColumn = elementStartTagBeginningColumn;
  }

  public ParseLocationBean(int elementStartTagBeginningLine,
      int elementStartTagBeginningColumn, int elementStartTagEndLine,
      int elementStartTagEndColumn) {
    this(elementStartTagBeginningLine, elementStartTagBeginningColumn);
    this.elementStartTagEndLine = elementStartTagEndLine;
    this.elementStartTagEndColumn = elementStartTagEndColumn;
  }

  public void setElementEndTagEndLine(int i) {
    this.elementEndTagEndLine = i;
  }

  public void setElementEndTagEndColumn(int i) {
    this.elementEndTagEndColumn = i;
  }

  public int elementStartTagBeginningLine() {
    return elementStartTagBeginningLine;
  }

  public int elementStartTagBeginningColumn() {
    return elementStartTagBeginningColumn;
  }

  public int elementStartTagEndLine() {
    return elementStartTagEndLine;
  }

  public int elementStartTagEndColumn() {
    return elementStartTagEndColumn;
  }

  public int elementEndTagEndLine() {
    return elementEndTagEndLine;
  }

  public int elementEndTagEndColumn() {
    return elementEndTagEndColumn;
  }

  @Override
public String toString() {
    return elementStartTagBeginningLine + "," + elementStartTagBeginningColumn
        + "," + elementStartTagEndLine + "," + elementStartTagEndColumn + ","
        + elementEndTagEndLine + "," + elementEndTagEndColumn;
  }

  public String toReadableString(Extent extent) {
    if (extent == Extent.TAG)
      return "(line " + elementStartTagBeginningLine + ", column "
          + elementStartTagBeginningColumn + ")" + " - (line "
          + elementStartTagEndLine + ", column " + elementStartTagEndColumn
          + ")";
    if (extent == Extent.ELEMENT)
      return "(line " + elementStartTagBeginningLine + ", column "
          + elementStartTagBeginningColumn + ")" + " - (line "
          + elementEndTagEndLine + ", column " + elementEndTagEndColumn + ")";
    return "(@line " + elementStartTagBeginningLine + ", column "
        + elementStartTagBeginningColumn + ")";
  }
}