package dongfang.xsltools.controlflow;

import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.xsltools.exceptions.XSLToolsSchemaException;
import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.util.Util;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xpath2.XPathExpr;

/**
 * apply-templates instruction might or might not be able to match with and
 * instantiate the target template rule. CandidateEdge objects are not part of
 * the control flow graph. They are simply a temporary holder.
 */
class CandidateEdge extends NewContextFlow {
  final ApplyTemplatesInst sourceApply; // apply-templates instruction from

  // which the edge originates.
  final int pathIndex; // Index of the source apply-templates

  // selection.
  // final Selection sourceSelect; // Source selection (location path).
  final Selection sourceSelect;

  /*
   * For diagnostics usage: Edge test may let the edge live on, even if they
   * determine that a more accurate analysis would surely kill it, but update
   * this bit. When the more accurate analysis sees the edge, a check could be
   * added in the code to test whether predicted-dead edged really are all
   * killed.
   */
  boolean wasPredictedToDie;

  /*
   * Reverse of the above: The reverse mill tester predicts with certainty the
   * survival -- not the death -- of edges in a more accurate edge killer test.
   * The reverse mill check could, as a sanity check, update this bit, and the
   * consequent, more accurate analysis could check that the edge really passes.
   * Still just diagnostics usage.
   */
  // boolean wasPredictedToSurvive;
  Set<DeclaredNodeType> maybeSurviveableTypes;

  Set<DeclaredNodeType> definitelySurviveableTypes;

  XPathExpr alphaExprForMills;

  XPathExpr alphaExprForAutomata;

  CandidateEdge(ApplyTemplatesInst sourceApply, int pathIndex,
      Selection selection, DeclaredNodeType contextType, TemplateRule target,
      ContextMode mode) {
    super(contextType, target, mode);
    this.sourceApply = sourceApply;
    this.pathIndex = pathIndex;
    this.sourceSelect = selection;
    assert (sourceApply == null || selection == sourceApply.selections
        .get(pathIndex));
  }

  /*
   * void canonicalizeFlows() { sourceSelect.canonicalizeFlows(contextType); }
   */

  ContextFlow getContextFlow() {
    return sourceSelect.getContextFlow(contextMode, contextType);
  }

  XPathExpr getAlphaExpressionForAutomata(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException, XSLToolsXPathException {
    if (this.alphaExprForAutomata == null)
      this.alphaExprForAutomata = sourceSelect.makeAlphaNodeSetExpForAutomata(
          contextType, clazz);
    return alphaExprForAutomata;
  }

  XPathExpr getAlphaExpressionForMills(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException, XSLToolsXPathException {
    if (this.alphaExprForMills == null)
      this.alphaExprForMills = sourceSelect.makeAlphaNodeSetExpForMills(
          contextType, clazz);
    return alphaExprForMills;
  }

  /*
  boolean allGuaranteedSurvivors(SingleTypeXMLClass clazz)
      throws XSLToolsSchemaException, XSLToolsXPathException {
    XPathExpr alpha = getAlphaExpressionForMills(clazz);
    if (alpha instanceof XPathPathExpr) {
      *//*
       * Apply the reverse step-by-step abstract mill (tm) to the two location
       * paths and the current context node, to see if survival of all flows in
       * the result of the above forward end-to-end abstract mill (tm) can be
       * guaranteed. If yes, then we know there's no need to run the alpha-beta
       * regexp test on them.
       *//*
      return (clazz.allPossibleFlowsCovered((XPathPathExpr) alpha,
          target.match, maybeSurviveableTypes, true, contextType));
    }
    return false; // give up
  }*/

  @Override
public String toString() {
    if (sourceApply == null)
      return "<candidateEdge root=\"bootstrap-rule\" contextNode=\""
          + contextType + "\" targetMatch=\"" + target.match
          + "\" targetMode=\"" + target.mode + "\"/>";

    return "<candidateEdge selection=\'" + sourceSelect + "\" mode=\""
        + sourceApply.getMode() + "\" contextNode=\"" + contextType
        + "\" targetMatch=\"" + target.match + "\" targetMode=\"" + target.mode
        + "\" targetPriority=\"" + target.priority + "\"/>";
  }

  @Override
public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement(Util
        .capitalizedStringToHyphenString(getClass()));
    parent.add(me);
    me.addAttribute("mode", contextMode.toString());
    me.addAttribute("pathIndex", Integer.toString(pathIndex));
    Element sourceApplyDiag = fac.createElement("source-apply-templates");
    me.add(sourceApplyDiag);
    if (sourceApply != null)
      sourceApply.diagnostics(sourceApplyDiag, fac, configuration);
    else
      sourceApplyDiag.add(fac.createText("bootstrap-rule"));
    Element selectionDiag = fac.createElement("selection");
    me.add(selectionDiag);
    sourceSelect.diagnostics(selectionDiag, fac, configuration);
    Element contextNodeDiag = fac.createElement("context-node");
    me.add(contextNodeDiag);
    if (contextType != null)
      contextType.diagnostics(contextNodeDiag, fac, configuration);
    Element targetDiag = fac.createElement("target");
    me.add(targetDiag);
    target.diagnostics(targetDiag, fac, configuration);
  }
}
