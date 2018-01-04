package examples;

import java.io.*;

import com.cleo.lexicom.external.*;
import com.cleo.lexicom.edi.*;

/*******************************************************************************
 * Used by EmbeddedILexiComIncoming class to filter incoming bytes
 ******************************************************************************/
public class EmbeddedFilterOutputStream extends FilterOutputStream {

  PipedOutputStream pout;
  CheckEDIThread checkEDIThread;

  public EmbeddedFilterOutputStream(OutputStream out)
    throws Exception
  {
    super(out);
    this.pout = new PipedOutputStream();
    this.checkEDIThread = new CheckEDIThread(new PipedInputStream(pout));
    checkEDIThread.setName("EmbeddedFilterOutputStream$CheckEDIThread:" + checkEDIThread.getName());
    checkEDIThread.start();
  }

/*------------------------------------------------------------------------------
 * Filter bytes through pipe
 *----------------------------------------------------------------------------*/
  public void write(int b)
    throws IOException
  {
    pout.write(b);
    if (checkEDIThread.ex != null)
      throw checkEDIThread.ex;
    out.write(b);
  }
  public void write(byte[] b)
    throws IOException
  {
    pout.write(b);
    if (checkEDIThread.ex != null)
      throw checkEDIThread.ex;
    out.write(b);
  }
  public void write(byte[] b,
                    int off,
                    int len)
    throws IOException
  {
    pout.write(b, off, len);
    if (checkEDIThread.ex != null)
      throw checkEDIThread.ex;
    out.write(b, off, len);
  }
  public void close()
    throws IOException
  {
    out.close();
    pout.close();
    try {
      checkEDIThread.join();
    } catch (InterruptedException ex) {
    }
    if (checkEDIThread.ex != null)
      throw checkEDIThread.ex;
  }

/*------------------------------------------------------------------------------
 * Thread which reads piped incoming bytes thru EDIFilterInputStream then checks
 * that file is expected type and has expected sender ID and qualifier
 *----------------------------------------------------------------------------*/
  protected class CheckEDIThread extends Thread {
    PipedInputStream pin;
    private byte[] buffer = new byte[8192];

    IOException ex = null;

    protected CheckEDIThread(PipedInputStream pin) {
      super();
      this.pin = pin;
    }
    public void run() {
      try {
        CheckEDIFilterInputStream in = new CheckEDIFilterInputStream(this.pin);
        if (!in.isX12())
          throw new LexiComNoRetriesException("Only EDI-X12 files accepted");
        int len = in.read(buffer);
        while (len != -1)
          len = in.read(buffer);
        in.close();
      } catch (IOException ex) {
        this.ex = ex;
      }
    }

/*------------------------------------------------------------------------------
 *  Check the interchange has expected sender
 *----------------------------------------------------------------------------*/
    protected void checkSegment(EDISegment edisegment)
      throws IOException
    {
      if (edisegment.getName().equals("ISA")) {
        if (!edisegment.getElement(5).getElement().trim().equals("EDI Sender") ||
            !edisegment.getElement(4).getElement().trim().equals("ZZ"))
          throw new LexiComNoRetriesException("Unexpected EDI sender '" + edisegment.getElement(5).getElement().trim() + ":" +
                                              edisegment.getElement(4).getElement().trim() + "'");
      }
    }

/*------------------------------------------------------------------------------
 *  Extension of EDI filter that simply passes each EDI segment to
 *  CheckEDIThread.checkSegment() method
 *----------------------------------------------------------------------------*/
    protected class CheckEDIFilterInputStream extends EDIFilterInputStream {
      protected CheckEDIFilterInputStream(InputStream in)
        throws IOException
      {
        super(in);
      }
      protected void process(EDISegment edisegment)
        throws IOException
      {
        CheckEDIThread.this.checkSegment(edisegment);
        super.buffer(edisegment.getSegment());
      }
    }
  }
}
