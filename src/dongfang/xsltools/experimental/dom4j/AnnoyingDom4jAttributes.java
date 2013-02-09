/*
 * dongfang M. Sc. Thesis
 * Created on 2005-02-27
 */
package dongfang.xsltools.experimental.dom4j;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @author dongfang
 */
public class AnnoyingDom4jAttributes {
  public static void main(String[] args) throws Exception {
    String filename = "experimental-data/AnnoyingAttributes.xml";// /args[0];
    SAXReader builder = new SAXReader();

    // builder.setFeature("http://xml.org/sax/features/namespaces", true);
    // builder.setFeature("http://xml.org/sax/features/namespace-prefixes",
    // false);

    Document doc = builder.read(filename);

    Element root = doc.getRootElement();

    verbose(root);

    for (Iterator children = root.elementIterator(); children.hasNext();) {
      verbose((Element) children.next());
    }
  }

  private static void verbose(Element e) {
    System.out.println("Naive name: " + e.getName());
    System.out.println("qual name: " + e.getQualifiedName());
    System.out.println("namespace: " + e.getNamespace());
    List attrs = e.attributes();
    for (int i = 0; i < attrs.size(); i++) {
      System.out.println("Att #" + i + ":");
      Attribute a = (Attribute) attrs.get(i);
      System.out.println("  Naive name: " + a.getName());
      System.out.println("  qual name: " + a.getQualifiedName());
      System.out.println("  namespace: " + a.getNamespace());
      System.out.println("  value: " + a.getValue());
      System.out.println("<<owner-element>>.getAttribute(" + a.getName() + ")"
          + " == this: " + (e.attribute(a.getName()) == a));
      System.out.println("<<owner-element>>.getAttribute(" + a.getQName() + ")"
          + " == this: " + (e.attribute(a.getQName()) == a));
    }
    System.out.println();
  }
}