/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-21
 */
package dongfang.xsltools.controlflow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.CommentNT;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.PINT;
import dongfang.xsltools.xmlclass.xslside.TextNT;

/**
 * @author dongfang
 */
public class DeathFollowerUpper implements FlowAnalyzer {
  private static DeathFollowerUpper instance = new DeathFollowerUpper();

  public static DeathFollowerUpper getInstance() {
    return instance;
  }

  public void analyze(/*List<TemplateRule> liveRules,*/
      ControlFlowGraph xcfg,
      ValidationContext validationContext, TemplateRule _bootstrapTemplateRule,
      ContextMode __mode) {
    PerformanceLogger pa = DiagnosticsConfiguration.current
        .getPerformanceLogger();

Set<DeclaredNodeType> slamKloak = new HashSet<DeclaredNodeType >();    
    
    for (TemplateRule rule : xcfg.templateRules) {
      String isDefault = "";
      int n = rule.getModuleLevelNumber();
      if (n > 100000)
        isDefault = ".toBuiltinRules";
      if (!(rule.mode.toString().contains("dongfang"))) {
        if (rule.getAllModesContextSet().isEmpty() && !rule.pronouncedDead) {
          pa.incrementCounter("DeadRules" + isDefault, "Death");
        }
      }
      for (ApplyTemplatesInst apply : rule.applies) {
        List<DeadContextFlow> death = apply.getDeathCauses();
        for (DeadContextFlow dead : death) {      
          isDefault = "";
          n = dead.target.getModuleLevelNumber();
          if (n > 100000)
            isDefault = ".toBuiltinRules";
          int no = dead.getLostNodeTypes().size();
          String ged = dead.cause();
          pa.incrementCounter(ged + ".types" + isDefault, "Death", no);
          if (no == 0)
            pa.incrementCounter(ged + ".notypes" + isDefault, "Death");
          else pa.incrementCounter(ged + ".withtypes"+ isDefault, "Death");
        }
        
        
//  fusk!!!
        try {
          isDefault = "";
          n = rule.getModuleLevelNumber();
          if (n > 100000)
            isDefault = ".inBuiltinRules";
          
          SingleTypeXMLClass clazz = validationContext.getInputType(validationContext.getSchemaIdentifier("", ResolutionContext.INPUT));

          for (Selection sele : apply.selections) {
            if (clazz.possibleTargetNodes(sele.getOriginalPath()).isEmpty()) {
              pa.incrementCounter("TotallyAbsurdSelections", "Death");
                
            }
            
            slamKloak.addAll(sele.allTypesEverSelected);
            
          }
        } catch (Exception ex) {
          throw new AssertionError(ex);
        }
      }
    }

    try {

    slamKloak.addAll(validationContext.getInputType(validationContext.getSchemaIdentifier("", ResolutionContext.INPUT)).getValueOfTouchedTypes());
    slamKloak.remove(CommentNT.instance);
    slamKloak.remove(PINT.chameleonInstance);
    slamKloak.remove(TextNT.chameleonInstance);
    
    Set <DeclaredNodeType> ng = new HashSet<DeclaredNodeType>();

    int last;
    
    do {
      last = slamKloak.size();
      ng.clear();
      for (DeclaredNodeType t : slamKloak) {
   //     ng.add(t);
        t.runParentAxis(validationContext.getInputType(validationContext.getSchemaIdentifier("", ResolutionContext.INPUT)), ng);
      }
   //   slamKloak.clear();
      slamKloak.addAll(ng);
    } while (last < slamKloak.size());
    
    Set<DeclaredNodeType> unused = validationContext.getInputType(validationContext.getSchemaIdentifier("", ResolutionContext.INPUT)).
    unusedElementTypes(slamKloak);
    
    System.err.println("Not used: " + unused);
    
    pa.incrementCounter("unusedElementDecls", "Death", unused.size());
  } catch (Exception ex) {
    throw new RuntimeException(ex);
  }
  }
 
  public void _analyze(List<TemplateRule> liveRules,
      ValidationContext validationContext, TemplateRule bootstrapTemplateRule,
      ContextMode _mode) {
    for (TemplateRule rule : liveRules) {
      for (ApplyTemplatesInst apply : rule.applies) {
        List<DeadContextFlow> death = apply.getDeathCauses();
        for (DeadContextFlow dead : death) {
          TemplateRule target = dead.target;
          System.out.println(this + "\nin " + apply.containingRule + "\nlost "
              + dead.lostNodeTypes.size() + " flows to \n" + target
              + "\nbecause of " + dead.cause() + "\n\n");
        }
      }
    }
  }
}