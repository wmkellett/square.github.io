package examples;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.cleo.lexicom.*;
import com.cleo.lexicom.external.*;

/*******************************************************************************
 * Either route SMG outbound request thru VLT mailbox or route inbound VLT
 * request to SMG
 ******************************************************************************/
public class EmbeddedSMGILexiComIncoming implements ILexiComIncoming, ILexiComMultipartIncoming
{
  final static public String SMG_OUTBOUND_ID = "_SMG_OUTBOUND_";
  final static public String SMG_INBOUND_ID  = "_SMG_INBOUND_";
  EmbeddedUtil util;

  public EmbeddedSMGILexiComIncoming()
    throws Exception
  {
    util = new EmbeddedUtil();
    // SMG inbound/outbound mailbox IDs set here, but this could be done via client API application
    util.clearID(SMG_OUTBOUND_ID);
    util.addID(new String[] {"Local SMG HTTP Users", "SMGOutbound"}, SMG_OUTBOUND_ID);
    util.clearID(SMG_INBOUND_ID);
    util.addID(new String[] {"SMG Inbound HTTP", "myMailbox"}, SMG_INBOUND_ID);
  }

/************************* ILexiComIncoming interface *************************/
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
    util.debug("ILexiComIncoming.open() source=>" + LexUtil.convert(ILexiCom.SERVICE, source) + "<\n" +
               "                        mailbox=>" + LexUtil.convert(ILexiCom.MAILBOX, mailbox) + "<\n" +
               "                        default file=>" + file.getPath() + "<\n" +
               "                        messageID=>" + messageID + "<\n" +
               "                        parameters=>>>");
    if (parameters != null)
      parameters.list(new PrintWriter(LexUtil.getSystemDebugWriter()));
    util.debug("<<<");

    String[] vltmbx = null; // will either be set to outbound tp mailbox or inbound smg mailbox
    final Properties props = new Properties(); // used for inbound parameters

/*------------------------------------------------------------------------------
 *  Get the ID associated with the incoming mailbox
 *----------------------------------------------------------------------------*/
    String id = util.getID(mailbox);

/*------------------------------------------------------------------------------
 *  If incoming mailbox does not have an ID assocation, don't route
 *----------------------------------------------------------------------------*/
    if (id == null || id.length() == 0) {
      return null;

/*------------------------------------------------------------------------------
 *  Else if outbound request from SMG
 *----------------------------------------------------------------------------*/
    } else if (id.equals(SMG_OUTBOUND_ID)) {
      String gxsmbx = parameters.getProperty("HTTP.parameter.GXSmbx");
      if (gxsmbx == null || gxsmbx.length() == 0)
        throw new LexiComNoRetriesException("Outbound SMG request missing GXSmbx parameter.");
      vltmbx = util.getMailbox(gxsmbx);
      if (vltmbx == null || vltmbx.length == 0)
        throw new LexiComNoRetriesException("Outbound mailbox '" + gxsmbx + "' ID not found.");
      util.logDetail(true, vltmbx, parameters); // log that piping outbound along with any desired metadata

/*------------------------------------------------------------------------------
 *  Else route inbound request to SMG
 *----------------------------------------------------------------------------*/
    } else {
      vltmbx = util.getMailbox(SMG_INBOUND_ID);
      if (vltmbx == null || vltmbx.length == 0)
        throw new LexiComNoRetriesException("Inbound SMG mailbox ID not configured.");
      props.setProperty("GXSmbx", id);
      props.setProperty("protocol", util.getProtocol(mailbox));
      util.logDetail(false, mailbox, parameters); // log that piping inbound along with any desired metadata
    }

/*------------------------------------------------------------------------------
 *  Route payload stream out through VLT mailbox controller
 *----------------------------------------------------------------------------*/
    PipedInputStream in = new PipedInputStream();
    LexiComOutgoing outgoing = new LexiComOutgoing(in); // use piped input stream for outgoing transfer
    outgoing.setFilename(file.getName());               // and retain filename
    IMailboxController controller = util.getMailboxController(vltmbx);
    RoutedInputThread thread = new RoutedInputThread(controller, outgoing, props);
    RoutedOutputStream out = new RoutedOutputStream(new PipedOutputStream(in), thread);
    thread.start();
    return out; // return filtered, piped output stream for incoming transfer to use
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
    util.debug("ILexiComMultipartIncoming.multipart() source=>" + LexUtil.convert(ILexiCom.SERVICE, source) + "<\n" +
               "                                      mailbox=>" + LexUtil.convert(ILexiCom.MAILBOX, mailbox) + "<\n" +
               "                                      messageID=>" + messageID + "<\n" +
               "                                      numparts=>" + numParts + "<");
    //
    return this;
  }

/*------------------------------------------------------------------------------
 * Receipt generation (e.g. AS2 MDN)
 *----------------------------------------------------------------------------*/
  public void receipt(String[] source, String[] mailbox, String messageID)
    throws Exception
  {
    util.debug("ILexiComIncoming.receipt() source=>" + LexUtil.convert(ILexiCom.SERVICE, source) + "<\n" +
               "                           mailbox=>" + LexUtil.convert(ILexiCom.MAILBOX, mailbox) + "<\n" +
               "                           messageID=>" + messageID + "<");
    // throw exception if payload not acceptable
//    throw new Exception("I just don't like this payload");
  }

/*******************************************************************************
 * Nested class for filtered, piped output stream
 ******************************************************************************/
  public class RoutedOutputStream extends FilterOutputStream {
    RoutedInputThread thread;
    public RoutedOutputStream(OutputStream out, RoutedInputThread thread) {
      super(out);
      this.thread = thread;
    }
    public void write(int b)
      throws IOException
    {
      super.write(b);
      checkInputThread();
    }
    public void write(byte[] b)
      throws IOException
    {
      super.write(b);
      checkInputThread();
    }
    public void write(byte[] b, int off, int len)
      throws IOException
    {
      super.write(b, off, len);
      checkInputThread();
    }
    public void flush()
      throws IOException
    {
      super.flush();
      checkInputThread();
    }
    public void close()
      throws IOException
    {
      super.close();
      do {
        try {
          thread.join(1000L);
        } catch (Exception ex) {
        }
        checkInputThread();
      } while (thread.isAlive());
    }
/*------------------------------------------------------------------------------
 *  Check if input side of pipe has failed and if so, throw exception on output side
 *----------------------------------------------------------------------------*/
    private void checkInputThread()
      throws IOException
    {
      if (thread.ex != null)
        throw new LexiComNoRetriesException("Nested exception: " + thread.ex.toString(), thread.ex);
    }
  }

/*******************************************************************************
 * Nested class for piped input stream
 ******************************************************************************/
  public class RoutedInputThread extends Thread {
    IMailboxController controller;
    LexiComOutgoing outgoing;
    Properties props;
    Exception ex = null;
    RoutedInputThread(IMailboxController controller, LexiComOutgoing outgoing, Properties props) {
      this.controller = controller;
      this.outgoing = outgoing;
      this.props = props;
    }
    public void run() {
      try {
        // stream incoming payload out using mailbox's default send action
        if (!controller.send(outgoing, props, false)) {
          Element result = controller.getLastResult();
          this.ex = new LexiComException(result.getAttribute("text") + " - " + util.getNodeValue(result));
        }
      } catch (Exception ex) {
        this.ex = ex;
        util.logException(ex);
      } finally {
        try {
          outgoing.getStream().close();
        } catch (Exception ex2) {
        }
      }
    }
  }

/******************** ILexiComMultipartIncoming interface *********************/
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
    util.debug("ILexiComMultipartIncoming.open() index=>" + index + "<");
    // just use open() method above in ILexiComIncoming interface for multipart too
    return open(source, mailbox, file, messageID, parameters);
  }

/*------------------------------------------------------------------------------
 * Multipart payload complete
 *----------------------------------------------------------------------------*/
  public void close(String messageID)
    throws Exception
  {
    util.debug("ILexiComMultipartIncoming.close() messageID=>" + messageID + "<");
  }
}