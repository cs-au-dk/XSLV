/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.util;

/**
 * Generates unique, formatted names.
 * @author dongfang
 */
public class UniqueNameGenerator {

  private int number = 0;

  private static String hex(int number) {
    StringBuilder result = new StringBuilder();
    int shift = 32 - 4;
    while (shift >= 0) {
      int mask = 15 << shift;
      char digit = (char) ((number/* s[channelNumber] */& mask) >> shift);
      digit = (char) (digit < 10 ? digit + '0' : digit - (char) 10 + 'a');
      result.append(digit);
      shift -= 4;
    }
    String result_s = result.toString();
    while (result_s.length() > 2 && result_s.startsWith("00")) {
      result_s = result_s.substring(2);
    }
    return result_s;
  }

  public String getFreshId(String format) {
    // if (format.contains("mo"))
    // new RuntimeException().printStackTrace();
    return String.format(format, new Object[] { hex(number++) });
  }

  public static String getFreshId(int number, String format) {
    // if (format.contains("mo"))
    // new RuntimeException().printStackTrace();
    return String.format(format, new Object[] { hex(number) });
  }

  public void reset() {
    // for (int i=0; i<NO_CHANNELS; i++)
    // numbers[i] = 0;
    number = 0;
  }
}