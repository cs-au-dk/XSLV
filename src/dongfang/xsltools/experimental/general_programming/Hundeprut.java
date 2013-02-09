package dongfang.xsltools.experimental.general_programming;

public class Hundeprut {
  public static void main(String[] args) {
    new Hundeprut().run();
  }
  
  int state;
 // boolean showingButton;
  int button;
  
  synchronized void getResource(int i) {
    System.out.println("Requesting " + i + "th resource");
    state = 2;
    try {
      wait();
    } catch (InterruptedException ex) {}
    System.out.println("Resource get ok, leavin'");
  }
  
  synchronized void pushResult() {
    System.out.println("Pushing result");
    state = 3;
  }
  
  void showButton() {
    state = 4;
  }
  
  synchronized void process() {
    if (state == 4) {
      //showingButton = false;
      if (Math.random() > 0.5) // simulated edit, no button press neither rerun or reset
        pressAButton();
    }
    
    if (state == 0) {
      state = 2;
      System.err.println("Starting all a-over");
      new Gedefims(this).start();
    }
    else if (state == 2) {
//      System.out.println("Providing a something");
      notify(); // provide a something.
    } else if (state == 3) {
      showButton();
    }
  }
  
  synchronized void pressAButton() {
    System.out.println("Pressing that button");
    button = (int)(Math.random() * 3);
    if (button == 0) {
      System.out.println("Reset");
      state = 0;
    } else {
      System.out.println("Rerun");
    }
    notify();
  }
  
  synchronized int waitForButton() {
    try {
      wait();
    } catch(InterruptedException ex) {}
    return button;
  }
  
  void run () {
    while(true) {
      try {
      Thread.sleep(10);
      } catch(InterruptedException ex) {}
      process();
    }
  }
}

/*
 * Consumer of data
 */
class Gedefims extends Thread {
  Hundeprut h;
  
  Gedefims(Hundeprut h) {
    this.h = h;
  }

  @Override
public void run() {
    boolean stop = false;
    while (!stop) {
      for (int i=0; i<3 ; i++) {
        h.getResource(i);
      }
      System.out.println("Resources loaded; crunching numbers");
      h.pushResult();
      stop = h.waitForButton() == 0;
    }
    System.err.println("dying");
  }
  
  @Override
public void finalize() throws Throwable {
    super.finalize();
    System.err.println("*** RIP ***");
  }
}