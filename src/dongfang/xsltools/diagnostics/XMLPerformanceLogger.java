package dongfang.xsltools.diagnostics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import dongfang.xsltools.util.Dom4jUtil;

public class XMLPerformanceLogger implements PerformanceLogger {
  private Map<String, Long> generationalTimers = new HashMap<String, Long>();

  private Map<String, Long> runningTimers = new HashMap<String, Long>();

  private Map<String, String> timersParentChild = new HashMap<String, String>();

  private Map<String, Integer> counters = new HashMap<String, Integer>();

  private Map<String, String> countersParentChild = new HashMap<String, String>();

  private List<String> orderOfFirstAppearance = new ArrayList<String>();

  private List<String> orderOfFirstParentAppearance = new ArrayList<String>();

  private LinkedList<Map<String, Long>> history = new LinkedList<Map<String, Long>>();

  private Map<String, Long> averaged = new HashMap<String, Long>();

  private int historyLength = 5;

  private int actualHistoryLength = 1;

  private DocumentFactory granada = new DocumentFactory();

  public void setValue(String itemName, String parentItemName, int value) {
    String parentName = countersParentChild.get(itemName);
    if (parentName != null) {
      if (!parentName.equals(parentItemName))
        throw new RuntimeException(
            "Process name had inconsistent parent process name (" + parentName
                + " and " + parentItemName + " for " + itemName + ")");
    } else
      countersParentChild.put(itemName, parentItemName);
    Integer Count = new Integer(value);
    counters.put(itemName, Count);
  }

  public void incrementCounter(String itemName, String parentItemName,
      int increment) {
    String parentName = countersParentChild.get(itemName);
    if (parentName != null) {
      if (!parentName.equals(parentItemName))
        throw new RuntimeException(
            "Process name had inconsistent parent process name (" + parentName
                + " and " + parentItemName + " for " + itemName + ")");
    } else
      countersParentChild.put(itemName, parentItemName);
    Integer Count = counters.get(itemName);
    if (Count == null) {
      setValue(itemName, parentItemName, increment);
    } else {
      setValue(itemName, parentItemName, Count.intValue() + increment);
    }
  }

  public void incrementCounter(String itemName, String parentItemName) {
    incrementCounter(itemName, parentItemName, 1);
  }

  private synchronized void generation() {
    history.addFirst(generationalTimers);
    for (Map.Entry<String, Long> times : generationalTimers.entrySet()) {
      Long l = averaged.get(times.getKey());
      if (l == null)
        l = new Long(0);
      l = new Long(l.longValue() + times.getValue().longValue());
      averaged.put(times.getKey(), l);
    }
    actualHistoryLength++;

    if (actualHistoryLength > historyLength) {
      generationalTimers = history.removeLast();
      for (Map.Entry<String, Long> times : generationalTimers.entrySet()) {
        Long l = averaged.get(times.getKey());
        if (l == null)
          l = new Long(0);
        l = new Long(l.longValue() - times.getValue().longValue());
        averaged.put(times.getKey(), l);
      }
      actualHistoryLength--;
    }
    generationalTimers = new HashMap<String, Long>();
  }

  public synchronized void resetTimers() {
    if (!runningTimers.isEmpty()) {
      System.err.println("Timers were still running at reset time: "
          + runningTimers);
    }
    history.clear();
    averaged.clear();
    generationalTimers.clear();
    timersParentChild.clear();
    orderOfFirstAppearance.clear();
    orderOfFirstParentAppearance.clear();
    runningTimers.clear();
  }

  public void resetCounters() {
    counters.clear();
    countersParentChild.clear();
  }

  /*
   * public synchronized void startTimer(String processName, String
   * parentProcessName) { _startTimer("Start", "Self"); _startTimer(processName,
   * parentProcessName); _stopTimer("Start", "Self"); }
   */

  public synchronized void startTimer(String processName,
      String parentProcessName) {
    String parentName = timersParentChild.get(processName);
    if (parentName != null) {
      if (!parentName.equals(parentProcessName))
        throw new RuntimeException(
            "Process name had inconsistent parent process name (" + parentName
                + " and " + parentProcessName + " for " + processName + ")");
    } else {
      timersParentChild.put(processName, parentProcessName);

      if (!orderOfFirstAppearance.contains(processName))
        orderOfFirstAppearance.add(processName);
      if (!orderOfFirstParentAppearance.contains(parentProcessName))
        orderOfFirstParentAppearance.add(parentProcessName);
    }

    Long Time = new Long(-System.currentTimeMillis());
    /*--splatt--*/
    runningTimers.put(processName, Time);
  }

  /*
   * public synchronized void stopTimer(String processName, String
   * parentProcessName) { _startTimer("Stop", "Self"); _stopTimer(processName,
   * parentProcessName); _stopTimer("Stop", "Self"); }
   */

  public synchronized void stopTimer(String processName,
      String parentProcessName) {
    Long Time = runningTimers.remove(processName);

    if (Time == null) {
      throw new RuntimeException("Process timer " + processName
          + " with parent name " + parentProcessName
          + " was stopped, but never started");
    }

    long e = Time.longValue() + System.currentTimeMillis();

    Long existing = generationalTimers.get(processName);

    if (existing != null)
      e += existing.longValue();
    generationalTimers.put(processName, new Long(e));
  }

  private String format(double d) {
    long l = (long) (d * 10.0 + 0.5);
    double d2 = l / 10.0;
    return Double.toString(d2);
  }

  private synchronized double timerIter(Element resultRoot, String s,
      Map<String, Set<String>> nodeMap, double parentTime,
      Map<String, Long> base, int factor) {
    Element me = granada.createElement(s);
    resultRoot.add(me);

    Long myTime = null;

    if (base.containsKey(s))
      myTime = base.get(s);

    double mrt = -1.0;

    if (myTime != null) {
      mrt = myTime.doubleValue() / factor;
      me.addAttribute("time", format(mrt));
      if (parentTime != -1.0) {
        me.addAttribute("percentage", format(mrt * 100 / parentTime));
      }
    }

    double childTime = 0;

    Set<String> children = nodeMap.get(s);
    if (!children.isEmpty()) {
      List<String> sch = new ArrayList<String>(orderOfFirstAppearance);
      sch.retainAll(children);

      for (String child : sch) {
        childTime += timerIter(me, child, nodeMap, mrt, base, factor);
      }

      if (myTime != null) {
        Element other = granada.createElement("Others");
        other.addAttribute("time", format(mrt - childTime));
        other.addAttribute("percentage",
            format((mrt - childTime) * 100.0 / mrt));
        me.add(other);
      }
    }

    return (myTime == null) ? (0) : (myTime.doubleValue() / factor);
  }

  private void counterIter(Element resultRoot, String s,
      Map<String, Set<String>> nodeMap, Integer parentCount) {
    Element me = granada.createElement(s);
    resultRoot.add(me);

    Integer myCount = counters.get(s);
    if (myCount != null) {
      me.addAttribute("count", myCount.toString());
      if (parentCount != null) {
        me.addAttribute("percentage", format(myCount.doubleValue() * 100
            / parentCount.doubleValue()));
      }
    }

    Set<String> children = nodeMap.get(s);

    if (children != null && !children.isEmpty()) {

      List<String> sch = new ArrayList<String>(children);
      Collections.sort(sch);

      for (String child : sch) {
        counterIter(me, child, nodeMap, myCount);
      }
    }
  }

  private synchronized boolean getTimerStats(Element root,
      Map<String, Long> base, int factor) {
    try {

      Map<String, Set<String>> nodeMap = new HashMap<String, Set<String>>();

      for (String s : timersParentChild.keySet()) {
        nodeMap.put(s, new HashSet<String>());
      }

      for (String s : timersParentChild.values()) {
        nodeMap.put(s, new HashSet<String>());
      }

      for (Map.Entry<String, String> e : timersParentChild.entrySet()) {
        Set<String> parent = nodeMap.get(e.getValue());
        parent.add(e.getKey());
      }

      Set<String> roots = new HashSet<String>();

      for (String s : timersParentChild.values()) {
        if (timersParentChild.get(s) == null)
          roots.add(s);
      }

      List<String> sroots = new ArrayList<String>(orderOfFirstParentAppearance);
      sroots.retainAll(roots);

      for (String s : sroots) {
        timerIter(root, s, nodeMap, -1.0, base, factor);
      }
    } catch (ConcurrentModificationException ex) {
      System.err.println("Ooops, exception: " + ex);
      return false;
    }
    return true;
  }

  private boolean getCounterStats(Element root) {
    try {
      Map<String, Set<String>> nodeMap = new HashMap<String, Set<String>>();

      for (String s : countersParentChild.keySet()) {
        nodeMap.put(s, new HashSet<String>());
      }

      for (String s : countersParentChild.values()) {
        nodeMap.put(s, new HashSet<String>());
      }

      for (Map.Entry<String, String> e : countersParentChild.entrySet()) {
        Set<String> parent = nodeMap.get(e.getValue());
        parent.add(e.getKey());
      }

      Set<String> roots = new HashSet<String>();

      for (String s : countersParentChild.values()) {
        if (countersParentChild.get(s) == null)
          roots.add(s);
      }

      List<String> sroots = new ArrayList<String>(roots);
      Collections.sort(sroots);

      for (String s : sroots) {
        counterIter(root, s, nodeMap, null);
      }
    } catch (ConcurrentModificationException ex) {
      System.err.println("Ooops, exception: " + ex);
      return false;
    }
    return true;
  }

  public synchronized Element getTimerStatsXML() {
    boolean failure = true;
    Element resultRoot = null;
    while (failure) {
      resultRoot = granada.createElement("Timers");
      resultRoot.addAttribute("HistoryLength", "" + actualHistoryLength);
      failure = !getTimerStats(resultRoot, generationalTimers,
          actualHistoryLength);
    }
    return resultRoot;
  }

  public String getTimerStats() {
    Element resultRoot = getTimerStatsXML();
    return Dom4jUtil.toDebugString(resultRoot);
  }

  public Element getCounterStatsXML() {
    boolean failure = true;
    Element resultRoot = null;
    while (failure) {
      resultRoot = granada.createElement("Counters");
      // Document resultDoc = granada.createDocument(resultRoot);
      failure = !getCounterStats(resultRoot);
    }
    return resultRoot;
  }

  public String getCounterStats() {
    Element root = getCounterStatsXML();
    return Dom4jUtil.toDebugString(root);
  }

  public Document getBothStats() {
    Element counters = getCounterStatsXML();
    Element timers = getTimerStatsXML();
    Element root = granada.createElement("Statistics");
    Document doc = granada.createDocument(root);
    root.add(counters);
    root.add(timers);
    return doc;
    // return Dom4jUtil.toDebugString(doc);
  }
}
