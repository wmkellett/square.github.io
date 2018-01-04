package examples.URISchemeExamples.com.companyabc.mydb;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;

import com.cleo.lexicom.beans.LexURIFile;

/**
 * MyDBInputStream is a VLTrader URI custom scheme used as an InputStream
 * for a {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBFile}.
 *
 * <p> <b>NOTES:</b>
 * <ul><li>This is a <b>sample</b> implementation of a <i>Scheme</i>InputStream
 *        class.</li>
 *     <li>All implementing <i>Scheme</i>InputStream classes <b>MUST</b> extend
 *         <a href="http://docs.oracle.com/javase/6/docs/api/java/io/InputStream.html">java.io.InputStream</a>.
 * </li></ul>
 *
 * This class will read the database record and save the bytes internally.
 * As <code>read(...)</code> operations are called, it returns data from the
 * internal byte array. The record is read from the database on the first
 * call to {@link #available()}, {@link #read()}, {@link #read(byte[])}, or
 * {@link #read(byte[], int, int)}.
 */
public class MyDBInputStream extends InputStream {
  // Private attributes
  private MyDBFile   myDBFile      = null;
  private Connection dbConnection  = null;
  private boolean    openAttempted = false;
  private MyDBItem   myDBItem      = null;
  private byte[]     fileBytes     = new byte[0];
  private int        nextBytePos   = 0;
  private int        markPos       = 0;
  private static HashMap<String, Long> lastRecordIDs = new HashMap<String, Long>();

  /**
   * Creates a MyDBInputStream class using the <code>LexURIFile</code>
   * provided. The <code>LexURIFile</code> can be cast to a
   * {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBFile} so data regarding
   * the URI can be retrieved.
   *
   * @param lexURIFile <code>LexURIFile</code> representing the database
   *                   table and record from which to read.
   * @throws Exception
   */
  public MyDBInputStream(LexURIFile lexURIFile) throws Exception {
    this.myDBFile = (MyDBFile)lexURIFile;
    LexURIFile.debug("MyDBInputStream(LexURIFile '" + myDBFile.getPath() + "')");
  }

  /**
   * Returns the number of bytes available to read.
   *
   * @return Remaining bytes that can be read
   * @throws IOException
   */
  @Override
  public int available() throws IOException {
    checkDBOpen();
    if (this.myDBItem == null)
      readRecord(false/*existsCheck*/);
    return fileBytes.length - nextBytePos;
  }

  /**
   * Closes the MyDBInputStream input stream.
   * @throws IOException
   */
  @Override
  public void close() throws IOException {
    if (this.dbConnection != null) {
      this.fileBytes = new byte[0];
      try {
        this.dbConnection.close();
      } catch (Exception ex) {
        LexURIFile.debug(true, "MyDBInputStream.close> Exception closing. MyDBFile path["
                         + this.myDBFile.getPath() + "]", ex);
        throw new IOException(ex);
      }
      this.dbConnection = null;
    }
  }

  /**
   * Creates a <code>mark</code> position in the input stream at the current
   * position in the stream.
   * @param readlimit This parameter is ignored.
   */
  @Override
  public void mark(int readlimit) {
    markPos = nextBytePos;
  }

  /**
   * Returns <code>true</code> as <code>mark</code> is supported (partially).
   * @return <code>true</code>
   */
  @Override
  public boolean markSupported() {
    return true;
  }

  /**
   * Reads are returns a byte from the input stream.
   *
   * @return The byte read or -1 if EOF has been reached
   * @throws IOException
   */
  @Override
  public int read() throws IOException {
    checkDBOpen();
    if (this.myDBItem == null)
      readRecord(false/*existsCheck*/);

    if (nextBytePos >= fileBytes.length)
      return -1;
    return fileBytes[nextBytePos++];
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
    checkDBOpen();
    if (this.myDBItem == null)
      readRecord(false/*existsCheck*/);

    if (nextBytePos >= fileBytes.length)
      return -1;

    int retLen = 0;
    int bytesToRead = b.length;
    int msgLen = fileBytes.length;
    for (int ii = 0; ii < bytesToRead; ii++) {
      if (nextBytePos >= msgLen)
        break;
      b[retLen++] = fileBytes[nextBytePos++];
    }
    return retLen;
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
    checkDBOpen();
    if (this.myDBItem == null)
      readRecord(false/*existsCheck*/);

    if (nextBytePos >= fileBytes.length)
      return -1;

    int bytesRead = 0;
    int msgLen = fileBytes.length;
    for (int ii = 0; ii < len; ii++) {
      if (nextBytePos >= msgLen)
        break;
      b[off + bytesRead++] = fileBytes[nextBytePos++];
    }
    return bytesRead;
  }

  /**
   * Resets input stream to last position specified by {@link #mark(int)} or
   * the beginning of the input stream if {@link #mark(int)} has not been
   * called.
   * @throws IOException
   */
  @Override
  public void reset() throws IOException {
    if (this.dbConnection == null)
      throw new IOException("DB connection is not open");

    nextBytePos = markPos;
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
    checkDBOpen();
    if (this.myDBItem == null)
      readRecord(false/*existsCheck*/);

    long skipLen = 0;
    if (n <= 0)
      return skipLen;

    int msgLen = fileBytes.length;
    for (int ii = 0; ii < n; ii++) {
      if (nextBytePos >= msgLen)
        return skipLen;
      else
        nextBytePos++;
    }
    return skipLen;
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
      throw new IOException("DB connection is not open");
  }


  /**
   * Opens the database connection.
   *
   * @throws IOException
   */
  private void openDBConnection() throws IOException {
    this.openAttempted = true;

    String tableName = myDBFile.getTableName();
    try {
      this.dbConnection = MyDBUtil.getDBConnection();
    } catch (Exception ex) {
      LexURIFile.debug(true, "MyDBInputStream.openDBConnection> Exception "
              + "retrieving database connection for MyDBFile path["
                       + this.myDBFile.getPath() + "]", ex);
      throw new IOException("Failed to open MyDB:" + tableName, ex);
    }
  }

  /**
   * Returns the record length of the next DB record or the current DB record
   * if one has already been read.
   * @return Number of bytes in the next (or current) DB record.
   * @throws IOException
   */
  protected int getNextRecordLen() throws IOException {
    checkDBOpen();
    if (this.myDBItem == null)
      readRecord(true/*existsCheck*/);
    return this.fileBytes.length;
  }

  /**
   * Reads a record from the DB.
   *
   * <p> This uses the information in
   * {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBFile} to read a record
   * from the database.
   *
   * @param existsCheck <code>true</code> if this is an existence check.
   * @throws IOException
   */
  private void readRecord(boolean existsCheck) throws IOException {
    this.nextBytePos = 0;
    this.myDBItem  = null;
    this.fileBytes   = null;

    try {
      // If we have a filename but no ID yet, call readSpecificRecordData()
      // which will fill in the record ID
      if ((this.myDBFile.getFilename() != null) &&
          (this.myDBFile.getRecordID() < 0))
        this.myDBFile.readSpecificRecord(this, this.myDBFile.getFilename(), -1);

      this.myDBItem  = MyDBUtil.readRecord(this.dbConnection, this.myDBFile.getTableName(), this.myDBFile.getRecordID());
      this.fileBytes = this.myDBItem.getFileBytes();

      if (!existsCheck)
        saveRecordID(); // Save last record ID read for this table
    } catch (Exception ex) {
      LexURIFile.debug(true, "MyDBInputStream.readRecord(" + existsCheck
                       + ")> Read/Peek exception. MyDBFile path["
                       + this.myDBFile.getPath() + "]", ex);
      throw new IOException(ex);
    }
  }

  /**
   * Saves record ID for the table specified in
   * {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBFile}.
   */
  private void saveRecordID() {
    long recId = this.myDBItem.getRecordID();
    if (recId >= 0) {
      String tableName = this.myDBFile.getTableName();
      lastRecordIDs.put(tableName, new Long(recId));
    }
  }

  /**
   * Deletes a specific record from the database based on the
   * <code>recordID</code>.
   *
   * @param recordID Record ID to delete.
   * @throws IOException
   */
  protected void deleteRecord(long recordID) throws IOException {
    LexURIFile.debug("MyDBInputStream.deleteRecord(" + recordID + ")");
    String tableName  = this.myDBFile.getTableName();

    Long recIdToDelete;
    if (recordID >= 0)
      recIdToDelete = recordID;
    else
      recIdToDelete = lastRecordIDs.get(tableName);

    if ((recIdToDelete != null) && (recIdToDelete.longValue() >= 0)) {
      LexURIFile.debug("MyDBInputStream.deleteRecord> Record ID to delete=" + recIdToDelete.longValue());

      try {
        checkDBOpen();
        MyDBUtil.deleteRecord(this.dbConnection, this.myDBFile.getTableName(), recIdToDelete);

        // Delete last saved record ID for this table if it matches what
        // was just deleted.
        Long lastRecId = lastRecordIDs.get(tableName);
        LexURIFile.debug("MyDBInputStream.deleteRecord> last Record ID="
                         + lastRecId);
        if ((lastRecId != null) && (lastRecId.longValue() == recordID))
          lastRecordIDs.remove(tableName);
      } catch (Exception ex) {
        LexURIFile.debug(true, "MyDBInputStream.deleteRecord> Exception removing recordId '"
                         + recIdToDelete + "'. MyDBFile path["
                         + this.myDBFile.getPath() + "]", ex);
        throw new IOException("Failed to delete DB record from table", ex);
      }
    }
  }

  /**
   * Returns an array of {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBItem}
   * which contains the records matching the search criteria.
   *
   * @param srchFilename Filename to search for
   * @param srchRecordID Record ID to search for
   * @return An array of {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBItem}
   *         matching the search criteria.
   * @throws IOException
   */
  public MyDBItem[] listRecordItems(String srchFilename, long srchRecordID) throws IOException {
    if (!this.openAttempted)
      openDBConnection();

    if (this.dbConnection == null)
      throw new IOException("DB connection is not open");

    MyDBItem[] myDBItems = null;
    try {
      myDBItems = MyDBUtil.getDBItems(this.dbConnection, this.myDBFile.getTableName(), srchFilename, srchRecordID);
    } catch (Exception ex) {
      LexURIFile.debug(true, "MyDBInputStream.listRecordItems> MyDBUtil.getDBItems("
              + srchFilename + "," + srchRecordID + ") exception. "
              + "MyDBFile path[" + this.myDBFile.getPath() + "]", ex);
      throw new IOException("Failed to list DB items", ex);
    }
    return myDBItems;
  }

}
