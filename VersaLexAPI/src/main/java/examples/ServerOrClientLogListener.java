package examples;

import java.io.*;
import org.w3c.dom.*;
import com.cleo.lexicom.external.*;

/*******************************************************************************
 * This example demonstrates:
 * - The coding difference between SERVER_ONLY and CLIENT_ONLY models, which is
 *   minimal.  The bigger difference is in functionality, where there can be
 *   only one SERVER_ONLY instance per installation while there can be one
 *   CLIENT_ONLY instance per JVM
 * - How to interrogate <Result> log events
 ******************************************************************************/
public class ServerOrClientLogListener implements LexiComLogListener {
  ILexiCom lexicom;
  public static void main(String[] args)
  {
    new ServerOrClientLogListener();
  }
  public ServerOrClientLogListener()
  {
    try {
/*------------------------------------------------------------------------------
 *    Either attach to an already running LexiCom service/daemon or BE the
 *    LexiCom service/daemon.
 *
 *    The SERVER_ONLY model is more efficient than the CLIENT_ONLY model in that
 *    there is only one JVM process rather than two, but CLIENT_ONLY may avoid
 *    conflicts between your application JARs and VersaLex - e.g. XML processors,
 *    mailcap properties, etc.  (The combination CLIENT_OR_SERVER model is not
 *    recommended.)
 *
 *    Refer to the LexiComFactory javadoc for required classpath and other JVM
 *    parameters for either model
 *----------------------------------------------------------------------------*/
      int model = LexiComFactory.CLIENT_ONLY;
//      int model = LexiComFactory.SERVER_ONLY;

      lexicom = LexiComFactory.getVersaLex(LexiComFactory.LEXICOM,
                                           "C:\\Program Files\\LexiCom",
                                           model);

/*------------------------------------------------------------------------------
 *    Listen for log events
 *----------------------------------------------------------------------------*/
      lexicom.addLogListener(this);

/*------------------------------------------------------------------------------
 *    If we're the server, need to start the listener and the schedule ourselves
 *    (if we're a client, then the server's already started them)
 *----------------------------------------------------------------------------*/
      if (model == LexiComFactory.SERVER_ONLY)
        lexicom.startService();

/*------------------------------------------------------------------------------
 *    Wait until interrupted
 *----------------------------------------------------------------------------*/
      while (true) {
        try {
          Thread.sleep(60000);
        } catch (InterruptedException e) {
          break;
        }
      }

/*------------------------------------------------------------------------------
 *    Remove the log listener and close down the LexiCom instance
 *----------------------------------------------------------------------------*/
      if (model == LexiComFactory.CLIENT_ONLY)
        lexicom.removeLogListener(this);
      lexicom.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.exit(0);
  }

/*------------------------------------------------------------------------------
 * Watch for incoming files
 *----------------------------------------------------------------------------*/
  public void log(LexiComLogEvent e) {
    try {
      // Dump out log event
//      print(e);

      Element event = e.getEvent();
      if (event.getNodeName().equals("Result")) {

        String status = event.getAttribute("text");
        String source = event.getAttribute("source");
        String destination = event.getAttribute("destination");
        String direction = event.getAttribute("direction");

/*------------------------------------------------------------------------------
 *      Successful result
 *----------------------------------------------------------------------------*/
        if (status.equals("Success") ||
            status.equals("Warning") ||
            status.equals("-")) { // "-" status indicates warning as well

          boolean incoming = false;
          boolean outgoing = false;
          if (direction != null) {
            if (direction.equals("Incoming") || // reserved for future use
                direction.equals("Host->Local") ||
                direction.equals("Remote->Local"))
              incoming = true;
            else if (direction.equals("Outgoing") || // reserved for future use
                     direction.equals("Local->Host") ||
                     direction.equals("Local->Remote"))
              outgoing = true;
          }

          // check if AS2/AS3 MDN or ebMS acknowledgment
          boolean isReceipt = false;
          if (source != null &&
              (source.equals("MDN") ||
               source.equals("Acknowledgment")))
            isReceipt = true;

          // ignore receipts
          if (!isReceipt) {
            if (incoming) {
              File file = lexicom.getAbsolute(new File(destination));
              // process new file from trading partner
              System.out.println("Incoming=>" + file.getPath() + "<");

            } else if (outgoing) {
              File file = lexicom.getAbsolute(new File(source));
              // add sent file to my audit trail
              System.out.println("Outgoing=>" + file.getPath() + "<");
            }
          }

/*------------------------------------------------------------------------------
 *      On failure, alert the system administrator (unless EmailOnFail is already
 *      configured into LexiCom/VLTrader and that suffices)
 *----------------------------------------------------------------------------*/
        } else if (status.equals("Exception") ||
                   status.equals("Error")) {
          System.out.println("Alert=>" + e.getMessage() + "<");

/*------------------------------------------------------------------------------
 *      A user interactively interrupted a transfer, so may either want to ignore
 *      or alert the system administrator
 *----------------------------------------------------------------------------*/
        } else if (status.equals("Interrupted")) {

/*------------------------------------------------------------------------------
 *      Just ignore retries, another result will be logged with the final result
 *----------------------------------------------------------------------------*/
        } else if (status.equals("Retry")) {

        // unexpected status
        } else {
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

  }

/*------------------------------------------------------------------------------
 * Print out all of the log event contents
 *----------------------------------------------------------------------------*/
  private void print(LexiComLogEvent e) {
    System.out.print("Source=>");
    if (e.getSource() != null) {
      for (int i = 0; i < e.getSource().length; i++) {
        System.out.print(e.getSource()[i]);
        if (i < e.getSource().length - 1)
          System.out.print("\\");
      }
    }
    System.out.println("<");

    System.out.println(" Event=>" + e.getEvent().getNodeName() + "<");

    NamedNodeMap list = e.getEvent().getAttributes();
    for (int i = 0; i < list.getLength(); i++)
      System.out.println("  Attr=>" + list.item(i).getNodeName() + "=" + list.item(i).getNodeValue() + "<");

    NodeList children = e.getEvent().getChildNodes();
    for (int i = 0; i < children.getLength(); i++)
      System.out.println("  Text=>" + children.item(i).getNodeValue() + "<");

    System.out.println(" Message=>" + e.getMessage() + "<");
    System.out.println(" ThreadID=>" + e.getThreadID() + "<");
    System.out.println(" CommandID=>" + e.getCommandID() + "<");
    System.out.println(" Date=>" + e.getDate() + "<");
    System.out.println(" InLogFile=>" + e.isInLogFile() + "<");
    System.out.println(" InMessageList=>" + e.isInMessageList() + "<");
  }
}
