/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-20
 */
package dongfang.xsltools.controlflow;

import java.util.HashSet;
import java.util.Set;

import dongfang.xsltools.exceptions.XSLToolsXPathException;
import dongfang.xsltools.xmlclass.xslside.RootNT;
import dongfang.xsltools.xmlclass.xslside.UndeclaredNodeType;
import dongfang.xsltools.xpath2.XPathAbsolutePathExpr;
import dongfang.xsltools.xpath2.XPathAxisStep;
import dongfang.xsltools.xpath2.XPathExpr;
import dongfang.xsltools.xpath2.XPathNodeTest;
import dongfang.xsltools.xpath2.XPathPathExpr;

/**
 * @author dongfang
 */
public class SimpleRawTAGFlowGrapher extends RawTAGFlowGrapher {

  private static SimpleRawTAGFlowGrapher instance = new SimpleRawTAGFlowGrapher();

  public static SimpleRawTAGFlowGrapher getInstance() {
    return instance;
  }

  @Override
  public Set<UndeclaredNodeType> possibleTargetNodes(XPathPathExpr path)
      throws XSLToolsXPathException {
    if (path instanceof XPathAbsolutePathExpr && path.getStepCount() == 0) {
      Set<UndeclaredNodeType> r = new HashSet<UndeclaredNodeType>();
      r.add(RootNT.instance);
      return r;
    }

    Set<UndeclaredNodeType> nodeSet = fullyFedNodeSet();

    XPathExpr s1 = path.getLastStep();

    Set<UndeclaredNodeType> newNodeSet = null;

    XPathAxisStep step = (XPathAxisStep) s1;
    short axis = step.getAxis();

    XPathNodeTest test = step.getNodeTest();

    // System.out.println("BEFORE: "+nodeSet);
    // Run axis to create initial new node list:
    newNodeSet = runAxis(nodeSet, axis);

    // System.out.println(" AXIS: "+newNodeSet);
    // Filter new node set by node test:
    if (axis == XPathAxisStep.ATTRIBUTE)
      newNodeSet = test.atest(newNodeSet);
    else
      newNodeSet = test.etest(newNodeSet);
    // System.out.println(" TEST: "+newNodeSet);
    // Replace old node list with new:
    return newNodeSet;
  }
}
