package dongfang.xsltools.experimental.xsd;

import java.util.Collections;

import dk.brics.relaxng.Param;
import dk.brics.relaxng.converter.StandardDatatypes;

public class DatatypesBugTest {
  public static void main(String[] args) {
    StandardDatatypes stddt = new StandardDatatypes();

    Param p1 = new Param("whiteSpace", "preserve");
    Param p2 = new Param("whiteSpace", "replace");
    Param p3 = new Param("whiteSpace", "collapse");

    stddt.datatypeToAutomaton("http://www.w3.org/2001/XMLSchema-datatypes",
        "string", Collections.EMPTY_LIST);

    stddt.datatypeToAutomaton("http://www.w3.org/2001/XMLSchema-datatypes",
        "string", Collections.singletonList(p1));

    stddt.datatypeToAutomaton("http://www.w3.org/2001/XMLSchema-datatypes",
        "string", Collections.singletonList(p2));

    stddt.datatypeToAutomaton("http://www.w3.org/2001/XMLSchema-datatypes",
        "string", Collections.singletonList(p3));

  }
}
