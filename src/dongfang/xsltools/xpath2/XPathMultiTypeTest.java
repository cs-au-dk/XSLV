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
public class XPathMultiTypeTest extends XPathNodeTest {
  // Set<QName> matchedNames;
  Set<? extends ElementUse> matchedTypes;

  public XPathMultiTypeTest(int id) {
    super(id);
  }

  public XPathMultiTypeTest(XPathParser p, int id) {
    super(p, id);
  }

  public XPathMultiTypeTest() {
    this(0);
    // matchedNames = new HashSet<QName>();
    matchedTypes = new HashSet<ElementUse>();
  }

  // public XPathMultiNameTest(Set<QName> names) {
  public XPathMultiTypeTest(Set<? extends ElementUse> types) {
    this();
    // this.matchedNames = names;
    this.matchedTypes = types;
  }

  @Override
public Set<UndeclaredNodeType> etest(Set<UndeclaredNodeType> delta) {
    Set<UndeclaredNodeType> testers = new HashSet<UndeclaredNodeType>();
    // for (QName q : matchedNames)
    // testers.add(new ElementNT(q));
    for (ElementUse q : matchedTypes)
      testers.add(new ElementNT(q));
    if (delta.contains(NodeType.ONE_ANY_NAME_ELEMENT_NT))
      return testers;
    testers.retainAll(delta);
    return testers;
  }

  @Override
public Set<UndeclaredNodeType> atest(Set<UndeclaredNodeType> delta) {
    Set<UndeclaredNodeType> testers = new HashSet<UndeclaredNodeType>();
    // for (QName q : matchedNames)
    // testers.add(new AttributeNT(q));
    // ???
    if (delta.contains(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT))
      return testers;
    testers.retainAll(delta);
    return testers;
  }

  private boolean t(/* String ntLocal, String ntURI */QName name) {
    // if (matchedNames.isEmpty())
    if (matchedTypes.isEmpty())
      return false;

    // case :{*}*, anything goes
    if (name.getName().equals("*") && name.getNamespaceURI().equals("*"))
      return true;

    // case{foo}bar, only a hit is good enough
    // (we assume no wildcards in matchedNames)
    if (!name.getName().equals("*") && !name.getNamespaceURI().equals("*")) {
      for (ElementUse decl : matchedTypes) {
        if (decl.getQName().equals(name))
          return true;
      }
      return false;
    }

    else if (name.getName().equals("*")) {
      for (ElementUse n : matchedTypes) {
        if (name.getNamespaceURI().equals(n.getQName().getNamespaceURI()))
          return true;
      }
      return false;
    }

    else if (name.getNamespaceURI().equals("*")) {
      for (ElementUse n : matchedTypes) {
        if (name.getName().equals(n.getQName().getName()))
          return true;
      }
      return false;
    }

    throw new AssertionError("All possibilities should have been exhausted");
  }

  @Override
  public  boolean accept(AttributeUse nt, SingleTypeXMLClass clazz) {
    return t(nt.getQName());
  }

  @Override
  boolean accept(CommentNT nt) {
    return false;
  }

  @Override
  public boolean accept(ElementUse nt, SingleTypeXMLClass clazz) {
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
  public String testAttributeAxisRegExp(SingleTypeXMLClass clazz) {
    StringBuilder re = new StringBuilder();
    // if (!matchedNames.isEmpty()) {
    if (!matchedTypes.isEmpty()) {
      re.append('(');
      // for (Iterator<QName> names = matchedNames.iterator(); names.hasNext();)
      // {
      for (Iterator<? extends ElementUse> types = matchedTypes.iterator(); types
          .hasNext();) {
        // QName name = names.next();
        // if (name.getName().equals("*")) {
        // re.append(clazz.getAttributeRegExp());
        // } else {
        // re.append(clazz.getCharForAttributeName(name));
        // }
        ElementUse type = types.next();
        re.append(type.getCharRepresentation(clazz));
        // if (names.hasNext())
        if (types.hasNext())
          re.append('|');
      }
      re.append(')');
    } else
      return "" + CharGenerator.getAbsurdChar();
    // if (matchedNames.size()==1)
    if (matchedTypes.size() == 1)
      return re.toString();
    return "(" + re.toString() + ")";
  }

  @Override
  public String testCDAxisRegExp(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    StringBuilder re = new StringBuilder();
    // if (!matchedNames.isEmpty()) {
    if (!matchedTypes.isEmpty()) {
      // for (Iterator<QName> names = matchedNames.iterator(); names.hasNext();)
      // {
      for (Iterator<? extends ElementUse> types = matchedTypes.iterator(); types
          .hasNext();) {
        // QName name = names.next();
        ElementUse type = types.next();
        // if (name.getName().equals("*")) {
        // re.append(clazz.getElementRegExp());
        // } else {
        // re.append(clazz.getCharForElementName(name));
        re.append(type.getCharRepresentation(clazz));
        // }
        // if (names.hasNext())
        if (types.hasNext())
          re.append('|');
      }
    } else
      return "" + CharGenerator.getAbsurdChar();
    // if (matchedNames.size()==1)
    if (matchedTypes.size() == 1)
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
    // for (QName name : matchedNames) {
    for (ElementUse type : matchedTypes) {
      if (needsPipe)
        sb.append('|');
      else
        needsPipe = true;
      // sb.append(Dom4jUtil.clarkName(name));
      sb.append(Dom4jUtil.clarkName(type.getQName()));
    }
    sb.append(')');
    return sb.toString();
  }
}
