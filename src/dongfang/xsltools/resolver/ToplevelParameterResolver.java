package dongfang.xsltools.resolver;

import org.dom4j.QName;

/*
 * Bare et forslag...
 */
public interface ToplevelParameterResolver {

  boolean acceptValidatingWithDefaultedToplevelParameters();

  // could move this to ResolutionContext
  Object resolveToplevelParameter(QName name);

  // could move this to ResolutionContext
  int getToplevelParameterType(QName name);
}
