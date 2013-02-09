/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.experimental.general_programming;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author dongfang
 */

public class Permute {

  public interface Callback {
    void callback(Object[] os);
  }

  static class printer implements Callback {
    public void callback(Object[] os) {
      for (int i = 0; i < os.length; i++)
        System.out.print(os[i] + " ");
      System.out.println();
    }
  }

  static void perm(Object[] os, Set<Integer> livePoss, Callback callback) {
    if (livePoss.isEmpty())
      callback.callback(os);
    else {
      int somePlace = livePoss.iterator().next().intValue();
      for (Iterator<Integer> it = livePoss.iterator(); it.hasNext();) {
        Integer Num = it.next();
        int num = Num.intValue();
        Object temp = os[num];
        os[num] = os[somePlace];
        os[somePlace] = temp;
        Set<Integer> clown = new HashSet<Integer>(livePoss);
        clown.remove(Num);
        perm(os, clown, callback);
        temp = os[num];
        os[num] = os[somePlace];
        os[somePlace] = temp;
      }
    }
  }

  public static void perm(Object[] os, Callback callback) {
    Set<Integer> livePoss = new HashSet<Integer>();
    for (int i = 0; i < os.length; i++) {
      Integer I = new Integer(i);
      livePoss.add(I);
    }
    perm(os, livePoss, callback);
  }

  public static void main(String[] args) {
    Object[] test = { "a", "b", "c", "d" };
    perm(test, new printer());
  }
}
