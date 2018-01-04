package examples.URISchemeExamples.com.companyabc.myfs;

import com.cleo.lexicom.beans.LexURIFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * MyFSOutputStream is a VLTrader URI custom scheme used as an OutputStream
 * for a {@link examples.URISchemeExamples.com.companyabc.myfs.MyFSFile}.
 * 
 * <p> <b>NOTES:</b>
 * <ul><li>This is a <b>sample</b> implementation of a <i>Scheme</i>OutputStream
 *            class.</li>
 *     <li>All implementing <i>Scheme</i>OutputStream classes <b>MUST</b> extend
 *      <a href="http://docs.oracle.com/javase/6/docs/api/java/io/OutputStream.html">java.io.OutputStream</a>.
 * </li></ul>
 * 
 * This class uses a 
 * <a href="http://docs.oracle.com/javase/6/docs/api/java/io/FileOutputStream.html">java.io.FileOutputStream</a>
 * on a 
 * <a href="http://docs.oracle.com/javase/6/docs/api/java/io/File.html">java.io.File</a>
 * object stored with a {@link examples.URISchemeExamples.com.companyabc.myfs.MyFSFile}
 * object.
 */
public class MyFSOutputStream extends OutputStream {
  private MyFSFile myFSFile = null;
  private boolean  append = false;
  private boolean  openAttempted = false;
  private FileOutputStream myOutputStream = null;

//  public MyFSOutputStream(String uri, boolean append) throws Exception {
//    LexURIFile.debug("MyFSOutputStream(" + uri + "," + append + ")");
//    this.myFSFile = new MyFSFile(uri);
//    this.append = append;
//  }

  /**
   * Creates a MyFSOutputStream class using the <code>LexURIFile</code>
   * provided. The <code>LexURIFile</code> can be cast to a
   * {@link examples.URISchemeExamples.com.companyabc.myfs.MyFSFile} so data regarding
   * the URI can be retrieved.
   * 
   * @param lexURIFile <code>LexURIFile</code> representing a folder or file
   *                   within the base or common folders.
   * @param append If appending to the <code>LexURIFile</code> is desired.
   * @throws Exception
   */
  public MyFSOutputStream(LexURIFile lexURIFile, boolean append) throws Exception {
    this.myFSFile = (MyFSFile)lexURIFile;
    LexURIFile.debug("MyFSOutputStream(" + this.myFSFile.getPath() + "," + append + ")");
    this.append = append;
  }

  /**
   * Closes the file output stream.
   * 
   * @throws IOException 
   */
  @Override
  public void close() throws IOException {
    if (myOutputStream != null) {
      myOutputStream.close();
      myOutputStream = null;
    }
  }
  
  /**
   * Flushes the output.
   * 
   * @throws IOException 
   */
  @Override
  public void flush() throws IOException {
    if (myOutputStream != null)
      myOutputStream.flush();
  }
  
  /**
   * Writes byte array <code>b[]</code> to the output stream.
   * 
   * @param b Byte array to be written.
   * @throws IOException 
   */
  @Override
  public void write(byte[] b) throws IOException {
    if (b == null)
      throw new NullPointerException();

    checkStreamOpen();

    if (myOutputStream != null)
      myOutputStream.write(b);
  }
  
  /**
   * Writes byte array <code>b[]</code> starting at <code>off</code> for
   * a total of <code>len</code> bytes to the output stream.
   * 
   * @param b   Byte array to be partially written
   * @param off Offset into <code>b[]</code> of where to start writing
   * @param len Number of bytes to write
   * @throws IOException 
   */
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (b == null)
      throw new NullPointerException();
    else if ((off < 0) || (off >= b.length))
      throw new IOException("Invalid 'off' parameter");
    
    checkStreamOpen();

    if ((len > 0) && (myOutputStream != null))
      myOutputStream.write(b, off, len);
  }
  
  /**
   * Writes a byte, <code>b</code>, to the output stream.
   * 
   * Since we are not streaming to the database, we save the byte into the
   * <code>outputByteArray</code>.
   * 
   * @param b Byte to be written
   * @throws IOException 
   */
  @Override
  public void write(int b) throws IOException {
    checkStreamOpen();

    if (myOutputStream != null)
      myOutputStream.write(b);
  }

//------------------------------------------------------------------------------
// Helper functions
//------------------------------------------------------------------------------
  /**
   * Opens the file output stream if it has not been attempted previously.
   * 
   * @throws IOException 
   */
  private void checkStreamOpen() throws IOException {
    if (!this.openAttempted)
      openOutputStream();

    if (this.myOutputStream == null)
      throw new IOException("MYFS output stream is not open");
  }

  /**
   * Opens the file output stream.
   * 
   * @throws IOException 
   */
  private void openOutputStream() throws IOException {
    this.openAttempted = true;
    
    try {
      this.myOutputStream = new FileOutputStream(this.myFSFile.getInternalFile(), this.append);
    } catch (Exception ex) {
      LexURIFile.debug(true, "MyFSOutputStream.openOutputStream> Exception opening OutputStream. "
                       + " MyFSFile path[" + this.myFSFile.getPath()
                       + "]", ex);
      throw new IOException("Failed to open MYFS '" + this.myFSFile.getPath() + "'", ex);
    }
  }
}
