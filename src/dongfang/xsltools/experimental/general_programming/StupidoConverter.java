package dongfang.xsltools.experimental.general_programming;

import java.io.FileReader;

public class StupidoConverter {
  public static void main(String[] args) throws Exception {
    FileReader fr = new FileReader("experimental-data/stupido.txt");

    StringBuffer ct = new StringBuffer();

    int i;

    int mode = 0;
    while ((i = fr.read()) >= 0) {
      if (mode == 0) {
        if (i != '\"') {
          ct.append((char) i);
        } else {
          mode = 1;
          ct.append('\"');
        }
      } else {
        if (i != '\"') {
          ct.append(new String(new char[] { (char) i }).toLowerCase());
        } else {
          mode = 0;
          ct.append('\"');
        }
      }
    }

    /*
     * while((i = fr.read())>=0) { if (mode==0 && i!='#') { ct.append((char)i); }
     * else { if (i=='#') { mode = 1; ct.append('\"'); } else { if (mode>=1) {
     * if (i=='x') { mode = 2; } else { if (mode==2) { if (i=='-' || i==']' ||
     * i==' ') { mode = 0; ct.append((char)uc); ct.append('\"');
     * ct.append((char)i); uc = 0; } else uc = uc * 16 + hexval(i); } } } } } }
     */
    System.out.println(ct);
  }

  static int hexval(int i) {
    if (i >= '0' && i <= '9')
      return i - '0';
    else if (i >= 'A' && i <= 'F')
      return i - 'A' + 10;
    return 0;
  }
}