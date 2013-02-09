/*
 * dongfang M. Sc. Thesis
 * Created on 15-02-2005
 */
package dongfang.xsltools.experimental;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.PlainDocument;

import org.dom4j.Document;
import org.xml.sax.InputSource;

import dongfang.xsltools.configuration.ConfigurationFactory;
import dongfang.xsltools.diagnostics.MemoryErrorReporter;
import dongfang.xsltools.diagnostics.ParseLocation;
import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.exceptions.XSLToolsLocatableException;
import dongfang.xsltools.model.Stylesheet;
import dongfang.xsltools.model.StylesheetModule;
import dongfang.xsltools.resolver.ResolutionContext;
import dongfang.xsltools.simplification.DefaultSimplifier;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.util.UniqueNameGenerator;

/**
 * @author dongfang
 * 
 */
public class InteractiveEditorAndTree extends JPanel {
  /**
	 * 
	 */
	private static final long serialVersionUID = -207727832372775705L;

PlainDocument doc;

  JTextArea inputta;

  JTextArea outputta;

  JTextArea errorta;

  int[] lineoffsets;

  InteractiveEditorAndTree() {
    setLayout(new GridLayout(3, 1));
    inputta = new JTextArea();
    // inputta.setPreferredSize(new Dimension(400, 200));
    doc = (PlainDocument) inputta.getDocument();
    //
    // setLayout(new BorderLayout());
    JScrollPane scroller = new JScrollPane(inputta);
    add(scroller);

    outputta = new JTextArea();
    add(new JScrollPane(outputta));

    errorta = new JTextArea();
    add(new JScrollPane(errorta));

    init();
  }

  InteractiveEditorAndTree(String text) {
    this();
    inputta.setText(text);
  }

  private boolean processorWorking = false;

  private boolean queue = false;

  private void inputUpdated() {
    if (processorWorking) {
      queue = true;
      return;
    }
    startProcess();
  }

  private class Processor extends Thread {
    // output just has some setText .. replace by interface and wrapper!
    String input;

    JTextArea outputter;

    List<Integer> lineStartPositions;

    Processor(String input, JTextArea outputter) {
      this.input = input;
      this.outputter = outputter;
    }

    private void makeLineStartPosTable() {
      lineStartPositions = new ArrayList<Integer>();
      int startPos = 0;
      while (startPos >= 0) {
        lineStartPositions.add(new Integer(startPos));
        startPos = input.indexOf('\n', startPos + 1);
      }
    }

    private int getLineStartPos(int line) {
      return lineStartPositions.get(line).intValue();
    }

    @Override
	public void run() {
      processorWorking = true;

      ResolutionContext resolver = new ResolutionContext() {

        public void earlyStreamRequest(String systemId, String user, int key) {
        }

        public InputSource resolveStream(String systemId, String user, int key)
            throws IOException {
          return resolveStream(systemId);
        }

        public InputSource resolveStream(String systemId) throws IOException {
          StringReader sr = new StringReader(input);
          InputSource source = new InputSource(sr);
          return source;
        }
      };

      MemoryErrorReporter cesspool = new MemoryErrorReporter();

      UniqueNameGenerator names = new UniqueNameGenerator();

      DefaultSimplifier defaultSimplifier = DefaultSimplifier.getInstance(
          resolver, cesspool, names);

      // document
      try {
        Stylesheet stylesheet = defaultSimplifier.getStylesheet("", resolver,
            cesspool);

        String asText = "*** Parse Errors! Could not even make a stylesheet out of that! ***";

        if (stylesheet != null) {

          StylesheetModule pm = stylesheet.getPrincipalModule();

          if (pm != null) {
            Document desiredOutput = pm
                .getDocument(StylesheetModule.CORE);
            asText = Dom4jUtil.toString(desiredOutput);
          }

        }

        outputter.setText(asText);

        errorta.setText("");

        int noHighlights = inputta.getHighlighter().getHighlights().length;
        if (noHighlights > 0) {
          inputta.getHighlighter().removeAllHighlights();
        }

        Iterator<XSLToolsException> errors = cesspool.errorIterator();

        boolean madeLineStartPosTable = false;

        while (errors.hasNext()) {

          if (!madeLineStartPosTable) {
            makeLineStartPosTable();
            madeLineStartPosTable = true;
          }

          XSLToolsException ex = errors.next();
          errorta.append(ex.toString());
          if (ex instanceof XSLToolsLocatableException) {
            XSLToolsLocatableException lex = (XSLToolsLocatableException) ex;
            errorta.append("\n" + lex.getParsePositionAsReadableString());

            ParseLocation ploc = lex.getParseLocation();

            if (ploc != null) {
              int startLine = ploc.elementStartTagBeginningLine();
              int startCol = ploc.elementStartTagBeginningColumn();

              int endLine = lex.getExtent() == ParseLocation.Extent.TAG ? ploc
                  .elementStartTagEndLine() : ploc.elementEndTagEndLine();

              int endCol = lex.getExtent() == ParseLocation.Extent.TAG ? ploc
                  .elementStartTagEndColumn() : ploc.elementEndTagEndColumn();

              int startPos = getLineStartPos(startLine - 1) + startCol;
              int endPos = getLineStartPos(endLine - 1) + endCol;

              try {
                inputta.getHighlighter().addHighlight(startPos, endPos,
                    DefaultHighlighter.DefaultPainter);
              } catch (BadLocationException blex) {
                System.err.println(blex);
              }
            }
          } else {
            errorta.append("\nparse location missing");
          }
          errorta.append("\n-----\n");
        }
        if (noHighlights > 0 || madeLineStartPosTable) {
          inputta.repaint();
        }
      } catch (XSLToolsException ex) {
        System.err.println("Escaping exception! " + ex);
        // StackTraceElement[] st = ex.getStackTrace();
        // System.err.println(st[0]);
        ex.printStackTrace();
      }

      processorWorking = false;

      boolean hadQueue = queue;
      queue = false;
      if (hadQueue)
        startProcess();
    }
  }

  private void startProcess() {
    new Processor(inputta.getText(), outputta).start();
  }

  private void init() {
    inputta.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent evt) {
        inputUpdated();
      }

      public void insertUpdate(DocumentEvent evt) {
        inputUpdated();
      }

      public void removeUpdate(DocumentEvent evt) {
        inputUpdated();
        try {
          int offset = evt.getOffset();
          /* Position p = */doc.createPosition(offset);
          // System.out.println(p);
        } catch (BadLocationException e) {
        }
      }
    });
    inputta.addCaretListener(new CaretListener() {
      public void caretUpdate(CaretEvent evt) {
        int offset = evt.getDot();
        int l = getLineOf(offset);
        int c = getColOf(offset, l);
        // System.out.println(l + "\t" + c);
        findElementsAt(l, c);
      }
    });

    lineoffsets = new int[inputta.getLineCount()];

    try {

      for (int i = 0; i < inputta.getLineCount(); i++) {
        lineoffsets[i] = inputta.getLineStartOffset(i);
      }
    } catch (BadLocationException e) {
    }
  }

  private int getLineOf(int offset) {
    int i = 1;
    while (i < lineoffsets.length && offset >= lineoffsets[i])
      i++;
    return i;
  }

  private int getColOf(int offset, int linenumber) {
    return offset - lineoffsets[linenumber - 1] + 1;
  }

  private void findElementsAt(int line, int col) {
    /*
     * for (Iterator it = parsePositions.iterator(); it.hasNext();) {
     * TextPositionToElementMapping map = (TextPositionToElementMapping) it
     * .next(); if (map.contains(line, col)) System.out.println(map); }
     * System.out.println();
     */
  }

  private static String readFile(String filename) throws IOException {
    BufferedReader bre = new BufferedReader(new FileReader(filename));
    StringBuffer result = new StringBuffer();
    String s;
    while ((s = bre.readLine()) != null) {
      result.append(s);
      result.append("\n");
    }
    return result.toString();
  }

  public static void main(String[] args) throws IOException {
    String filename = args[0];

    /*
     * ParseLoctionAppendingSAXBuilderDecorator bob = new
     * ParseLoctionAppendingSAXBuilderDecorator(); Collection dumper = new
     * ArrayList(); bob.build(filename, dumper);
     */

    String filecontents = readFile(filename);

    JFrame input = new JFrame("XSLT Simplifier");
    input.getContentPane().add(new InteractiveEditorAndTree(filecontents));
    input.setSize(new Dimension(400, 800));
    // input.pack();
    input.setVisible(true);
    input.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // ConfigurationFactory.setFormattingConfiguration(new
    // FormattingConfiguration(FormattingConfiguration.PRESERVE_WHITESPACEONLY_TEXTNODES,
    // "", false));
    ConfigurationFactory.setReorganizingFormattingConfiguration();
  }
}