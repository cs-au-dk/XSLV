package dongfang.xsltools.xmlclass.schemaside.dropoff;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Origin;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.CharNameResolver;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NodeType;
import dongfang.xsltools.xpath2.XPathAxisStep;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathPathExpr;
import dongfang.xsltools.xpath2.XPathStepExpr;

/**
 * The class uses the Prototype design pattern: Some instances do not decorate a
 * type, but act purely as checkers-and-decorator-adders.
 * 
 * @author dongfang
 */
public abstract class DynamicRedeclaration implements DeclaredNodeType {

	public static final Set<DynamicRedeclaration> prototypes = new HashSet<DynamicRedeclaration>();

	/*
	static {
		// Remember the list of prototypes.
		prototypes.add(new AttributePresentDynamicRedeclaration());
		prototypes.add(new AttributeValueDynamicRedeclaration());
		prototypes.add(new ElementLocalNameDynamicRedeclaration());
		prototypes.add(new AttributeLocalNameDynamicRedeclaration());
	}
	*/

	public static boolean alwaysPassesPredicate(DeclaredNodeType candidate,
			XPathPathExpr expr, SingleTypeXMLClass clazz)
			throws XSLToolsSchemaException {
		if (!expr.hasPredicates())
			return true;

		Iterator<XPathStepExpr> steps = expr.steps();

		XPathStepExpr lastStep = null;

		/*
		 * We can only analyze the last predicate in a multi step expr.
		 */
		while (steps.hasNext()) {
			XPathStepExpr step = steps.next();
			lastStep = step;
			if (steps.hasNext()) {
				if (step.hasPredicates())
					return false;
			}
		}

		/*
		 * Abnormal case, but at least we will avoid crashes by handling it
		 */
		if (lastStep == null)
			return false;

		/*
		 * Abnormal case, but at least we will avoid crashes by handling it
		 */
		if (!(lastStep instanceof XPathAxisStep))
			return false;

		XPathAxisStep atep = (XPathAxisStep) lastStep;

		if (atep.getPredicateList().getPredicateCount() > 1)
			return false;

		XPathExpr predicate = atep.getPredicate(0);

		// current limitation, needs not always be so..
		if (candidate instanceof DynamicRedeclaration) {
			DynamicRedeclaration redecl = (DynamicRedeclaration) candidate;
			return redecl.transform(clazz, candidate, predicate)
					.argTypeAlwaysPasses();
		}
		return false;
	}

	// null when we are a prototype
	protected final DeclaredNodeType decorated;

	/**
	 * Constructor for non-prototypes.
	 * 
	 * @param decorated
	 */
	protected DynamicRedeclaration(DeclaredNodeType decorated) {
		this.decorated = decorated;
	}

	public DeclaredNodeType getDecorated() {
		return decorated;
	}

	public abstract CardinalMatch transform(SingleTypeXMLClass clazz,
			DeclaredNodeType candidate, XPathExpr predicate)
			throws XSLToolsSchemaException;

	public void diagnostics(Branch parent, DocumentFactory fac,
			Set<Object> configuration) {
		Element local = fac.createElement("dynamic-redeclaration");
		local.addAttribute("label", toLabelString());
		parent.add(local);
		decorated.diagnostics(local, fac, configuration);
	}

	/*
	 * The methodes to be overridden as needed.
	 */
	public Node constructAncestorFM(SGFragment fraggle,
			SingleTypeXMLClass clazz, boolean maxOne,
			Set<DeclaredNodeType> typeSet) throws XSLToolsSchemaException {
		return decorated.constructAncestorFM(fraggle, clazz, maxOne, typeSet);
	}

	public Node constructAncestorOrSelfFM(SGFragment fraggle,
			SingleTypeXMLClass clazz, boolean maxOne,
			Set<DeclaredNodeType> typeSet, DeclaredNodeType selfType,
			ContentOrder order) throws XSLToolsSchemaException {
		return decorated.constructAncestorOrSelfFM(fraggle, clazz, maxOne,
				typeSet, selfType, order);
	}

	public Node constructAttributeFM(SGFragment fraggle,
			SingleTypeXMLClass clazz, Set<DeclaredNodeType> typeSet)
			throws XSLToolsSchemaException {
		return decorated.constructAttributeFM(fraggle, clazz, typeSet);
	}

	public Node constructChildFM(SGFragment fraggle, SingleTypeXMLClass clazz,
			boolean maxOne, Set<DeclaredNodeType> weCare,
			boolean allowInterleave, ContentOrder order)
			throws XSLToolsSchemaException {
		return decorated.constructChildFM(fraggle, clazz, maxOne, weCare,
				allowInterleave, order);
	}

	public Node constructDescendantFM(SGFragment fraggle,
			SingleTypeXMLClass clazz, boolean maxOne,
			Set<DeclaredNodeType> typeSet) throws XSLToolsSchemaException {
		return decorated.constructDescendantFM(fraggle, clazz, maxOne, typeSet);
	}

	public Node constructDescendantOrSelfFM(SGFragment fraggle,
			SingleTypeXMLClass clazz, boolean maxOne,
			Set<DeclaredNodeType> typeSet, DeclaredNodeType selfType,
			ContentOrder order) throws XSLToolsSchemaException {
		return decorated.constructDescendantOrSelfFM(fraggle, clazz, maxOne,
				typeSet, selfType, order);
	}

	public Node constructInstantiationFM(SGFragment fraggle, int content,
			SingleTypeXMLClass clazz) throws XSLToolsSchemaException {
		return decorated.constructInstantiationFM(fraggle, content, clazz);
	}

	public Node constructParentFM(SGFragment fraggle, SingleTypeXMLClass clazz,
			Set<DeclaredNodeType> typeSet) throws XSLToolsSchemaException {
		return decorated.constructParentFM(fraggle, clazz, typeSet);
	}

	public Node constructSelfFM(SGFragment fraggle, DeclaredNodeType selfType)
			throws XSLToolsSchemaException {
		return decorated.constructSelfFM(fraggle, /* selfType */this);
	}

	public Automaton getATSAutomaton(SingleTypeXMLClass clazz) {
		return decorated.getATSAutomaton(clazz);
	}

	public Origin getDeclarationOrigin() {
		return decorated.getDeclarationOrigin();
	}

	public Object getIdentifier() {
		return decorated.getIdentifier();
	}

	public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz)
			throws XSLToolsSchemaException {
		return decorated.getValueOfAutomaton(clazz);
	}

	public boolean matches(XPathAxisStep s, SingleTypeXMLClass clazz)
			throws XSLToolsSchemaException {
		return decorated.matches(s, clazz);
	}

	public void runAttributeAxis(SingleTypeXMLClass clazz,
			Set<? super DeclaredNodeType> result) {
		decorated.runAttributeAxis(clazz, result);
	}

	public void runChildAxis(SingleTypeXMLClass clazz,
			Set<? super DeclaredNodeType> result)
			throws XSLToolsSchemaException {
		decorated.runChildAxis(clazz, result);
	}

	public void runParentAxis(SingleTypeXMLClass clazz,
			Set<? super DeclaredNodeType> result) {
		decorated.runParentAxis(clazz, result);
	}

	public void runReverseAttributeAxis(SingleTypeXMLClass clazz,
			Set<? super DeclaredNodeType> result) {
		decorated.runReverseAttributeAxis(clazz, result);
	}

	public void runReverseChildAxis(SingleTypeXMLClass clazz,
			Set<? super DeclaredNodeType> result) {
		decorated.runReverseChildAxis(clazz, result);
	}

	public char getCharRepresentation(CharNameResolver resolver) {
		return decorated.getCharRepresentation(resolver);
	}

	public String toLabelString() {
		return decorated.toLabelString();// getClass().getSimpleName() + "
											// over " +
		// decorated.toLabelString();
	}

	public int compareTo(NodeType o) {
		return decorated.compareTo(o);
	}

	protected abstract boolean dynamicRedeclarationPropertiesEquals(
			DynamicRedeclaration o);

	boolean dynamicRedeclarationEquals(DynamicRedeclaration o) {
		return o.getClass() == getClass()
				&& dynamicRedeclarationPropertiesEquals(o);
	}

	@Override
	public boolean equals(Object o) {
		if (decorated == null)
			return false;

		if (o instanceof DynamicRedeclaration) {
			return dynamicRedeclarationEquals((DynamicRedeclaration) o);
		}

		// experiment: A declaration and its dropoffs are not the same.
		// return decorated.equals(o);
		return false;
	}

	@Override
	public int hashCode() {
		if (decorated == null)
			return 0; // gedefims
		return decorated.hashCode();
	}

	public DeclaredNodeType getOriginalDeclaration() {
		return getDecorated().getOriginalDeclaration();
	}

	public Collection<DeclaredNodeType> getMyTypes(
			Collection<DeclaredNodeType> choices) {
		Collection<DeclaredNodeType> result = new HashSet<DeclaredNodeType>();
		if (choices.contains(this))
			result.add(this);
		return result;
	}

	@Override
	public String toString() {
		return decorated.toString();
	}
}