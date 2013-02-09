/*
 * dongfang M. Sc. Thesis
 * Created on 2005-02-24
 */
package dongfang.xsltools.experimental.general_programming;

/**
 * @author dongfang
 */
public class VisitorExperiment {
  private interface Visitor {
    void visit(A a);
  }

  private interface ExtendedVisitor extends Visitor {
    void visit(B b);
  }

  private static class ExtendedVisitorImpl implements ExtendedVisitor {
    public void visit(A a) {
      System.out.println("smutter");
    }

    public void visit(B b) {
      System.out.println("træffer");
    }
  }

  private static class A {
    public void accept(Visitor vis) {
      vis.visit(this);
    }
  }

  private static class B extends A {
    public void accept(ExtendedVisitor vis) {
      vis.visit(this);
    }
  }

  public static void main(String[] args) {
    B b = new B();
    ExtendedVisitor ev = new ExtendedVisitorImpl();
    b.accept(ev);

    Visitor v = ev;
    b.accept(v);

    A a = b;
    a.accept(ev);

    a.accept(v);
  }
}