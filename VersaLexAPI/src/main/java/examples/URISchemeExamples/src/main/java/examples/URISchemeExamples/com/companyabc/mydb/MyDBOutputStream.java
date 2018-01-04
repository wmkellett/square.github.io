package examples.URISchemeExamples.com.companyabc.mydb;

import com.cleo.lexicom.beans.LexURIFile;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.ArrayList;

/**
 * MyDBOutputStream is a VLTrader URI custom scheme used as an OutputStream
 * for a {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBFile}.
 * 
 * <p> <b>NOTES:</b>
 * <ul><li>This is a <b>sample</b> implementation of a <i>Scheme</i>OutputStream
 *            class.</li>
 *     <li>All implementing <i>Scheme</i>OutputStream classes <b>MUST</b> extend
 *      <a href="http://docs.oracle.com/javase/6/docs/api/java/io/OutputStream.html">java.io.OutputStream</a>.
 * </li></ul>
 * 
 * <p> This class saves all the bytes written and writes the database
 * record on the {@link #close()} call. However, the database connection
 * is opened on the first <code>write</code>.
 */
public class MyDBOutputStream extends OutputStream {
  // Private attributes
  private MyDBFile   myDBFile      = null;
  private Connection dbConnection  = null;
  private boolean    openAttempted = false;
  private ArrayList<byte[]> outputByteArray = new ArrayList<byte[]>();

  /**
   * Creates a MyDBOutputStream class using the <code>LexURIFile</code>
   * provided. The <code>LexURIFile</code> can be cast to a
   * {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBFile} so data regarding
   * the URI can be retrieved.
   * 
   * @param lexURIFile <code>LexURIFile</code> representing the database table
   *                   and record information to be written
   * @param append If appending to the <code>LexURIFile</code> is desired.
   *               (NOTE: This is not supported).
   * @throws Exception
   */
  public MyDBOutputStream(LexURIFile lexURIFile, boolean append) throws Exception {
    this.myDBFile = (MyDBFile)lexURIFile;
    LexURIFile.debug("MyDBOutputStream(" + this.myDBFile.getPath() + "," + append + ")");
    if (append)
      throw new Exception("Appending to an MyDB record is not supported");
  }

  /**
   * Writes the record to the DB and closes the connection.
   * 
   * @throws IOException 
   */
  @Override
  public void close() throws IOException {
    if (this.dbConnection != null) {
      try {
        // Count total number of bytes received
        int numBytes = 0;
        for (int ii = 0; ii < this.outputByteArray.size(); ii++)
          numBytes += this.outputByteArray.get(ii).length;
        
        // Allocate memory for entire message
        byte[] fileBytes = new byte[numBytes];
        // Copy all the byte arrays into a single byte array
        int byteCtr = 0;
        for (int obaIdx = 0; obaIdx < this.outputByteArray.size(); obaIdx++) {
          byte[] tmpBytes = this.outputByteArray.get(obaIdx);
          int tbLen = tmpBytes.length;
          for (int tbIdx = 0; tbIdx < tbLen; tbIdx++)
            fileBytes[byteCtr++] = tmpBytes[tbIdx];
          this.outputByteArray.set(obaIdx, null); // Release the old memory
        }
        this.outputByteArray.clear();

        // Get the filename to store in the record
        String filename = myDBFile.getFilename();
        if ((filename == null) || (filename.trim().length() == 0))
          filename = "MyDB_" + System.currentTimeMillis();

        // Write the record to the database
        MyDBUtil.writeRecord(this.dbConnection, this.myDBFile.getTableName(),
                             filename, fileBytes);
      } catch (Exception ex) {
        LexURIFile.debug(true, "MyDBOutputStream.close> Exception during "
                + "MyDBUtil.writeRecord. MyDBFile path['"
                + this.myDBFile.getPath() + "]", ex);
        throw new IOException(ex);
      } finally {
        if (this.dbConnection != null) {
          try {
            this.dbConnection.close();
          } catch (Exception ex) {
            LexURIFile.debug(true, "MyDBOutputStream.close> Exception during "
                    + "dbConnection.close(). MyDBFile path['"
                    + this.myDBFile.getPath() + "]", ex);
          }
        }
      }
      this.dbConnection = null;
    }
  }
  
  /**
   * Flushes the output.
   * 
   * <p> This method does nothing.
   * @throws IOException 
   */
  @Override
  public void flush() throws IOException {
    // Does nothing
  }
  
  /**
   * Writes byte array <code>b[]</code> to the output stream.
   * 
   * Since we are not streaming to the database, we save the bytes into the
   * <code>outputByteArray</code>.
   * 
   * @param b Byte array to be written.
   * @throws IOException 
   */
  @Override
  public void write(byte[] b) throws IOException {
    // Parameter checking
    if (b == null)
      throw new NullPointerException();

    // Check if database is open
    checkDBOpen();

    // Save the bytes in outputByteArray
    if ((b != null) && (b.length > 0)) {
      byte[] byteCopy = new byte[b.length];
      System.arraycopy(b, 0, byteCopy, 0, b.length);
      this.outputByteArray.add(byteCopy);
    }
  }
  
  /**
   * Writes byte array <code>b[]</code> starting at <code>off</code> for
   * a total of <code>len</code> bytes to the output stream.
   * 
   * Since we are not streaming to the database, we save the bytes into the
   * <code>outputByteArray</code>.
   * 
   * @param b   Byte array to be partially written
   * @param off Offset into <code>b[]</code> of where to start writing
   * @param len Number of bytes to write
   * @throws IOException 
   */
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    // Parameter checking
    if (b == null)
      throw new NullPointerException();
    else if ((off < 0) || (off >= b.length))
      throw new IOException("Invalid 'off' parameter");
    
    // Check if database is open
    checkDBOpen();

    // Save the bytes in outputByteArray
    if (len > 0) {
      byte[] byteCopy = new byte[len];
      if (byteCopy.length > 0) {
        System.arraycopy(b, off, byteCopy, 0, len);
        this.outputByteArray.add(byteCopy);
      }
    }
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
    // Check if database is open
    checkDBOpen();

    // Save the byte in outputByteArray
    byte[] byteCopy = new byte[1];
    byteCopy[0] = (byte)b;
    this.outputByteArray.add(byteCopy);
  }
  
//------------------------------------------------------------------------------
// Helper functions
//------------------------------------------------------------------------------
  /**
   * Opens the database if it has not been attempted previously.
   * 
   * @throws IOException 
   */
  private void checkDBOpen() throws IOException {
    if (!this.openAttempted)
      openDBConnection();

    if (this.dbConnection == null)
      throw new IOException("DB Connection is not open");
  }

  /**
   * Opens the database connection.
   * 
   * @throws IOException 
   */
  private void openDBConnection() throws IOException {
    this.openAttempted = true;
    
    try {
      this.dbConnection = MyDBUtil.getDBConnection();
    } catch (Exception ex) {
      LexURIFile.debug(true, "MyDBOutputStream.openDBConnection> Exception "
              + "opening Database. MyDBFile path[" + this.myDBFile.getPath()
                       + "]", ex);
      throw new IOException("Failed to open '" + this.myDBFile.getPath()
              + "'", ex);
    }
  }

}
