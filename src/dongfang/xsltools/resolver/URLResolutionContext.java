/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import org.xml.sax.InputSource;

/**
 * @author dongfang
 */
public class URLResolutionContext implements ResolutionContext {
	//Logger logger = Logger.getLogger("resolution");

  public InputSource resolveStream(String systemId) throws IOException {
	  logger.fine("Resolving locally: SystemId: " + systemId);
    try {
      URL url = new URL(systemId);
      InputStream is = url.openStream();
      InputSource iso = // new InputSource(systemId);
      new ComboInputSource(is, "utf-8");
      iso.setSystemId(systemId);
      // Assumption: It's a file. Encoding? What is that?
      // is.setCharacterStream(new BufferedReader(new FileReader(systemId)));
      logger.fine("Succesfully opened local input on: " + systemId);
      return iso;
    } catch (IOException ex) {
      logger.warning("Failed to open local input on " + systemId + ": " + ex);
      throw ex;
    }
  }

  public InputSource resolveStream(String systemId, String user, int humanReadableKey)
      throws IOException {
    return resolveStream(systemId);
  }

  public void earlyStreamRequest(String systemId, String user, int humanReadableKey) {// ignore
  }

  public void earlyStringRequest(String id) {
  }
}
