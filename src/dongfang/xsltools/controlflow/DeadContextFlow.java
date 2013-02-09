/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-21
 */
package dongfang.xsltools.controlflow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.XMLConstants;
import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.xmlclass.xslside.NodeType;

/**
 * @author dongfang
 */
public class DeadContextFlow implements Diagnoseable {

  public static final byte PRIORITY_OVERRIDDEN = 1;

  public static final byte PRECEDENCE_OVERRIDDEN = 2;

  public static final byte DOES_NOT_MATCH_SCHEMA = 3;

  public static final byte WRECKED_IN_SELECTION_EXPERIMENTAL = 4;

  final TemplateRule target;

  final int cause;

  final String source;

  Set<NodeType> lostNodeTypes;

  public DeadContextFlow(TemplateRule to, int cause, String source) {
    this.target = to;
    this.cause = cause;
    this.source = source;
  }

  public void addLostNodeType(NodeType type) {
    if (lostNodeTypes == null) {
      lostNodeTypes = new HashSet<NodeType>();
    }
    lostNodeTypes.add(type);
  }

  public void addLostNodeTypes(Set<? extends NodeType> types) {
    if (types == null || types.isEmpty())
      return;
    if (lostNodeTypes == null) {
      lostNodeTypes = new HashSet<NodeType>();
    }
    lostNodeTypes.addAll(types);
  }

  public Set<NodeType> getLostNodeTypes() {
    if (lostNodeTypes == null)
      return Collections.emptySet();
    return lostNodeTypes;
  }

  public String cause() {
    switch (cause) {
    case PRIORITY_OVERRIDDEN:
      return "Other template has higher priority";
    case PRECEDENCE_OVERRIDDEN:
      return "Other template has higher import precedence";
    case DOES_NOT_MATCH_SCHEMA:
      return "Input schema restrictions";
    }
    return "Unknown";
  }

  public Element describeDeathCause(DocumentFactory fac) {
    Element result = fac.createElement(XMLConstants.DEATH_CAUSE_QNAME);
    Element lostTypesE = fac.createElement(XMLConstants.CONTEXTSET_QNAME);
    result.add(lostTypesE);
    Set<NodeType> types = getLostNodeTypes();

    for (NodeType nodetype : types) {
      Element flowtype = fac.createElement(XMLConstants.CONTEXT_TYPE_QNAME);
      flowtype.addAttribute(XMLConstants.CONTEXT_TYPE_ATTR_QNAME, nodetype
          .toLabelString());
      lostTypesE.add(flowtype);
    }

    Element victim = fac.createElement(XMLConstants.VICTIM_QNAME);
    result.add(victim);
    victim.add(target.createXMLReference(fac));
    result.addAttribute(XMLConstants.DEATH_CAUSE_ATTR_QNAME, cause());
    return result;
  }

  public TemplateRule getTarget() {
    return target;
  }
  
  @Override
public boolean equals(Object o) {
    if (o instanceof DeadContextFlow) {
      DeadContextFlow c = (DeadContextFlow) o;
      return target.equals(c.target) && cause == c.cause;
    }
    return false;
  }

  @Override
public int hashCode() {
    return target.hashCode() + cause;
  }

  void moreDiagnostics(Element me, DocumentFactory fac) {
  }
  
  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement(getClass().getSimpleName());
    parent.add(me);
    me.addAttribute("cause", cause());
    me.addAttribute("victim", target.toLabelString());
    me.addAttribute("source", source);

    moreDiagnostics(me, fac);

    if (lostNodeTypes != null)
      for (NodeType type : lostNodeTypes) {
        type.diagnostics(me, fac, configuration);
      }
  }
}
