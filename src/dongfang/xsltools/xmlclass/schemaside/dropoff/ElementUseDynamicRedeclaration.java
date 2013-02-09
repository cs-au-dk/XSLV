package dongfang.xsltools.xmlclass.schemaside.dropoff;

import java.util.Map;
import java.util.Set;

import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ElementDecl;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;

public abstract class ElementUseDynamicRedeclaration extends NamedTypeDynamicRedeclaration implements ElementUse {

  public ElementUseDynamicRedeclaration(ElementUse decorated) {
    super(decorated);
  }
  
  public boolean acceptsCommentPIs() {
    return ((ElementUse)decorated).acceptsCommentPIs();
  }

  public boolean acceptsText() {
    return ((ElementUse)decorated).acceptsText();
  }

  public void addAttributeUse(AttributeUse use) {
    ((ElementUse)decorated).addAttributeUse(use);
  }

  public void attributeUses(Map<QName, AttributeUse> dumper) {
    ((ElementUse)decorated).attributeUses(dumper);
  }

  public void childDeclarations(Map<QName, ElementDecl> dumper) {
    ((ElementUse)decorated).childDeclarations(dumper);
  }

  public void fixupParentReferences() {
    ((ElementUse)decorated).fixupParentReferences();
  }

  public void fixupParentReferences(ElementUse canonical) {
    ((ElementUse)decorated).fixupParentReferences(canonical);
  }

  public Set<? extends ElementUse> getSGRPSubstituteableElementUses() {
    return ((ElementUse)decorated).getSGRPSubstituteableElementUses();
  }

  public boolean isNilled() {
    return ((ElementUse)decorated).isNilled();
  }

  public ElementDecl myElementDeclaration() {
    return ((ElementUse)decorated).myElementDeclaration();
  }

  public boolean typeMayDeriveFrom(QName typeQName) throws XSLToolsSchemaException {
    return ((ElementUse)decorated).typeMayDeriveFrom(typeQName);
  }
}
