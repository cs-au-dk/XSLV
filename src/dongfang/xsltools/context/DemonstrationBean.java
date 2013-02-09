package dongfang.xsltools.context;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.InputSource;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLoadException;
import dongfang.xsltools.resolver.CachedInputSource;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.util.TestTriple;

public class DemonstrationBean extends InteractiveRequestResponseBean {

  private class PreresolvedResource {
    String uri;

    String requestTail;

    String encoding;

    PreresolvedResource(String uri, String requestTail, String encoding) {
      this.uri = uri;
      this.requestTail = requestTail;
      this.encoding = encoding;
    }

    String getURI() {
      return uri;
    }

    String getRequestTail() {
      return uri;
    }

    boolean check(String request) {
      return request.endsWith(requestTail);
    }

    String getEncoding() {
      return encoding;
    }

    @Override
	public String toString() {
      return requestTail + "-->" + getURI();
    }

    @Override
	public int hashCode() {
      return requestTail.hashCode();
    }

    @Override
	public boolean equals(Object o) {
      return (o instanceof PreresolvedResource)
          && requestTail.equals(((PreresolvedResource) o).getRequestTail());
    }
  }

  private class PreloadedString {
    String value;

    String requestTail;

    PreloadedString(String requestTail, String value) {
      this.requestTail = requestTail;
      this.value = value;
    }

    String getValue() {
      return value;
    }

    String getRequestTail() {
      return requestTail;
    }

    boolean check(String request) {
      return request.endsWith(requestTail);
    }

    @Override
	public String toString() {
      return requestTail + "-->" + getValue();
    }

    @Override
	public int hashCode() {
      return requestTail.hashCode();
    }

    @Override
	public boolean equals(Object o) {
      return (o instanceof PreloadedString)
          && requestTail.equals(((PreloadedString) o).getRequestTail());
    }
  }

  private Set<PreresolvedResource> preloads = new HashSet<PreresolvedResource>();

  private Set<PreloadedString> preloadStrings = new HashSet<PreloadedString>();

  String democaseInfo;

  String democaseNotes;

  boolean newDemocaseLoaded;  
  
  /*
   * If a new demo case has just been loaded, do not wait for a terminated validator.
   * The user has obviously lost interest in the old demo case, and wants to move on.
   * The validator should then be started without a click on the restart button.
   * (non-Javadoc)
   * @see dongfang.xsltools.context.InteractiveRequestResponseBean#handleRestartAfterCompletedValidationRun()
   */
  @Override
  void handleRestartAfterCompletedValidationRun() {
    if (newDemocaseLoaded) {
      newDemocaseLoaded = false;
      if (getState()==States.TERMINATED)
        setState(States.NOT_STARTED);
    }
  }

  public void preresolve(String requestTail, String uri, String encoding) {
    preloads.add(new PreresolvedResource(uri, requestTail, encoding));
  }

  public void preresolve(String requestTail, String uri) {
    preresolve(requestTail, uri, "utf-8");
  }

  public void preresolveTriple(String url, String contextUrl)
      throws XSLToolsLoadException, XSLToolsException, MalformedURLException {
    diagnostics.append("preresolveTriple: " + url);

    URL Url = new URL(url);

    newDemocaseLoaded = true;
    
    TestTriple triple = TestTriple.readTriple(Url);
    // preloads.add(new PreloadedResource(uri, requestTail, encoding));
    // triple.applyBase(url); // done already.
    preresolve(
        ResolutionContext.SystemInterfaceStrings[STYLESHEET_PRINCIPAL_MODULE_IDENTIFIER_KEY],
        triple.getStylesheetPrimaryModuleURI());
    preresolve(
        ResolutionContext.SystemInterfaceStrings[INPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY],
        triple.getInputSchemaURI());
    preresolve(
        ResolutionContext.SystemInterfaceStrings[OUTPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY],
        triple.getOutputSchemaURI());
    checkHackApplies(triple.getOutputSchemaURI());

    preresolveString(
        ResolutionContext.SystemInterfaceStrings[INPUT_DTD_NAMESPACE_URI_KEY],
        triple.getInputDTDNamespaceURI());
    preresolveString(
        ResolutionContext.SystemInterfaceStrings[OUTPUT_DTD_NAMESPACE_URI_KEY],
        triple.getOutputDTDNamespaceURI());

    preresolveString(
        ResolutionContext.SystemInterfaceStrings[INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY],
        triple.getInputRootElementName());
    preresolveString(
        ResolutionContext.SystemInterfaceStrings[OUTPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY],
        triple.getOutputRootElementName());

    democaseInfo = triple.getInfo();
    democaseNotes = triple.getNotes();

    // This comes again and again, just slam it in.
    preresolve("http://www.w3.org/2001/xml.xsd", contextUrl
        + "/examples/xhtml-from-xsd/xml.xsd");

    logger.fine(hashCode() + " preloaded: " + url + ", now have: " + preloads);
  }

  public void preresolveString(String requestTail, String value) {
    preloadStrings.add(new PreloadedString(requestTail, value));
  }

  @Override
  public InputSource resolveStream(String systemId, String userExplanation,
      int kind) throws IOException {
    if (getState() == States.ABORTING)
      throw new IOException("User or timeout abort");

    logger.fine("resolveStream");
    PreresolvedResource candidate = null;
    logger.fine("Has preloads: " + preloads);

    for (PreresolvedResource res : preloads) {
      if (res.check(systemId)) {
        candidate = res;
        break;
      }
    }

    logger.fine("Found preload: " + candidate);

    if (candidate != null) {
      URL url = new URL(candidate.getURI());
      logger.fine(hashCode() + " picked preresolve: " + systemId + "-->"
          + candidate.getURI());
      CachedInputSource iso = new CachedInputSource(url, systemId);
      iso.setEncoding(candidate.getEncoding());
      super.cacheEnvironmentProvidedResource(systemId, iso, "FILE", candidate
          .getURI());
      // taintInputSource(systemId);

      preloads.remove(candidate);

      return iso;
    } else {
      logger.fine(hashCode() + " found nothing for: " + systemId);
      logger.fine(hashCode() + "have: " + preloads);
    }
    return super.resolveStream(systemId, userExplanation, kind);
  }

  @Override
  public String resolveString(String systemId, String userExplanation,
      String noneExplanation, int kind) throws IOException {
    if (getState() == States.ABORTING)
      throw new IOException("User or timeout abort");

    PreloadedString candidate = null;

    for (PreloadedString res : preloadStrings) {
      if (res.check(systemId)) {
        candidate = res;
        break;
      }
    }

    if (candidate != null) {
      logger.fine("Picked preresolved string: " + systemId + "-->"
          + candidate.getRequestTail());
      cacheEnvironmentProvidedResource(systemId, candidate.getValue());

      preloadStrings.remove(candidate);

      return candidate.getValue();
    } else {
      logger.fine("Found nothing for: " + systemId);
      logger.fine("Have: " + preloads);
    }
    return super
        .resolveString(systemId, userExplanation, noneExplanation, kind);
  }

  synchronized void resolveGoAheadToControlFlow() {
    if (getState() == States.ABORTING)
      throw new RuntimeException("User or timeout abort");

    this.requestedId = key + "/controlflow.go.ahead";
    this.requestedResourceTypeName = "Go on to control flow analysis";
    this.c_humanReadable = "Go on to control flow analysis";
    this.requestType = GOAHEAD_CONTROLFLOW_REQUEST;

    logger
        .fine("Analysis thread notifying service thread; go ahead to control flow");

    long time = System.currentTimeMillis();

    notify();

    try {
      wait(PATIENCE);
    } catch (InterruptedException ex) {
      // ensure that validator dies.
      setState(States.ABORTING);
      throw new RuntimeException("User or timeout abort");
    }

    if (System.currentTimeMillis() - time > PATIENCE) {
      setState(States.ABORTING);
      throw new RuntimeException("User or timeout abort");
    }

    this.requestedId = null;
    // this.lastResourcesFromEnvironment.clear();
  }

  synchronized void resolveGoAheadToValidation() {
    if (getState() == States.ABORTING)
      throw new RuntimeException("User or timeout abort");

    this.requestedId = key + "/validation.go.ahead";
    this.requestedResourceTypeName = "Go on to validaiton";
    this.c_humanReadable = "Go on to validation";
    this.requestType = GOAHEAD_VALIDATION_REQUEST;

    logger
        .fine("Analysis thread notifying service thread; go ahead to control flow");

    long time = System.currentTimeMillis();

    notify();

    try {
      wait(PATIENCE);
    } catch (InterruptedException ex) {
      // ensure that validator dies.
      setState(States.ABORTING);
      throw new RuntimeException("User or timeout abort");
    }

    if (System.currentTimeMillis() - time > PATIENCE) {
      setState(States.ABORTING);
      throw new RuntimeException("User or timeout abort");
    }

    this.requestedId = null;
    // this.lastResourcesFromEnvironment.clear();
  }

  @Override
  void afterStylesheetLoadedEvent() {
    /*
     * Take care that these two are loaded immediately (so they are not billed
     * on validation time, ha ha)
     */
    try {
      long time = System.currentTimeMillis();
      // System.err.println("Cheating: Preloading input schema (this can be
      // turned off; find this println and remove it and the line below)");
      getInputType(getSchemaIdentifier("", INPUT));
      long input = System.currentTimeMillis() - time;

      time = System.currentTimeMillis();
      // System.err.println("Cheating: Preloading output schema (this can be
      // turned off; find this println and remove it and the line below)");
      getOutputType(getSchemaIdentifier("", OUTPUT));
      long output = System.currentTimeMillis() - time;

      logger.fine("input: " + input);
      logger.fine("output: " + output);

    } catch (XSLToolsLoadException ex) {
      logger.warning(ex.toString());
    }
    resolveGoAheadToControlFlow();
  }

  @Override
  void afterXcfgEvent() {
    resolveGoAheadToValidation();
  }

  public synchronized void reset(boolean includePreresolves) {
    killValidatorThread();
    super.reset();
    if (includePreresolves) {
      preloads.clear();
      preloadStrings.clear();
    }
    setState(States.NOT_STARTED);
  }

  public String getDemocaseInfo() {
    return democaseInfo;
  }

  public String getDemocaseNotes() {
    return democaseNotes;
  }

  @Override
  public void reset() {
    reset(true);
  }

  @Override
  public String getSessionType() {
    return "demo";
  }

  /*
   * An ugly turbo-XML Schema hack! The XML Schema for XHTML takes too long to
   * load. Cheating, we keep a static instance of the output type around, and
   * use that, at least until the user changes some resource. Really, the taint
   * detection system should have been used, but heck, this is simple. And, the
   * taint detection system cannot really be trusted, when, as here, the output
   * type was not created in the context.
   */

  static XMLGraph xhtmlSchemaHack;

  private boolean xhtmlSchemaHackApplies;

  private boolean someResourceChanged;

  /*
   * The hack is in avoiding a costly reload of the XHTML-transitional output schema.
   * Saves time.
   */
  private void checkHackApplies(String schemaname) {
    xhtmlSchemaHackApplies = schemaname.endsWith("xhtml1-transitional.xsd");
  }

  @Override
  protected void cacheEnvironmentProvidedResource(String resourceName,
      InputSource o, String method, String reference) {
    someResourceChanged = true;
    super.cacheEnvironmentProvidedResource(resourceName, o, method, reference);
  }

  @Override
  public XMLGraph getOutputType(String systemId) throws XSLToolsLoadException {
    logger.fine("getOutputType");
    XMLGraph result = null;
    if (xhtmlSchemaHackApplies && !someResourceChanged) {
      if (xhtmlSchemaHack == null)
        xhtmlSchemaHack = super.getOutputType(systemId);
      result = xhtmlSchemaHack;
    } else {
      // someResourceChanged = false;
      result = super.getOutputType(systemId);
    }
    return result;
  }
  
  @Override
  public boolean isDisplayingResetButton() {
    return false;
  }
}
