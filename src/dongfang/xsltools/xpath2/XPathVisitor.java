package dongfang.xsltools.xpath2;

import dongfang.xsltools.exceptions.XSLToolsException;

/*
 * If needed, the distinction between types can always be made
 * finer (replace visit on a common supertype by a set of visit,
 * one for each relevant subtype...)
 */
public class XPathVisitor {

  /**
   * Visit a node in an expression. This implementation just recurses (this
   * class is not a visitor in the traditional sense -- recursion is driven from
   * within the visitor!) and returns its argument.
   * 
   * @param trashcan
   * @return
   * @throws XSLToolsException
   */
  XPathBase visit(XPathBase trashcan) throws XSLToolsException {
    continueVisit(trashcan, trashcan);
    return trashcan;
  }

  /**
   * Visit children of node. This implementation does the following: - Calls
   * descend() to indicate that a child descent it started. - Visits each child
   * on <em>trachcan</em>. If a non-null result comes back, it is added on
   * <em>result</em>. The order of child recursion results is preserved.
   * <em>trascan</em> and <result> may alias, in that case, the children on
   * <em>result</em> are replaced by the visit results.
   * 
   * @param trashcan
   * @param result
   * @throws XSLToolsException
   */
  protected void continueVisit(XPathBase trashcan, XPathBase result)
      throws XSLToolsException {
    descend();
    int j = 0;
    for (int i = 0; i < trashcan.jjtGetNumChildren(); i++) {
      XPathBase c = (XPathBase) trashcan.jjtGetChild(i);

      XPathBase r = c.accept(this);
      if (r != null)
        result.jjtAddChild(r, j++);
      /*
       * if (c instanceof XPathFunctionCallExpr) { if
       * (getClass()==KeyKiller.class) { if
       * (((XPathFunctionCallExpr)c).getQName().getName().equals("key")) {
       * System.out.println("continueVisit: Got key in, visitor returned " + r); } } }
       */
    }
    if (j < result.jjtGetNumChildren()) {
      // shrink child array
      Node[] chs = result.children;
      Node[] shrunk = new Node[j];
      System.arraycopy(chs, 0, shrunk, 0, j);
      result.children = shrunk;
    }
    ascend();
  }

  /*
   * Invoked after a node has been visited, but before its first child is
   * visited. Useful for reconstructing local scopes.
   */
  protected void descend() {
  }

  /*
   * Invoked after the last child of a node (and all their descendants) have
   * been visited. Useful for reconstructing end of local scopes.
   */
  protected void ascend() {
  }

  XPathBase visit(XPathAxisStep a) throws XSLToolsException {
    visit((XPathBase) a);
    return visit((XPathBase) a);
  }

  /*
   * This is used by (at least) the variable resolver.
   */
  XPathBase visit(XPathVarRef r) throws XSLToolsException {
    visit((XPathBase) r);
    return visit((XPathBase) r);
  }

  XPathBase visit(XPathFunctionCallExpr e) throws XSLToolsException {
    visit((XPathBase) e);
    return visit((XPathBase) e);
  }

  XPathBase visit(XPathPathExpr e) throws XSLToolsException {
    visit((XPathBase) e);
    return visit((XPathBase) e);
  }

  /*
   * Local variables are bound in quantified exprs. This is used by (at least)
   * the variable resolver.
   */
  XPathBase visit(XPathQuantifiedExpr e) throws XSLToolsException {
    visit((XPathBase) e);
    return visit((XPathBase) e);
  }

  /*
   * Local variables are bound in for exprs. This is used by (at least) the
   * variable resolver.
   */
  XPathBase visit(XPathForExpr e) throws XSLToolsException {
    visit((XPathBase) e);
    return visit((XPathBase) e);
  }

  /*
   * Local variables are bound in for exprs. This is used by (at least) the
   * variable resolver.
   */
  XPathBase visit(XPathSimpleForClause2 e) throws XSLToolsException {
    visit((XPathBase) e);
    return visit((XPathBase) e);
  }
}
