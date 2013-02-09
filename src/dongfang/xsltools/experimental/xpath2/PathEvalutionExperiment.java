package dongfang.xsltools.experimental.xpath2;

import java.io.Reader;
import java.io.StringReader;

import dongfang.xsltools.model.ElementNamespaceExpander;
import dongfang.xsltools.xpath2.Node;
import dongfang.xsltools.xpath2.XPathParser;
import dongfang.xsltools.xpath2.XPathPathExpr;

public class PathEvalutionExperiment {
  void test() throws Exception {
    // String exp = "-27"; FAILER FIXED.
    // String exp = "/foo:foo"; FAILER FIXED (men funktioner er vaerre end
    // nogensinde)
    // String exp = "s()"; FAILER
    // DONE: Attributter ud af SELF aksen! (hmm?)
    // String exp = "//foo/btr//parent::ddd/self::node()/.";
    // String exp = "*:bar"; // FAILER // FIXED
    // ISSUE omkring forskellige farver context item
    // String exp = "."; // FAILER som axisstep ... ikke godt // FIXED men paa
    // bekostn. af den andne mulighed-. OK konflikt løst trick 87a
    // String exp = "1 treat as empty-sequence()"; // FAILER mangler repr. //
    // FIXED.
    // String exp = "bar[1]/foo:bar[2]/*:baz[3]/foo:*[4]/*[5]";
    // String exp = "string(self::node())";
    // String exp = "foo(attribute::shref | attribute::href, '#')";

    String exp = "$allow-anchors != 0";

    Reader r = new StringReader(exp);

    XPathParser xpp = new XPathParser(r);
    xpp.setNsExpander(new ElementNamespaceExpander());
    xpp.XPath();
    Node n = (Node) xpp.getParseTree();
    System.out.println(n);

    assert (n instanceof XPathPathExpr);

    // XPathExpr pe = (XPathExpr) n;

    // RawTAGFlowGrapher rtg = RawTAGFlowGrapher.getInstance();

    // System.out.println(RawTAGFlowGrapher.possibleTargetNodes(pe));
  }

  public static void main(String[] args) throws Exception {
    new PathEvalutionExperiment().test();
  }
}
