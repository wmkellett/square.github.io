package examples;

import com.cleo.lexicom.external.*;

/*******************************************************************************
 * This example demonstrates:
 * - How to unregister a VersaLex license
 ******************************************************************************/
public class UnregisterProduct implements LexiComLogListener {
  public static void main(String[] args)
  {
    new UnregisterProduct();
  }
  public UnregisterProduct()
  {
    try {
/*------------------------------------------------------------------------------
 *    Get a client instance of LexiCom
 *----------------------------------------------------------------------------*/
      ILexiCom lexicom = LexiComFactory.getVersaLex(LexiComFactory.LEXICOM,
                                                    "C:\\Program Files\\LexiCom",
                                                    LexiComFactory.CLIENT_ONLY);

/*------------------------------------------------------------------------------
 *    Get a LexiCom licenser instance and listen for license log events
 *----------------------------------------------------------------------------*/
      ILicenser licenser = lexicom.getLicenser();
      licenser.addLogListener(this); // listen for licensing events

/*------------------------------------------------------------------------------
 *    Unregister product, after which serial number can be registered on another
 *    installation
 *----------------------------------------------------------------------------*/
      licenser.unregister();

/*------------------------------------------------------------------------------
 *    Do not need to remove the license log listener or close down the LexiCom
 *    instance, as unregister() shuts it down
 *----------------------------------------------------------------------------*/

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
