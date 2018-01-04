package examples;

import java.io.*;

import com.cleo.lexicom.external.*;
import com.cleo.lexicom.certmgr.external.*;

/*******************************************************************************
 * Demonstrates how to configure a new OFTP trading partner in VLTrader
 ******************************************************************************/
public class ConfigureOFTPHostMailbox {
  public static void main(String[] args)
  {
    new ConfigureOFTPHostMailbox();
  }
  public ConfigureOFTPHostMailbox()
  {
    try {
/*------------------------------------------------------------------------------
 *    Get a client instance of VLTrader
 *----------------------------------------------------------------------------*/
      ILexiCom lexicom = LexiComFactory.getVersaLex(LexiComFactory.VLTRADER,
//                                                    "C:\\Program Files\\VLTrader",
                                                    "D:\\Program Files\\VLTraderGXS",
                                                    LexiComFactory.CLIENT_ONLY);

/*------------------------------------------------------------------------------
 *    Create and configure a new OFTP trading partner (i.e. host/mailbox).  Just
 *    configure it to send to ourselves (i.e. looptest).
 *----------------------------------------------------------------------------*/
      String[] partner = {"Looptest OFTP", "myMailbox"};
      lexicom.activateHost("Generic OFTP", partner[0], true); // overlay if already exists

/*------------------------------------------------------------------------------
 *    Following properties are pertinent to both OFTP1 and OFTP2
 *----------------------------------------------------------------------------*/
      lexicom.setProperty(lexicom.HOST, partner, "Tcpip", "True");        // tcp/ip connection
      lexicom.setProperty(lexicom.HOST, partner, "ConnectType", "0");     // system default connection type
      lexicom.setProperty(lexicom.HOST, partner, "Address", "localhost"); // tp's OFTP addresss
      lexicom.setProperty(lexicom.HOST, partner, "Port", "3305");         // tp's OFTP port
      lexicom.setProperty(lexicom.HOST, partner, "BufferSize", "2048");   // preferred OFTP buffer size (negotiated)
      lexicom.setProperty(lexicom.HOST, partner, "BufferCredits", "7");   // preferred OFTP buffer credits (negotiated)

      lexicom.setProperty(lexicom.MAILBOX, partner, "PartnerUserId", "LOOPTEST");   // tp's OFTP user id
      lexicom.setProperty(lexicom.MAILBOX, partner, "PartnerPassword", "LOOPTEST"); // tp's OFTP password

      lexicom.setProperty(lexicom.MAILBOX, partner, "OverrideId", "True");   // override my id/password because looptest
      lexicom.setProperty(lexicom.MAILBOX, partner, "UserId", "LOOPTEST");   // my OFTP user id
      lexicom.setProperty(lexicom.MAILBOX, partner, "Password", "LOOPTEST"); // my OFTP password

/*------------------------------------------------------------------------------
 *    All remaining properties are pertinent only to OFTP2
 *----------------------------------------------------------------------------*/
      lexicom.setProperty(lexicom.MAILBOX, partner, "Encryption", "True"); // encrypt outbound
      lexicom.setProperty(lexicom.MAILBOX, partner, "Signed", "True");     // sign outbound
      lexicom.setProperty(lexicom.MAILBOX, partner, "SignEerp", "True");   // sign outbound eerps (receipts)
      lexicom.setProperty(lexicom.MAILBOX, partner, "ForceEncryption", "True"); // only accept encrypted inbound
      lexicom.setProperty(lexicom.MAILBOX, partner, "ForceSignature", "True");  // only accept signed inbound
      
/*------------------------------------------------------------------------------
 *    Generate a self-signed user certificate (with the minimum settings)
 *    if not already present
 *----------------------------------------------------------------------------*/
      String certAlias = "OFTP_TEST_CERT";
      String certPassword = "myoftptest";
      File file = new File("certs", "myoftptest.cer");

      ICertManagerRunTime certmgr = lexicom.getCertManager();
      if (certmgr.getCertificateChain(certAlias) == null) {
        CertificateInfo certinfo = new CertificateInfo();
        certinfo.setCommonName("myoftptest");
        certinfo.setCountry("US");
        certinfo.setKeyEncipherment(true);
        certinfo.setDigitalSignature(true);
        certmgr.generateUserCertKey(certAlias, certinfo, certPassword, false);

/*------------------------------------------------------------------------------
 *      Export the user certificate to the certs/ directory.  Normally we wouldn't
 *      necessarily export it to our own certs directory - we would just export
 *      it to a location in which we could then send (email) it to your trading
 *      partners.  But for our looptest example, we need it in the /certs directory.
 *----------------------------------------------------------------------------*/
        certmgr.exportUserCert(certAlias, lexicom.getAbsolute(file), true, true);
      }

/*------------------------------------------------------------------------------
 *    OFTP2 can have four different protocol certificates*
 *      - session authentication
 *      - payload signature
 *      - payload encryption
 *      - receipt (eerp) signature
 *    * not counting SSL server and client certificates, which we don't cover here
 *----------------------------------------------------------------------------*/
      // tp payload signing/encryption
      lexicom.setProperty(lexicom.MAILBOX, partner, "PartnerSignCertFile", file.getPath()); // tp's payload signing cert
      lexicom.setProperty(lexicom.MAILBOX, partner, "PartnerEncrCertFile", file.getPath()); // tp's payload encryption cert
//      lexicom.setProperty(lexicom.MAILBOX, partner, "UsePartnerSameCerts", "True");       // or could set that encrypt same as sign

      // my payload signing/encryption
      lexicom.setProperty(lexicom.MAILBOX, partner, "OverrideListenerCerts", "True");       // override my sign/encrypt certs because looptest
      lexicom.setProperty(lexicom.MAILBOX, partner, "LocalSignCertAlias", certAlias);       // my payload signing cert
      lexicom.setProperty(lexicom.MAILBOX, partner, "LocalSignCertPassword", certPassword); // my payload signing cert password
      lexicom.setProperty(lexicom.MAILBOX, partner, "LocalEncrCertAlias", certAlias);       // my payload encrypt cert
      lexicom.setProperty(lexicom.MAILBOX, partner, "LocalEncrCertPassword", certPassword); // my payload encrypt cert password
//      lexicom.setProperty(lexicom.MAILBOX, partner, "UseLocalSameCerts", "True");         // or could set that encrypt same as sign

      // tp session authentication
      lexicom.setProperty(lexicom.MAILBOX, partner, "AuthPartnerCertFile", file.getPath());              // tp's authentication cert
//      lexicom.setProperty(lexicom.MAILBOX, partner, "AuthenticationPartnerUseEncryptionCert", "True"); // or could set that auth same as payload encrypt

      // my session authentication
      lexicom.setProperty(lexicom.MAILBOX, partner, "AuthenticationCertAlias", certAlias);          // my authentication cert
      lexicom.setProperty(lexicom.MAILBOX, partner, "AuthenticationCertPassword", certPassword);    // my authentication cert password
//      lexicom.setProperty(lexicom.MAILBOX, partner, "AuthenticationMyUseEncryptionCert", "True"); // or could set that auth same as payload encrypt

      // tp eerp (receipt) signing
      lexicom.setProperty(lexicom.MAILBOX, partner, "EerpPartnerSignFile", file.getPath()); // tp's eerp (receipt) signing cert
//      lexicom.setProperty(lexicom.MAILBOX, partner, "EerpPartnerUseSigningCert", "True"); // or could set that eerp same as payload sign

      // my eerp (receipt) signing
      lexicom.setProperty(lexicom.MAILBOX, partner, "EerpCertAlias", certAlias);       // my eerp (receipt) signing cert
      lexicom.setProperty(lexicom.MAILBOX, partner, "EerpCertPassword", certPassword); // my eerp (receipt) signing cert password
//      lexicom.setProperty(lexicom.MAILBOX, partner, "EerpMyUseSigningCert", "True"); // or could set that eerp same as payload sign

/*------------------------------------------------------------------------------
 *    Save all the settings and set the test mailbox ID
 *----------------------------------------------------------------------------*/
      lexicom.save(partner[0]);
      lexicom.addID(lexicom.MAILBOX, partner, "OFTPclient1");

/*------------------------------------------------------------------------------
 *    Close down the VLTrader instance
 *----------------------------------------------------------------------------*/
      lexicom.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(0);
  }
}
