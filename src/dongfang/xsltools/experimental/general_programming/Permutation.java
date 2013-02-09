package dongfang.xsltools.experimental.general_programming;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import dk.brics.misc.Origin;

public class Permutation {
  public String createPermutationMonster(List<String> somePermutation,
      Origin origin) {
    List<String> result = new LinkedList<String>();
    createPermutationMonster(new LinkedList<String>(), somePermutation, result,
        somePermutation.size());
    if (result.size() == 1)
      return result.toString();
    return "choice(" + result.toString() + ")";
  }

  void createPermutationMonster(LinkedList<String> settled,
      List<String> unsettled, Collection<String> result, int nsettled) {
    if (settled.size() == nsettled)
      result.add(settled.toString());
    for (int i = 0; i < unsettled.size(); i++) {
      String I = unsettled.remove(i);
      settled.add(I);
      createPermutationMonster(settled, unsettled, result, nsettled);
      unsettled.add(i, I);
      settled.removeLast();
    }
  }

  public static void main(String[] args) {
    LinkedList<String> sl = new LinkedList<String>();
    sl.add("a");
    sl.add("b");
    sl.add("c");
    sl.add("d");
    String s = new Permutation().createPermutationMonster(sl, null);
    System.out.println(s);
  }
}
