/* Generated By:JJTree: Do not edit this line. XPathAxisStep.java */

package dongfang.xsltools.xpath2;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.PINT;
import dongfang.xsltools.xmlclass.xslside.RootNT;
import dongfang.xsltools.xmlclass.xslside.TextNT;

public class XPathAxisStep extends XPathPredicateCarrier implements
    XPathStepExpr {
  public static final short CHILD = 10;

  public static final short ATTRIBUTE = 20;

  public static final short DESCENDANT = 30;

  public static final short DESCENDANT_OR_SELF = 40;

  public static final short ANCESTOR_OR_SELF = 50;

  public static final short FOLLOWING_SIBLING = 60;

  public static final short SIBLING = 70;

  public static final short PRECEDING_SIBLING = 80;

  public static final short ANCESTOR = 90;

  public static final short PARENT = 100;

  public static final short NAMESPACE = 110;

  public static final short SELF = 120;

  public static final short FOLLOWING = 130;

  public static final short PRECEDING = 140;

  short axis;

  public XPathAxisStep(int id) {
    super(id);
  }

  public XPathAxisStep(XPathParser p, int id) {
    super(p, id);
  }

  public final String axisToString() {
    return axisToString(axis);
  }

  public XPathAxisStep(short axis, XPathNodeTest nt) {
    this(0);
    this.axis = axis;
    jjtAddChild(nt, 0);
    jjtAddChild(new XPathPredicateList(0), 1);
    // addEmptyPredicateList();
  }

  public static final String axisToString(short axis) {
    switch (axis) {
    case CHILD:
      return "child";
    case ATTRIBUTE:
      return "attribute";
    case DESCENDANT:
      return "descendant";
    case DESCENDANT_OR_SELF:
      return "descendant-or-self";
    case ANCESTOR_OR_SELF:
      return "ancestor-or-self";
    case FOLLOWING_SIBLING:
      return "following-sibling";
    case SIBLING:
      return "sibling";
    case PRECEDING_SIBLING:
      return "preceding-sibling";
    case ANCESTOR:
      return "ancestor";
    case PARENT:
      return "parent";
    case NAMESPACE:
      return "namespace";
    case SELF:
      return "self";
    case FOLLOWING:
      return "following";
    case PRECEDING:
      return "preceding";
    default:
      return "???";
    }
  }

  void setAxis(short axis) {
    this.axis = axis;
  }

  public short getAxis() {
    return axis;
  }

  public XPathNodeTest getNodeTest() {
    return (XPathNodeTest) jjtGetChild(0);
  }

  public void setNodeTest(XPathNodeTest t) {
    jjtAddChild(t, 0);
  }

  @Override
  public boolean similarTo(XPathBase that) {
    if (!(that instanceof XPathAxisStep))
      return false;
    return axis == ((XPathAxisStep) that).axis && super.similarTo(that);
  }

  public void copyPredicatesFrom(XPathAxisStep other) {
    Node myPredicateList = jjtGetChild(1);
    Node otherPredicateList = other.jjtGetChild(1);
    for (int i = 0; i < otherPredicateList.jjtGetNumChildren(); i++) {
      myPredicateList.jjtAddChild(otherPredicateList.jjtGetChild(i), i);
    }
  }

  public boolean accept(ElementUse nt, SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    return getNodeTest().accept(nt, clazz);
  }

  public boolean accept(AttributeUse nt, SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    return getNodeTest().accept(nt, clazz);
  }

  public boolean accept(CommentNT nt) {
    return getNodeTest().accept(nt);
  }

  public boolean accept(PINT nt) {
    return getNodeTest().accept(nt);
  }

  public boolean accept(RootNT nt, SingleTypeXMLClass clazz) {
    return getNodeTest().accept(nt, clazz);
  }

  public boolean accept(TextNT nt) {
    return getNodeTest().accept(nt);
  }

  public static boolean isReverse(short axis) {
    return axis == ANCESTOR || axis == ANCESTOR_OR_SELF || axis == PRECEDING
        || axis == PRECEDING_SIBLING;
  }

  public static boolean isStrictlyDownward(short axis) {
    return axis == CHILD || axis == DESCENDANT || axis == ATTRIBUTE;
  }

  public static boolean isStrictlyDownwardOrSelf(short axis) {
    return isStrictlyDownward(axis) || axis == SELF
        || axis == DESCENDANT_OR_SELF;
  }

  public static boolean isDownward(short axis) {
    return isStrictlyDownward(axis) || axis == SELF
        || axis == DESCENDANT_OR_SELF;
  }

  public static boolean isStrictlyUpward(short axis) {
    return axis == PARENT || axis == ANCESTOR;
  }

  public static boolean isUpward(short axis) {
    return isStrictlyUpward(axis) || axis == SELF || axis == ANCESTOR_OR_SELF;
  }

  public static boolean isReflective(short axis) {
    return axis == SELF || axis == ANCESTOR_OR_SELF
        || axis == DESCENDANT_OR_SELF || axis == SIBLING;
  }

  public boolean isReverse() {
    return isReverse(axis);
  }

  public boolean isStrictyDownward() {
    return isStrictlyDownward(axis);
  }

  public boolean isDownward() {
    return isDownward(axis);
  }

  public boolean isStrictlyUpward() {
    return isStrictlyUpward(axis);
  }

  public boolean isUpward() {
    return isUpward(axis);
  }

  void fixupDefaultAxis() {
    if (jjtGetChild(0) instanceof XPathAttributeTest
        || jjtGetChild(0) instanceof XPathSchemaAttributeTest)
      axis = ATTRIBUTE;
    else
      axis = CHILD;
  }

  public short getType() {
    return TYPE_NODELIST;
  }

  @Override
public Object clone() {
    return super.clone();
  }

  void moreDiagnostics(Element me, DocumentFactory fac) {
    me.addAttribute("axis", axisToString());
  }

  @Override
public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(axisToString());
    result.append("::");
    result.append(jjtGetChild(0).toString());
    result.append(jjtGetChild(1).toString());
    return result.toString();
  }
}
