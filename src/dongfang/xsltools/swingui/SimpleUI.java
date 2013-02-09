package dongfang.xsltools.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import org.dom4j.Document;
import org.xml.sax.InputSource;

import dk.brics.xmlgraph.XMLGraph;
import dongfang.XMLConstants;
import dongfang.xsltools.context.ValidationContextImpl;
import dongfang.xsltools.controlflow.ApplyTemplatesInst;
import dongfang.xsltools.controlflow.ControlFlowFunctions;
import dongfang.xsltools.controlflow.ControlFlowGraph;
import dongfang.xsltools.controlflow.DeadContextFlow;
import dongfang.xsltools.controlflow.TemplateRule;
import dongfang.xsltools.diagnostics.DiagnosticsConfiguration;
import dongfang.xsltools.diagnostics.MemoryErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.diagnostics.ParseLocationUtil;
import dongfang.xsltools.diagnostics.PerformanceLogger;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLocatableException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.resolver.ComboInputSource;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.resolver.URLResolutionContext;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.TestTriple;
import dongfang.xsltools.util.Util;
import dongfang.xsltools.validation.InputToucherRun;
import dongfang.xsltools.validation.ValidationError;
import dongfang.xsltools.validation.ValidationResult;
import dongfang.xsltools.validation.ValidationRun;
import dongfang.xsltools.validation.ValidationRunDumper;
import dongfang.xsltools.validation.VerboseRunDecorator;
import dongfang.xsltools.validation.XSLTValidator;
import dongfang.xsltools.xmlclass.schemaside.SingleTypeXMLClass;
import dongfang.xsltools.xmlclass.xslside.DeclaredNodeType;
import dongfang.xsltools.xmlclass.xslside.NodeType;

/**
 * A simple, rudimentary demonstrator implementation of how an UI can
 * be slapped over the XSLT validator, making a code-assisted XSLT IDE.
 * Presently, there are nice pop-up menus for flow visualization, but
 * there is not even a save feature!
 * It is better invoked with the file name of a triple file as the only
 * argument.
 * @author dongfang
 */
public class SimpleUI extends ValidationContextImpl implements ValidationRun {
  private static final int MIN_RUN_INTERVAL = 2500;

  private static final int STATS_UPDATE_INTERVAL = 4900;

  private static final int POPUPMENU_MAXLENGTH = 50;
  
  private static final boolean VERBOSE = true;

  private Map<String, DocumentEditor> editors = new HashMap<String, DocumentEditor>();

  private Map<String, StylesheetModule> allKnownStylesheetModules = new HashMap<String, StylesheetModule>();

  private JTabbedPane tabs;

  private JTextArea result = new JTextArea();

  private JTextField cursorLine;

  private JTextField cursorCol;

  private TestTriple triple;

  private LevelMatic levelmatic;

  private boolean rereadSchemas = true;

  private boolean saveDump = true;

  private boolean singleShotMode = false;

  private boolean singleShotGoOne = true;

  private Object singleShotMonitor = new Object();

  private boolean reinitEditors;

  private boolean clearBeforeWriteToResult;

  private boolean testInputTouch = true;

  private short progress = 0;

  private ValidationResult lastValidationResult;

  interface DocumentEditor {
    String getText();

    void setText(String text, short scope);

    void append(String text);

    String getName();

    void highlight(int start, int end);

    void clearHighlighting();

    int getLineStartPos(int line) throws BadLocationException;

    void install(JTabbedPane container);

    void uninstall(JTabbedPane container);

    int getHumanKey();

    boolean accessed();
  }


  private class JumpToActionListener implements ActionListener {
    private final JTextArea ta;

    private final int line;

    private final int col;

    JumpToActionListener(JTextArea ta, int line, int col) {
      this.ta = ta;
      this.line = line;
      this.col = col;
    }

    public void actionPerformed(ActionEvent e) {
      try {
        int pos = ta.getLineStartOffset(line - 1);
        pos += col;
        ta.setCaretPosition(pos);
      } catch (BadLocationException ex) {
        System.err.println("Oops .. bad location.");
      }
    }
  }

  class SingleDocumentEditor extends JTextArea implements DocumentEditor {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7260190929691971337L;

	int humanKey;

    boolean didHighlight;

    boolean accessed = true;

    boolean permalock;

    JScrollPane scroller;

    SingleDocumentEditor(String systemId, int humanKey, boolean permalock) {
      this.humanKey = humanKey;
      this.setName(systemId);
      this.permalock = permalock;
      // addCaretListener(calis);
    }

    public boolean accessed() {
      boolean accessed = this.accessed;
      if (!permalock)
        this.accessed = false;
      return accessed;
    }

    @Override
    public String getText() {
      accessed = true;
      return super.getText();
    }

    public void clearHighlighting() {

      if (didHighlight)
        getHighlighter().removeAllHighlights();
      if (tabs.getSelectedComponent() == this)
        repaint();
      didHighlight = false;
    }

    public int getLineStartPos(int line) throws BadLocationException {
      return getLineStartOffset(line);
    }

    public void highlight(int start, int end) {
      try {
        getHighlighter().addHighlight(start, end,
            DefaultHighlighter.DefaultPainter);
        didHighlight = true;
      } catch (BadLocationException blex) {
        System.err.println(blex);
      }
    }

    public void setText(String text, short scope) {
      setText(text);
      accessed = true;
    }

    public void install(JTabbedPane tabs) {
      scroller = new JScrollPane(this);
      scroller.setName(getName());
      tabs.add(scroller);
    }

    public void uninstall(JTabbedPane tabs) {
      tabs.remove(scroller);
    }

    public int getHumanKey() {
      return humanKey;
    }
  }

  class DoubleDocumentEditor extends JTabbedPane implements DocumentEditor {
    /**
	 * 
	 */
	private static final long serialVersionUID = -793707256409736973L;

	JTextArea original;

    JTextArea simplified = new JTextArea();

    String name;

    int humanKey;

    boolean didHighlight;

    boolean accessed = true;

    boolean permalock;

    DoubleDocumentEditor(String name, int humanKey, boolean permalock) {
      super(SwingConstants.BOTTOM);
      original = new JTextArea() {
        /**
		 * 
		 */
		private static final long serialVersionUID = -5520528037165291615L;

		@Override
        public void processMouseEvent(MouseEvent e) {
          if (e.isPopupTrigger()) {
            final JPopupMenu pop = new JPopupMenu("gedefims");
            pop.show(this, e.getX(), e.getY());
            int i = viewToModel(e.getPoint());
            try {
              int l = getLineOfOffset(i);

              locateContent(DoubleDocumentEditor.this.getName(), l + 1, i
                  - getLineStartOffset(l), this, e);

            } catch (BadLocationException ex) {
            }

          } else {
            super.processMouseEvent(e);
          }
        }
      };
      original.addCaretListener(new CaretListener() {
        public void caretUpdate(CaretEvent e) {
          try {
            int line = original.getLineOfOffset(e.getDot());
            int lineStartPos = original.getLineStartOffset(line);
            cursorLine.setText(Integer.toString(line + 1));
            cursorCol.setText(Integer.toString(e.getDot() - lineStartPos));
          } catch (BadLocationException ex) {
          }
        }
      });

      simplified.setEditable(false);
      add("Original", new JScrollPane(original));
      add("Simplified", new JScrollPane(simplified));
      this.name = name;
      this.humanKey = humanKey;
      this.permalock = permalock;
    }

    public boolean accessed() {
      boolean accessed = this.accessed;
      if (!permalock)
        this.accessed = false;
      return accessed;
    }

    public void clearHighlighting() {
      if (didHighlight)
        original.getHighlighter().removeAllHighlights();
      if (tabs.getSelectedComponent() == this)
        repaint();
      didHighlight = false;
    }

    public int getLineStartPos(int line) throws BadLocationException {
      return original.getLineStartOffset(line);
    }

    public void highlight(int start, int end) {
      try {
        original.getHighlighter().addHighlight(start, end,
            DefaultHighlighter.DefaultPainter);
        didHighlight = true;
      } catch (BadLocationException blex) {
        System.err.println(blex);
      }
    }

    public void setText(String text, short scope) {
      if (scope == StylesheetModule.ORIGINAL) {
        original.setText(text);
        accessed = true;
      } else
        simplified.setText(text);
    }

    public void append(String text) {
      simplified.append(text);
      // accessed = true;
    }

    public String getText() {
      accessed = true;
      return original.getText();
    }

    @Override
	public String getName() {
      return name;
    }

    public int getHumanKey() {
      return humanKey;
    }

    public void install(JTabbedPane tabs) {
      tabs.add(this);
    }

    public void uninstall(JTabbedPane tabs) {
      tabs.remove(this);
    }
  }

  SimpleUI() {
    setResolver(this);

    tabs = new JTabbedPane(SwingConstants.TOP);
    JFrame jf = new JFrame("Dongfang XSL Validator");
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitter.add(tabs);
    splitter.add(new JScrollPane(result));

    JPanel main = new JPanel();
    main.setLayout(new BorderLayout());
    main.add(splitter, BorderLayout.CENTER);

    JPanel bottom = new JPanel();
    bottom.setLayout(new BorderLayout());

    JPanel buttonPanel = new JPanel();

    // JButton save = new JButton("Save triple");
    // buttonPanel.add(save);

    JPanel cursorPosPanel = new JPanel();
    cursorLine = new JTextField("1", 4);
    cursorPosPanel.add(cursorLine);

    cursorCol = new JTextField("0", 4);
    cursorPosPanel.add(cursorCol);

    buttonPanel.add(cursorPosPanel);

    JButton reread = new JButton("Reread schemas");
    buttonPanel.add(reread);

    reread.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        rereadSchemas = true;
      }
    });

    JButton savedump = new JButton("Dump info");
    buttonPanel.add(savedump);

    savedump.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveDump = true;
      }
    });

    /*
    JCheckBox useFastAlgo = new JCheckBox("Fast Algorithm");
    buttonPanel.add(useFastAlgo);
    useFastAlgo.setSelected(SimpleUI.this.useFastAlgo);
    useFastAlgo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        SimpleUI.this.useFastAlgo = ((JCheckBox) evt.getSource()).isSelected();
        if (!SimpleUI.this.useFastAlgo)
          rereadSchemas = true;
      }
    });
    */

    JPanel singleShotPanel = new JPanel();
    singleShotPanel.setBorder(BorderFactory.createEtchedBorder());

    final JButton singleShotTrigger = new JButton("Run");
    singleShotTrigger.setEnabled(SimpleUI.this.singleShotMode);
    singleShotTrigger.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        synchronized (singleShotMonitor) {
          singleShotGoOne = true;
        }
      }
    });

    JCheckBox singleShotMode = new JCheckBox("Single-run");
    singleShotPanel.add(singleShotMode);
    singleShotPanel.add(singleShotTrigger);

    buttonPanel.add(singleShotPanel);

    singleShotMode.setSelected(SimpleUI.this.singleShotMode);
    singleShotMode.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        SimpleUI.this.singleShotMode = ((JCheckBox) evt.getSource())
            .isSelected();
        singleShotTrigger.setEnabled(SimpleUI.this.singleShotMode);
        synchronized (singleShotMonitor) {
          singleShotGoOne = !SimpleUI.this.singleShotMode;
        }
      }
    });

    bottom.add(buttonPanel, BorderLayout.WEST);

    LevelMatic lm = makeLevelMatic();
    bottom.add(lm, BorderLayout.CENTER);
    main.add(bottom, BorderLayout.SOUTH);

    jf.getContentPane().add(main);
    jf.setSize(new Dimension(1024, 768));
    jf.setVisible(true);

    splitter.setDividerLocation(0.75);
  }

  void locateContent(String name, int line, int col, JTextArea ta, MouseEvent e) {
    StylesheetModule m = allKnownStylesheetModules.get(name);
    if (m != null) {
      Set<ApplyTemplatesInst> invokers = m.searchForInvokers(line, col);
      if (!invokers.isEmpty()) {
        popupInvokers(invokers, ta, e.getX(), e.getY());
      } else {

        Set<TemplateRule> templates = m.searchForTemplates(line, col);
        if (!templates.isEmpty()) {
          popupTemplates(ta, templates, e.getX(), e.getY());
        }
      }
    }
  }

  private void popupInvokers(Set<ApplyTemplatesInst> invokers, JTextArea ta,
      int x, int y) {
    JPopupMenu m = makePopupMenuForInvoker(ta, invokers);
    m.show(ta, x, y);
  }

  private void popupTemplates(JTextArea ta, Set<TemplateRule> templates, int x,
      int y) {
    JPopupMenu m = makePopupMenuForTemplates(ta, templates);
    m.show(ta, x, y);
  }

  private JMenuItem getSubSubMenu(String label, ActionListener l,
      Set<DeclaredNodeType> flow) {
    JMenu result = new JMenu("To: " + label);
    boolean didSomething = false;
    List<DeclaredNodeType> emAll = new ArrayList<DeclaredNodeType>();
    emAll.addAll(flow);
    Collections.sort(emAll);
    for (DeclaredNodeType t : emAll) {
      JMenuItem zippxx = new JMenuItem("Outflow: " + t.toLabelString());
      if (l != null)
        zippxx.addActionListener(l);
      result.add(zippxx);
      didSomething = true;
    }
    if (didSomething)
      return result;
    //JMenuItem dead = new JMenuItem("Dead code - no flows");
    //dead.setForeground(Color.RED);
    //return dead;
    return null;
  }

  private JMenu getSubMenu(JTextArea ta, String label,
      Map<TemplateRule, Set<DeclaredNodeType>> flow) {
    JMenu result = new JMenu("@: " + label);
    
    Map<TemplateRule, JMenuItem> allEmBastards = new HashMap<TemplateRule, JMenuItem>();
    List<TemplateRule> allRulesMentioned = new ArrayList<TemplateRule>();
    
    boolean clipd = false;
    
    for (Map.Entry<TemplateRule, Set<DeclaredNodeType>> e : 
      flow.entrySet()) {
      ParseLocation backThere = e.getKey().getOriginalLocation();
      ActionListener al = null;
      if (backThere != null) {
        al = new JumpToActionListener(ta, backThere
            .elementStartTagBeginningLine(), backThere
            .elementStartTagBeginningColumn());
      }
      JMenuItem zippxx = getSubSubMenu(e.getKey().toLabelString(), al, e
          .getValue());
      if (zippxx!=null) {
        allRulesMentioned.add(e.getKey());
        allEmBastards.put(e.getKey(), zippxx);
      }
      if (allRulesMentioned.size()>=POPUPMENU_MAXLENGTH) {
        clipd = true;
        break;
      }
    }
    Collections.sort(allRulesMentioned, ControlFlowFunctions.getAppearanceOrdering());
    for (TemplateRule r : allRulesMentioned) {
      result.add(allEmBastards.get(r));
    }
    if (clipd)
      result.add(new JMenuItem("..."))
;    return result;
  }

  private JMenuItem getDeathSubSubMenu(NodeType type, String cause) {
    JMenuItem item = new JMenuItem("No flow of " + type.toLabelString()
        + "; cause: " + cause);
    return item;
  }

  private JMenu getDeathSubMenu(DeadContextFlow dead) {
    JMenu result = new JMenu(dead.getTarget().toLabelString());
    result.setForeground(Color.RED);
    if (dead.getLostNodeTypes().isEmpty()) {
      JMenuItem alld = new JMenuItem("No flows; cause: " + dead.cause());
      result.add(alld);
    } else {
      for (NodeType type : dead.getLostNodeTypes()) {
        result.add(getDeathSubSubMenu(type, dead.cause()));
      }
    }
    return result;
  }

  private JPopupMenu makePopupMenuForInvoker(JTextArea ta, Set<ApplyTemplatesInst> src) {
    JPopupMenu result = new JPopupMenu();

    Set<DeclaredNodeType> allContextTypes = new HashSet<DeclaredNodeType>();

    Map<DeclaredNodeType, Map<TemplateRule, Set<DeclaredNodeType>>> flows 
    = new HashMap<DeclaredNodeType, Map<TemplateRule, Set<DeclaredNodeType>>>();

    for (ApplyTemplatesInst i : src) {
      i.getAllContextTypes(allContextTypes);
      i.getAllFlowsForAllModesOtherWayAround(flows);
    }

    for (DeclaredNodeType context : flows.keySet()) {
      allContextTypes.remove(context);
      JMenu sub = getSubMenu(ta, context.toLabelString(), flows.get(context));
      result.add(sub);
    }

    boolean addedNoSelectSeparator = false;

    for (DeclaredNodeType type : allContextTypes) {
      if (addedNoSelectSeparator) {
      } else {
        result.addSeparator();
        addedNoSelectSeparator = true;
      }
      JMenuItem noflo = new JMenuItem("No types selected @ "
          + type.toLabelString());
      noflo.setForeground(Color.BLUE);
      result.add(noflo);
    }

    boolean addedDeathSeparator = false;

    for (ApplyTemplatesInst i : src) {
      if (!i.getDeathCauses().isEmpty()) {
        if (addedDeathSeparator) {
        } else {
          result.addSeparator();
          addedDeathSeparator = true;
        }
      }
      for (DeadContextFlow dead : i.getDeathCauses()) {
        result.add(getDeathSubMenu(dead));
      }
    }

    return result;
  }

  private JMenu getInvokerMenu(JTextArea ta, DeclaredNodeType type,
      Set<ApplyTemplatesInst> invokers) {
    JMenu result = new JMenu("Inflow: " + type.toLabelString());
    
    List<ApplyTemplatesInst> invokersAsList = 
      new ArrayList<ApplyTemplatesInst>();
    
    invokersAsList.addAll(invokers);
    
    Collections.sort(invokersAsList, ControlFlowFunctions.getInvokerAppearanceOrdering());

    boolean clipd = invokersAsList.size() > POPUPMENU_MAXLENGTH;
    
    while(invokersAsList.size() > POPUPMENU_MAXLENGTH)
      invokersAsList.remove(invokersAsList.size()-1);
    
    for (ApplyTemplatesInst invoker : invokersAsList) {
      String l;
      if (invoker == null)
        l = "* ENTRY *";
      else
        l = invoker.toLabelString();
      JMenuItem mi = new JMenuItem("From: " + l);
      ParseLocation originalLocation = invoker.getOriginalLocation();
      if (originalLocation != null) {
        mi.addActionListener(new JumpToActionListener(ta, originalLocation
            .elementStartTagBeginningLine(), originalLocation
            .elementStartTagBeginningColumn()));
      }
      result.add(mi);
    }
    if(clipd)
      result.add(new JMenuItem("..."));
    return result;
  }
  private JPopupMenu makePopupMenuForTemplates(JTextArea ta,
      Set<TemplateRule> src) {
    JPopupMenu result = new JPopupMenu();
    String idxx = "Index: ";
    boolean needsComma = false;
    
    for (TemplateRule r : src) {
      if (needsComma)
        idxx += ", ";
      else needsComma = true;
      idxx += (Integer.toString(r.getIndex()));
    }
    
    JMenuItem indexLabel = new JMenuItem(idxx);
    result.add(indexLabel);
    result.addSeparator();
    
    boolean didSomething = false;
    for (TemplateRule template : src) {
      
      List<DeclaredNodeType> contextTypesAsList = 
        new ArrayList<DeclaredNodeType>(template.getAllModesContextSet());
      
      Collections.sort(contextTypesAsList);

      boolean clipd = contextTypesAsList.size() > POPUPMENU_MAXLENGTH;
      
      while(contextTypesAsList.size() > POPUPMENU_MAXLENGTH)
        contextTypesAsList.remove(contextTypesAsList.size()-1);
      
      for (DeclaredNodeType type : contextTypesAsList) {
        Set<ApplyTemplatesInst> invokersForType = template
            .getInvokersForType(type);
        result.add(getInvokerMenu(ta, type, invokersForType));
        didSomething = true;
      }
      
      if(clipd)
        result.add(new JMenuItem("..."));
        
      if (!didSomething) {
        JMenuItem dead = new JMenuItem("Dead code - no context types");
        dead.setForeground(Color.RED);
        result.add(dead);
      }
    }

    return result;
  }

  private LevelMatic makeLevelMatic() {
    levelmatic = new LevelMatic();
    levelmatic.addOption("Simplify");
    levelmatic.addOption("Compute CFG");
    levelmatic.addOption("Construct SG");
    levelmatic.addOption("Validate");
    levelmatic.setLevel(2);
    return levelmatic;
  }

  /*
   * private void installEditor(DocumentEditor editor) { JScrollPane sp = new
   * JScrollPane((JComponent)editor); sp.setName(editor.getName());
   * tabs.add(sp); }
   */

  private ResolutionContext input = new URLResolutionContext();

  private DocumentEditor openDocumentEditor(String systemId, int humanKey)
      throws IOException {
    DocumentEditor editor = editors.get(systemId);

    if (editor != null)
      return editor;

    StringBuilder fileContents = new StringBuilder();

    if (!XMLConstants.VALIDATOR_NAMESPACE_URI.equals(systemId)) {
      if (humanKey == INPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY
          || humanKey == OUTPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY
          || humanKey == STYLESHEET_MODULE_IDENTIFIER_KEY
          || humanKey == XMLINSTANCE_MODULE_IDENTIFIER_KEY
          || humanKey == STYLESHEET_MODULE_IDENTIFIER_KEY
          || humanKey == STYLESHEET_PRINCIPAL_MODULE_IDENTIFIER_KEY
          || humanKey == SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY) {
        InputSource is = input.resolveStream(systemId, "", humanKey);
        java.io.Reader rdr = is.getCharacterStream();
        char[] buf = new char[1024];
        int l;
        while ((l = rdr.read(buf)) > 0) {
          fileContents.append(buf, 0, l);
        }
      }
    }

    if (humanKey == STYLESHEET_MODULE_IDENTIFIER_KEY
        || humanKey == STYLESHEET_PRINCIPAL_MODULE_IDENTIFIER_KEY) {
      if (systemId.equals(XMLConstants.VALIDATOR_NAMESPACE_URI)) {
        editor = new SingleDocumentEditor("Default Rules", humanKey, true);
        ((SingleDocumentEditor) editor).setEditable(false);
      } else {
        editor = new DoubleDocumentEditor(systemId, humanKey, false);
      }
    } else
      editor = new SingleDocumentEditor(systemId, humanKey, false);

    editor.setText(fileContents.toString(), StylesheetModule.ORIGINAL);
    editor.install(tabs);
    editors.put(systemId, editor);
    return editor;
  }

  private DocumentEditor openStringEditor(String systemId, int humanKey)
      throws IOException {
    DocumentEditor editor = editors.get(systemId);

    if (editor != null)
      return editor;

    editor = new SingleDocumentEditor(
        ResolutionContext.HUMAN_INTERFACE_STRINGS[humanKey], humanKey, true);

    if (humanKey == INPUT_DTD_NAMESPACE_URI_KEY) {
      editor.setText(triple.getInputDTDNamespaceURI(),
          StylesheetModule.CORE);
    } else if (humanKey == INPUT_SCHEMA_ROOT_ELEMENT_NAME_KEY) {
      editor.setText(triple.getInputRootElementName(),
          StylesheetModule.CORE);
    }

    editor.install(tabs);
    editors.put(systemId, editor);
    return editor;
  }

  @Override
public void earlyStreamRequest(String systemId, String user, int humanKey)
      throws IOException {
    // TODO Auto-generated method stub
    openDocumentEditor(systemId, humanKey);
  }

  @Override
public InputSource resolveStream(String systemId, String user, int humanKey)
      throws IOException {
    // TODO Auto-generated method stub

    // if (systemId == identityStunt)
    //  return super.resolveStream(systemId, humanKey);

    DocumentEditor editor = openDocumentEditor(systemId, humanKey);
    ComboInputSource cis = new ComboInputSource(new StringReader(editor
        .getText()), "UTF-8");
    cis.setSystemId(systemId);
    return cis;
  }

  public void earlyStringRequest(String systemId, String user, String none, int humanKey)
      throws IOException {
    openStringEditor(systemId, humanKey);
  }

  private String ask(String resourceDesc) {
    final JFileChooser fc = new JFileChooser("test/resources/dtdtriples",
        FileSystemView.getFileSystemView());
    final JFrame fr = new JFrame(resourceDesc);
    final File[] result = new File[1];
    fr.getContentPane().add(fc);
    fc.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        fr.setVisible(false);
        result[0] = fc.getSelectedFile();
      }
    });
    fr.pack();
    fr.setVisible(true);
    while (result[0] == null)
      ;
    String path = result[0].getAbsolutePath();
    String asURL = Util.toUrlString(path);
    return asURL;
  }

  public String getSchemaIdentifier(String s, short io) {
    // TODO Auto-generated method stub
    if (io == INPUT) {
    if (triple.getInputSchemaURI() == null)
      triple.setInputSchemaURI(ask("Input schema"));
    return triple.getInputSchemaURI();
    }
    if (triple.getOutputSchemaURI() == null)
      triple.setOutputSchemaURI(ask("Output schema"));
    return triple.getOutputSchemaURI();
  }

  public String getStylesheetIdentifier() {
    if (triple.getStylesheetPrimaryModuleURI() == null)
      triple.setStylesheetPrimaryModuleURI(ask("Stylesheet principal module"));
    return triple.getStylesheetPrimaryModuleURI();
  }

  public String getRootElementNameIdentifier(String s, short io) {
    return s;
  }
  
  public String getNamespaceURIIdentifier(String s, short io) {
    return s;
  }

  private void clearAtWrite(boolean force) {
    if (force || clearBeforeWriteToResult) {
      result.setText("");
      clearBeforeWriteToResult = false;
    }
  }

  private void checkClearResult() {
    if (reinitEditors && progress == 0) {

      for (DocumentEditor edt : editors.values())
        edt.clearHighlighting();

      clearBeforeWriteToResult = true;

      reinitEditors = false;
    }
  }

  public void pushMessage(String target, String message) {
    clearAtWrite(false);
    result.append(target + "<--" + message + "\n");
  }

  private void highlight(String moduleId, ParseLocation ploc,
      ParseLocation.Extent extent) throws IOException {

    DocumentEditor editor = openDocumentEditor(moduleId,
        STYLESHEET_MODULE_IDENTIFIER_KEY);

    int startLine = ploc.elementStartTagBeginningLine();
    int startCol = ploc.elementStartTagBeginningColumn();

    int endLine;
    switch (extent) {
    case TAG:
      endLine = ploc.elementStartTagEndLine();
      break;
    case ELEMENT:
      endLine = ploc.elementEndTagEndLine();
      break;
    case POINT:
      endLine = ploc.elementStartTagBeginningLine();
      break;
    default:
      endLine = -1;
    }

    int endCol;

    switch (extent) {

    case TAG:
      endCol = ploc.elementStartTagEndColumn();
      break;
    case ELEMENT:
      endCol = ploc.elementEndTagEndColumn();
      break;
    case POINT:
      endCol = ploc.elementStartTagBeginningColumn() + 16;
      break;
    default:
      endCol = -1;
    }

    try {
      int startPos = editor.getLineStartPos(startLine - 1) + startCol;
      int endPos = editor.getLineStartPos(endLine - 1) + endCol;

      editor.highlight(startPos, endPos);
    } catch (BadLocationException ex) {
    }
  }

  private void error(String errorType, MemoryErrorReporter cp)
      throws IOException {
    Map<String, XSLToolsException> antidupe = new HashMap<String, XSLToolsException>();

    Iterator<XSLToolsException> iter = cp.errorIterator();

    while (iter.hasNext()) {
      XSLToolsException e = iter.next();
      antidupe.put(e.getMessage(), e);
    }

    List<String> keys = new ArrayList<String>(antidupe.keySet());
    Collections.sort(keys);

    Iterator<String> iter2 = keys.iterator();

    while (iter2.hasNext()) {
      String key = iter2.next();
      XSLToolsException ex = antidupe.get(key);

      clearAtWrite(false);

      result.append(ex.getMessage() + "\n");

      // ex.printStackTrace();

      if (ex instanceof XSLToolsLocatableException) {
        XSLToolsLocatableException lex = (XSLToolsLocatableException) ex;

        result.append(lex.getParsePositionAsReadableString());

        ParseLocation ploc = lex.getParseLocation();

        if (ploc != null) {
          String moduleId = lex.getOriginId();
          ParseLocation.Extent extent = lex.getExtent();
          highlight(moduleId, ploc, extent);
        }
      } else {
        result.append("\nparse location missing");
      }
      result.append("\n-----\n");
    }
  }

  private class CounterTicker extends Thread {
    @Override
	public void run() {
      while (true) {
        try {
          Thread.sleep(STATS_UPDATE_INTERVAL);
        } catch (InterruptedException ex) {
        }
        displayCounterResults();
      }
    }

    private void displayCounterResults() {
      try {
        PerformanceLogger pa = DiagnosticsConfiguration.current
            .getPerformanceLogger();
        DocumentEditor statsEditor = SimpleUI.this.openDocumentEditor(
            "CounterStatistics", (short) 0);
        String stats = pa.getCounterStats();
        statsEditor.setText(stats, StylesheetModule.CORE);
      } catch (IOException ex) {
        System.err.println("Trouble in CounterTicker");
      }
    }
  }

  private class TimerTicker extends Thread {
    @Override
	public void run() {
      while (true) {
        try {
          Thread.sleep(STATS_UPDATE_INTERVAL);
        } catch (InterruptedException ex) {
        }
        displayTimerResults();
      }
    }

    private void displayTimerResults() {
      try {
        PerformanceLogger pa = DiagnosticsConfiguration.current
            .getPerformanceLogger();
        DocumentEditor statsEditor = SimpleUI.this.openDocumentEditor(
            "TimerStatistics", (short) 0);
        String stats = pa.getTimerStats();
        statsEditor.setText(stats, StylesheetModule.CORE);
      } catch (IOException ex) {
        System.err.println("Trouble in TimerTicker");
      }
    }
  }

  private void cycle() {

    boolean first = true;

    new CounterTicker().start();
    new TimerTicker().start();

    while (true) {
      while (singleShotMode) {
        synchronized (singleShotMonitor) {
          if (singleShotGoOne) {
            singleShotGoOne = false;
            break;
          }
        }
        try {
          Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
      }

      if (rereadSchemas) {
        reset();
        rereadSchemas = false;
      }

      reinitEditors = true;

      this.progress = 0;

      PerformanceLogger pa = DiagnosticsConfiguration.current
          .getPerformanceLogger();
      pa.resetCounters();
      pa.resetTimers();

      long time = System.currentTimeMillis();

      try {
        MemoryErrorReporter _cp = new MemoryErrorReporter();

        if (first) {
          XSLTValidator.prepareValidation(this);
          first = false;
        }

        /*
         * for (Iterator<String> enames = editors.keySet().iterator();
         * enames.hasNext();) { String ename = enames.next(); DocumentEditor
         * editor = editors.get(ename); editor.makeLineStartPosTable(); }
         */
        ValidationRun run = this;

        if (VERBOSE)
          run = new VerboseRunDecorator(run, System.out);

        if (saveDump) {
          run = new ValidationRunDumper(run, "tmp/" + triple.getName());
          saveDump = false;
        }

        if (testInputTouch) {
          run = new InputToucherRun(getInputType(getSchemaIdentifier("", INPUT)),
              run);
        }

        XSLTValidator.validate(triple.getStylesheetPrimaryModuleURI(), this,
            _cp, run);

        // for (XSLToolsLocatableException ex: cp.getLocatableErrors()) {
        error("Syntactic / semantic error", _cp);
        // }

        /*
         * PerformanceAnalyzer pa =
         * DiagnosticsConfiguration.current.getPerformanceAnalyzer();
         * DocumentEditor statsEditor = openDocumentEditor("TimerStatistics",
         * (short) 0); String stats = pa.getTimerStats();
         * statsEditor.setText(stats, StylesheetModule.SIMPLIFIED);
         * pa.resetTimers();
         */
        /* } */
      } catch (XSLToolsException ex) {
        System.err.println("Escaping exception! " + ex);
        clearAtWrite(true);
        result.append(ex.getMessage());
        // StackTraceElement[] st = ex.getStackTrace();
        // System.err.println(st[0]);
        ex.printStackTrace();
      } catch (Throwable ex) {
        System.err.println("Escaping exception! " + ex);
        // StackTraceElement[] st = ex.getStackTrace();
        // System.err.println(st[0]);
        ex.printStackTrace();
      } finally {
        time = System.currentTimeMillis() - time;
        if (!singleShotMode && time < MIN_RUN_INTERVAL) {
          try {
            Thread.sleep(MIN_RUN_INTERVAL - time);
          } catch (InterruptedException ex) {
          }
        }
      }
    }
  }

  /*
  private void removeDeadEditors() {
    Set<String> death = new HashSet<String>();

    for (Iterator<String> enames = editors.keySet().iterator(); enames
        .hasNext();) {
      String ename = enames.next();
      DocumentEditor editor = editors.get(ename);
      if (!editor.accessed()) {
        death.add(ename);
        editor.uninstall(tabs);
      }
    }

    for (String ename : death)
      editors.remove(ename);
  }
  */

  public String resolveString(String systemId, String user, String none, int humanKey)
      throws IOException {
    openDocumentEditor(systemId, humanKey);
    DocumentEditor editor = editors.get(systemId);
    return editor.getText();
  }

  public static void main(String[] args) throws XSLToolsException {
    SimpleUI ui = new SimpleUI();

    if (args.length >= 3) {
      ui.triple = new TestTriple();
      ui.triple.parseArgs(args);
    } else if (args.length >= 1) {
      ui.triple = TestTriple.makeTriple(args[0]);
    } else
      ui.triple = new TestTriple();

    ui.cycle();
  }

  public short getProgress() {
    return progress;
  }

  public ValidationResult getValidationResult() {
    return lastValidationResult;
  }

  public boolean relaxateProgress(short progress) {
    checkClearResult();
    this.progress = (short) Math.max(progress, this.progress);
    return this.progress < levelmatic.getLevel();
  }

  public void setControlFlowSG(XMLGraph controlFlowSG) {
  }

  public void setInputType(SingleTypeXMLClass inputType) {
    try {
      DocumentEditor inputSchemaDiags = openDocumentEditor(
          "input-schema-diags", (short) 0);
      String isd = Dom4jUtil.diagnostics(inputType);
      inputSchemaDiags.setText(isd, StylesheetModule.CORE);
    } catch (IOException ex) {
      System.err.println(ex);
    }
  }

  public void setOutputType(XMLGraph outputType) {
  }

  public void setPerformanceLogger(PerformanceLogger pa) {
  }
  
  public void setSemPreservingSimplifiedStylesheet(Stylesheet ss) {
  }

  public void setApproxSimplifiedStylesheet(Stylesheet ss) {
    if (ss == null)
      return;

    List<StylesheetModule> simplified = ss.getAllModules();

    allKnownStylesheetModules.clear();

    try {
      for (StylesheetModule module : simplified) {
        allKnownStylesheetModules.put(module.getSystemId(), module);
        Document simplifiedSS = module.getDocument(StylesheetModule.CORE);
        simplifiedSS = (Document) simplifiedSS.clone();
        simplifiedSS.getRootElement()
            .addAttribute("href", module.getSystemId());
        simplifiedSS.getRootElement().addAttribute("levelnumber",
            Integer.toString(module.getlLevelNumber()));
        simplifiedSS.getRootElement().addAttribute("modulenumber",
            Integer.toString(module.getModuleNumber()));
        simplifiedSS.getRootElement().addAttribute("sublevel-upper-bound",
            Integer.toString(module.getSublevelUpperBound()));

        DocumentEditor ed = openDocumentEditor(module.getSystemId(),
            STYLESHEET_MODULE_IDENTIFIER_KEY);

        ed.setText(Dom4jUtil.toDebugString(simplifiedSS),
            StylesheetModule.CORE);
      }
    } catch (IOException ex) {
      System.err.println(ex);
    }
  }

  public void setValidationResult(ValidationResult vres) {
    //Document errorReport = vres.getErrorReport();
    //String asString = Dom4jUtil.toDebugString(errorReport);

    String asString = vres.toString();
    
    clearAtWrite(false);

    result.append(asString);

    vres.killDuplicates();
    
    for (ValidationError error : vres.getValidationErrors()) {
      ParseLocation pl = error.getParseLocation();
      try {
        if (pl != null) {
          highlight(error.getFilename(), pl, ParseLocation.Extent.POINT);
        }
        if (error.getCulpritOrigin() != null) {
          pl = ParseLocationUtil.getParseLocation(error.getCulpritOrigin());
          if (pl != null) {
            highlight(error.getCulpritOrigin().getFile(), pl,
                ParseLocation.Extent.POINT);
          }
        }
      } catch (IOException ex) {
      }
    }
  }

  public void setXcfg(ControlFlowGraph xcfg) {
    try {
      DocumentEditor xcfgEditor = openDocumentEditor("xcfg", (short) 0);
      String xcfgd = Dom4jUtil.diagnostics(xcfg);
      xcfgEditor.setText(xcfgd, StylesheetModule.CORE);
    } catch (IOException ex) {
      System.err.println(ex);
    }
  }

  public boolean restarted() {
    return false;
  }
}
