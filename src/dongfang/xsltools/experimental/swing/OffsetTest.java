/*
 * Created on Feb 15, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package dongfang.xsltools.experimental.swing;

/**
 * @author dongfang
 */
// OffsetTest.java
// Show line start/end offsets in a JTextArea.
//
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

public class OffsetTest {
  public static void main(String[] args) {
    JTextArea ta = new JTextArea();
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    JScrollPane scroll = new JScrollPane(ta);

    // Add three lines of text to the JTextArea.
    ta.append("The first line.\n");
    ta.append("Line Two!\n");
    ta.append("This is the 3rd line of this document.");

    // Print some results . . .
    try {
      for (int n = 0; n < ta.getLineCount(); n += 1)
        System.out.println("line " + n + " starts at "
            + ta.getLineStartOffset(n) + ", ends at " + ta.getLineEndOffset(n));
      System.out.println();

      int n = 0;
      while (true) {
        System.out.print("offset " + n + " is on ");
        System.out.println("line " + ta.getLineOfOffset(n));
        n += 13;
      }
    } catch (BadLocationException ex) {
      System.out.println(ex);
    }

    // Layout . . .
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.getContentPane().add(scroll, java.awt.BorderLayout.CENTER);
    f.setSize(150, 150);
    f.setVisible(true);
  }
}