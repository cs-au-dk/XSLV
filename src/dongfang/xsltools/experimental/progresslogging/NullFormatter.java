/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.experimental.progresslogging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author dongfang
 */
public class NullFormatter extends Formatter {

  /*
   * (non-Javadoc)
   * 
   * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
   */
  @Override
public String format(LogRecord record) {
    return record.getMessage() + "\n";
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.logging.Formatter#formatMessage(java.util.logging.LogRecord)
   */
  @Override
public synchronized String formatMessage(LogRecord record) {
    return super.formatMessage(record);
  }
}