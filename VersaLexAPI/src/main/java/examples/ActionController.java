package examples;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.cleo.lexicom.*;
import com.cleo.lexicom.beans.*;
import com.cleo.lexicom.external.*;

public class ActionController implements ILexiComIncoming, ILexiComMultipartIncoming {
  public static void main(String[] args)
  {
    new ActionController();
  }
  public ActionController()
  {
    try {
      // get a remote client instance
      ILexiCom lexicom = LexiComFactory.getVersaLex(LexiComFactory.VLTRADER,
                                                    "C:\\Program Files\\VLTrader",
                                                    LexiComFactory.CLIENT_ONLY);
      // CHANGE HOST/MAILBOX/ACTION TO YOURS
      String host = "Looptest HTTP";
      String mailbox = "myMailbox";
      String action = "receive";

      String[] path  = {host, mailbox, action};

      // clone a new temporary receive action
      path[ILexiCom.ACTION_INDEX] = lexicom.clone(ILexiCom.ACTION, path, true);

      // get the action controller
      IActionController controller = lexicom.getActionController(path);

      try {
        // get the action's commands and execute them one-by-one
        String[] commands = lexicom.getProperty(ILexiCom.ACTION, path, "Commands");
        StringTokenizer tokens = new StringTokenizer(commands[0], "\r\n", false);
        while (tokens.hasMoreTokens()) {
          String command = tokens.nextToken();
          LexCommand lcommand = controller.parse(command);

          // if it's a macro GET command
          if (lcommand.getType() == LexCommand.MACRO &&
              lcommand.getCommand().equalsIgnoreCase("GET")) {
            // ADD IN A SET COMMAND HERE
            // controller.execute("SET Host.Path.GET=...");

            Properties parms = lcommand.getParameters();
            // ADD IN YOUR OWN NAME=VALUE PARAMETER SETTINGS HERE
            // parms.setProperty("StationID", "5097");

            controller.get(lcommand.getOptions(),
                           lcommand.getSource(),
                           new RemoteLexiComIncoming(this),
                           parms);

          // if it's not a macro GET command, just execute the command as is
          } else if (lcommand.getType() != LexCommand.COMMENT)
            controller.execute(command);
        }

        //  print out the result
        Element element = controller.getLastResult();
        if (element != null) {
          System.out.println("Event=>" + element.getNodeName() + "<");
          NamedNodeMap list = element.getAttributes();
          for (int i = 0; i < list.getLength(); i++)
            System.out.println(" Attr=>" + list.item(i).getNodeName() + "=" + list.item(i).getNodeValue() + "<");
          NodeList children = element.getChildNodes();
          for (int i = 0; i < children.getLength(); i++)
            System.out.println(" Text=>" + children.item(i).getNodeValue() + "<");
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      } finally {
        // end the action
        controller.end();
      }

      // close down the LexiCom instance
      lexicom.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(0);
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
    System.out.println("ILexiComIncoming.open() source=>" + LexUtil.convert(ILexiCom.SERVICE, source) + "<\n" +
                       "                        mailbox=>" + LexUtil.convert(ILexiCom.MAILBOX, mailbox) + "<\n" +
                       "                        default file=>" + file.getPath() + "<\n" +
                       "                        messageID=>" + messageID + "<\n" +
                       "                        parameters=>>>");
    if (parameters != null)
      parameters.list(System.out);
    System.out.println("<<<");

    // return null to use default file
    return null;
  }

  // receipt generation
  public void receipt(String[] source, String[] mailbox, String messageID)
    throws Exception
  {
    System.out.println("ILexiComIncoming.receipt() source=>" + LexUtil.convert(ILexiCom.SERVICE, source) + "<\n" +
                       "                           mailbox=>" + LexUtil.convert(ILexiCom.MAILBOX, mailbox) + "<\n" +
                       "                           messageID=>" + messageID + "<");
    // throw exception if payload not acceptable
//    throw new Exception("I just don't like this payload");
  }

/*------------------------------------------------------------------------------
 * ILexiComMultipartIncoming interface
 *----------------------------------------------------------------------------*/
  // incoming multipart payload
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

  // one part of incoming multipart payload
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

  // multipart payload complete
  public void close(String messageID)
    throws Exception
  {
    System.out.println("ILexiComMultipartIncoming.close() messageID=>" + messageID + "<");
  }
}
