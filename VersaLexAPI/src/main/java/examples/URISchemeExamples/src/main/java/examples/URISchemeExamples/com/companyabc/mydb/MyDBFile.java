package examples.URISchemeExamples.com.companyabc.mydb;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import com.cleo.lexicom.beans.LexFileType;
import com.cleo.lexicom.beans.LexURIFile;
import com.cleo.lexicom.beans.LexURIFileFilter;
import com.cleo.lexicom.beans.LexWildcardInfo;

/**
 * MyDBFile is a VLTrader URI File scheme used to read/write files to a MYSQL
 * database.
 *
 * <p> <b>NOTES:</b>
 * <ul><li>This is a <b>sample</b> implementation of a <i>Scheme</i>File class.</li>
 *     <li>All implementing <i>Scheme</i>File classes <b>MUST</b>
 *           extend <code>com.cleo.lexicom.beans.LexURIFile</code>.</li></ul>
 *
 * The basic format of the MyDB URI is:
 *
 * <ul>
 * <li> MyDB:<i>dbTableName</i>?<i>param1</i>=<i>value1</i>&amp;<i>param2</i>=<i>value2</i>&amp;<i>param3</i>=...
 * </ul>
 *
 * <p> Examples:
 * <ul>
 * <li> MyDB:MyDBInboxTable
 * <li> MyDB:MyDBOutboxTable?Filename=test.edi
 * </ul>
 *
 * Parameters specified in the URI include the following:
 * <ul>
 * <li> filename=<i>fname</i>
 *      <ul> <li> This parameter is used to specify the filename for a PUT,
 *                GET, PUT+GET, or LCOPY command. When used as the source for
 *                a command, the filename can be a wildcard or regular
 *                expression. </li> </ul>
 * <li> ID=<i>recordID</i>
 *      <ul> <li> This parameter is used to specify a specific DB table record
 *                ID. This parameter cannot be a wildcard or regular expression.
 *                This parameter is not typically used during normal operation.
 *                It can be used to retrieve a specific record out of the
 *                table. </li> </ul>
 * </ul>
 *
 * Parameters that may automatically be added by VLTrader include the
 * following:
 * <ul>
 * <li> filename=<i>fname</i>
 *      <ul> <li> This parameter is used to specify the filename. </li> </ul>
 * <li> ID=<i>recordID</i>
 *      <ul> <li> This parameter is used to specify a specific DB table record
 *                ID. </li> </ul>
 * <li> length=<i>fileLength</i>
 *      <ul> <li> This is the length of the file. </li> </ul>
 * <li> dateTime=<i>fileTime</i>
 *      <ul> <li> This is the timestamp of the DB record. The format of
 *                recordTime will be YYYYMMDD-HHMMSS-SSS. </li> </ul>
 * </ul>
 *
 * <p> Four system properties are used to configure this scheme. The
 * property name is all lowercase including the scheme name. The property
 * values are mixed case.
 *
 * <table border="1">
 *   <tr>
 *     <th>System Property</th>
 *     <th>Required</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td>cleo.uri.mydb.file</td>
 *     <td>Yes</td>
 *     <td>examples.URISchemeExamples.com.companyabc.mydb.MyDBFile</td>
 *   </tr>
 *   <tr>
 *     <td>cleo.uri.mydb.inputstream</td>
 *     <td>Yes</td>
 *     <td>examples.URISchemeExamples.com.companyabc.mydb.MyDBInputStream</td>
 *   </tr>
 *   <tr>
 *     <td>cleo.uri.mydb.outputstream</td>
 *     <td>Yes</td>
 *     <td>examples.URISchemeExamples.com.companyabc.mydb.MyDBOutputStream</td>
 *   </tr>
 *   <tr>
 *     <td>cleo.uri.mydb.classpath</td>
 *     <td>No</td>
 *     <td> This property defines the Java class path to the MyDBURI.jar which
 *          contains the scheme implementation.</td>
 *   </tr>
 * </table>
 *
 * <br> The SQL for creating the tables in MySQL used with the MyDB scheme
 *      is as follows:
 * <pre>
 * {@code
 * delimiter $$
 * CREATE DATABASE `mysqlvltrader` $$
 * CREATE TABLE `mydbinbox` (
 *   `ID` bigint(20) NOT NULL AUTO_INCREMENT,
 *   `FileTime` bigint(20) NOT NULL,
 *   `Filename` varchar(255) NOT NULL,
 *   `FileLength` bigint(20) NOT NULL,
 *   `FileContents` longblob NOT NULL,
 *   PRIMARY KEY (`ID`),
 *   UNIQUE KEY `idDBTestInboxTable_UNIQUE` (`ID`)
 * ) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=latin1$$
 * CREATE TABLE `mydboutbox` (
 *   `ID` bigint(20) NOT NULL AUTO_INCREMENT,
 *   `FileTime` bigint(20) NOT NULL,
 *   `Filename` varchar(255) NOT NULL,
 *   `FileLength` bigint(20) NOT NULL,
 *   `FileContents` longblob NOT NULL,
 *   PRIMARY KEY (`ID`),
 *   UNIQUE KEY `ID_UNIQUE` (`ID`)
 * ) ENGINE=InnoDB AUTO_INCREMENT=106 DEFAULT CHARSET=latin1$$
 * }
 * </pre>
 *
 * <br> Database connection information used:
 * <ul>
 * <li>DRIVER: com.mysql.jdbc.Driver
 *     <ul><li>The file, <b>mysql-connector-java-5.1.14-bin.jar</b>, should
 *             be placed in the <i>VLTraderInstallFolder</i>/lib/ext/ folder.
 *     </li></ul></li>
 * <li>URL: jdbc:mysql://localhost:3306/mysqlvltrader</li>
 * <li>USERNAME: root</li>
 * <li>PW: mysqlroot</li>
 * </ul>
 *
 * Sample Usages:
 * <br> If the host-level Inbox/Outbox are specified as MyDB tables:
 * <ul> <li> Inbox:
 *           <ul> <li>MyDB:MyDBInbox</li></ul>
 *      </li>
 *      <li> Outbox:
 *           <ul> <li>MyDB:MyDBOutbox</li></ul>
 *      </li> </ul>
 *
 * Sample commands:
 * <ul>
 * <li> PUT -DEL *
 *      <ul><li>Sends all records in the MyDBOutbox table and deletes them
 *              after successful send</li></ul>
 * </li>
 * <li> PUT -DEL test.edi
 *      <ul><li>Sends the first record in the MyDBOutbox table with the filename
 *              of 'test.edi' and deletes it after successful send</li></ul>
 * </li>
 * <li> PUT -DEL [test.edi]
 *      <ul><li>Sends all records in the MyDBOutbox table with the filename
 *              matching the regular expression [test.edi]. If there are
 *              multiple 'test.edi' filenames in the table, each of them will
 *              be sent and deleted after successful send.</li></ul>
 * </li>
 * <li> GET *
 *      <ul><li>Retrieves all remote files and stores them in the MyDBInbox
 *              table.</li></ul>
 * </li>
 * <li> LCOPY -DEL * C:\SomeDir\
 *      <ul><li>Copies all records from the MyDBInbox table to C:\SomeDir\ and
 *              deletes them from the table.</li></ul>
 * </li> </ul>
 *
 * In the case of unsolicited incoming files, the files will automatically be
 * added as records in the MyDBInbox table.
 *
 * <br> If the host-level Inbox/Outbox are specified as folders on the local
 * file system:
 *
 * <ul> <li> Inbox:
 *           <ul> <li>Inbox/</li></ul>
 *      </li>
 *      <li> Outbox:
 *           <ul> <li>Outbox/</li></ul>
 *      </li> </ul>
 *
 * Then you can still use the MSMQ queue within the action commands.
 *
 * <br> Sample commands:
 * <ul>
 * <li> PUT -DEL "MyDB:MyDBOutbox?filename=*"
 *      <ul><li>Sends all records in the MyDBOutbox table and deletes them after
 *              successful send</li></ul>
 * </li>
 * <li> PUT -DEL "MyDB:MyDBOutbox?filename=test.edi"
 *      <ul><li>Sends the first record in the MyDBOutbox table with the
 *              filename 'test.edi' and deletes it after successful send.</li></ul>
 * </li>
 * <li> PUT -DEL "MyDB:MyDBOutbox?filename=[test.edi]"
 *      <ul><li>Sends all records in the MyDBOutbox table with filenames
 *              matching the regular expression [test.edi]. If there are
 *              multiple 'test.edi' filenames in the table, each of them will
 *              be sent and deleted after successful send.</li></ul>
 * </li>
 * <li> GET * "MyDB:MyDBInbox"
 *      <ul><li>Retrieves all remote files and stores them in MyDBInbox
 *              table.</li></ul>
 * </li>
 * <li> LCOPY -DEL "MyDB:MyDBInbox" C:\SomeDir\
 *      <ul><li>Copies all records from the MyDBInbox table to C:\SomeDir\
 *              and deletes them from the table.</li></ul>
 * </li>
 * </ul>
 */
public class MyDBFile extends LexURIFile {
  // Static strings for scheme name and parameters
  private final static String MYDB_SCHEME    = "mydb:";
  private final static String PARAM_FILENAME = "filename";
  private final static String PARAM_ID       = "ID";
  private final static String PARAM_DATETIME = "dateTime";
  private final static String PARAM_LENGTH   = "length";

  // Attributes
  private String  originalURI   = null;
  private String  tableName     = null;
  private String  filename      = null;
  private long    recordID      = -1;
  private boolean exists        = false;
  private long    length        = -1;
  private long    fileDateTime  = 0;
  private long    objCreateTime = System.currentTimeMillis();
  private URISyntaxException uriSyntaxException = null;

  SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");

//==============================================================================
// Object Constructors
//==============================================================================
  /**
   * Constructor with entire URI.
   *
   * <p> <b>NOTE:</b> The first line should always be <code>super(uri);</code>
   *
   * @param uri URI for MyDBFile
   */
  public MyDBFile(String uri) {
    super(uri);
    this.originalURI = uri;
    LexURIFile.debug("MyDBFile(" + uri + ")");
    parseURI(uri);
  }

  /**
   * Constructor for a MyDBFile where the filename is passed in as the child.
   *
   * <p> <b>NOTE:</b> The first line should always be
   *      <code>super(parentUri, child);</code>
   *
   * @param parentUri Parent URI not including the filename. This would include
   *                  the table name.
   * @param child     Filename
   */
  public MyDBFile(String parentUri, String child) {
    super(parentUri, child);
    LexURIFile.debug("MyDBFile(" + parentUri + "," + child + ")");
    this.originalURI = MyDBFile.buildURIString(parentUri, child);
    parseURI(originalURI);
  }

//==============================================================================
// File methods that MUST be overriden
//==============================================================================
  /**
   * Returns <code>true</code> if the MyDBFile can be read.
   *
   * @return <code>true</code> if the table named in MyDBFile exists.
   */
  @Override
  public boolean canRead() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    return this.tableExists();  // Assume we can read if the DB table exists
  }

  /**
   * Returns <code>true</code> if the MyDBFile can be written.
   *
   * @return <code>true</code> if the table named in MyDBFile exists.
   */
  @Override
  public boolean canWrite() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    return this.tableExists(); // Assume we can write if the DB table exists
  }

  /**
   * Compares the URIs of two LexURIFiles lexicographically.
   *
   * @param pathname The <code>LexURIFile</code> to be compared to this
   *                 <code>MyDBFile</code>.
   * @return Zero if the argument is equal to this <code>MyDBFile</code>, a
   *         value less than zero if this <code>MyDBFile</code>'s URI is
   *         lexicographically less than the argument, or a value greater than
   *         zero if this <code>MyDBFile</code>'s URI is lexicographically
   *         greater than the argument.
   */
  @Override
  public int compareTo(LexURIFile pathname) {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    if (pathname instanceof MyDBFile)
      return this.originalURI.compareToIgnoreCase(((MyDBFile)pathname).originalURI);
    else
      throw new SecurityException("Invalid parameter passed to MyDBFile.compareTo()");
  }

  /**
   * Throws an exception as this is not supported.
   *
   * @return No return as an IOException is always thrown.
   * @throws IOException
   */
  @Override
  public boolean createNewFile() throws IOException {
    throw new IOException("MyDBFile.createNewFile() not supported");
  }

  /**
   * Deletes a file (a database record). Directory (database table) deletion
   * is not supported.
   * @return <code>true</code> on a successful delete of a file. Always
   *         returns <code>true</code> if called with a directory URI
   */
  @Override
  public boolean delete() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    if (this.isDirectory())
      return true; // Do nothing for directories
    else
      return deleteRecord(this.recordID);
  }

  /**
   * Compares to see if passed-in object is the same object as <code>this</code>.
   *
   * @param obj Object to compare
   * @return <true> if the pass-in object is the same object as
   *         <code>this</code>; <code>false</code> otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if ((obj == null) || (obj.getClass() != MyDBFile.class))
      return false;
    return (obj == this);
  }

  /**
   * Checks if the specified <code>MyDBFile</code> URI exists.
   * @return <code>true</code> if the <code>MyDBFile</code> exists
   */
  @Override
  public boolean exists() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    if (!this.exists) {
      if ((this.filename != null) || (this.recordID >= 0))
        readSpecificRecord(this.filename, this.recordID);
      else
        this.exists = this.tableExists();
    }

    return this.exists;
  }

  /**
   * Returns a new <code>MyDBFile</code> object using the original URI as
   * all URI paths are absolute.
   *
   * @return A new <code>MyDBFile</code> object with the same URI as
   *         <code>this</code>.
   */
  @Override
  public MyDBFile getAbsoluteFile() {
    return new MyDBFile(this.originalURI);
  }

  /**
   * Returns the original URI used when creating this object as all URI paths
   * are absolute.
   * @return The original URI used to create this object.
   */
  @Override
  public String getAbsolutePath() {
    return this.originalURI;
  }

  /**
   * Returns the same result as <code>new MyDBFile(this.{@link #getPath()})</code>.
   *
   * @return A new <code>MyDBFile</code> object matching the original URI.
   */
  @Override
  public MyDBFile getCanonicalFile() {
    String path = this.getPath();
    if ((path != null) && (path.length() > 0))
      return new MyDBFile(path);
    else
      return null;
  }

  /**
   * Returns the same URI as if <code>this.{@link #getPath()}</code> were called.
   *
   * @return The original URI.
   * @throws IOException
   */
  @Override
  public String getCanonicalPath() throws IOException {
    return this.getPath();
  }

  /**
   * Returns the filename portion of the URI. This should be a valid filename
   * that could be stored on a Windows or Unix file system.
   * @return The filename represented in the URI
   */
  @Override
  public String getName() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);

    if (this.filename != null) {
      String name = LexURIFile.sanitizeFilename(this.filename.trim());
      if (name.length() > 0)
        return name.trim();
    }

    // If no filename found, make one up.
    if (this.recordID >= 0)
      return "MyDBFileRec_" + recordID;
    else
      return "MyDBFile";
  }

  /**
   * Returns the parent of the directory or file.
   *
   * <ul> <li> If the <code>MyDBFile</code> represents a file, then the URI
   *           without any file specific parameters is returned.
   *      <li> If the <code>MyDBFile</code> represents a folder, then a
   *           <code>null</code> is returned.
   *
   * <p> Examples:
   * <ul>
   * <li> MyDB:MyDBOutbox?Filename=test.edi
   * <br> <code>getParent()</code> would return <b>MyDB:MyDBOutbox</b>
   * <p>
   * <li> MyDB:MyDBOutbox
   * <br> <code>getParent()</code> would return <code>null</code>
   * <p>
   * <li> MyDB:MyDBOutbox?Filename=[test.edi]
   * <br> <code>getParent()</code> would return <b>MyDB:MyDBOutbox</b>
   * </ul>
   *
   * @return Returns the name of the parent represented by this
   *         <code>MyDBFile</code>.
   */
  @Override
  public String getParent() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);

    if (isFile()) {
      StringBuilder sb = new StringBuilder(MYDB_SCHEME);
      sb.append(this.tableName);
      return sb.toString();
    } else
      return null; // No parent
  }

  /**
   * Returns the <code>MyDBFile</code> object of this <code>MyDBFile</code>'s
   * parent, or <code>null</code> if this pathname does not name a parent
   * directory.
   *
   * <p> If this <code>MyDBFile</code> has a parent, then this is equivalent to
   * <code>new&nbsp;MyDBFile(this.{@link #getParent})</code>.
   *
   * @return  The <code>MyDBFile</code> object of the parent directory named
   *          by this <code>MyDBFile</code>, or <code>null</code> if this
   *          <code>MyDBFile</code> does not name a parent
   */
  @Override
  public MyDBFile getParentFile() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);

    String parentURI = getParent();
    if (parentURI == null)
      return null;
    else
      return new MyDBFile(parentURI);
  }

  /**
   * Returns the original URI string for the <code>MyDBFile</code>.
   *
   * @return Returns the URI string for the <code>MyDBFile</code> object
   */
  @Override
  public String getPath() {
    return this.originalURI;
  }

  /**
   * Returns <code>true</code> as all URI paths are absolute.
   * @return <code>true</code>
   */
  @Override
  public boolean isAbsolute() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    else
      return true;
  }

  /**
   * Returns <code>true</code> if the URI represented by this
   * <code>MyDBFile</code> is a directory (database table).
   *
   * @return <code>true</code> if <code>MyDBFile</code> represents a directory.
   */
  @Override
  public boolean isDirectory() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    else
      return ((this.filename == null) && (this.recordID < 0));
  }

  /**
   * Returns <code>true</code> if the URI represented by this
   * <code>MyDBFile</code> is a file (database record).
   *
   * @return <code>true</code> if <code>MyDBFile</code> represents a file.
   */
  @Override
  public boolean isFile() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    else
      return ((this.filename != null) || (this.recordID >= 0));
  }

  /**
   * Returns <code>false</code> as no records are considered hidden.
   * @return <code>false</code>
   */
  @Override
  public boolean isHidden() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    else
      return false;
  }

  /**
   * Returns the last modified time of the file. The Date/Time representing
   * the last modified time of the file is stored in the database record.
   * @return Last modified time of the file.
   */
  @Override
  public long lastModified() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    else if (this.fileDateTime != 0)
      return this.fileDateTime;
    else
      return objCreateTime;
  }

  /**
   * Returns the length of the file represented by this <code>MyDBFile</code>.
   * @return The length, in bytes, of the file represented by this
   *         <i>Scheme</i>File, or 0L if the file does not exist.
   */
  @Override
  public long length() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    else if (!this.exists) {
      if ((this.filename != null) || (this.recordID >= 0))
        readSpecificRecord(this.filename, this.recordID);
      else
        readNextRecordData();
    }

    return (this.length < 0) ? 0 : this.length;
  }

  /**
   * Returns an array of strings naming the files and directories in the
   * directory represented by this <code>MyDBFile</code>.
   *
   * <p> An array of strings is returned, one for each file or in the directory.
   * Each string is a complete path including the file name. The URI should
   * also include any additional information uniquely identifying the item such
   * as the record ID. This is because the filename is not necessarily unique
   * in the database table.
   *
   * <p> For example:
   * <br> If the list() was called for a URI of
   *      <b>MyDB:MyDBOutbox</b>
   * <br> The following is an example of return values:
   * <ul>
   * <li> MyDB:MyDBOutbox?filename=test.edi&ID=7893370&length=1533&dateTime=20120315-160240-023
   * <li> MyDB:MyDBOutbox?filename=abc.edi&ID=7893376&length=1533&dateTime=20120315-160240-118
   * <li> MyDB:MyDBOutbox?filename=test100K.edi&ID=7893361&length=102400&dateTime=20120315-427
   * <li> MyDB:MyDBOutbox?filename=test100K.edi&ID=7893364&length=102400&dateTime=20120315-100
   * </ul>
   *
   * <p> There is no guarantee that the name strings in the resulting array
   *     will appear in any specific order; they are not, in particular,
   *     guaranteed to appear in alphabetical order.
   *
   * @return An array of strings naming the files and directories in the
   *         directory denoted by this <code>MyDBFile</code>. The array will be
   *         empty if the directory is empty. Returns null if this
   *         <code>MyDBFile</code> does not represent a directory, or if an I/O
   *         error occurs.
   */
  @Override
  public String[] list() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);

    // Get a list of all items in the table
    MyDBItem[] dbItemData = getDBTableItemList(null/*dbTestInputStream*/,
                                               null/*srchFilename*/,
                                               -1/*srchRecordID*/);
    if (dbItemData == null)
      return null;

    // Convert each dbItemData[ii] into a URI string and store in the String
    // array.
    String[] fileList = new String[dbItemData.length];
    for (int ii = 0; ii < dbItemData.length; ii++)
      fileList[ii] = buildURIString(dbItemData[ii]);

    return fileList;
  }

  /**
   * Returns an array of <code>MyDBFile</code> objects in the directory denoted by
   * this <code>MyDBFile</code>.
   *
   * <p> An array of <code>MyDBFile</code> objects is returned, one for each
   * file in the directory.
   *
   * <p> There is no guarantee that the name strings in the resulting array
   * will appear in any specific order; they are not, in particular,
   * guaranteed to appear in alphabetical order.
   *
   * @return An array of <code>MyDBFile</code>s denoting the files and
   *         directories in the directory denoted by this <code>MyDBFile</code>.
   *         The array will be empty if the directory is empty.
   *         Returns <code>null</code> if this <code>MyDBFile</code> does not
   *         denote a directory, or if an I/O error occurs.
   */
  @Override
  public MyDBFile[] listFiles() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);

    // Call this.list() to get an array of URIs representing the records
    // in the database table.
    String[] uriFileList = this.list();
    if (uriFileList == null)
      return null;

    // Convert the list into MyDBFiles
    ArrayList<MyDBFile> fileList = new ArrayList<MyDBFile>();
    for (int ii = 0; ii < uriFileList.length; ii++) {
      try {
        MyDBFile dbTestFile = new MyDBFile(uriFileList[ii]);
        fileList.add(dbTestFile);
      } catch (Exception ex) {
        LexURIFile.debug(true, "MyDBFile.listFiles() exception. "
                         + "uriFileList[" + ii + " of " + uriFileList.length
                         + "]='" + uriFileList[ii] + "'", ex);
      }
    }

    // Return the array of MyDBFiles
    return fileList.toArray(new MyDBFile[fileList.size()]);
  }

  /**
   * Returns an array of <code>MyDBFile</code> objects filtered by the
   * LexURIFileFilter.
   *
   * @param filter A LexURIFileFilter implemented with VLTrader. The
   *               <code>MyDBFile</code> implementation should call
   *               filter.accept(lexURIFile) method to determine if this
   *               <code>MyDBFile</code> should be included in the return array.
   * @return Returns an array of <code>MyDBFile</code> objects filtered by
   *         <code>filter</code>.
   */
  @Override
  public MyDBFile[] listFiles(LexURIFileFilter filter) {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);

    // Get a list of all files in the directory (database table)
    String[] uriFileList = this.list();
    if (uriFileList == null)
      return null;

    // Loop through the list of files
    ArrayList<MyDBFile> fileList = new ArrayList<MyDBFile>();
    for (int ii = 0; ii < uriFileList.length; ii++) {
      try {
        // Create a MyDBFile
        MyDBFile myDBFile = new MyDBFile(uriFileList[ii]);
        // If the filter exists and accepts the file, then add it to the list
        if ((filter == null) || (filter.accept(myDBFile)))
          fileList.add(myDBFile);
      } catch (Exception ex) {
        LexURIFile.debug(true, "MyDBFile.listFiles(LexURIFileFilter) exception. "
                         + "uriFileList[" + ii + " of " + uriFileList.length
                         + "]='" + uriFileList[ii] + "'", ex);
      }
    }

    // Return the filtered array of MyDBFiles
    return fileList.toArray(new MyDBFile[fileList.size()]);
  }

  /**
   * Does nothing and returns <code>true</code>.
   * @return <code>true</code>
   */
  @Override
  public boolean mkdir() {
    if (this.uriSyntaxException != null)
      return false;
    else
      return true; // Do nothing
  }

  /**
   * Does nothing and returns <code>true</code>.
   * @return <code>true</code>
   */
  @Override
  public boolean mkdirs() {
    if (this.uriSyntaxException != null)
      return false;
    else
      return true; // Do nothing
  }

  /**
   * Throws a SecurityException as renaming is not supported.
   * @param dest Destination MyDBFile containing new name
   * @return Nothing since a SecurityException is always thrown
   */
  @Override
  public boolean renameTo(LexURIFile dest) {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    else
      throw new SecurityException("MyDBFile.renameTo() not supported");
  }

  /**
   * Throws a SecurityException as <code>setLastModified</code> is not supported.
   * @param time New last modified time.
   * @return Nothing since a SecurityException is always thrown
   */
  @Override
  public boolean setLastModified(long time) {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    else
      throw new SecurityException("MyDBFile.setLastModified() not supported");
  }

  /**
   * Throws a SecurityException as <code>setReadable(readable)</code> is not
   * supported.
   * @param readable If <code>true</code>, sets the access permission to
   *                 allow read operations; if <code>false</code> to disallow
   *                 read operations
   * @return Nothing since a SecurityException is always thrown
   */
  @Override
  public boolean setReadable(boolean readable) {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    else
      throw new SecurityException("MyDBFile.setReadable() not supported");
  }

  /**
   * Throws a SecurityException as <code>setReadable(readable, ownerOnly)</code>
   * is not supported.
   * @param readable  If <code>true</code>, sets the access permission to allow
   *                  read operations; if <code>false</code> to disallow read
   *                  operations
   * @param ownerOnly If <code>true</code>, the read permission applies only to
   *                  the owner's read permission; otherwise, it applies to
   *                  everybody.  If the underlying file system can not
   *                  distinguish the owner's read permission from that of
   *                  others, then the permission will apply to everybody,
   *                  regardless of this value.
   * @return Nothing since a SecurityException is always thrown
   */
  @Override
  public boolean setReadable(boolean readable, boolean ownerOnly) {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    else
      throw new SecurityException("MyDBFile.setReadable() not supported");
  }

  /**
   * Throws a SecurityException as <code>setReadOnly()</code> is not
   * supported.
   * @return Nothing since a SecurityException is always thrown
   */
  @Override
  public boolean setReadOnly() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);
    else
      throw new SecurityException("MyDBFile.setReadOnly() not supported");
  }

  /**
   * Returns a string representation of this <code>MyDBFile</code>.
   * @return The original URI
   */
  @Override
  public String toString() {
    return this.originalURI;
  }

//==============================================================================
// File methods to be OPTIONALLY overriden
//==============================================================================
  /**
   * Computes a hash code for this URI.
   * @return A hash code for this URI
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 71 * hash + (this.originalURI != null ? this.originalURI.hashCode() : 0);
    hash = 71 * hash + (int) (this.objCreateTime ^ (this.objCreateTime >>> 32));
    return hash;
  }

//==============================================================================
// Additional non-File methods that must be implemented
//==============================================================================
  /**
   * Returns the type of this <i>Scheme</i>File implementation.
   *
   * @return LexFileType.DATABASE
   */
  @Override
  public LexFileType getType() {
    return LexFileType.DATABASE;
  }

  /**
   * Returns an exception generated during the <code>MyDBFile</code> constructor.
   *
   * <p> If there is a problem with the URI, an exception should not be thrown
   * during the constructor. Instead the exception should be saved and
   * <b>getURIException()</b> should return this exception. In addition, if
   * any method is called after a constructor with a bad URI, the saved
   * exception should be thrown at the time the method is called.
   *
   * @return The exception generated during the <code>MyDBFile</code>
   * constructor. Otherwise a <code>null</code> should be returned.
   */
  @Override
public Exception getURIException() {
    return this.uriSyntaxException;
  }

  /**
   * Returns an object representing the wildcard information in the URI.
   *
   * <p> This should interrogate the URI and return a LexWildcardInfo object
   * created with LexWildcardInfo(String path, String wildcard). If there
   * is no wildcard ('*', '?', or '[regex]') in the filename portion of the
   * URI, then a <code>null</code> object should be returned. If a wildcard
   * exists in the filename portion of the URI, the <code>path</code> should be
   * a valid URI without the filename parameter. The <code>wildcard</code>
   * would be the value portion of the filename parameter.
   *
   * <p> Examples:
   * <ul>
   * <li> MyDB:MyDBOutbox?filename=test.edi
   * <br> This would return a <code>null</code> object as there is no wildcard.
   * <p>
   * <li> MyDB:MyDBOutbox?filename=*.edi
   * <br> This would return a new LexWildcardInfo( "MyDB:MyDBOutbox", "*.edi" ) object.
   * <p>
   * <li> MyDB:MyDBOutbox?filename=[test.edi]
   * <br> This would return a new LexWildcardInfo( "MyDB:MyDBOutbox", "[test.edi]" ) object.
   * </ul>
   *
   * @return Returns a <code>com.cleo.lexicom.beans.LexWildcardInfo</code>
   *         object if a wildcard is present or <code>null</code> otherwise.
   */
  @Override
  public LexWildcardInfo getLexWildcardInfo() {
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);

    if ((this.filename == null) || (this.filename.trim().length() == 0))
      return null;

    boolean hasWildcard = false;

    // First, look for a regex.  Regular expressions must follow these rules:
    // - enclosed in brackets
    // - closing bracket must be last character of filename string
    // - opening bracket must be first character of filename string
    int begIdx = this.filename.lastIndexOf('[');
    int endIdx = this.filename.lastIndexOf(']');
    if (begIdx == -1 || endIdx == -1)
      hasWildcard = false;
    else if (begIdx == 0 && endIdx == this.filename.length()-1)
      hasWildcard = true;

    if (!hasWildcard) {
      // If no regular expression is found, then look for traditional wildcards.
      if (this.filename.lastIndexOf('*') >= 0)
        hasWildcard = true;
      else if (this.filename.lastIndexOf('?') >= 0)
        hasWildcard = true;
    }

    if (hasWildcard) {
      String wildString = this.filename;
      String path = this.getParent();
      return new LexWildcardInfo(path, wildString);
    } else
      return null;
  }

//==============================================================================
// Helper methods
//==============================================================================
  /**
   * Returns the database table name portion of the URI.
   * @return The database table name
   */
  protected String getTableName() {
    return this.tableName;
  }

  /**
   * Returns the filename portion of the URI.
   * @return A filename from the URI
   */
  protected String getFilename() {
    return this.filename;
  }

  /**
   * Returns the database record ID.
   * @return The database record ID if it is known; -1 otherwise
   */
  protected long getRecordID() {
    return this.recordID;
  }

  /**
   * Parses the URI string.
   * @param uri URI string to parse
   * @throws NullPointerException
   */
  private void parseURI(String uri) throws NullPointerException {
    // If null is passed in, then throw this exception now.
    // Otherwise we save the exception and throw it when the MyDBFile object is used.
    if (uri == null)
      throw new NullPointerException("MyDB URI is null");

    // Make sure URI starts with "dbtest:"
    if (!uri.toLowerCase().startsWith(MYDB_SCHEME.toLowerCase())) {
      this.uriSyntaxException = new URISyntaxException(uri,
               "Invalid MyDB syntax-URI does not start with '"
               + MYDB_SCHEME + "'");
      LexURIFile.debug(true, this.uriSyntaxException.getMessage());
      return;
    }

    String tblName = uri.substring(MYDB_SCHEME.length());
    String params = null;
    int qmIdx = tblName.indexOf('?');
    if (qmIdx >= 0) {
      params = tblName.substring(qmIdx+1);
      tblName  = tblName.substring(0, qmIdx);
    }
    if (tblName.length() == 0) {
      this.uriSyntaxException = new URISyntaxException(uri, "Invalid MyDB syntax-Missing table name");
      LexURIFile.debug(true, this.uriSyntaxException.getMessage());
      return;
    }
    this.tableName = tblName;

    if (params != null) {
      StringTokenizer st = new StringTokenizer(params, "&");
      while (st.hasMoreTokens()) {
        String paramAndVal = st.nextToken();
        String param;
        String val;
        int equalsIdx = paramAndVal.indexOf("=");
        if (equalsIdx >= 0) {
          param = paramAndVal.substring(0, equalsIdx);
          val   = paramAndVal.substring(equalsIdx+1);
        } else {
          param = paramAndVal;
          val = null;
        }

        if (param.length() == 0)
          param = null;
        if ((val != null) && (val.length() == 0))
          val = null;

        if ((param != null) && (val == null)) {
          this.uriSyntaxException = new URISyntaxException(uri, "Parameter '" + param + "' does not have a value");
          LexURIFile.debug(true, this.uriSyntaxException.getMessage());
          return;
        } else if ((param == null) && (val != null)) {
          this.uriSyntaxException = new URISyntaxException(uri, "No parameter associated with value '" + val + "'");
          LexURIFile.debug(true, this.uriSyntaxException.getMessage());
          return;
        } else if ((param != null) && (val != null)) {
          //------- PARAM_FILENAME (Filename string) -------
          if (param.equalsIgnoreCase(PARAM_FILENAME)) {
            this.filename = val;

          //------- PARAM_DBID (Record ID converted to a string) -------
          } else if (param.equalsIgnoreCase(PARAM_ID)) {
            try {
              int recID = Integer.valueOf(val);
              this.recordID = recID;
            } catch (NumberFormatException nfe) {
              this.uriSyntaxException = new URISyntaxException(uri, "Value for parameter '" + param + "' is not a number");
              LexURIFile.debug(true, "MyDBFile.parseURI(" + uri + ") exception.", this.uriSyntaxException);
              return;
            }

          //------- PARAM_DATETIME (YYYYMMDD-HHMMSS-SSS) -------
          } else if (param.equalsIgnoreCase(PARAM_DATETIME)) {
            try {
              Date dateVal = sdf.parse(val);
              this.fileDateTime = dateVal.getTime();
            } catch (ParseException pe) {
              this.uriSyntaxException = new URISyntaxException(uri, "Parameter '" + param + "' does not have the correct format (YYYYMMDD-HHMMSS)");
              LexURIFile.debug(true, "MyDBFile.parseURI(" + uri + ") exception.", this.uriSyntaxException);
              return;
            }

          //------- PARAM_LENGTH (# of bytes) -------
          } else if (param.equalsIgnoreCase(PARAM_LENGTH)) {
            try {
              int lenVal = Integer.valueOf(val);
              this.length = lenVal;
            } catch (NumberFormatException nfe) {
              this.uriSyntaxException = new URISyntaxException(uri, "Value for parameter '" + param + "' is not a number");
              LexURIFile.debug(true, "MyDBFile.parseURI(" + uri + ") exception.", this.uriSyntaxException);
              return;
            }

          //------- Unrecognized parameter -------
          } else {
            this.uriSyntaxException = new URISyntaxException(uri, "Parameter '" + param + "' not recognized");
            LexURIFile.debug(true, this.uriSyntaxException.getMessage());
            return;
          }
        }
      }
    }
  }

  /**
   * Builds a URI string from the <code>parent</code> and <code>child</code>.
   *
   * <p> This also supports the case where multiple parameters are passed in
   * for the <code>child</code>.
   *
   * @param parent Parent URI which should include scheme and table name
   * @param child  Child should be a filename
   * @return A URI string with the child (filename) parameter added to the URI.
   */
  private static String buildURIString(String parent, String child) {
    // Set parent to null if it is an empty string
    if ((parent != null) && (parent.trim().length() == 0))
      parent = null;
    // Set child to null if it is an empty string
    if ((child == null) || (child.trim().length() == 0))
      child = null;
    // If neither parent nor child specified, return a null
    if ((parent == null) && (child == null))
      return null;

    StringBuilder sb = new StringBuilder();
    if ((parent != null) && (child == null))
      sb.append(parent);
    else if ((parent == null) && (child != null))
      sb.append(child);
    else {
      sb.append(parent);
      if (child.startsWith("&") || child.startsWith("?"))
        child = child.substring(1);
      StringTokenizer st = new StringTokenizer(child, "&");
      while (st.hasMoreTokens()) {
        String param = st.nextToken();
        if ((param != null) && (param.trim().length() > 0)) {
          if (param.indexOf("=") > 0)
            addParameter(sb, param);
          else
            addParameter(sb, PARAM_FILENAME + "=" + param);
        }
      }
    }

    return sb.toString();
  }

  /**
   * Builds a URI string from a
   * {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBItem} object that
   * contains the ID, file size, and file date/time.
   *
   * @param dbItem Item to be converted to a URI
   * @return A URI string representing the
   *         {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBItem} object
   */
  private String buildURIString(MyDBItem dbItem) {
    StringBuilder uri = new StringBuilder(MYDB_SCHEME);
    uri.append(this.tableName);

    String fn = dbItem.getFilename();
    if ((fn != null) && (fn.trim().length() > 0))
      addParameter(uri, PARAM_FILENAME + "=" + fn.trim());
    addParameter(uri, PARAM_ID + "=" + dbItem.getRecordID());
    if (dbItem.getFileLength() >= 0)
      addParameter(uri, PARAM_LENGTH + "=" + String.valueOf(dbItem.getFileLength()));
    if (dbItem.getDateTimeMS() > 0) {
      // Time format: YYYYMMDD-HHMMSS-SSS
      String dateTimeStr = sdf.format(new Date(dbItem.getDateTimeMS()));
      addParameter(uri, PARAM_DATETIME + "=" + dateTimeStr);
    }

    return uri.toString();
  }

  /**
   * Added a single parameter to the string. It determines whether this is
   * the first parameter and '?' should be used or if this is a subsequent
   * parameter in which a '&' should be used.
   *
   * @param sb    URI string to be appended to
   * @param child Parameter to append
   */
  private static void addParameter(StringBuilder sb, String child) {
    if ((child == null) || (child.length() == 0))
      return;
    if (child.startsWith("?") || child.startsWith("&"))
      child = child.substring(1);
    if (sb.indexOf("?") > 0)
      sb.append("&");
    else
      sb.append("?");
    sb.append(child);
  }

  /**
   * Deletes a record from the database table based on the record ID.
   *
   * @param recordID Record ID in table to delete
   * @return <code>true</code> if the record was deleted
   */
  private boolean deleteRecord(long recordID) {
    MyDBInputStream dbTestInputStream = null;
    boolean retVal = false;
    try {
      dbTestInputStream = new MyDBInputStream(this);
      dbTestInputStream.deleteRecord(recordID);
      retVal = true;
    } catch (Exception ex) {
      LexURIFile.debug(true, "MyDBFile.deleteRecord(" + recordID + ") delete exception.", ex);
      throw new SecurityException(ex);
    } finally {
      try {
        if (dbTestInputStream != null)
          dbTestInputStream.close();
      } catch (Exception ex) {
        LexURIFile.debug(true, "MyDBFile.deleteRecord(" + recordID + ") close exception.", ex);
      }
    }
    return retVal;
  }

  /**
   * Reads the next record out of the database table
   */
  private void readNextRecordData() {
    MyDBInputStream dbTestInputStream = null;
    this.exists = false;
    this.length = -1;

    try {
      dbTestInputStream = new MyDBInputStream(this);
      this.length = dbTestInputStream.getNextRecordLen();
      this.exists = true;
    } catch (Exception ex) {
      // Do nothing as we are just checking if a message exists
    } finally {
      try {
        if (dbTestInputStream != null)
          dbTestInputStream.close();
      } catch (Exception ex) {
      }
    }
    LexURIFile.debug("MyDBFile.readNextRecordData> exists=" + this.exists
                     + " length=" + this.length);
  }

  /**
   * Reads a specific record out of the database based on the search parameters
   * provided.
   *
   * @param srchFilename Filename to search for or <code>null</code> if none
   * @param srchRecordID DB table record ID to search for or -1 if none
   * @return  <code>true</code> if the record exists and could be read
   */
  private boolean readSpecificRecord(String srchFilename, long srchRecordID) {
    return readSpecificRecord(null/*dbTestInputStream*/, srchFilename, srchRecordID);
  }

  /**
   * Reads a specific record out of the database based on the search parameters
   * provided.
   *
   * @param myDBInputStream MyDBInputStream to use when reading the record
   * @param srchFilename    Filename to search for or <code>null</code> if
   *                        none
   * @param srchRecordID    DB table record ID to search for or -1 if none
   * @return <code>true</code> if the record exists and was read
   */
  protected boolean readSpecificRecord(MyDBInputStream myDBInputStream,
                                       String srchFilename, long srchRecordID) {
    boolean found = false;
    LexURIFile.debug("MyDBFile.readSpecificRecordData>"
                     + " srchFilename=" + srchFilename
                     + " srchRecordID=" + srchRecordID);
    if ((srchFilename != null) || (srchRecordID >= 0)) {
      MyDBItem[] dbItemData = getDBTableItemList(myDBInputStream, srchFilename, srchRecordID);
      if ((dbItemData != null) && (dbItemData.length > 0)) {
        LexURIFile.debug("MyDBFile.readSpecificRecordData> FOUND-->" + dbItemData[0].toString());
        // Get data for first matching item
        this.filename     = dbItemData[0].getFilename();
        this.recordID     = dbItemData[0].getRecordID();
        this.length       = dbItemData[0].getFileLength();
        this.fileDateTime = dbItemData[0].getDateTimeMS();
        this.exists = true;
        found = true;
      }
    }
    if (!found)
      LexURIFile.debug("MyDBFile.readSpecificMessageData> NOT found!");
    return found;
  }

  /**
   * Returns an array of {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBItem}
   * objects that match the search parameters.
   *
   * @param dbTestInputStream MyDBInputStream to use when search the records
   * @param srchFilename    Filename to search for or <code>null</code> if none
   * @param srchRecordID    DB table record ID to search for or -1 if none
   * @return An array of {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBItem}
   *        objects that match the search parameters or <code>null</code> if
   *        there was an exception.
   */
  private MyDBItem[] getDBTableItemList(MyDBInputStream dbTestInputStream,
                                        String srchFilename, long srchRecordID) {
    MyDBItem[] dbItemList = null;
    boolean closeInputStream = false;

    LexURIFile.debug("MyDBFile.getDBTableItemList> srchLabel=" + srchFilename +
                     " srchIdStr=" + srchRecordID);
    try {
      if (dbTestInputStream == null) {
        dbTestInputStream  = new MyDBInputStream(this);
        closeInputStream = true;
      }
      dbItemList = dbTestInputStream.listRecordItems(srchFilename, srchRecordID);
    } catch (Exception ex) {
      dbItemList = null;
      LexURIFile.debug(true, "MyDBFile.getDBTableItemList(dbTestInputStream," + srchFilename
                       + "," + srchRecordID + ") exception. MyDBFile path["
                       + this.getPath() + "]", ex);
    } finally {
      try {
        if (closeInputStream && (dbTestInputStream != null))
          dbTestInputStream.close();
      } catch (Exception ex) {
        LexURIFile.debug(true, "MyDBFile.getDBTableItemList(dbTestInputStream," + srchFilename
                         + "," + srchRecordID + ") close exception. MyDBFile path["
                         + this.getPath() + "]", ex);
      }
    }
    return dbItemList;
  }

  /**
   * Returns <code>true</code> if table specified in URI exists.
   * @return <code>true</code> if table specified in URI exists.
   */
  private boolean tableExists() {
    return MyDBUtil.dbTableExists(this.getTableName());
  }
}
