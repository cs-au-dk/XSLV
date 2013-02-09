package dongfang.xsltools.xmlclass.schemaside;

import org.dom4j.QName;

import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NamedNodeType;

public interface AttributeUse extends NamedNodeType, DeclaredNodeType {
  enum Cardinal {
    REQUIRED, OPTIONAL, PROHIBITED, UNKNOWN
  }

  AttributeDecl getAttributeDeclaration();

  Cardinal getCardinality();

  //public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz,
  //    AttributeUse realMcCoy, AttributeUse.Cardinal cardinal);

  public Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz,
      AttributeUse.Cardinal cardinal);

  //Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz,
  //    AttributeUse real);

  Node constructFlowModel(SGFragment fraggle, SingleTypeXMLClass clazz);

  ElementUse getOwnerElementUse();

  QName getOwnerElementQName();

  boolean typeMayDeriveFrom(QName name) throws XSLToolsSchemaException;
}