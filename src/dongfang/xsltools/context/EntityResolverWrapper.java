package dongfang.xsltools.context;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import dongfang.xsltools.resolver.ResolutionContext;

/**
 * A thing that makes an EntityResolver out of a ResolutionContext.
 * Used where EntityResolvers are neede as resource feeders, but we 
 * want to use a ResolutionContext instead (currently that is only 
 * the DTD class).
 * @author dongfang
 */
public class EntityResolverWrapper implements EntityResolver {
  private ResolutionContext context;
  private String userExplanation;
  private int userKey;
  
  public EntityResolverWrapper(ResolutionContext vcon, String userExplanation, int userKey) {
    this.context = vcon;
    this.userExplanation = userExplanation;
    this.userKey = userKey;
  }
  
  public InputSource resolveEntity (String publicId, String systemId) throws IOException {
    return context.resolveStream(systemId, userExplanation, userKey);
  }
}
