package examples;

import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;

import org.w3c.dom.*;
import com.cleo.lexicom.*;
import com.cleo.lexicom.external.*;
import javax.activation.*;

/*******************************************************************************
 * Sample implementation of LexiComLogListener that can be embedded directly
 * inside VersaLex engine.  See javadoc for LexiComLogListener for more
 * information.
 ******************************************************************************/
public class EmbeddedLexiComLogListener implements LexiComLogListener
{
  EmbeddedUtil util;

  public EmbeddedLexiComLogListener()
    throws Exception
  {
    util = new EmbeddedUtil();
  }

/*------------------------------------------------------------------------------
 * LexiComLogListener interface
 *----------------------------------------------------------------------------*/
  public void log(LexiComLogEvent e) {
    Element event = e.getEvent();
    if (LexUtil.isResult(event)) {
      String status = event.getAttribute("text");

/*------------------------------------------------------------------------------
 *    On failure, email the system administrator
 *----------------------------------------------------------------------------*/
      if (LexUtil.isFailureResult(status)) {
        util.debug("LexiComLogListener.log() Problem result detected=>" + status + "<, sending email");
        new SendMail(status, event, e.getMessage()).start();
      }
    }
  }
  public class SendMail extends Thread {
    String status;
    Element event;
    String message;
    public SendMail(String status, Element event, String message) {
      this.status = status;
      this.event = event;
      this.message = message;
    }
    public void run() {
      String host = event.getAttribute("host");
      String mailbox = event.getAttribute("mailbox");
      String source = event.getAttribute("source");
      String destination = event.getAttribute("destination");
      String direction = event.getAttribute("direction");

      try {
        Session session = LexUtil.getSession();
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setSubject("Failure at " + util.getProduct() + " " + util.getSerialNumber());
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress("jdoe@abc.com"));
        mimeMessage.setFrom(new InternetAddress("jdoe@abc.com"));

        // Create and fill first bodypart with the logged message
        Multipart mp = new MimeMultipart();
        MimeBodyPart part1 = new MimeBodyPart();
        String text = status + " encountered";
        if (LexUtil.isOutgoingFile(direction)) {
          text += " while sending";
          if (source.length() > 0)
            text += " " + source;
          if (host.length() > 0 || mailbox.length() > 0)
            text += " to " + host + "\\" + mailbox;
        } else if (LexUtil.isIncomingFile(direction)) {
          text += " while receiving";
          if (destination.length() > 0)
            text += " " + destination;
          if (host.length() > 0 || mailbox.length() > 0)
            text += " from " + host + "\\" + mailbox;
        } else if (LexUtil.isCopyFile(direction))
          text += " while copying " + source + " to " + destination;
        text += ":\n\n" + this.message;
        part1.setText(text);
        mp.addBodyPart(part1);

        // Create additional part with log file attached
        MimeBodyPart part2 = new MimeBodyPart();
        File logfile = new File ("logs" + File.separator +
                                 util.getProduct() + ".xml");
        FileDataSource fds2 = new FileDataSource(logfile);
        part2.setDataHandler(new DataHandler(fds2));
        part2.setFileName(fds2.getName());
        mp.addBodyPart(part2);
        mimeMessage.setContent(mp);
        mimeMessage.saveChanges();

        LexUtil.sendMail(session, mimeMessage);
      } catch (Exception ex) {
        util.debug("LexiComLogListener.log() Error sending email=>" + ex.toString() + "<");
      }
    }
  }
}
