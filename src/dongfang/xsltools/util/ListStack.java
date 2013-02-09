/*
 * dongfang M. Sc. Thesis
 * Created on 2005-02-19
 */
package dongfang.xsltools.util;

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * Just another Stack implementation -- as opposed to java.util.Stack, this is
 * not synchronized, and as such faster.
 * 
 * @author dongfang
 */
public class ListStack<E> extends ArrayList<E> {
  /**
	 * 
	 */
	private static final long serialVersionUID = 6976385081824569464L;

public ListStack() {
    this(10);
  }

  public ListStack(int capacity) {
    super(capacity);
  }

  public void push(E e) {
    add(e);
  }

  public E pop() {
    if (size() == 0)
      throw new EmptyStackException();
    E e = get(size() - 1);
    remove(size() - 1);
    return e;
  }

  public E peek() {
    if (size() == 0)
      throw new EmptyStackException();
    return get(size() - 1);
  }
}
