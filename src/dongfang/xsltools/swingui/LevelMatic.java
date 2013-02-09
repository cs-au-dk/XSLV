package dongfang.xsltools.swingui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * A sequence to JCheckBoxes: Call the rightmost selected box X.
 * Every box on the left of X is automatically selected.
 * Used in an UI for selecting a set of features 
 * {f_1}, {f_1,f_2}, {f_1,f_2,f_3} etc.
 * (as opposed to radio buttons that can only select {f_i} for some i,
 * or a set of undiciplined JCheckBoxes that can select any subset
 * of {f_1,..,f_n}.
 * @author dongfang
 */
public class LevelMatic extends JPanel implements ActionListener {
  /**
	 * 
	 */
	private static final long serialVersionUID = -6235908174189311111L;

List<JCheckBox> boxen = new ArrayList<JCheckBox>();

  private int level;

  boolean alreadyLookingIntoIt = false;

  public LevelMatic() {
    setLayout(new GridLayout(1, 0));
    setBorder(BorderFactory.createEtchedBorder());
  }

  public void addOption(String title) {
    JCheckBox b = new JCheckBox(title);
    boxen.add(b);
    add(b);
    b.addActionListener(this);
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    if (level == 0)
      boxen.get(0).setSelected(false);
    else
      boxen.get(level).setSelected(true);
    update(level, boxen.get(level));
  }

  public synchronized void actionPerformed(ActionEvent evt) {
    if (alreadyLookingIntoIt)
      return;
    alreadyLookingIntoIt = true;

    Object o = evt.getSource();
    int i = boxen.indexOf(o);
    JToggleButton box = (JToggleButton) o;
    update(i, box);
  }

  private void update(int i, JToggleButton box) {
    if (box.isSelected()) {
      // select all up to this
      for (int j = 0; j < i; j++) {
        boxen.get(j).setSelected(true);
      }
    } else {
      // unselect all down to this
      for (int j = boxen.size() - 1; j > i; j--) {
        boxen.get(j).setSelected(false);
      }
    }
    level = i + (box.isSelected() ? 1 : 0);
    alreadyLookingIntoIt = false;
  }

  public static void main(String[] args) {
    JFrame f = new JFrame("Nussemus");
    LevelMatic m = new LevelMatic();
    m.addOption("foo1");
    m.addOption("foo2");
    m.addOption("foo3");
    m.addOption("foo4");

    f.getContentPane().add(m);

    f.pack();

    f.setVisible(true);
  }
}
