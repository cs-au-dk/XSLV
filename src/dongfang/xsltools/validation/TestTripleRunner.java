/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.validation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.dom4j.Document;

import dongfang.xsltools.context.ValidationContextImpl;
import dongfang.xsltools.controlflow.ControlFlowConfiguration;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.controlflow.ControlFlowConfiguration.ControlFlowAlgorithm;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.diagnostics.ShowStopperErrorReporter;
import dongfang.xsltools.diagnostics.XMLPerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.resolver.URLResolutionContext;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.TestTriple;
import dongfang.xsltools.util.Util;

/**
 * A TODO could be: Do not run validation from here. Just be a shell for
 * XSLTValidatorMain; fire up its main method with the data in the triple as
 * arguments. Would be nicer.
 * 
 * @author dongfang
 */
public class TestTripleRunner extends ValidationContextImpl {

	private TestTriple triple;

	private static boolean dumpLightly = false;

	private static boolean dumpFully = false;

	private static boolean verboseProgress = false;

	private static final boolean traceStats = false;

	private static short wellDone = ValidationRun.VALIDATED;

	private static class Ticker extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException ex) {
				}
				displayResults();
			}
		}

		private void displayResults() {
			PerformanceLogger pa = DiagnosticsConfiguration.current
					.getPerformanceLogger();
			if (pa instanceof XMLPerformanceLogger) {
				XMLPerformanceLogger xps = (XMLPerformanceLogger) pa;
				Document stats = xps.getBothStats();
				try {
					Writer wr = new FileWriter("ticking.stats.xml");
					wr.write(Dom4jUtil.toDebugString(stats));
					wr.close();
				} catch (IOException ex) {
					System.err.println("Verdammt, ioException! " + ex);
				}
			}
		}
	}

	// Map<String, String> m = new HashMap<String, String>();

	public String getStylesheetIdentifier() {
		return triple.getStylesheetPrimaryModuleURI();
	}

	@Override
	public void earlyStreamRequest(String systemId, String user, int humanKey) {
	}

	public void earlyStringRequest(String id, String user, String none,
			int humanKey) {
	}

	public TestTripleRunner() {
		setResolver(new URLResolutionContext());
	}

	public boolean restarted() {
		return false;
	}

	public boolean isGoodTriple(File tripleFile) {
		if (!tripleFile.canRead())
			return false;
		try {
			URL url = tripleFile.toURL();
			triple = TestTriple.readTriple(url);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	public void runBatch(File dir, boolean onlyCheckResourcePresence,
			boolean waitEachTriple, int numRuns) throws Exception {
		File[] subs = dir.listFiles();
		for (int i = 0; i < subs.length; i++) {
			File sub = subs[i];
			if (sub.isDirectory()) {
				File tripleFile = new File(sub, "triple.xml");
				if (!tripleFile.canRead()) {
					System.err.println("No triple.xml in " + sub);
				} else {
					reset();
					URI uri = tripleFile.toURI();
					/*
					 * Oppps this seems to be a java bug: Filenames are not escaped when converting to URI.
					 */
					URL url = uri.toURL();
					triple = TestTriple.readTriple(url);
					if (!onlyCheckResourcePresence) {
						if (triple.isEnabled())
							run(numRuns);
						else
							System.out.println("Test " + triple.getName()
									+ " was not run as it was disabled");
					}
					if (waitEachTriple)
						new BufferedReader(new InputStreamReader(System.in))
								.readLine();
				}
			}
		}
	}

	public String getSchemaIdentifier(String s, short io) {
		if (io == INPUT)
			return triple.getInputSchemaURI();
		return triple.getOutputSchemaURI();
	}

	public String getRootElementNameIdentifier(String s, short io) {
		return s;
	}

	public String getNamespaceURIIdentifier(String s, short io) {
		return s;
	}

	public String resolveString(String id, String user, String none,
			int humanKey) {
		if (humanKey == INPUT_DTD_NAMESPACE_URI_KEY) {
			return triple.getInputDTDNamespaceURI();
		}
		if (humanKey == OUTPUT_DTD_NAMESPACE_URI_KEY) {
			return triple.getOutputDTDNamespaceURI();
		}
		if (humanKey == INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY) {
			return triple.getInputRootElementName();
		}
		if (humanKey == OUTPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY) {
			return triple.getOutputRootElementName();
		}
		throw new RuntimeException("Human key " + humanKey
				+ " -- what is that??");
	}

	public void pushMessage(String target, String message) {
		System.out.println(message + "-->" + target);
	}

	private void savePA(String filenamePrefix, PerformanceLogger pa, int numRunsEach)
			throws XSLToolsException {
		try {
			// just to avoid problems with the dummy logger.
			if (pa instanceof XMLPerformanceLogger) {
				Writer w = new FileWriter(filenamePrefix + ".xml");
				Document bothStats = ((XMLPerformanceLogger) pa).getBothStats();
				bothStats.getRootElement().addAttribute("name",
						triple.getName());
				bothStats.getRootElement().addAttribute("noRuns",
						Integer.toString(numRunsEach));
				w.write(Dom4jUtil.toDebugString(bothStats));
				w.close();
			}
		} catch (IOException ex) {
			throw new XSLToolsException(ex);
		}

		/*
		 * try { Writer w = new FileWriter(filenamePrefix + "Counters.xml");
		 * w.write(pa.getCounterStats()); w.close(); } catch (IOException ex) {
		 * throw new XSLToolsException(ex); }
		 */
	}

	public void run(int numRuns) throws XSLToolsException {
		// coupled with the analyzer actually used in ValidationAnalyzer!
		PerformanceLogger pa = DiagnosticsConfiguration.current
				.getPerformanceLogger();
		{
			pa.resetCounters();
			pa.resetTimers();

			String filterKind = ControlFlowConfiguration.current
					.candidateFilterAlgorithm() == ControlFlowAlgorithm.ABSTRACT_EVALUATION ? "MillFilter"
					: ControlFlowConfiguration.current
							.candidateFilterAlgorithm() == ControlFlowAlgorithm.INSENSITIVE_REGEXP ? "InsensitiveFilter"
							: "UnknownFilter";

			String dump = ("tmp/"
					+ /* "fastAlgorithm" + "/" + */triple.getName() + "." + filterKind);

			for (int i = 0; i < numRuns; i++) {
				System.out.println("Run # " + i);
				reset();
				pa.setValue("UsingMillFilter", "Configuration", 1);
				prun(pa /* , "fastAlgorithm" */);
			}
			savePA(dump, pa, numRuns);
		}
	}

	class Foo extends MinimalValidationRun {
		ControlFlowGraph xcfg;

		@Override
		public void setXcfg(ControlFlowGraph xcfg) {
			this.xcfg = xcfg;
		}

		void clear() {
			this.xcfg = null;
		}
	}

	public void prun(PerformanceLogger pa /* , String algoType */) {
		boolean busted = false;
		String reason = "";
		System.out.println("Running " + triple.getName());
		try {

			Foo foo = new Foo();

			ValidationRun vr = foo;

			vr = new StopShortDecorator(vr, wellDone);

			if (dumpLightly)
				vr = new LightValidationRunDumper(vr, "tmp/" /*
																 * + algoType +
																 * "/"
																 */
						+ triple.getName());

			else if (dumpFully)
				vr = new ValidationRunDumper(vr, "tmp/" /* + algoType + "/" */
						+ triple.getName());

			if (verboseProgress)
				vr = new VerboseRunDecorator(vr, System.out);

			XSLTValidator.validate(triple.getStylesheetPrimaryModuleURI(),
					this, new ShowStopperErrorReporter(), vr);

			if (vr.getProgress() == wellDone) {
				if (wellDone == ValidationRun.VALIDATED) {
					vr.getValidationResult().killDuplicates();
					Document report = vr.getValidationResult().getErrorReport();
					Dom4jUtil.debugPrettyPrint(report);
				}

				// System.err.println("With CG and input schema");
				// Runtime.getRuntime().gc();

				// pa.incrementCounter("MemoryWithCGAndInputSchema", "Memory",
				// (int) (Runtime.getRuntime().totalMemory() -
				// Runtime.getRuntime()
				// .freeMemory()));

				foo.clear();

				// System.err.println("With input schema alone");
				// Runtime.getRuntime().gc();
				// System.err.print(foo.xcfg == null ? "" : "");
				// pa.incrementCounter("MemoryWithInputSchema", "Memory", (int)
				// (Runtime
				// .getRuntime().totalMemory() -
				// Runtime.getRuntime().freeMemory()));
				// System.err.print(foo.xcfg == null ? "" : "");
				// System.err.print(inputTypes.get("fff") == null ? "" : "");
				// System.err.print(foo.xcfg == null ? "" : "");
				// System.err.print(inputTypes.get("fff") == null ? "" : "");

				reset();

				vr = null;
				foo = null;

				// System.err.println("Without input schema");
				// Runtime.getRuntime().gc();

				// pa.incrementCounter("MemoryEmptyHanded", "Memory", (int)
				// (Runtime
				// .getRuntime().totalMemory() -
				// Runtime.getRuntime().freeMemory()));

				pa.setValue("Busted", "Result", 0);
			} else {
				System.err
						.println("Validation did not complete, and no exceptions??!!!??");
				pa.setValue("Busted", "Result", 1);
			}
		} catch (Throwable t) {
			busted = true;
			reason = t.getMessage();
			pa.setValue("Busted", "Result", 1);
			t.printStackTrace();
		}
		System.out.println("  Triple "
				+ triple.getName()
				+ (busted ? (" busted because " + reason)
						: " completed w/o casualties"));
		System.out.println();
	}

	private static void usage() {
		System.err
				.println("Usage: "
						+ TestTripleRunner.class.getCanonicalName()
						+ " <triple-file-name> | <dir-name>  [-num-runs-each=n] [-only-test-files] [-wait-each-triple] [-verbose-progress] [-dump-lightly] [-dump-fully] [-only-xcfg] [-only-xmlg]");
		System.err
				.println("<triple-file-name> is the name of an xml file on the triple.xml format, OR");
		System.err
				.println("<dir-name> is the name of a directory whose immediate subdirectories are searched for files named triple.xml. These are each run (batch mode).");
		System.err
				.println("[-only-test-files]: Only test-read the triple file(s) and the files referenced; do not analyze or validate.");
		System.err
				.println("[wait-each-triple]: Require user response between triples in a batch.");
		System.err
				.println("[-num-runs-each=n]: Run each triple n times (for more accurate timing -- HotSpot compilers will warm up, JIT compilation time will contribute less, etc.");
		System.err
				.println("[-dump-lightly]: Dump some generated resources (in a dir named ./tmp)");
		System.err
				.println("[-dump-fully]: Dump a lot of generated resources (in a dir named ./tmp)");

		XSLTValidatorMain.commonSwitches();
		System.exit(-1);
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			usage();
		}

		if (traceStats) {
			Ticker t = new Ticker();
			t.setDaemon(true);
			t.start();
		}

		TestTripleRunner m = new TestTripleRunner();

		int numRuns = 1;
		
		boolean onlyTestFiles = XSLTValidatorMain.hasSwitch(args,
				"only-test-files");

		boolean waitEachTriple = XSLTValidatorMain.hasSwitch(args,
				"wait-each-triple");

		dumpLightly = XSLTValidatorMain.dumpLightly(args);

		dumpFully = XSLTValidatorMain.dumpFully(args);

		verboseProgress = XSLTValidatorMain.verboseProgress(args);

		wellDone = XSLTValidatorMain.fixupStop(wellDone, args);

		Map<String, String> parameterMappings = XSLTValidatorMain
				.parameterMappings(args, true);

		if (parameterMappings.containsKey("num-runs-each")) {
			numRuns = Integer.parseInt(parameterMappings
					.get("num-runs-each"));
		}

		String uriToTriple = Util.isURL(args[0]) ? args[0] : Util.toUrlString(args[0]);
		URI UriToTriple = new URI(uriToTriple);
		File sniffer = new File(UriToTriple);
		
		if (sniffer.isFile()) {
			m.triple = TestTriple.readTriple(new URL(uriToTriple));

			if (!onlyTestFiles) {
				m.run(numRuns);
			}

		} else if (sniffer.isDirectory()) {
			// first one is to warm up JVM
			if (numRuns > 1)
			m.runBatch(sniffer, onlyTestFiles, waitEachTriple, 1);
			m.runBatch(sniffer, onlyTestFiles, waitEachTriple, numRuns);
		} else {
			System.err.println(sniffer.getName()
					+ " is not a file and not a directory. Check name.");
			usage();
		}
	}
}
