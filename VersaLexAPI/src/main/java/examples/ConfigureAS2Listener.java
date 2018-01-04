package examples;

import java.io.*;

import com.cleo.lexicom.external.*;
import com.cleo.lexicom.certmgr.external.*;

/*******************************************************************************
 * This example demonstrates:
 * - How to configure the Local Listener for AS2 via the API
 ******************************************************************************/
public class ConfigureAS2Listener implements LexiComLogListener {
  public static void main(String[] args)
  {
    new ConfigureAS2Listener();
  }
  public ConfigureAS2Listener()
  {
    try {
/*------------------------------------------------------------------------------
 *    Get a server instance of LexiCom
 *----------------------------------------------------------------------------*/
      ILexiCom lexicom = LexiComFactory.getVersaLex(LexiComFactory.LEXICOM,
                                                    "C:\\Program Files\\LexiCom",
                                                    LexiComFactory.SERVER_ONLY);

/*------------------------------------------------------------------------------
 *    Listen for log events
 *----------------------------------------------------------------------------*/
      lexicom.addLogListener(this);

/*------------------------------------------------------------------------------
 *    Find the local listener
 *----------------------------------------------------------------------------*/
      String[] hosts = lexicom.list(lexicom.HOST, null);
      String[] listener = null;
      int i;
      for (i = 0; i < hosts.length; i++) {
        String[] host = {hosts[i]};
        String[] prop = lexicom.getProperty(lexicom.HOST, host, "local");
        if (prop[0].equals("True")) {
          listener = new String[] {hosts[i]};
          String[] services = lexicom.list(lexicom.SERVICE, listener);
          if (services.length > 0)
            break;
        }
      }

/*------------------------------------------------------------------------------
 *    If the listener isn't there (first time), then create it
 *----------------------------------------------------------------------------*/
      if (i == hosts.length) {
        String alias = "Local Listener";
        lexicom.activateHost(alias, null, true);
        listener = new String[] {alias};
      }

/*------------------------------------------------------------------------------
 *    Generate a self-signed user certificate (with the minimum settings) for
 *    the listener if not already there
 *----------------------------------------------------------------------------*/
      String certAlias = "SIGN_ENCRYPT_CERT";
      String certPassword = "mytest";

      ICertManagerRunTime certmgr = lexicom.getCertManager();
      if (certmgr.getCertificateChain(certAlias) == null) {
        CertificateInfo certinfo = new CertificateInfo();
        certinfo.setCommonName("myname or myservicename");
        certinfo.setCountry("US");
        certinfo.setKeyEncipherment(true);
        certinfo.setDigitalSignature(true);
        certmgr.generateUserCertKey(certAlias, certinfo, certPassword, false);

/*------------------------------------------------------------------------------
 *      Export the user certificate to the certs/ directory.  Normally you wouldn't
 *      necessarily export it to your own certs directory - you would just export
 *      it to a location in which you could then send (email) it to your trading
 *      partners.  But for our examples, ConfigureAS2HostMailbox.java configures
 *      an AS2 looptest and expects the certificate to already be in the certs/
 *      directory.
 *----------------------------------------------------------------------------*/
        File file = lexicom.getAbsolute(new File("certs", "mysignencrypt.cer"));
        certmgr.exportUserCert(certAlias, file, true, true);
      }

/*------------------------------------------------------------------------------
 *    Add the user certificate to the listener for signing/encryption
 *    Refer to 'Host Files' in the 'LexiCom XML File Formats' appendix
 *      of the LexiCom User's Guide for information on host files.
 *----------------------------------------------------------------------------*/
      lexicom.setProperty(lexicom.HOST, listener, "Localsigncertalias", certAlias);
      lexicom.setProperty(lexicom.HOST, listener, "Localsigncertpassword", certPassword);
      lexicom.setProperty(lexicom.HOST, listener, "Usesamecerts", "True");

      String[] as2service = {listener[0], "AS2"};
      lexicom.setProperty(lexicom.SERVICE, as2service, "Hostname", "localhost"); // this is supposed to be my
                                                                                 // external address, but for
                                                                                 // testing purposes we're just
                                                                                 // setting it to localhost
      lexicom.setProperty(lexicom.SERVICE, as2service, "Localadminemailaddr", "myemail@mycompany.com");
      lexicom.save(listener[0]);

/*------------------------------------------------------------------------------
 *    Start the listener (and the scheduler)
 *----------------------------------------------------------------------------*/
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
      lexicom.removeLogListener(this);
      lexicom.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(0);
  }

/*------------------------------------------------------------------------------
 * Print the log events
 *----------------------------------------------------------------------------*/
  public void log(LexiComLogEvent e) {
    System.out.println("Message=>" + e.getMessage() + "<");
  }
}
