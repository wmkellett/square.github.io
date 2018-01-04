package examples.URISchemeExamples.com.companyabc.myfs;

import com.cleo.lexicom.beans.LexURIFile;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * MyFSInputStream is a VLTrader URI custom scheme used as an InputStream
 * for a {@link examples.URISchemeExamples.com.companyabc.myfs.MyFSFile}.
 * 
 * <p> <b>NOTES:</b>
 * <ul><li>This is a <b>sample</b> implementation of a <i>Scheme</i>InputStream
 *        class.</li>
 *     <li>All implementing <i>Scheme</i>InputStream classes <b>MUST</b> extend
 *         <a href="http://docs.oracle.com/javase/6/docs/api/java/io/InputStream.html">java.io.InputStream</a>.
 * </li></ul>
 * 
 * This class uses a 
 * <a href="http://docs.oracle.com/javase/6/docs/api/java/io/FileInputStream.html">java.io.FileInputStream</a>
 * on a 
 * <a href="http://docs.oracle.com/javase/6/docs/api/java/io/File.html">java.io.File</a>
 * object stored with a {@link examples.URISchemeExamples.com.companyabc.myfs.MyFSFile}
 * object.
 */
public class MyFSInputStream extends InputStream {
  private MyFSFile myFSFile = null;
  private boolean openAttempted = false;
  private FileInputStream myInputStream = null;
  
//  public MyFSInputStream(String uri) throws Exception {
//    LexURIFile.debug("MyFSInputStream(URI '" + uri + "')");
//    this.myFSFile = new MyFSFile(uri);
//  }
  
  /**
   * Creates a MyFSInputStream class using the <code>LexURIFile</code>
   * provided. The <code>LexURIFile</code> can be cast to a
   * {@link examples.URISchemeExamples.com.companyabc.myfs.MyFSFile} so data regarding
   * the URI can be retrieved.
   * 
   * @param lexURIFile <code>LexURIFile</code> representing a folder or file
   *                   within the base or common folders.
   * @throws Exception 
   */
  public MyFSInputStream(LexURIFile lexURIFile) throws Exception {
    this.myFSFile = (MyFSFile)lexURIFile;
    LexURIFile.debug("MyFSInputStream(LexURIFile '" + myFSFile.getPath() + "')");
  }

  /**
   * Returns the number of bytes available to read.
   * 
   * @return Remaining bytes that can be read
   * @throws IOException 
   */
  @Override
  public int available() throws IOException {
    checkStreamOpen();
    return myInputStream.available();
  }
  
  /**
   * Closes the MyFSInputStream input stream.
   * @throws IOException 
   */
  @Override
  public void close() throws IOException {
    if (myInputStream != null) {
      myInputStream.close();
      myInputStream = null;
    }
  }
  
  /**
   * Creates a <code>mark</code> position in the input stream at the current
   * position in the stream.
   * @param readlimit This parameter is ignored.
   */
  @Override
  public void mark(int readlimit) {
    if (myInputStream != null)
      myInputStream.mark(readlimit);
  }
  
  /**
   * Returns <code>true</code> if the FileInputStream supports mark().
   * @return <code>true</code> if mark() is supported; <code>false</code> otherwise;
   */
  @Override
  public boolean markSupported() {
    if (myInputStream != null)
      return myInputStream.markSupported();
    else
      return false;
  }
  
  /**
   * Reads are returns a byte from the input stream.
   * 
   * @return The byte read or -1 if EOF has been reached
   * @throws IOException 
   */
  @Override
  public int read() throws IOException {
    checkStreamOpen();
    return myInputStream.read();
  }
  
  /**
   * Reads from the input stream into the byte array and returns the number
   * of bytes read.
   * 
   * @param b Byte array to store data read
   * @return  Number of bytes read or -1 for EOF
   * @throws IOException 
   */
  @Override
  public int read(byte[] b) throws IOException {
    checkStreamOpen();
    return myInputStream.read(b);
  }
  
  /**
   * Reads from the input stream into a section of the byte array.
   * 
   * @param b   Byte array to store the data read
   * @param off Offset into byte array to start storing data
   * @param len Number of bytes to read and store
   * @return Number of bytes read or -1 for EOF
   * @throws IOException 
   */
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    checkStreamOpen();
    return myInputStream.read(b, off, len);
  }
  
  /**
   * Resets input stream to last position specified by {@link #mark(int)} or
   * the beginning of the input stream if {@link #mark(int)} has not been
   * called.
   * @throws IOException 
   */
  @Override
  public void reset() throws IOException {
    checkStreamOpen();
    myInputStream.reset();
  }
  
  /**
   * Skips the specified number of bytes in the input stream.
   * 
   * @param n Number of bytes to skip in the input stream
   * @return  Number of bytes skipped
   * @throws IOException 
   */
  @Override
  public long skip(long n) throws IOException {
    checkStreamOpen();
    return myInputStream.skip(n);
  }

//------------------------------------------------------------------------------
// Helper functions
//------------------------------------------------------------------------------
  /**
   * Opens the file input stream if it has not been attempted previously.
   * 
   * @throws IOException 
   */
  private void checkStreamOpen() throws IOException {
    if (!this.openAttempted)
      openInputStream();

    if (this.myInputStream == null)
      throw new IOException("MYFS input stream is not open");
  }

  /**
   * Opens the file input stream.
   * 
   * @throws IOException 
   */
  private void openInputStream() throws IOException {
    this.openAttempted = true;
    
    try {
      this.myInputStream = new FileInputStream(this.myFSFile.getInternalFile());
    } catch (Exception ex) {
      LexURIFile.debug(true, "MyFSOutputStream.openOutputStream> Exception opening OutputStream. "
                       + " MyFSFile path[" + this.myFSFile.getPath()
                       + "]", ex);
      throw new IOException("Failed to open MYFS '" + this.myFSFile.getPath() + "'", ex);
    }
  }
}
