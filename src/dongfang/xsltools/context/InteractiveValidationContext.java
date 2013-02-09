/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.diagnostics.MemoryErrorReporter;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.experimental.progresslogging.ProgressLogger;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.resolver.dongfang.Handler;
import dongfang.xsltools.validation.ResultFeedbackValidationContext;
import dongfang.xsltools.validation.ResultListener;
import dongfang.xsltools.validation.ValidationResult;
import dongfang.xsltools.validation.XSLTValidator;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;

/**
 * @author dongfang
 */
public abstract class InteractiveValidationContext extends
		ValidationContextImpl {

	protected Logger logger;
	
	/*
	 * Key: The current request from the validator is for a string resource
	 * (like a namespace URI or a root element name)
	 */
	public final static int STRING_REQUEST = 0;

	/*
	 * Key: The current request from the validator is for a stream resource
	 * (like a stylesheet or a schema)
	 */
	public final static int STREAM_REQUEST = 1;

	DiagnosticsBuilder diagnostics;

	protected static class DiagnosticsBuilder {
		String session;
		long time = System.currentTimeMillis();
		Logger logger; 
		List<String> messages = new ArrayList<String>();
		
		DiagnosticsBuilder(String sessionName) {
			logger = ProgressLogger.getLogger(sessionName);
		}
		
		public void append(String message) {
			logger.info(/*source + " : " + */message);
			if (messages.size() < 100) {
				messages.add(message);
			} else if (messages.size() == 100) {
				messages.add("...");				
			}
		}

		public void clear() {
			messages.clear();
			//ProgressLogger.handback(logger);
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < messages.size(); i++) {
				result.append("<tr>");
				result.append("<td>");
				result.append(messages.get(i));
				result.append("</td>");
				result.append("</tr>");
			}
			return result.toString();
		}
	}

	/*
	 * Key: The current request from the validator is for a go-ahead to validate
	 * (after all resourced are loaded, or seem to be loaded)
	 */
	public final static int GOAHEAD_CONTROLFLOW_REQUEST = 2;

	public final static int GOAHEAD_VALIDATION_REQUEST = 3;

	public enum States {
		NOT_STARTED, LOADING, PRESENTING, ABORTING, TERMINATED
	}

	public static final String APPROXIMATED_STYLESHEET = "APPROXIMATED_STYLESHEET";

	public static final String SEMANTICS_PRESERVED_STYLESHEET = "SEMANTICS_PRESERVED_STYLESHEET";

	public static final String CONTROL_FLOW_GRAPH = "CONTROL_FLOW_GRAPH";

	public static final String SCHEMALESS_CONTROL_FLOW_GRAPH_AS_DOT = "SCHEMALESS_CONTROL_FLOW_GRAPH_AS_DOT";

	public static final String CONTROL_FLOW_GRAPH_AS_DOT = "CONTROL_FLOW_GRAPH_AS_DOT";

	public static final String TRANSFORM_TYPE_XML_GRAPH = "TRANSFORM_TYPE_XML_GRAPH";

	public static final String TRANSFORM_TYPE_XML_GRAPH_AS_DOT = "TRANSFORM_TYPE_XML_GRAPH_AS_DOT";

	public static final String INPUT_TYPE = "INPUT_TYPE";

	public static final String OUTPUT_TYPE_XML_GRAPH = "OUTPUT_TYPE_XML_GRAPH";

	public static final String OUTPUT_TYPE_XML_GRAPH_AS_DOT = "OUTPUT_TYPE_XML_GRAPH_AS_DOT";

	public static final String PERFORMANCE_REPORT = "PERFORMANCE_REPORT";

	private States _state = States.NOT_STARTED;

	synchronized States getState() {
		return this._state;
	}

	synchronized void setState(States state) {
		this._state = state;
	}

	/*
	 * The system ID of the resource currently requested
	 */
	protected String requestedId;

	/*
	 * A string, like "An XML instance document" shown to the user to help him
	 * understand what to serve us. Has no influence on the code.
	 */
	protected String requestedResourceTypeName;

	/*
	 * This is currently not used. Apparently is was something with a nullable
	 * input, for namespaces and root elements
	 */
	protected String stringReqUserExplanation;

	/*
	 * This is currently not used. Apparently is was something with a nullable
	 * input, for namespaces and root elements
	 */
	protected String stringReqNoneExplanation;

	/*
	 * Readable resource type names, like "input schema root element name"
	 */
	protected String c_humanReadable;

	/*
	 * STREAM or STRING.
	 */
	protected int requestType;

	/*
	 * This field is used for inter-thread comms (under monitors!!!): The last
	 * resource that the user provided is dumped here, and the validator is then
	 * notified. When the validator picks up the resource, is should null the
	 * field afterwards.
	 */
	// Object lastResourceFromEnvironment;
	// protected final Map<String, Object> lastResourcesFromEnvironment = new
	// HashMap<String, Object>();
	/*
	 * For diagnostics (what the heck happened?)
	 */
	// protected Throwable _lastEnvironmentException;
	/*
	 * For diagnostics (what the heck happened?)
	 */
	protected Exception lastValidatorException;

	/*
	 * This is the reference to the key string that prevents it from being
	 * kicked out of the weak hash map!
	 * 
	 * Something like "session-00".
	 */

	protected String sessionKey;

	protected String sessionKeyForLogging;

	/*
	 * Something like "dongfang://session-00".
	 */
	protected String key;

	protected LinkedHashMap<String, Object> generatedResources = new LinkedHashMap<String, Object>();

	protected Thread validatorThread;

	/*
	 * private boolean aborting;
	 * 
	 * private boolean isAborting() { return aborting; }
	 * 
	 * private void abort() { aborting = true; }
	 * 
	 * private void resetAborting() { aborting = false; }
	 */

	public InteractiveValidationContext() {
		String session = new Handler().addSession(this);
		this.sessionKey = session;
		this.sessionKeyForLogging = new String(session);
		
		logger = ProgressLogger.getLogger(sessionKeyForLogging);
		this.diagnostics = new DiagnosticsBuilder(this.sessionKeyForLogging);
		
		this.key = Handler.makeKey(session);
		// This ValidationContext is its own ResolutionContext.
		super.setResolver(this);
	}

	/*
	 * Something like "dongfang://session-00/input-schema" s served to make it
	 * variable (somehow) for, say, schemas for xsl:document. This will probably
	 * never be used...
	 */
	public String getSchemaIdentifier(String s, short io) {
		return key
				+ "/"
				+ SystemInterfaceStrings[INPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY
						+ io];
	}

	/*
	 * Something like "dongfang://session-00/principal-module" s served to make
	 * it variable (somehow) for, say, schemas for xsl:document. This will
	 * probably never be used...
	 */
	public String getStylesheetIdentifier() {
		return key
				+ "/"
				+ SystemInterfaceStrings[STYLESHEET_PRINCIPAL_MODULE_IDENTIFIER_KEY];
	}

	/*
	 * Something like "dongfang://session-00/input-root-element" or
	 * "dongfang://session-00/output-root-element" s served to make it variable
	 * (somehow) for, say, schemas for xsl:document. This will probably never be
	 * used...
	 */
	public String getRootElementNameIdentifier(String s, short io) {
		return key
				+ "/"
				+ SystemInterfaceStrings[INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY
						+ io];
	}

	/*
	 * Something like "dongfang://session-00/dtd-input-namespace" or
	 * "dongfang://session-00/dtd-output-namespace" s served to make it variable
	 * (somehow) for, say, schemas for xsl:document. This will probably never be
	 * used...
	 */
	public String getNamespaceURIIdentifier(String s, short io) {
		return key + "/"
				+ SystemInterfaceStrings[INPUT_DTD_NAMESPACE_URI_KEY + io];
	}

	/*
	 * public void validationResult(String result) {
	 * presentValidationResultToEnvironment(result); }
	 * 
	 * public void analysisResult(ValidationResult result) {
	 * presentValidationResultToEnvironment(result); }
	 */
	/**
	 * API towards web / console / whatever user interface that is the server is
	 * this.
	 */

	abstract boolean isCurrentRequestServed();

	protected abstract void doPresentNonFatalErrorsToEnvironment(
			List<XSLToolsException> errors);

	protected abstract void doPresentValidationResultToEnvironment(String result);

	protected abstract void doPresentValidationResultToEnvironment(
			ValidationResult result);

	protected abstract void clearMessages();

	private void presentNonFatalErrorsToEnvironment(
			List<XSLToolsException> errors) throws IOException {
		if (_state == States.ABORTING)
			throw new IOException("User or timeout abort");
		doPresentNonFatalErrorsToEnvironment(errors);
	}

	/*
	 * private void presentValidationResultToEnvironment(String result) throws
	 * IOException { if (state == States.ABORTING) throw new IOException("User
	 * or timeout abort"); doPresentValidationResultToEnvironment(result); }
	 */

	private void presentValidationResultToEnvironment(ValidationResult result)
			throws IOException {
		if (_state == States.ABORTING)
			throw new IOException("User or timeout abort");
		doPresentValidationResultToEnvironment(result);
	}

	/*
	 * synchronized boolean ___getStop() throws IOException { if (isAborting())
	 * throw new IOException("User or timer abort"); int patience = 10 * 60 *
	 * 1000; notify(); long time = System.currentTimeMillis(); try {
	 * wait(patience); } catch (InterruptedException ex) { abort(); throw new
	 * IOException("User or timer abort"); } if (System.currentTimeMillis() -
	 * time > patience) abort(); return isAborting(); //return true; }
	 */

	// abstract boolean getStop() throws IOException;
	/*
	 * Caches are not visible at this level; this is called even thru a cache
	 * hit! synchronized void _deliverResourceFromEnvironment(String
	 * abstractName, Object o) { }
	 */

	/*
	 * synchronized void deliverBadExcuse(Throwable ex) {
	 * logger.warning("InteractiveValidationContext received exception :" + ex);
	 * this.lastEnvironmentException = ex; logger.fine("Service thread notifying
	 * validator thread"); notify(); }
	 */

	protected synchronized void deliverEnvironmentAbort() {
		diagnostics.append("deliverEnvironmentAbort: start");
		// logger.info("deliverEnvironmentAbort: called");
		if (validatorThread != null)
			validatorThread.interrupt();
		diagnostics.append("deliverEnvironmentAbort: end");
	}

	public boolean isRequestingResources() {
		return this.requestedId != null;
	}

	/*
	 * public boolean isFlowAnalysisDone() { return this.flowAnalysisDone; }
	 */

	/**
	 * Some time during this call, requestInputSource or deliverResult can be
	 * expected to be called. Caller may then decide what to do, like sending a
	 * user message, and call back with deliverInputSource with the result --
	 * and then call this once again, for next request.
	 */
	public synchronized void serveCurrentRequest() throws Exception {
		while (_state == States.LOADING) {
			if (!isCurrentRequestServed()) {
				logger.fine("serveCurrentRequest: Unserved request (" + requestedId + ") received, breaking load-state wait loop.");
				break;
			}
			try {
				// Service thread waits for validator thread. We can not really
				// rely on
				// it notifying us -- it might crash with an exception.
				// Also, as long as the validator is LOADING, it may be thinking
				// about
				// something, and not have a request yet. We should
				// still wait for it then.
				wait(50);
			} catch (InterruptedException ex) {
			}
			if (lastValidatorException != null) {
				Exception ex = lastValidatorException;
				lastValidatorException = null;
				// ex.printStackTrace();
				throw ex;
			}
		}
		logger.fine("serveCurrentRequest: No longer in load-state, breaking load-state wait loop.");
	}

	public synchronized void waitForAllRequests() throws Exception {
		serveCurrentRequest();
	}

	public Map<String, Object> getGeneratedResources() {
		return generatedResources;
	}

	void afterStylesheetLoadedEvent() {
	}

	void afterXcfgEvent() {
	}

	public abstract String getSessionType();

	// This would be one nice place to pass validator exceptions...
	protected abstract void notifyTerminated();

	/**
	 * Let servlet container thread wait till a request or an answer comes in
	 * from validator.
	 */

	/**
	 * Considered cheating! public boolean isFinished() { return finished; }
	 */

	/**
	 * Fire and forget validator start... Caller should wait, by calling one of
	 * the waitFor... methods afterwards.
	 * 
	 * @throws IOException
	 */
	protected synchronized void startValidator(final ValidationContext val) {
		setState(States.LOADING);
		clearMessages();
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					// The validator runs in a different thread. We need to make the logger 
					// available from that thread also.
					ProgressLogger.setThreadLocal(logger);
					
					diagnostics.append("startValidator");
					//logger.info("Session " + sessionKeyForLogging
					//		+ ": validator thread (re)starting");

					MemoryErrorReporter cesspool = new MemoryErrorReporter();
					
					ResultListener rl = new ResultListener() {

						public void setSemPreservingSimplifiedStylesheet(
								Stylesheet ss) {
							if (ss != null)
								generatedResources.put(
										SEMANTICS_PRESERVED_STYLESHEET, ss.clone());
							logger.info("Session "
									+ sessionKeyForLogging
									+ ": done constructing semantics-preserved simplified stylesheet. Was null: " + 
									(ss==null ? "yes" : "no"));
						}

						public void setApproxSimplifiedStylesheet(Stylesheet ss) {
							if (ss != null) {
								generatedResources.put(APPROXIMATED_STYLESHEET, ss);
							}
							logger.info("Session "
									+ sessionKeyForLogging
									+ ": done constructing approximated simplified stylesheet. Was null: " + 
									(ss==null ? "yes" : "no"));
							afterStylesheetLoadedEvent();
						}

						public void setXcfg(ControlFlowGraph xcfg) {
							if (xcfg != null)
								generatedResources.put(CONTROL_FLOW_GRAPH, xcfg);
							logger.info("Session " + sessionKeyForLogging
									+ ": done constructing xcfg. Was null: " + 
									(xcfg==null ? "yes" : "no"));
						}

						public void setControlFlowSG(XMLGraph controlFlowSG) {
							if (controlFlowSG != null)
								generatedResources
										.put(TRANSFORM_TYPE_XML_GRAPH,
												controlFlowSG);
							afterXcfgEvent();
							logger.info("Session " + sessionKeyForLogging
									+ ": done constructing flow-xmlg. Was null: " + 
									(controlFlowSG==null ? "yes" : "no"));
						}

						public void setInputType(SingleTypeXMLClass inputType) {
							if (inputType != null)
								generatedResources.put(INPUT_TYPE, inputType);
						}

						public void setOutputType(XMLGraph outputType) {
							if (outputType != null)
								generatedResources.put(OUTPUT_TYPE_XML_GRAPH,
										outputType);
						}

						public void setPerformanceLogger(PerformanceLogger pa) {
							generatedResources.put(PERFORMANCE_REPORT, pa);
						}

						public void setValidationResult(ValidationResult result) {
							logger.info("Session " + sessionKeyForLogging
											+ ": done validating; result "
											+ result == null ? "is null"
											: ("has " + result.getErrorCount() + " errors"));
						}
					};

					// logger.info("###################################");
					// logger.info("######### validating with #########");
					// logger.info("###################################");
					logger.info("Session " + sessionKeyForLogging
							+ ": Validation context identifiers: Stylesheet:"
							+ val.getStylesheetIdentifier());
					logger.info("Session " + sessionKeyForLogging
							+ ": Validation context identifiers: Input type: "
							+ val.getSchemaIdentifier("", INPUT));
					logger.info("Session " + sessionKeyForLogging
							+ ": Validation context identifiers: Output type: "
							+ val.getSchemaIdentifier("", OUTPUT));

					// logger.info("Starting validation");

					diagnostics.append(
							"start validation");
					diagnostics.append("Stylesheet: " + val
							.getStylesheetIdentifier());
					diagnostics.append("Input type:" + val.getSchemaIdentifier(
							"", INPUT));
					diagnostics.append("Output type:" + val.getSchemaIdentifier("",
							OUTPUT));

					generatedResources.clear();

					long time = System.currentTimeMillis();

					ValidationContext ctx = new ResultFeedbackValidationContext(
							InteractiveValidationContext.this, rl);

					ValidationResult handler = XSLTValidator.validate(ctx,
							cesspool, rl, true);

					diagnostics.append("end validation");

					diagnostics.append("Time: " + (System.currentTimeMillis() - time) + " millis");

					if (cesspool.hasErrors()) {
						List<XSLToolsException> errors = cesspool.getErrors();
						presentNonFatalErrorsToEnvironment(errors);
					} else {
						presentValidationResultToEnvironment(handler);
					}
				} catch (Exception ex) {
					// cheapo logging
					if (_state == States.ABORTING) {
						logger.info("Session "
										+ sessionKeyForLogging
										+ ": Validator threw an exception, but is was supposed to. It was aborting.");
						diagnostics.append("validatorException: abort");
					} else {
						ex.printStackTrace();
						logger.info("Session " + sessionKeyForLogging
								+ ": Validator threw exception: "
								+ ex.getClass().getCanonicalName()
								+ ", message: " + ex.getMessage());
						lastValidatorException = ex;
						diagnostics.append("validatorException: crash");
					}
					// this will ensure that validator is fired up anew on next
					// web
					// service request.
					// Really, we might want to have a state for fixing the
					// error that
					// killed the
					// validator, BEFORE it is restarted. That state should
					// result in a
					// restart
					// button being displayed, and otherwise result in resources
					// to be
					// uploadable,
					// business as usual LOADING. The restart button should
					// simply restart
					// the
					// validator when clicked (skipping the NEWBORN state).
				} finally {
					// if we were aborted, we want to be sure to clear that for
					// each
					// single validation run.
					diagnostics.append("validator: terminated");
					logger.info("Session " + sessionKeyForLogging
							+ ": Validator thread terminated.");
					InteractiveValidationContext.this
							.setState(States.TERMINATED);
					InteractiveValidationContext.this.notifyTerminated();
				}
			}
		};
		t.start();
		validatorThread = t;
	}

	@Override
	public synchronized void reset() {
		super.reset();
		generatedResources.clear();
		requestedId = null;
		requestedResourceTypeName = null;
		// ProgressLogger.handback(logger);
	}

	@Override
	protected void finalize() throws Throwable {
		logger.info("Session: " + sessionKeyForLogging + ": "
				+ getClass().getSimpleName() + " " + hashCode()
				+ " leaving this world. Catch ya on the flip side.");
		ProgressLogger.handback(logger);
		super.finalize();
	}
}
