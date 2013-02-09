package dongfang.xsltools.xmlclass.xsd;

import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ConcreteAttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;

public class XSITypeUse extends ConcreteAttributeUse {

  public XSITypeUse(XSITypeAttr attr, boolean required, ElementUse owner) {
    super(attr, required ? AttributeUse.Cardinal.REQUIRED
        : AttributeUse.Cardinal.OPTIONAL, Dom4jUtil.clarkName(attr.typename),
        owner);
  }
}
