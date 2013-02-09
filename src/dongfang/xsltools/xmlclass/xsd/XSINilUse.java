package dongfang.xsltools.xmlclass.xsd;

import dongfang.xsltools.xmlclass.schemaside.AttributeDecl;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ConcreteAttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;

/*
 * TODO: Somehow canonicalize SG fragments!
 */
public class XSINilUse extends ConcreteAttributeUse {

  public XSINilUse(AttributeDecl attributeDecl, boolean nilled,
      ElementUse owner) {
    super(attributeDecl, nilled ? AttributeUse.Cardinal.REQUIRED
        : AttributeUse.Cardinal.OPTIONAL, Boolean.toString(nilled), owner);
  }
}
