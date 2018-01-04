package examples;

import java.io.*;
import java.util.*;

import com.cleo.lexicom.*;
import com.cleo.lexicom.external.*;

/*******************************************************************************
 * Sample implementation of ILexiComIncoming that can be embedded directly
 * inside VersaLex engine.  See javadoc for ILexiComIncoming for more
 * information.
 ******************************************************************************/
public class EmbeddedILexiComIncoming implements ILexiComIncoming, ILexiComMultipartIncoming
{
  EmbeddedUtil util;

  public EmbeddedILexiComIncoming()
    throws Exception
  {
    util = new EmbeddedUtil();
  }

/*------------------------------------------------------------------------------
 * ILexiComIncoming interface
 *----------------------------------------------------------------------------*/
  // incoming payload
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

    // return null to use the default file

    // for testing just open a stream to the default file
    // first make sure the parent directory exists in case it's multipart payload
    //   that will be put in a new subdirectory
    if (file.getParentFile() != null)
      file.getParentFile().mkdirs();
    return new EmbeddedFilterOutputStream(new FileOutputStream(file));
  }

  // incoming multipart payload
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
    return this;
  }

  // receipt generation (e.g. AS2 MDN)
  public void receipt(String[] source, String[] mailbox, String messageID)
    throws Exception
  {
    util.debug("ILexiComIncoming.receipt() source=>" + LexUtil.convert(ILexiCom.SERVICE, source) + "<\n" +
               "                           mailbox=>" + LexUtil.convert(ILexiCom.MAILBOX, mailbox) + "<\n" +
               "                           messageID=>" + messageID + "<");
    // throw exception if payload not acceptable
//    throw new LexiComException("I just don't like this payload");
  }

/*------------------------------------------------------------------------------
 * ILexiComMultipartIncoming interface
 *----------------------------------------------------------------------------*/
  // one part of incoming multipart payload
  public OutputStream open(String[] source,
                           String[] mailbox,
                           int index,
                           File file,
                           String messageID,
                           Properties parameters)
    throws Exception
  {
    util.debug("ILexiComMultipartIncoming.open() index=>" + index + "<");

    // for testing just use open() method above in ILexiComIncoming interface for multipart too
    return open(source, mailbox, file, messageID, parameters);
  }

  // multipart payload complete
  public void close(String messageID)
    throws Exception
  {
    util.debug("ILexiComMultipartIncoming.close() messageID=>" + messageID + "<");
  }
}