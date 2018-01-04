package examples;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import com.cleo.lexicom.external.*;

/*******************************************************************************
 * This example demonstrates:
 * - How to use a mailbox controller for an open-ended SMTP host and send a
 *   test email
 ******************************************************************************/
public class UseSMTPOpenEndedHost {
  public static void main(String[] args)
  {
    new UseSMTPOpenEndedHost();
  }
  public UseSMTPOpenEndedHost() {
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
 *    Get a mailbox controller for the open-ended SMTP host
 *    Change the host and mailbox aliases below if necessary
 *----------------------------------------------------------------------------*/
      String host    = "Open SMTP"; // SMTP host with address set to *
      String mailbox = "myMailbox"; // default mailbox
      String[] path  = {host, mailbox};
      IMailboxController controller = lexicom.getMailboxController(path);

/*------------------------------------------------------------------------------
 *    Send a test email with two attachments
 *----------------------------------------------------------------------------*/
      LexiComOutgoing[] outgoing = new LexiComOutgoing[2];
      File file1 = new File(VLTraderHome + "\\outbox\\test\\test.edi");
      outgoing[0] = new LexiComOutgoing(new FileInputStream(file1));
      outgoing[0].setFilename(file1.getName());
      File file2 = new File(file1.getPath());
      outgoing[1] = new LexiComOutgoing(new FileInputStream(file2));
      outgoing[1].setFilename(file2.getName());

      // Look at the mailbox SMTP tab in VLTrader to see all the possible parameters
      Properties parms = new Properties();
      parms.setProperty("To", "aevett@cleo.com");
      parms.setProperty("Subject", "Just testing");
      parms.setProperty("[Inline]", "See attachment");
      boolean success = controller.send(new RemoteLexiComOutgoing(outgoing), parms, false);

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
 *    Close the instance
 *----------------------------------------------------------------------------*/
      lexicom.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(0);
  }
}
