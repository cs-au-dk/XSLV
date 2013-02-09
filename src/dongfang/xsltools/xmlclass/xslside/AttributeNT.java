package dongfang.xsltools.xmlclass.xslside;

import java.util.HashSet;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.schemaside.AttributeDecl;

/*
 * Just a model of the Attribute Node Type 
 * @author dongfang
 */
public class AttributeNT extends AbstractNodeType implements
    UndeclaredNodeType, NamedNodeType {
  Set<QName> ownerElementNames = new HashSet<QName>();

  QName name;

  /*
   * In the astar test, this is created with NO owner elements. Problem?
   */
  public AttributeNT(QName name) {
    /*
     * if (name.getName().equals("*") || name.getNamespaceURI().equals("*"))
     * throw new AssertionError("Not allowed to instantiate wildcard NTs
     * (yet)"); if (element.getName().equals("*") ||
     * element.getNamespaceURI().equals("*")) throw new AssertionError("Not
     * allowed to instantiate wildcard NTs (yet)");
     */
    // this.element = element;
    this.name = name;
  }

  public AttributeNT(AttributeNT original) {
    this(original.name);
  }

  public AttributeNT(AttributeDecl original) {
    this(original.getQName());
  }

  protected void addOwnerElementQName(QName n) {
    ownerElementNames.add(n);
  }

  public QName getQName() {
    return name;
  }

  public Automaton getClarkNameAutomaton() {
    if ("".equals(getQName().getNamespaceURI())) {
      return Automaton.makeString(getQName().getName());
    }

    return Automaton.makeChar('{').concatenate(
        Automaton.makeString(getQName().getNamespaceURI())).concatenate(
        Automaton.makeChar('}')).concatenate(
        Automaton.makeString(getQName().getName()));
  }

  public String getLocalName() {
    return name.getName();
  }

  public Set<QName> getOwnerElementNames() {
    return ownerElementNames;
  }

  // @Override
  @Override
public char getCharRepresentation(CharNameResolver clazz) {
    return clazz.getCharForAttributeName(getQName());
  }

  @Override
  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement(getClass().getSimpleName());
    me.addAttribute("name", getLocalName());
    me.addAttribute("namespaceURI", getQName().getNamespaceURI());
    parent.add(me);
  }

  /*
   * SEEMS to work oki doke. Semantics: Chameleon (decl) equals any color (use).
   */
  @Override
public boolean equals(Object o) {
    if (o.getClass() == AttributeNT.class) {
      AttributeNT att = (AttributeNT) o;
      boolean r = name.equals(att.name);
      return r;
    }
    //return this == val;
    return super.equals(o);
  }

  @Override
public int hashCode() {
    return name.hashCode();
  }

  /*
   * protected void subdiagnostics(Element me, DocumentFactory fac) {
   * me.addAttribute("element", element == null ? "null" :
   * element.getQualifiedName()); me.addAttribute("name", name == null ? "null" :
   * name.getName()); me.addAttribute("namespace", name == null ? "null" :
   * name.getNamespaceURI()); }
   */

  @Override
public String toString() {
    StringBuilder res = new StringBuilder();
    res.append(Dom4jUtil.clarkName(name));
    boolean needsComma = false;
    res.append('(');
    for (QName n : ownerElementNames) {
      if (needsComma)
        res.append(',');
      else
        needsComma = true;
      res.append(Dom4jUtil.clarkName(n));
    }
    res.append(')');
    return res.toString();
  }
}
