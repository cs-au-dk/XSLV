package dongfang.xsltools.xmlclass.dtd;

import java.util.Iterator;
import java.util.Set;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Automata;
import dk.brics.misc.Origin;
import dongfang.xsltools.exceptions.XSLToolsRuntimeException;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.AttributeDeclImpl;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

/**
 * Represents an element type described by a DTD.
 */
public class DTDAttributeDecl extends AttributeDeclImpl {

  enum Type {
    CDATA, ID, NMTOKEN, NMTOKENS, IDREF, IDREFS, ENTITY, ENTITIES, FIXED, ENUMERATION
  };

  private Automaton cachedContentAutomaton;

  Type type;

  String defaultValue;

  Set<String> enumerationItems;

  final Origin origin;
  
  DTDAttributeDecl(dongfang.dtdparser.DTDAttributeDecl original)
      throws XSLToolsSchemaException {

    // Assumption: DTD attributes are in no namespace land
    super(QName.get(original.getName(), Namespace.NO_NAMESPACE));

    // Examine presence:
    if (original.getType() == dongfang.dtdparser.DTDAttributeDecl.Types.CDATA)
      type = Type.CDATA;
    else if (original.getType() == dongfang.dtdparser.DTDAttributeDecl.Types.NMTOKEN)
      type = Type.NMTOKEN;
    else if (original.getType() == dongfang.dtdparser.DTDAttributeDecl.Types.NMTOKENS)
      type = Type.NMTOKENS;
    else if (original.getType() == dongfang.dtdparser.DTDAttributeDecl.Types.ENTITY)
      type = Type.ENTITY;
    else if (original.getType() == dongfang.dtdparser.DTDAttributeDecl.Types.ENTITIES)
      type = Type.ENTITIES;
    else if (original.getType() == dongfang.dtdparser.DTDAttributeDecl.Types.ID)
      type = Type.ID;
    else if (original.getType() == dongfang.dtdparser.DTDAttributeDecl.Types.IDREF)
      type = Type.IDREF;
    else if (original.getType() == dongfang.dtdparser.DTDAttributeDecl.Types.IDREFS)
      type = Type.IDREFS;
    else if (original.getType() == dongfang.dtdparser.DTDAttributeDecl.Types.ENUMERATION) {
      type = Type.ENUMERATION;
      this.enumerationItems = original.getEnumerationValues();
    }
    
    if (original.getMode() == dongfang.dtdparser.DTDAttributeDecl.Modes.FIXED)
      type = Type.FIXED;

    this.defaultValue = original.getValue();
    
    origin = new Origin(original.getParseSystemId(), original.getParseLocationLine(), original.getParseLocationColumn());
  }

  protected Iterator getEnumerationType() {
    return enumerationItems.iterator();
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  @Override
public Origin getOrigin() {
    return origin;
  }
  
  private Automaton makeValueOfAutomaton() {
    switch (type) {
    case ENTITY:
      // We have to simulate that XML places no constraints...
      // Guess the empty string should be fine :)
      return Automaton.makeEmptyString();
    case ENTITIES:
      return Automaton.makeEmptyString();
    case CDATA:
      return Automata.get("string");
    case FIXED:
      return Automaton.makeString(getDefaultValue());
    case NMTOKEN:
      return Automata.get("Nmtoken2");
    case NMTOKENS:
      return Automata.get("Nmtokens");
    case ID:
      return Automata.get("Name2");
    case IDREF:
      return Automata.get("Name2");
    case IDREFS:
      return Automata.get("Names");
    // Automaton automatic = Automata.get("Name2");
    // Automaton repeater = Automaton.makeString(" ").concatenate(automatic);
    // return automatic.concatenate(repeater.repeat());
    case ENUMERATION:
      Automaton a = new Automaton();
      for (Iterator it = getEnumerationType(); it.hasNext();) {
        String s = it.next().toString();
        a = a.union(Automaton.makeString(s));
      }
      return a;
    default:
      throw new XSLToolsRuntimeException("Unknown DTD attribute type!");
    }
  }

  public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz) {
    if (cachedContentAutomaton == null) {
      cachedContentAutomaton = makeValueOfAutomaton();
    }
    return cachedContentAutomaton;
  }

  @Override
protected void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration) {
  }

  /*
   * This semantics should be OK, as different named attribute declarations aree
   * never stored in the same collection anyway???
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  // public boolean equals(Object val) {
  // return val instanceof AttributeNT
  // && getQName().equals(((AttributeNT) val).getQName());
  /*
   * if (val instanceof AttributeNT) { AttributeNT att = (AttributeNT) val;
   * return (getOwnerElementNames().equals(att.getOwnerElementNames()) &&
   * getName() .equals(att.getName())); } return false;
   */
  // }
}