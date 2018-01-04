package examples;

import com.cleo.lexicom.external.*;

/*******************************************************************************
 * This example demonstrates:
 * - How to register a new VersaLex installation via the API
 ******************************************************************************/
public class RegisterProduct implements LexiComLogListener {
  public static void main(String[] args)
  {
    new RegisterProduct();
  }
  public RegisterProduct()
  {
    try {
      String lexicomHome =  "C:\\Program Files\\LexiCom";

      ILexiCom lexicom = null;
/*------------------------------------------------------------------------------
 *    Try to get a client instance of LexiCom, but it may not be licensed yet
 *----------------------------------------------------------------------------*/
      try {
        lexicom = LexiComFactory.getVersaLex(LexiComFactory.LEXICOM,
                                             lexicomHome,
                                             LexiComFactory.CLIENT_ONLY);
      } catch (Exception ex) {
        ex.printStackTrace();
      }

/*------------------------------------------------------------------------------
 *    Get a LexiCom licenser instance, either from the LexiCom instance or
 *    separate, and listen for license log events
 *----------------------------------------------------------------------------*/
      ILicenser licenser = null;
      if (lexicom != null)
        licenser = lexicom.getLicenser();
      else
        licenser = LexiComFactory.getLicenser(LexiComFactory.LEXICOM,
                                              lexicomHome);
      licenser.addLogListener(this); // listen for licensing events

/*------------------------------------------------------------------------------
 *    Query the license server
 *----------------------------------------------------------------------------*/
      RegistrationInfo registrationInfo = licenser.registrationQuery("AA1234-ZZ5678");

      System.out.println("Current registration information:");
      System.out.println(" firstName=>" + registrationInfo.getFirstName() + "<");
      System.out.println(" lastName=>" + registrationInfo.getLastName() + "<");
      System.out.println(" phone=>" + registrationInfo.getPhone() + "<");
      System.out.println(" extension=>" + registrationInfo.getExtension() + "<");
      System.out.println(" email=>" + registrationInfo.getEmail() + "<");
      System.out.println(" company=>" + registrationInfo.getCompany() + "<");
      System.out.println(" address1=>" + registrationInfo.getAddress1() + "<");
      System.out.println(" address2=>" + registrationInfo.getAddress2() + "<");
      System.out.println(" city=>" + registrationInfo.getCity() + "<");
      System.out.println(" state=>" + registrationInfo.getState() + "<");
      System.out.println(" zip=>" + registrationInfo.getZip() + "<");
      System.out.println(" country=>" + registrationInfo.getCountry() + "<");

/*------------------------------------------------------------------------------
 *    Update the contact information in the registration
 *----------------------------------------------------------------------------*/
      registrationInfo.setFirstName("myfirstname");
      registrationInfo.setLastName("mylastname");
      registrationInfo.setPhone("###-###-####");
      registrationInfo.setExtension("");
      registrationInfo.setEmail("me@mycompany.com");

/*------------------------------------------------------------------------------
 *    Update the registration info (and apply a temporary license)
 *
 *    Note: If a permanent license has been previously requested and applied for
 *          this serial number either through the GUI or the API, a temporary
 *          license is never re-issued, so a permanent license would be applied
 *          here instead
 *----------------------------------------------------------------------------*/
      licenser.register(registrationInfo);

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
}
