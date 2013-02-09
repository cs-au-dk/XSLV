package dongfang.xsltools.xmlclass.xsd;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.ControlFlowConfiguration;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.ElementDecl;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.CharNameResolver;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.ElementNT;
import dongfang.xsltools.xmlclass.xslside.PINT;
import dongfang.xsltools.xmlclass.xslside.RootNT;
import dongfang.xsltools.xmlclass.xslside.TextNT;
import dongfang.xsltools.xpath2.XPathAxisStep;

/**
 * This is where XSD's funny subtyping is modeled: If type a has a
 * defined subtype b, and element x is declared with type a, then
 * two instances of this class are made:
 * One with a as subsumedType, and an optional xsi:type=a attribute.
 * One with b as subsumedType, and a required xsi:type=b attribute.
 * @author dongfang
 */
public class SubsumptionElementUse extends ElementNT implements
    ElementUse {

  final ElementDecl decl;

  final XSDType subsumedType;

  private final Map<QName, AttributeUse> attributeUses = 
    new HashMap<QName, AttributeUse>();

  SubsumptionElementUse(ElementDecl decl, XSDType subsumedType) {
    super(decl.getQName());
    this.decl = decl;
    this.subsumedType = subsumedType;
  }

  public void fixupParentReferences(ElementUse canonical) {
    for (XSDElementDecl decl : subsumedType
        .getLocalAndInheritedElementContents().values()) {
      for (ElementDecl decl2 : decl.getSGRPSubstituteableElementDecls()) {
        decl2.addParentUse(canonical);
      }
    }
  }

  public void fixupParentReferences() {
    fixupParentReferences(this);
  }

  public void addAttributeUse(AttributeUse xsi) {
    attributeUses.put(xsi.getQName(), xsi);
  }

  public void attributeUses(Map<QName, AttributeUse> dumper) {
    dumper.putAll(attributeUses);
  }

  public void childDeclarations(Map<QName, ElementDecl> dumper) {
    dumper.putAll(subsumedType.getLocalAndInheritedElementContents());
  }

  public ElementDecl myElementDeclaration() {
    return decl;
  }

  @Override
public Automaton getClarkNameAutomaton() {
    return decl.getClarkNameAutomaton();
  }

  /*
   * All uses have one name...
   */
  @Override
public QName getQName() {
    return decl.getQName();
  }

  /*
   * All uses have one charname...
   */
  @Override
public char getCharRepresentation(CharNameResolver resolver) {
    return decl.getCharRepresentation(resolver);
  }

  /*
   * All uses have not necessarily one labelstr...
   */
  @Override
public String toLabelString() {
    return decl.toLabelString() + '(' + subsumedType.getQName().getQualifiedName() + ')';
  }

  public void diagnostics(Branch parent, DocumentFactory fac) {
    Element me = fac.createElement(getClass().getSimpleName());
    parent.add(me);
    me.addAttribute("declaration", decl.toLabelString());
    me.addAttribute("type", subsumedType.toString());
  }

  @Override
  public Node constructAttributeFM(SGFragment fraggle,
      SingleTypeXMLClass clazz, Set<DeclaredNodeType> attrs)
      throws XSLToolsSchemaException {
    return seriousAttributeFM(fraggle, clazz, attrs);
  }

  @Override
  public Node constructChildFM(SGFragment fraggle, SingleTypeXMLClass clazz,
      boolean maxOne, Set<DeclaredNodeType> typeSet,
      boolean allowInterleave, ContentOrder order)
      throws XSLToolsSchemaException {
    return subsumedType.constructChildFM(fraggle, 
        (XSDSchema)clazz, maxOne, typeSet, order, allowInterleave);
  }

  public Node constructParentFM(SGFragment fraggle, SingleTypeXMLClass clazz,
      Set<DeclaredNodeType> typeSet)
      throws XSLToolsSchemaException {
    typeSet = new HashSet<DeclaredNodeType>(typeSet);
    typeSet.retainAll(decl.getParentUses()); // should never have any effect really...
    return constructAllModel(fraggle, clazz, true, typeSet);
  }
  
  @Override
  public Node constructDescendantFM
  (SGFragment fraggle, SingleTypeXMLClass clazz,
      boolean maxOne, Set<DeclaredNodeType> typeSet) throws XSLToolsSchemaException {
    return constructAllModel
    (fraggle, clazz, maxOne, typeSet);
  }

  public Automaton getATSAutomaton(SingleTypeXMLClass clazz) {
    return decl.getATSAutomaton(clazz);
  }

  @Override
public Object getIdentifier() {
    return decl.getIdentifier();
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    // TODO Auto-generated method stub
    return subsumedType.getValueOfAutomaton((XSDSchema) clazz);
  }

  public boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException {
    // TODO Auto-generated method stub
    return s.accept(this, clazz);
  }

  @Override
public void runAttributeAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    // result.addAll(subsumedType.getAttributes().values());
    result.addAll(attributeUses.values());
    //result.addAll(subsumedType.getLocalAndInheritedAttributeUses());
  }

  @Override
  public void runChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result)
      throws XSLToolsSchemaException {

    Collection<XSDElementDecl> childDecls = subsumedType
        .getLocalAndInheritedElementContents().values();

    for (XSDElementDecl child : childDecls) {
      for (ElementDecl zitt : child.getSGRPSubstituteableElementDecls()) {
        result.addAll(zitt.getAllUses());
      }
    }
    
    result.add(CommentNT.instance);
    result.add(PINT.chameleonInstance);

    if (subsumedType.mayContainTextNodes()) {
      TextNT text;
      if (ControlFlowConfiguration.current.useColoredContextTypes())
        text = new TextNT(subsumedType.languageOfTextNodes((XSDSchema) clazz),
            getDeclarationOrigin());
      else
        text = TextNT.chameleonInstance;
      result.add(text);
    }
  }

  public void runParentAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    // TODO Auto-generated method stub
    Set<? extends DeclaredNodeType> parents = decl.getParentUses();
    result.addAll(parents);
  }

  @Override
  public void runReverseChildAxis(SingleTypeXMLClass clazz,
      Set<? super DeclaredNodeType> result) {
    runParentAxis(clazz, result);
    if (decl == clazz.getDocumentElementDecl())
      result.add(RootNT.instance);
  }

  public boolean acceptsCommentPIs() {
    return true;
  }

  public boolean acceptsText() {
    return subsumedType.mayContainTextNodes();
  }

  public Set<? extends ElementUse> getSGRPSubstituteableElementUses() {
    Collection<? extends ElementDecl> substz = decl
        .getSGRPSubstituteableElementDecls();
    Set<ElementUse> result = new HashSet<ElementUse>();
    for (ElementDecl d : substz) {
      result.addAll(d.getAllUses());
    }
    return result;
  }

  public boolean isNilled() {
    return false;
  }

  public boolean typeMayDeriveFrom(QName typeQName)
      throws XSLToolsSchemaException {
    // TODO Auto-generated method stub
    return subsumedType.deriveableFrom(typeQName);
  }

  @Override
public String toString() {
    return getClass().getSimpleName() + " decl = " + decl.toLabelString()
        + " type = " + subsumedType.toString();
  }
  
  public Node constructInstantiationFM(SGFragment fraggle, int content, SingleTypeXMLClass clazz) 
  throws XSLToolsSchemaException {
    return decl.constructInstantiationFM(fraggle, content, clazz);
  }
  
  public Origin getDeclarationOrigin() {
    return decl.getOrigin();
  }
  
  public DeclaredNodeType getOriginalDeclaration() {
    return this;
  }
}
