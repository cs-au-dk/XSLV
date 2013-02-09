package dongfang.xsltools.xmlclass.schemaside;

import java.util.Map;
import java.util.Set;

import org.dom4j.QName;

import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NamedNodeType;

public interface ElementUse extends NamedNodeType, DeclaredNodeType {
  /*
   * Get the explicit declaration that we are an explicit or implicit image of.
   * It should hold that x = this.myElementDeclaration() implies that this is in
   * x.getAllUses(...), and the other way around.
   */
  ElementDecl myElementDeclaration();

  /*
   * Get all declared attribute uses, PLUS the implicit ones that follow from
   * any funny subtyping systems like the one from XML Schema.
   */
  void attributeUses(Map<QName, AttributeUse> dumper);

  void addAttributeUse(AttributeUse use);
  
//  Set<? extends AttributeUse> getAllAttributeUses();
  
  /*
   * Get declared children. These are the one seen in a traverse UP the type
   * hierarchy, from the declared or implicit type, adding up all bits 'n'
   * pieces of the content models. If we're representing a nilled element,
   * though, then there are no declared childen at all, haha. This is exactly
   * the C_d function.
   */
  void childDeclarations(Map<QName, ElementDecl> dumper);

  boolean acceptsText();

  boolean acceptsCommentPIs();

  void fixupParentReferences();

  void fixupParentReferences(ElementUse canonical);

  /*
   * XSD specific bullshit
   */
  boolean typeMayDeriveFrom(QName typeQName) throws XSLToolsSchemaException;

  boolean isNilled();

  Set<? extends ElementUse> getSGRPSubstituteableElementUses();
}
