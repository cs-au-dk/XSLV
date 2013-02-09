package dongfang.xsltools.controlflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.dom4j.Element;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Automata;
import dk.brics.misc.Origin;
import dk.brics.relaxng.Param;
import dk.brics.relaxng.converter.StandardDatatypes;
import dk.brics.xmlgraph.AttributeNode;
import dk.brics.xmlgraph.ChoiceNode;
import dk.brics.xmlgraph.ElementNode;
import dk.brics.xmlgraph.InterleaveNode;
import dk.brics.xmlgraph.Node;
import dk.brics.xmlgraph.OneOrMoreNode;
import dk.brics.xmlgraph.SequenceNode;
import dk.brics.xmlgraph.TextNode;
import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.xmlclass.xsd.XSDSchemaConstants;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.PINT;

public class SGFragment extends HashMap<DeclaredNodeType, ChoiceNode> {

  /**
	 * 
	 */
	private static final long serialVersionUID = -1728951480961790627L;

private XMLGraph myGraph;

  private Node entryNode;

  private Node[] epsilon = new Node[1];

  static StandardDatatypes stddt = new StandardDatatypes();

  int anyAttributeNode;

  int anyElementNode;

  String originString;

  public SGFragment(XMLGraph g, String originString) {
    this.myGraph = g;
    if (DiagnosticsConfiguration.useOriginStringsForFragments()) {
      this.originString = originString;
    }
  }

  public SGFragment(SGFragment fraggle, String originString) {
    this.myGraph = fraggle.getXMLGraph();
    this.epsilon = fraggle.epsilon;
    if (DiagnosticsConfiguration.useOriginStringsForFragments()) {
      this.originString = originString;
    }
  }

  private <T extends Node> T register(T n) {
    myGraph.addNode(n);
    return n;
  }

  public ChoiceNode createChoiceNode(Collection<Integer> content, Origin origin) {
    return register(new ChoiceNode(content, origin));
  }

  public ElementNode createElementNode(Automaton name, int content,
      Origin origin) {
    return register(new ElementNode(name, content, true, origin));
  }

  int maxPermutationCount = 3;

  public Node createInterleaveNode(Collection<Integer> content,
      boolean allowInterleave, Origin origin) {

    if (allowInterleave) {
      return register(new InterleaveNode(content, origin));
    }

    if (content.size() > maxPermutationCount) {
      return createOneOrMoreNode(createChoiceNode(content,
          new Origin("InterleaveSubstPerm", 0, 0)).getIndex(), origin);
    }

    LinkedList<Integer> somePermutation = new LinkedList<Integer>(content);

    return createPermutationMonster(somePermutation, origin);
  }

  public Node createPermutationMonster(Collection<Integer> somePermutation,
      Origin origin) {
    List<Integer> result = new LinkedList<Integer>();
    createPermutationMonster(new LinkedList<Integer>(),
        new LinkedList<Integer>(somePermutation), result, somePermutation
            .size(), origin);
    if (result.size() == 1)
      return getNodeAt(result.get(0));
    return createChoiceNode(result, origin);
  }

  void createPermutationMonster(LinkedList<Integer> settled,
      List<Integer> unsettled, Collection<Integer> result, int nsettled,
      Origin origin) {
    if (settled.size() == nsettled)
      if (unsettled.isEmpty())
        result.add(createSequenceNode(new ArrayList<Integer>(settled), origin)
            .getIndex());
    for (int i = 0; i < unsettled.size(); i++) {
      Integer I = unsettled.remove(i);
      settled.add(I);
      createPermutationMonster(settled, unsettled, result, nsettled, origin);
      unsettled.add(i, I);
      settled.removeLast();
    }
  }

  public OneOrMoreNode createOneOrMoreNode(int content, Origin origin) {
    return register(new OneOrMoreNode(content, origin));
  }

  public OneOrMoreNode createOneOrMoreNode(Node content, Origin origin) {
    return register(new OneOrMoreNode(content.getIndex(), origin));
  }

  public SequenceNode createSequenceNode(List<Integer> content, Origin origin) {
    return register(new SequenceNode(content, origin));
  }

  public TextNode createTextNode(Automaton text, Origin origin) {
    return register(new TextNode(text, origin));
  }

  public AttributeNode createAttributeNode(Automaton name, int content,
      Origin origin) {
    return register(new AttributeNode(name, content, origin));
  }

  public ChoiceNode createPlaceholder(DeclaredNodeType type) {
    Origin origin = type.getDeclarationOrigin();//new Origin("placeholder-for-" + type.toLabelString(), 0, 0);
    return createPlaceholder(type, origin);
  }
  
    public ChoiceNode createPlaceholder(DeclaredNodeType type, Origin origin) {
    ChoiceNode n = get(type);
    if (n == null) {
      Set<Integer> e = new HashSet<Integer>();// Collections.emptySet();
      n = createChoiceNode(e, origin);
      put(type, n);
    }
    return n;
  }

  public void setEntryNode(Node n) {
    entryNode = n;
  }

  public Node createEpsilonNode() {
    if (epsilon[0] == null) {
      List<Integer> el = Collections.emptyList();
      epsilon[0] = createSequenceNode(el, new Origin("epsilon", 0, 0));
    }
    return epsilon[0];
  }

  public Node constructOptionalCardinal(int victim, Origin origin) {
    Collection<Integer> pair = new ArrayList<Integer>(2);
    pair.add(victim);
    pair.add(createEpsilonNode().getIndex());
    return createChoiceNode(pair, origin);
  }

  public Node constructOptionalCardinal(Node victim, String origin) {
    return constructOptionalCardinal(victim.getIndex(), origin);
  }

  public Node constructOptionalCardinal(int victim, String origin) {
    return constructOptionalCardinal(victim, new Origin(origin + "-?", 0, 0));
  }

  public Node constructOneManyCardinal(int victim, String origin) {
    return createOneOrMoreNode(victim, new Origin(origin + "-+", 0, 0));
  }

  public Node constructOneManyCardinal(Node victim, String origin) {
    return constructOneManyCardinal(victim.getIndex(), origin);
  }

  public Node constructOptionalCardinal(Node victim, Origin origin) {
    return constructOptionalCardinal(victim.getIndex(), origin);
  }

  public Node constructOneManyCardinal(int victim, Origin origin) {
    return createOneOrMoreNode(victim, new Origin(origin + "-+", 0, 0));
  }

  public Node constructOneManyCardinal(Node victim, Origin origin) {
    return constructOneManyCardinal(victim.getIndex(), origin);
  }

  public Node constructZeroManyCardinal(int victim, String origin) {
    /*
     * return constructOneManyCardinal(constructOptionalCardinal(victim,
     * fragment, origin), fragment, origin);
     */
    return constructOptionalCardinal(constructOneManyCardinal(victim, origin),
        origin);
  }

  /*
   * public static int constructZeroManyCardinal(int victim, SGFragment
   * fragment, String origin) { return constructOptionalCardinal(
   * constructOneManyCardinal(victim, fragment, origin), fragment, origin); }
   */
  public Node constructZeroManyCardinal(Node victim, String origin) {
    return constructZeroManyCardinal(victim.getIndex(), origin);
  }

  /*
   * Zero or more (comment or PI) We might be ignoring a tight corner here,
   * distinguishing between several different colors of PINT...
   */
  private Node createCommentPIConstruct() {
    Collection<Integer> contents = new ArrayList<Integer>(2);
    contents.add(createPlaceholder(CommentNT.instance).getIndex());
    contents.add(createPlaceholder(PINT.chameleonInstance).getIndex());
    Node goose = createChoiceNode(contents, new Origin("comment-pi-construct",
        0, 0));
    return constructZeroManyCardinal(goose, "comment-pi-construct");
  }

  public Node wrapInCommentPIConstruct(Node victim, boolean wantsCommentPI, Origin containerOrigin) {
    if (!ControlFlowConfiguration.current.useCommentPIPropagation()
        || !wantsCommentPI)
      return victim;
    List<Integer> contents = new ArrayList<Integer>(3);
    int cpi = createCommentPIConstruct().getIndex();
    contents.add(cpi);
    contents.add(victim.getIndex());
    contents.add(cpi);
    /*
     * This is basically wrong, as comments and PIs may appear anywhere in a
     * content model... we just slam it before and after, better than nothing
     * anyway...
     */
    return createSequenceNode(contents, containerOrigin);
  }

  public Node getEntryNode() {
    return entryNode;
  }

  /*
   * public DeclaredNodeType getEntryType() { return entryType; }
   */

  public XMLGraph getXMLGraph() {
    return myGraph;
  }

  private Map<DeclaredNodeType, SGFragment> stom = new HashMap<DeclaredNodeType, SGFragment>();

  public void eat(DeclaredNodeType type, SGFragment fragment) {
    if ((type == CommentNT.instance || type instanceof PINT)
        && !ControlFlowConfiguration.current.useCommentPIPropagation())
      return;
    stom.put(type, fragment);
  }

  public void hookup() {
    for (Map.Entry<DeclaredNodeType, SGFragment> other : stom.entrySet()) {
      DeclaredNodeType otherType = other.getKey();
      // Set<ChoiceNode> sockets = get(otherType);
      ChoiceNode socket = get(otherType);
      // if (sockets == null || sockets.isEmpty())
      /*
       * if (socket == null) { throw new NullPointerException("Nowhere to plug " +
       * otherType + " in " + this); }
       */
      SGFragment fraggle = other.getValue();
      /*
       * if (fraggle == null) throw new NullPointerException("Node type " +
       * otherType + " mapped to null");
       */
      Node otherEntryNode = fraggle.getEntryNode();
      /*
       * if (otherEntryNode == null) { System.err.println("Danm, entry node null
       * for:" + otherType); }
       */
      Collection<Integer> oldies = socket.getContents();
      Collection<Integer> merged = new HashSet<Integer>(oldies);
      merged.add(otherEntryNode.getIndex());
      socket.setContent(merged, myGraph);
    }

    clear();
  }
  
  public void flush() {

    hookup();
    
    Map<DeclaredNodeType, List<Integer>> dupes = new HashMap<DeclaredNodeType, List<Integer>>();
    for (SGFragment other : stom.values()) {
      for (Map.Entry<DeclaredNodeType, ChoiceNode> e : other.entrySet()) {
        List<Integer> cns = dupes.get(e.getKey());
        if (cns == null) {
          cns = new LinkedList<Integer>();
          dupes.put(e.getKey(), cns);
        }
        cns.add(e.getValue().getIndex());
      }
    }

    for (Map.Entry<DeclaredNodeType, List<Integer>> e : dupes.entrySet()) {
      if (e.getValue().size() == 1) {
        int idx = e.getValue().get(0);
        put(e.getKey(), (ChoiceNode) myGraph.getNode(idx));
      } else if (e.getValue().size() == 0) {
        System.err.println("WEIRD!");
      } else {
        Collection<Integer> contents = new HashSet<Integer>();
        ChoiceNode n = createChoiceNode(contents, new Origin("flush", 0, 0));
        put(e.getKey(), n);

        for (Integer I : e.getValue()) {
          ChoiceNode oldies = (ChoiceNode) myGraph.getNode(I);
          Collection<Integer> oldconts = oldies.getContents();
          oldconts.add(n.getIndex());
        }
      }
    }
    stom.clear();
  }

  public Node getNodeAt(int index) {
    return getXMLGraph().getNode(index);
  }

  /*
   * public ChoiceNode getRootChoose() { Node root = getNodeAt(0); return
   * (ChoiceNode)root; }
   */
  /*
   * public static Automaton makeClarkAutomaton2(Automaton namespaceAutomaton,
   * Automaton localNameAutomaton) { if
   * (namespaceAutomaton.equals(Automaton.makeEmpty())) throw new
   * AssertionError("Empty-language namespace automaton. Can't be.");
   * 
   * if (namespaceAutomaton.equals(Automaton.makeEmptyString())) return
   * localNameAutomaton;
   * 
   * Automaton result = Automaton.makeString("{").concatenate(
   * namespaceAutomaton).concatenate(Automaton.makeString("}").concatenate(localNameAutomaton));
   * 
   * String x = result.getShortestExample(true);
   * 
   * return result; }
   */

  /*
   * Return the namespace automaton for the namespace string given
   */
  /*
   * Automaton makeNamespaceAutomaton ( String namespace, String
   * schemaDocumentNSURI) {
   * 
   * Automaton nsAutomaton;
   * 
   * List<Param> el = Collections.emptyList();
   * 
   * if (namespace==null) namespace = "##any"; else namespace =
   * namespace.trim();
   * 
   * if (namespace.equals("##any")) { // all URIs and the null NS if (stddt ==
   * null) return Automaton.makeString("Verdammt:anyURI"); nsAutomaton =
   * stddt.datatypeToAutomaton( XSDSchemaConstants.DATATYPE_NAMESPACE_URI,
   * "anyURI", el); nsAutomaton =
   * nsAutomaton.union(Automaton.makeEmptyString()); } else if
   * (namespace.equals("##other")) { // all URIs if (stddt == null) return
   * Automaton.makeString("Verdammt:otherURI"); nsAutomaton =
   * //stddt.datatypeToAutomaton( //XSDSchemaConstants.DATATYPE_NAMESPACE_URI,
   * "anyURI", el); Automata.get("anyURI"); if (!schemaDocumentNSURI.equals("")) { //
   * except target, if there is one nsAutomaton =
   * nsAutomaton.intersection(Automaton.makeString(
   * schemaDocumentNSURI).complement()); } } else { StringTokenizer st = new
   * StringTokenizer(namespace, " "); nsAutomaton = Automaton.makeEmpty(); while
   * (st.hasMoreTokens()) { String token = st.nextToken(); if
   * ("##targetNamespace".equals(token)) { nsAutomaton =
   * nsAutomaton.union(Automaton .makeString(schemaDocumentNSURI)); } else if
   * ("##local".equals(token)) { nsAutomaton =
   * nsAutomaton.union(Automaton.makeEmptyString()); } else { nsAutomaton =
   * nsAutomaton.union(Automaton.makeString(token)); } } } return nsAutomaton; }
   */

  public static Automaton clampOnCurlies(Automaton victim) {
    return Automaton.makeChar('{').concatenate(victim).concatenate(
        Automaton.makeChar('}'));
  }

  /*
   * Return the namespace automaton for the namespace string given
   */
  Automaton makeNamespaceAutomaton2(String namespace, String schemaDocumentNSURI) {

    Automaton nsAutomaton;

    if (namespace == null)
      namespace = "##any";
    else
      namespace = namespace.trim();

    if (namespace.equals("##any")) {
      // all URIs and the null NS
      if (stddt == null)
        return Automaton.makeString("Verdammt:anyURI");
      nsAutomaton = clampOnCurlies(Automata.get("URI"));
      nsAutomaton = nsAutomaton.union(Automaton.makeEmptyString());
      return nsAutomaton;
    } else if (namespace.equals("##other")) {
      // all URIs
      if (stddt == null)
        return Automaton.makeString("Verdammt:otherURI");
      nsAutomaton = Automata.get("anyURI");
      if (!schemaDocumentNSURI.equals("")) {
        // except target, if there is one
        nsAutomaton = nsAutomaton.intersection(Automaton.makeString(
            schemaDocumentNSURI).complement());
      }
      return clampOnCurlies(nsAutomaton);
    } else {
      StringTokenizer st = new StringTokenizer(namespace, " ");
      nsAutomaton = Automaton.makeEmpty();
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        if ("##targetNamespace".equals(token)) {
          nsAutomaton = nsAutomaton.union(clampOnCurlies(Automaton
              .makeString(schemaDocumentNSURI)));
        } else if ("##local".equals(token)) {
          nsAutomaton = nsAutomaton.union(Automaton.makeEmptyString());
        } else {
          nsAutomaton = nsAutomaton.union(clampOnCurlies(Automaton
              .makeString(token)));
        }
      }
      return nsAutomaton;
    }
  }

  /*
   * Return the namespace automaton for the any or anyAttribute decl given.
   */
  Automaton namespaceAutomatonForElement(Element any) {
    String namespace = any
        .attributeValue(XSDSchemaConstants.ATTR_NAMESPACE_QNAME);

    if (namespace == null)
      namespace = "##any";
    else
      namespace = namespace.trim();

    // this attribute was copied in at flattening time.
    String schemaDocumentNSURI = any
        .attributeValue(XSDSchemaConstants.ATTR_TARGET_NAMESPACE_QNAME);

    if (schemaDocumentNSURI == null) {
      schemaDocumentNSURI = "";
    }

    return makeNamespaceAutomaton2(namespace, schemaDocumentNSURI);
  }

  /*
   * Make a construct approximating the result of evaluting a template with
   * anyNode as the context node: We have generally no idea what that end up
   * like ... we return a messy glob of anything and noting in particular.
   */
  Node constructAnyElement2(Automaton namespaceAutomaton,
      boolean allowInterleave, Origin origin) {
    Automaton ncname;
    ncname = Automata.get("NCName");
    ElementNode ztingr = createElementNode(
    // makeClarkAutomaton(namespaceAutomaton, ncname),
        namespaceAutomaton.concatenate(ncname), 0, origin);

    // Any attribute in any namespace at all.
    int att = constructAnyAttribute(makeNamespaceAutomaton2(null, null), origin)
        .getIndex();

    int text = createTextNode(Automata.get("string"), null).getIndex();

    List<Integer> interleaveOptions = new ArrayList<Integer>(3);
    // interleaveOptions.add(att);
    interleaveOptions.add(text);
    interleaveOptions.add(ztingr.getIndex());

    Node content = createInterleaveNode(interleaveOptions, allowInterleave,
        null);

    List<Integer> interleaveAndAttOptions = new LinkedList<Integer>();
    interleaveAndAttOptions.add(content.getIndex());
    interleaveAndAttOptions.add(att);

    Node contentAndAttributes = createSequenceNode(interleaveAndAttOptions,
        null);

    content = constructZeroManyCardinal(contentAndAttributes, "nested");

    ztingr.setContent(content.getIndex());

    // TODO: Any number of nasty attributes on it as well.
    return ztingr;
  }

  Node constructAnyElement(boolean allowInterleave, Origin origin) {
    // return constructAnyElement2(makeNamespaceAutomaton2(null, null),
    // allowInterleave, origin);
    return constructSimplifiedAnyElement(makeNamespaceAutomaton2(null, null),
        origin);
  }

  Node constructAnyAttribute(Automaton namespaceAutomaton, Origin origin) {

    List<Param> el = Collections.emptyList();

    Automaton ncname = stddt.datatypeToAutomaton(
        XSDSchemaConstants.DATATYPE_NAMESPACE_URI, "NCName", el);

    TextNode content = createTextNode(Automata.get("string"), origin);

    // Automaton nameAut = makeClarkAutomaton(namespaceAutomaton, ncname);

    AttributeNode cm = createAttributeNode(namespaceAutomaton
        .concatenate(ncname), content.getIndex(), origin);

    return cm;
  }

  Node constructAnyAttribute(Element any, Origin origin) {
    return constructAnyAttribute(namespaceAutomatonForElement(any), origin);
  }

  Node constructAnyAttribute(Origin origin) {
    return constructAnyAttribute(makeNamespaceAutomaton2(null, null), origin);
  }

  Node constructSimplifiedAnyElement(Automaton namespaceAutomaton, Origin origin) {
    Automaton ncname = Automata.get("NCName");

    ElementNode ztingr = createElementNode(
    // makeClarkAutomaton(namespaceAutomaton, ncname),
        namespaceAutomaton.concatenate(ncname), 0, origin);

    return ztingr;
  }

  public Node getAnyElement(boolean allowInterleave) {
    if (anyElementNode == 0)
      anyElementNode = constructAnyElement(allowInterleave, null).getIndex();
    return getNodeAt(anyElementNode);
  }

  public Node getAnyAttribute() {
    if (anyAttributeNode == 0)
      anyAttributeNode = constructAnyAttribute(null).getIndex();
    return getNodeAt(anyAttributeNode);
  }

  /*
   * public int getRootIndex() { return 0; }
   */

  @Override
public String toString() {
    String result = "SGFragment ";
    if (originString != null)
      result += originString;
    else
      result += "noOriginString";
    result += "\n";
    result += "EntryNode= " + getEntryNode() + "\n";
    for (Map.Entry<DeclaredNodeType, ChoiceNode> e : entrySet()) {
      result += "Type: " + e.getKey() + "-->" + e.getValue() + "\n";
    }
    return result;
  }
}
