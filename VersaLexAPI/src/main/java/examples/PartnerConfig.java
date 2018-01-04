package examples;

import java.io.*;
import java.util.*;

import com.cleo.lexicom.*;
import com.cleo.lexicom.beans.*;
import com.cleo.lexicom.external.*;
import com.cleo.lexicom.certmgr.external.*;

/******************************************************************************
 * Sample partner configuration class for interfacing to Cleo VersaLex communications
 * engine.  This example demonstrates how to configure VersaLex to be in sync
 * with your own master trading partner profile database.  It currently only
 * demonstrates AS2.
 ******************************************************************************/
public class PartnerConfig {
  ILexiCom lexicom;
  String partner[];

  public PartnerConfig(ILexiCom lexicom, String lexHostMailbox)
    throws Exception
  {
    this.lexicom = lexicom;

    StringTokenizer tokens = new StringTokenizer(lexHostMailbox, "\\", false);
    if (tokens.countTokens() != 2)
      throw new LexiComException("Host and mailbox names must be separated by but not contain a \\ character.");

    this.partner = new String[] {tokens.nextToken(), tokens.nextToken()};
  }

/*------------------------------------------------------------------------------
 * Configure an AS2 trading partner
 *----------------------------------------------------------------------------*/
  public void configureAS2(String partnerID,
                           String preconfiguredHost)
    throws Exception
  {
    boolean newHost = createHost(preconfiguredHost);
    if (newHost) {
      if (preconfiguredHost.startsWith("Generic ")) {
        // Add code here to use the partnerID to get the partner's URL and other
        // host-level properties from your database.  For now, just setup
        // a loopback test.
        java.net.URL url = new java.net.URL("http://localhost:5080/as2");

        lexicom.setProperty(lexicom.HOST, partner, "Address", url.getHost());
        lexicom.setProperty(lexicom.HOST, partner, "Port", String.valueOf(url.getPort()));
        lexicom.setProperty(lexicom.HOST, partner, "Secure", String.valueOf(url.getProtocol().equalsIgnoreCase("https")));

        String[] syntax = lexicom.getProperty(lexicom.HOST, partner, "Syntax");
        StringTokenizer tokens = new StringTokenizer(syntax[0], "\r\n", false);
        String commandMethodPath = tokens.nextToken();
        int index = commandMethodPath.lastIndexOf(" ");
        syntax[0] = commandMethodPath.substring(0,index + 1) + url.getPath() + "\n" + tokens.nextToken();
        lexicom.setProperty(lexicom.HOST, partner, "Syntax", syntax);

        // set the forward proxy URL, if necessary
        // lexicom.setProperty(lexicom.HOST, partner, "Proxyfirewall", "http://10.10.1.10:8080");

        // set some advanced properties
        String[] advanced = new String[] {"CommandRetries=2",       // 2 retries
                                          "ConnectionTimeout=300"}; // 5 minute timeout
        lexicom.setProperty(lexicom.HOST, partner, "Advanced", advanced);
      }
    }

    if (createMailbox(newHost)) {
      // Add code here to use the partnerID to get our AS2-From & certificates,
      // the partner's AS2-To & certificates, and other mailbox-level properties
      // from your database.  For now, just setup a loopback test.

      // generate and set a signing/encryption user certificate
      String certAlias = "MYCERT";
      String certPassword = "mytest";

      ICertManagerRunTime certmgr = lexicom.getCertManager();
      CertificateInfo certinfo = new CertificateInfo();
      certinfo.setCommonName("myname");
      certinfo.setCountry("US");
      certinfo.setKeyEncipherment(true);
      certinfo.setDigitalSignature(true);
      certmgr.generateUserCertKey(certAlias, certinfo, certPassword, false);

      lexicom.setProperty(lexicom.MAILBOX, partner, "Overridelistenercerts", "True");
      lexicom.setProperty(lexicom.MAILBOX, partner, "Localsigncertalias", certAlias);
      lexicom.setProperty(lexicom.MAILBOX, partner, "Localsigncertpassword", certPassword);
      lexicom.setProperty(lexicom.MAILBOX, partner, "Usesamecerts", "True"); // The signing certificate and the
                                                                             // encryption certificate are the
                                                                             // same certificate
      // send encrypted/signed message and request a signed receipt
      lexicom.setProperty(lexicom.MAILBOX, partner, "Encryptedrequest", "True");
      lexicom.setProperty(lexicom.MAILBOX, partner, "Encryptionmethod", "4"); // TripleDES
      lexicom.setProperty(lexicom.MAILBOX, partner, "Signedrequest", "True");
      lexicom.setProperty(lexicom.MAILBOX, partner, "Receiptdesired", "True");
      lexicom.setProperty(lexicom.MAILBOX, partner, "Signedreceipt", "True");
      lexicom.setProperty(lexicom.MAILBOX, partner, "DeliveryType", "1"); // async MDN (sync MDN=0)
      lexicom.setProperty(lexicom.MAILBOX, partner, "Asyncdeliverymethod", "2"); // http (https=3)

      // export the user certificate to the certs/ directory for the loopback test.
      File file = new File("certs", "mycert.cer");
      certmgr.exportUserCert(certAlias, lexicom.getAbsolute(file), true, true);

      // set the trading partner encryption/signing certificate
      lexicom.setProperty(lexicom.MAILBOX, partner, "Servercertfile", file.getPath()); // encryption cert
      lexicom.setProperty(lexicom.MAILBOX, partner, "Usesigncert", "True");            // specific signing cert
      lexicom.setProperty(lexicom.MAILBOX, partner, "Useencrcert", "False");           // signing cert not the same
                                                                                       // as the encryption cert
      lexicom.setProperty(lexicom.MAILBOX, partner, "Serversigncertfile", file.getPath()); // signing cert

      // require encrypted/signed messages from partner
      lexicom.setProperty(lexicom.MAILBOX, partner, "Forceencryption", "True");
      lexicom.setProperty(lexicom.MAILBOX, partner, "Forcesignature", "True");

      // set the AS2-To and AS2-From to the same value for the loopback test
      lexicom.setProperty(lexicom.MAILBOX, partner, "Header", new String[] {"PUT AS2-From=looptest",
                                                                            "PUT AS2-To=looptest"});

      // set the WWW authentication username/password, if necessary
      // lexicom.setProperty(lexicom.MAILBOX, partner, "Authtype", "1"); // basic authentication (2=digest)
      // lexicom.setProperty(lexicom.MAILBOX, partner, "Authusername", "testuser");
      // lexicom.setProperty(lexicom.MAILBOX, partner, "Authpassword", "testpassword");
    }

    lexicom.save(partner[0]);
  }

/*------------------------------------------------------------------------------
 * Create the host if it doesn't exist
 *----------------------------------------------------------------------------*/
  private boolean createHost(String preconfiguredHost)
    throws Exception
  {
    boolean newHost = true;
    String[] hosts = lexicom.list(lexicom.HOST, null);
    for (int i = 0; i < hosts.length; i++) {
      if (hosts[i].equalsIgnoreCase(partner[0])) {
        newHost = false;
        break;
      }
    }

    if (newHost)
      lexicom.activateHost(preconfiguredHost, partner[0], true);

    return newHost;
  }

/*------------------------------------------------------------------------------
 * Create the mailbox if it doesn't exist
 *----------------------------------------------------------------------------*/
  private boolean createMailbox(boolean newHost)
    throws Exception
  {
    boolean newMailbox = true;
    String[] mailboxes = lexicom.list(lexicom.MAILBOX, partner);
    if (!newHost) {
      for (int i = 0; i < mailboxes.length; i++) {
        if (mailboxes[i].equalsIgnoreCase(partner[1])) {
          newMailbox = false;
          break;
        }
      }
    }

    if (newMailbox) {
      // if it's a new host, then just rename the default mailbox
      if (newHost)
        lexicom.setProperty(ILexiCom.MAILBOX, new String[] {partner[0], mailboxes[0]},
                            "alias", partner[1]);
      // otherwise clone one
      else
        lexicom.clone(ILexiCom.MAILBOX, new String[] {partner[0], mailboxes[0]}, partner[1], false);
    }

    return newMailbox;
  }

  public static void main(String[] args)
  {
    try {
/*------------------------------------------------------------------------------
 *    Get a server instance of LexiCom
 *----------------------------------------------------------------------------*/
      ILexiCom lexicom = LexiComFactory.getVersaLex(LexiComFactory.LEXICOM,
                                                    "C:\\Program Files\\LexiCom",
                                                    LexiComFactory.SERVER_ONLY);

/*------------------------------------------------------------------------------
 *    Create a partner communications object, and configure for AS2 trades
 *----------------------------------------------------------------------------*/
      PartnerConfig partnerConfig = new PartnerConfig(lexicom, "companyA\\12345678");
      partnerConfig.configureAS2("partnerX", "Generic AS2");

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
