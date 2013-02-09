package dongfang.xsltools.xmlclass;
/*
public class SGFragment {
   * public String name;
   * 
   * public List<Node> roots;
   * 
   * public Map<String, List<Node>> gapToNodes; // Gap Name -> SG Node
   * 
   * public SGFragment() { name = "N/A"; roots = new LinkedList<Node>();
   * gapToNodes = new HashMap<String, List<Node>>(); } / ** Add each gap->Node
   * in the recieved map to the internal map. / public void addToMap(Map<String,
   * List<Node>> otherMap) { Iterator<String> numse =
   * otherMap.keySet().iterator();
   * 
   * while (numse.hasNext()) { String gap = numse.next(); // Fetch other node
   * list List<Node> otherList = otherMap.get(gap); // Fetch local node list:
   * List<Node> localList = gapToNodes.get(gap); // Create new list of none was
   * present: if (localList == null) { localList = new LinkedList<Node>();
   * gapToNodes.put(gap, localList); } // Add each entry:
   * localList.addAll(otherList); } } / ** Add each gap in the recieved SG node
   * to the internal map. / public void addToMap(Node otherNode) { Set gaps =
   * otherNode.getTemplate().getTemplateGaps();
   * 
   * Iterator gapIter = gaps.iterator();
   * 
   * while (gapIter.hasNext()) { String gap = (String) gapIter.next(); // Fetch
   * local node list: List<Node> localList = gapToNodes.get(gap); // Create new
   * list of none was present: if (localList == null) { localList = new
   * LinkedList<Node>(); gapToNodes.put(gap, localList); } // Add an entry:
   * localList.add(otherNode); // System.out.println("Content model fragment:
   * Gap '"+gap+"' added!"); } }
   * 
   * public static String nodeToGapName(NodeType node) { if (node instanceof
   * RootNT) return "root"; else if (node instanceof TextNT) return "pcdata";
   * else if (node instanceof CommentNT || node instanceof PINT) return
   * "comment_or_pi"; else if (node instanceof ElementNT) return "elm_" +
   * ((ElementNT) node).getColonTranslatedQualifiedName(); else if (node
   * instanceof AttributeNT) return "att_" + ((AttributeNT)
   * node).getColonTranslatedOwnerElementName() + "_" + ((AttributeNT)
   * node).getColonTranslatedQualifiedName(); return null; }
   * 
   * public void plug(String gapName, Node targetSGNode) { // Find all the nodes
   * with such a gap: // System.out.println(gapName); //
   * System.out.println(gapToNodes); LinkedList nodeList = (LinkedList)
   * this.gapToNodes.get(gapName);
   * 
   * assert (nodeList != null && nodeList.size() > 0) : "Could not find nodes
   * with gap '" + gapName + "' in fragment '" + name + "'!"; // Add SG edge
   * from each of the nodes to targetSGNode: Iterator<Node> nodeIter =
   * nodeList.iterator();
   * 
   * while (nodeIter.hasNext()) { Node contentModelNode = nodeIter.next(); //
   * Add SG edge: // dongfang: OUCH! Was is that, static method???
   * ControlFlowFunctions.addSGEdge(contentModelNode, gapName, targetSGNode); } }
   * 
   * public void plugInstantiation(NodeType node, Node targetSGNode) { //
   * System.out.println("PLUGGING "+node+" with "+targetSGNode); // Construct
   * the instantiation gap name: String instGapName = "inst_" +
   * nodeToGapName(node); // Add instantiation to each of the nodes:
   * plug(instGapName, targetSGNode); }
}
*/