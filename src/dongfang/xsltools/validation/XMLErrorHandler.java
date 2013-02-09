package dongfang.xsltools.validation;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dk.brics.misc.Origin;
import dk.brics.xmlgraph.ElementNode;
import dk.brics.xmlgraph.validator.ValidationErrorHandler;

public class XMLErrorHandler implements ValidationResult,
		ValidationErrorHandler {

	private LinkedList<ValidationError> errors = new LinkedList<ValidationError>();

	public boolean error(ElementNode arg0, Origin arg1, String arg2,
			String arg3, Origin arg4) {
		ValidationError error = new ValidationError(errors.size(), arg0, arg1,
				arg2, arg3, arg4);
		errors.add(error);
		return true;
	}

	public int getErrorCount() {
		return errors.size();
	}

	public Document getErrorReport() {
		Collections.sort(errors);
		DocumentFactory fac = DocumentFactory.getInstance();
		Element report = fac.createElement("errorReport");
		report.addAttribute("numErrors", Integer.toString(errors.size()));
		Document result = fac.createDocument(report);
		if (errors.isEmpty()) {
		} else
			for (ValidationError err : errors) {
				err.getErrorReport(report, fac);
			}
		return result;
	}

	@Override
	public String toString() {
		Collections.sort(errors);
		StringBuilder result = new StringBuilder();
		result.append("Totally " + errors.size() + " errors");
		int l = result.length();
		result.append("\n");
		for (int i = 0; i < l; i++)
			result.append('-');
		result.append("\n");
		Iterator<ValidationError> errs = errors.iterator();
		while (errs.hasNext()) {
			ValidationError err = errs.next();
			result.append(err.toString());
			if (errs.hasNext())
				result.append("---\n");
		}
		return result.toString();
	}

	public List<ValidationError> getValidationErrors() {
		return errors;
	}

	public boolean isValid() {
		return getErrorCount() == 0;
	}

	/*
	 * The duplicates killed are not always strutural duplicates, but the will appear at duplicates to the user all the same.
	 * This hack should really be removed and be replaced by some sort of flow back tracking on the cause of the error.
	 */
	public void killDuplicates() {
		//System.err.println("Pre duplicate kill: " + errors.size());
		Collections.sort(errors);
		ValidationError prev = null;
		ListIterator<ValidationError> erroriter = errors.listIterator();
		while (erroriter.hasNext()) {
			ValidationError next = erroriter.next();
			if (prev != null && prev.equals(next)) {
				erroriter.remove();
			} else {
				prev = next;
			}
		}
		//System.err.println("Post duplicate kill: " + errors.size());
	}
}
