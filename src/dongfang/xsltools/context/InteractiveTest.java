/*
 * dongfang M. Sc. Thesis
 * Created on 2005-04-27
 */
package dongfang.xsltools.context;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.xml.sax.InputSource;

import dongfang.xsltools.exceptions.XSLToolsException;
import dongfang.xsltools.resolver.ComboInputSource;
import dongfang.xsltools.util.Dom4jUtil;
import dongfang.xsltools.validation.ValidationResult;

/**
 * @author dongfang
 */
public class InteractiveTest extends InteractiveValidationContext implements
    ActionListener {

  private File file;
  private String string;
  private JFrame f;
  private JTextField stringf;
  private JButton again = new JButton("again");
  private JButton ommer = new JButton("new run");

  InteractiveTest() throws Exception {
    startValidator(this);
    waitForAllRequests();
  }

  public static void main(String[] args) throws Exception {
    new InteractiveTest();
  }

  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() instanceof JFileChooser) {
      JFileChooser slam = (JFileChooser) evt.getSource();
      file = slam.getSelectedFile();
    } else if (evt.getSource() == again) {
      // reset();
    }
    else if (evt.getSource() == ommer) {
      // restart();
    }
    else {
      string = stringf.getText();
    }
    f.setVisible(false);
    f.dispose();
  }

  /*
   * (non-Javadoc)
   * 
   * @see dongfang.xsltools.context.InteractiveResolutionContext#getInputSource(java.lang.String,
   *      java.lang.String)
   */
  @Override
boolean isCurrentRequestServed() {
    if (requestType == STREAM_REQUEST) {
      if (requestedId.equals(SystemInterfaceStrings
          [INPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY]))
        ;
      if (requestedId.equals(SystemInterfaceStrings
          [OUTPUT_SCHEMA_PRIMARY_COMPONENT_IDENTIFIER_KEY]))
        ;
      JFileChooser fc = new JFileChooser("test/resources/triples");
      fc.addActionListener(this);
      f = new JFrame("Please provide the " + requestedResourceTypeName);
      JLabel l = new JLabel("Gimme a file for the ID: " + requestedId);
      f.setLayout(new BorderLayout());
      f.getContentPane().add(fc, BorderLayout.CENTER);
      f.getContentPane().add(l, BorderLayout.SOUTH);
      f.pack();
      f.setVisible(true);

      // kunne for så vidt returnere???
      while (file == null) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
      }
      
      InputSource slamkage = new ComboInputSource(requestedId, "utf-8");

      try {
        slamkage.setByteStream(new FileInputStream(file));
        
        
        //deliverResourceFromEnvironment(requestedId, slamkage);
        
        
        
      } catch (IOException ex) {
        // deliverBadExcuse(ex);
      }
      file = null;
    }

    else if (requestType == STRING_REQUEST) {
      stringf = new JTextField(20);
      f = new JFrame(requestedId);
      f.setLayout(new BorderLayout());
      JButton go = new JButton("go");
      go.addActionListener(this);
      f.getContentPane().add(stringf, BorderLayout.WEST);
      f.getContentPane().add(go, BorderLayout.EAST);
      f.pack();
      f.setVisible(true);

      while (string == null)
        ;

      if ("".equals(string))
        string = null;

      
      //deliverResourceFromEnvironment(requestedId, string);
      
      
      
      string = null;
    }
    return true;
  }

  @Override
public void earlyStreamRequest(String systemId, String user, int s) {
  }

  public void earlyStringRequest(String id, String user, String none, int s) {
  }

  @Override
  protected synchronized void notifyTerminated() {
    notify();
  }

  void presentValidationResultToEnvironment(String result) {
    System.out.println(result);
    presentRoundButtons();
  }

  void presentValidationResultToEnvironment(ValidationResult result) {
    if (result != null) {
      Dom4jUtil.debugPrettyPrint(result.getErrorReport());
    }
    presentRoundButtons();
  }

  void presentNonFatalErrorsToEnvironment(List<XSLToolsException> errors) {
    presentRoundButtons();
  }

  InputSource getCachedStreamResource(String systemId) {
    return null;
  }

  String getCachedStringResource(String id) {
    return null;
  }

  public void pushMessage(String target, String message) {
    System.err.println(message + "-->" + target);
  }

  public boolean restarted() {
    // TODO Auto-generated method stub
    return true;
  }

  void presentRoundButtons() {
    System.err.println("Butt's!");
  }

  /*
  @Override
  boolean getStop() {
    return false;
  }
  */

  @Override
  public String getSessionType() {
    return "test";
  }

  @Override
  protected void doPresentNonFatalErrorsToEnvironment(List<XSLToolsException> errors) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void doPresentValidationResultToEnvironment(String result) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void doPresentValidationResultToEnvironment(ValidationResult result) {
    // TODO Auto-generated method stub
    
  }

  public String resolveString(String systemId, String user, String none, int humanKey) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void clearMessages() {
    // TODO Auto-generated method stub
    
  }
  
  
  
  
}
