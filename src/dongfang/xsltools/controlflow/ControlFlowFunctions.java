/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-13
 */
package dongfang.xsltools.controlflow;

import java.util.Comparator;

/**
 * @author dongfang
 */
public class ControlFlowFunctions {
  private final static Comparator<TemplateRule> order = new Comparator<TemplateRule>() {
    public int compare(TemplateRule r1, TemplateRule r2) {
      int hakke = r1.compareTo(r2);
      if (hakke != 0)
        return -hakke; // highest priority first.
      return r1.index - r2.index;
    }
  };

  public static Comparator<TemplateRule> getTemplateDiagnosticsOrdering() {
    return order;
  }
  
  private final static Comparator<TemplateRule> appearanceOrder = new Comparator<TemplateRule>() {
    public int compare(TemplateRule r1, TemplateRule r2) {
      return r1.index - r2.index;
    }
  };

  public static Comparator<TemplateRule> getAppearanceOrdering() {
    return appearanceOrder;
  }

  private final static Comparator<ApplyTemplatesInst> invokerAppearanceOrder = 
    new Comparator<ApplyTemplatesInst>() {
    public int compare(ApplyTemplatesInst r1, ApplyTemplatesInst r2) {
      int i1 = r1.containingRule == null ? -1 : r1.containingRule.index;
      int i2 = r2.containingRule == null ? -1 : r2.containingRule.index;
      if (i1 != i2)
        return i1 - i2;
      if (r1.getOriginalLocation()!=null && r2.getOriginalLocation() != null) {
        int x = r1.getOriginalLocation().elementStartTagBeginningLine()
        - r2.getOriginalLocation().elementStartTagBeginningLine();
        if (x != 0)
          return x;
        return 1;
      }
      return 1;
    }
  };

  public static Comparator<ApplyTemplatesInst> getInvokerAppearanceOrdering() {
    return invokerAppearanceOrder;
  }
}
