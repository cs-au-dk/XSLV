package dongfang.xsltools.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author dongfang
 */
public class InteractiveURLFactory {
  static class SlamKanal extends URLConnection {
    ResolutionContext context;

    public SlamKanal(final URL url, final ResolutionContext context) {
      super(url);
      this.context = context;
    }

    @Override
	public void connect() {
      ResolutionContext.logger.info("SlamKanal connected at URL: " + url);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.net.URLConnection#getInputStream()
     */
    @Override
	public InputStream getInputStream() throws IOException {
      return context.resolveStream(getURL().toString(), "No message", (short) -1)
          .getByteStream();
    }
  }

  private static int protno = 0;

  private static Map<String, URLStreamHandlerFactory> instances = new WeakHashMap<String, URLStreamHandlerFactory>();

  static synchronized String addSession(final ResolutionContext context) {
    final String myKey = "dongfang" + (protno++);
    final String myKey2 = new String(myKey);
    ResolutionContext.logger.info("Created session: " + myKey);
    URLStreamHandlerFactory fac = new URLStreamHandlerFactory() {
      URLStreamHandler handler = new URLStreamHandler() {
        @Override
		protected URLConnection openConnection(URL url) {
          return new SlamKanal(url, context);
        }
      };

      public URLStreamHandler createURLStreamHandler(String protocol) {
        if (!myKey2.equals(protocol))
          return null;
        return handler;
      }
    };
    instances.put(myKey, fac);
    return myKey;
  }
  /*
   * static { URLStreamHandlerFactory splatfac = new URLStreamHandlerFactory() {
   * public URLStreamHandler createURLStreamHandler(String protocol) {
   * URLStreamHandlerFactory result = instances.get(protocol); if (result ==
   * null) return null; return result.createURLStreamHandler(protocol); } };
   * URL.setURLStreamHandlerFactory(splatfac); }
   */
} // End of class.
