package dongfang.xsltools.controlflow;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Branch;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dk.brics.xmlgraph.Node;
import dk.brics.xmlgraph.XMLGraph;
import dongfang.xsltools.context.ValidationContext;
import dongfang.xsltools.diagnostics.Diagnoseable;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.ErrorReporter;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.Util;
import dongfang.xsltools.xmlclass.schemaside.ContentOrder;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NodeType;
import dongfang.xsltools.xmlclass.xslside.RootNT;

/**
 * It is very quiet around here.... most stuff is really for .dot output (and it should be moved to some
 * other place....)
 * @author dongfang
 */
public class ControlFlowGraph implements Diagnoseable, Cloneable {

  public final List<TemplateRule> templateRules;

  private Selection rootSelection;

  void setRootSelection(Selection s) {
    this.rootSelection = s;
  }

  /**
   * Construct control flow graph (it is ok this one time that so
   * many things happen in the constructor....)
   * @param validationContext
   * @param stylesheet
   * @param cesspool
   * @throws XSLToolsException
   */
  public ControlFlowGraph(ValidationContext validationContext,
      Stylesheet stylesheet, ErrorReporter cesspool) throws XSLToolsException {
    
    TemplateRuleExtractor extractor = new TemplateRuleExtractor(cesspool);
    
    extractor.process(stylesheet);
    
    this.templateRules = extractor.getAllTemplateRules();
    
    DiagnosticsConfiguration.current.getPerformanceLogger().setValue(
        "NumTemplates", "Stylesheet", this.templateRules.size());
  }

  private static String contextSetToString(Collection<? extends NodeType> contextSet) {
    // Create a manageable context set string (they get too long):
    if (contextSet == null)
      return "{}";

    String contextSetStr = "{";

    Iterator<? extends NodeType> contextIter = contextSet.iterator();
    int lineNodeCount = 0;

    while (contextIter.hasNext()) {
      NodeType node = contextIter.next();
      contextSetStr += node.toLabelString();
      // Comma?
      if (contextIter.hasNext()) {
        contextSetStr += ",";

        // Newline?
        lineNodeCount++;

        if (lineNodeCount >= 3) {
          contextSetStr += "\\n";
          lineNodeCount = 0;
        }
      }
    }
    contextSetStr += "}";
    return contextSetStr;
  }

  private static String modeListToString(List<? extends ContextMode> modeSet) {
    // Create a manageable context set string (they get too long):
    if (modeSet == null)
      return "{}";

    String modeSetStr = "{";

    Iterator<? extends ContextMode> modeIter = modeSet.iterator();
    int lineNodeCount = 0;

    while (modeIter.hasNext()) {
      ContextMode mode = modeIter.next();
      modeSetStr += mode.toString();
      // Comma?
      if (modeIter.hasNext()) {
        modeSetStr += ",";

        // Newline?
        lineNodeCount++;

        if (lineNodeCount >= 3) {
          modeSetStr += "\\n";
          lineNodeCount = 0;
        }
      }
    }
    modeSetStr += "}";
    return modeSetStr;
  }

  public void saveDot(Writer out) throws IOException {
    // Clear statistics vars:
    int nodeCount = 0;
    int edgeCount = 0;

    // Open file for text writing:
    //FileWriter out = new FileWriter(filename);

    // Dot prefix:
    out.write("digraph XCFG {\n");

    // Create a node for each template rule:
    Iterator iter = templateRules.iterator();

    while (iter.hasNext()) {
      TemplateRule rule = (TemplateRule) iter.next();

      if (!rule.getModedContextSet().isEmpty()) {

        String contextSetStr = "";// rule.getModedContextSet().keySet().toString();

        // Construct label:
        String label = rule.toLabelString();

        for (ContextMode mode : rule.getModedContextSet().keySet()) {

          contextSetStr += mode + contextSetToString(rule.getContextSet(mode));
        }
        // Create a manageable context set string (they get too long):

        label += "\\n" + contextSetStr;

        // Decide shape:
        String shape;

        // if (rule.mode.getNamespacePrefix().startsWith("df"))
        // shape = "octagon";
        // really means: if it is a default template rule.
        // Could check instead that is comes from the stylesheet
        // body .. if, that is, nothing else is stuffed there.
        // else if (rule.priority == lowestPriority)
        // shape = "octagon";
        // else
        shape = "ellipse";

        // Create node:
        out.write("  rule_" + rule.index + " [label=\"" + label + "\" shape="
            + shape + "]\n");

        // Add to statistics:
        nodeCount++;
      }
    }

    // Root node:
    out.write("  root [shape=plaintext label=\"Initial\\nSelection\"]\n");

    // Create edges:
    Iterator<TemplateRule> ruleIter = templateRules.iterator();

    while (ruleIter.hasNext()) {
      TemplateRule rule = ruleIter.next();

      // For each instruction:
      Iterator<ApplyTemplatesInst> instIter = rule.applies.iterator();
      int instIndex = 0;

      while (instIter.hasNext()) {
        ApplyTemplatesInst apply = instIter.next();

        // For each selection:
        Iterator selIter = apply.selections.iterator();
        int selIndex = 0;

        while (selIter.hasNext()) {
          Selection sel = (Selection) selIter.next();

          // Merge flows to increase readability:

          for (ContextMode mode : sel.getAllModes()) {

            Set<TemplateRule> possibleTargets = sel.getAllTargetsForMode(mode);

            // For each target:
            Iterator<TemplateRule> targetIter = possibleTargets.iterator();

            while (targetIter.hasNext()) {
              TemplateRule target = targetIter.next();

              // Construct taillabel:
              String taillabel = "";

              if (apply.selections.size() > 1)
                taillabel += (instIndex + 1);

              if (apply.selections.size() > 1)
                taillabel += "." + (selIndex + 1);

              // Fetch context flow data:
              Map<Set<NodeType>, Set<? extends NodeType>> flowMap = new HashMap<Set<NodeType>, Set<? extends NodeType>>();

              // for (ContextMode mode : rule.getModedContextSet().keySet()) {

              Iterator<? extends DeclaredNodeType> contextIter = rule
                  .getContextSet(mode).iterator();

              while (contextIter.hasNext()) {
                DeclaredNodeType contextType = contextIter.next();

                // Add context flow to flow map:
                ContextFlow flow = sel.getContextFlow(mode, contextType);

                if (flow != null && !flow.isEmpty()) {

                  Set<? extends NodeType> contextFlow = flow.get(target);

                  if (contextFlow != null && !contextFlow.isEmpty()) {
                    // See of there is a flow like this already:
                    Iterator<Set<NodeType>> fromSetEnum = flowMap.keySet()
                        .iterator();
                    boolean mergedWithOtherSet = false;

                    while (fromSetEnum.hasNext()) {
                      Set<NodeType> fromSet = fromSetEnum.next();
                      Set<? extends NodeType> targetSet = flowMap.get(fromSet);

                      // If equals, add node to from:
                      if (contextFlow.equals(targetSet)) {
                        // Remove:
                        flowMap.remove(fromSet);

                        // Add to from set:
                        fromSet.add(contextType);

                        // Re-insert:
                        flowMap.put(fromSet, targetSet);

                        mergedWithOtherSet = true;
                        break;
                      }
                    }
                    // Add to map:
                    if (!mergedWithOtherSet) {
                      Set<NodeType> fromSet = new HashSet<NodeType>();
                      fromSet.add(contextType);
                      flowMap.put(fromSet, contextFlow);
                    }
                  }
                }
              }

              // Construct label:
              assert (!rule.getModedContextSet().isEmpty()) : "Empty context set! This edge should not be drawn!";

              String label = "";

              Iterator<Set<NodeType>> fromSetEnum = flowMap.keySet().iterator();

              while (fromSetEnum.hasNext()) {
                Set<NodeType> fromSet = fromSetEnum.next();
                Set<? extends NodeType> targetSet = flowMap.get(fromSet);

                // Add context flow to label:
                String fromSetStr = contextSetToString(fromSet);
                String targetSetStr = contextSetToString(targetSet);

                fromSetStr = fromSetStr.replaceAll("\\\\n", "\\\\l  ");
                targetSetStr = targetSetStr.replaceAll("\\\\n",
                    "\\\\l            ");

                //label += mode + fromSetStr + "->" + targetSetStr + "\\l";
                label += fromSetStr + "->" + targetSetStr + "\\l";
              }

              assert (label.length() > 0) : "No flow along edge??? Then it shouldn't be there at all!";

              // Create edge:
              out.write("  rule_" + rule.index + " -> rule_" + target.index);
              out.write(" [label=\"" + label + "\" fontsize=10 taillabel=\""
                  + taillabel + "\" labelfontsize=10]");
              out.write("\n");

              // Add to statistics:
              edgeCount++;
            }
          }
          selIndex++;
        } // while (selIter.hasNext())

        instIndex++;
      } // while (instIter.hasNext())
    } // while (ruleIter.hasNext())

    // Merge root flows to increase readability:
    Set<TemplateRule> possibleTargets = rootSelection
        ./*getContextSensitiveTargetSet()*/getAllTargets();

    // Root edges:
    Iterator targetIter = possibleTargets.iterator();

    while (targetIter.hasNext()) {
      TemplateRule target = (TemplateRule) targetIter.next();
      // logger.fine("root TARGET");

      // Create edge:
      out.write("  root -> rule_" + target.index + "\n");

      // Add to statistics:
      edgeCount++;
    }

    // Dot suffix:
    out.write("}\n");

    // Close file:
    out.close();
  }

  public void saveSchemalessDot(Writer out) throws IOException {
    // Clear statistics vars:
    int nodeCount = 0;
    int edgeCount = 0;

    // Open file for text writing:
    // FileWriter out = new FileWriter(filename);

    // Dot prefix:
    out.write("digraph XCFG {\n");

    // Create a node for each template rule:

    for (TemplateRule rule : templateRules) {
      // if (!rule.getSchemalessContextSet().isEmpty()) {

      // Create a manageable context set string (they get too long):
      String contextSetStr = contextSetToString(rule.getSchemalessContextSet());

      // Construct label:
      String label = rule.toLabelString();

      label += "\\n" + contextSetStr;

      // Decide shape:
      String shape;

      // if (rule.mode.getNamespacePrefix().startsWith("df"))
      // shape = "octagon";
      // really means: if it is a default template rule.
      // Could check instead that is comes from the stylesheet
      // body .. if, that is, nothing else is stuffed there.
      // else if (rule.priority == lowestPriority)
      // shape = "octagon";
      // else
      shape = "ellipse";

      // Create node:
      out.write("  rule_" + rule.index + " [label=\"" + label + "\" shape="
          + shape + "]\n");

      // Add to statistics:
      nodeCount++;
      // }
    }

    // Root node:
    out.write("  root [shape=plaintext label=\"Initial\\nSelection\"]\n");

    // Create edges:
    for (TemplateRule rule : templateRules) {

      for (ContextMode mode : rule.getContextModeSet()) {

        // For each instruction:
        int instIndex = 0;

        for (ApplyTemplatesInst apply : rule.applies) {

          // For each selection:
          int selIndex = 0;

          for (Selection sel : apply.selections) {

            // Merge flows to increase readability:
            Set<TemplateRule> possibleTargets = sel
                .getContextInsensitiveTargetSet(mode);

            // For each target:
            Iterator<TemplateRule> targetIter = possibleTargets.iterator();

            while (targetIter.hasNext()) {
              TemplateRule target = targetIter.next();

              // Construct taillabel:
              String taillabel = "";

              if (apply.selections.size() > 1)
                taillabel += (instIndex + 1);

              if (apply.selections.size() > 1)
                taillabel += "." + (selIndex + 1);

              // Fetch context flow data:

              // Construct label:

              String label = "";

              // Iterator<Set<NodeType>> fromSetEnum =
              // flowMap.keySet().iterator();

              Set<? extends NodeType> targetSet = sel
                  .getContextInsensitiveEdgeFlow(mode, target);

              // Add context flow to label:
              String targetSetStr = contextSetToString(targetSet);

              targetSetStr = targetSetStr.replaceAll("\\\\n",
                  "\\\\l            ");

              //label = mode + "->" + apply.getMode().contextualize(mode) + ": "
              //    + targetSetStr + "\\l";

              label = targetSetStr + "\\l";

              // Create edge:
              out.write("  rule_" + rule.index + " -> rule_" + target.index);
              out.write(" [label=\"" + label + "\" fontsize=10 taillabel=\""
                  + taillabel + "\" labelfontsize=10]");
              out.write("\n");

              // Add to statistics:
              edgeCount++;
            }

            selIndex++;
          } // while (selIter.hasNext())

          instIndex++;
        } // while (instIter.hasNext())
      }
    } // while (ruleIter.hasNext())

    // Merge root flows to increase readability:
    Set<TemplateRule> possibleTargets = rootSelection
        ./*getContextSensitiveTargetSet()*/getAllTargets();

    // Root edges:
    Iterator targetIter = possibleTargets.iterator();

    while (targetIter.hasNext()) {
      TemplateRule target = (TemplateRule) targetIter.next();
      // logger.fine("root TARGET");

      // Create edge:
      out.write("  root -> rule_" + target.index + "\n");

      // Add to statistics:
      edgeCount++;
    }

    // Dot suffix:
    out.write("}\n");

    // Close file:
    out.close();
  }

  public void saveModeCompatibilityDot(String filename) throws IOException {
    // Clear statistics vars:
    int nodeCount = 0;
    int edgeCount = 0;

    // Open file for text writing:
    FileWriter out = new FileWriter(filename);

    // Dot prefix:
    out.write("digraph XCFG {\n");

    // Create a node for each template rule:
    Iterator<TemplateRule> iter = templateRules.iterator();

    while (iter.hasNext()) {
      TemplateRule rule = iter.next();

      // if (!rule.getContextModeSet().isEmpty()) {

      // Construct label:
      String label = rule.toLabelString();

      // Decide shape:
      String shape;

      // if (rule.mode.getNamespacePrefix().startsWith("df"))
      // shape = "octagon";
      // really means: if it is a default template rule.
      // Could check instead that is comes from the stylesheet
      // body .. if, that is, nothing else is stuffed there.
      // else if (rule.priority == lowestPriority)
      // shape = "octagon";
      // else
      shape = "ellipse";

      // Create node:
      out.write("  rule_" + rule.index + " [label=\"" + label + "\" shape="
          + shape + "]\n");

      // Add to statistics:
      nodeCount++;
      // }
    }

    // Root node:
    out.write("  root [shape=plaintext label=\"Initial Mode\"]\n");

    // Create edges:
    Iterator<TemplateRule> ruleIter = templateRules.iterator();

    while (ruleIter.hasNext()) {
      TemplateRule rule = ruleIter.next();

      // For each instruction:
      Iterator<ApplyTemplatesInst> instIter = rule.applies.iterator();

      int applyIndex = 0;

      while (instIter.hasNext()) {
        ApplyTemplatesInst apply = instIter.next();

        // Merge flows to increase readability:
        // For each target:

        for (TemplateRule target : apply.getModeInsensitiveTargetSet()) {

          // Construct taillabel:
          String taillabel = "apply-";

          taillabel += (applyIndex + 1);

          // Fetch context flow data:
          // Construct label:

          String label = "";

          // Iterator<Set<NodeType>> fromSetEnum =
          // flowMap.keySet().iterator();

          List<ContextMode> modeSet = apply.getContextModesFor(target);

          // Add context flow to label:
          String fromSetStr = modeListToString(modeSet);

          apply.contextualize(modeSet);

          String modeSetStr = modeListToString(modeSet);

          fromSetStr = fromSetStr.replaceAll("\\\\n", "\\\\l            ");

          modeSetStr = modeSetStr.replaceAll("\\\\n", "\\\\l            ");

          label = taillabel + ":" + fromSetStr + "->" + modeSetStr + "\\l";

          // Create edge:
          out.write("  rule_" + rule.index + " -> rule_" + target.index);
          out.write(" [label=\"" + label + "\" fontsize=10 labelfontsize=10]");
          out.write("\n");

          // Add to statistics:
          edgeCount++;
        } // while (selIter.hasNext())
        applyIndex++;
      }
    } // while (instIter.hasNext())

    // Merge root flows to increase readability:
    Set<TemplateRule> possibleTargets = rootSelection
        ./*getContextSensitiveTargetSet()*/getAllTargets();

    // Root edges:
    Iterator targetIter = possibleTargets.iterator();

    while (targetIter.hasNext()) {
      TemplateRule target = (TemplateRule) targetIter.next();
      // logger.fine("root TARGET");

      // Create edge:
      out.write("  root -> rule_" + target.index + "\n");

      // Add to statistics:
      edgeCount++;
    }

    // Dot suffix:
    out.write("}\n");

    // Close file:
    out.close();
  }

  /**
   * Should check: 
   * 1) All context flows on edges that go to some template, are
   * reflected in context set of that template
   * 2) All types in a template's context set are on at
   * least one incoming edge (the converse).
   */
  public void sanityCheckFlows() {
    ContextMode bootstrapMode = DefaultTemplateMode.instance;
    DeclaredNodeType bootstrapType = RootNT.instance;

    Map<ContextMode, Map<TemplateRule, Set<DeclaredNodeType>>> inferredContextSets = new HashMap<ContextMode, Map<TemplateRule, Set<DeclaredNodeType>>>();

    rootSelection.inferContextSets(bootstrapMode, inferredContextSets, null);

    // these are the entry templates,
    // it's OK that no template rules emit these
    // flows.
    Set<TemplateRule> ignoreRootNTFlow = rootSelection.getAllTargets();

    for (TemplateRule t : templateRules) {
      for (ContextMode mode : t.getContextModeSet()) {

        Collection<DeclaredNodeType> forMode;

        if ((forMode = t.getContextSet(mode)) == null) {
          forMode = new HashSet<DeclaredNodeType>();
        }

        Set<? extends DeclaredNodeType> clone = new HashSet<DeclaredNodeType>(
            forMode);

        Map<TemplateRule, Set<DeclaredNodeType>> simForMode = inferredContextSets
            .get(mode);
        Set<DeclaredNodeType> simContextSet = simForMode == null ? null
            : simForMode.get(t);

        if (simContextSet == null) {
          simContextSet = new HashSet<DeclaredNodeType>();
        }

        clone.removeAll(simContextSet);

        if (!clone.isEmpty()) {
          boolean isEntry = false;
          if (clone.size() == 1) {
            DeclaredNodeType testRoot = clone.iterator().next();
            if (testRoot.equals(bootstrapType) && ignoreRootNTFlow.contains(t))
              isEntry = true;
          }
          if (!isEntry) {
            System.err.println("Too large context set in " + t);
            System.err
                .println("These types were not present on incoming edges: "
                    + clone);
          }
        }
      }
    }
  }

  public XMLGraph constructSummaryGraph(SingleTypeXMLClass clazz)
      throws XSLToolsException {

    // PerformanceAnalyzer pa =
    // DiagnosticsConfiguration.current.getPerformanceAnalyzer();

    XMLGraph result = new XMLGraph();

    ContextMode bootstrapMode = DefaultTemplateMode.instance;

    DeclaredNodeType bootstrapType = RootNT.instance;

    // det ondeste hack ...
    SGFragment global = new SGFragment(result, "global");
    Node root = global.createPlaceholder(bootstrapType);
    result.addRoot(root);
    global.setEntryNode(root);

    Set<TemplateRule> roots = rootSelection.getAllTargetsForMode(bootstrapMode);
    // pa.startTimer("Construction", "Construction");
    for (TemplateRule rule : roots) {
      rule.constructSGFragments(bootstrapMode, bootstrapType, clazz, result,
          true);
    }

    // hookup root selection stuff
    rootSelection.constructSGSubgraph(clazz, bootstrapMode, RootNT.instance,
        global, ContentOrder.FORWARD, false, -1);

    Map<ContextMode, Set<TemplateRule>> done = new HashMap<ContextMode, Set<TemplateRule>>();

    // pa.stopTimer("Construction", "Construction");
    // pa.startTimer("Assembly", "SGConstruction");

    rootSelection.hookupFragments(bootstrapMode, global, null, done);

    // pa.stopTimer("Assembly", "SGConstruction");

    return result;
  }

  public void annotateContextSensitiveFlows(Stylesheet target, int version) {
    DocumentFactory fac = new DocumentFactory();
    for (TemplateRule rule : templateRules) {
      rule.annotateContextSensitiveFlows(target, version, fac);
    }
  }

  public void annotateContextInsensitiveFlows(Stylesheet target, int version) {
    DocumentFactory fac = new DocumentFactory();
    for (TemplateRule rule : templateRules) {
      rule.annotateContextInsensitiveFlows(target, version, fac);
    }
  }
  
  public void diagnostics(Branch parent, DocumentFactory fac, Set<Object> configuration) {
    Element me = fac.createElement(Util.capitalizedStringToHyphenString(getClass()));
    parent.add(me);
    me.addAttribute("template-rule-count", Integer.toString(templateRules.size()));
    Element rs = fac.createElement("root-selection");
    me.add(rs);
    if (rootSelection != null)
      rootSelection.diagnostics(rs, fac, configuration);
    else
      rs.addText("null");
    Dom4jUtil.collectionDiagnostics(me, templateRules, "template-rules", fac, configuration);
  }
}
