package dongfang.xsltools.experimental.swing;

import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

public class EditorContextMenuExperiment extends JPanel {
  /**
	 * 
	 */
	private static final long serialVersionUID = -4313602380296340976L;

EditorContextMenuExperiment() {
    super();

    final JPopupMenu pop = new JPopupMenu("gedefims");
    pop.add(new JMenuItem("mjfskdl"));
    pop.add(new JMenuItem("slfkd"));
    pop.add(new JPopupMenu("hundeprut"));
    
    JTextArea ta = new JTextArea() {
      /**
		 * 
		 */
		private static final long serialVersionUID = 7827850009011882377L;

	@Override
      public void processMouseEvent(MouseEvent e) {
        if (e.isPopupTrigger()) {
          System.err.println("Showing Pops!");
          pop.show(this, e.getX(), e.getY());
          int i = viewToModel(e.getPoint());
          try {
          int l = getLineOfOffset(i);
          System.err.println(l);
          System.err.println(i - getLineStartOffset(l));
          } catch (BadLocationException ex){}
          
        } else {
          super.processMouseEvent(e);
        }
      }
    };
//    ta.add(pop);
    ta
        .setText(" sjdlfkj dsoifj dsfoisifhewiof hewoif hewoif jewfoijewfjewf\n"
            + "sfj sdfuwsufewfewio fuewoif jewif ewjfef hewfkjewfjewnfj kewnf jkenfj\n"
            + "fje wfkjewfk jdefkd jfkld jfdlkf jdlkf jdsklfjdsklfjdsklfj dskfjds \n"
            + "sfjdsjf dsjf dsklfjdslkfjf dsk fjdslkf jdskfjsfk sjflkdsf jdslkf jdslkf");
    add(ta);
  }

  public static void main(String[] args) { 
    JFrame f = new JFrame("Himlens Kloakker");
    f.getContentPane().add(new EditorContextMenuExperiment());
    f.pack();
    f.setVisible(true);
  }
}
