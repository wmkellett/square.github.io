package examples;

import java.io.*;
import java.util.*;

import com.cleo.lexicom.external.*;

public class TestAPI {
  int iProduct;
  ILexiCom iLexicom;
  public static void main(String[] args)
  {
    new TestAPI();
  }
  public TestAPI()
  {
    try {
      iProduct = LexiComFactory.LEXICOM;
      if (new File("lib", "VLTrader.jar").exists())
        iProduct = LexiComFactory.VLTRADER;

      iLexicom = getVersaLex();

      debug("API connection established!");
      debug("Testing connection every 15 seconds...");

      while (true) {
        Thread.sleep(15000L);
        try {
          iLexicom.noop();
        } catch (Exception ex) {
          debug("Trying to re-establish API connection...");
          getVersaLex();
          debug("API connection re-established!");
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(0);
  }
  private ILexiCom getVersaLex()
    throws Exception
  {
    return LexiComFactory.getVersaLex(iProduct,
                                      System.getProperty("user.dir"),
                                      LexiComFactory.CLIENT_ONLY);
  }
  private void debug(String line) {
    System.out.println(new Date() + " " + line);
  }
}
