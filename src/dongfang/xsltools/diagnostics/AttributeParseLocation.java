package dongfang.xsltools.diagnostics;

/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
import java.util.StringTokenizer;

/**
 * @author dongfang
 */
public class AttributeParseLocation extends ParseLocationBean {
  public AttributeParseLocation(String serialized) {
    StringTokenizer tknzr = new StringTokenizer(serialized, ",");
    elementStartTagBeginningLine = Integer.parseInt(tknzr.nextToken());
    elementStartTagBeginningColumn = Integer.parseInt(tknzr.nextToken());
    elementStartTagEndLine = Integer.parseInt(tknzr.nextToken());
    elementStartTagEndColumn = Integer.parseInt(tknzr.nextToken());
    elementEndTagEndLine = Integer.parseInt(tknzr.nextToken());
    elementEndTagEndColumn = Integer.parseInt(tknzr.nextToken());
  }

  public AttributeParseLocation(int elementStartTagBeginningLine,
      int elementStartTagBeginningColumn, int elementStartTagEndLine,
      int elementStartTagEndColumn) {
    super(elementStartTagBeginningLine, elementStartTagBeginningColumn,
        elementStartTagEndLine, elementStartTagEndColumn);
  }
}