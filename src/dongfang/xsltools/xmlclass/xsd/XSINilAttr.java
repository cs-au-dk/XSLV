package dongfang.xsltools.xmlclass.xsd;

import java.util.Set;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Origin;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.AttributeDeclImpl;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

/**
 * Explicit representation of the implicit nil attribute declaration.
 * @author dongfang
 *
 */
public class XSINilAttr extends AttributeDeclImpl {

  XSINilAttr() {
    super(XSDSchemaConstants.NIL_QNAME);
  }

  @Override
  public Origin getOrigin() {
    return null;
  }

  @Override
  protected void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration) {
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    return Automaton.makeString("true").union(Automaton.makeString("false"));
  }
}
