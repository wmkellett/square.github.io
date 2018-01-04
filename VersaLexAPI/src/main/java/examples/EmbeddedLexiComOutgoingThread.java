package examples;

import java.io.*;
import java.util.*;

import com.cleo.lexicom.*;
import com.cleo.lexicom.external.*;

/*******************************************************************************
 * Sample implementation of LexiComOutgoingThread that can be embedded directly
 * inside VersaLex engine.  See javadoc for LexiComOutgoingThread for more
 * information.
 ******************************************************************************/
public class EmbeddedLexiComOutgoingThread extends LexiComOutgoingThread
{
  EmbeddedUtil util;
  String[] testMailboxPath = new String[] {"Looptest AS2", "myMailbox"};

  public EmbeddedLexiComOutgoingThread()
    throws Exception
  {
    util = new EmbeddedUtil();
  }

/*------------------------------------------------------------------------------
 * LexiComOutgoingThread interface
 *----------------------------------------------------------------------------*/
  public void run() {
    Hashtable hashtable = new Hashtable();
    try {
      util.debug("LexiComOutgoingThread.run() starting...");

      File dir = new File("outbox");
      while (!isStopped()) {
        try {
/*------------------------------------------------------------------------------
 *        Find stable files in outbox
 *----------------------------------------------------------------------------*/
          File[] files = dir.listFiles();
          for (int i = 0; files != null && i < files.length; i++) {
            if (files[i].isFile() && files[i].canRead()) {
              CheckFile checkFile = (CheckFile)hashtable.get(files[i].getName());
              // if first check of file, just create entry in hashtable
              if (checkFile == null)
                hashtable.put(files[i].getName(), new CheckFile(files[i],
                                                                System.currentTimeMillis(),
                                                                files[i].lastModified(),
                                                                files[i].length()));

              // if file's last modified time/date or length have changed
              else if (checkFile.lastModified != checkFile.file.lastModified() ||
                       checkFile.lastLength != checkFile.file.length()) {
                checkFile.lastChange   = System.currentTimeMillis();
                checkFile.lastModified = checkFile.file.lastModified();
                checkFile.lastLength   = checkFile.file.length();

              // otherwise if it's been stable for at least 7 seconds
              } else if (checkFile.lastChange < System.currentTimeMillis() - 7500L) {
                boolean canRead = true;
                // make sure can open file
                try {
                  FileInputStream in = new FileInputStream(files[i]);
                  in.close();
                } catch (Exception ex) {
                  canRead = false;
                }
                if (canRead) {
                  hashtable.remove(files[i].getName());

/*------------------------------------------------------------------------------
 *                Send the file out through our testing mailbox
 *----------------------------------------------------------------------------*/
                  IMailboxController mailbox = this.getMailboxController(testMailboxPath);
                  if (mailbox.send(new LexiComOutgoing(files[i]), false))
                    files[i].delete();
                  mailbox.end();
                }
              }
            }
          }

          // clean out table of any files that were deleted before we could try sending them
          Enumeration enum1 = hashtable.keys();
          while (enum1.hasMoreElements()) {
            String name = (String)enum1.nextElement();
            CheckFile checkFile = (CheckFile)hashtable.get(name);
            if (checkFile != null && !checkFile.file.exists())
              hashtable.remove(name);
          }

          // sleep for a bit
          Thread.sleep(5000L);
        } catch (Exception ex) {
          ex.printStackTrace(new PrintWriter(LexUtil.getSystemDebugWriter()));
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace(new PrintWriter(LexUtil.getSystemDebugWriter()));
    } finally {
      util.debug("LexiComOutgoingThread.run() exiting...");
    }
  }

/*------------------------------------------------------------------------------
 * Class used to check file stability
 *----------------------------------------------------------------------------*/
  protected class CheckFile {
    File file;
    long lastChange;
    long lastModified;
    long lastLength;
    protected CheckFile(File file, long lastChange, long lastModified, long lastLength) {
      this.file = file;
      this.lastChange   = lastChange;
      this.lastModified = lastModified;
      this.lastLength   = lastLength;
    }
  }
}
