package dongfang.xsltools.validation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.xml.sax.InputSource;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

public abstract class CachingValidationContext extends ValidationContextDecoratorBase {

  //static final Logger logger = Logger.getLogger(CachingValidationContext.class.getSimpleName());
  
  private boolean stylesheetTainted;
  private Set<String> taintedInputTypes = new HashSet<String>();
  private Set<String> taintedOutputTypes = new HashSet<String>();
  
  private boolean recordingStylesheet;
  private String recordingInputType; 
  private String recordingOutputType;
    
  private Map<String, Set<String>> knownInputResources = new HashMap<String, Set<String>>();
  private Map<String, Set<String>> knownOutputResources = new HashMap<String, Set<String>>();
  private Set<String> knownStylesheetResources = new HashSet<String>();

  private Map<String, InputSource> streamCache = new HashMap<String, InputSource>();
  
  private Map<String, SingleTypeXMLClass> inputTypeCache = new HashMap<String, SingleTypeXMLClass>();
  private Map<String, XMLGraph> outputTypeCache = new HashMap<String, XMLGraph>();

  private Stylesheet stylesheetCache;
  private ControlFlowGraph controlFlowCache;
  private XMLGraph controlFlowXMLGraphCache;

  /*
   private class CacheEntry<T> {
   int timestamp;
   T t;

   CacheEntry(int timestamp, T t) {
   this.timestamp = timestamp;
   this.t = t;
   }

   int getTimestamp() {
   return timestamp;
   }

   T getResource() {
   return t;
   }
   }
   */

  public CachingValidationContext() {
    super();
  }
  
  public CachingValidationContext(ValidationContext vcon) {
    super(vcon);
  }

  protected void deliverInputSource(String systemId, InputSource stream) {
    log("Delivering InputSource: " + systemId);
    
    streamCache.put(systemId, stream);
    Set<String> ss = knownInputResources.get(recordingInputType);
    if (ss != null) {
      log("InputSource input schema recording is for " + recordingInputType +"; recording " + systemId);
      ss.add(systemId); // record
    }
    for (Map.Entry<String, Set<String>> sss : knownInputResources.entrySet()) {
      if (sss.getValue().contains(systemId)) {
        log("Tainting input schema: " + sss.getKey());
        taintedInputTypes.add(sss.getKey());
      }
    }
    
    ss = knownOutputResources.get(recordingOutputType);
    if (ss != null) {
      log("InputSource output schema recording is for " + recordingInputType +"; recording " + systemId);
      ss.add(systemId); // record
    }
    for (Map.Entry<String, Set<String>> sss : knownOutputResources.entrySet()) {
      if (sss.getValue().contains(systemId)) {
        log("Tainting output schema: " + sss.getKey());
        taintedOutputTypes.add(sss.getKey());
      }
    }

    if (recordingStylesheet) {
      log("Stylesheet recording is on");
      knownStylesheetResources.add(systemId); // record
    }

    if (knownStylesheetResources.contains(systemId)) {
      log("Tainting stylesheet");
      stylesheetTainted = true;
    }
  }

  private boolean inputTypeTainted(String type) {
    return taintedInputTypes.contains(type) || !knownInputResources.containsKey(type) || knownInputResources.get(type).isEmpty();
  }

  private void untaintInputType(String type) {
    taintedInputTypes.remove(type);
  }

  private boolean outputTypeTainted(String type) {
    return taintedOutputTypes.contains(type) || !knownOutputResources.containsKey(type) || knownOutputResources.get(type).isEmpty();
  }

  private void untaintOutputType(String type) {
    taintedOutputTypes.remove(type);
  }

  private boolean stylesheetTainted() {
    return stylesheetTainted;
  }

  private void untaintStylesheet() {
    stylesheetTainted = false;
  }

  @Override
  public InputSource resolveStream(String systemId, String userExplanation, int humanKey) throws IOException {
    InputSource source = super.resolveStream(systemId, userExplanation, humanKey);
    deliverInputSource(systemId, source);
    return source;
  }

  @Override
  public String resolveString(String systemId, String user, String none, int humanKey) throws IOException {
    // TODO Auto-generated method stub
    return super.resolveString(systemId, user, none, humanKey);
  }

  @Override
  public Stylesheet getStylesheet(ErrorReporter cesspool) throws XSLToolsException {
    if (stylesheetTainted()) {
      log("Stylesheet is tainted; loading from below");
      stylesheetCache = super.getStylesheet(cesspool);
      untaintStylesheet();
    }
    return stylesheetCache;
  }

  @Override
  public SingleTypeXMLClass getInputType(String systemId) throws XSLToolsLoadException {
    if (inputTypeTainted(systemId)) {
      log("Input type " + systemId + " is tainted; loading from below");
      if (!knownInputResources.containsKey(systemId)) {
        knownInputResources.put(systemId, new HashSet<String>());
      }
      recordingInputType = systemId;
      inputTypeCache.put(systemId, super.getInputType(systemId));
      recordingInputType = null;
      untaintInputType(systemId);
    }
    return inputTypeCache.get(systemId);
  }

  @Override
  public XMLGraph getOutputType(String systemId) throws XSLToolsLoadException {
    if (outputTypeTainted(systemId)) {
      log("Output type " + systemId + " is tainted; loading from below");
      if (!knownOutputResources.containsKey(systemId)) {
        knownOutputResources.put(systemId, new HashSet<String>());
      }
      recordingOutputType = systemId;
      outputTypeCache.put(systemId, super.getOutputType(systemId));
      
      recordingOutputType = null;
      untaintOutputType(systemId);
    }
    return outputTypeCache.get(systemId);
  }

  @Override
  public ControlFlowGraph getControlFlowGraph(ErrorReporter cesspool) throws XSLToolsException {
    if (stylesheetTainted() || !taintedInputTypes.isEmpty() || controlFlowCache == null) {
      log("XCFG is tainted; loading from below");
      controlFlowCache = super.getControlFlowGraph(cesspool);
    } 
    return controlFlowCache;
  }

  @Override
  public XMLGraph getControlFlowXMLGraph(ErrorReporter cesspool) throws XSLToolsException {
    if (stylesheetTainted() || !taintedInputTypes.isEmpty() || controlFlowXMLGraphCache == null) {
      log("XCFG XMLGraph is tainted; loading from below");
      controlFlowXMLGraphCache = super.getControlFlowXMLGraph(cesspool);
    } 
    return controlFlowXMLGraphCache;
  }

  @Override
  public void validate(ErrorReporter cesspool, ValidationResult result) throws XSLToolsException {
    super.validate(cesspool, result);
  }
  
  @Override
public void reset() {
    stylesheetTainted = false;
    taintedInputTypes.clear();
    taintedOutputTypes.clear();
    
    recordingStylesheet = false;
    recordingInputType = null; 
    recordingOutputType = null;
      
    knownInputResources.clear();
    knownOutputResources.clear();
    knownStylesheetResources.clear();

    streamCache.clear();
    inputTypeCache.clear();
    outputTypeCache.clear();

    stylesheetCache = null;
    controlFlowCache = null;
    controlFlowXMLGraphCache = null;
  }
  
  private void log(String message) {
    logger.info(message);
  }
}
