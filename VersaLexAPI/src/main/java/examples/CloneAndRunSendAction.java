package examples;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import com.cleo.lexicom.beans.*;
import com.cleo.lexicom.external.*;

/*******************************************************************************
 * This example demonstrates:
 * - How to clone an action, set the action's commands, run the action, watch
 *   the log for the <Result> and <End> events, and then delete the action
 ******************************************************************************/
public class CloneAndRunSendAction implements LexiComLogListener {
  ILexiCom lexicom;
  Hashtable runTable = new Hashtable();
  public static void main(String[] args)
  {
    new CloneAndRunSendAction();
  }
  public CloneAndRunSendAction()
  {
    try {
/*------------------------------------------------------------------------------
 *    Get a client instance of LexiCom and listen for log events
 *----------------------------------------------------------------------------*/
      lexicom = LexiComFactory.getVersaLex(LexiComFactory.LEXICOM,
                                           "C:\\Program Files\\LexiCom",
                                           LexiComFactory.CLIENT_ONLY);
      lexicom.addLogListener(this);

/*------------------------------------------------------------------------------
 *    We're just going to clone one send action and run it, but more than likely
 *    an application like this would be fed sends to perform and feed results
 *    back over and over again.
 *
 *    Looptest AS2\myMailbox is configured by ConfigureAS2HostMailbox.java
 *----------------------------------------------------------------------------*/
      String host    = "Looptest AS2";
      String mailbox = "myMailbox";
      String action  = "send";
      String[] path  = {host, mailbox, action};

      // clone a new send action
      while (true) {
        String alias = action + String.valueOf(System.currentTimeMillis());
        try {
          lexicom.clone(ILexiCom.ACTION, path, alias, false);
          path[ILexiCom.ACTION_INDEX] = alias;
          break;
        } catch (LexBeanException ex2) {
          if (!ex2.isAlreadyExists()) // if by chance the action name already exists,
                                      // just try again
            throw ex2;
        }
      }

      // send the canned test.edi file
      String[] commands = {"PUT \"" + lexicom.getAbsolute("outbox" + File.separator + "test" + File.separator + "test.edi") + "\""};
      lexicom.setProperty(lexicom.ACTION, path, "Commands", commands);

/*------------------------------------------------------------------------------
 *    Run the action
 *----------------------------------------------------------------------------*/
      Run run = new Run();
      this.runTable.put(path, run); // using a hashtable for storing run flags so
                                    // that this example could be expanded to run multiple
                                    // sends concurrently
      try {
        lexicom.startRun(lexicom.ACTION, path, null, false); // don't wait here for completion because
                                                             // getting result and end marker through log listener
      } catch (Exception ex) {
        this.runTable.remove(path);
        throw ex;
      }

/*------------------------------------------------------------------------------
 *    Wait until action is complete or interrupted
 *----------------------------------------------------------------------------*/
      while (run.isRunning()) {
        try {
          Thread.sleep(250);
        } catch (InterruptedException e) {
          break;
        }
      }
      if (run.getResult() != null)
        System.out.println("Result=>" + run.getResult().getAttribute("text") + "<");

/*------------------------------------------------------------------------------
 *    Remove the action
 *----------------------------------------------------------------------------*/
      lexicom.remove(ILexiCom.ACTION, path);

/*------------------------------------------------------------------------------
 *    Remove the log listener and close down the LexiCom instance
 *----------------------------------------------------------------------------*/
      lexicom.removeLogListener(this);
      lexicom.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(0);
  }

/*------------------------------------------------------------------------------
 * Watch the log events for our send action in order to:
 * - Save the result event
 * - Set the running flag off
 *----------------------------------------------------------------------------*/
  public void log(LexiComLogEvent e) {
    Run run = getRun(e.getSource());
    if (run != null &&
        run.isRunning()) {
      System.out.println("Message=>" + e.getMessage() + "<");

      if (e.getEvent().getNodeName().equals("Result"))
        run.setResult(e.getEvent());
      else if (e.getEvent().getNodeName().equals("Stop"))
        run.setRunning(false);
    }
  }

/*------------------------------------------------------------------------------
 * Check to see if the log event source matches our send action and return the
 * run object
 *----------------------------------------------------------------------------*/
  private Run getRun(String[] source) {
    if (source.length <= ILexiCom.ACTION_INDEX)
      return null;
    Enumeration enum1 = this.runTable.keys();
    while (enum1.hasMoreElements()) {
      String[] path = (String[])enum1.nextElement();
      if (path[ILexiCom.HOST_INDEX].equalsIgnoreCase(source[ILexiCom.HOST_INDEX]) &&
          path[ILexiCom.MAILBOX_INDEX].equalsIgnoreCase(source[ILexiCom.MAILBOX_INDEX]) &&
          path[ILexiCom.ACTION_INDEX].equalsIgnoreCase(source[ILexiCom.ACTION_INDEX]))
        return (Run)this.runTable.get(path);
    }
    return null;
  }

/*------------------------------------------------------------------------------
 * Class to hold action running flag and result event
 *----------------------------------------------------------------------------*/
  private class Run {
    boolean running = true;
    Element result;
    public void setRunning(boolean running) {
      this.running = running;
    }
    public void setResult(Element result) {
      this.result = result;
    }
    public boolean isRunning() {
      return this.running;
    }
    public Element getResult() {
      return this.result;
    }
  }
}
