package dongfang.xsltools.xmlclass.xsd;

import java.util.Set;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Origin;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.schemaside.AttributeDeclImpl;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

/**
 * Explicit representation of the implicit type attribute declaration.
 * @author dongfang
 *
 */
public class XSITypeAttr extends AttributeDeclImpl {

  final QName typename;

  public XSITypeAttr(QName typename) {
    super(XSDSchemaConstants.TYPE_QNAME);
    this.typename = typename;
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
    return Automaton.makeString(Dom4jUtil.clarkName(typename));
  }
}
