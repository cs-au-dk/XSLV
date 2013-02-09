/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-27
 */
package dongfang.xsltools.context;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.xml.sax.InputSource;

import dk.brics.xmlgraph.XMLGraph;
import dk.brics.xmlgraph.converter.Serializer;
import dk.brics.xmlgraph.converter.XMLGraph2Dot;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.diagnostics.DiagnosticsConfigurationOptions;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLocatableException;
import dongfang.xsltools.experimental.progresslogging.ProgressLogger;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.resolver.CachedInputSource;
import dongfang.xsltools.resolver.CachedWebResource;
import dongfang.xsltools.resolver.CachedWebString;
import dongfang.xsltools.resolver.ReadableRequestBean;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.DotConverter;
import dongfang.xsltools.validation.ValidationResult;

/**
 * @author dongfang
 */
public class InteractiveRequestResponseBean extends
		InteractiveValidationContext {

	protected static final int PATIENCE = 10 * 60 * 1000;

	private ValidationResult validationResult;

	private List<XSLToolsException> nonFatalErrors;

	private SortedMap<String, CachedWebResource> cachedResources = new TreeMap<String, CachedWebResource>();

	private Map<String, CachedWebString> cachedStrings = new HashMap<String, CachedWebString>();

	private List<ReadableRequestBean> earlyStreamRequests = new LinkedList<ReadableRequestBean>();

	private List<ReadableRequestBean> earlyStringRequests = new LinkedList<ReadableRequestBean>();

	private List<String> shortLivedMessages = new LinkedList<String>();

	private List<String> competitionMessages = new LinkedList<String>();

	private String currentProcessControl;

	/*
	 * A commons-fileupload thingy
	 */
	private ServletFileUpload upl = new ServletFileUpload(
			new DiskFileItemFactory());

	public InteractiveRequestResponseBean() {
	}
	
	/*
	 * UIs call that to get the name of the resource that MUST be supplied
	 * (unless finished)
	 */
	public synchronized String getRequestedFieldName() {
		return requestedId;
	}

	/*
	 * UIs call that to get the type of the resource that MUST be supplied
	 * (unless finished)
	 */
	public synchronized int getRequestResourceType() {
		return requestType;
	}

	/*
	 * For JSPs that don't cope so well with static stuff...
	 */
	public int getStreamRequestResourceCode() {
		return STREAM_REQUEST;
	}

	public int getStringRequestResourceCode() {
		return STRING_REQUEST;
	}

	public int getGoAheadToControlFlowCode() {
		return GOAHEAD_CONTROLFLOW_REQUEST;
	}

	public int getGoAheadToValidationCode() {
		return GOAHEAD_VALIDATION_REQUEST;
	}

	/*
	 * UI thread thingy
	 */
	public String getRequestMessage() {
		return c_humanReadable;
	}

	/*
	 * UI thread thingy
	 */
	public String getStringRequestUserExplanation() {
		return stringReqUserExplanation;
	}

	/*
	 * UI thread thingy
	 */
	public String getStringRequestNoneExplanation() {
		return stringReqNoneExplanation;
	}

	/*
	 * UI thread thingy
	 */
	public String getRequestedResourceTypeName() {
		return requestedResourceTypeName;
	}

	/*
	 * UI thread thingy
	 */
	public ValidationResult getValidationResult() {
		return validationResult;
	}

	/*
	 * Internal (really a Servlet thing, not generic): Get data from request. UI
	 * thread.
	 */
	private synchronized void processUpload(HttpServletRequest request)
			throws IOException {
		String encoding = request.getCharacterEncoding();

		if (encoding == null)
			encoding = "utf-8";

		List files = null;

		try {
			files = upl.parseRequest(request);
		} catch (FileUploadException ex) {
			logger.warning(ex.toString());
			return;
		}

		Map<String, String> options = new HashMap<String, String>();

		for (Iterator iter = files.iterator(); iter.hasNext();) {
			FileItem fi = (FileItem) iter.next();
			if (fi.isFormField()) {
				// field name syntax is: X:n
				// where X is "URL", "FILE" or "TEXT" and n is the system ID
				// OR X is "input-method" and n the system ID
				// diagnostics += ("Got form field: fieldName: " +
				// fi.getFieldName() +
				// ", name: " + fi.getName() + "<br/>");
				String fname = fi.getFieldName();
				if (fname.indexOf("input-method:") == 0) {
					String resname = fname.substring("input-method:".length());
					String restype = fi.getString();
					options.put(resname, restype);
					iter.remove();
				} else if (fname.indexOf("input-option:") == 0) {
					String resname = fname.substring("input-option:".length());
					String restype = fi.getString();
					options.put(resname, restype);
					iter.remove();
				} else if (fname.indexOf("DONE:") == 0) {
					String resname = "DONE";
					String resval = fname.substring(5);
					options.put(resname, resval);
				} else {
					// Achtung! This used bleedin' default encoding.
					options.put(fname, fi.getString());
				}
			}
		}

		for (Iterator iter = files.iterator(); iter.hasNext();) {
			FileItem fi = (FileItem) iter.next();

			String resourceName = fi.getFieldName();
			int colonIdx = resourceName.indexOf(':');

			String myOption = resourceName.substring(0, colonIdx);
			resourceName = resourceName.substring(colonIdx + 1);

			currentProcessControl = null;

			if ("STRING".equals(myOption)) {
				String s = null;
				if (!"NONE".equals(options.get(resourceName))) {
					s = fi.getString(encoding);
					logger.fine("a string was provided: " + resourceName + "-->" + s);
				}
				logger.fine("a nulled string was provided: " + resourceName);
				cacheEnvironmentProvidedResource(resourceName, s);
			} else if ("DONE".equals(myOption)) {
				currentProcessControl = options.get("DONE");
			} else {
				String chosen = options.get(resourceName);
				// diagnostics += "chosen is=" + chosen + "<br/>";
				if (chosen == null || !(chosen.equals(myOption)))
					continue;
				InputSource iso = null;
				if ("URL".equals(myOption)) {
					URL ulla = new URL(fi.getString());

					String protocol = ulla.getProtocol();
					logger.info("Protocol is: " + protocol);
					if ("file".equalsIgnoreCase(protocol)) {
						throw new IOException(
								"Sorry, you cannot use the file protocol for snooping around on this server. Nice try anyway.");
					}

					InputStream is = ulla.openStream();
					String systemId = ulla.toString();

					// This (resourceName instead of systemId) should solve the
					// problem with resources disappearing from edit if opened
					// from an url. On the other side, it will probably fuck
					// up entity resolution.
					iso = new CachedInputSource(is, systemId);
					iso.setEncoding(encoding); // a LONG shot...
					cacheEnvironmentProvidedResource(resourceName, iso,
							myOption, fi.getString());
				}

				else if ("TEXT".equals(myOption)) {
					// logger.info("a text stream: " + resourceName);
					iso = new CachedInputSource(fi.getString(encoding),
							resourceName);
					iso.setEncoding(encoding);
					cacheEnvironmentProvidedResource(resourceName, iso,
							myOption, "");
					// diagnostics += "cached text: " + escapeXML(resourceName)
					// + "<br/>";
				}

				else if ("FILE".equals(myOption)) {
					// logger.info("a file: " + resourceName);
					if (fi.getSize() > 0) {
						iso = new CachedInputSource(fi.get(), resourceName);
						iso.setEncoding(encoding);
						cacheEnvironmentProvidedResource(resourceName, iso,
								myOption, fi.getName());
					}
				} else {
					throw new IOException("Bad myOption: " + myOption);
				}
			}
		}
		// logger.info("Service thread processed request");
	}

	public String getDiagnostics() {
		String diagnostics = this.diagnostics.toString();
		this.diagnostics.clear();
		return diagnostics;
	}

	synchronized void killValidatorThread() {
		if (getState() == States.TERMINATED || getState() == States.NOT_STARTED) {
			diagnostics.append("killValidatorThread: Already terminated or not started, nothing to do here");
			return;
		}
		setState(States.ABORTING);
		diagnostics.append("killValidatorThread: set aborting");
		long time = System.currentTimeMillis();
		while (validatorThread != null && getState() == States.ABORTING
				&& System.currentTimeMillis() < time + 10000) {
			// diagnostics.append("killValidatorThread - before
			// deliverAbort<br/>");
			deliverEnvironmentAbort();
			try {
				wait(250);
			} catch (InterruptedException ex) {
			}
			// logger.info("Validator thread killer recycled");
		}
		diagnostics.append("killValidatorThread: Validator thread killer released");
	}

	void handleRestartAfterCompletedValidationRun() {
	}

	/*
	 * UIs call this once each cycle (as the first thing)
	 */
	public synchronized void service(HttpServletRequest request)
			throws Exception {
		
		// Share our session local logger with other clients in same thread.
		ProgressLogger.setThreadLocal(logger);
		
		diagnostics.append("*** service: entry state: " + getState());
		try {
			/*
			 * Store the key in the session, and only there. The session is the
			 * only thing preventing the delegate handler for this session from
			 * being purged in the dongfang Handler. The purging should prevent
			 * old, dead session beans from piling up forever.
			 */
			if (sessionKey != null) {
				request.getSession().setAttribute("session-key", sessionKey);
				sessionKey = null;

				// Let's not take the risk of a DNS lookup now. At least I
				// should study the documentation
				// for error behavior etc before doing it.
				String remoteAddr = request.getRemoteAddr();
				logger.info("*** A new session was initiated; remote IP is "
						+ remoteAddr + "; session key is " + sessionKeyForLogging);
			}

			processUpload(request);

			// an experiment. If ill effects observed, try remove me.
			// This should solve the problem of the user having to go through
			// one extra request/response after a succesful termination in
			// the demo stuff, by causing an immediate restart.
			//
			// OUCH! It causes a restart even when the user wants to
			// do a little editing (like trying to correct resources)
			// after validation, then hoping to be able to restart
			// manually using the restart button.
			// What we really want is to fire this thing after a CHANGE
			// of demo-case. Maybe just put it into a virtual method
			// in the demo bean, with the impl of the same method here
			// doing nothing. Let's try just that.
			/*
			 * if (!"reset".equals(currentProcessControl)) { if (getState() ==
			 * States.TERMINATED) { setState(States.NOT_STARTED); } }
			 */
			// here she is:
			handleRestartAfterCompletedValidationRun();

			if (getState() == States.NOT_STARTED) {
				diagnostics.append("service: thread NOT_STARTED; started it (starting)");
				startValidator();
				diagnostics.append("service: thread NOT_STARTED; started it (done)");
			}

			// maybe this could be moved to above previous if statement, and
			// instead
			// of restarting, simply set state to NOT_STARTED. Simpler.
			if ("reset".equals(currentProcessControl)) {
				if (getState() != States.TERMINATED) {
					diagnostics.append("service: user reset (validator not terminated)");
				}
				diagnostics.append("service: reset");
				killValidatorThread();
				reset();
				diagnostics.append("service: validator start after reset(before)");
				startValidator();
				diagnostics.append("service: validator start after reset(after)");
			}

			else if ("restart".equals(currentProcessControl)) {
				killValidatorThread();
				restart();
				diagnostics.append("service: user restart(before)");
				startValidator();
				diagnostics.append("service: user restart(after)");
			}

			if (getState() == States.LOADING) {
				diagnostics.append("service: loading");
				serveCurrentRequest();
			}

			if (getState() == States.ABORTING) {
				// wait for validator to quit
				deliverEnvironmentAbort();
				try {
					wait(10000);
				} catch (InterruptedException ex) {
				}
				diagnostics.append("service: strange aborting case(before)");
				startValidator();
				diagnostics.append("service: strange aborting case(after)");
				diagnostics.append("service: validator restarted");
			}
		} catch (Exception ex) {
			logger.warning("Service failure: " + ex.toString());
			ex.printStackTrace();
			// startValidator(this);
			// we assume that the exception was in validation, not in the web
			// stuff.
			throw ex;
		}
		diagnostics.append("service: exit state: " + getState());
	}

	/*
	 * Internal (really a Servlet thing, not generic): Get data from request. UI
	 * thread.
	 */
	protected void cacheEnvironmentProvidedResource(String resourceName,
			InputSource o, String method, String reference) {
		logger.info("Session "+sessionKeyForLogging+" received resource: resourceName: " + resourceName + ", systemId=" + o.getSystemId() + " with input method: " + method);
		CachedWebResource cr = new CachedWebResource(resourceName, o, reference, method);
		cachedResources.put(resourceName, cr);
		earlyStreamRequests.remove(new ReadableRequestBean(resourceName, null));
		taintInputSource(resourceName);
	}

	protected void cacheEnvironmentProvidedResource(String resourceName,
			String o) {
		CachedWebString cs = new CachedWebString(o, o);
		logger.info("Session "+sessionKeyForLogging+" received string: resourceName: " + resourceName + ", value=" + o);
		cachedStrings.put(resourceName, cs);
		earlyStringRequests.remove(new ReadableRequestBean(resourceName, null));
	}

	/*
	 * Part of the old request-ahead framework. Currently not used.
	 */
	public List<ReadableRequestBean> getOptionalStreamRequests() {
		List<ReadableRequestBean> clone = new LinkedList<ReadableRequestBean>(
				earlyStreamRequests);
		if (requestType == STREAM_REQUEST && requestedId != null)
			clone.remove(new ReadableRequestBean(requestedId, null));
		return clone;
	}

	/*
	 * Part of the old request-ahead framework. Currently not used.
	 */
	public List<ReadableRequestBean> getOptionalStringRequests() {
		List<ReadableRequestBean> clone = new LinkedList<ReadableRequestBean>(
				earlyStringRequests);
		if (requestType == STRING_REQUEST && requestedId != null)
			clone.remove(new ReadableRequestBean(requestedId, null));
		return clone;
	}

	@Override
	protected synchronized void notifyTerminated() {
		diagnostics.append("notifyTerminated: go");
		notify();
		// experiment
	}

	public List<XSLToolsException> getNonFatalErrors() {
		if (nonFatalErrors == null)
			return Collections.emptyList();
		return nonFatalErrors;
	}

	public List<XSLToolsLocatableException> getNonFatalLocatableErrors() {
		List<XSLToolsLocatableException> result = new LinkedList<XSLToolsLocatableException>();
		for (XSLToolsException ex : getNonFatalErrors()) {
			if (ex instanceof XSLToolsLocatableException) {
				result.add((XSLToolsLocatableException) ex);
			}
		}
		return result;
	}

	public List<XSLToolsException> getNonFatalNonLocatableErrors() {
		List<XSLToolsException> result = new LinkedList<XSLToolsException>();
		for (XSLToolsException ex : getNonFatalErrors()) {
			if (!(ex instanceof XSLToolsLocatableException)) {
				result.add(ex);
			}
		}
		return result;
	}

	synchronized boolean isCurrentRequestServed() {
		Object cachedResult = null;

		if (requestedId == null) // null requests yield null results.
			return true;

		// taintInputSource(requestedId);

		if (requestType == STREAM_REQUEST) {
			cachedResult = cachedResources.get(requestedId);
		}

		else if (requestType == STRING_REQUEST) {
			cachedResult = cachedStrings.get(requestedId);
		}

		else if (requestType == GOAHEAD_CONTROLFLOW_REQUEST
				|| requestType == GOAHEAD_VALIDATION_REQUEST) {
			cachedResult = cachedStrings.get(requestedId);
			if (cachedResult != null) {
				// The go-aheads are supposed to live only once. On the other
				// hand,
				// maybe for a re-run, this behavior is just annoying.
				// Removed for now...
				cachedStrings.remove(requestedId);
			}
		}

		if (cachedResult != null) {
			logger.fine("Service thread: Delivered a resource: " + requestedId);
			logger.fine("Service thread notifying validator thread about "
					+ requestedId);
			notify();
			return true;
		}

		logger.fine("Did not have data for input resource: " + requestedId);
		return false;
	}

	protected synchronized void doPresentNonFatalErrorsToEnvironment(
			List<XSLToolsException> errors) {
		this.nonFatalErrors = errors;
		setState(States.PRESENTING);
		diagnostics.append("doPresentNonFatalErrorsToEnvironment: presenting");
		notify();
	}

	/*
	 * Analyzer thread
	 */
	protected synchronized void doPresentValidationResultToEnvironment(
			String result) {
		setState(States.PRESENTING);
		diagnostics.append("doPresentValidationResultToEnvironment: presenting");
		notify();
	}

	/*
	 * Analyzer thread
	 */
	protected synchronized void doPresentValidationResultToEnvironment(
			ValidationResult result) {
		result.killDuplicates();
		this.validationResult = result;
		setState(States.PRESENTING);
		diagnostics.append("doPresentValidationResultToEnvironment: presenting");
		notify();
	}

	/*
	 * Analyzer thread
	 */
	public void earlyStreamRequest(String systemId, String user,
			int humanReadable) {
		String hrm = ResolutionContext.HUMAN_INTERFACE_STRINGS[humanReadable];
		earlyStreamRequests.add(new ReadableRequestBean(systemId, hrm, user,
				null));
	}

	/*
	 * Analyzer thread
	 */
	public void earlyStringRequest(String id, String user, String none,
			int humanReadable) {
		String hrm = ResolutionContext.HUMAN_INTERFACE_STRINGS[humanReadable];
		earlyStringRequests.add(new ReadableRequestBean(id, hrm, user, none));
	}

	/*
	 * Analyzer thread
	 */
	public synchronized void reset() {
		logger.info("Service thread resetting...");
		diagnostics.append("reset: start");
		super.reset();
		cachedResources.clear();
		cachedStrings.clear();
		earlyStreamRequests.clear();
		earlyStringRequests.clear();
		nonFatalErrors = null;
		validationResult = null;
		diagnostics.append("reset: end");
	}

	/*
	 * Analyzer thread
	 */
	protected synchronized void restart() {
		logger.info("Restarting...");
		diagnostics.append("restart: start");
		nonFatalErrors = null;
		validationResult = null;
		requestedId = null;
		requestedResourceTypeName = null;
		diagnostics.append("restart: end");
	}

	/*
	 * ??? thread
	 */
	public void uncacheResource(String systemId) {
		logger.info("Removed resource: " + systemId);
		taintInputSource(systemId);
		cachedResources.remove(systemId);
	}

	/*
	 * ??? thread
	 */
	public void uncacheString(String systemId) {
		logger.info("Removed string: " + systemId);
		cachedStrings.remove(systemId);
	}

	public boolean isHavingCachedResources() {
		return !cachedResources.isEmpty();
	}

	public boolean isHavingResult() {
		return (getState() != States.LOADING && getState() != States.ABORTING)
				&& validationResult != null;
	}

	public Collection<CachedWebResource> getCachedResources() {
		List<CachedWebResource> result = new ArrayList<CachedWebResource>(
				cachedResources.values());
		Collections.sort(result);
		return result;
	}

	public void zipCachedResources(OutputStream resultDumper)
			throws IOException {
		logger.info("zipCachedResources: start");
		Collection<CachedWebResource> resources = getCachedResources();
		ZipOutputStream zos = new ZipOutputStream(resultDumper);
		byte[] buf = new byte[1024];
		for (CachedWebResource res : resources) {
			String sid = res.getSystemId();
			String filename = sid.substring(sid.lastIndexOf('/') + 1, sid
					.length());
			ZipEntry ze = new ZipEntry(filename);
			zos.putNextEntry(ze);
			InputSource iso = res.getInputSource();
			InputStream is = iso.getByteStream();
			int ptr;
			while ((ptr = is.read(buf)) > 0) {
				zos.write(buf, 0, ptr);
			}
			zos.closeEntry();
		}
		zos.finish();
		logger.info("zipCachedResources: end");
	}

	public Collection<CachedWebString> getCachedStrings() {
		return cachedStrings.values();
	}

	public String escapeXML(String s) {
		return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(
				">", "&gt;");
	}

	public String getResourceAsString(String systemId) {
		logger.fine("getResourceAsString request: " + systemId);
		logger.fine("cachedResources: " + cachedResources);
		if (cachedResources.containsKey(systemId)) {
			CachedWebResource r = cachedResources.get(systemId);
			return r.getContents();
		}
		return "";
	}

	public String truncateUntilSolved(String s) {
		return s.substring(0, Math.min(s.length(), 150000));
	}

	public String getGeneratedXMLResourceAsString(String resourceId,
			DiagnosticsConfigurationOptions options) {
		logger.fine("getGeneratedXMLResourceAsString request: " + resourceId);
		if (generatedResources.containsKey(resourceId)) {
			Object o = generatedResources.get(resourceId);
			if (INPUT_TYPE.equals(resourceId)
					|| CONTROL_FLOW_GRAPH.equals(resourceId)) {
				return Dom4jUtil.diagnostics((Diagnoseable) o, options);
			}
		}
		return "";
	}

	public void getGeneratedResourceAsDot(String resourceId,
			PrintWriter resultDumper) throws IOException {
		logger.info("Session "+sessionKeyForLogging+" making DOT resource; request: " + resourceId + ": begin");
		if (SCHEMALESS_CONTROL_FLOW_GRAPH_AS_DOT.equals(resourceId)) {
			ControlFlowGraph cfg = (ControlFlowGraph) generatedResources
					.get(CONTROL_FLOW_GRAPH);
			if (cfg == null) {
				logger
						.warning("I was called, but my resource CONTROL_FLOW_GRAPH was not present!");
				return;
			}
			cfg.saveSchemalessDot(resultDumper);
		}

		if (CONTROL_FLOW_GRAPH_AS_DOT.equals(resourceId)) {
			ControlFlowGraph cfg = (ControlFlowGraph) generatedResources
					.get(CONTROL_FLOW_GRAPH);
			if (cfg == null) {
				logger
						.warning("I was called, but my resource CONTROL_FLOW_GRAPH was not present!");
				return;
			}
			cfg.saveDot(resultDumper);
		}

		if (TRANSFORM_TYPE_XML_GRAPH_AS_DOT.equals(resourceId)) {
			XMLGraph cfg = (XMLGraph) generatedResources
					.get(TRANSFORM_TYPE_XML_GRAPH);
			XMLGraph2Dot dotter = new XMLGraph2Dot(resultDumper);
			if (cfg == null) {
				logger
						.warning("I was called, but my resource TRANSFORM_TYPE_XML_GRAPH was not present!");
				return;
			}
			dotter.print(cfg);
		}

		if (OUTPUT_TYPE_XML_GRAPH_AS_DOT.equals(resourceId)) {
			XMLGraph cfg = (XMLGraph) generatedResources
					.get(OUTPUT_TYPE_XML_GRAPH);
			XMLGraph2Dot dotter = new XMLGraph2Dot(resultDumper);
			if (cfg == null) {
				logger
						.warning("I was called, but my resource TRANSFORM_TYPE_XML_GRAPH was not present!");
				return;
			}
			dotter.print(cfg);
		}
		logger.info("Session "+sessionKeyForLogging+" making DOT resource; request: " + resourceId + ": end");
	}

	public void getGeneratedResourceAsZip(String resourceId,
			OutputStream resultDumper, boolean storeOrigins) throws IOException {
		logger.info("Session "+sessionKeyForLogging+" making Zip resource; request: " + resourceId + ": begin");
		if (TRANSFORM_TYPE_XML_GRAPH.equals(resourceId)
				|| OUTPUT_TYPE_XML_GRAPH.equals(resourceId)) {
			if (!generatedResources.containsKey(resourceId)) {
				logger.warning("I was called, but my resource " + resourceId
						+ " was not present!");
				// it is not there; fizzle.
				return;
			}
			Serializer ser = new Serializer();
			XMLGraph xg = (XMLGraph) generatedResources.get(resourceId);
			ZipOutputStream zip = new ZipOutputStream(resultDumper);
			ser.store(xg, zip, storeOrigins);
			zip.finish();
		} else {
			logger
					.warning("I only work with TRANSFORM_TYPE_XML_GRAPH and OUTPUT_TYPE_XML_GRAPH; got "
							+ resourceId);
		}
		logger.info("Session "+sessionKeyForLogging+" making Zip resource; request: " + resourceId + ": end");
	}

	public void getGeneratedResourceAsImage(String resourceId,
			OutputStream resultDumper, String format) throws IOException {
		logger.info("Session "+sessionKeyForLogging+" generating image resource; request: " + resourceId + ": begin");

		if (SCHEMALESS_CONTROL_FLOW_GRAPH_AS_DOT.equals(resourceId)) {
			ControlFlowGraph cfg = (ControlFlowGraph) generatedResources
					.get(CONTROL_FLOW_GRAPH);
			if (cfg == null) {
				logger
						.warning("I was called, but my resource CONTROL_FLOW_GRAPH was not present!");
				return;
			}
			DotConverter dot = new DotConverter();
			dot.startGraphvizProcess(resultDumper, format);
			cfg.saveSchemalessDot(new OutputStreamWriter(dot.getOutputStream(),
					"utf-8"));
			dot.getOutputStream().close();
			dot.waitToComplete();
		}

		if (CONTROL_FLOW_GRAPH_AS_DOT.equals(resourceId)) {
			ControlFlowGraph cfg = (ControlFlowGraph) generatedResources
					.get(CONTROL_FLOW_GRAPH);
			if (cfg == null) {
				logger
						.warning("I was called, but my resource CONTROL_FLOW_GRAPH was not present!");
				return;
			}
			DotConverter dot = new DotConverter();
			dot.startGraphvizProcess(resultDumper, format);
			cfg.saveDot(new OutputStreamWriter(dot.getOutputStream(), "utf-8"));
			dot.getOutputStream().close();
			dot.waitToComplete();
		}

		if (TRANSFORM_TYPE_XML_GRAPH_AS_DOT.equals(resourceId)) {
			XMLGraph cfg = (XMLGraph) generatedResources
					.get(TRANSFORM_TYPE_XML_GRAPH);
			if (cfg == null) {
				logger
						.warning("I was called, but my resource TRANSFORM_TYPE_XML_GRAPH was not present!");
				return;
			}
			DotConverter dot = new DotConverter();
			PrintWriter pw = new PrintWriter(dot.getOutputStream()); // Achtung!
			// Default
			// encoding!
			XMLGraph2Dot dotter = new XMLGraph2Dot(pw);
			dot.startGraphvizProcess(resultDumper, format);
			dotter.print(cfg);
			pw.close();
			dot.waitToComplete();
		}

		if (OUTPUT_TYPE_XML_GRAPH_AS_DOT.equals(resourceId)) {
			XMLGraph cfg = (XMLGraph) generatedResources
					.get(OUTPUT_TYPE_XML_GRAPH);
			if (cfg == null) {
				logger
						.warning("I was called, but my resource OUTPUT_TYPE_XML_GRAPH was not present!");
				return;
			}
			DotConverter dot = new DotConverter();
			PrintWriter pw = new PrintWriter(dot.getOutputStream()); // Achtung!
			// Default
			// encoding!
			XMLGraph2Dot dotter = new XMLGraph2Dot(pw);
			dot.startGraphvizProcess(resultDumper, format);
			dotter.print(cfg);
			pw.close();
			dot.waitToComplete();
		}
		logger.info("Session "+sessionKeyForLogging+" generating image resource; request: " + resourceId + ": end");
	}

	public Stylesheet getSemamticsPreservedStylesheet() {
		return (Stylesheet) generatedResources
				.get(SEMANTICS_PRESERVED_STYLESHEET);
	}

	public Stylesheet getApproximatedStylesheet() {
		return (Stylesheet) generatedResources.get(APPROXIMATED_STYLESHEET);
	}

	public void pushMessage(String target, String message) {
		// diagnostics.append("pushMessage: target:" + target + ", message:" +
		// message + "<br/>");
		if ("autodetect".equals(target)) {
			shortLivedMessages.add(message);
		} else if ("flow-competition".equals(target)) {
			competitionMessages.add(message);
		}
	}

	public boolean isDisplayingSessionButtons() {
		States state = getState();
		return state == States.LOADING || state == States.TERMINATED
				|| state == States.PRESENTING || state == States.NOT_STARTED;
	}

	public boolean isDisplayingResetButton() {
		return true;
	}

	String annoSystemId;

	public void setAnnotatedSystemId(String id) {
		this.annoSystemId = id;
	}

	public String getAnnotatedSystemId() {
		return key + "lavOm:" + annoSystemId;
	}

	@Override
	public String getSessionType() {
		return "validation";
	}

	public boolean getSystemIdAppearsToBeURL() {
		String systemId = getRequestedFieldName();
		return systemId != null
				&& (systemId.startsWith("http://") || systemId
						.startsWith("ftp://"));
	}

	public List<String> getMessages() {
		List<String> result = new LinkedList<String>();
		result.addAll(shortLivedMessages);
		result.addAll(competitionMessages);
		return result;
	}

	public void clearShortLivedMessages() {
		shortLivedMessages.clear();
		// competitionMessages.clear();
	}

	protected void clearMessages() {
		competitionMessages.clear();
	}

	/**
	 * API towards validator
	 */
	@Override
	public synchronized InputSource resolveStream(String systemId,
			String userExplanation, int humanKey) throws IOException {

		diagnostics.append("resolveStream: " +  systemId);
		logger.info("Session " +sessionKeyForLogging+ " resolving stream: " + systemId + ": begin");

		if (getState() == States.ABORTING) {
			diagnostics.append("resolveStream: " + 
					"state was aborting, throwing an exception");
			throw new IOException("Session: "+sessionKeyForLogging+": User or timeout abort");
		}

		CachedWebResource cwr = cachedResources.get(systemId);

		while (cwr == null) {
			setState(States.LOADING);
			this.requestedId = systemId;
			this.requestedResourceTypeName = userExplanation;
			this.c_humanReadable = ResolutionContext.HUMAN_INTERFACE_STRINGS[humanKey];
			this.requestType = STREAM_REQUEST;

			// Wake up service thread
			notify();

			long time = System.currentTimeMillis();

			// Now wait for service thread to complete. After some time, we lose
			// patience and abort,
			// causing the validator thread to die.
			// Why the heck are we necessarily involving the Web thread in
			// this??
			try {
				wait(PATIENCE);
			} catch (InterruptedException ex) {
				// ensure that validator dies.
				setState(States.ABORTING);
				// hopefully kill validator thread.
				throw new IOException("Session: "+sessionKeyForLogging+": User or timeout abort");
			}

			if (System.currentTimeMillis() - time > PATIENCE) {
				setState(States.ABORTING);
				// hopefully kill validator thread.
				throw new IOException("Session: "+sessionKeyForLogging+": User or timeout abort");
			}

			// Reset control variables
			cwr = cachedResources.get(systemId);
		}
		this.requestedId = null;
		logger.info("Session " +sessionKeyForLogging+ " resolving stream: " + systemId + ": end");
		logger.finest("Content is:");
		logger.finest(cwr.getContents());
		return cwr.getInputSource();
	}

	public synchronized String resolveString(String id, String user,
			String none, int humanKey) throws IOException {

		logger.info("Session " +sessionKeyForLogging+ " resolving string: " + id + ": begin");

		if (getState() == States.ABORTING)
			throw new IOException("Session: "+sessionKeyForLogging+": User or timeout abort");

		boolean hasResult = cachedStrings.containsKey(id);
		Object result = cachedStrings.get(id);

		while (!hasResult) {
			setState(States.LOADING);
			this.requestedId = id;
			this.stringReqUserExplanation = user;
			this.stringReqNoneExplanation = none;
			this.requestType = STRING_REQUEST;
			this.c_humanReadable = ResolutionContext.HUMAN_INTERFACE_STRINGS[humanKey];

			// wake up service thread
			notify();

			long time = System.currentTimeMillis();

			try {
				wait(PATIENCE);
			} catch (InterruptedException ex) {
				setState(States.ABORTING);
				throw new IOException("Session: "+sessionKeyForLogging+": User or timeout abort");
			}
			if (System.currentTimeMillis() - time > PATIENCE) {
				setState(States.ABORTING);
				throw new IOException("Session: "+sessionKeyForLogging+": User or timeout abort");
			}

			hasResult = cachedStrings.containsKey(id);
			result = cachedStrings.get(id);

			// experiment: Do not cache strings, make them single use.
			cachedStrings.remove(id);
		}
		this.requestedId = null;

		logger.info("Session " +sessionKeyForLogging+ " resolving string: " + id + ": end");
		logger.finest("Content is:");
		logger.finest((result == null) ? null : result.toString());

		return (result == null) ? null : result.toString();
	}

	protected synchronized void startValidator() {
		super.startValidator(this);
		/*
		 * A hack for the problem: The validator is started, but the service
		 * thread quits before the validator has gotten as far as to make a
		 * request.
		 */
		try {
			wait(250);
		} catch (InterruptedException ex) {
		}
	}
}
