package dongfang.xsltools.xmlclass.schemaside.dropoff;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.dom4j.QName;

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
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathPathExpr;
import dongfang.xsltools.xpath2.XPathStepExpr;

public class AttributePresentDynamicRedeclaration extends
		ElementUseDynamicRedeclaration {

	private final AttributeUse mkReq;

	private final boolean isAcceptor;

	/*
	 * prototype pattern stuff
	 */
	AttributePresentDynamicRedeclaration() {
		super(null);
		this.mkReq = null;
		this.isAcceptor = false;
	}

	private AttributePresentDynamicRedeclaration(ElementUse required,
			AttributeUse mkReq, boolean isAcceptor) {
		super(required);
		this.mkReq = mkReq;
		this.isAcceptor = isAcceptor;
	}

	@Override
	public CardinalMatch transform(SingleTypeXMLClass clazz,
			DeclaredNodeType candidate, XPathExpr predicate)
			throws XSLToolsSchemaException {

		// not our predicate, leave
		if (!(predicate instanceof XPathPathExpr))
			return CardinalMatch.MAYBE_ARG_TYPE;

		XPathPathExpr p = (XPathPathExpr) predicate;

		if (p.getStepCount() == 0)
			return CardinalMatch.MAYBE_ARG_TYPE;

		try {
			XPathStepExpr se = (XPathStepExpr) p.getFirstStep();

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
					if (keeper != null)
						more = true;
					keeper = au;
				}
			}

			if (keeper == null)
				return CardinalMatch.NEVER_ARG_TYPE;

			if (more)
				return CardinalMatch.MAYBE_ARG_TYPE;

			DynamicRedeclaration t1 = new AttributePresentDynamicRedeclaration(
					(ElementUse) candidate, keeper, true);
			DynamicRedeclaration t2 = new AttributePresentDynamicRedeclaration(
					(ElementUse) candidate, keeper, false);

			return new CardinalMatch(t1, t2);
		} catch (XSLToolsXPathException ex) {
			throw new AssertionError(ex);
		}
	}

	/*
	 * @Override public DeclaredNodeType reject(SingleTypeXMLClass clazz,
	 * DeclaredNodeType candidate, XPathExpr predicate) throws
	 * XSLToolsSchemaException { // not our predicate, leave if (!(predicate
	 * instanceof XPathPathExpr)) return candidate;
	 * 
	 * XPathPathExpr p = (XPathPathExpr) predicate;
	 * 
	 * if (p.getStepCount() == 0) return candidate;
	 * 
	 * try { XPathStepExpr se = (XPathStepExpr) p.getFirstStep();
	 * 
	 * if (!(se instanceof XPathAxisStep)) return candidate;
	 * 
	 * XPathAxisStep ase = (XPathAxisStep) se;
	 * 
	 * if (ase.getAxis() != XPathAxisStep.ATTRIBUTE) return candidate;
	 * 
	 * if (!(candidate instanceof ElementUse)) return candidate;
	 * 
	 * ElementUse eu = (ElementUse) candidate;
	 * 
	 * Map<QName, AttributeUse> aus = new HashMap<QName, AttributeUse>();
	 * 
	 * eu.attributeUses(aus);
	 *  // all declared attributes passing the node test... // iff one, we can
	 * do some meaningful decoration, // otherwise, give it up... // iff zero,
	 * ok that element sure never gets past // the predicate anyway.
	 * AttributeUse keeper = null; boolean more = false;
	 * 
	 * for (QName name : aus.keySet()) { AttributeUse au = aus.get(name); if
	 * (ase.getNodeTest().accept(au, clazz)) { if (keeper != null) more = true;
	 * keeper = au; } }
	 * 
	 * if (more) return candidate;
	 * 
	 * return new AttributePresentDynamicRedeclaration((ElementUse) candidate,
	 * keeper, false);
	 *  } catch (XSLToolsXPathException ex) { throw new AssertionError(ex); } }
	 */

	@Override
	public void attributeUses(Map<QName, AttributeUse> dumper) {
		super.attributeUses(dumper);
		if (!isAcceptor) {
			dumper.remove(mkReq.getQName());
		}
	}

	@Override
	public Node constructAttributeFM(SGFragment fraggle,
			SingleTypeXMLClass clazz, Set<DeclaredNodeType> typeSet)
			throws XSLToolsSchemaException {
		if (typeSet.contains(mkReq)) {
			typeSet.remove(mkReq);
			if (isAcceptor) {
				AttributeUse replace = new AttributeUseDecorator(mkReq) {
					@Override
					public Node constructFlowModel(SGFragment fraggle,
							SingleTypeXMLClass clazz) {
						// here is the point of it all: Atribute always present.
						return constructFlowModel(fraggle, clazz,
								Cardinal.REQUIRED);
					}
				};
				typeSet.add(replace);
			}
		}
		return decorated.constructAttributeFM(fraggle, clazz, typeSet);
	}

	@Override
	protected boolean dynamicRedeclarationPropertiesEquals(
			DynamicRedeclaration o) {
		AttributePresentDynamicRedeclaration o2 = (AttributePresentDynamicRedeclaration) o;
		return mkReq != null && mkReq.equals(o2.mkReq)
				&& isAcceptor == o2.isAcceptor;
	}

	@Override
	public void runAttributeAxis(SingleTypeXMLClass clazz,
			Set<? super DeclaredNodeType> result) {
		decorated.runAttributeAxis(clazz, result);
		if (!isAcceptor) {
			result.remove(mkReq);
		}
	}

	@Override
	public String toLabelString() {
		return super.toLabelString() + "["+(isAcceptor ? "" : "!")+"@"
				+ Dom4jUtil.clarkName(mkReq.getQName()) + "]";
	}

	@Override
	public String toString() {
		return super.toString() + "["+(isAcceptor ? "" : "!")+"@" + Dom4jUtil.clarkName(mkReq.getQName())
				+ "]";
	}
}