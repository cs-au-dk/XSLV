/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.model;

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;

import dongfang.xsltools.diagnostics.ParseLocation;

/**
 * Element specialization with parse locations added.
 * 
 * @author dongfang
 */
public class ParseLocationElement extends DefaultElement implements
    ParseLocation {

  /**
	 * 
	 */
	private static final long serialVersionUID = -3349773672935258103L;

public ParseLocationElement(QName qname, int attributeCount) {
    super(qname, attributeCount);
  }

  public ParseLocationElement(QName qname) {
    super(qname);
  }

  public ParseLocationElement(String name, Namespace namespace) {
    super(name, namespace);
  }

  public ParseLocationElement(String name) {
    super(name);
  }

  int elementStartTagBeginningLine;

  int elementStartTagBeginningColumn;

  int elementStartTagEndLine;

  int elementStartTagEndColumn;

  int elementEndTagEndLine;

  int elementEndTagEndColumn;

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

  public String toReadableString(Extent extent) {
    if (extent == Extent.TAG)
      return "(line " + elementStartTagBeginningLine + ", column "
          + elementStartTagBeginningColumn + ")" + " - (line "
          + elementStartTagEndLine + ", column " + elementStartTagEndColumn
          + ")";
    return "(line " + elementStartTagBeginningLine + ", column "
        + elementStartTagBeginningColumn + ")" + " - (line "
        + elementEndTagEndLine + ", column " + elementEndTagEndColumn + ")";
  }
}
