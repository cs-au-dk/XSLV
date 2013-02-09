/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-28
 */
package dongfang.xsltools.resolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.xml.sax.InputSource;

/**
 * @author dongfang
 */
public class CachedInputSource extends InputSource {
  /**
   * 
   */
  private byte[] data;

  private String sdata;

  // private short mode;

  public CachedInputSource(byte[] data, String systemId) {
    // this might not always work .. .. (with non upload types)
    if (data == null)
      throw new NullPointerException("Null not accepted !");
    this.data = data;
    setSystemId(systemId);
  }

  public CachedInputSource(String sdata, String systemId) {
    if (sdata == null)
      throw new NullPointerException("Null not accepted !");
    // System.err.println(sdata);
    this.sdata = sdata;
    setSystemId(systemId);
  }

  public CachedInputSource(InputStream is, String systemId) throws IOException {
    loadFromStream(is);
    setSystemId(systemId);
  }

  public CachedInputSource(URL url, String systemId) throws IOException {
    InputStream is = url.openStream();
    loadFromStream(is);
    setSystemId(systemId);
  }

  private void loadFromStream(InputStream is) throws IOException {
    byte[] buf = new byte[1024];
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    int i;
    while ((i = is.read(buf)) > 0) {
      bos.write(buf, 0, i);
    }
    is.close();
    data = bos.toByteArray();
  }

  @Override
public InputStream getByteStream() {
    // System.err.println("getByteStream()");
    if (data == null) {
      // System.err.println("encoding: " + getEncoding());
      try {
        data = sdata.getBytes(getEncoding());
        // System.err.println("dlen" + data.length);
      } catch (UnsupportedEncodingException ex) {
        // System.err.println("så skete det sgu");
        throw new RuntimeException(ex);
      }
    }
    return new ByteArrayInputStream(data);
  }

  @Override
public void setByteStream(InputStream is) {
    throw new UnsupportedOperationException("Don't touch!!!");
  }

  @Override
public Reader getCharacterStream() {
    // System.err.println("getCharacterStream() " + (sdata==null ? "n" :
    // Integer.toString(sdata.length())));
    if (sdata != null)
      return new StringReader(sdata);
    Reader s = null;
    try {
      s = new InputStreamReader(getByteStream(), getEncoding());
    } catch (UnsupportedEncodingException ex) {
      // System.err.println(ex);
      throw new RuntimeException(ex);
    }
    return s;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.InputSource#setCharacterStream(java.io.Reader)
   */
  @Override
public void setCharacterStream(Reader characterStream) {
    throw new UnsupportedOperationException("Don't touch!!!");
  }

  @Override
public boolean equals(Object o) {
    if (!(o instanceof CachedInputSource))
      return false;
    return getSystemId().equals(((CachedInputSource) o).getSystemId());
  }

  @Override
public int hashCode() {
    return getSystemId().hashCode();
  }
}
