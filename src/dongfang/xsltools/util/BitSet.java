/*
package dongfang.xsltools.util;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class BitSet<T> implements Set<T> {
  private BitSetStructure<T> struc;

  private static final int ullog = 5;

  private static final int ul = 1 << ullog;

  private int[] data;

  public BitSet(BitSetStructure<T> struc) {
    this.struc = struc;
    this.data = new int[(struc.size() + ul / *- 1* /) >> ullog];
  }

  private final int makeMask(int pos) {
    pos = pos % ul;
    return 1 << pos;
  }

  private final boolean set(int pos) {
    int unit = pos >> ullog;
    int mask = makeMask(pos);
    int value = data[unit];
    int newvalue = value | mask;
    data[unit] = newvalue;
    return value != newvalue;
  }

  private final void checkSize(int newPos) {
    if (data.length << ullog <= newPos) {
      throw new UnsupportedOperationException("Autogrow not impl.");
    }
  }

  public boolean add(T o) {
    int pos = struc.autoStructindexOf(o);
    checkSize(pos);
    return set(pos);
  }

  public boolean addAll(Collection<? extends T> c) {
    boolean changed = false;
    if (c instanceof BitSet) {
      BitSet bt = (BitSet) c;
      checkSize(bt.size());
      bt.checkSize(size());
      if (bt.struc != struc)
        throw new IllegalArgumentException(
            "BitSets MUST have same backing struct");
      for (int i = 0; i < data.length; i++) {
        // TODO: If lazy extension used, a length check here.
        int j = data[i];
        data[i] |= bt.data[i];
        changed |= (data[i] != j);
      }
      return changed;
    }
    for (T t : c) {
      changed |= (add(t));
    }
    return changed;
  }

  public void clear() {
    for (int i = 0; i < data.length; i++)
      data[i] = 0;
  }

  public boolean contains(Object o) {
    int pos = struc.indexOf(o);
    if (pos == -1)
      return false;
    if (pos >= data.length << ullog)
      return false;
    int unit = pos >> ullog;
    int mask = makeMask(pos);
    return (data[unit] & mask) != 0;
  }

  public boolean containsAll(Collection<?> c) {
    if (c instanceof BitSet) {
      BitSet bt = (BitSet) c;
      checkSize(bt.size());
      bt.checkSize(size());
      if (bt.struc != struc)
        throw new IllegalArgumentException(
            "BitSets MUST have same backing struct");
      for (int i = 0; i < data.length; i++) {
        int j = bt.data[i];
        j &= ~data[i]; // kill all bits that we have
        if (j != 0)
          return false;
      }
      return true;
    }

    boolean has = true;

    for (Iterator it = c.iterator(); it.hasNext() && has;) {
      Object o = it.next();
      has &= (contains(o));
    }
    return has;
  }

  public boolean isEmpty() {
    for (int i = 0; i < data.length; i++) {
      if (data[i] != 0)
        return false;
    }
    return true;
  }

  public Iterator<T> iterator() {
    return new Iterator<T>() {
      int nextpos = -1;

      int lastpos;

      int mask = 1;

      int skipmask;

      int lastmask;

      int unit = 0;

      private Iterator<T> advance() {
        while (true) {
          if ((nextpos % ul) == 0) {
            mask = 1;
            skipmask = 0;
            unit++;
            if (unit >= data.length) {
              nextpos = -2;
              return this;
            }
          }
          if ((data[unit] & ~skipmask) == 0)
            nextpos = (nextpos + ul - 1) % ul;
          nextpos++;
          skipmask |= mask;
          if ((data[unit] & mask) != 0)
            return this;
          mask <<= 1;
        }
      }

      public boolean hasNext() {
        return nextpos != -2;
      }

      public T next() {
        lastpos = nextpos;
        lastmask = mask;
        advance();
        return struc.getAt(lastpos);
      }

      public void remove() {
        int unit = lastpos >>> ullog;
        data[unit] &= ~lastmask;
      }

    }.advance();
  }

  public boolean remove(Object o) {
    int pos = struc.indexOf(o);
    if (pos == -1)
      return false;
    int unit = pos >>> ullog;
    int mask = makeMask(pos);
    int before = data[unit];
    data[unit] &= ~mask;
    return data[unit] != before;
  }

  public boolean removeAll(Collection<?> c) {
    boolean changed = false;
    if (c instanceof BitSet) {
      BitSet bt = (BitSet) c;
      checkSize(bt.size());
      bt.checkSize(size());
      if (bt.struc != struc)
        throw new IllegalArgumentException(
            "BitSets MUST have same backing struct");
      for (int i = 0; i < data.length; i++) {
        // TODO: If lazy extension used, a length check here.
        int j = data[i];
        data[i] &= ~bt.data[i];
        changed |= (data[i] != j);
      }
      return changed;
    }
    for (Object o : c) {
      changed |= (remove(o));
    }
    return changed;
  }

  public boolean retainAll(Collection<?> c) {
    boolean changed = false;
    if (c instanceof BitSet) {
      BitSet bt = (BitSet) c;
      checkSize(bt.size());
      bt.checkSize(size());
      if (bt.struc != struc)
        throw new IllegalArgumentException(
            "BitSets MUST have same backing struct");
      for (int i = 0; i < data.length; i++) {
        // TODO: If lazy extension used, a length check here.
        int j = data[i];
        data[i] &= bt.data[i];
        changed |= (data[i] != j);
      }
      return changed;
    }

    for (T t : this) {
      if (!c.contains(t)) {
        changed |= (remove(t));
      }
    }
    return changed;
  }

  public int size() {
    int size = 0;
    for (int i = 0; i < data.length; i++) {
      int j = data[i];
      int m = 1;
      for (int k = 0; k < ul; k++) {
        if ((j & m) != 0)
          size++;
        m <<= 1;
      }
    }
    return size;
  }

  public Object[] toArray() {
    // TODO Auto-generated method stub
    return null;
  }

  public <T> T[] toArray(T[] a) {
    // TODO Auto-generated method stub
    return null;
  }

  public BitSet<T> newInstance() {
    return new BitSet<T>(struc);
  }

  public Object clone() {
    BitSet clown = newInstance();
    clown.data = new int[data.length];
    System.arraycopy(data, 0, clown.data, 0, data.length);
    return clown;
  }

  public String toString() {
    boolean needsComma = false;
    StringBuilder result = new StringBuilder();
    result.append('[');
    for (T t : this) {
      if (needsComma)
        result.append(", ");
      else
        needsComma = true;
      result.append(t.toString());
    }
    result.append(']');
    return result.toString();
  }

  public static void main(String[] args) {
    BitSetStructure<String> struc = new BitSetStructure<String>();
    BitSet<String> bs = new BitSet<String>(struc);
    bs.add("foo");
    bs.add("bar");
    bs.add("baz");
    System.out.println(bs);
    System.out.println(bs.contains("bar"));
    bs.remove("bar");
    System.out.println(bs);
    System.out.println(bs.contains("bar"));
  }
}
*/