/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-20
 */
package dongfang.xsltools.controlflow;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.xmlclass.xslside.AttributeNT;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.ElementNT;
import dongfang.xsltools.xmlclass.xslside.NodeType;
import dongfang.xsltools.xmlclass.xslside.PINT;
import dongfang.xsltools.xmlclass.xslside.RootNT;
import dongfang.xsltools.xmlclass.xslside.TextNT;
import dongfang.xsltools.xmlclass.xslside.UndeclaredNodeType;
import dongfang.xsltools.xpath2.XPathAbsolutePathExpr;
import dongfang.xsltools.xpath2.XPathAxisStep;
import dongfang.xsltools.xpath2.XPathNodeTest;
import dongfang.xsltools.xpath2.XPathPathExpr;
import dongfang.xsltools.xpath2.XPathStepExpr;

/**
 * An implementation of the Dong and Bailey Raw-TAG flow grapher.
 * @author dongfang
 */
public class RawTAGFlowGrapher extends AbstractContextInsensitiveFlowGrapher {
  
  private static RawTAGFlowGrapher instance = new RawTAGFlowGrapher();

  public static RawTAGFlowGrapher getInstance() {
    return instance;
  }

  /**
   * Return {e*}
   */
  private static Set<UndeclaredNodeType> getEStarSingleton() {
    Set<UndeclaredNodeType> result = new HashSet<UndeclaredNodeType>();
    result.add(NodeType.ONE_ANY_NAME_ELEMENT_NT);
    return result;
  }

  /**
   * Return {a*}
   */
  private static Set<UndeclaredNodeType> getAStarSingleton() {
    Set<UndeclaredNodeType> result = new HashSet<UndeclaredNodeType>();
    result.add(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT);
    return result;
  }

  /**
   * Step over an axis.
   * @param nodeSet
   * @param axis
   * @return
   * @throws XSLToolsXPathException
   */
  public static Set<UndeclaredNodeType> runAxis(
      Set<UndeclaredNodeType> nodeSet, short axis)
      throws XSLToolsXPathException {

    boolean hasMyType = false;
    boolean hasOtherType = false;

    Set<UndeclaredNodeType> newNodeSet;
    
    switch (axis) {

    case XPathAxisStep.CHILD:
    case XPathAxisStep.DESCENDANT:

      // Child axis will return e* if there is just an element in the set
      for (UndeclaredNodeType node : nodeSet) {
        if (node instanceof ElementNT) {
          hasMyType = true;
          break;
        }
      }
      // Child axis will return e* if there is just an element in the set, or root
      if (hasMyType || nodeSet.contains(RootNT.instance)) {
        newNodeSet = getEStarSingleton();
        if (hasMyType)
          // if it was an element, there is also text children 
          newNodeSet.add(TextNT.chameleonInstance);
        // in any case, there are comment and PI children
        newNodeSet.add(CommentNT.instance);
        newNodeSet.add(PINT.chameleonInstance);
        return newNodeSet;
      }
      // no element and no root .. bad luck
      return Collections.emptySet();

    case XPathAxisStep.PARENT:
    case XPathAxisStep.ANCESTOR:
      for (NodeType node : nodeSet) {
        if (node instanceof ElementNT || node instanceof CommentNT
            || node instanceof PINT) {
          hasMyType = true; // pot. element or root
        } else if (node instanceof AttributeNT || node instanceof TextNT) {
          hasOtherType = true; // pot. element
        }
      }
      if (hasMyType) {
        newNodeSet = getEStarSingleton();
        newNodeSet.add(RootNT.instance);
        return newNodeSet;
      } else if (hasOtherType)
        return getEStarSingleton();
      return Collections.emptySet();

    case XPathAxisStep.FOLLOWING:
    case XPathAxisStep.PRECEDING:
    case XPathAxisStep.FOLLOWING_SIBLING:
    case XPathAxisStep.PRECEDING_SIBLING:
      for (NodeType node : nodeSet) {
        if (node instanceof ElementNT) {
          hasMyType = true;
          break;
        }
        if (node == CommentNT.instance) {
          hasMyType = true;
          break;
        }
        if (node == PINT.chameleonInstance) {
          hasMyType = true;
          break;
        }
        if (node == TextNT.chameleonInstance) {
          hasMyType = true;
          break;
        }
      }
      if (hasMyType) {
        newNodeSet = getEStarSingleton();
        newNodeSet.add(CommentNT.instance);
        newNodeSet.add(PINT.chameleonInstance);
        newNodeSet.add(TextNT.chameleonInstance);
        if (axis == XPathAxisStep.PRECEDING)
          newNodeSet.add(RootNT.instance);
        return newNodeSet;
      }
      return Collections.emptySet();

    case XPathAxisStep.ATTRIBUTE:
      for (UndeclaredNodeType node : nodeSet) {
        if (node instanceof ElementNT) {
          hasMyType = true;
          break;
        }
      }
      if (hasMyType)
        return getAStarSingleton();
      return Collections.emptySet();

    case XPathAxisStep.SELF:
      return new HashSet<UndeclaredNodeType>(nodeSet);

    case XPathAxisStep.DESCENDANT_OR_SELF:
      // Combine self and descendant axes.
      newNodeSet = runAxis(nodeSet, XPathAxisStep.SELF);
      newNodeSet.addAll(runAxis(nodeSet, XPathAxisStep.DESCENDANT));
      return newNodeSet;

    case XPathAxisStep.ANCESTOR_OR_SELF:
      // Combine self and descendant axes.
      newNodeSet = runAxis(nodeSet, XPathAxisStep.SELF);
      newNodeSet.addAll(runAxis(nodeSet, XPathAxisStep.DESCENDANT));
      return newNodeSet;

    default:
      throw new XSLToolsXPathException("Unhandled axis " + axis);
    }
  }

  static Set<UndeclaredNodeType> fullyFedNodeSet() {
    Set<UndeclaredNodeType> nodeSet = new HashSet<UndeclaredNodeType>();
    // This is the FULL FEEDER. Everything goes in.
    // Relative path. Insert all nodes.
    // Insert root:
    nodeSet.add(RootNT.instance);

    // Insert PCDATA, comment and PI:
    nodeSet.add(TextNT.chameleonInstance);
    nodeSet.add(CommentNT.instance);
    nodeSet.add(PINT.chameleonInstance);

    nodeSet.add(NodeType.ONE_ANY_NAME_ELEMENT_NT);
    nodeSet.add(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT);

    return nodeSet;
  }

  /**
   * Do first the absolute/relative check, then continue.
   * @param path
   * @return
   * @throws XSLToolsXPathException
   */
  public Set<UndeclaredNodeType> possibleTargetNodes(XPathPathExpr path)
      throws XSLToolsXPathException {
    Set<UndeclaredNodeType> nodeSet = new HashSet<UndeclaredNodeType>();

    if (path instanceof XPathAbsolutePathExpr) {
      // Abolute path. Insert only the root node:
      nodeSet.add(RootNT.instance);
    } else {
      nodeSet = fullyFedNodeSet();
    }
    return possibleTargetNodes(path, nodeSet);
  }

  /**
   * Start out from onw type, not a set as usual
   * @param path
   * @param startType
   * @return
   * @throws XSLToolsXPathException
   */
  public static Set<UndeclaredNodeType> possibleTargetNodes(XPathPathExpr path,
      UndeclaredNodeType startType) throws XSLToolsXPathException {
    return possibleTargetNodes(path, Collections.singleton(startType));
  }

  /**
   * The normal way...
   * @param path
   * @param nodeSet
   * @return
   * @throws XSLToolsXPathException
   */
  public static Set<UndeclaredNodeType> possibleTargetNodes(XPathPathExpr path,
      Set<UndeclaredNodeType> nodeSet) throws XSLToolsXPathException {
    Iterator stepIter = path.steps();

    while (stepIter.hasNext() && !nodeSet.isEmpty()) {
      Object o = stepIter.next();
      if (!(o instanceof XPathAxisStep)) {
        System.err.println(RawTAGFlowGrapher.class.getSimpleName()
            + ": That is a funny type for a location step: " + o + " (class "
            + o.getClass() + "), we approximate to all types");
        // approximate, panic, we have no idea what it can be...
        nodeSet = fullyFedNodeSet();
      } else {
        XPathAxisStep step = (XPathAxisStep) o;
        short axis = step.getAxis();

        XPathNodeTest test = step.getNodeTest();

        // System.out.println("BEFORE: "+nodeSet);
        // Run axis to create initial new node list:
        Set<UndeclaredNodeType> newNodeSet = runAxis(nodeSet, axis);

        // System.out.println(" AXIS: "+newNodeSet);
        // Filter new node set by node test:
        if (axis == XPathAxisStep.ATTRIBUTE)
          newNodeSet = test.atest(newNodeSet);
        else
          newNodeSet = test.etest(newNodeSet);
        // System.out.println(" TEST: "+newNodeSet);
        // Replace old node list with new:
        nodeSet = newNodeSet;
      }
    }
    return nodeSet;
  }

  /**
   * If one contains e* and the other some element, return true.
   * @param s1
   * @param s2
   * @return
   */
  private static boolean smallShotgunE(Set<? extends NodeType> s1,
      Set<? extends NodeType> s2) {
    if (s1.contains(NodeType.ONE_ANY_NAME_ELEMENT_NT)) {
      for (NodeType nt : s2) {
        if (nt instanceof ElementNT)
          return true;
      }
    }
    return false;
  }

  /**
   * If one contains e* and the other some element, or opposite, return true.
   * @param s1
   * @param s2
   * @return
   */
  private static boolean shotgunE(Set<? extends NodeType> s1,
      Set<? extends NodeType> s2) {
    return smallShotgunE(s1, s2) || smallShotgunE(s2, s1);
  }

  /**
   * If one contains a* and the other some attribute, return true.
   * @param s1
   * @param s2
   * @return
   */
  private static boolean smallShotgunA(Set<? extends NodeType> s1,
      Set<? extends NodeType> s2) {
    if (s1.contains(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT)) {
      for (NodeType nt : s2) {
        if (nt instanceof AttributeNT)
          return true;
      }
    }
    return false;
  }

  /**
   * If one contains a* and the other some attribute, or opposite, return true.
   * @param s1
   * @param s2
   * @return
   */
  private static boolean shotgunA(Set<? extends NodeType> s1,
      Set<? extends NodeType> s2) {
    return smallShotgunA(s1, s2) || smallShotgunA(s2, s1);
  }

  /*
   * public static boolean nonemptyElementIntersection(Set<ElementDecl> s1, Set<?
   * extends UndeclaredNodeType> s2) { if
   * (s2.contains(NodeType.ANY_NAME_ELEMENT_NT)) return true; for (ElementDecl t :
   * s1) { if (s2.contains(new ElementNT(t))) return true; } return false; }
   * 
   * public static boolean nonemptyAttributeIntersection(Set<AttributeDecl> s1,
   * Set<? extends UndeclaredNodeType> s2) { if
   * (s2.contains(NodeType.ANY_NAME_ATTRIBUTE_NT)) return true; for
   * (AttributeDecl t : s1) { if (s2.contains(new AttributeNT(t))) return true; }
   * return false; }
   */

  /*
   * Returns whether the intersection of two node sets is nonempty, taking the
   * *:* element and attribute node types into consideration.
   */
  public static boolean nonemptyIntersection(Set<? extends NodeType> s1,
      Set<? extends NodeType> s2) {
    if (shotgunE(s1, s2))
      return true;
    if (shotgunA(s1, s2))
      return true;
    Set<NodeType> clone = new HashSet<NodeType>(s1);
    clone.retainAll(s2);
    return !clone.isEmpty();
  }

  /*
   * Intersects two node sets.
   */
  public static Set<UndeclaredNodeType> intersection(
      Set<UndeclaredNodeType> s1, Set<UndeclaredNodeType> s2) {

    // plain ol' intersection.
    Set<UndeclaredNodeType> result = new HashSet<UndeclaredNodeType>(s1);
    result.retainAll(s2);

    if (result.contains(NodeType.ONE_ANY_NAME_ELEMENT_NT)) {
      // if e* is there, purge all element types from result, except e*.
      for (Iterator<UndeclaredNodeType> iter = result.iterator(); iter
          .hasNext();) {
        NodeType nt = iter.next();
        if (nt instanceof ElementNT) {
          ElementNT ent = (ElementNT) nt;
          if (!ent.getQName().equals(NodeType.ANY_NAME))
            iter.remove();
        }
      }
    } else {
      if (s1.contains(NodeType.ONE_ANY_NAME_ELEMENT_NT)) {
        // if e* is in s1 only, make up for the missing addition of non-e* elements from s2
        for (UndeclaredNodeType nt : s2) {
          if (nt instanceof ElementNT)
            result.add(nt);
        }
      } else if (s2.contains(NodeType.ONE_ANY_NAME_ELEMENT_NT)) {
        // if e* is in s2 only, make up for the missing addition of non-e* elements from s1
        for (UndeclaredNodeType nt : s1) {
          if (nt instanceof ElementNT)
            result.add(nt);
        }
      }
    }

    /*
     * Same story for attributes.
     */
    if (result.contains(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT)) {
      // purge all attribute types from result
      for (Iterator<UndeclaredNodeType> iter = result.iterator(); iter
          .hasNext();) {
        NodeType nt = iter.next();
        if (nt instanceof AttributeNT
            && !(nt.equals(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT)))
          iter.remove();
      }
    } else {
      if (s1.contains(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT)) {
        for (UndeclaredNodeType nt : s2) {
          if (nt instanceof AttributeNT)
            result.add(nt);
        }
      } else if (s2.contains(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT)) {
        for (UndeclaredNodeType nt : s1) {
          if (nt instanceof AttributeNT)
            result.add(nt);
        }
      }
    }
    return result;
  }

  /*
   * The main test:
   * @see dongfang.xsltools.controlflow.AbstractContextInsensitiveFlowGrapher#flowSurvivors(dongfang.xsltools.xpath2.XPathPathExpr, dongfang.xsltools.controlflow.TemplateRule, dongfang.xsltools.context.ValidationContext)
   */
  @Override
Set<UndeclaredNodeType> flowSurvivors(XPathPathExpr path1,
      TemplateRule target, ValidationContext context)
      throws XSLToolsXPathException {
    
    Set<UndeclaredNodeType> s1 = possibleTargetNodes(path1);
    Set<UndeclaredNodeType> s2 = possibleTargetNodes(target.match);

    Set<UndeclaredNodeType> i = intersection(s1, s2);

    return i;
  }

  int _priorityOverrideTest(TemplateRule e1Target,
      Set<? extends NodeType> e1EdgeFlow, TemplateRule e2Target,
      Set<? extends NodeType> e2EdgeFlow, ValidationContext context) {
    return 0;
  }

  @Override
int priorityOverrideTest(TemplateRule e1Target,
      Set<? extends NodeType> e1EdgeFlow, TemplateRule e2Target,
      Set<? extends NodeType> e2EdgeFlow, ValidationContext context) {

    // System.err.println("Template 1: i " + e1Target.module.importPrecedence +
    // " p " + e1Target.priority + " mn " + e1Target.module.getURI());
    // System.err.println("Edgeflow 1: " + e1EdgeFlow);

    // System.err.println("Template 2: i " + e2Target.module.importPrecedence +
    // " p "+ e2Target.priority + " mn " + e2Target.module.getURI());
    // System.err.println("Edgeflow 2: " + e2EdgeFlow);

    int override = e1Target.compareTo(e2Target);

    if (override > 0 && !e1Target.match.hasPredicates()) {
      // Early bail if no context nodes in common:
      // System.err.println("#1 beat #2");
      if (!nonemptyIntersection(e1EdgeFlow, e2EdgeFlow)) {
        // System.err.println("but flow intersection is empty\n");
        return TemplateRule.NO_OVERRIDE;
      }

      // panic if only the challenger is abs.
      if (e1Target.match instanceof XPathAbsolutePathExpr
          && !(e2Target.match instanceof XPathAbsolutePathExpr))
        return TemplateRule.NO_OVERRIDE;

      Iterator<XPathStepExpr> st1 = e1Target.match.reverseSteps();
      Iterator<XPathStepExpr> st2 = e2Target.match.reverseSteps();

      while (st1.hasNext() && st2.hasNext()) {
        XPathStepExpr e1 = st1.next();
        XPathStepExpr e2 = st2.next();

        if (!(e1 instanceof XPathAxisStep)) {
          throw new AssertionError(
              "non axis step");
        }

        if (!(e2 instanceof XPathAxisStep)) {
          throw new AssertionError(
              "non axis step");
        }

        XPathAxisStep s1 = (XPathAxisStep) e1;
        XPathAxisStep s2 = (XPathAxisStep) e2;

        // we only venture into this for the child axis, element types...
        if (s1.getAxis() == XPathAxisStep.CHILD) {

          if (s2.getAxis() != XPathAxisStep.CHILD)
            return TemplateRule.NO_OVERRIDE;

          Set<UndeclaredNodeType> allChildren1 = new HashSet<UndeclaredNodeType>();
          allChildren1.add(NodeType.ONE_ANY_NAME_ELEMENT_NT);
          allChildren1.add(CommentNT.instance);
          allChildren1.add(PINT.chameleonInstance);
          allChildren1.add(TextNT.chameleonInstance);

          Set<UndeclaredNodeType> passedOld = s2.getNodeTest().etest(
              allChildren1);
          Set<UndeclaredNodeType> passedChallenger = s1.getNodeTest().etest(
              allChildren1);

          // avoid aliasing problems if any -- and those darn Singletons, too.
          passedOld = new HashSet<UndeclaredNodeType>(passedOld);

          passedOld.removeAll(passedChallenger);

          if (!passedOld.isEmpty())
            return TemplateRule.NO_OVERRIDE;
        } else if (s1.getAxis() == XPathAxisStep.ATTRIBUTE) {

          if (s2.getAxis() != XPathAxisStep.ATTRIBUTE)
            return TemplateRule.NO_OVERRIDE;

          Set<UndeclaredNodeType> allAttributes1 = new HashSet<UndeclaredNodeType>();
          allAttributes1.add(NodeType.ONE_ANY_NAME_ATTRIBUTE_NT);

          Set<UndeclaredNodeType> passedOld = s2.getNodeTest().atest(
              allAttributes1);
          Set<UndeclaredNodeType> passedChallenger = s1.getNodeTest().atest(
              allAttributes1);

          // avoid aliasing problems if any -- and those darn Singletons, too.
          passedOld = new HashSet<UndeclaredNodeType>(passedOld);

          passedOld.removeAll(passedChallenger);

          if (!passedOld.isEmpty())
            return TemplateRule.NO_OVERRIDE;
        } else {
          return TemplateRule.NO_OVERRIDE;
        }
      }

      if (st1.hasNext()) // has more ancestor details; we can't use it.
        return TemplateRule.NO_OVERRIDE;

      return override;
    }
    return TemplateRule.NO_OVERRIDE;
  }
}
