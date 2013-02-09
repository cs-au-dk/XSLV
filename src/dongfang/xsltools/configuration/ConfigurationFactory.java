package dongfang.xsltools.configuration;

public class ConfigurationFactory {

  // private static DumpConfiguration dumpConfiguration;

  private static FormattingConfiguration formattingConfiguration;

  // private static SelfTestConfiguration selfTestConfiguration;

  static {
    /*
     * The defaults. Arguably, some cases may arise where configuration aspects
     * are orthogonal -- e.g. we want verbose logging, but with the log file
     * paths suitable for web server deployment... ideally, the configuration
     * cetegories here should be on a one-per-aspect basis. Could be fixed. This
     * code is not sacred.
     */
    // setDebugDumpConfiguration();
    setConservativeFormattingConfiguration();
    // setWarningLoggerConfiguration();
    // setZealousSelfTestConfiguration();
  }

  /*
   public static DumpConfiguration getDumpConfiguration() {
   return dumpConfiguration;
   }
   */
  // public static SelfTestConfiguration getSelfTestConfiguration() {
  // return selfTestConfiguration;
  // }
  /*
   public static void setDumpConfiguration(DumpConfiguration c) {
   dumpConfiguration = c;
   }
   */

  /*
   public static void setWebserverDumpConfiguration() {
   DumpConfiguration c = new DumpConfiguration("/tmp", "/tmp", "/tmp", "/tmp",
   false, false, false, false, false);
   setDumpConfiguration(c);
   }

   public static void setQuietDumpConfiguration() {
   DumpConfiguration c = new DumpConfiguration("tmp", "tmp/annotation",
   "tmp/simplication", "tmp/html", false, false, false, false, false);
   setDumpConfiguration(c);
   }

   public static void setDebugDumpConfiguration() {
   DumpConfiguration c = new DumpConfiguration("tmp", "tmp/annotation",
   "tmp/simplification", "tmp/html", false, true, true, true, true);
   setDumpConfiguration(c);
   }
   */

  public static FormattingConfiguration getFormattingConfiguration() {
    return formattingConfiguration;
  }

  public static void setFormattingConfiguration(FormattingConfiguration c) {
    formattingConfiguration = c;
  }

  public static void setConservativeFormattingConfiguration() {
    FormattingConfiguration c = new FormattingConfiguration(FormattingConfiguration.PRESERVE_WHITESPACEONLY_TEXTNODES,
        "", false);
    setFormattingConfiguration(c);
  }

  public static void setReorganizingFormattingConfiguration() {
    FormattingConfiguration c = new FormattingConfiguration(FormattingConfiguration.KILL_WHITESPACEONLY_TEXTNODES,
        "  ", true);
    setFormattingConfiguration(c);
  }

  /*
   * public static void setZealousSelfTestConfiguration() {
   * selfTestConfiguration = new SelfTestConfiguration(false, false); }
   * 
   * public static void setConservativeSelfTestConfiguration() {
   * selfTestConfiguration = new SelfTestConfiguration(true, true); }
   */
}
