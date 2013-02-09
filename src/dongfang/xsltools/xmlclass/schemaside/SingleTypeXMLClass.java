/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.xmlclass.schemaside;

import java.io.FileWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.xmlgraph.ChoiceNode;
import dk.brics.xmlgraph.Node;
import dk.brics.xmlgraph.XMLGraph;
import dk.brics.xmlgraph.converter.XMLGraphReducer;
import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsUnhandledNodeTestException;
import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.CharGenerator;
import dongfang.xsltools.xmlclass.schemaside.dropoff.CardinalMatch;
import dongfang.xsltools.xmlclass.schemaside.dropoff.DynamicRedeclaration;
import dongfang.xsltools.xmlclass.xslside.AbstractNodeType;
import dongfang.xsltools.xmlclass.xslside.AttributeNT;
import dongfang.xsltools.xmlclass.xslside.CharNameResolver;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.ElementNT;
import dongfang.xsltools.xmlclass.xslside.NodeType;
import dongfang.xsltools.xmlclass.xslside.PINT;
import dongfang.xsltools.xmlclass.xslside.RootNT;
import dongfang.xsltools.xmlclass.xslside.TextNT;
import dongfang.xsltools.xmlclass.xslside.UndeclaredNodeType;
import dongfang.xsltools.xpath2.XPathAbsolutePathExpr;
import dongfang.xsltools.xpath2.XPathAnyNodeTest;
import dongfang.xsltools.xpath2.XPathAxisStep;
import dongfang.xsltools.xpath2.XPathCommentTest;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathNameTest;
import dongfang.xsltools.xpath2.XPathNodeTest;
import dongfang.xsltools.xpath2.XPathNumericLiteral;
import dongfang.xsltools.xpath2.XPathPITest;
import dongfang.xsltools.xpath2.XPathParser;
import dongfang.xsltools.xpath2.XPathPathExpr;
import dongfang.xsltools.xpath2.XPathStepExpr;
import dongfang.xsltools.xpath2.XPathTextTest;
import dongfang.xsltools.xpath2.XPathUnionExpr;

/**
 * @author dongfang
 */
public abstract class SingleTypeXMLClass implements CharNameResolver,
		Diagnoseable {

	public static final short ABSOLUTE_DEATH_CAUSE = 1;

	public static final short AXIS_DEATH_CAUSE = 2;

	public static final short TESTSET_DEATH_CAUSE = 3;

	public static final short PARENTSET_DEATH_CAUSE = 4;

	public static final short MCSAXIS_SURVIVAL_CAUSE = -1;

	public static final short MATCHAXIS_SURVIVAL_CAUSE = -2;

	public static final short EXHAUSTED_SURVIVAL_CAUSE = -3;

	/*
	 * Automaton init things
	 */
	protected static final boolean DO_RESTORE_INVARIANT = true;

	protected static final boolean DO_REMOVE_DEAD_TRANSITIONS = false;

	protected static final boolean DO_SELF_TEST = false;

	protected static final boolean VERBOSE_AUTOMATA_INFO = false;

	protected static final boolean REMOVE_UNREACHABLE_DECLARATIONS = false;

	protected Map<QName, Character> elementQNameToCharMap = new HashMap<QName, Character>();

	protected Map<QName, Character> attributeQNameToCharMap = new HashMap<QName, Character>();

	/*
	 * Inited properly RNG: DTD:
	 */
	private Set<ElementDecl> allElementDecls = new HashSet<ElementDecl>();

	/*
	 * Inited properly RNG: DTD:
	 */
	private Map<QName, Set<ElementDecl>> allElementDeclsByName = new HashMap<QName, Set<ElementDecl>>();

	/*
	 * Inited properly RNG: DTD:
	 */
	private Set<AttributeDecl> allAttributeDecls = new HashSet<AttributeDecl>();

	/*
	 * Inited properly RNG: DTD:
	 */
	private Map<QName, Set<AttributeDecl>> allAttributeDeclsByName = new HashMap<QName, Set<AttributeDecl>>();

	/*
	 * Set of all element decls that permit PCDATA, for use in abstract eval.
	 * [root] does not permit, and should not be added.
	 */
	/*
	 * Inited properly RNG: DTD:
	 */
	protected Set<ElementUse> pcDataParents = new HashSet<ElementUse>();

	/*
	 * Should not be needed -- all elements generally permit comments and PIs,
	 * plus PCDATA and [root] (all right, we put them in the set, then) [root]
	 * does permit, but is not added.
	 */
	/*
	 * Inited properly RNG: Y, DTD:
	 */
	protected Set<ElementUse> commentPIParents = new HashSet<ElementUse>();

	/*
	 * Inited properly RNG: DTD:
	 */
	protected ElementDecl documentElementDecl;

	/*
	 * Inited properly RNG: Y, DTD:
	 */
	protected String elementRegExp;

	/*
	 * Inited properly RNG: Y, DTD:
	 */
	protected String attributeRegExp;

	/*
	 * Automaton of everything
	 */
	/*
	 * Inited properly RNG: Y, DTD:
	 */
	protected Automaton schemaATSPathAutomaton = new Automaton();

	/*
	 * Inited properly RNG: Y, DTD:
	 */
	protected Automaton rootNodeTypeAutomaton;

	protected Automaton commentNodeTypeAutomaton = new Automaton();

	protected Automaton PINodeTypeAutomaton = new Automaton();

	protected Automaton textNodeTypeAutomaton = new Automaton();

	/**
	 * Get an automaton representing the VDPL language over the regular tree
	 * typed schema.
	 * 
	 * @return
	 */
	public Automaton getSchemaATSPathAutomaton() {
		return schemaATSPathAutomaton;
	}

	public char getCharForElementName(QName name) {
		Character c = elementQNameToCharMap.get(name);
		if (c == null)
			return CharGenerator.getAbsurdChar();
		return c.charValue();
	}

	public char getCharForAttributeName(QName name) {
		Character c = attributeQNameToCharMap.get(name);
		if (c == null)
			return CharGenerator.getAbsurdChar();
		return c.charValue();
	}

	public ElementDecl getDocumentElementDecl() {
		return documentElementDecl;
	}

	protected void addElementDecl(ElementDecl decl) {
		allElementDecls.add(decl);

		Set<ElementDecl> aliases = allElementDeclsByName.get(decl.getQName());
		if (aliases == null)
			aliases = Collections.singleton(decl);
		else if (aliases.size() == 1) {
			aliases = new HashSet<ElementDecl>(aliases);
			aliases.add(decl);
		} else
			aliases.add(decl);
		allElementDeclsByName.put(decl.getQName(), aliases);
	}

	protected void removeElementDecl(ElementDecl decl) {
		Set<ElementDecl> aliases = allElementDeclsByName.get(decl.getQName());
		if (aliases == null) {
			return;
		} else if (aliases.size() == 1) {
			allElementDeclsByName.remove(decl.getQName());
		} else {
			aliases.remove(decl);
		}
	}

	public Set<? extends ElementDecl> getAllElementDecls() {
		return allElementDecls;
	}

	public Map<QName, ? extends Set<ElementDecl>> getAllElementDeclsByName() {
		return allElementDeclsByName;
	}

	protected void addAttributeDecl(AttributeDecl decl) {
		allAttributeDecls.add(decl);
		Set<AttributeDecl> aliases = allAttributeDeclsByName.get(decl
				.getQName());
		if (aliases == null)
			aliases = Collections.singleton(decl);
		else if (aliases.size() == 1) {
			aliases = new HashSet<AttributeDecl>(aliases);
			aliases.add(decl);
		}
		allAttributeDeclsByName.put(decl.getQName(), aliases);
	}

	protected void removeAttributeDecl(AttributeDecl decl) {
		allAttributeDecls.remove(decl);
		Set<AttributeDecl> aliases = allAttributeDeclsByName.get(decl
				.getQName());
		if (aliases == null) {
			return;
		} else if (aliases.size() == 1) {
			allAttributeDeclsByName.remove(decl.getQName());
		} else {
			aliases.remove(decl);
		}
	}

	protected void diagnoseStats() {
		PerformanceLogger pa = DiagnosticsConfiguration.current.getPerformanceLogger();
		pa.setValue("ElementCount", "Schema", getElementTypeCount());
		pa.setValue("AttributeCount", "Schema", getAttributeTypeCount());
	}

	protected void fixUpAutomata(State dontaccept, State root) {
		Iterator stateIter = schemaATSPathAutomaton.getStates().iterator();

		while (stateIter.hasNext()) {
			State state = (State) stateIter.next();

			if (state != dontaccept && state != root)
				state.setAccept(true);
		}

		if (DO_RESTORE_INVARIANT)
			commentNodeTypeAutomaton.restoreInvariant();
		commentNodeTypeAutomaton.setDeterministic(false);
		if (DO_REMOVE_DEAD_TRANSITIONS)
			commentNodeTypeAutomaton.removeDeadTransitions();

		if (DO_RESTORE_INVARIANT)
			PINodeTypeAutomaton.restoreInvariant();
		PINodeTypeAutomaton.setDeterministic(false);
		if (DO_REMOVE_DEAD_TRANSITIONS)
			PINodeTypeAutomaton.removeDeadTransitions();

		if (DO_RESTORE_INVARIANT)
			textNodeTypeAutomaton.restoreInvariant();
		textNodeTypeAutomaton.setDeterministic(false);
		if (DO_REMOVE_DEAD_TRANSITIONS)
			textNodeTypeAutomaton.removeDeadTransitions();

		if (DO_RESTORE_INVARIANT)
			rootNodeTypeAutomaton.restoreInvariant();
		rootNodeTypeAutomaton.setDeterministic(false);
		if (DO_REMOVE_DEAD_TRANSITIONS)
			rootNodeTypeAutomaton.removeDeadTransitions();

		schemaATSPathAutomaton = schemaATSPathAutomaton.union(
				rootNodeTypeAutomaton).union(commentNodeTypeAutomaton).union(
				PINodeTypeAutomaton).union(textNodeTypeAutomaton);
	}

	/*
	 * Get size of transitive clousure of descendant element types. Used for
	 * root guessing.
	 */
	private int sizeOfReachableClosure(ElementDecl e) {
		Set<ElementDecl> done = new HashSet<ElementDecl>();
		LinkedList<ElementDecl> work = new LinkedList<ElementDecl>();
		work.add(e);
		done.add(e);
		while (!work.isEmpty()) {
			e = work.removeFirst();
			Collection<? extends ElementDecl> children = e
					.getWidenedChildElementDecls();
			for (ElementDecl ch : children) {
				Collection<? extends ElementDecl> childrenss = ch
						.getSGRPSubstituteableElementDecls();
				for (ElementDecl ch2 : childrenss) {
					if (!done.contains(ch2)) {
						done.add(ch2);
						work.add(ch2);
					}
				}
			}
			for (ElementDecl ch : e.getSGRPSubstituteableElementDecls()) {
				if (!done.contains(ch)) {
					done.add(ch);
					work.add(ch);
				}
			}
		}
		return done.size();
	}

	/*
	 * Tactique: See who has the biggest reachable component.
	 */
	protected ElementDecl guessDocumentElementDecl() {
		ElementDecl recordHolder = null;
		int record = -1;
		for (ElementDecl e : allElementDecls) {
			if (e.isAbstract())
				continue;
			int i = sizeOfReachableClosure(e);
			if (i > record) {
				record = i;
				recordHolder = e;
			}
		}
		return recordHolder;
	}

	/*
	 * Accept user hint for document element name. TODO: Qualify by URI.
	 */
	protected ElementDecl sniffElementDecl(String name) {
		for (ElementDecl decl : getAllElementDecls()) {
			ElementDecl test = decl;
			if (test.getQName().getName().equals(name))
				return test;
		}
		return null;
	}

	/*
	 * Determine which decl declares the document element. The autodetection
	 * works really well and there is almost? never any user interference
	 * needed.
	 */
	protected String detectDocumentElement(String rootElementNameFromContext,
			ValidationContext context) throws XSLToolsSchemaException {
		if (rootElementNameFromContext != null
				&& !"".equals(rootElementNameFromContext)) {
			ElementDecl root = sniffElementDecl(rootElementNameFromContext);
			if (root == null) {
				throw new XSLToolsSchemaException(
						"No element declaration was found with the name "
								+ rootElementNameFromContext);
			}
			documentElementDecl = root;
		} else {
			documentElementDecl = guessDocumentElementDecl();
			String rootName = null;
			if (documentElementDecl != null) {
				rootName = Dom4jUtil.clarkName(documentElementDecl.getQName());
			}
			context.pushMessage("autodetect", "Detected root element name: "
					+ rootName);
		}
		return Dom4jUtil.clarkName(documentElementDecl.getQName());
	}

	protected void selfTest(ValidationContext context) {
		PerformanceLogger pa = DiagnosticsConfiguration.current
				.getPerformanceLogger();

		pa.startTimer("SelfTest", "InputSchema");

		// 1: Test all languages mutually exclusive
		Map<QName, Automaton> allAutomata = new HashMap<QName, Automaton>();

		for (ElementDecl decl : allElementDecls)
			allAutomata.put(decl.getQName(), decl.getATSAutomaton(this));

		for (AttributeDecl decl : allAttributeDecls)
			allAutomata.put(decl.getQName(), decl.getATSAutomaton(this));

		allAutomata.put(QName.get("comment-pseudoname"),
				commentNodeTypeAutomaton);

		allAutomata.put(QName.get("pi-pseudoname"), PINodeTypeAutomaton);

		allAutomata.put(QName.get("text-pseudoname"), textNodeTypeAutomaton);

		allAutomata.put(QName.get("root-pseudoname"), rootNodeTypeAutomaton);

		Automaton total = new Automaton();

		for (Map.Entry<QName, Automaton> ae : allAutomata.entrySet()) {
			for (Map.Entry<QName, Automaton> be : allAutomata.entrySet()) {
				if (ae.getKey() == be.getKey())
					continue;
				Automaton test1 = ae.getValue().intersection(be.getValue());
				if (!test1.isEmpty())
					context
							.pushMessage(
									getClass().getSimpleName(),
									"Error - Automaton for "
											+ Dom4jUtil.clarkName(ae.getKey())
											+ " had nonempty intersection with that for "
											+ Dom4jUtil.clarkName(be.getKey())
											+ " : "
											+ test1.getFiniteStrings()
											+ ". The schema is not single type.");

			}
			total = total.union(ae.getValue());
		}

		for (Map.Entry<QName, Automaton> ae : allAutomata.entrySet()) {
			if (ae.getValue().isEmpty()) {
				context.pushMessage(getClass().getSimpleName(), "Node type "
						+ Dom4jUtil.clarkName(ae.getKey())
						+ " has empty language. May not be an error.");
			}

			Automaton inter = schemaATSPathAutomaton
					.intersection(ae.getValue());

			if (!inter.equals(ae.getValue()))
				context
						.pushMessage(
								getClass().getSimpleName(),
								"Language of "
										+ Dom4jUtil.clarkName(ae.getKey())
										+ " is not contained in schema language. Internal error in the class.");
		}

		Automaton blamm = schemaATSPathAutomaton.intersection(total
				.complement());

		if (!blamm.isEmpty()) {
			context
					.pushMessage(
							getClass().getSimpleName(),
							"Schema automaton seemed to have a larger language than the union automaton of all type languages: "
									+ blamm + ". Internal error in the class.");

			boolean found = false;

			for (Map.Entry<QName, Automaton> ae : allAutomata.entrySet()) {
				Automaton test = ae.getValue().intersection(blamm);
				if (!test.isEmpty()) {
					found = true;
					context.pushMessage(getClass().getSimpleName(),
							"The too large part of the schema auto had something in common w that of "
									+ Dom4jUtil.clarkName(ae.getKey()));
				}
			}
			if (!found)
				context.pushMessage(getClass().getSimpleName(),
						"The too large part was disjoint w all type languages");
			context.pushMessage(getClass().getSimpleName(),
					"The too large part has an example string: "
							+ blamm.getShortestExample(true));
		}

		blamm = total.intersection(schemaATSPathAutomaton.complement());

		if (!blamm.isEmpty()) {
			context
					.pushMessage(
							getClass().getSimpleName(),
							"Schema automaton seemed to have a smaller language than the union automaton of all type languages: "
									+ blamm + ". Internal error in the class.");

			boolean found = false;

			for (Map.Entry<QName, Automaton> ae : allAutomata.entrySet()) {
				Automaton test = ae.getValue().intersection(blamm);
				if (!test.isEmpty()) {
					context.pushMessage(getClass().getSimpleName(),
							"The too large part of the schema auto had something in common w that of "
									+ Dom4jUtil.clarkName(ae.getKey()));
					found = true;
				}
			}
			if (!found)
				context.pushMessage(getClass().getSimpleName(),
						"The too small part was disjoint w all type languages");
		}

		pa.stopTimer("SelfTest", "InputSchema");
	}

	/***************************************************************************
	 * * And now to the core difference between local and single -- finding
	 * types! **
	 **************************************************************************/

	/**
	 * Search EVERYTHING for matches. Only useful for comparing some smarter
	 * approach with in a test.
	 */
	public Set<Declaration> paranoicallyGetDeclarationsFor(
			Automaton ancestorLanguageSuperlang) {

		Set<Declaration> result = new HashSet<Declaration>();

		for (ElementDecl type : allElementDecls) {
			Automaton intersection = ancestorLanguageSuperlang
					.intersection(type.getATSAutomaton(this));
			if (!intersection.isEmpty())
				result.add(type);
		}

		for (AttributeDecl type : allAttributeDecls) {
			Automaton intersection = ancestorLanguageSuperlang
					.intersection(type.getATSAutomaton(this));
			if (!intersection.isEmpty())
				result.add(type);
		}

		/*
		 * Also check PIs and comments ...
		 */
		if (!ancestorLanguageSuperlang.intersection(rootNodeTypeAutomaton)
				.isEmpty())
			result.add(RootNT.instance);

		if (!ancestorLanguageSuperlang.intersection(commentNodeTypeAutomaton)
				.isEmpty())
			result.add(CommentNT.instance);

		if (!ancestorLanguageSuperlang.intersection(PINodeTypeAutomaton)
				.isEmpty())
			result.add(PINT.chameleonInstance);

		/*
		 * ACHTUNG!!! This is generally not possible -- not all PCDATA are the
		 * same.
		 */
		if (!ancestorLanguageSuperlang.intersection(textNodeTypeAutomaton)
				.isEmpty()) {
			result.add(TextNT.chameleonInstance);
		}
		return result;
	}

	private Map<String, Set<DeclaredNodeType>> searchCache = new HashMap<String, Set<DeclaredNodeType>>();

	public void clearSearchCache() {
		searchCache.clear();
	}

	public Set<DeclaredNodeType> testDeclarationsFor(
			Automaton ancestorLanguageSuperlang,
			Set<? extends DeclaredNodeType> input, String key) {

		Set<DeclaredNodeType> result = null;

		if (key != null)
			result = searchCache.get(key);

		if (result != null) {
			return new HashSet<DeclaredNodeType>(result);
		}

		result = new HashSet<DeclaredNodeType>();

		for (Iterator<? extends DeclaredNodeType> nodes = input.iterator(); nodes
				.hasNext();) {
			DeclaredNodeType node = nodes.next();
			Automaton intersection = ancestorLanguageSuperlang
					.intersection(node.getATSAutomaton(this));
			if (!intersection.isEmpty())
				result.add(node);
		}

		if (key != null)
			searchCache.put(key, result);

		return result;
	}

	/**
	 * It is assumed here that the input is all elements ... anything that is
	 * not, goes out anyway.
	 */
	public Set<DeclaredNodeType> getElementDeclarationsFor(
			Automaton ancestorLanguageSuperlang,
			Set<? extends DeclaredNodeType> input) {
		Set<DeclaredNodeType> result = new HashSet<DeclaredNodeType>();
		for (DeclaredNodeType node : input) {
			if (node instanceof ElementUse) {
				Automaton intersection = ancestorLanguageSuperlang
						.intersection(node.getATSAutomaton(this));
				if (!intersection.isEmpty())
					result.add(node);
			}
		}
		return result;
	}

	/**
	 * It is assumed here that the input is all elements ... anything that is
	 * not, goes out anyway.
	 */
	public Set<Declaration> getElementDeclarationsFor(
			Automaton ancestorLanguageSuperlang) {
		Set<Declaration> result = new HashSet<Declaration>();
		for (ElementDecl type : allElementDecls) {
			Automaton intersection = ancestorLanguageSuperlang
					.intersection(type.getATSAutomaton(this));
			if (!intersection.isEmpty())
				result.add(type);
		}
		return result;
	}

	/**
	 * We have no hint on the name or type (element, attribute, other...), just
	 * search almost everything.
	 */
	/*
	 * public Set<BackendDecl> getDeclarationsFor( Automaton
	 * ancestorLanguageSuperlang, Set<UndeclaredNodeType> limit) {
	 * 
	 * Set<BackendDecl> result = new HashSet<BackendDecl>(); / * We have:
	 * Elements, attributes, root, comment, PI, text... / boolean estar =
	 * limit.contains(NodeType.ONE_ANY_NAME_ELEMENT_NT); boolean astar =
	 * limit.contains(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT);
	 * 
	 * for (BackendElementDecl type : allElementDecls) { if (estar ||
	 * limit.contains(new ElementNT(type))) { Automaton intersection =
	 * ancestorLanguageSuperlang.intersection(type .getATSAutomaton(this)); if
	 * (!intersection.isEmpty()) result.add(type); } }
	 * 
	 * for (BackendAttributeDecl type : allAttributeDecls) { if (astar ||
	 * limit.contains(new AttributeNT(type)) || limit.contains(new
	 * AttributeNT(type.getQName()))) { Automaton intersection =
	 * ancestorLanguageSuperlang.intersection(type .getATSAutomaton(this)); if
	 * (!intersection.isEmpty()) result.add(type); } } / * Also check PIs and
	 * comments ... / if (limit.contains(RootNT.instance)) if
	 * (!ancestorLanguageSuperlang.intersection(rootNodeTypeAutomaton)
	 * .isEmpty()) result.add(RootNT.instance);
	 * 
	 * if (limit.contains(CommentNT.instance)) if
	 * (!ancestorLanguageSuperlang.intersection(commentNodeTypeAutomaton)
	 * .isEmpty()) result.add(CommentNT.instance);
	 * 
	 * if (limit.contains(PINT.chameleonInstance)) if
	 * (!ancestorLanguageSuperlang.intersection(PINodeTypeAutomaton) .isEmpty()) {
	 * for (UndeclaredNodeType sniff : limit) { if (sniff instanceof PINT)
	 * result.add((PINT) sniff); } } / * ACHTUNG!!! This is generally not
	 * possible -- not all PCDATA are the same. / if
	 * (limit.contains(TextNT.chameleonInstance)) if
	 * (!ancestorLanguageSuperlang.intersection(textNodeTypeAutomaton)
	 * .isEmpty()) { for (UndeclaredNodeType sniff : limit) { if (sniff
	 * instanceof TextNT) result.add((TextNT) sniff); } } return result; }
	 */

	public Set<DeclaredNodeType> getUsesFor(
			Automaton ancestorLanguageSuperlang, Set<UndeclaredNodeType> limit) {

		Set<DeclaredNodeType> result = new HashSet<DeclaredNodeType>();

		/*
		 * We have: Elements, attributes, root, comment, PI, text...
		 */
		boolean estar = limit.contains(NodeType.ONE_ANY_NAME_ELEMENT_NT);
		boolean astar = limit.contains(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT);

		for (ElementDecl type : allElementDecls) {
			if (estar || limit.contains(new ElementNT(type))) {
				Automaton intersection = ancestorLanguageSuperlang
						.intersection(type.getATSAutomaton(this));
				if (!intersection.isEmpty())
					result.addAll(type.getAllUses());
			}
		}

		// todo: ommer!
		for (AttributeDecl type : allAttributeDecls) {
			if (astar || limit.contains(new AttributeNT(type))
					|| limit.contains(new AttributeNT(type.getQName()))) {
				Automaton intersection = ancestorLanguageSuperlang
						.intersection(type.getATSAutomaton(this));
				if (!intersection.isEmpty())
					// result.addAll(type.getAllUses());
					throw new RuntimeException(
							"If this legacy code is run, first fix it. Attribute decls no longer"
									+ "know their uses (and who wants to use automata anyway?).");
			}
		}

		/*
		 * Also check PIs and comments ...
		 */
		if (limit.contains(RootNT.instance))
			if (!ancestorLanguageSuperlang.intersection(rootNodeTypeAutomaton)
					.isEmpty())
				result.add(RootNT.instance);

		if (limit.contains(CommentNT.instance))
			if (!ancestorLanguageSuperlang.intersection(
					commentNodeTypeAutomaton).isEmpty())
				result.add(CommentNT.instance);

		if (limit.contains(PINT.chameleonInstance))
			if (!ancestorLanguageSuperlang.intersection(PINodeTypeAutomaton)
					.isEmpty()) {
				for (UndeclaredNodeType sniff : limit) {
					if (sniff instanceof PINT)
						result.add((PINT) sniff);
				}
			}
		/*
		 * ACHTUNG!!! This is generally not possible -- not all PCDATA are the
		 * same.
		 */
		if (limit.contains(TextNT.chameleonInstance))
			if (!ancestorLanguageSuperlang.intersection(textNodeTypeAutomaton)
					.isEmpty()) {
				for (UndeclaredNodeType sniff : limit) {
					if (sniff instanceof TextNT)
						result.add((TextNT) sniff);
				}
			}
		return result;
	}

	public Set<Declaration> getElementDeclarationsFor(
			Automaton ancestorLanguageSuperlang, QName hint) {
		Set<Declaration> result = new HashSet<Declaration>();

		Set<? extends Declaration> testers;

		if (hint != null)
			testers = allElementDeclsByName.get(hint);
		else
			testers = allElementDecls;

		if (testers != null)
			for (Declaration decl : testers) {
				Automaton intersection = ancestorLanguageSuperlang
						.intersection(decl.getATSAutomaton(this));
				if (!intersection.isEmpty())
					result.add(decl);
			}
		return result;
	}

	/*
	 * public Set<DeclaredNodeType> getAttributeDeclarationsFor( Automaton
	 * ancestorLanguageSuperlang, QName hint) { Set<DeclaredNodeType> result =
	 * new HashSet<DeclaredNodeType>();
	 * 
	 * Set<AttributeDecl> testers;
	 * 
	 * if (hint != null) testers = allAttributeDeclsByName.get(hint); else
	 * testers = allAttributeDecls;
	 * 
	 * if (testers != null) for (AttributeDecl decl : testers) { Automaton
	 * intersection = ancestorLanguageSuperlang.intersection(decl
	 * .getATSAutomaton(this)); if (!intersection.isEmpty()) result.add(decl); }
	 * return result; }
	 */
	/***************************************************************************
	 * * And now to the RegExp construction stuff, still working over names **
	 **************************************************************************/

	public Set<Declaration> approximateToDeclaredTypes(
			Set<? extends UndeclaredNodeType> original) {

		Set<Declaration> result = new HashSet<Declaration>();
		for (UndeclaredNodeType nt : original) {
			if (nt == RootNT.instance)
				result.add(RootNT.instance);

			else if (nt == CommentNT.instance)
				result.add(CommentNT.instance);

			else if (nt instanceof PINT)
				result.add(PINT.chameleonInstance);

			else if (nt instanceof TextNT)
				result.add((TextNT) nt);

			else if (nt instanceof ElementNT) {
				ElementNT ent = (ElementNT) nt;
				Set<ElementDecl> eds = allElementDeclsByName
						.get(ent.getQName());
				if (eds != null)
					for (ElementDecl decl : eds) {
						result.add(decl);
					}
			} else {
				AttributeNT ant = (AttributeNT) nt;
				Set<AttributeDecl> ads = allAttributeDeclsByName.get(ant
						.getQName());
				if (ads != null)
					for (AttributeDecl decl : ads) {
						result.add(decl);
					}
				// throw new AssertionError("Unhandled type: attributeNT");
			}
		}
		return result;
	}

	public Set<? extends UndeclaredNodeType> bleachUndeclaredSet(
			Set<? extends Declaration> original) {

		Set<UndeclaredNodeType> result = new HashSet<UndeclaredNodeType>();
		for (Declaration nt : original) {
			if (nt == RootNT.instance)
				result.add(RootNT.instance);

			else if (nt == CommentNT.instance)
				result.add(CommentNT.instance);

			else if (nt instanceof PINT)
				result.add(PINT.chameleonInstance);

			else if (nt instanceof TextNT)
				result.add((TextNT) nt);

			else if (nt instanceof ElementDecl) {
				ElementDecl ent = (ElementDecl) nt;
				result.add(new ElementNT(ent));
			} else
				throw new AssertionError("Unhandled type: attributeNT");
		}
		return result;
	}

	private void runAxis(Set<? extends DeclaredNodeType> nodeSet,
			Set<? super DeclaredNodeType> result, short axis)
			throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException {

		// boolean includeDerivedTypeContents = false;

		switch (axis) {
		case XPathAxisStep.CHILD:
			for (Iterator<? extends DeclaredNodeType> iter = nodeSet.iterator(); iter
					.hasNext();) {
				DeclaredNodeType nodeType = iter.next();
				nodeType.runChildAxis(this, result);
			}
			break;

		case XPathAxisStep.PARENT:
			for (Iterator<? extends DeclaredNodeType> iter = nodeSet.iterator(); iter
					.hasNext();) {
				DeclaredNodeType nodeType = iter.next();
				nodeType.runParentAxis(this, result);
			}
			break;

		case XPathAxisStep.ATTRIBUTE:
			for (Iterator<? extends DeclaredNodeType> iter = nodeSet.iterator(); iter
					.hasNext();) {
				DeclaredNodeType nodeType = iter.next();
				nodeType.runAttributeAxis(this, result);
			}
			break;

		case XPathAxisStep.SELF: {
			// Simply add all nodes in the node set.
			result.addAll(nodeSet);
		}
			break;

		case XPathAxisStep.DESCENDANT: {
			// Start with all possible children of each node. Repeatedly add all
			// possible children until set does not change.
			Set<DeclaredNodeType> inter = new HashSet<DeclaredNodeType>();
			runAxis(nodeSet, inter, XPathAxisStep.CHILD);
			result.addAll(inter);

			Set<DeclaredNodeType> inter2 = new HashSet<DeclaredNodeType>();

			int lastNodeCount;
			do {
				lastNodeCount = result.size();
				runAxis(inter, inter2, XPathAxisStep.CHILD);
				result.addAll(inter2);
				Set<DeclaredNodeType> temp = inter;
				inter = inter2;
				inter2 = temp;
				inter2.clear();
			} while (result.size() > lastNodeCount);
		}
			break;
		case XPathAxisStep.DESCENDANT_OR_SELF: {
			// Combine self and descendant axes.
			runAxis(nodeSet, result, XPathAxisStep.SELF);
			// Set<DeclaredNodeType> newResult = new
			// HashSet<DeclaredNodeType>();
			// runAxis(result, newResult, new XPathAxis(XPathAxis.DESCENDANT));
			// result.addAll(newResult);
			runAxis(nodeSet, result, XPathAxisStep.DESCENDANT);
		}
			break;
		case XPathAxisStep.ANCESTOR: {
			// Start with all possible parents of each node. Repeatedly add all
			// possible parents until set does not change.
			Set<DeclaredNodeType> inter = new HashSet<DeclaredNodeType>();
			runAxis(nodeSet, inter, XPathAxisStep.PARENT);
			result.addAll(inter);

			Set<DeclaredNodeType> inter2 = new HashSet<DeclaredNodeType>();

			int lastNodeCount;
			do {
				lastNodeCount = result.size();
				runAxis(inter, inter2, XPathAxisStep.PARENT);
				result.addAll(inter2);
				Set<DeclaredNodeType> temp = inter;
				inter = inter2;
				inter2 = temp;
				inter2.clear();
			} while (result.size() > lastNodeCount);
		}
			break;
		case XPathAxisStep.ANCESTOR_OR_SELF: {
			// Combine self and descendant axes.
			runAxis(nodeSet, result, XPathAxisStep.SELF);
			// Set<DeclaredNodeType> newResult = new
			// HashSet<DeclaredNodeType>();
			// runAxis(newResult, newResult, new
			// XPathAxis(XPathAxis.DESCENDANT));
			runAxis(nodeSet, result, XPathAxisStep.DESCENDANT);
			// result.addAll(newResult);
		}
			break;
		case XPathAxisStep.FOLLOWING_SIBLING:
		case XPathAxisStep.PRECEDING_SIBLING: {
			// Compose parent and child axes.
			Set<DeclaredNodeType> newResult = new HashSet<DeclaredNodeType>();
			runAxis(nodeSet, newResult, XPathAxisStep.PARENT);
			runAxis(newResult, result, XPathAxisStep.CHILD);
		}
			break;
		case XPathAxisStep.PRECEDING:
			// Insert root:
			result.add(RootNT.instance);
		case XPathAxisStep.FOLLOWING: {
			// Approximate with anything but attribues:
			// newNodeSet = new HashSet<NodeType>();
			// Insert PCDATA, comment and PI:
			result.add(TextNT.chameleonInstance);
			result.add(CommentNT.instance);
			result.add(PINT.chameleonInstance);

			// Insert Elements and their gedefimz
			// for (BackendElementDecl decl : allElementDecls)
			// Insert element:
			result.addAll(allElementUses());
			break;
		}
		default: {
			throw new XSLToolsUnhandledNodeTestException("Unhandled axis "
					+ XPathAxisStep.axisToString(axis));
		}
		}
	}

	/***************************************************************************
	 * * And now to the abstract XPath evaluation stuff; modified to work over
	 * types**
	 **************************************************************************/
	/**
	 * Simulates then XPath expression on the DTD and returns the list of nodes
	 * that can potentially be selected by the XPath expression. Essentially
	 * repeats the operation nodetest(axis(nodeSet)) for each location step.
	 */
	/*
	 * public Set<? extends DeclaredNodeType>
	 * possibleTargetNodesFromAnyElement( NodeSetLocationPath path) throws
	 * XSLToolsUnhandledNodeTestException { // Construct initial node set: Set<DeclaredNodeType>
	 * nodeSet = new HashSet<DeclaredNodeType>();
	 * 
	 * if (path instanceof NodeSetAbsLocationPath) { // Abolute path. Insert
	 * only the root node: nodeSet.add(RootNT.getInstance()); } else {
	 * nodeSet.addAll(allElementDeclarations); } return
	 * possibleTargetNodes(path, nodeSet); }
	 */

	private Set<ElementUse> refAllElementUses;

	private Set<? extends ElementUse> allElementUses() {
		if (refAllElementUses == null) {
			refAllElementUses = new HashSet<ElementUse>();
			for (ElementDecl decl : allElementDecls) {
				refAllElementUses.addAll(decl.getAllUses());
			}
		}
		return refAllElementUses;
	}

	private Set<AttributeUse> refAllAttributeUses;

	private Set<AttributeUse> allAttributeUses() {
		if (refAllAttributeUses == null) {
			refAllAttributeUses = new HashSet<AttributeUse>();
			// for (AttributeDecl decl : allAttributeDecls) {
			// refAllAttributeUses.addAll(decl.getAllUses());
			// }

			Map<QName, AttributeUse> slam = new HashMap<QName, AttributeUse>();

			for (ElementDecl decl : allElementDecls) {
				for (ElementUse euse : decl.getAllUses()) {
					euse.attributeUses(slam);
					refAllAttributeUses.addAll(slam.values());
					slam.clear();
				}
			}
		}
		return refAllAttributeUses;
	}

	private Set<DeclaredNodeType> fullDeclaredSeedSet() {
		Set<DeclaredNodeType> allTypes = new HashSet<DeclaredNodeType>();
		allTypes.addAll(allElementUses());
		Set<AttributeUse> fis = allAttributeUses();
		allTypes.addAll(fis);
		return allTypes;
	}

	private Set<DeclaredNodeType> fullSeedSet() {
		Set<DeclaredNodeType> allTypes = fullDeclaredSeedSet();
		allTypes.add(CommentNT.instance);
		allTypes.add(PINT.chameleonInstance);
		allTypes.add(RootNT.instance);
		allTypes.add(TextNT.chameleonInstance);
		return allTypes;
	}

	private void runTest(Set<DeclaredNodeType> newNodeSet, XPathAxisStep step)
			throws XSLToolsSchemaException {
		Iterator<DeclaredNodeType> nodeIter = newNodeSet.iterator();

		while (nodeIter.hasNext()) {
			DeclaredNodeType node = nodeIter.next();
			if (!node.matches(step, this))
				nodeIter.remove();
		}
	}

	private void runPredicateTest(Set<DeclaredNodeType> newNodeSet,
			XPathAxisStep step) throws XSLToolsSchemaException {
		if (!step.hasPredicates())
			return;

		Set<DeclaredNodeType> result = new HashSet<DeclaredNodeType>();
		Iterator<DeclaredNodeType> nodeIter = newNodeSet.iterator();

		while (nodeIter.hasNext()) {
			DeclaredNodeType node = nodeIter.next();
			Iterator<DynamicRedeclaration> it = DynamicRedeclaration.prototypes
					.iterator();
			CardinalMatch match;
			if (it.hasNext()) {
				do {
					DynamicRedeclaration deco = it.next();
					match = deco.transform(this, node, step.getPredicate(0));
				} while (it.hasNext() && match.cannotDetermine());
				if (match.isSplit()) {
					result.add(match.getPassAlternative());
				} else if (!match.argTypeNeverPasses()) {
					result.add(node);
				}
			} else
				result.add(node);
		}
		newNodeSet.clear();
		newNodeSet.addAll(result);
	}

	/**
	 * Simulates then XPath expression on the DTD and returns the list of nodes
	 * that can potentially be selected by the XPath expression. Essentially
	 * repeats the operation nodetest(axis(nodeSet)) for each location step.
	 */
	/*
	 * public Set<DeclaredNodeType> possibleTargetNodes(NodeSetLocationPath
	 * path) throws XSLToolsUnhandledNodeTestException { // Construct initial
	 * node set: return possibleTargetNodes(path, fullSeedSet()); }
	 */
	/**
	 * The delta function
	 * 
	 * @param path
	 * @param sigma
	 * @return
	 * @throws XSLToolsXPathException
	 */
	/*
	 * public Set<? extends DeclaredNodeType> possibleTargetNodes(
	 * NodeSetLocationPath path, DeclaredNodeType sigma) throws
	 * XSLToolsUnhandledNodeTestException { return possibleTargetNodes(path,
	 * Collections.singleton(sigma)); }
	 */
	/**
	 * Simulates then XPath expression on the DTD and returns the list of nodes
	 * that can potentially be selected by the XPath expression. Essentially
	 * repeats the operation nodetest(axis(nodeSet)) for each location step.
	 */
	public Set<DeclaredNodeType> possibleTargetNodes(XPathPathExpr path)
			throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException {
		// Construct initial node set:
		Set<DeclaredNodeType> seedy = fullSeedSet();
		Set<DeclaredNodeType> targets = possibleTargetNodes(path, seedy);
		return targets;
	}

	/**
	 * The delta function
	 * 
	 * @param path
	 * @param sigma
	 * @return
	 * @throws XSLToolsXPathException
	 */
	public Set<DeclaredNodeType> possibleTargetNodes(XPathPathExpr path,
			DeclaredNodeType sigma) throws XSLToolsUnhandledNodeTestException,
			XSLToolsSchemaException {
		return possibleTargetNodes(path, Collections.singleton(sigma));
	}

	public Set<DeclaredNodeType> possibleTargetNodes(XPathExpr expr)
			throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException {
		if (expr instanceof XPathPathExpr)
			return possibleTargetNodes((XPathPathExpr) expr);

		if (expr instanceof XPathUnionExpr) {
			Set<DeclaredNodeType> result = new HashSet<DeclaredNodeType>();
			XPathUnionExpr union = (XPathUnionExpr) expr;
			for (XPathExpr subexp : union) {
				result.addAll(possibleTargetNodes(subexp));
			}
			return result;
		}
		throw new AssertionError("Not a location path and not a union");
	}

	public Set<DeclaredNodeType> possibleTargetNodes(XPathPathExpr path,
			Set<DeclaredNodeType> nodeSet)
			throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException {

		if (path instanceof XPathAbsolutePathExpr) {
			// Abolute path. Override whatever was given as arg.
			nodeSet = new HashSet<DeclaredNodeType>();
			nodeSet.add(RootNT.instance);
		} else {
			// don't mess w caller's set
			nodeSet = new HashSet<DeclaredNodeType>(nodeSet);
		}

		// Iterate steps until final node set or empty is found:
		Iterator stepIter = path.steps();

		Set<DeclaredNodeType> newNodeSet = new HashSet<DeclaredNodeType>();

		while (stepIter.hasNext() && !nodeSet.isEmpty()) {

			Object otep = stepIter.next();

			if (!(otep instanceof XPathAxisStep)) {
				System.err.println("Non axis step -- what to do?");
			}

			XPathAxisStep step = (XPathAxisStep) otep;

			// Run axis to create initial new node list:
			// this will go out and be replaced by runAxis2..

			runAxis(nodeSet, newNodeSet, step.getAxis());

			// Filter new node set by node test:
			runTest(newNodeSet, step);

			runPredicateTest(newNodeSet, step);

			// Replace old node list with new:
			Set<DeclaredNodeType> temp = nodeSet;
			nodeSet = newNodeSet;
			newNodeSet = temp;
			newNodeSet.clear();
		}
		return nodeSet;
	}

	/***************************************************************************
	 * * And now to the overly strict reverse mill test, with GedeFims
	 * technology **
	 **************************************************************************/
	private Set<DeclaredNodeType> runReverseAxis(Set<DeclaredNodeType> types,
			short axis) {
		Set<DeclaredNodeType> result = new HashSet<DeclaredNodeType>();
		switch (axis) {
		case XPathAxisStep.CHILD:
			for (DeclaredNodeType type : types) {
				type.runReverseChildAxis(this, result);
				/*
				 * if (type instanceof ElementDecl) {
				 * result.addAll(((ElementDecl) type).parentDecls); } else if
				 * (type instanceof AttributeDecl) { // nothing can be the
				 * parent of an attribute on the child axis, so // nothing is
				 * the answer. } else if (type == CommentNT.instance || type ==
				 * PINT.instance) { result.add(RootNT.instance);
				 * result.addAll(commentPIParents); } else if (type instanceof
				 * TextNT) { result.addAll(pcDataParents); } else if (type ==
				 * RootNT.instance) { // nothing can be the parent of root, so
				 * nothing is the answer. } else throw new
				 * AssertionError("Unhandled node type: " + type);
				 */
			}
			break;
		case XPathAxisStep.DESCENDANT:
			// apply CHILD recursively until N C
			int saveSize;
			Set<DeclaredNodeType> grynt = types;
			do {
				saveSize = result.size();
				grynt = runReverseAxis(grynt, XPathAxisStep.CHILD);
				result.addAll(grynt);
			} while (result.size() > saveSize);

		case XPathAxisStep.ATTRIBUTE:
			for (DeclaredNodeType type : types) {
				type.runReverseAttributeAxis(this, result);
				/*
				 * if (type instanceof ElementDecl) { // nothing can be the
				 * parent of elements on the attribute axis, so // nothing is
				 * the answer. } else if (type instanceof AttributeUse) {
				 * result.add(((AttributeUse) type).getOwnerElementDecl()); }
				 * else if (type instanceof AttributeDecl) {
				 * result.addAll(((AttributeDecl) type).getOwnerElementDecls()); }
				 * else if (type == CommentNT.instance || type == PINT.instance) { }
				 * else if (type instanceof TextNT) { } else if (type ==
				 * RootNT.instance) { } else throw new AssertionError("Unhandled
				 * node type: " + type);
				 */
			}

			break;
		case XPathAxisStep.SELF:
			// identity xform
			result.addAll(types);
			break;
		case XPathAxisStep.DESCENDANT_OR_SELF:
			result.addAll(runReverseAxis(types, XPathAxisStep.DESCENDANT));
			result.addAll(types);
			break;

		case XPathAxisStep.ANCESTOR:
		case XPathAxisStep.ANCESTOR_OR_SELF:
		case XPathAxisStep.FOLLOWING_SIBLING:
		case XPathAxisStep.PRECEDING_SIBLING:
		case XPathAxisStep.FOLLOWING:
		case XPathAxisStep.PRECEDING:
		case XPathAxisStep.PARENT:
		case XPathAxisStep.NAMESPACE:
			throw new AssertionError(
					"This axis should have been filtered out: "
							+ XPathAxisStep.axisToString(axis));
		}
		return result;
	}

	public boolean allPossibleFlowsCovered(XPathPathExpr MCS,
			XPathPathExpr match, Set<? extends DeclaredNodeType> boot,
			boolean asymmetricMode, DeclaredNodeType contextType) throws XSLToolsSchemaException {

		if (match instanceof XPathAbsolutePathExpr
				&& !(MCS instanceof XPathAbsolutePathExpr))
			return false;

		if (match.hasPredicates()
				&& !DynamicRedeclaration.alwaysPassesPredicate(contextType, match, this))
			return false;

		Iterator<XPathStepExpr> MCSStepIter = MCS.reverseSteps();
		Iterator<XPathStepExpr> matchStepIter = match.reverseSteps();

		Set<DeclaredNodeType> MCSNodeSet = new HashSet<DeclaredNodeType>(boot);
		Set<DeclaredNodeType> matchNodeSet = new HashSet<DeclaredNodeType>(boot);

		while (MCSStepIter.hasNext() && matchStepIter.hasNext()) {

			XPathStepExpr sMCSStep = MCSStepIter.next();
			XPathStepExpr smatchStep = matchStepIter.next();

			if (!(sMCSStep instanceof XPathAxisStep))
				throw new AssertionError("TODO! Fix this. "
						+ sMCSStep.getClass());

			if (!(smatchStep instanceof XPathAxisStep))
				throw new AssertionError("TODO! Fix this. "
						+ smatchStep.getClass());

			XPathAxisStep MCSStep = (XPathAxisStep) sMCSStep;
			XPathAxisStep matchStep = (XPathAxisStep) smatchStep;

			// We're in reverse: Run node tests before the axis mappings!
			for (Iterator<DeclaredNodeType> MCSNodeIter = MCSNodeSet.iterator(); MCSNodeIter
					.hasNext();) {
				DeclaredNodeType node = MCSNodeIter.next();
				// Apply node test:
				if (!node.matches(MCSStep, this))
					MCSNodeIter.remove();
			}

			for (Iterator<DeclaredNodeType> matchNodeIter = matchNodeSet
					.iterator(); matchNodeIter.hasNext();) {
				DeclaredNodeType node = matchNodeIter.next();
				// Apply node test:
				if (!node.matches(matchStep, this))
					matchNodeIter.remove();
			}

			/*
			 * System.out.println("MCS after test:
			 * ("+MCSStep.toString().substring(MCSStep.getAxis().toString().length()+2)+")
			 * "+MCSNodeSet); System.out.println("Match after test:
			 * ("+matchStep.toString().substring(matchStep.getAxis().toString().length()+2)+")
			 * "+matchNodeSet);
			 */

			Set<DeclaredNodeType> sniffer = new HashSet<DeclaredNodeType>(
					MCSNodeSet);
			sniffer.removeAll(matchNodeSet);
			if (!sniffer.isEmpty())
				return false;
			/*
			 * System.out.println("Both after communism: " + matchNodeSet);
			 * System.out.println();
			 */
			short MCSAxis = MCSStep.getAxis();
			short matchAxis = matchStep.getAxis();

			// The test can't handle different axes.
			// However, in the asymmetric mode, it can handle that the select
			// expr
			// begins with //,
			// while the axis begins with child::... and is not absolute. The
			// match
			// pattern has a
			// sort of implicit // at the beginning when not absolute...
			if (asymmetricMode && !matchStepIter.hasNext()
					&& matchAxis == XPathAxisStep.CHILD
					&& MCSAxis == XPathAxisStep.DESCENDANT_OR_SELF
					&& !(match instanceof XPathAbsolutePathExpr))
				return true;

			if (MCSAxis != matchAxis)
				return false;

			// Run axis to create initial new node list:
			Set<DeclaredNodeType> MCSNodeSetAA = runReverseAxis(MCSNodeSet,
					MCSAxis);
			Set<DeclaredNodeType> matchNodeSetAA = runReverseAxis(matchNodeSet,
					matchAxis);
			/*
			 * System.out.println("MCS after axis: ("+MCSAxis+")
			 * "+MCSNodeSetAA); System.out.println("Match after axis:
			 * ("+matchAxis+") "+matchNodeSetAA);
			 */
			sniffer = new HashSet<DeclaredNodeType>(MCSNodeSetAA);
			sniffer.removeAll(matchNodeSetAA);
			if (!sniffer.isEmpty())
				return false;

			MCSNodeSet = MCSNodeSetAA;
			matchNodeSet = matchNodeSetAA;

			/*
			 * System.out.println("Both after communism: " + matchNodeSet);
			 * System.out.println();
			 */
		}
		return (!matchStepIter.hasNext());
	}

	public short incompatiblePaths(XPathPathExpr MCS, XPathPathExpr match,
			Set<? extends DeclaredNodeType> boot)
			throws XSLToolsSchemaException {

		// This looks dubious: A descendant-or-self match will be killed off this way.
		// Maybe a solution is to just remove descendant-or-self prefixes on match,
		// as they are good for nothing anyway.
		// What is the purpose of this test????
		// An absolute match expression can easily be compatible with some non absolute
		// selection??!!
		// Let's get rid of it.
		/*
		if (match instanceof XPathAbsolutePathExpr
				&& !(MCS instanceof XPathAbsolutePathExpr))
			return ABSOLUTE_DEATH_CAUSE;
		*/

		Iterator<XPathStepExpr> MCSStepIter = MCS.reverseSteps();
		Iterator<XPathStepExpr> matchStepIter = match.reverseSteps();

		Set<DeclaredNodeType> MCSNodeSet = new HashSet<DeclaredNodeType>(boot);
		Set<DeclaredNodeType> matchNodeSet = new HashSet<DeclaredNodeType>(boot);

		while (MCSStepIter.hasNext() && matchStepIter.hasNext()) {

			XPathStepExpr sMCSStep = MCSStepIter.next();
			XPathStepExpr smatchStep = matchStepIter.next();

			if (!(sMCSStep instanceof XPathAxisStep))
				throw new AssertionError("TODO! Fix this. "
						+ sMCSStep.getClass());

			if (!(smatchStep instanceof XPathAxisStep))
				throw new AssertionError("TODO! Fix this. "
						+ smatchStep.getClass());

			XPathAxisStep MCSStep = (XPathAxisStep) sMCSStep;
			XPathAxisStep matchStep = (XPathAxisStep) smatchStep;

			short MCSAxis = MCSStep.getAxis();
			short matchAxis = matchStep.getAxis();

			if (MCSAxis != XPathAxisStep.CHILD
					&& MCSAxis != XPathAxisStep.ATTRIBUTE)
				return MCSAXIS_SURVIVAL_CAUSE;

			if (matchAxis != XPathAxisStep.CHILD
					&& matchAxis != XPathAxisStep.ATTRIBUTE)
				return MATCHAXIS_SURVIVAL_CAUSE;

			if (MCSAxis != matchAxis)
				return AXIS_DEATH_CAUSE;

			// We're in reverse: Run node tests before the axis mappings!
			for (Iterator<DeclaredNodeType> MCSNodeIter = MCSNodeSet.iterator(); MCSNodeIter
					.hasNext();) {
				DeclaredNodeType node = MCSNodeIter.next();
				// Apply node test:
				if (!node.matches(MCSStep, this))
					MCSNodeIter.remove();
			}

			for (Iterator<DeclaredNodeType> matchNodeIter = matchNodeSet
					.iterator(); matchNodeIter.hasNext();) {
				DeclaredNodeType node = matchNodeIter.next();
				// Apply node test:
				if (!node.matches(matchStep, this))
					matchNodeIter.remove();
			}

			Set<DeclaredNodeType> sniffer = new HashSet<DeclaredNodeType>(
					MCSNodeSet);
			sniffer.retainAll(matchNodeSet);
			if (sniffer.isEmpty())
				return TESTSET_DEATH_CAUSE;

			// Run axis to create initial new node list:
			Set<DeclaredNodeType> MCSNodeSetAA = runReverseAxis(MCSNodeSet,
					MCSAxis);
			Set<DeclaredNodeType> matchNodeSetAA = runReverseAxis(matchNodeSet,
					matchAxis);

			sniffer = new HashSet<DeclaredNodeType>(MCSNodeSetAA);
			sniffer.retainAll(matchNodeSetAA);

			if (sniffer.isEmpty())
				return PARENTSET_DEATH_CAUSE;

			MCSNodeSet = MCSNodeSetAA;
			matchNodeSet = matchNodeSetAA;
		}
		return EXHAUSTED_SURVIVAL_CAUSE;
	}

	public static String deathCause(short deathCause) {
		switch (deathCause) {
		case ABSOLUTE_DEATH_CAUSE:
			return "ABSOLUTE_DEATH_CAUSE";
		case AXIS_DEATH_CAUSE:
			return "AXIS_DEATH_CAUSE";
		case TESTSET_DEATH_CAUSE:
			return "TESTSET_DEATH_CAUSE";
		case PARENTSET_DEATH_CAUSE:
			return "PARENTSET_DEATH_CAUSE";
		case MCSAXIS_SURVIVAL_CAUSE:
			return "MCSAXIS_SURVIVAL_CAUSE";
		case MATCHAXIS_SURVIVAL_CAUSE:
			return "MATCHAXIS_SURVIVAL_CAUSE";
		case EXHAUSTED_SURVIVAL_CAUSE:
			return "EXHAUSTED_SURVIVAL_CAUSE";
		default:
			return "unknown";
		}
	}

	public short incompatiblePaths(XPathPathExpr MCS, XPathPathExpr match,
			DeclaredNodeType boot) throws XSLToolsSchemaException {
		return incompatiblePaths(MCS, match, Collections.singleton(boot));
	}

	/***************************************************************************
	 * * And now to the RegExp construction stuff, still working over names **
	 **************************************************************************/

	public String getElementRegExp() throws XSLToolsSchemaException {
		if (elementRegExp.length() > 2)
			return elementRegExp;
		throw new XSLToolsSchemaException("No Elements");
	}

	public String getAttributeRegExp() throws XSLToolsSchemaException {
		if (attributeRegExp.length() > 2)
			return attributeRegExp;
		throw new XSLToolsSchemaException("No attributes");
	}

	protected String testRegExpNewStyle(XPathNodeTest test, short axis)
			throws XSLToolsSchemaException, XSLToolsUnhandledNodeTestException {
		if (axis == XPathAxisStep.ATTRIBUTE)
			return test.testAttributeAxisRegExp(this);
		if (axis == XPathAxisStep.SELF
				|| axis == XPathAxisStep.DESCENDANT_OR_SELF)
			return test.testSDOSAxisRegExp(this);
		if (axis == XPathAxisStep.CHILD || axis == XPathAxisStep.DESCENDANT)
			return test.testCDAxisRegExp(this);
		throw new XSLToolsUnhandledNodeTestException("Unhandled axis: "
				+ XPathAxisStep.axisToString(axis) + "::" + test + "("
				+ test.getClass().getSimpleName() + ")");
	}

	protected String testRegExpSurGammelGed(XPathNodeTest test, short axis)
			throws XSLToolsSchemaException, XSLToolsUnhandledNodeTestException {
		if (axis == XPathAxisStep.ATTRIBUTE) {
			if (test instanceof XPathNameTest) {
				// Any attribute with this name.

				// must handle wildcards gracefully, too....
				// probably involves making REs over pairs of element /
				// attribute
				// names, not just singletons (uh well, we only concern
				// ourselves
				// with what's schema-defined?).

				XPathNameTest nameTest = (XPathNameTest) test;
				String regexp = "";
				QName name = nameTest.getQName();

				if (name.getName().equals("*")) {
					regexp += getAttributeRegExp();
				} else {
					Character C = attributeQNameToCharMap.get(name);

					if (C == null)
						regexp += CharGenerator.getAbsurdChar();
					else
						regexp += C.charValue();
				}
				return "(" + regexp + ")";// cookd
			} else if (test instanceof XPathAnyNodeTest) {
				// Any attribute.
				return getAttributeRegExp(); // cookd
			} else {
				throw new XSLToolsUnhandledNodeTestException(
						"Unhandled node test");
			}
		} else if (axis == XPathAxisStep.SELF
				|| axis == XPathAxisStep.DESCENDANT_OR_SELF) {
			if (test instanceof XPathNameTest) {
				// Only this specific element. (A name step can only apply to
				// nodes of
				// the principal node type: elements in this case)
				XPathNameTest nameTest = (XPathNameTest) test;
				// Look up element:
				QName name = nameTest.getQName();
				if (name.getName().equals("*"))
					return getElementRegExp();
				return "" + getCharForElementName(name);
			} else if (test instanceof XPathAnyNodeTest) {
				// Any node.
				return ".";
			} else if (test instanceof XPathTextTest) {
				// Only a text node.
				return ""
						+ TextNT.chameleonInstance.getCharRepresentation(this);
			} else if (test instanceof XPathCommentTest) {
				// Only a comment node.
				return "" + CommentNT.instance.getCharRepresentation(this);
			} else if (test instanceof XPathPITest) {
				// Only a PI node.
				return "" + PINT.chameleonInstance.getCharRepresentation(this);
			} else {
				throw new XSLToolsUnhandledNodeTestException(
						"Unhandled node test: " + test);
			}
		} else if (axis == XPathAxisStep.CHILD
				|| axis == XPathAxisStep.DESCENDANT) {
			if (test instanceof XPathNameTest) {
				// Only this specific element.
				XPathNameTest nameTest = (XPathNameTest) test;
				// Look up element:
				QName name = nameTest.getQName();
				if (name.getName().equals("*"))
					return getElementRegExp();
				return "" + getCharForElementName(name);
			} else if (test instanceof XPathAnyNodeTest) {
				// Any node that can be a child.
				return "(" + getElementRegExp() + "|"
						+ TextNT.chameleonInstance.getCharRepresentation(this)
						+ "|" + CommentNT.instance.getCharRepresentation(this)
						+ "|"
						+ PINT.chameleonInstance.getCharRepresentation(this)
						+ ")";
			} else if (test instanceof XPathTextTest) {
				// Only a text node.
				return ""
						+ TextNT.chameleonInstance.getCharRepresentation(this);
			} else if (test instanceof XPathCommentTest) {
				// Only a comment node.
				return "" + CommentNT.instance.getCharRepresentation(this);
			} else if (test instanceof XPathPITest) {
				// Only a PI node.
				return "" + PINT.chameleonInstance.getCharRepresentation(this);
			} else {
				throw new XSLToolsUnhandledNodeTestException(
						"Unhandled Node Test: " + test);
			}
		}
		throw new XSLToolsUnhandledNodeTestException("Unhandled axis: "
				+ XPathAxisStep.axisToString(axis) + "::" + test + "("
				+ test.getClass().getSimpleName() + ")");
	}

	protected String testRegExp(XPathNodeTest test, short axis)
			throws XSLToolsSchemaException, XSLToolsUnhandledNodeTestException {

		String s1 = testRegExpNewStyle(test, axis);
		/*
		 * String s2;
		 * 
		 * try { s2 = testRegExpGammelGed(test, axis); } catch (Exception ex) {
		 * s2 = "nixxe"; }
		 * 
		 * if (!s1.equals(s2)) { System.err.println( "GEDEFIMS !!!: " +
		 * XPathAxisStep.axisToString(axis) + "::" + test + "(" +
		 * test.getClass().getSimpleName() + ")"); System.err.println("Ny: " +
		 * s1); System.err.println("Gammel: " + s2); }
		 */
		return s1;
	}

	protected String axisRegExp(short axis) throws Exception {
		if (axis == XPathAxisStep.DESCENDANT
				|| axis == XPathAxisStep.DESCENDANT_OR_SELF)
			return getElementRegExp() + "*";
		return "()";
	}

	protected String applyStepToRegExp(String P, XPathAxisStep step,
			short originalAxis, short axisOverride)
			throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException {
		// Resolve axis:
		short axis;

		if (axisOverride == 0)
			axis = originalAxis;
		else
			axis = axisOverride;

		// Switch on axis:
		switch (axis) {
		case XPathAxisStep.CHILD:
		case XPathAxisStep.ATTRIBUTE:
		case XPathAxisStep.DESCENDANT: {
			// Simply concat P with the axis and node test regexps.
			try {
				String axisR = axisRegExp(axis);
				if (!axisR.equals("()"))
					P += axisR;
				P += testRegExp(step.getNodeTest(), axis);
			} catch (Exception e) {
				e.printStackTrace();
				if (e instanceof XSLToolsSchemaException
						|| e instanceof XSLToolsUnhandledNodeTestException) {
					// NoAttributesException: Attribute axis was used, but there
					// is no
					// attributes! Return EMPTY LANGUAGE regex.
					// UnhandledNodeTestException: namespace, comment of
					// processing-instruction axis was used. Return EMPTY
					// LANGUAGE regex.
					return "#";
				}
				throw new XSLToolsUnhandledNodeTestException("", e);
			}
		}
			break;
		case XPathAxisStep.SELF: {
			// Intersect P with ".*Rtest". Last node in P must match the node
			// test
			// regexp.
			P = "((" + P + ")&(.*" + testRegExp(step.getNodeTest(), axis)
					+ "))";
		}
			break;
		case XPathAxisStep.DESCENDANT_OR_SELF: {
			// Combine descendant and self axis regexps.
			String descendantP = applyStepToRegExp(P, step,
					XPathAxisStep.DESCENDANT_OR_SELF, XPathAxisStep.DESCENDANT);
			String selfP = applyStepToRegExp(P, step,
					XPathAxisStep.DESCENDANT_OR_SELF, XPathAxisStep.DESCENDANT);
			P = "(" + descendantP + ")|(" + selfP + ")";
		}
			break;
		default: {
			throw new XSLToolsUnhandledNodeTestException(
					"Unsupported XPath axis: " + axis);
		}
		}
		return P;
	}

	private String constructXPath2PathRegExp(XPathPathExpr path)
			throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException {
		// Create initial regexp:

		String P;

		if (path instanceof XPathAbsolutePathExpr) {
			// Simply start with root node:
			P = "" + RootNT.instance.getCharRepresentation(this);
		} else {
			// Start with root and any following content (sigma star):
			P = RootNT.instance.getCharRepresentation(this) + ".*";
		}

		// Iterate location steps:
		// TODO: Generalize to non axis steps.
		for (Object otep : path) {
			XPathAxisStep step = (XPathAxisStep) otep;
			P = applyStepToRegExp(P, step, step.getAxis(), (short) 0);
			// Return immediately if has become the empty language:
			if (P.equals("#"))
				return P;
		}
		return P;
	}

	public String constructXPathRegExp(XPathExpr expr)
			throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException {
		if (expr == null)
			return "#";

		// Create initial regexp:

		if (expr instanceof XPathPathExpr)
			return constructXPath2PathRegExp((XPathPathExpr) expr);

		assert (expr instanceof XPathUnionExpr) : "Path was a "
				+ (expr == null ? "nullie" : expr.getClass()) + ", " + expr
				+ " exprected a path expr or a union expr";

		// TODO: Should work with longer subexps to .. if
		// unions de-binarized...
		// assert (expr.getSubExpressions().size() < 3);

		StringBuilder result = new StringBuilder();

		for (Iterator<XPathExpr> subs = ((XPathUnionExpr) expr).iterator(); subs
				.hasNext();) {
			result.append('(');
			result.append(constructXPathRegExp(subs.next()));
			result.append(')');
			if (subs.hasNext())
				result.append('|');
		}
		return result.toString();
	}

	public Automaton constructPathAutomaton(XPathExpr expr)
			throws XSLToolsUnhandledNodeTestException, XSLToolsSchemaException {
		String regexp = constructXPathRegExp(expr);
		return new RegExp(regexp).toAutomaton();
	}

	public Automaton getRootNodeTypeAutomaton() {
		return rootNodeTypeAutomaton;
	}

	/*
	 * ACHTUNG!!! Apart from these, RootNT is implicitly a comment / PI parent.
	 */
	public Set<? extends ElementUse> getCommentPIParentElements() {
		return commentPIParents;
	}

	public Set<? extends ElementUse> getPCDataParents() {
		return pcDataParents;
	}

	public Automaton getCommentNodeTypeAutomaton() {
		return commentNodeTypeAutomaton;
	}

	public Automaton getPINodeTypeAutomaton() {
		return PINodeTypeAutomaton;
	}

	public Automaton getTextNodeTypeAutomaton() {
		return textNodeTypeAutomaton;
	}

	// TODO: Out of here. Has to do with MORE than just the schema.
	private Set<DeclaredNodeType> valueOfTouchedTypes = new HashSet<DeclaredNodeType>();

	// TODO: Out of here. Has to do with MORE than just the schema.
	public Set<DeclaredNodeType> getValueOfTouchedTypes() {
		return valueOfTouchedTypes;
	}

	public Set<DeclaredNodeType> unusedElementTypes(
			Set<DeclaredNodeType> usedTypes) {
		Set<DeclaredNodeType> elu = new HashSet<DeclaredNodeType>();

		for (ElementDecl decl : getAllElementDecls()) {
			elu.addAll(decl.getAllUses());
		}

		elu.removeAll(usedTypes);

		// elu.removeAll(valueOfTouchedTypes);

		return elu;
	}

	/*
	 * For diagnostics dumping. Should never be used for anything else... Record
	 * the types that a value-of expr has been run on...
	 * 
	 */
	public void addValueOfTouchedTypes(
			Set<? extends DeclaredNodeType> usedTypes, XPathPathExpr expr,
			DeclaredNodeType contextType) {
		try {
			Set<DeclaredNodeType> copy = new HashSet<DeclaredNodeType>(
					usedTypes);
			if (copy.contains(TextNT.chameleonInstance)) {
				copy.clear();
				if (expr.getStepCount() == 1)
					copy.add(contextType);
				else {
					XPathPathExpr cutt = (XPathPathExpr) expr.clone();
					cutt.removeLastStep();
					copy.addAll(possibleTargetNodes(cutt, contextType));
				}
			}
			valueOfTouchedTypes.addAll(copy);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/*
	 * private String constructPathRegExp(NodeSetLocationPath path) throws
	 * XSLToolsUnhandledNodeTestException, XSLToolsSchemaException { // Create
	 * initial regexp:
	 * 
	 * String P;
	 * 
	 * if (path instanceof NodeSetAbsLocationPath) { // Simply start with root
	 * node: P = "" + RootNT.instance.getCharRepresentation(this); } else { //
	 * Start with root and any following content (sigma star): P =
	 * RootNT.instance.getCharRepresentation(this) + ".*"; } // Iterate location
	 * steps: for (LocationStep step : path.getSteps()) { P =
	 * applyStepToRegExp(P, step, null); // Return immediately if has become the
	 * empty language: if (P.equals("#")) return P; } return P; }
	 */
	public void pathsToDot(String filename) throws Exception {
		// Open file for text writing:
		FileWriter out = new FileWriter(filename);

		// Fetch dot data from paths-FA:
		String dotData = schemaATSPathAutomaton.toDot();
		out.write(dotData);

		// Close file:
		out.close();
	}

	public int getElementTypeCount() {
		return allElementDecls.size();
	}

	public int getAttributeTypeCount() {
		return allAttributeDecls.size();
	}

	public int getTextTypeCount() {
		return 1;
	}

	public int getTotalTypeCount() {
		return getElementTypeCount() + getAttributeTypeCount()
				+ getTextTypeCount() + 1 + // comment
				1 + // PI
				1 // root
		;
	}

	/*
	 * public Set<BackendDecl> unusedTypes (Automaton totalSlammer) { Set<UndeclaredNodeType>
	 * theWholeSmack = new HashSet<UndeclaredNodeType>();
	 * theWholeSmack.add(NodeType.ONE_ANY_NAME_ELEMENT_NT);
	 * theWholeSmack.add(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT);
	 * theWholeSmack.add(TextNT.chameleonInstance);
	 * 
	 * Set<BackendDecl> touched = getDeclarationsFor(totalSlammer,
	 * theWholeSmack);
	 * 
	 * Set<BackendDecl> all = new HashSet<BackendDecl>();
	 * all.addAll(getAllElementDecls()); all.addAll(allAttributeDecls);
	 * all.add(TextNT.chameleonInstance);
	 * 
	 * all.removeAll(touched);
	 * 
	 * return all; }
	 */

	/*
	 * Construct a fragment making
	 * 
	 * interleave over (all attributes model, text, all childrens model) iff
	 * context is a complex mixed element type (could also just say, an element
	 * type with any text children at all)
	 * 
	 * Sequence over (all attributes model, all childrens model) iff context is
	 * a nonmixed element type (element type w o children)
	 * 
	 * Sequence over all children iff anything else. Probably empty. An fact,
	 * just empty except for root.
	 * 
	 * Then we will stick them together -- how???
	 */

	private static XPathPathExpr childElements;

	private static XPathPathExpr childText;

	private static XPathPathExpr attributes;

	static {
		try {
			childElements = (XPathPathExpr) XPathParser.parse("child::*");
			childText = (XPathPathExpr) XPathParser.parse("child::text()");
			attributes = (XPathPathExpr) XPathParser.parse("attribute::*");
		} catch (Exception ex) {
		}
	}

	public XMLGraph createSG(String systemId) throws XSLToolsSchemaException,
			XSLToolsUnhandledNodeTestException {
		XMLGraph result = new XMLGraph();

		SGFragment root = new SGFragment(result, systemId);
		root.createPlaceholder(RootNT.instance);

		// root.setEntryNode(rootnode);

		Set<DeclaredNodeType> all = fullDeclaredSeedSet();

		all.add(RootNT.instance);
		// all.add(TextNT.chameleonInstance);

		Map<DeclaredNodeType, SGFragment> everybodysContent = new HashMap<DeclaredNodeType, SGFragment>();

		for (DeclaredNodeType type : all) {
			everybodysContent.put(type, constructSGFragment(root, type,
					systemId));
		}

		// put together!
		for (Map.Entry<DeclaredNodeType, SGFragment> e : everybodysContent
				.entrySet()) {
			SGFragment curr = e.getValue();
			for (Map.Entry<DeclaredNodeType, ChoiceNode> e2 : curr.entrySet()) {
				DeclaredNodeType hunger = e2.getKey();
				SGFragment other = everybodysContent.get(hunger);
				if (other == null) {
					// throw new NullPointerException(hunger.toString());
					// hehehehe, try patch em up ... text types:
					Node kanone = hunger
							.constructInstantiationFM(curr, 0, this);
					other = new SGFragment(curr, systemId);
					other.setEntryNode(kanone);
				}
				curr.eat(hunger, other);
			}
			curr.hookup();
		}

		result.addRoot(root.getEntryNode());

		new XMLGraphReducer().reduce(result);

		return result;
	}

	private SGFragment constructSGFragment(SGFragment fraggle,
			DeclaredNodeType contextType, String systemId)
			throws XSLToolsSchemaException, XSLToolsUnhandledNodeTestException {

		SGFragment result = null;

		if (contextType == RootNT.instance)
			result = fraggle;
		else
			result = new SGFragment(fraggle.getXMLGraph(), systemId);

		Set<DeclaredNodeType> childTypes = possibleTargetNodes(childElements,
				contextType);
		Set<DeclaredNodeType> textTypes = possibleTargetNodes(childText,
				contextType);
		Set<DeclaredNodeType> attributeTypes = possibleTargetNodes(attributes,
				contextType);

		LinkedList<Integer> l = new LinkedList<Integer>();

		Node attributeFragmentNode = null;
		if (!attributeTypes.isEmpty()) {
			attributeFragmentNode = contextType.constructAttributeFM(result,
					this, attributeTypes);
			l.add(attributeFragmentNode.getIndex());
		}

		Node childFragmentNode = null;
		if (!childTypes.isEmpty()) {
			childFragmentNode = contextType.constructChildFM(result, this,
					false, childTypes, false, ContentOrder.FORWARD);
			l.add(childFragmentNode.getIndex());
		}

		Node textFragmentNode = null;
		if (!textTypes.isEmpty()) {
			textFragmentNode = contextType.constructChildFM(result, this,
					false, textTypes, false, ContentOrder.FORWARD);
		}

		Node contents = null;

		if (textFragmentNode == null) {
			if (l.size() == 0) // no children no attributes and no text
				contents = fraggle.createEpsilonNode();
			else if (l.size() == 1) // some children or attributes, no text
				contents = fraggle.getNodeAt(l.get(0));
			else
				contents = fraggle.createSequenceNode(l, null); // both children
																// and
			// attributes, no text
		} else {
			l.add(textFragmentNode.getIndex()); // add da text
			if (l.size() == 1) // text only
				contents = fraggle.getNodeAt(l.get(0));
			else
				// well simple content complex also get interleaved...
				contents = fraggle.createInterleaveNode(l, true, null);
		}

		Node nodeset = contextType.constructInstantiationFM(fraggle, contents
				.getIndex(), this);

		result.setEntryNode(nodeset);

		return result;
	}

	public SGFragment constructSGFragment(SGFragment fraggle,
			XPathPathExpr path, DeclaredNodeType contextType, /* Map<DeclaredNodeType,DeclaredNodeType> */
			Set<DeclaredNodeType> contextFlow, ContentOrder order,
			boolean allowInterleave) throws XSLToolsUnhandledNodeTestException,
			XSLToolsSchemaException {

		Set<DeclaredNodeType> nodeSet = new HashSet<DeclaredNodeType>();
		Set<DeclaredNodeType> newNodeSet = new HashSet<DeclaredNodeType>();

		SGFragment pathEvaluation = new SGFragment(fraggle, "for context type "
				+ contextType + " for path " + path);

		if (path instanceof XPathAbsolutePathExpr) {
			nodeSet.add(RootNT.instance);
			pathEvaluation.createPlaceholder(RootNT.instance);
		} else {
			nodeSet.add(contextType);
			pathEvaluation.createPlaceholder(contextType);
		}

		SGFragment first = null;

		for (Iterator<XPathStepExpr> stepIter = path.iterator(); stepIter
				.hasNext();) {
			XPathStepExpr step = stepIter.next();
			if (!(step instanceof XPathAxisStep)) {
				System.err.println("Select expressions in apply-templates "
						+ "must be node sets!");
			}

			XPathAxisStep astep = (XPathAxisStep) step;

			/*
			 * One famous application of the abstract test: For limiting SG
			 * fragments
			 */
			runAxis(nodeSet, newNodeSet, astep.getAxis());
			runTest(newNodeSet, astep);

			if (!stepIter.hasNext()) {
				/*
				 * Special stunt: If that LAST step result set is greater than
				 * the result of the flow analysis, reduce to that!!! That is:
				 * Each node in newNodeSet must be in the result, maybe as a
				 * dynamic redeclaration.
				 */

				Set<DeclaredNodeType> newNewNodeSet = new HashSet<DeclaredNodeType>();

				for (DeclaredNodeType type : contextFlow) {
					if (newNodeSet.contains(type.getOriginalDeclaration())
							|| newNodeSet.contains(type))
						newNewNodeSet.add(type);
				}

				newNodeSet = newNewNodeSet;

				// newNodeSet.retainAll(contextFlow/*.keySet()*/);
				// Set<DeclaredNodeType> diff = new
				// HashSet<DeclaredNodeType>(newNodeSet);
				// diff.removeAll(contextFlow);
				// if (!diff.isEmpty()) {
				// System.out.println("Some flow is missing, relative to naive
				// abstract
				// evaluation. We will plug it");
				// System.out.println(diff);
				// newNodeSet.removeAll(diff);
				// }
			}

			boolean onlyMaybe = astep.hasPredicates();

			boolean isPositionalPredicate = false;
			if (onlyMaybe && astep.getPredicateList().getPredicateCount() == 1) {
				XPathExpr predicate = astep.getPredicate(0);
				// TODO: Explicit case: position()==n
				isPositionalPredicate = (predicate instanceof XPathNumericLiteral);
			}

			for (DeclaredNodeType type : nodeSet) {
				SGFragment t = new SGFragment(fraggle, this
						+ " for context type " + contextType + " for step "
						+ step);
				Node n;
				switch (astep.getAxis()) {
				case XPathAxisStep.CHILD:
					n = type.constructChildFM(t, this, isPositionalPredicate,
							newNodeSet, allowInterleave, order);
					break;
				case XPathAxisStep.ATTRIBUTE:
					n = type.constructAttributeFM(t, this, newNodeSet);
					break;
				case XPathAxisStep.PARENT:
					n = type.constructParentFM(t, this, newNodeSet);
					break;
				case XPathAxisStep.SELF:
					if (newNodeSet.contains(type))
						n = t.createPlaceholder(type);
					else
						n = fraggle.createEpsilonNode();
					break;
				case XPathAxisStep.ANCESTOR:
					n = type.constructAncestorFM(t, this,
							isPositionalPredicate, newNodeSet);
					break;
				case XPathAxisStep.ANCESTOR_OR_SELF:
					n = type.constructAncestorOrSelfFM(t, this,
							isPositionalPredicate, newNodeSet, type, order);
					/*
					 * 
					 * Node n2 = fraggle.createPlaceholder(type); Collection<Integer>
					 * contents = new ArrayList<Integer>(2);
					 * contents.add(n2.getIndex()); contents.add(n.getIndex()); //
					 * Lav om til sequence, lav order om til at numse AS_AXIS, //
					 * OPPOSITE_AXIS (hvor er det lige vi bruger REVERSED
					 * overhovedet???) // , hvis bagvendt, vend sequencen om.
					 * Samme med DESC_O_S.
					 * 
					 * n = fraggle.createPermutationMonster(contents, new
					 * Origin( "ancestor-or-self", 0, 0));
					 */
					break;
				case XPathAxisStep.DESCENDANT:
					n = type.constructDescendantFM(t, this,
							isPositionalPredicate, newNodeSet);
					break;
				case XPathAxisStep.DESCENDANT_OR_SELF:

					// TODO:
					// - order of descendants can be approximated
					n = type.constructDescendantOrSelfFM(t, this,
							isPositionalPredicate, newNodeSet, type, order);

					/*
					 * // TODO: Dette er lidt i ged... Duer ikke ... helt
					 * utilstraekkelig approx. if (type != RootNT.instance) { n2 =
					 * fraggle.createPlaceholder(type); contents = new ArrayList<Integer>(2);
					 * contents.add(n2.getIndex()); contents.add(n.getIndex());
					 * if (isPositionalPredicate) n =
					 * fraggle.createChoiceNode(contents, new Origin(
					 * "descendant-or-self"+type, 0, 0)); else n =
					 * fraggle.createPermutationMonster(contents, new Origin(
					 * "descendant-or-self"+type, 0, 0)); }
					 */
					break;
				case XPathAxisStep.SIBLING:
				case XPathAxisStep.FOLLOWING:
				case XPathAxisStep.FOLLOWING_SIBLING:
				case XPathAxisStep.PRECEDING:
				case XPathAxisStep.PRECEDING_SIBLING:
					n = AbstractNodeType.s_constructPanicModel(t, this,
							isPositionalPredicate, newNodeSet);
					break;
				case XPathAxisStep.NAMESPACE:
					throw new XSLToolsUnhandledNodeTestException(
							"Namespace axis is unimplemented (and deprecated)");
				default:
					throw new AssertionError("Unimplemented axis "
							+ XPathAxisStep.axisToString(astep.getAxis()));
				}

				if (onlyMaybe) {
					n = t.constructOptionalCardinal(n, "predicate-maybe");
				}

				t.setEntryNode(n);

				if (first == null) {
					first = t;
				}

				pathEvaluation.eat(type, t);
				allowInterleave = false;
			}

			pathEvaluation.flush();

			Set<DeclaredNodeType> temp = nodeSet;
			nodeSet = newNodeSet;
			newNodeSet = temp;
			newNodeSet.clear();
		}

		if (first == null)
			return null;

		pathEvaluation.setEntryNode(first.getEntryNode());
		return pathEvaluation;
	}

	public Set<ElementDecl> getElementDeclsByName(QName name) {
		return allElementDeclsByName.get(name);
	}

	/*
	 * Either this, or a getTypeName() on ElementDecl and AttributeDecl. The
	 * latter is, in fact, easier.
	 */
	/*
	 * public abstract Set<? extends ElementDecl> getElementDeclsOfType(QName
	 * type); public abstract Set<? extends ElementDecl>
	 * getAttributeDeclsOfType(QName type);
	 */

	public void diagnostics(Branch parent, DocumentFactory fac,
			Set<Object> configuration) {
		Element me = fac.createElement("schema");
		parent.add(me);
		me.addAttribute("kind", getClass().getSimpleName());
		Element elements = fac.createElement("DeclaredElements");
		me.add(elements);
		for (ElementDecl decl : allElementDecls) {
			decl.diagnostics(elements, fac, configuration, true);
		}
		Element attributes = fac.createElement("DeclaredAttributes");
		me.add(attributes);
		for (AttributeDecl decl : allAttributeDecls) {
			decl.diagnostics(attributes, fac, configuration);
		}
		Element misc = fac.createElement("Misc");
		me.add(misc);

		Element nameToCharMappings = fac.createElement("ElementNameMappings");
		me.add(nameToCharMappings);

		for (Map.Entry<QName, Character> e : elementQNameToCharMap.entrySet()) {
			Element mapping = fac.createElement("Mapping");
			nameToCharMappings.add(mapping);
			mapping.addAttribute("name", e.getKey().getName());
			mapping.addAttribute("namespaceURI", e.getKey().getNamespaceURI());
			mapping.addAttribute("char", "" + e.getValue());
		}

		nameToCharMappings = fac.createElement("AttributeNameMappings");
		me.add(nameToCharMappings);

		for (Map.Entry<QName, Character> e : attributeQNameToCharMap.entrySet()) {
			Element mapping = fac.createElement("Mapping");
			nameToCharMappings.add(mapping);
			mapping.addAttribute("name", e.getKey().getName());
			mapping.addAttribute("namespaceURI", e.getKey().getNamespaceURI());
			mapping.addAttribute("char", "" + e.getValue());
		}
	}
}
