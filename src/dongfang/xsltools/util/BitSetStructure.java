package dongfang.xsltools.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BitSetStructure<T> {
  private List<T> l = new ArrayList<T>();

  private Map<T, Integer> m = new HashMap<T, Integer>();

  // autostruct ---
  final int autoStructindexOf(T t) {
    Integer I = m.get(t);
    if (I != null)
      return I;
    l.add(t);
    m.put(t, l.size() - 1);
    return l.size() - 1;
  }

  final int indexOf(Object o) {
    Integer I = m.get(o);
    if (I != null)
      return I;
    return -1;
  }

  final int size() {
    return l.size();
  }

  final T getAt(int pos) {
    return l.get(pos);
  }
}
