package examples;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import com.cleo.lexicom.external.*;

/*******************************************************************************
 * This example demonstrates:
 * - How to create a temporary SSH FTP host, configure it, and use a mailbox controller
 *   to send a test file
 ******************************************************************************/
public class CreateAndUseSSHFTPHost {
  public static void main(String[] args)
  {
    new CreateAndUseSSHFTPHost();
  }
  public CreateAndUseSSHFTPHost() {
    String VLTraderHome = "C:\\Program Files\\VLTrader";
    try {
/*------------------------------------------------------------------------------
 *    Get a client instance of VLTrader
 *
 *    The same client instance can be used to get multiple and even
 *    concurrent mailbox controllers
 *----------------------------------------------------------------------------*/
      ILexiCom lexicom = LexiComFactory.getVersaLex(LexiComFactory.VLTRADER,
                                                    VLTraderHome,
                                                    LexiComFactory.CLIENT_ONLY);

/*------------------------------------------------------------------------------
 *    Create and configure a temporary SSH FTP trading partner (i.e. host/mailbox)
 *----------------------------------------------------------------------------*/
      String host = lexicom.activateHost("Generic SSH FTP", "SSH FTP", false); // "SSH FTP" is the preferred alias;
                                                                               // can be anything you want;
                                                                               // VLTrader will make it unique if
                                                                               // already exists
      String mailbox = "myMailbox"; // default mailbox
      String[] path  = {host, mailbox};

      lexicom.setProperty(lexicom.HOST, path, "Address", "test.cleo.com");
      lexicom.setProperty(lexicom.HOST, path, "Port", "22");

      lexicom.setProperty(lexicom.MAILBOX, path, "Username", "SSHFTP_USER");
      lexicom.setProperty(lexicom.MAILBOX, path, "Password", "*HBMaECAMDBcZCw8gTk9MSQ**"); // password encoded here
                                                                                           // so not advertised, but can be
                                                                                           // set in cleartext
      lexicom.save(host);

/*------------------------------------------------------------------------------
 *    Get a mailbox controller for the new host/mailbox
 *----------------------------------------------------------------------------*/
      IMailboxController controller = lexicom.getMailboxController(path);

/*------------------------------------------------------------------------------
 *    Register a log listener
 *----------------------------------------------------------------------------*/
      // create temp action right away so can register log listener
      String[] temppath = controller.createTempAction("send");

      final StringBuffer sb = new StringBuffer();
      LexiComLogListener listener = new LexiComLogListener() {
        public void log(LexiComLogEvent e) {
          if (!e.getEvent().getNodeName().equals("Stop")) {
            if (sb.length() > 0)
              sb.append("\n"); // separate messages with a newline
            sb.append(e.getMessage());
          }
        }
      };
      lexicom.addLogListener(listener, temppath);

/*------------------------------------------------------------------------------
 *    Send two test files
 *----------------------------------------------------------------------------*/
      LexiComOutgoing[] outgoing = new LexiComOutgoing[2];
      File file1 = new File(VLTraderHome + "\\outbox\\test\\test.edi");
      outgoing[0] = new LexiComOutgoing(new FileInputStream(file1));
      outgoing[0].setFilename(file1.getName());
      File file2 = new File(file1.getPath());
      outgoing[1] = new LexiComOutgoing(new FileInputStream(file2));
      outgoing[1].setFilename(file2.getName());

      // Optional property for specifying destination directory
      Properties parms = new Properties();
      parms.setProperty(controller.PUT_DESTINATION, "inbox/");

      boolean success = true;
      for (int i = 0; success && i < outgoing.length; i++)
        // last parameter is keepalive; keepalive until last file so same SSH FTP session used for both files
        success = controller.send(new RemoteLexiComOutgoing(outgoing[i]), parms, i < outgoing.length - 1);

      if (success)
        System.out.println("Success!");
      else {
        // get reason for failure
        String result = null;
        Element element = controller.getLastResult();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
          if (result == null)
            result = "";
          else
            result += ";";
          result += children.item(i).getNodeValue();
        }
        System.out.println("Failure reason=>" + result + "<");
      }

/*------------------------------------------------------------------------------
 *    Print the logged messages
 *----------------------------------------------------------------------------*/
      // first unregister log listener
      lexicom.removeLogListener(listener, temppath);

      System.out.println(sb.toString());

/*------------------------------------------------------------------------------
 *    Remove the host
 *----------------------------------------------------------------------------*/
      lexicom.remove(lexicom.HOST, path);

/*------------------------------------------------------------------------------
 *    Close the instance
 *----------------------------------------------------------------------------*/
      lexicom.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(0);
  }
}
