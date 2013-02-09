/*
 * dongfang M. Sc. Thesis
 * Created on 2005-03-23
 */
package dongfang.xsltools.experimental.general_programming;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author snk
 */
public class JavaLoggingExperiment {
  static class LoggingSubClass1 {
    Logger mine = Logger
        .getLogger("dongfang.xsltools.experimental.general_programming.JavaLoggingExperiment.SubClass1");

    /**
     * 
     */
    LoggingSubClass1() {
      // mine.addHandler(new ConsoleHandler());
      mine.setLevel(Level.FINE);
    }

    void logSomeInfo() {
      mine.log(Level.INFO, "Subclass1 INFO!");
    }

    void logSomeFine() {
      mine.log(Level.FINE, "Subclass1 FINE!");
    }

    void logSomeConfig() {
      mine.log(Level.CONFIG, "Subclass1 CONFIG!");
    }

    void logSomeSevere() {
      mine.log(Level.SEVERE, "Subclass1 SEVERE!");
    }

  }

  static class LoggingSubClass2 {
    Logger mine = Logger
        .getLogger("dongfang.xsltools.experimental.general_programming.JavaLoggingExperiment");
  }

  public static void main(String[] args) {
    /*
     * Logger common = Logger
        .getLogger("dongfang.xsltools.experimental.general_programming.JavaLoggingExperiment");
     */
    // common.addHandler(new ConsoleHandler());
    LoggingSubClass1 sub1 = new LoggingSubClass1();

    sub1.logSomeFine();
    sub1.logSomeInfo();
    sub1.logSomeConfig();
    sub1.logSomeSevere();

    try {
      Handler h = new FileHandler("sub1.log");
      sub1.mine.addHandler(h);
      h.setLevel(Level.FINE);
      h.setFormatter(new SimpleFormatter());
    } catch (IOException e) {
    }

    sub1.logSomeFine();
    sub1.logSomeInfo();
    sub1.logSomeConfig();
    sub1.logSomeSevere();

  }
}