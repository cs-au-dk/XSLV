package dongfang.xsltools.experimental.progresslogging;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ProgressLoggerFormatter extends Formatter {
	Formatter nf = new NullFormatter();
	
	@Override
	public String format(LogRecord record) {
		// TODO Auto-generated method stub
		String s = nf.format(record);
		s = new Date().toString() + ": " + s;
		// System.out.println("Should log: " + s);
		return s;
	}
}
