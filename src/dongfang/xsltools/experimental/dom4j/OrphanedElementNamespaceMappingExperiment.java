/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-18
 */
package dongfang.xsltools.experimental.dom4j;

import java.io.StringReader;
import java.util.List;
import java.util.ListIterator;

import org.dom4j.Attribute;
import org.dom4j.CDATA;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentType;
import org.dom4j.Element;
import org.dom4j.Entity;
import org.dom4j.Namespace;
import org.dom4j.ProcessingInstruction;
import org.dom4j.Text;
import org.dom4j.Visitor;
import org.dom4j.io.SAXReader;

/**
 * @author snk
 */
public class OrphanedElementNamespaceMappingExperiment {
  public static void main(String[] args) throws DocumentException {
    String testDocument = "<?xml version='1.0'?>" + "<foo "
        + "xmlns:gedefims='http://dongfang.dk/2' " + "x='1' gedefims:y='2'>" + // an
        // attribute
        // w o
        // even
        // default
        // NS
        // declared, and one in an explicit
        // NS
        "<bar x='3' gedefims:y='4'/>" + // a nested element
        "<bar xmlns='http://dongfang.dk/3' x='5' gedefims:y='5'/>"
        + "<gedefims:bar x='5' gedefims:y='5'/>" + "</foo>";

    SAXReader sr = new SAXReader();
    Document d = sr.read(new StringReader(testDocument));

    d.accept(new VerboseVisitor());

    List tjakk = d.selectNodes("foo/*");

    for (ListIterator it = tjakk.listIterator(); it.hasNext();) {
      System.out.println("---");
      Element e = (Element) it.next();
      e.detach();
      e.accept(new VerboseVisitor());
    }
  }

  static class VerboseVisitor implements Visitor {

    /*
     * (non-Javadoc)
     * 
     * @see org.dom4j.Visitor#visit(org.dom4j.Attribute)
     */
    public void visit(Attribute node) {
      System.out.println("Attribute");
      System.out.println("	QName: " + node.getQualifiedName());
      System.out.println("	NS Prefix: " + node.getNamespacePrefix());
      System.out.println("	NS URI: " + node.getNamespaceURI());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dom4j.Visitor#visit(org.dom4j.CDATA)
     */
    public void visit(CDATA node) {
      System.out.println("cdata");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dom4j.Visitor#visit(org.dom4j.Comment)
     */
    public void visit(Comment node) {
      System.out.println("comment");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dom4j.Visitor#visit(org.dom4j.Document)
     */
    public void visit(Document document) {
      System.out.println("document");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dom4j.Visitor#visit(org.dom4j.DocumentType)
     */
    public void visit(DocumentType documentType) {
      System.out.println("doctype");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dom4j.Visitor#visit(org.dom4j.Element)
     */
    public void visit(Element node) {
      System.out.println("element");
      System.out.println("  QName: " + node.getQualifiedName());
      System.out.println("  NS prefix: " + node.getNamespacePrefix());
      System.out.println("  NS URI" + node.getNamespaceURI());

      // Iterator it = node.attributeIterator();

      /*
       * while (it.hasNext()) { Attribute a = (Attribute) it.next();
       * System.out.println(" QName: " + a.getQualifiedName());
       * System.out.println(" NS Prefix: " + a.getNamespacePrefix());
       * System.out.println(" NS URI: " + a.getNamespaceURI()); }
       */
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dom4j.Visitor#visit(org.dom4j.Entity)
     */
    public void visit(Entity node) {
      System.out.println("Entity");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dom4j.Visitor#visit(org.dom4j.Namespace)
     */
    public void visit(Namespace namespace) {
      System.out.println("Namespace");
      System.out.println("  Name: " + namespace.getName());
      System.out.println("  TS: " + namespace.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dom4j.Visitor#visit(org.dom4j.ProcessingInstruction)
     */
    public void visit(ProcessingInstruction node) {
      System.out.println("P-I");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dom4j.Visitor#visit(org.dom4j.Text)
     */
    public void visit(Text node) {
      System.out.println("Text");
    }
  }
}