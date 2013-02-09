/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-29
 */
package dongfang.xsltools.resolver.dongfang;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.WeakHashMap;

import org.xml.sax.InputSource;

import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * @author dongfang
 */
public class Handler extends URLStreamHandler {
  private static int nextSessionNumber = 0;

  private static final String SESSION_KEY_TEMPLATE = "se-%1S";

  private static Map<String, MyHandler> delegates = new WeakHashMap<String, MyHandler>();

  public Handler() {
  }

  private static class MyHandler extends URLStreamHandler {
    private ResolutionContext myContext;

    // private String user;
    // a mousie <:3 )---

    MyHandler(ResolutionContext myContext) {
      this.myContext = myContext;
      // this.user = user;
    }

    private static class SlamKloak extends URLConnection {
      ResolutionContext context;

      // private String user;

      SlamKloak(URL url, ResolutionContext context) {
        super(url);
        this.context = context;
        // this.user = user;
      }

      @Override
	public InputStream getInputStream() throws IOException {
        String h = url.toString();
        // url.getPath();
        InputSource source = context.resolveStream(h, "No message",
            ResolutionContext.UNKNOWN_RESOURCE);
        return source.getByteStream();
      }

      @Override
	public void connect() {
        ResolutionContext.logger.info("SlamKloak connected on URL: " + url);
      }
    }

    @Override
	protected URLConnection openConnection(URL url) {
      return new SlamKloak(url, myContext);
    }
  }

  public String addSession(final ResolutionContext context) {
    /*
     * If session number is becoming large, look for holes in the
     * session map of unused numbers. Use the first hole available.
     */
    if (nextSessionNumber >= 256) {
      int i;
      for (i = 0; i < nextSessionNumber; i++) {
        String myTempKey = UniqueNameGenerator.getFreshId(i,
            SESSION_KEY_TEMPLATE);
        if (!delegates.containsKey(myTempKey))
          break;
      }
      nextSessionNumber = i;
    }

    final String mySession = UniqueNameGenerator.getFreshId(
        nextSessionNumber++, SESSION_KEY_TEMPLATE);

    final String myKey = makeKey(mySession);
    MyHandler handler = new MyHandler(context);
    delegates.put(mySession, handler);
    ResolutionContext.logger.info("Created session for " + context
        + " under key " + myKey);
    return mySession;
  }

  public static String makeKey(String mySession) {
    return "dongfang" + "://" + mySession;
  }

  @Override
protected URLConnection openConnection(URL u) throws IOException {
    String session = u.getHost(); // cheating...
    ResolutionContext.logger.info("Trying to open connection on URL: " + u
        + ", session name is:" + session);
    MyHandler delegate = delegates.get(session);
    if (delegate == null) {
      ResolutionContext.logger.severe("Weird! Host " + session
          + " did not resolve. Available sessions are named: "
          + delegates.keySet());
      throw new IOException("Weird! Host " + session
          + " did not resolve. Available sessions are named: "
          + delegates.keySet());
    }
    ResolutionContext.logger.info("Found delegate for URL: " + u);
    return delegate.openConnection(u);
  }

  @Override
public String toString() {
    return getClass().getSimpleName() + ", delegates: " + delegates;
  }
}
