/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-23
 */
package dongfang.xsltools.experimental.general_programming;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author snk
 */
public class ListIteratorExperiment {
  public static void main(String[] args) {
    List<Integer> l = new LinkedList<Integer>();
    l.add(new Integer(10));
    l.add(new Integer(20));
    l.add(new Integer(30));
    l.add(new Integer(40));

    ListIterator<Integer> iter = l.listIterator();

    while (iter.hasNext()) {
      Integer I = iter.next();
      int i = I.intValue();
      if ((i % 10) == 0)
        iter.add(new Integer(i + 1));
      else
        System.out.println("Skipped: " + i);
    }
    System.out.println(l);
  }
}
