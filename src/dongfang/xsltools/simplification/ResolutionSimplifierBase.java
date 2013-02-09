package dongfang.xsltools.simplification;

import dongfang.xsltools.exceptions.XSLToolsException;

public abstract class ResolutionSimplifierBase extends
    StructureCopyingSimplifierBase {
  abstract boolean simplify(XPathExpressionParameterOrVariableBinding binding,
      Resolver resolver) throws XSLToolsException;
}
