package dongfang.xsltools.configuration;

public class FormattingConfiguration {
  public static final int KILL_WHITESPACEONLY_TEXTNODES = 0;

  public static final int PRESERVE_WHITESPACEONLY_TEXTNODES = 1;

  private int whitespaceOnlyTextNodeBevaviour = PRESERVE_WHITESPACEONLY_TEXTNODES;

  private String indentationString = "";

  private boolean addNewlines = false;

  public FormattingConfiguration(int whitespaceOnlyTextNodeBevaviour,
      String indentationString, boolean addNewLines) {
    this.whitespaceOnlyTextNodeBevaviour = whitespaceOnlyTextNodeBevaviour;
    this.indentationString = indentationString;
    this.addNewlines = addNewLines;
  }

  public int getWhitespaceOnlyTextNodeBevaviour() {
    return whitespaceOnlyTextNodeBevaviour;
  }

  public String getIndentationString() {
    return indentationString;
  }

  public boolean addNewlines() {
    return addNewlines;
  }
}
