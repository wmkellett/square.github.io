package examples;

import java.io.*;
import java.util.*;

import com.cleo.lexicom.external.*;

/*******************************************************************************
 * This example demonstrates:
 * - How to configure a new FTP trading partner into LexiCom.  In this case,
 *   we're the FTP client and we're connecting to an FTP server
 ******************************************************************************/
public class ConfigureFTPHostMailbox {
  ILexiCom lexicom;
  Hashtable runTable = new Hashtable();
  public static void main(String[] args)
  {
    new ConfigureFTPHostMailbox();
  }
  public ConfigureFTPHostMailbox()
  {
    try {
/*------------------------------------------------------------------------------
 *    Get a client instance of LexiCom
 *----------------------------------------------------------------------------*/
      lexicom = LexiComFactory.getVersaLex(LexiComFactory.LEXICOM,
                                           "C:\\Program Files\\LexiCom",
                                           LexiComFactory.CLIENT_ONLY);

/*------------------------------------------------------------------------------
 *    Create and configure a new FTP trading partner (i.e. host/mailbox).  Just
 *    configure it to send to ourselves (i.e. looptest).
 *----------------------------------------------------------------------------*/
      String[] partner = {"Cleotest FTPs", "myMailbox"};
      lexicom.activateHost("Generic FTPs", partner[lexicom.HOST_INDEX], true);

      lexicom.setProperty(lexicom.HOST, partner, "Address", "test.cleo.com");
      lexicom.setProperty(lexicom.HOST, partner, "Port", "990");
      lexicom.setProperty(lexicom.HOST, partner, "Securitymode", "SSL Implicit");
      lexicom.setProperty(lexicom.HOST, partner, "Substitutepassiveipaddress", "True");

/*------------------------------------------------------------------------------
 *    Normally we'd also want to acquire and then copy the server's SSL certificate
 *    to the certs/ directory so that it is trusted.  In this case, since we're
 *    going to the Cleo test site, it's already trusted.
 *----------------------------------------------------------------------------*/

      lexicom.setProperty(lexicom.MAILBOX, partner, "Username", "cleoftptest");
      lexicom.setProperty(lexicom.MAILBOX, partner, "Password", "*ExoHFhwQEg**");

      lexicom.save(partner[0]);

/*------------------------------------------------------------------------------
 *    Create an action that just lists the FTP mailbox contents.
 *----------------------------------------------------------------------------*/
      lexicom.create(lexicom.ACTION, partner, "list", false);
      String[] list = new String[] {partner[lexicom.HOST_INDEX],
                                    partner[lexicom.MAILBOX_INDEX],
                                    "list"};
      lexicom.setProperty(lexicom.ACTION, list, "Commands", new String[] {"DIR"});
      lexicom.save(partner[0]);

/*------------------------------------------------------------------------------
 *    We don't show running this action here.  You can do that yourself!
 *----------------------------------------------------------------------------*/

/*------------------------------------------------------------------------------
 *    Close down the LexiCom instance
 *----------------------------------------------------------------------------*/
      lexicom.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(0);
  }
}
