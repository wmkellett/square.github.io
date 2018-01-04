package examples;

import java.io.*;
import java.util.*;

import com.cleo.lexicom.*;
import com.cleo.lexicom.beans.*;
import com.cleo.lexicom.external.*;


/*******************************************************************************
 * Sample Http Response Listener class for exposing the HTTP request and
 * response objects.  This example shows how to define a callback in  the
 * mailbox controller to return the HTTP request and response objects and
 * optionally receive the input stream of the HTTP response object.
 ******************************************************************************/

 public class HttpResponseListener implements IHttpResponseListener {

    boolean isRemote;
    boolean returnResponseStream;

    public HttpResponseListener(boolean isRemote, boolean returnResponseStream) {
      this.isRemote = isRemote;
      this.returnResponseStream = returnResponseStream;
    }
    public OutputStream put(String[] path, LexCommand command, HttpRequest request, HttpResponse response)
        throws Exception {
      try {
        String fullPath = "";
        if (path != null) {
          for (int i = 0; i < path.length; i++) {
            fullPath += path[i];
            if (i < path.length-1)
              fullPath += " / ";
          }
        }
        System.out.println("\nIHttpResponseListener.put  path=>" + fullPath + "<\n" +
                           "                           command=>" + command.getCommand() + "<\n\n" +
                           "                     === Http Request ===>\n" +
                           "                         method=>" + request.getMethod() + "<\n" +
                           "                         requestURI=>" + request.getRequestURI()+ "<\n" +
                           "                         queryString=>" + request.getQueryString()+ "<\n" +
                           "                         version=>" + request.getVersion()+ "<\n" );

        Enumeration reqHeaders = request.getHeaderNames();
        if (reqHeaders != null) {
          System.out.println("                         === Request Headers ===>");
          while (reqHeaders.hasMoreElements()) {
           String header = (String)reqHeaders.nextElement();
           String value = request.getHeader(header);
           System.out.println("                         " + header + ": " + value);
          }
        }

        System.out.println("\n                     === Http Response ===>\n" +
                           "                         version=>" + response.getVersion() + "<\n" +
                           "                         statusCode=>" + response.getStatusCode() + "<\n" +
                           "                         reasonLine=>" + response.getReasonLine() + "<\n");

        Enumeration respHeaders = response.getHeaderNames();
        if (respHeaders != null) {
          System.out.println("                         === Response Headers ===>");
          while (respHeaders.hasMoreElements()) {
            String header = (String)respHeaders.nextElement();
            String value = response.getHeader(header);
            System.out.println("                         " + header + ": " + value);
          }
        }

        Enumeration respTrailers = response.getTrailerNames();
        if (respTrailers != null) {
          boolean firstTrailer = true;

          while (respTrailers.hasMoreElements()) {
            if (firstTrailer) {
              System.out.println("\n                         === Response Trailers ===>");
              firstTrailer = false;
            }
            String trailer = (String)respTrailers.nextElement();
            String value = response.getTrailer(trailer);
            System.out.println("                         " + trailer + ": " + value);
          }
        }
        if (returnResponseStream) {
          // Create pipes for reading the returned input stream,
          // i.e., the specified output stream is "piped"
          // to an input stream so it can be read
          PipedInputStream pipeIn = new PipedInputStream();
          PipedOutputStream pipeOut = new PipedOutputStream(pipeIn);

          // Start the thread to read from the piped stream
          new ReaderThread(pipeIn).start();

          // When running as a remote application, the output stream
          // must be returned as a RemoteOutputStream otherwise when
          // running as an embedded application within VersaLex, or
          // within the same JVM, just the native output stream may be returned

          if (isRemote)
            return new RemoteOutputStream(new OutputStreamWrapper(pipeOut));
          else
            return pipeOut;

        } else {
          // If the contents of the HTTP response input stream are not desired,
          // then just return a null value
          return null;
        }

      } catch (Exception ex) {
        ex.printStackTrace();
        throw ex;
      }
    }

  /*******************************************************************************
   * ReaderThread - receives the input stream and processes it
   *******************************************************************************/
    private class ReaderThread extends Thread {
      InputStream  in;
      boolean firstRead = true;

      ReaderThread(InputStream in) {
        this.in  = in;
      }

      public void run() {
        // Now start receiving the returned stream
        int numRead;
        byte[] bytes = new byte[1024];
        try {
          while ((numRead = in.read(bytes)) > 0) {
            if (firstRead) {
              System.out.println("\n                         === Response Input Stream ===>");
              firstRead = false;
            }
            String string = new String(bytes, 0, numRead);
            System.out.print(string);
          }
          if (!firstRead)
            System.out.println("\n");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
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
 * Test
 ******************************************************************************/
  public static void main(String[] args) {
    try {
      String LexiComHome = "C:\\Program Files\\LexiCom";

/*------------------------------------------------------------------------------
*    Get a client instance of LexiCom (LexiCom must already be running as a
*    Windows service or Unix daemon)
*----------------------------------------------------------------------------*/
      ILexiCom lexicom = LexiComFactory.getVersaLex(LexiComFactory.LEXICOM,
                                                    LexiComHome,
                                                    LexiComFactory.CLIENT_ONLY);

      String httpPartner = "Local HTTP Test\\myMailbox";
      boolean foundHttpPartner = false;
/*------------------------------------------------------------------------------
 *    Get (and optionally print) the list of host\mailboxes
 *----------------------------------------------------------------------------*/
      String[] hostMailboxes = listHostMailboxes(lexicom);
      for (int i = 0; i < hostMailboxes.length; i++) {
        //System.out.println("Host\\mailbox=>" + hostMailboxes[i] + "<");

        if (hostMailboxes[i].equals(httpPartner))
          foundHttpPartner = true;
      }

/*------------------------------------------------------------------------------
 *    If there's an HTTP partner, go ahead and test sending and
 *    receiving the response through the callback.
 *----------------------------------------------------------------------------*/
      if (foundHttpPartner) {
        System.out.println("\nTesting '" + httpPartner + "' host\\mailbox...");

        StringTokenizer tokens = new StringTokenizer(httpPartner, "\\", false);
        if (tokens.countTokens() != 2)
          throw new LexiComException("Host and mailbox names must be separated by but not contain a \\ character.");

        String[] partner = new String[] {tokens.nextToken(), tokens.nextToken()};

        // Define the mailbox controller
        IMailboxController controller = lexicom.getMailboxController(partner);

        // Define an implementation of IHttpResponseListener.
        // When the mailbox controller receives a response from the HTTP partner,
        // the put method in this class will be called allowing processing or
        // further manipulation of the HTTP request and response.

        // Note: Because we're running through a remote client, the HTTPResponseListener
        // implementation MUST be wrapped in a RemoteHttpResponseListener
        controller.setHttpResponseListener(new RemoteHttpResponseListener(new HttpResponseListener(true, true)));

/*------------------------------------------------------------------------------
 *        Stream out a test file
 *----------------------------------------------------------------------------*/
        File file = new File(LexiComHome, "outbox" + File.separator +
                                          "test" + File.separator +
                                          "test.edi");
        LexiComOutgoing[] outgoing =
              new LexiComOutgoing[] {new LexiComOutgoing(new FileInputStream(file),
                                                         new LexMimeType().getMimeType(file.getPath()),
                                                         file.getName(),
                                                         file.length())
                                     };

        RemoteLexiComOutgoing routgoing = new RemoteLexiComOutgoing(outgoing);
        controller.send(routgoing, false);

      } else
        System.out.println("Create a '" + httpPartner + "' host\\mailbox for testing.");

/*------------------------------------------------------------------------------
 *    Close down the LexiCom instance
 *----------------------------------------------------------------------------*/
      lexicom.close();

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    System.exit(0);
  }
}
