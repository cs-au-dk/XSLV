package dongfang.xsltools.xmlclass.xsd;

import java.io.Reader;
import java.io.StringReader;

/**
 * An approximastion of a declaration of xs:anyType.
 * 
 * @author dongfang
 */
public class XSDAnyType {
  final static String it = "<schema xmlns='http://www.w3.org/2001/XMLSchema' "
      + "targetNamespace='http://www.w3.org/2001/XMLSchema' "
      + "elementFormDefault='qualified'>"
      + "<complexType name='anyType' mixed='true'>" + "<sequence>"
      + "<any minOccurs='0' maxOccurs='unbounded'/>" + "</sequence>"
      + "<anyAttribute/>" + "</complexType>" + "</schema>";

  public static Reader getReader() {
    return new StringReader(it);
  }
}
