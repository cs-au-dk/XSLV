package dongfang.xsltools.xmlclass;

import dongfang.xsltools.exceptions.XSLToolsSchemaException;

/**
 * Helper class, generating streams of characters for an element and attribute
 * alphabet. Elements range in upper case letters while attributes are lower
 * case, to ease human readability. Currently fails when it runs out of letters,
 * but is easily extendable.
 */
public class CharGenerator {
  private class Ranges {
    public String str;

    public boolean rangeRunning;

    public Ranges() {
      str = "";
      rangeRunning = false;
    }
  }

  static final char MIN_ELEMENT_CHAR_1 = 'A';

  static final char MAX_ELEMENT_CHAR_1 = 'Z';

  static final char MIN_ELEMENT_CHAR_2 = '\u1000';

  static final char MAX_ELEMENT_CHAR_2 = '\u9000';

  static final char MIN_ATTRIBUTE_CHAR_1 = 'a';

  static final char MAX_ATTRIBUTE_CHAR_1 = 'z';

  static final char MIN_ATTRIBUTE_CHAR_2 = '\u9001';

  static final char MAX_ATTRIBUTE_CHAR_2 = '\uFFFE';

  private boolean closed;

  protected Ranges elementCharRanges;

  protected Ranges attributeCharRanges;

  protected char currentElementChar;

  protected char currentAttributeChar;

  public CharGenerator() {
    closed = false;
    elementCharRanges = new Ranges();
    attributeCharRanges = new Ranges();
    currentElementChar = MIN_ELEMENT_CHAR_1 - 1;
    currentAttributeChar = MIN_ATTRIBUTE_CHAR_1 - 1;
  }

  private static void addToRanges(Ranges ranges, char c) {
    if (ranges.str.length() == 0) {
      ranges.str = "" + c;
      ranges.rangeRunning = false;
    } else {
      char lastChar = ranges.str.charAt(ranges.str.length() - 1);

      if (ranges.rangeRunning) {
        // String consists of some ranges.
        if (c == lastChar + 1) {
          // Add to range:
          ranges.str = ranges.str.substring(0, ranges.str.length() - 1) + c;
          ranges.rangeRunning = true;
        } else {
          // Concat with char:
          ranges.str += c;
          ranges.rangeRunning = false;
        }
      } else {
        // String ends with a non-range.
        if (c == lastChar + 1) {
          // Start a new range:
          ranges.str += "-" + c;
          ranges.rangeRunning = true;
        } else {
          // Concat with char:
          ranges.str += c;
          ranges.rangeRunning = false;
        }
      }
    }
  }

  public char nextElementChar() throws XSLToolsSchemaException {
    if (closed)
      throw new AssertionError(
          "Tried to fetch character from closed CharGenerator stream");

    // Handle character stream:
    currentElementChar++;

    if (currentElementChar == MAX_ELEMENT_CHAR_1 + 1)
      currentElementChar = MIN_ELEMENT_CHAR_2;
    else if (currentElementChar == MAX_ELEMENT_CHAR_2 + 1)
      throw new XSLToolsSchemaException("Out of letters for element types");

    // Update ranges for regexps:
    addToRanges(elementCharRanges, currentElementChar);

    // Return char:
    return currentElementChar;
  }

  public char nextAttributeChar() throws XSLToolsSchemaException {
    if (closed)
      throw new AssertionError(
          "Tried to fetch character from closed CharGenerator stream");

    // Handle character stream:
    currentAttributeChar++;

    if (currentAttributeChar == MAX_ATTRIBUTE_CHAR_1 + 1)
      currentAttributeChar = MIN_ATTRIBUTE_CHAR_2;
    else if (currentAttributeChar == MAX_ATTRIBUTE_CHAR_2 + 1)
      throw new XSLToolsSchemaException("Out of letters for attribute types");

    // Update ranges for regexps:
    addToRanges(attributeCharRanges, currentAttributeChar);

    // Return char:
    return currentAttributeChar;
  }

  public static final char getRootChar() {
    return '0';
  }

  public static final char getPCDATAChar() {
    return '_';
  }

  public static final char getCommentChar() {
    return '1';
  }

  public static final char getPIChar() {
    return '2';
  }

  public static final char getAbsurdChar() {
    return '!';
  }

  public String getElementRegExp() {
    closed = true;

    return "[" + elementCharRanges.str + "]";
  }

  public String getAttributeRegExp() {
    closed = true;
    return "[" + attributeCharRanges.str + "]";
  }

  /*
   * public static Node constructOptionalSG(Graph sg, Node subSGNode) throws
   * XSLToolsMalformedXMLException { Node node; // Construct SG node: String
   * templateStr = "<sg:optional/>\n";
   * 
   * node = ControlFlowFunctions.addSGNode(sg, templateStr, "?"); // Add SG
   * edge: TemplateEdge sgEdge = new TemplateEdge(subSGNode, "optional",
   * "optional"); node.addTemplateEdge(sgEdge); // Add empty node and edge:
   * templateStr = "";
   * 
   * Node localEmpty = ControlFlowFunctions.addSGNode(sg, templateStr, "e");
   * 
   * sgEdge = new TemplateEdge(localEmpty, "optional", "optional");
   * node.addTemplateEdge(sgEdge);
   * 
   * return node; }
   * 
   * public static Node constructOneManySG(Graph sg, Node subSGNode) throws
   * XSLToolsMalformedXMLException { Node node; // Construct SG node: String
   * templateStr = "<sg:onemany/>\n<sg:more/>\n";
   * 
   * node = ControlFlowFunctions.addSGNode(sg, templateStr, "+"); // Add repeat
   * edge: TemplateEdge sgEdge = new TemplateEdge(node, "more", "more");
   * node.addTemplateEdge(sgEdge); / * Node localEmpty =
   * ControlFlowFunctions.addSGNode(sg, "", "e"); sgEdge = new
   * TemplateEdge(localEmpty, "more", "more"); node.addTemplateEdge(sgEdge); / //
   * Add SG edge: sgEdge = new TemplateEdge(subSGNode, "onemany", "onemany");
   * node.addTemplateEdge(sgEdge);
   * 
   * return node; }
   */
}
