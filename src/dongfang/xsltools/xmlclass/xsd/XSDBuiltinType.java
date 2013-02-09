package dongfang.xsltools.xmlclass.xsd;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xml.sax.InputSource;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.misc.Origin;
import dk.brics.relaxng.Param;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.diagnostics.ShowStopperErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.resolver.URLResolutionContext;
import dongfang.xsltools.util.Dom4jUtil;

class XSDBuiltinType extends XSDAbstractSimpleType {

  //static final boolean _emergencyAndersBugMode = true;

  static final Origin ORIGIN = new Origin("xsd:builtin", 0, 0);

  XSDBuiltinType(QName name, QName superTypeName)
      throws XSLToolsSchemaException {
    super(name, superTypeName, DER_RESTRICTION, Collections.EMPTY_LIST, ORIGIN);
    skipDiagnostics = true;
  }

  XSDBuiltinType(String name, String superTypeName)
      throws XSLToolsSchemaException {
    this(QName.get(name, XSDSchemaConstants.NAMESPACE_URI), QName.get(
        superTypeName, XSDSchemaConstants.NAMESPACE_URI));
  }

  @Override
  public Automaton getValueOfAutomaton(XSDSchema schema)
      throws XSLToolsSchemaException {
    return makeValueOfAutomaton(schema, new LinkedList<Param>());
  }

  public Automaton makeValueOfAutomaton(XSDSchema schema, List<Param> facets)
      throws XSLToolsSchemaException {
    if (name.getNamespaceURI().equals(XSDSchemaConstants.NAMESPACE_URI)) {
      String bname = name.getName();
      try {
        // for whenever summarygraph won't build.
        if (stddt == null)
          return Automaton.makeString("Verdammt");

        if (bname.equals("anySimpleType"))
          bname = "string";

        Automaton a = stddt.datatypeToAutomaton(
            XSDSchemaConstants.DATATYPE_NAMESPACE_URI, bname, facets);

        // FIXME!!! Someone.
        if (!"string".equals(bname)) {
          //System.err.println("D'oh!");
          Automaton withLeadingWS = 
            new RegExp("[\u0009\u0020\r\n]+[^\u0009\u0020\r\n].*").toAutomaton();
          Automaton withTrailingWS = 
            new RegExp(".*[^\u0009\u0020\r\n]+[\\\u0009\\\u0020\r\n]+").toAutomaton();
          //a = a.intersection(new RegExp("[a-z]*").toAutomaton());
          a = a.intersection(withLeadingWS.complement().intersection(withTrailingWS.complement()));
          a.reduce();
          a.removeDeadTransitions();
        }

        return a;
      } catch (IllegalArgumentException ex) {
        System.err.println("Complaint about type: " + bname
            + ", making emergency fix (stripping facets)");
        // throw new XSLToolsSchemaException(ex);
        try {
          Automaton a = stddt.datatypeToAutomaton(
              XSDSchemaConstants.DATATYPE_NAMESPACE_URI, bname, null);
          return a;
        } catch (IllegalArgumentException ex2) {
          throw new XSLToolsSchemaException(ex2);
        }
      }
    }
    throw new XSLToolsSchemaException(
        "Could not find definition for simple type: "
            + Dom4jUtil.clarkName(name));
  }

  @Override
  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    if (!skipDiagnostics())
      super.diagnostics(parent, fac, configuration);
  }

  void moreDiagnostics(Element me, DocumentFactory fac, Set<Object> configuration) {
    me.addAttribute("kind", "builtin");
  }

  /*
   * A document making explicit the complex def of anyType
   */
  private static Document anyTypeDocument;

  /*
   * Whip up the anyType document -- once, thereafter clone it.
   */
  static XSDSchemaDocument makeBuiltinTypeDocument() throws XSLToolsException {
    ErrorReporter rep = new ShowStopperErrorReporter();

    final String GEDEFIMS = "xsd-builtins";

    if (anyTypeDocument == null) {

      ResolutionContext ct = new URLResolutionContext() {
        public InputSource resolveStream(String systemId) throws IOException {
          if (systemId.equals(GEDEFIMS)) {
            InputSource so = new InputSource(systemId);
            so.setCharacterStream(XSDAnyType.getReader());
            return so;
          }
          return super.resolveStream(systemId);
        }
      };

      anyTypeDocument = XSDSchemaFactory.getDocument(GEDEFIMS,
          ResolutionContext.STYLESHEET_MODULE_IDENTIFIER_KEY, ct, rep);
    }

    Document doc = (Document) anyTypeDocument.clone();

    XSDSchemaDocument builtins = (XSDSchemaFactory.createSchemaDocument(
        "xsd:anyType", doc, XSDSchemaConstants.NAMESPACE_URI, rep));

    fixupBuiltinTypes(builtins);

    return builtins;
  }

  private static void addBuiltin(XSDSchemaDocument doc, XSDBuiltinType builtin) {
    doc.localTypedefsByRef.put(builtin.name, builtin);
  }

  private static void fixupBuiltinTypes(XSDSchemaDocument schema)
      throws XSLToolsSchemaException {
    addBuiltin(schema, new XSDBuiltinType("anySimpleType", "anyType"));
    addBuiltin(schema, new XSDBuiltinType("duration", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("dateTime", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("time", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("date", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("gYearMonth", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("gYear", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("gMonthDay", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("gDay", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("gMonth", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("boolean", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("base64Binary", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("hexBinary", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("float", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("double", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("anyURI", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("QName", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("NOTATION", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("string", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("decimal", "anySimpleType"));
    addBuiltin(schema, new XSDBuiltinType("normalizedString", "string"));
    addBuiltin(schema, new XSDBuiltinType("token", "normalizedString"));
    addBuiltin(schema, new XSDBuiltinType("language", "token"));
    addBuiltin(schema, new XSDBuiltinType("Name", "token"));
    addBuiltin(schema, new XSDBuiltinType("NMTOKEN", "token"));
    addBuiltin(schema, new XSDBuiltinType("NCName", "Name"));
    addBuiltin(schema, new XSDBuiltinType("ID", "NCName"));
    addBuiltin(schema, new XSDBuiltinType("IDREF", "NCName"));
    addBuiltin(schema, new XSDBuiltinType("ENTITY", "NCName"));
    addBuiltin(schema, new XSDBuiltinType("integer", "decimal"));
    addBuiltin(schema, new XSDBuiltinType("nonPositiveInteger", "integer"));
    addBuiltin(schema, new XSDBuiltinType("long", "integer"));
    addBuiltin(schema, new XSDBuiltinType("nonNegativeInteger", "integer"));
    addBuiltin(schema, new XSDBuiltinType("negativeInteger",
        "nonPositiveInteger"));
    addBuiltin(schema, new XSDBuiltinType("int", "long"));
    addBuiltin(schema, new XSDBuiltinType("short", "int"));
    addBuiltin(schema, new XSDBuiltinType("byte", "short"));
    addBuiltin(schema, new XSDBuiltinType("unsignedLong", "nonNegativeInteger"));
    addBuiltin(schema, new XSDBuiltinType("positiveInteger",
        "nonNegativeInteger"));
    addBuiltin(schema, new XSDBuiltinType("unsignedInt", "unsignedLong"));
    addBuiltin(schema, new XSDBuiltinType("unsignedShort", "unsignedInt"));
    addBuiltin(schema, new XSDBuiltinType("unsignedByte", "unsignedShort"));

    QName name = QName.get("IDREFS", XSDSchemaConstants.NAMESPACE_URI);
    XSDAbstractType type = new XSDListSimpleType(name, QName.get("IDREF",
        XSDSchemaConstants.NAMESPACE_URI), Collections.EMPTY_LIST,
        XSDBuiltinType.ORIGIN);
    type.skipDiagnostics = true;
    schema.localTypedefsByRef.put(name, type);

    name = QName.get("NMTOKENS", XSDSchemaConstants.NAMESPACE_URI);
    type = new XSDListSimpleType(name, QName.get("NMTOKEN",
        XSDSchemaConstants.NAMESPACE_URI), Collections.EMPTY_LIST,
        XSDBuiltinType.ORIGIN);
    type.skipDiagnostics = true;
    schema.localTypedefsByRef.put(name, type);

    name = QName.get("ENTITIES", XSDSchemaConstants.NAMESPACE_URI);
    type = new XSDListSimpleType(name, QName.get("ENTITY",
        XSDSchemaConstants.NAMESPACE_URI), Collections.EMPTY_LIST,
        XSDBuiltinType.ORIGIN);
    type.skipDiagnostics = true;
    schema.localTypedefsByRef.put(name, type);

    /*
     * addBuiltin(schema, new XSDBuiltinType(QName.get("lang",
     * XMLConstants.NAMESPACE_NAMESPACE), QName.get("anySimpleType",
     * XSDSchemaConstants.NAMESPACE))); addBuiltin(schema, new
     * XSDBuiltinType(QName.get("base", XMLConstants.NAMESPACE_NAMESPACE),
     * QName.get("anyURI", XSDSchemaConstants.NAMESPACE))); addBuiltin(schema,
     * new XSDBuiltinType(QName.get("space", XMLConstants.NAMESPACE_NAMESPACE),
     * QName.get("anySimpleType", XSDSchemaConstants.NAMESPACE)));
     * addBuiltin(schema, new XSDBuiltinType(QName.get("id",
     * XMLConstants.NAMESPACE_NAMESPACE), QName.get("anySimpleType",
     * XSDSchemaConstants.NAMESPACE))); }
     */
  }
}
