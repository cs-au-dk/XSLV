/*
 * dongfang M. Sc. Thesis
 * Created on 18-02-2005
 */
package dongfang.xsltools.util;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Not used. Go away.
 * @author dongfang
 */
public class IndentedBufferedWriter extends BufferedWriter {

  private static final char[] spcs = new char[256];
  static {
    for (int i = 0; i < spcs.length; i++)
      spcs[i] = ' ';
  }

  int tablength = 2;

  int indent = 0;

  public IndentedBufferedWriter(Writer out, int tablength) {
    super(out);
    this.tablength = tablength;
  }

  public IndentedBufferedWriter(Writer out, int bufsiz, int tablength) {
    super(out, bufsiz);
    this.tablength = tablength;
  }

  @Override
public void write(char[] cbuf, int off, int len) throws IOException {
    int i = off;
    int notwrittenpos = off;
    int end = off + len;
    while (notwrittenpos < end) {
      while (i < end && cbuf[i] != '\n') {
        i++;
      }
      if (i != end) // is was a CR
        super.write(spcs, 0, indent);
      super.write(cbuf, notwrittenpos, i - notwrittenpos);
      notwrittenpos = i++;
    }
  }

  @Override
public void write(int c) throws IOException {
    // ignore this
    super.write(c);
  }

  @Override
public void write(String s, int off, int len) throws IOException {
    // TODO improve !
    super.write(s.toCharArray(), off, len);
  }

  @Override
public void write(char[] cbuf) throws IOException {
    write(cbuf, 0, cbuf.length);
  }

  @Override
public void write(String str) throws IOException {
    write(str, 0, str.length());
  }

  public void increaseIndent() {
    indent = Math.min(indent + tablength, spcs.length);
  }

  public void decreasIndent() {
    indent = Math.max(0, indent - tablength);
  }
}