package dongfang.xsltools.xmlclass.dtd;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Namespace;
import org.dom4j.QName;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dongfang.XMLConstants;
import dongfang.dtdparser.DTDParser;
import dongfang.dtdparser.contentmodel.DTDParseException;
import dongfang.xsltools.context.EntityResolverWrapper;
import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.xmlclass.CharGenerator;
import dongfang.xsltools.xmlclass.schemaside.ElementDecl;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.PINT;
import dongfang.xsltools.xmlclass.xslside.RootNT;

/*
 * Represents an XML class described by a DTD.
 */
public class DTD extends SingleTypeXMLClass {

  /**
   * Namespace of all the elements in the DTD (attributes are in no namespace).
   * This feature serves to make DTD descriptions of namespaced elements
   * possible, even though DTD does not support namespaces.
   */
  private Namespace elementNamespace;

  public DTD(InputSource dtdSource, ValidationContext context, String systemId,
      short io) throws IOException, XSLToolsSchemaException {
    this(context, systemId, io);
  }

  /*
  void fool() throws XSLToolsSchemaException {
    for (ElementDecl element : getAllElementDecls()) {
      DTDElementDecl dtdd = (DTDElementDecl) element;
      System.out.println(dtdd + "-->" + dtdd.regexContentModel(this));
      Automaton automobiile = new RegExp(dtdd.regexContentModel(this))
          .toAutomaton();
      automobiile.removeDeadTransitions();
      automobiile.restoreInvariant();

      Set<State> gen = new HashSet<State>();
      Set<Character> result = new HashSet<Character>();
      State initer = automobiile.getInitialState();
      gen.add(initer);

      for (int i = 1; i < 1; i++) {
        Set<State> nextGen = new HashSet<State>();
        for (State steak : gen) {
          for (Transition transss : steak.getTransitions()) {
            nextGen.add(transss.getDest());
          }
        }
        gen = nextGen;
      }

      for (State steak : gen) {
        for (Transition transss : steak.getTransitions()) {
          char min = transss.getMin();
          char max = transss.getMax();
          for (char i = min; i <= max; i++) {
            result.add(i);
          }
        }
      }
      System.out.println(dtdd + "-->" + result);
    }
  }
  */

  public DTD(ValidationContext context, String systemId, short io)
      throws IOException, XSLToolsSchemaException {
    init(context, systemId, io);
  }

  void init(ValidationContext context, String systemId, short io)
      throws IOException, XSLToolsSchemaException {
    String _systemId = context
        .getRootElementNameIdentifier(
            ResolutionContext.SystemInterfaceStrings[ResolutionContext.INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY
                + io], io);

    String documentElementNameFromContext = context.resolveString(_systemId,
        "Name of designated root element", "Auto-detect",
        ResolutionContext.INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY + io);

    if ("".equals(documentElementNameFromContext))
      documentElementNameFromContext = null;

    _systemId = context
        .getNamespaceURIIdentifier(
            //ResolutionContext.HUMAN_INTERFACE_STRINGS[ResolutionContext.INPUT_DTD_NAMESPACE_URI_KEY + io], io);
        	ResolutionContext.SystemInterfaceStrings[ResolutionContext.INPUT_DTD_NAMESPACE_URI_KEY + io], io);

    //System.err.println("Resolving uri");
    String namespaceURI = context.resolveString(_systemId,
        "Namespace URI for DTD-declared elements", "No namespace",
        ResolutionContext.INPUT_DTD_NAMESPACE_URI_KEY + io);
    //System.err.println("Resolved uri; got " + namespaceURI);

    context.pushMessage("context",
        "Got document element name from context: "
            + documentElementNameFromContext);
    context.pushMessage("context", "Got NS URI from context: "
        + namespaceURI);

    PerformanceLogger pa = DiagnosticsConfiguration.current
        .getPerformanceLogger();

    if (namespaceURI == null) {
      // alternatively NAMESPACE.NO_NAMESPACE, which should make no difference
      this.elementNamespace = Namespace.get(XMLConstants.DTD_NAMESPACE_PREFIX,
          "");
    } else {
      this.elementNamespace = Namespace.get(XMLConstants.DTD_NAMESPACE_PREFIX,
          namespaceURI);
    }
    // Read input DTD: (move to earlier?)
    // UNICODE mapping:

    // char for [root] type

    EntityResolver er = new EntityResolverWrapper
    (context, ResolutionContext.HUMAN_INTERFACE_STRINGS
        [ResolutionContext.INPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY],
        ResolutionContext.SCHEMA_SECONDARY_COMPONENT_IDENTIFIER_KEY);

    // Parse the DTD
    try {
      pa.startTimer("Parse", "InputSchema");
      dongfang.dtdparser.DTD rdtd = DTDParser.parse(systemId, er);
      pa.stopTimer("Parse", "InputSchema");

      // Construct initial paths-FA:
      // validAncestorPathFA = new Automaton();
      Map<QName, ElementDecl> tempNameToElementMap = new HashMap<QName, ElementDecl>();

      // Construct specialized DTDElement objects:
      for (dongfang.dtdparser.DTDElementDecl rdecl : rdtd.elementDecls()) {
        // Fetch DTD Parser element:
        String localName = rdecl.getName();
        QName name = QName.get(localName, elementNamespace);

        // Construct specialized object:
        DTDElementDecl newElement = new DTDElementDecl(this, name, rdecl);

        tempNameToElementMap.put(name, newElement);

        // Create paths FA state:
        // State elementState = new State();
        // pathsFAStates.put(name, elementState);
      }

      pa.startTimer("ProcessContentModel", "InputSchema");

      // Generate parents and children tables on DTDElement objects:
      CharGenerator charGen = new CharGenerator();

      for (ElementDecl element : getAllElementDecls()) {
        // fix up char names
        element.fixupCharacterNames(this, charGen);

        // Process content model:
        element.processContentModel(this, tempNameToElementMap);

        // Possibly add element as possible parent of comments and PIs:
        commentPIParents.addAll(element.getCommentsPIAcceptingVariations());

        // Possibly add element as possible parent of PCDATA:
        pcDataParents.addAll(element.getTextAcceptingVariations());
      }

      pa.stopTimer("ProcessContentModel", "InputSchema");

      detectDocumentElement(documentElementNameFromContext, context);
      // context.pushMessage("autodetect"," document element is: " + docElm);
      // legacy code; we do not use automata any more.
      if (1 != 1) {
        // Add paths-FA root state and edge:
        State overRootState = new State();
        State overOverRootState = new State();

        State commentOveroverroot = new State();
        State commentOverroot = new State();
        State commentAccept = new State();
        commentAccept.setAccept(true);
        commentNodeTypeAutomaton.setInitialState(commentOveroverroot);

        commentOveroverroot.addTransition(new Transition(CharGenerator
            .getRootChar(), commentOverroot));

        State PIOveroverroot = new State();
        State PIOverroot = new State();
        State PIAccept = new State();
        PIAccept.setAccept(true);
        PINodeTypeAutomaton.setInitialState(PIOveroverroot);

        PIOveroverroot.addTransition(new Transition(
            CharGenerator.getRootChar(), PIOverroot));

        State textOveroverroot = new State();
        State textOverroot = new State();
        State textAccept = new State();
        textAccept.setAccept(true);
        textNodeTypeAutomaton.setInitialState(textOveroverroot);

        textOveroverroot.addTransition(new Transition(CharGenerator
            .getRootChar(), textOverroot));

        schemaATSPathAutomaton.setInitialState(overOverRootState);
        overOverRootState.addTransition(new Transition(RootNT.instance
            .getCharRepresentation(this), overRootState));

        overRootState.setAccept(true);
        rootNodeTypeAutomaton = (Automaton) schemaATSPathAutomaton.clone();
        overRootState.setAccept(false);

        // Add paths-FA edges from root to document element, comment and pi:
        // QName docElementName = documentElement.name;

        pa.startTimer("CommonAutomata", "InputSchema");
        DTDElementDecl docElement = (DTDElementDecl) documentElementDecl;
        docElement.supportCommonATSPathAutomata(overRootState, commentOverroot,
            commentAccept, PIOverroot, PIAccept, textOverroot, textAccept);
        pa.stopTimer("CommonAutomata", "InputSchema");

        pa.startTimer("MakeAutomata", "InputSchema");

        for (ElementDecl decl : getAllElementDecls()) {
          ((DTDElementDecl) decl).snapshootATSPathAutomaton(this);
        }

        pa.stopTimer("MakeAutomata", "InputSchema");

        /*
         * Comments and PIs may occur next to doc element, undeclared.
         */
        commentOverroot.addTransition(new Transition(CommentNT.instance
            .getCharRepresentation(this), commentAccept));
        PIOverroot.addTransition(new Transition(PINT.chameleonInstance
            .getCharRepresentation(this), PIAccept));

        pa.startTimer("FixupCommonAutomata", "InputSchema");
        fixUpAutomata(overOverRootState, overRootState);
        pa.stopTimer("FixupCommonAutomata", "InputSchema");

        if (DO_SELF_TEST) {
          selfTest(context);
        }

        elementRegExp = charGen.getElementRegExp();
        attributeRegExp = charGen.getAttributeRegExp();

        context.pushMessage(this.getClass().getSimpleName(),
            "Ancestor path automata constructed");
      }
      diagnoseStats();
    } catch (DTDParseException ex) {
      throw new XSLToolsSchemaException(ex);
    }
  }

  protected String getElementNamespaceURI() {
    return elementNamespace.getURI();
  }

  protected void addElementDeclaration(DTDElementDecl decl) {
    super.addElementDecl(decl);
  }

  protected void addAttributeDeclaration(DTDAttributeDecl decl) {
    super.addAttributeDecl(decl);
  }
}
