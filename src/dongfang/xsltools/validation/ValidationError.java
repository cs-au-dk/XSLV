package dongfang.xsltools.validation;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dk.brics.misc.Origin;
import dk.brics.xmlgraph.ElementNode;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.diagnostics.ParseLocationUtil;

public class ValidationError implements Comparable<ValidationError> {
	final ElementNode enode;

	final Origin origin;

	final String message;

	final String example;

	final Origin schema;

	final int seq;

	private final static String NA = "n/a";

	ValidationError(int seq, ElementNode enode, Origin origin, String message,
			String example, Origin schema) {
		this.seq = seq;
		this.origin = origin;
		this.enode = enode;
		this.message = message;
		this.example = example;
		this.schema = schema;
	}

	boolean contentEquals(Object o) {
		ValidationError ve = (ValidationError) o;
		if (origin == null && ve.origin != null)
			return false;
		if (origin != null && ve.origin == null)
			return false;
		if (origin != null && ve.origin != null)
			if (!origin.equals(ve.origin))
				return false;

		if (enode == null && ve.enode != null)
			return false;
		if (enode != null && ve.enode == null)
			return false;
		if (enode != null && ve.enode != null)
			if (!enode.equals(ve.enode))
				return false;

		if (message == null && ve.message != null)
			return false;
		if (message != null && ve.message == null)
			return false;
		if (message != null && ve.message != null)
			if (!message.equals(ve.message))
				return false;

		if (example == null && ve.example != null)
			return false;
		if (example != null && ve.example == null)
			return false;
		if (example != null && ve.example != null)
			if (!example.equals(ve.example))
				return false;

		if (schema == null && ve.schema != null)
			return false;
		if (schema != null && ve.schema == null)
			return false;
		if (schema != null && ve.schema != null)
			if (!schema.equals(ve.schema))
				return false;

		return true;
	}

	public String getMessage() {
		if (message != null)
			return message;
		return NA;
	}

	public String getExample() {
		if (example != null)
			return example;
		return NA;
	}

	public String getFilename() {
		if (schema == null)
			return NA;
		return schema.getFile();
	}

	public String getLine() {
		if (schema == null)
			return NA;
		return Integer.toString(schema.getLine());
	}

	public String getColumn() {
		if (schema == null)
			return NA;
		return Integer.toString(schema.getColumn());
	}

	public ParseLocation getParseLocation() {
		if (schema != null) {
			return ParseLocationUtil.getParseLocation(schema);
		}
		return null;
	}

	public Origin getCulpritOrigin() {
		if (enode == null)
			return origin;
		return enode.getOrigin();
	}

	public String getCulpritElementName() {
		if (enode != null) {
			String name = enode.getName().getShortestExample(true);
			if (name == null)
				name = "[multi-named]";
			return name;
		}
		return null;
	}

	public String getCulpritLineNumberAsString() {
		if (enode == null)
			return NA;
		Origin o = enode.getOrigin();
		return "" + o.getLine();
	}

	public String getCulpritColNumberAsString() {
		if (enode == null)
			return NA;
		Origin o = enode.getOrigin();
		return "" + o.getColumn();
	}

	public String getCulpritFilename() {
		if (enode == null)
			return NA;
		Origin o = enode.getOrigin();
		return "" + o.getFile();
	}

	void getErrorReport(Branch parent, DocumentFactory fac) {
		Element me = fac.createElement("error");
		parent.add(me);
		if (message != null) {
			Element dmessage = fac.createElement("message");
			dmessage.setText(message);
			me.add(dmessage);
		}
		if (example != null) {
			Element dexample = fac.createElement("example");
			dexample.setText(example);
			me.add(dexample);
		}
		if (schema != null)
			me.addAttribute("origin", schema.toString());
		Element denode = fac.createElement("element");
		me.add(denode);
		if (enode != null) {
			String name = enode.getName().getShortestExample(true);
			if (name == null)
				name = "???";
			denode.addAttribute("name", name);
			Origin eno = enode.getOrigin();
			if (eno != null) {
				denode.addAttribute("origin", eno.toString());
			}
		}
	}

	public int compareTo(ValidationError other) {
		int cr;
		if (schema != null && other.schema != null) {
			String s = schema.getFile();
			cr = s.compareTo(other.getFilename());
			if (cr != 0)
				return cr;
			cr = schema.getLine() - other.schema.getLine();
			if (cr != 0)
				return cr;
			cr = schema.getColumn() - other.schema.getColumn();
			if (cr != 0)
				return cr;
		}
		cr = message.compareTo(other.message);
		if (cr != 0)
			return cr;
		if (example != null && other.example != null) {
			cr = example.compareTo(other.example);
			if (cr != 0)
				return cr;
		}
		if (seq != other.seq) {
			return seq - other.seq;
		}
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("Error:   " + message + "\n");
		result.append("Example: " + example + "\n");
		result.append("Schema:  " + schema + "\n");
		if (enode != null) {
			result.append("@Content of: "
					+ enode.getName().getShortestExample(true) + "\n");
			if (enode.getOrigin() != null)
				result.append("             " + enode.getOrigin() + "\n");
		}
		return result.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		return contentEquals(o);
	}
}
