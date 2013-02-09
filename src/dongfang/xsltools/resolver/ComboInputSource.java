/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-28
 */
package dongfang.xsltools.resolver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.xml.sax.InputSource;

/**
 * @author dongfang
 */
public class ComboInputSource extends InputSource {
  /**
   * 
   */
  public ComboInputSource(String encoding) {
    super();
    setEncoding(encoding);
  }

  /**
   * @param byteStream
   */
  public ComboInputSource(InputStream byteStream, String encoding) {
    super(byteStream);
    setEncoding(encoding);
  }

  /**
   * @param characterStream
   */
  public ComboInputSource(Reader characterStream, String encoding) {
    super(characterStream);
    setEncoding(encoding);
  }

  /**
   * @param systemId
   */
  public ComboInputSource(String systemId, String encoding) {
    super(systemId);
    setEncoding(encoding);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.InputSource#getCharacterStream()
   */
  @Override
public Reader getCharacterStream() {
    Reader s = super.getCharacterStream();
    if (s == null) {
      try {
        InputStream is = getByteStream();
        super.setCharacterStream(s = new InputStreamReader(is, getEncoding()));
      } catch (UnsupportedEncodingException ex) {
        // oops ... wastt to do?
      }
    }
    return s;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.InputSource#setCharacterStream(java.io.Reader)
   */
  /*
   * public void setCharacterStream(Reader characterStream) { throw new
   * UnsupportedOperationException("Don't touch!!!"); }
   */
}
