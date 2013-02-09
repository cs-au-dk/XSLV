/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-09
 */
package dongfang.xsltools.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dongfang
 */
public class Util {
  
  public static String capitalizedStringToHyphenString(String cap) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < cap.length(); i++) {
      char c = cap.charAt(i);
      if (Character.isUpperCase(c)) {
        if (i > 0)
          result.append('-');
        result.append(Character.toLowerCase(c));
      } else
        result.append(c);
    }
    return result.toString();
  }

  public static String capitalizedStringToHyphenString(Class clazz) {
    return capitalizedStringToHyphenString(clazz.getSimpleName());
  }

  private static class EmptyIterator implements Iterator {
    public boolean hasNext() {
      return false;
    }

    public Object next() {
      return null;
    }

    public void remove() {
    }
  }

  public static final Iterator EMPTY_ITER = new EmptyIterator();

  public static String toUrlString(String filename) {
    // String currentDir = System.getProperty("user.dir");
    File f = new File(/* currentDir, */filename);
    String path = f.getAbsolutePath();
    // path = java.net.URLEncoder.encode(path);
    // this is not good enough: Will cause trouble for the resolver.
    path = path.replaceAll(" ", "%20"); // ouch!
    path = path.replaceAll("\\\\", "/");

    if (path.startsWith("/"))
      return "file://" + path;
    return "file:///" + path;
  }

  /*
   * public static void main(String[] args) {
   * System.out.println(toUrlString("gedefims.dat")); }
   */
  public static boolean isURL(String s) {
    try {
      /* URL sniff = */new URL(s);
      return true;
    } catch (MalformedURLException ex) {
      return false;
    }
  }

  private static final Pattern spaceOutsideQuotes = Pattern
      .compile("( |\n)+((([^\']*?'[^\']*?)|([^\"]*?\"[^\"]*?))*?)");

  private static final Pattern nsDeclZapper = Pattern.compile("(.*?)(xmlns:.*?=('|\").*?('|\"))(.*?)");

  public static String wrapOverWhitespace(String s) {
    // return s.replace(' ', '\n');
    Matcher mask = spaceOutsideQuotes.matcher(s);
    return mask.replaceAll("$2\n");
  }

  public static String zapXMLnsDecl(String s) {
    Matcher mask = nsDeclZapper.matcher(s);
    return mask.replaceAll("$1$5");
  }

}