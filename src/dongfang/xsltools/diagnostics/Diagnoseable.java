/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-31
 */
package dongfang.xsltools.diagnostics;

import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;

/**
 * @author dongfang
 */
public interface Diagnoseable {
  /**
   * Make diagnosis structure.
   * 
   * @param parent -
   *          element to append diagnosis to
   * @param fac -
   *          a DocumentFactory
   */
  void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration);
}