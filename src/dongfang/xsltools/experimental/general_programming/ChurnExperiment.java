/*
package dongfang.xsltools.experimental.general_programming;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import dongfang.xsltools.util.BitSet;
import dongfang.xsltools.util.BitSetStructure;

public class ChurnExperiment {

  interface SetFactory<T> {
    Set<T> makeSet();
  }

  public static <T> void churnGen(SetFactory<T> fac, Set<T> references) {
    Set<T> ag = fac.makeSet();
    // just add all
    // for (T t : references)
    // if (Math.random() > 0.2)
    // ag.add(t);

    ag.addAll(references);

    for (Iterator<T> test = ag.iterator(); test.hasNext();) {
      T t = test.next();
      if (Math.random() > 0.2)
        test.remove();
    }
  }

  static void churn(SetFactory<String> fac) {
    Set<String> s = fac.makeSet();
    s.add("a");
    s.add("b");
    s.add("c");
    s.add("d");
    s.add("e");
    s.add("f");
    s.add("g");
    s.add("h");
    s.add("i");
    s.add("j");
    s.add("k");
    s.add("l");
    s.add("m");
    s.add("n");
    s.add("o");
    s.add("p");

    for (int i = 0; i < 10000000; i++) {
      churnGen(fac, s);
    }
  }

  public static void main(String[] args) {

    SetFactory<String> bit = new SetFactory<String>() {
      BitSet<String> ref = new BitSet(new BitSetStructure<String>());

      public Set<String> makeSet() {
        return ref.newInstance();
      }
    };

    SetFactory<String> hark = new SetFactory<String>() {
      public Set<String> makeSet() {
        return new HashSet<String>();
      }
    };

    long time = System.currentTimeMillis();

    churn(bit);

    long t1 = System.currentTimeMillis();

    churn(hark);

    long t2 = System.currentTimeMillis();

    System.out.println("bit: " + (t1 - time));
    System.out.println("hark: " + (t2 - t1));
  }
}
*/