package dongfang.xsltools.xmlclass.xslside;

import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.schemaside.ElementDecl;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;

public class ElementNT extends AbstractNodeType implements UndeclaredNodeType,
    NamedNodeType {
  QName name;

  public ElementNT(QName name) {
    this.name = name;
  }

  public ElementNT(ElementNT original) {
    this(original.name);
  }

  public ElementNT(ElementDecl original) {
    this(original.getQName());
  }

  public ElementNT(ElementUse original) {
    this(original.getQName());
  }

  @Override
public boolean equals(Object val) {
    if (val.getClass() == ElementNT.class) {
      ElementNT elm = (ElementNT) val;
      return name.equals(elm.name);
    }
    return super.equals(val);
  }

  @Override
public int hashCode() {
    return name.hashCode();
  }

  @Override
public char getCharRepresentation(CharNameResolver clazz) {
    return clazz.getCharForElementName(getQName());
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

  @Override
  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement(getClass().getSimpleName());
    parent.add(me);
    me.addAttribute("name", toString());
  }

  @Override
public String toString() {
    return Dom4jUtil.clarkName(name);
  }
}
