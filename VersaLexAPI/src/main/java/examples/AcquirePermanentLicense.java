package examples;

import com.cleo.lexicom.external.*;

/*******************************************************************************
 * This example demonstrates:
 * - How to request and apply a permanent VersaLex license via the API
 ******************************************************************************/
public class AcquirePermanentLicense implements LexiComLogListener {
  public static void main(String[] args)
  {
    new AcquirePermanentLicense();
  }
  public AcquirePermanentLicense()
  {
    try {
/*------------------------------------------------------------------------------
 *    Get a client instance of LexiCom
 *----------------------------------------------------------------------------*/
      ILexiCom lexicom = LexiComFactory.getVersaLex(LexiComFactory.LEXICOM,
                                                    "C:\\Program Files\\LexiCom",
                                                    LexiComFactory.CLIENT_ONLY);

      System.out.println("Current license:");
      print(lexicom.getLicense());

/*------------------------------------------------------------------------------
 *    Get a LexiCom licenser instance and listen for license log events
 *----------------------------------------------------------------------------*/
      ILicenser licenser = lexicom.getLicenser();
      licenser.addLogListener(this); // listen for licensing events

/*------------------------------------------------------------------------------
 *    Fill in the optional system information and query the license server
 *----------------------------------------------------------------------------*/
      SystemInfo systemInfo = new SystemInfo();
      systemInfo.setFirewallProxy("Microsoft Proxy Server");
      systemInfo.setFirewallProxyVersion("2.0");
      systemInfo.setTranslator("Inovis TrustedLink for Windows");
      systemInfo.setTranslatorVersion("5.4");

      ILicense license = licenser.licenseQuery(systemInfo);

/*------------------------------------------------------------------------------
 *    Check if permanent license values as expected
 *----------------------------------------------------------------------------*/
      System.out.println("Available permanent license:");
      if (license == null) {
        System.out.println(" License is up-to-date!");

      } else {
        print(license);

/*------------------------------------------------------------------------------
 *      Apply the permanent license
 *----------------------------------------------------------------------------*/
        licenser.license(license);
      }

/*------------------------------------------------------------------------------
 *    Remove the licenser log listener and close down the LexiCom instance
 *----------------------------------------------------------------------------*/
      licenser.removeLogListener(this);
      if (lexicom != null)
        lexicom.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(0);
  }

/*------------------------------------------------------------------------------
 * Print license events
 *----------------------------------------------------------------------------*/
  public void log(LexiComLogEvent e) {
    System.out.println(e.getMessage());
  }

  public void print(ILicense license) {
    System.out.println(" Expires              : " + ((license.getKeyExpiration() == null) ?
                                                     "-" :
                                                     license.getKeyExpiration().toString()));

    System.out.print(" Hosts                : ");
    int[] hosts = license.getAllowedHosts();
    if (hosts.length == 0)
      System.out.println("Any");
    else {
      for (int i = 0; i < hosts.length; i++) {
        if (i > 0)
          System.out.print(",");
        int host = hosts[i];
        switch (host) {
          case ILicense.ASDA:
            System.out.print("ASDA");
            break;
          case ILicense.EDS_ELIT:
            System.out.print("EDS_ELIT");
            break;
          case ILicense.GXS:
            System.out.print("GXS");
            break;
          case ILicense.IBM_IE:
            System.out.print("IBM_IE");
            break;
          case ILicense.KOHLS:
            System.out.print("KOHLS");
            break;
          case ILicense.MICHAELS:
            System.out.print("MICHAELS");
            break;
          case ILicense.WAL_MART:
            System.out.print("WAL_MART");
            break;
        }
      }
      System.out.println("");
    }

    System.out.println(" # Hosts              : " + ((license.getActiveHostLimit() <= 0) ?
                                                     "Unlimited" :
                                                     String.valueOf(license.getActiveHostLimit())));
    String perHost = "";
    System.out.println(" # Mailboxes          : " + ((license.getActiveMailboxLimit() <= 0) ?
                                                     "Unlimited" :
                                                     String.valueOf(license.getActiveMailboxLimit())) +
                                                    ((license.isMailboxLimitPerHost()) ?
                                                     " per host" :
                                                     ""));

    System.out.println(" Integration          : " + license.isTranslatorLicensed());
    System.out.println(" VLProxy              : " + license.isVLProxyLicensed());
    System.out.println(" Web Browser Interface: " + license.isWebBrowserLicensed());
    System.out.println(" API                  : " + license.isApiLicensed());

    System.out.println(" Support              : " + ((license.getSupportExpiration() == null) ?
                                                     license.getKeyExpiration().toString() :
                                                     license.getSupportExpiration().toString()));
  }
}
