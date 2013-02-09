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

class XSDAttributeDecl extends AttributeDeclImpl {
  XSDSimpleType declaredType;
  QName declaredTypeName;
  Origin origin;

  XSDAttributeDecl(QName myName, QName declaredTypeName,
      XSDSimpleType myDeclaredType, Origin o) {
    super(myName);
    this.declaredTypeName = declaredTypeName;
    this.declaredType = myDeclaredType;
    this.origin = o;
  }

  @Override
  public Origin getOrigin() {
    return origin;
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    return declaredType.getValueOfAutomaton((XSDSchema) clazz);
  }

  /*
   * protected void constructATSPathAutomaton(State oneOwnerState) {
   * super.constructATSPathAutomaton(oneOwnerState); }
   */

  @Override
  public boolean typeMayDeriveTo(QName othersName, SingleTypeXMLClass clazz) {
    XSDSchema schema = (XSDSchema) clazz;
    XSDType other = schema.getTypedef(othersName);
    return other.deriveableFrom(declaredTypeName);
  }

  @Override
  public boolean typeMayDeriveFrom(QName typeName) {
    return declaredType.deriveableFrom(typeName);
  }

  @Override
  protected void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration) {
    me.addAttribute("declaredType", Dom4jUtil
        .clarkName(declaredType.getQName()));
  }
}
