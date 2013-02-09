package dongfang.xsltools.controlflow;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsXPathUnresolvedNamespaceException;
import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.model.NamespaceExpander;
import dongfang.xsltools.util.Dom4jUtil;

public interface ContextMode extends TemplateInvokerMode {
  /*
   * #all is compatible with everything QNamed modes are compatible with QNamed
   * modes with same QName, and #all #default is compatible with #default, and
   * #all
   */
  // boolean compatibleWith(TemplateMode other);
}

interface TemplateMode {
  boolean accepts(ContextMode other);
}

interface TemplateInvokerMode {
  ContextMode contextualize(ContextMode context);
  /*
   * Behaviour of mode in apply-templates: #current passes the argument #all
   * throws exception / is statically impossible #default returns itself QNamed
   * returns itself
   */
}

class AllTemplateMode implements TemplateMode {
  public static AllTemplateMode instance = new AllTemplateMode();

  private AllTemplateMode() {
  }

  @Override
public String toString() {
    return "#all";
  }

  public boolean accepts(ContextMode other) {
    return true;
  }

  @Override
public int hashCode() {
    return 0;
  }

  @Override
public boolean equals(Object o) {
    return o == instance;
  }
}

class CompositeTemplateMode implements TemplateMode {
  private List<TemplateMode> subs = new LinkedList<TemplateMode>();

  CompositeTemplateMode(String sModeAtt, Element coreTemplateElement)
      throws XSLToolsXPathUnresolvedNamespaceException {
    StringTokenizer tnz = new StringTokenizer(sModeAtt, " ");
    while (tnz.hasMoreTokens()) {
      TemplateMode mode;

      String modeAtt = tnz.nextToken();

      if ("#all".equals(sModeAtt))
        mode = AllTemplateMode.instance;
      else if ("#default".equals(sModeAtt))
        mode = DefaultTemplateMode.instance;
      else {
        QName qname = ElementNamespaceExpander.qNameForXSLAttributeValue(
            modeAtt, coreTemplateElement,
            NamespaceExpander.BIND_PREFIXLESS_TO_NO_NAMESPACE);
        mode = new QNameTemplateMode(qname);
      }
      subs.add(mode);
    }
  }

  @Override
public String toString() {
    return subs.toString().replace(',', ' ');
  }

  public boolean accepts(ContextMode other) {
    for (TemplateMode m : subs) {
      if (m.accepts(other))
        return true;
    }
    return false;
  }
}

class QNameTemplateMode implements ContextMode, TemplateMode {
  QName qname;

  QNameTemplateMode(QName qname) {
    this.qname = qname;
  }

  @Override
public String toString() {
    return Dom4jUtil.clarkName(qname);
  }

  public boolean compatibleWith(TemplateMode other) {
    if (other instanceof QNameTemplateMode) {
      return qname.equals(((QNameTemplateMode) other).qname);
    }
    return other == AllTemplateMode.instance;
  }

  public boolean accepts(ContextMode other) {
    if (other instanceof QNameTemplateMode) {
      return qname.equals(((QNameTemplateMode) other).qname);
    }
    // we dont' want CurrentContextMode
    return false;
  }

  @Override
public int hashCode() {
    return qname.hashCode();
  }

  @Override
public boolean equals(Object o) {
    return o instanceof QNameTemplateMode
        && qname.equals(((QNameTemplateMode) o).qname);
  }

  public ContextMode contextualize(ContextMode mode) {
    return this;
  }

  public boolean includedIn(Collection<QNameTemplateMode> coll) {
    for (QNameTemplateMode mode : coll) {
      if (compatibleWith(mode))
        return true;
    }
    return false;
  }
}

class CurrentContextMode implements TemplateInvokerMode {
  public static CurrentContextMode instance = new CurrentContextMode();

  private CurrentContextMode() {
  }

  public ContextMode contextualize(ContextMode mode) {
    return mode;
  }

  @Override
public String toString() {
    return "#current";
  }

  @Override
public int hashCode() {
    return 1;
  }

  @Override
public boolean equals(Object o) {
    return o == instance;
  }
}

class DefaultTemplateMode extends QNameTemplateMode {
  public static DefaultTemplateMode instance = new DefaultTemplateMode();

  private DefaultTemplateMode() {
    super(QName.get("", Namespace.NO_NAMESPACE));
  }

  @Override
public String toString() {
    return "#default";
  }
}
