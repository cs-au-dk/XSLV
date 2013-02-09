package dongfang.xsltools.xpath2;

import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.exceptions.XSLToolsException;

public abstract class XPathBase extends SimpleNode implements IXPathBase {

  // DONE: Consider interface instead.

  // String artifact;

  public XPathBase(int id) {
    super(id);
  }

  public XPathBase(XPathParser p, int id) {
    super(p, id);
  }

  public XPathBase accept(XPathVisitor v) throws XSLToolsException {
    return v.visit(this);
  }

  /*
   * public void acceptChildren(XPathVisitor v) throws XSLToolsException {
   * v.descend(); Node[] children = new Node[jjtGetNumChildren()]; for (int i =
   * 0; i < jjtGetNumChildren(); i++) { Node n = jjtGetChild(i); XPathBase x =
   * (XPathBase) n; children[i] = x.accept(v); } v.ascend(); this.children =
   * children; }
   */

  @Override
public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new AssertionError(ex);
    }
  }

  public boolean similarTo(XPathBase that) {
    // Check class:
    if (this.getClass() != that.getClass())
      return false;

    // Check subexpressions:
    if (jjtGetNumChildren() != that.jjtGetNumChildren()) {
      return false;
    } // Recurse:
    for (int i = 0; i < jjtGetNumChildren(); i++) {
      XPathBase b1 = (XPathBase) jjtGetChild(i);
      XPathBase b2 = (XPathBase) that.jjtGetChild(i);
      if (!b1.similarTo(b2))
        return false;
    }
    return true;
  }

  void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration) {
    // me.addAttribute("artifact", artifact==null ? "null" : artifact);
  }

  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement(getClass().getSimpleName());
    parent.add(me);
    moreDiagnostics(me, fac, configuration);
    for (int i = 0; i < jjtGetNumChildren(); i++) {
      Diagnoseable x = (Diagnoseable) jjtGetChild(i);
      x.diagnostics(me, fac, configuration);
    }
  }

  public void setArtifact(String s) {
    // this.artifact = s;
  }
}
