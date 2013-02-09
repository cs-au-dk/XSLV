/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.validation;

import java.util.HashMap;
import java.util.Map;

import dongfang.xsltools.context.ValidationContextImpl;
import dongfang.xsltools.diagnostics.ShowStopperErrorReporter;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.resolver.URLResolutionContext;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.Util;

/**
 * @author dongfang
 */
public class XSLTValidatorMain extends ValidationContextImpl {

  String primaryModuleId;

  String inputSchemaId;

  String outputSchemaId;

  Map<String, String> namespaceAndRootParams;

  XSLTValidatorMain(String[] args) {
    String urledSS = Util.isURL(args[0]) ? args[0] : Util.toUrlString(args[0]);
    String urledIn = Util.isURL(args[1]) ? args[1] : Util.toUrlString(args[1]);

    namespaceAndRootParams = parameterMappings(args, true);

    String urledOut = args.length < 3 ? null : Util.isURL(args[2]) ? args[2]
        : Util.toUrlString(args[2]);

    primaryModuleId = urledSS;
    inputSchemaId = urledIn;
    outputSchemaId = urledOut;

    setResolver(new URLResolutionContext());
  }

  public String getStylesheetIdentifier() {
    return primaryModuleId;
  }

  @Override
public void earlyStreamRequest(String systemId, String user, int humanKey) {
  }

  public void earlyStringRequest(String id, String user, String none,
      int humanKey) {
  }

  public String getSchemaIdentifier(String s, short io) {
    if (io == INPUT)
      return inputSchemaId;
    return outputSchemaId;
  }

  public String getNamespaceURIIdentifier(String s, short io) {
    return s;
  }

  public String getRootElementNameIdentifier(String s, short io) {
    return s;
  }

  public String resolveString(String id, String user, String none, int humanKey) {
    return namespaceAndRootParams.get(id);
  }

  public void pushMessage(String target, String message) {
    System.out.println(message + "-->" + target);
  }

  @Override
public void reset() {
  }

  static boolean hasSwitch(String[] args, String key) {
    key = "-" + key;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals(key))
        return true;
    }
    return false;
  }

  /**
   * Catch up all cmd line args with a = in them; split as name to value map.
   * 
   * @param args
   * @param verbose
   * @return
   */
  static Map<String, String> parameterMappings(String[] args, boolean verbose) {
    Map<String, String> result = new HashMap<String, String>();
    for (int i = 0; i < args.length; i++) {
      int dashIndex = args[i].indexOf('-');
      int equalIndex = args[i].indexOf('=');
      if (equalIndex < 0 || dashIndex != 0)
        continue;
      String key = args[i].substring(1, equalIndex);
      String value = args[i].substring(equalIndex + 1);
      result.put(key, value);
      if (verbose)
        System.out.println(key + " is: " + value);
    }
    return result;
  }

  /*
   * Find out when validator is to stop.
   */
  static short fixupStop(short stopAt, String[] args) {
    stopAt = hasSwitch(args, "only-xcfg") ? ValidationRun.XCFG_CONSTRUCTED
        : stopAt;
    stopAt = hasSwitch(args, "only-xmlg") ? ValidationRun.SG_CONSTRUCTED
        : stopAt;
    return stopAt;
  }

  static boolean verboseProgress(String[] args) {
    return hasSwitch(args, "verbose-progress");
  }

  static boolean dumpLightly(String[] args) {
    return hasSwitch(args, "dump-lightly");
  }

  static boolean dumpFully(String[] args) {
    return hasSwitch(args, "dump-fully");
  }

  static void commonSwitches() {
    System.err
        .println("[-verbose-progress]: Be verbose about process progress.");
    System.err
        .println("[-only-xcfg]: Only proceed to flow graph generation stage. Do not validate.");
    System.err
        .println("[-only-xmlg]: Only proceed to XML Graph construction stage. Do not validate.");
  }

  static void usage() {
    System.err
        .println("Usage: "+XSLTValidatorMain.class.getCanonicalName()+" <stylesheet> <input-type> [output-type] "
            + "[-"
            + ResolutionContext.SystemInterfaceStrings[ResolutionContext.INPUT_DTD_NAMESPACE_URI_KEY]
            + "=namespace-uri] "
            + "[-"
            + ResolutionContext.SystemInterfaceStrings[ResolutionContext.INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY]
            + "=element-name] "
            + "[-"
            + ResolutionContext.SystemInterfaceStrings[ResolutionContext.OUTPUT_DTD_NAMESPACE_URI_KEY]
            + "=namespace-uri] "
            + "[-"
            + ResolutionContext.SystemInterfaceStrings[ResolutionContext.OUTPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY]
            + "=element-name] "
            + " [-verbose-progress] [-dump-lightly] [-dump-fully] [-only-xcfg] [-only-xmlg]");
    System.err
        .println("[-dump-lightly]: Dump some generated resources (in current dir)");
    System.err
        .println("[-dump-fully]: Dump a lot of generated resources (in current dir)");
    commonSwitches();
    System.exit(-1);
  }

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      usage();
    }

    XSLTValidatorMain m = new XSLTValidatorMain(args);
    ValidationRun vr = new MinimalValidationRun();

    short stopAt = fixupStop(ValidationRun.VALIDATED, args);

    vr = new StopShortDecorator(vr, stopAt);

    if (verboseProgress(args)) {
      vr = new VerboseRunDecorator(vr, System.out);
    }
    if (dumpLightly(args)) {
      vr = new LightValidationRunDumper(vr, "");
    } else if (dumpFully(args)) {
      vr = new ValidationRunDumper(vr, "");
    }

    String urledSS = m.getStylesheetIdentifier();

    XSLTValidator.validate(urledSS, m, new ShowStopperErrorReporter(), vr);

    if (vr.getProgress() < stopAt) {
      System.out
          .println("Processing not completed, due to static errors (or bugs in validator?)");
    } else if (stopAt == ValidationRun.VALIDATED) {
      if (vr.getValidationResult().isValid())
        System.out.println("XSLT is valid");
      else {
        System.out.println("XSLT is NOT valid:");
        vr.getValidationResult().killDuplicates();
        Dom4jUtil.debugPrettyPrint(vr.getValidationResult().getErrorReport());
      }
    }
  }
}
