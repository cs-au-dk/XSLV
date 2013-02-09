package dongfang.xsltools.experimental.general_programming;

public class StringCleanup {
  static String toFilterExprFreeString(String x) {
    String s = x.toString();
    int start;
    int end;
    while ((start = s.indexOf('[')) >= 0) {
      end = s.indexOf(']');
      s = s.substring(0, start) + s.substring(end + 1);
    }
    return s;
  }

  public static void main(String[] args) {
    System.out.println(toFilterExprFreeString("a[e]b[d]c"));
  }
}
