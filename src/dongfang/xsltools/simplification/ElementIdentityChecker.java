/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.simplification;

import java.util.HashSet;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.VisitorSupport;

import dongfang.XMLConstants;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetLevel;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * Check that every element has a unique element ID (eid) and core element ID
 * (cid). Neither of the IDs are strictly necessary for the validation, but they
 * are used for mapping messages about issues in the simplified document back to
 * the original document. Core IDs are used as XPath cache keys as well.
 * 
 * @author dongfang
 */
public class ElementIdentityChecker implements StylesheetProcessor {
  boolean verbose;

  String after;

  ElementIdentityChecker(String after) {
    this.after = after;
  }

  class CheckerVisitor extends VisitorSupport {

    StylesheetModule module;

    int version;

    Set<String> eids = new HashSet<String>();

    Set<String> cids = new HashSet<String>();

    CheckerVisitor(StylesheetModule module, int version) {
      this.module = module;
      this.version = version;
    }

    @Override
	public void visit(Element element) {
      // Attribute at = element.attribute(XMLConstants.ELEMENT_CORE_ID_QNAME);
      // if (at!=null)
      // throw new RuntimeException("Already had core id! " +
      // element);

      String eid = element.attributeValue(XMLConstants.ELEMENT_ID_QNAME);
      String cid = element.attributeValue(XMLConstants.ELEMENT_CORE_ID_QNAME);

      if (version == StylesheetModule.ORIGINAL) {

        if (cid != null)
          throw new AssertionError(
              "cid ended up in original document -- funny: " + element);

        if (eid == null) {
          throw new AssertionError(
              "Element in original stylesheet did not have eid: " + element);
        }
        {
          if (eids.contains(eid)) {
            System.err
                .println("ACHTUNG: Saw this eid duplicated in original document: "
                    + eid + " in module: " + module + " after " + after);
          } else {
            eids.add(eid);
          }
        }
      }
      // if (version==StylesheetModule.SIMPLIFIED) {
      else {
        if (cid == null) {
          throw new AssertionError(
              "Element in simplified stylesheet did not have cid: " + element);
        } /* else */
        {
          if (cids.contains(cid))
            throw new AssertionError(
                "Saw this cid duplicated in simplified document: " + cid);
          // else
          cids.add(cid);
        }
      }
    }
  }

  static ElementIdentityChecker getInstance(ErrorReporter cesspool,
      UniqueNameGenerator names) {
    return new ElementIdentityChecker();
  }

  public ElementIdentityChecker() {
  }

  public void check(final StylesheetModule module, int version) {
    Document doc = module.getDocument(version);

    if (verbose) {
      System.out
          .println("Now testing: "
              + module
              + " version "
              + (version == StylesheetModule.CORE ? "SIMPLIFIED"
                  : "ORIGINAL"));
      Dom4jUtil.prettyPrint(doc);
    }

    doc.accept(new CheckerVisitor(module, version));
  }

  public void check(final StylesheetModule module) {
    check(module, StylesheetModule.ORIGINAL);
    check(module, StylesheetModule.CORE);
  }

  public void process(StylesheetLevel level) {
    for (StylesheetModule module : level.contents()) {
      check(module);
    }
    for (StylesheetLevel imported : level.imports()) {
      process(imported);
    }
  }

  public void process(Stylesheet stylesheet) {
    process(stylesheet.getPrincipalLevel());
    check(stylesheet);
  }
}
