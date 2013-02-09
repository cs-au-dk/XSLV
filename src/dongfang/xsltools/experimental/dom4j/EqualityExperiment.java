/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.experimental.dom4j;

import org.dom4j.Namespace;
import org.dom4j.QName;

/**
 * @author dongfang
 */
public class EqualityExperiment {
  public static void main(String[] args) {
    Namespace n1 = Namespace.get("abc", "dongfang");
    Namespace n2 = Namespace.get("xyz", "dongfang");

    System.out.println(n1.equals(n2));

    QName q1 = QName.get("q", n1);
    QName q2 = QName.get("q", n2);

    System.out.println(q1.equals(q2));
  }
}