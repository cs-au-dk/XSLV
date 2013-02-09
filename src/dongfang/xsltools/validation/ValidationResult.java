/*
 * dongfang M. Sc. Thesis
 * Created on 09-02-2005
 */
package dongfang.xsltools.validation;

import java.util.List;

import org.dom4j.Document;

/**
 * @author dongfang
 */
public interface ValidationResult {
  boolean isValid();

  int getErrorCount();

  Document getErrorReport();

  List<ValidationError> getValidationErrors();
  
  void killDuplicates();
}
