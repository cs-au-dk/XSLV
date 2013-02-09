package dongfang.xsltools.controlflow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dongfang.xsltools.xmlclass.xslside.UndeclaredNodeType;

/*
 * A T x 2^Sigma gadget
 */
public class SchemalessFlow extends
    HashMap<TemplateRule, Set<UndeclaredNodeType>> {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1565268476435059422L;

public SchemalessFlow() {
    super();
    // TODO Auto-generated constructor stub
  }

  public SchemalessFlow(Map<? extends TemplateRule, ? extends Set<UndeclaredNodeType>> m) {
    super(m);
    // TODO Auto-generated constructor stub
  }

}
