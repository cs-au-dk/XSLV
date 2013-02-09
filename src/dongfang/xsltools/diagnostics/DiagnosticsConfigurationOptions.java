package dongfang.xsltools.diagnostics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DiagnosticsConfigurationOptions {
  
  public static final String AUTOMATA_TYPES = "AUTOMATA_TYPES";
  public static final String FAST_TYPES = "FAST_TYPES";
  public static final String CONTROL_FLOW_DIAGNOSTICS = "CONTROL_FLOW_DIAGNOSTICS";
  public static final String INPUT_TYPE_DIAGNOSTICS = "INPUT_TYPE_DIAGNOSTICS";
  
  public static class ConfigurationOption {
    boolean isEnabled;
    String key;
    String user;
    Set<String> relevantFor = new HashSet<String>();
    
    public ConfigurationOption(String key, String user, boolean isEnabled, String... relevantWith) {
      this.key = key;
      this.user = user;
      this.isEnabled = isEnabled;
      for (String s : relevantWith)
        this.relevantFor.add(s);
    }

    public String getKey() {return key;}
    
    public String getUserString() {return user;}
    
    public boolean isRelevantFor(String type) {
      return relevantFor.contains(type);
    }
    
    public Set<String> getRelevantFor() {
      return relevantFor;
    } 
    
    public boolean isEnabled() {
      return isEnabled;
    }
    
    public void setEnabled(boolean isEnabled) {
      this.isEnabled = isEnabled;
    }
    
    public boolean isRelevantForControlFlowDiagnostics() {
      return isRelevantFor(CONTROL_FLOW_DIAGNOSTICS);
    }
    public boolean isRelevantForInputTypeDiagnostics() {
      return isRelevantFor(INPUT_TYPE_DIAGNOSTICS);
    }
    public boolean isRelevantForFastTypes() {
      return isRelevantFor(FAST_TYPES);
    }
    public boolean isRelevantForAutomataTypes() {
      return isRelevantFor(AUTOMATA_TYPES);
    }
  }

  List<ConfigurationOption> options = new ArrayList<ConfigurationOption>();

  public DiagnosticsConfigurationOptions() {
    options.add(new ConfigurationOption("outputAllInstructionsInControlFlowGraph",
        "Show all template instructions (not only <tt>&lt;apply-templates/&gt;</tt> instructions)", 
        DiagnosticsConfiguration.current.outputAllInstructionsInControlFlowGraph(), AUTOMATA_TYPES, FAST_TYPES, CONTROL_FLOW_DIAGNOSTICS));
    options.add(new ConfigurationOption("outputSchemalessStuffInControlFlowGraph",
        "Show diagnostic info from the schemaless flow analyzer", DiagnosticsConfiguration.current.outputSchemalessStuffInControlFlowGraph(), AUTOMATA_TYPES, FAST_TYPES, CONTROL_FLOW_DIAGNOSTICS));
    options.add(new ConfigurationOption("outputDeadFlowDiagsInControlFlowGraph",
        "Show diagnostic info on killed flows", DiagnosticsConfiguration.current.outputDeadFlowDiagsInControlFlowGraph(), AUTOMATA_TYPES, FAST_TYPES, CONTROL_FLOW_DIAGNOSTICS));
    options.add(new ConfigurationOption("outputCharNamesInControlFlowGraph",
        "Show the internally assigned names for declarations", DiagnosticsConfiguration.current.outputCharNamesInControlFlowGraph(), AUTOMATA_TYPES, FAST_TYPES, CONTROL_FLOW_DIAGNOSTICS, INPUT_TYPE_DIAGNOSTICS));
    options.add(new ConfigurationOption("outputAncestorStringsInControlFlowGraph",
        "Show ancestor string examples in element/attribute declarations, and of nodes matching templates",DiagnosticsConfiguration.current.outputAncestorStringsInControlFlowGraph(), AUTOMATA_TYPES, CONTROL_FLOW_DIAGNOSTICS));
    options.add(new ConfigurationOption("outputAutomataInControlFlowGraph",
        "Show regular expressions for template rule patterns", DiagnosticsConfiguration.current.outputAutomataInControlFlowGraph(), AUTOMATA_TYPES, CONTROL_FLOW_DIAGNOSTICS));
  }
  
  public List<ConfigurationOption> getConfigurationOptions() {
    return options;
  }
  
  public Set<Object> getSelectedOptions() {
    Set<Object> result = new HashSet<Object>();
    for (ConfigurationOption option : getConfigurationOptions()) {
     if (option.isEnabled())
       result.add(option.getKey());
    }
    return result;
  }
}
