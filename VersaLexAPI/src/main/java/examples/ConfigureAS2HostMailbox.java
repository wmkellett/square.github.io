package examples;

import java.io.*;
import java.util.*;

import com.cleo.lexicom.external.*;

/*******************************************************************************
 * This example demonstrates:
 * - How to configure a new AS2 trading partner into LexiCom
 ******************************************************************************/
public class ConfigureAS2HostMailbox {
  ILexiCom lexicom;
  Hashtable runTable = new Hashtable();
  public static void main(String[] args)
  {
    new ConfigureAS2HostMailbox();
  }
  public ConfigureAS2HostMailbox()
  {
    try {
/*------------------------------------------------------------------------------
 *    Get a client instance of LexiCom
 *----------------------------------------------------------------------------*/
      lexicom = LexiComFactory.getVersaLex(LexiComFactory.LEXICOM,
                                           "C:\\Program Files\\LexiCom",
                                           LexiComFactory.CLIENT_ONLY);

/*------------------------------------------------------------------------------
 *    Create and configure a new AS2 trading partner (i.e. host/mailbox).  Just
 *    configure it to send to ourselves (i.e. looptest).
 *----------------------------------------------------------------------------*/
      String[] partner = {"Looptest AS2", "myMailbox"};
      lexicom.activateHost("Generic AS2", partner[lexicom.HOST_INDEX], true);

      lexicom.setProperty(lexicom.HOST, partner, "Address", "localhost");
      lexicom.setProperty(lexicom.HOST, partner, "Port", "5080");

      // use the cert file exported in ConfigureAS2Listener.java
      File file = new File("certs" + File.separator + "mysignencrypt.cer");
      lexicom.setProperty(lexicom.MAILBOX, partner, "Servercertfile", file.getPath());

      lexicom.setProperty(lexicom.MAILBOX, partner, "Encryptedrequest", "True");
      lexicom.setProperty(lexicom.MAILBOX, partner, "Signedrequest", "True");
      lexicom.setProperty(lexicom.MAILBOX, partner, "Receiptdesired", "True");
      lexicom.setProperty(lexicom.MAILBOX, partner, "Signedreceipt", "True");
      lexicom.setProperty(lexicom.MAILBOX, partner, "Header", new String[] {"PUT AS2-From=looptest",
                                                                            "PUT AS2-To=looptest",
                                                                            "PUT Subject=test",
                                                                            "PUT Content-Type=Plain Text"});
      lexicom.save(partner[0]);

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
