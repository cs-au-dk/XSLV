package dongfang.xsltools.xmlclass.schemaside.dropoff;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dom4j.QName;

import dk.brics.automaton.Automaton;
import dk.brics.misc.Automata;
import dk.brics.xmlgraph.Node;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.xmlclass.schemaside.AttributeUse;
import dongfang.xsltools.xmlclass.schemaside.AttributeUseDecorator;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xpath2.XPathAxisStep;
import dongfang.xsltools.xpath2.XPathComparisonExpr;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathNumericLiteral;
import dongfang.xsltools.xpath2.XPathPathExpr;
import dongfang.xsltools.xpath2.XPathStepExpr;
import dongfang.xsltools.xpath2.XPathStringLiteral;

public class AttributeValueDynamicRedeclaration extends
		ElementUseDynamicRedeclaration {
	public class Positive extends AttributeUseDecorator {

		Positive(AttributeUse use) {
			super(use);
		}

		@Override
		public Node constructFlowModel(SGFragment fraggle,
				SingleTypeXMLClass clazz) {
			// here is the point of it all: Atribute always present.
			return constructFlowModel(fraggle, clazz, Cardinal.REQUIRED);
		}

		@Override
		public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz) {
			return Automaton.makeString(fixedValue);
		}
	}

	public class Negative extends AttributeUseDecorator {
		Negative(AttributeUse use) {
			super(use);
		}

		@Override
		public Automaton getValueOfAutomaton(SingleTypeXMLClass clazz)
				throws XSLToolsSchemaException {
			Automaton a = super.getValueOfAutomaton(clazz);
			return a.intersection(Automaton.makeString(fixedValue).complement());
		}
	}

	private final AttributeUse mkReq;

	private final String fixedValue;

	private final boolean isAccepting;

	AttributeValueDynamicRedeclaration() {
		super(null);
		this.mkReq = null;
		this.fixedValue = null;
		this.isAccepting = false;
	}

	private AttributeValueDynamicRedeclaration(ElementUse required,
			AttributeUse mkReq, String fixedValue, boolean isAccepting) {
		super(required);
		this.mkReq = mkReq;
		this.fixedValue = fixedValue;
		this.isAccepting = isAccepting;
	}

	@Override
	public CardinalMatch transform(SingleTypeXMLClass clazz,
			DeclaredNodeType candidate, XPathExpr predicate)
			throws XSLToolsSchemaException {
		boolean isEqualOperator = true;

		// not our predicate, leave
		if (!(predicate instanceof XPathComparisonExpr))
			return CardinalMatch.MAYBE_ARG_TYPE;

		XPathComparisonExpr p = (XPathComparisonExpr) predicate;

		if (!"=".equals(p.getOperator())) {
			isEqualOperator = false;
		}
		
		if (!isEqualOperator && !"!=".equals(p.getOperator()))
			return CardinalMatch.MAYBE_ARG_TYPE;

		if (!(p.getLHS() instanceof XPathPathExpr)
				&& !(p.getRHS() instanceof XPathPathExpr))
			return CardinalMatch.MAYBE_ARG_TYPE;

		if (!(p.getLHS() instanceof XPathStringLiteral)
				&& !(p.getRHS() instanceof XPathStringLiteral)
				&& !(p.getLHS() instanceof XPathNumericLiteral)
				&& !(p.getRHS() instanceof XPathNumericLiteral))
			return CardinalMatch.MAYBE_ARG_TYPE;

		XPathPathExpr pe;

		XPathExpr valueExp;

		if ((p.getLHS() instanceof XPathStringLiteral)
				|| (p.getLHS() instanceof XPathNumericLiteral)) {
			pe = (XPathPathExpr) p.getRHS();
			valueExp = p.getLHS();
		} else {
			pe = (XPathPathExpr) p.getLHS();
			valueExp = p.getRHS();
		}

		String val;

		if (valueExp instanceof XPathStringLiteral) {
			val = ((XPathStringLiteral) valueExp).getSweetContent();
		} else if (valueExp instanceof XPathNumericLiteral) {
			val = ((XPathNumericLiteral) valueExp).toString();
		} else {
			return CardinalMatch.MAYBE_ARG_TYPE;
		}

		if (pe.getStepCount() == 0)
			return CardinalMatch.MAYBE_ARG_TYPE;

		try {
			XPathStepExpr se = (XPathStepExpr) pe.getFirstStep();

			if (!(se instanceof XPathAxisStep))
				return CardinalMatch.MAYBE_ARG_TYPE;

			XPathAxisStep ase = (XPathAxisStep) se;

			if (ase.getAxis() != XPathAxisStep.ATTRIBUTE)
				return CardinalMatch.MAYBE_ARG_TYPE;

			if (!(candidate instanceof ElementUse))
				return CardinalMatch.NEVER_ARG_TYPE;

			ElementUse eu = (ElementUse) candidate;

			Map<QName, AttributeUse> aus = new HashMap<QName, AttributeUse>();

			eu.attributeUses(aus);

			// all declared attributes passing the node test...
			// iff one, we can do some meaningful decoration,
			// otherwise, give it up...
			// iff zero, ok that element sure never gets past
			// the predicate anyway.
			AttributeUse keeper = null;
			boolean more = false;

			for (QName name : aus.keySet()) {
				AttributeUse au = aus.get(name);
				if (ase.getNodeTest().accept(au, clazz)) {
					if (au.getValueOfAutomaton(clazz).run(val)) {
						if (keeper != null)
							more = true;
						keeper = au;
					}
				}
			}

			/*
			 * Case: No automaton accepted value.
			 */
			if (keeper == null)
				return CardinalMatch.NEVER_ARG_TYPE;

			/*
			 * Case: More than one automaton accepted value.
			 */
			if (more)
				return CardinalMatch.MAYBE_ARG_TYPE;

			/*
			 * We want to see whether val is the only accepted string of the automaton -- that is, is it simply a constant string
			 * automaton for val??
			 */
			Automaton alwaysMatches = Automaton.makeString(val);
			if (keeper.getValueOfAutomaton(clazz).equals(alwaysMatches))
				return CardinalMatch.ALWAYS_ARG_TYPE;
			
			/*
			 * Put together a pass type and a fail type.
			 */
			DynamicRedeclaration dr1 = new AttributeValueDynamicRedeclaration(
					(ElementUse) candidate.getOriginalDeclaration(), keeper,
					val, true);

			DynamicRedeclaration dr2 = new AttributeValueDynamicRedeclaration(
					(ElementUse) candidate.getOriginalDeclaration(), keeper,
					val, false);

			/*
			 * Case: Fail type has no values -- always pass.
			 */
			if (dr2.getValueOfAutomaton(clazz).isEmpty()) {
				return CardinalMatch.ALWAYS_ARG_TYPE;
			}
			
			CardinalMatch maybeResult;

			if (isEqualOperator)
				maybeResult = new CardinalMatch(dr1, dr2);
			else
				maybeResult = new CardinalMatch(dr2, dr1);

			return maybeResult;
		} catch (XSLToolsXPathException ex) {
			throw new AssertionError(ex);
		}
	}

	@Override
	public void runAttributeAxis(SingleTypeXMLClass clazz,
			Set<? super DeclaredNodeType> result) {
		super.runAttributeAxis(clazz, result);
		AttributeUse replace = isAccepting ? new Positive(mkReq) : new Negative(mkReq);
		if (result.contains(mkReq.getOriginalDeclaration())) {
			result.remove(mkReq.getOriginalDeclaration());
			result.add(replace);
		}
	}

	@Override
	public void attributeUses(Map<QName, AttributeUse> dumper) {
		super.attributeUses(dumper);
		AttributeUse replace = isAccepting ? new Positive(mkReq) : new Negative(mkReq);
		for (Map.Entry<QName, AttributeUse> u : dumper.entrySet()) {
			if (u.getValue().equals(mkReq.getOriginalDeclaration())) {
				u.setValue(replace);
			}
		}
	}

	@Override
	public Node constructAttributeFM(SGFragment fraggle,
			SingleTypeXMLClass clazz, Set<DeclaredNodeType> typeSet)
			throws XSLToolsSchemaException {
		// TODO Auto-generated method stub
		// return super.constructAttributeFM(fraggle, clazz, typeSet);
		// AttributeUse au = null;

		AttributeUse replace = isAccepting ? new Positive(mkReq) : new Negative(mkReq);

		if (typeSet.contains(mkReq.getOriginalDeclaration())) {
			typeSet.remove(mkReq.getOriginalDeclaration());
			typeSet.add(replace);
		}

		return decorated.constructAttributeFM(fraggle, clazz, typeSet);
	}

	@Override
	public String toLabelString() {
		return super.toLabelString() + "[@"
				+ Dom4jUtil.clarkName(mkReq.getQName()) + (isAccepting ? "=": "!=") + "'" + fixedValue
				+ "']";
	}

	@Override
	public String toString() {
		return super.toString() + "[@" + Dom4jUtil.clarkName(mkReq.getQName())
				+ (isAccepting ? "=": "!=") + "'" + fixedValue + "']";
	}

	@Override
	protected boolean dynamicRedeclarationPropertiesEquals(
			DynamicRedeclaration o) {
		AttributeValueDynamicRedeclaration o2 = (AttributeValueDynamicRedeclaration) o;
		return mkReq != null && mkReq.equals(o2.mkReq)
				&& fixedValue.equals(o2.fixedValue)
				&& isAccepting == o2.isAccepting;
	}
}