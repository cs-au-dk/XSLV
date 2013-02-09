package dongfang.xsltools.diagnostics;

import org.dom4j.Element;

import dk.brics.misc.Origin;
import dongfang.XMLConstants;
import dongfang.xsltools.model.ParseLocationElement;
import dongfang.xsltools.model.StylesheetModule;

public class ParseLocationUtil {
  public static ParseLocation getParseLocation(Element originalElement) {
    if (originalElement == null)
      return null;
    String parsePosStr = originalElement
        .attributeValue(XMLConstants.PARSELOCATION_DECORATION_QNAME);
    if (parsePosStr != null) {
      return new AttributeParseLocation(parsePosStr);
    }

    else if (originalElement instanceof ParseLocationElement)
      return (ParseLocation) originalElement;

    return null;
  }

  public static Origin getOrigin(String file, Element originalElement) {
    ParseLocation pl = getParseLocation(originalElement);
    if (pl == null)
      return null;
    return new Origin(file, pl.elementStartTagBeginningLine(), pl
        .elementStartTagBeginningColumn());
  }

  public static Origin getOrigin(String file, Element originalElement,
      String backupstr) {
    ParseLocation pl = getParseLocation(originalElement);
    if (pl == null)
      return new Origin(backupstr, 0, 0);
    return new Origin(file, pl.elementStartTagBeginningLine(), pl
        .elementStartTagBeginningColumn());
  }

  public static Origin getOrigin(Origin file, Element originalElement,
      String backupstr) {
    ParseLocation pl = getParseLocation(originalElement);
    if (pl == null)
      return new Origin(backupstr, 0, 0);
    return new Origin(file.getFile(), pl.elementStartTagBeginningLine(), pl
        .elementStartTagBeginningColumn());
  }

  public static Origin getOrigin(StylesheetModule module,
      String originalElementId, String bup) {
    Element o = module.getElementById(originalElementId,
        StylesheetModule.ORIGINAL);
    return getOrigin(module.getSystemId(), o, bup);
  }

  public static ParseLocation getParseLocation(Origin o) {
    ParseLocation result = new ParseLocationBean(o.getLine(), o.getColumn());
    return result;
  }
  
  public static boolean inside(int line, int col, ParseLocation location) {
    if (line < location.elementStartTagBeginningLine())
      return false;
    if (line > location.elementEndTagEndLine())
      return false;
    if (line > location.elementStartTagBeginningLine() && line < location.elementEndTagEndLine())
      return true;
    if (line == location.elementStartTagBeginningColumn() && line == location.elementEndTagEndLine()) {
      return col >= location.elementStartTagBeginningColumn() && col <= location.elementEndTagEndColumn();
    }
    if (line == location.elementStartTagBeginningLine())
      return col >= location.elementStartTagBeginningColumn();
      return col <= location.elementEndTagEndColumn();
  }
  
  public static boolean insideStartTag(int line, int col, ParseLocation location) {
    if (line < location.elementStartTagBeginningLine())
      return false;
    if (line > location.elementStartTagEndLine())
      return false;
    if (line > location.elementStartTagBeginningLine() && 
        line < location.elementStartTagEndLine())
      return true;
    if (line == location.elementStartTagBeginningColumn() && 
        line == location.elementStartTagEndLine()) {
      return col >= location.elementStartTagBeginningColumn() && 
      col <= location.elementStartTagEndColumn();
    }
    if (line == location.elementStartTagBeginningLine())
      return col >= location.elementStartTagBeginningColumn();
      return col <= location.elementStartTagEndColumn();
  }

}
