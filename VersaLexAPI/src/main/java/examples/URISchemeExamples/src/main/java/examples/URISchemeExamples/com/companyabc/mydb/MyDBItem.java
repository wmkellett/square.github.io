package examples.URISchemeExamples.com.companyabc.mydb;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class to store information regarding a single record in the database table.
 */
public class MyDBItem {
  private long   recordID;
  private String filename;
  private long   dateTimeMS;
  private long   fileLength;
  private byte[] fileBytes;

  /**
   * Constructor used to create a <code>MyDBItem</code> object containing
   * data from a database table record.
   *
   * <p> <b>Note:</b> In certain cases, such as a directory listing, this
   * constructor should be called with a <code>null</code> for the
   * <code>fileBytes</code> parameter as we do not want to store the contents
   * of all the files matching the directory listing.
   *
   * @param recordID   Record ID from database table.
   * @param filename   Name of file
   * @param dateTimeMS Date/Time in MS of the file
   * @param fileLength Length of file in bytes
   * @param fileBytes  Byte array containing the data
   */
  public MyDBItem(long recordID, String filename, long dateTimeMS,
                  long fileLength, byte[] fileBytes) {
    this.recordID   = recordID;
    this.filename   = filename;
    this.fileLength = fileLength;
    this.fileBytes  = (fileBytes != null) ? fileBytes : new byte[0];
    this.dateTimeMS = dateTimeMS;
  }

  /**
   * Return the record ID
   * @return record ID of the database record
   */
  public long getRecordID() {
    return this.recordID;
  }

  /**
   * Returns the filename
   * @return filename from the DB record
   */
  public String getFilename() {
    return this.filename;
  }

  /**
   * Returns the file length
   * @return length of the file stored in the database record
   */
  public long getFileLength() {
    return this.fileLength;
  }

  /**
   * Returns a byte array containing the file's bytes or possibly a 0-length
   * byte array if this object is part of a directory listing.
   * @return Bytes from database file
   */
  public byte[] getFileBytes() {
    return this.fileBytes;
  }

  /**
   * Returns the date/time of the file in MS
   * @return date/time of the file in MS
   */
  public long getDateTimeMS() {
    return this.dateTimeMS;
  }

  /**
   * Returns a string representation of this object used for debugging.
   * @return String representing this object
   */
  @Override
  public String toString() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
    String dateTimeStr = sdf.format(new Date(this.dateTimeMS));
    return "ID=" + this.recordID
            + " Filename=" + this.filename
            + " Length=" + this.fileLength
            + " BlobSize=" + this.fileBytes.length
            + " DateTime=" + dateTimeStr;
  }
}
