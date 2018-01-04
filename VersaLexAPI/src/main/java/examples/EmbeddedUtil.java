package examples;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.cleo.lexicom.*;
import com.cleo.lexicom.external.*;
import com.cleo.util.logger.*;

/*******************************************************************************
 * Utility class used by other EmbeddedXXX classes
 ******************************************************************************/
public class EmbeddedUtil
{
  ILexiCom iLexiCom;

  protected EmbeddedUtil()
    throws Exception
  {
    // get the ILexiCom API object
    iLexiCom = LexiComFactory.getCurrentInstance();
  }

/*------------------------------------------------------------------------------
 * Miscellaneous functions
 *----------------------------------------------------------------------------*/
  // get the product name
  protected String getProduct()
    throws Exception
  {
    int product = iLexiCom.getLicense().getProduct();
    if (product == ILicense.HARMONY)
      return "Harmony";
    else if (product == ILicense.VLTRADER)
      return "VLTrader";
    else
      return "LexiCom";
  }

  // get the registered serial number
  protected String getSerialNumber()
    throws Exception
  {
    return iLexiCom.getLicense().getSerialNumber();
  }

  // get the mailbox ID
  protected String getID(String[] mailbox)
    throws Exception
  {
    String[] id = iLexiCom.getID(ILexiCom.MAILBOX, mailbox);
    if (id == null || id.length == 0)
      return null;
    return id[0];
  }

  // clear the mailbox ID
  protected void clearID(String id)
    throws Exception
  {
    iLexiCom.clearID(ILexiCom.MAILBOX, id);
  }

  // add a mailbox ID
  protected void addID(String[] mailbox, String id)
    throws Exception
  {
    iLexiCom.addID(ILexiCom.MAILBOX, mailbox, id);
  }

  // get the mailbox corresponding to the specified ID
  protected String[] getMailbox(String id)
    throws Exception
  {
    return iLexiCom.getPath(ILexiCom.MAILBOX, id);
  }

  // get a mailbox controller
  protected IMailboxController getMailboxController(String[] mailbox)
    throws Exception
  {
    return iLexiCom.getMailboxController(mailbox);
  }

  // get the mailbox protocol
  protected String getProtocol(String[] mailbox)
    throws Exception
  {
    int packaging = iLexiCom.getMailboxPackaging(mailbox);
    int protocol = iLexiCom.getHostProtocol(mailbox[0]);
    if (packaging == iLexiCom.AS2)
      return "AS2";
    else if (protocol == iLexiCom.OFTP_CLIENT)
      return "OFTP";
    else
      return "unexpected";
  }

  // get xml node value
  protected String getNodeValue(Element element) {
    String value = null;
    if (element == null)
      return null;
    NodeList nodes = element.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node childNode = nodes.item(i);
      if (childNode instanceof Text) {
        if (value != null)
          value += childNode.getNodeValue();
        else
          value = childNode.getNodeValue();
      }
    }
    return value;
  }

  // log detail message about pipe
  protected void logDetail(boolean outbound, String[] mailbox, Properties parameters)
    throws Exception
  {
    String content = null;
    if (outbound)
      content = "Piping SMG outbound payload through VLTrader " + getProtocol(mailbox) + " mailbox '" + mailbox[0] + "\\" + mailbox[1] + "'...";
    else
      content = "Piping inbound VLTrader " + getProtocol(mailbox) + " mailbox '" + mailbox[0] + "\\" + mailbox[1] + "' payload to SMG...";
    // add any additional desired metadata here from parameters to logged message

    logDetail(content);
  }

  // log detail message to logs/PRODUCT.xml
  protected void logDetail(String content)
  {
    LexiCom.getCurrentInstance().logDetail(this, 0, content, XMLLogElement.BLACK, true, true, true);
  }

  // log exception message to logs/PRODUCT.xml
  protected void logException(Exception ex)
  {
    LexiCom.getCurrentInstance().logDetail(this, ex);
  }

  // print debug to logs/PRODUCT.dbg
  protected void debug(String line) {
    try {
      BufferedWriter writer = LexUtil.getSystemDebugWriter();
      writer.write(new Date().toString() + " " +
                   this.getClass().getName() + " " +
                   line);
      writer.newLine();
      writer.flush();
    } catch (Exception ex) {
    }
  }
}
