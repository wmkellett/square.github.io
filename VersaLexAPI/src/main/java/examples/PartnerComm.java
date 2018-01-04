package examples;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.cleo.lexicom.*;
import com.cleo.lexicom.beans.*;
import com.cleo.lexicom.external.*;

/*******************************************************************************
 * Sample partner communications class for interfacing to Cleo VersaLex
 * communications engine.  This example shows how to use a VersaLex mailbox controller
 * to stream both incoming and outgoing payload.
 ******************************************************************************/
public class PartnerComm implements ILexiComIncoming, ILexiComMultipartIncoming, LexiComLogListener {
  ILexiCom lexicom;
  String partner[];
  String[] path;
  IMailboxController mailbox;
  boolean alive = false;

  public PartnerComm(ILexiCom lexicom)
    throws Exception
  {
    this.lexicom = lexicom;
  }
  public PartnerComm(ILexiCom lexicom, String lexHostMailbox)
    throws Exception
  {
    this.lexicom = lexicom;
    setMailbox(lexHostMailbox);
  }
  public void setMailbox(String lexHostMailbox)
    throws Exception
  {
    StringTokenizer tokens = new StringTokenizer(lexHostMailbox, "\\", false);
    if (tokens.countTokens() != 2)
      throw new LexiComException("Host and mailbox names must be separated by but not contain a \\ character.");

    this.partner = new String[] {tokens.nextToken(), tokens.nextToken()};
    this.mailbox = lexicom.getMailboxController(this.partner);
  }

/*------------------------------------------------------------------------------
 * Send outgoing payload.  The contentType, filename, and length parameters are
 * optional.  Use null, null, and -1 respectively when not known/required.
 * The keepAlive parameters should be set to false if this is the only or last
 * mailbox access for a while.
 *----------------------------------------------------------------------------*/
  public void send(InputStream stream, String contentType, String filename, long length, boolean keepAlive)
    throws Exception
  {
    if (this.mailbox == null)
      throw new LexiComException("Use setMailbox() to identify 'host\\mailbox' pair.");

    // send() and receive() method will take care of creating the temporary action if it's not there,
    // but since we want to watch log events, we'll do it ourselves
    if (!this.alive) {
      this.path = this.mailbox.createTempAction("send");
      this.lexicom.addLogListener(this, path);
      this.alive = true;
    }

    boolean success = this.mailbox.send(new RemoteLexiComOutgoing(new LexiComOutgoing(stream, contentType, filename, length)), keepAlive);

    if (!keepAlive) {
      this.lexicom.removeLogListener(this, this.path);
      this.alive = false;
    }

    if (!success)
      getResult();
  }

/*------------------------------------------------------------------------------
 * Receive incoming payload.  The ILexiComIncoming and ILexiComMultipartIncoming
 * interface methods below will be invoked as each file is received. The keepAlive
 * parameters should be set to false if this is the only or last mailbox access
 * for a while.
 *----------------------------------------------------------------------------*/
  public void receive(boolean keepAlive)
    throws Exception
  {
    if (this.mailbox == null)
      throw new LexiComException("Use setMailbox() to identify 'host\\mailbox' pair.");

    // send() and receive() method will take care of creating the temporary action if it's not there,
    // but since we want to watch log events, we'll do it ourselves
    if (!this.alive) {
      this.path = this.mailbox.createTempAction("receive");
      this.lexicom.addLogListener(this, path);
      this.alive = true;
    }

    boolean success = this.mailbox.receive(new RemoteLexiComIncoming(this), keepAlive);

    if (!keepAlive) {
      this.lexicom.removeLogListener(this, this.path);
      this.alive = false;
    }

    if (!success)
      getResult();
  }

/*------------------------------------------------------------------------------
 * Interrogate the last send/receive result
 *----------------------------------------------------------------------------*/
  private void getResult()
    throws Exception
  {
    Element element = this.mailbox.getLastResult();

    // build the exception message from the result element contents
    String result = element.getAttribute("text"); // either "Error" or "Exception"
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (i == 0)
        result += ": ";
      result += children.item(i).getNodeValue();
    }

    throw new LexiComException(result);
}

/*------------------------------------------------------------------------------
 * If send or receive kept alive, ends the mailbox session
 *----------------------------------------------------------------------------*/
  public void end()
    throws Exception
  {
    if (this.mailbox == null)
      throw new LexiComException("Use setMailbox() to identify 'host\\mailbox' pair.");

    if (this.alive) {
      this.lexicom.removeLogListener(this, this.path);
      this.alive = false;
    }
    this.mailbox.end();
  }

/*------------------------------------------------------------------------------
 * List the existing VersaLex host/mailbox pairs
 *----------------------------------------------------------------------------*/
  public static String[] listHostMailboxes(ILexiCom lexicom)
    throws Exception
  {
    Vector vector = new Vector();

    String[] hosts = lexicom.list(ILexiCom.HOST, null);
    for (int i = 0; i < hosts.length; i++) {
      String[] mailboxes = lexicom.list(ILexiCom.MAILBOX, new String[] {hosts[i]});
      for (int j = 0; j < mailboxes.length; j++)
        vector.add(hosts[i] + LexUtil.beanSeparator + mailboxes[j]);
    }
    String[] hostMailboxes = new String[vector.size()];
    for (int i = 0; i < hostMailboxes.length; i++)
      hostMailboxes[i] = (String)vector.get(i);
    return hostMailboxes;
  }

/*******************************************************************************
 * ILexiComIncoming interface
 ******************************************************************************/
/*------------------------------------------------------------------------------
 * Incoming payload
 *----------------------------------------------------------------------------*/
  public OutputStream open(String[] source,
                           String[] mailbox,
                           File file,
                           String messageID,
                           Properties parameters)
    throws Exception
  {
    System.out.println("ILexiComIncoming.open() source=>" + LexUtil.convert(ILexiCom.SERVICE, source) + "<\n" +
                       "                        mailbox=>" + LexUtil.convert(ILexiCom.MAILBOX, mailbox) + "<\n" +
                       "                        default file=>" + file.getPath() + "<\n" +
                       "                        messageID=>" + messageID + "<\n" +
                       "                        parameters=>>>");
    if (parameters != null)
      parameters.list(System.out);
    System.out.println("<<<");

    // return null to use the default file

    // for testing just open a stream to the default file
    // first make sure the parent directory exists in case it's multipart payload
    //   that will be put in a new subdirectory
    if (file.getParentFile() != null)
      file.getParentFile().mkdirs();
    return new FileOutputStream(file);
  }

/*------------------------------------------------------------------------------
 * Incoming multipart payload
 *----------------------------------------------------------------------------*/
  public ILexiComMultipartIncoming multipart(String[] source,
                                             String[] mailbox,
                                             String messageID,
                                             int numParts)
    throws Exception
  {
    System.out.println("ILexiComMultipartIncoming.multipart() source=>" + LexUtil.convert(ILexiCom.SERVICE, source) + "<\n" +
                       "                                      mailbox=>" + LexUtil.convert(ILexiCom.MAILBOX, mailbox) + "<\n" +
                       "                                      messageID=>" + messageID + "<\n" +
                       "                                      numparts=>" + numParts + "<");
    return new RemoteLexiComMultipartIncoming(this);
  }

/*------------------------------------------------------------------------------
 * Receipt generation (e.g. AS2 MDN)
 *----------------------------------------------------------------------------*/
  public void receipt(String[] source, String[] mailbox, String messageID)
    throws Exception
  {
    System.out.println("ILexiComIncoming.receipt() source=>" + LexUtil.convert(ILexiCom.SERVICE, source) + "<\n" +
                       "                           mailbox=>" + LexUtil.convert(ILexiCom.MAILBOX, mailbox) + "<\n" +
                       "                           messageID=>" + messageID + "<");
    // throw exception if payload not acceptable
//    throw new Exception("I just don't like this payload");
  }

/*******************************************************************************
 * ILexiComMultipartIncoming interface
 ******************************************************************************/
/*------------------------------------------------------------------------------
 * One part of incoming multipart payload
 *----------------------------------------------------------------------------*/
  public OutputStream open(String[] source,
                           String[] mailbox,
                           int index,
                           File file,
                           String messageID,
                           Properties parameters)
    throws Exception
  {
    System.out.println("ILexiComMultipartIncoming.open() index=>" + index + "<");

    // for testing just use open() method above in ILexiComIncoming interface for multipart too
    return open(source, mailbox, file, messageID, parameters);
  }

/*------------------------------------------------------------------------------
 * Multipart payload complete
 *----------------------------------------------------------------------------*/
  public void close(String messageID)
    throws Exception
  {
    System.out.println("ILexiComMultipartIncoming.close() messageID=>" + messageID + "<");
  }

/*******************************************************************************
 * LexiComLogListener interface
 ******************************************************************************/
  public void log(LexiComLogEvent e) {
    System.out.println(e.getMessage());
  }

/*******************************************************************************
 * Test
 ******************************************************************************/
  public static void main(String[] args)
  {
    try {
      String LexiComHome = "C:\\Program Files\\LexiCom";
/*------------------------------------------------------------------------------
 *    Get a client instance of LexiCom (LexiCom must already be running as a
 *    Windows service or Unix daemon)
 *----------------------------------------------------------------------------*/
      ILexiCom lexicom = LexiComFactory.getVersaLex(LexiComFactory.LEXICOM,
                                                    LexiComHome,
                                                    LexiComFactory.CLIENT_ONLY);

      String as2looptest = "Looptest AS2\\myMailbox";
      boolean foundAS2looptest = false;
/*------------------------------------------------------------------------------
 *    Get and print the list of host\mailboxes
 *----------------------------------------------------------------------------*/
      String[] hostMailboxes = listHostMailboxes(lexicom);
      for (int i = 0; i < hostMailboxes.length; i++) {
        System.out.println("Host\\mailbox=>" + hostMailboxes[i] + "<");

        if (hostMailboxes[i].equals(as2looptest))
          foundAS2looptest = true;
      }

/*------------------------------------------------------------------------------
 *    If there's an AS2 looptest, go ahead and test sending and receiving
 *----------------------------------------------------------------------------*/
      if (foundAS2looptest) {
        System.out.println("Testing '" + as2looptest + "' host\\mailbox...");

/*------------------------------------------------------------------------------
 *      Looptest AS2\myMailbox should be setup to send files to itself (same
 *      AS2-To and AS2-From values) and the receive() below will collect files
 *      that have been placed in the Looptest AS2\myMailbox inbox.
 *
 *      If you would prefer to stream unsolicited incoming files in real-time
 *      as they are received by the listener (applies to AS2, ebMS, and, in
 *      VLTrader, FTP and AS3), uncomment the following line and comment out the
 *      receive()
 *----------------------------------------------------------------------------*/
//        lexicom.getListenerController().setDestination(new RemoteLexiComIncoming(new PartnerComm(lexicom)));

        PartnerComm partnerComm = new PartnerComm(lexicom, as2looptest);

        try {
/*------------------------------------------------------------------------------
 *        Stream out a test file
 *----------------------------------------------------------------------------*/
          File file = new File(LexiComHome, "outbox" + File.separator +
                                            "test" + File.separator +
                                            "test.edi");
          partnerComm.send(new FileInputStream(file), "application/edi-x12", file.getName(), file.length(), false);

/*------------------------------------------------------------------------------
 *        Stream back in same test file.
 *----------------------------------------------------------------------------*/
          partnerComm.receive(false);

        } catch (Exception ex) {
          ex.printStackTrace();
        }

      } else
        System.out.println("Create a '" + as2looptest + "' host\\mailbox for testing.");

/*------------------------------------------------------------------------------
 *    Close down the LexiCom instance
 *----------------------------------------------------------------------------*/
      lexicom.getListenerController().setDestination(null);
      lexicom.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.exit(0);
  }
}
