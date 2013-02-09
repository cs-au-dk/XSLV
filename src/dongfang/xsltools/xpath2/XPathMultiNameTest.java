package dongfang.xsltools.xpath2;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.CharGenerator;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.AttributeNT;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.ElementNT;
import dongfang.xsltools.xmlclass.xslside.NodeType;
import dongfang.xsltools.xmlclass.xslside.PINT;
import dongfang.xsltools.xmlclass.xslside.RootNT;
import dongfang.xsltools.xmlclass.xslside.TextNT;
import dongfang.xsltools.xmlclass.xslside.UndeclaredNodeType;

/*
 * This is NOT a representation of any real XPath, and this is never instantiated
 * by the XPath parser. It represents a name test, where several names are possible.
 * If XPath has such a feature, it might have looked like the 2nd step in:
 * a/(b1|b2|b3)/c
 * It is used for representing all the elements that can own an attribute, by
 * multiple references to the attribute's declaration. It is thus possible to
 * distinguish between the two attribute decls a in:
 * &lt;attribute name="a" type="t1"/&gt;
 * &lt;element name="e"&gt;
 *   &lt;attribute name="a" type="t2"&gt;
 * &lt;/element&gt;
 * 
 * because the local "a" attribute will have the declaration of "e" as owner,
 * whereas the other "a" attribute will have some other owners.
 */
public class XPathMultiNameTest extends XPathNodeTest {
  public XPathMultiNameTest(int id) {
    super(id);
  }

  public XPathMultiNameTest(XPathParser p, int id) {
    super(p, id);
  }

  public XPathMultiNameTest() {
    this(0);
    matchedNames = new HashSet<QName>();
  }

  public XPathMultiNameTest(Set<QName> names) {
    this();
    this.matchedNames = names;
  }

  Set<QName> matchedNames;

  @Override
public Set<UndeclaredNodeType> etest(Set<UndeclaredNodeType> delta) {
    Set<UndeclaredNodeType> testers = new HashSet<UndeclaredNodeType>();
    for (QName q : matchedNames)
      testers.add(new ElementNT(q));
    if (delta.contains(NodeType.ONE_ANY_NAME_ELEMENT_NT))
      return testers;
    testers.retainAll(delta);
    return testers;
  }

  @Override
public Set<UndeclaredNodeType> atest(Set<UndeclaredNodeType> delta) {
    Set<UndeclaredNodeType> testers = new HashSet<UndeclaredNodeType>();
    for (QName q : matchedNames)
      testers.add(new AttributeNT(q));
    if (delta.contains(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT))
      return testers;
    testers.retainAll(delta);
    return testers;
  }

  private boolean t(QName name) {
    String ntLocal = name.getName();
    String ntURI = name.getNamespaceURI();
    if (matchedNames.isEmpty())
      return false;

    // case :{*}*, anything goes
    if (ntLocal.equals("*") && ntURI.equals("*"))
      return true;

    // case{foo}bar, only a hit is good enough
    // (we assume no wildcards in matchedNames)
    if (!ntLocal.equals("*") && !ntURI.equals("*")) {
      for (QName n : matchedNames) {
        if (n.equals(name))
          
        return true;
      }
      return false;
    }

    if (ntLocal.equals("*")) {
      for (QName n : matchedNames) {
        if (ntURI.equals(n.getNamespaceURI()))
          return true;
      }
      return false;
    }

    if (ntURI.equals("*")) {
      for (QName n : matchedNames) {
        if (ntLocal.equals(n.getName()))
          return true;
      }
      return false;
    }

    throw new AssertionError("All possibilities should have been exhausted");
  }

  @Override
  public boolean accept(AttributeUse nt, SingleTypeXMLClass clazz) {
    // String ntLocal = nt.getQName().getName();
    // String ntURI = nt.getQName().getNamespaceURI();
    return t(nt.getQName());
  }

  @Override
  boolean accept(CommentNT nt) {
    return false;
  }

  @Override
  public boolean accept(ElementUse nt, SingleTypeXMLClass clazz) {
    // String ntLocal = nt.getQName().getName();
    // String ntURI = nt.getQName().getNamespaceURI();
    return t(nt.getQName());
  }

  @Override
  boolean accept(PINT nt) {
    return false;
  }

  @Override
  boolean accept(RootNT nt, SingleTypeXMLClass clazz) {
    return false;
  }

  @Override
  boolean accept(TextNT nt) {
    return false;
  }

  @Override
  public String testAttributeAxisRegExp(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    StringBuilder re = new StringBuilder();
    if (!matchedNames.isEmpty()) {
      re.append('(');
      for (Iterator<QName> names = matchedNames.iterator(); names.hasNext();) {
        QName name = names.next();
        if (name.getName().equals("*")) {
          re.append(clazz.getAttributeRegExp());
        } else {
          re.append(clazz.getCharForAttributeName(name));
        }
        if (names.hasNext())
          re.append('|');
      }
      re.append(')');
    } else
      return "" + CharGenerator.getAbsurdChar();
    if (matchedNames.size() == 1)
      return re.toString();
    return "(" + re.toString() + ")";
  }

  @Override
  public String testCDAxisRegExp(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    StringBuilder re = new StringBuilder();
    if (!matchedNames.isEmpty()) {
      for (Iterator<QName> names = matchedNames.iterator(); names.hasNext();) {
        QName name = names.next();
        if (name.getName().equals("*")) {
          re.append(clazz.getElementRegExp());
        } else {
          re.append(clazz.getCharForElementName(name));
        }
        if (names.hasNext())
          re.append('|');
      }
    } else
      return "" + CharGenerator.getAbsurdChar();
    if (matchedNames.size() == 1)
      return re.toString();
    return "(" + re.toString() + ")";
  }

  @Override
  public String testSDOSAxisRegExp(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    return testCDAxisRegExp(clazz);
  }

  @Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('(');
    boolean needsPipe = false;
    for (QName name : matchedNames) {
      if (needsPipe)
        sb.append('|');
      else
        needsPipe = true;
      sb.append(Dom4jUtil.clarkName(name));
    }
    sb.append(')');
    return sb.toString();
  }
}
