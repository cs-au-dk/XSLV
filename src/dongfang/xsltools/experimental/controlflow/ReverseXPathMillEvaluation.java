package dongfang.xsltools.experimental.controlflow;

import java.io.PrintWriter;

import org.xml.sax.InputSource;

import dk.brics.xmlgraph.XMLGraph;
import dk.brics.xmlgraph.converter.XMLGraph2Dot;
import dongfang.xsltools.controlflow.SGFragment;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.resolver.URLResolutionContext;
import dongfang.xsltools.util.Util;
import dongfang.xsltools.xmlclass.dtd.DTD;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.ElementUse;
import dongfang.xsltools.xpath2.XPathParser;
import dongfang.xsltools.xpath2.XPathPathExpr;

public class ReverseXPathMillEvaluation {
  public static void main(String[] args) throws Exception {
    String dtdId = "experimental-data/simple.dtd";

    if (!Util.isURL(dtdId))
      dtdId = Util.toUrlString(dtdId);

    ResolutionContext ctx = new URLResolutionContext();
    InputSource dtdSource = ctx.resolveStream(dtdId, "fims", (short) 0);
    DTD dtd = new DTD(dtdSource, null, null, ResolutionContext.INPUT); // will crash but do we ever need it
    // again?

    XMLGraph g = new XMLGraph();

    // SGFragment fraggle =
    // dtd.getDocumentElementDecl().constructSGFragment(dtd, g);

    SGFragment fraggle1 = new SGFragment(g, null);

    XPathPathExpr p = (XPathPathExpr) XPathParser.parse("b/b/parent::b");

    SGFragment fraggle = dtd.constructSGFragment(fraggle1, p, (ElementUse) dtd
        .getDocumentElementDecl(), null, ContentOrder.FORWARD, true);

    g.addRoot(fraggle.getEntryNode());

    XMLGraph2Dot dotter = new XMLGraph2Dot(new PrintWriter(System.out));

    dotter.print(g);
  }
}
