package examples.URISchemeExamples.com.companyabc.myfs;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.cleo.lexicom.beans.LexFileType;
import com.cleo.lexicom.beans.LexURIFile;
import com.cleo.lexicom.beans.LexURIFileFilter;
import com.cleo.lexicom.beans.LexWildcardInfo;
import com.cleo.lexicom.uri.HierarchicalURI;

/**
 * MyFS is a VLTrader URI File scheme used to read/write files to a
 * configurable file system. It allows for the addition of one "common"
 * folder to which all mailboxes have access.
 *
 * <p> This scheme is an example of a scheme that could be used for
 * the <b>Local FTP Users</b>. If the <b>Default Root Directory</b> is
 * configured as <b>MyFS:\</b>, then all FTP users will have access to a
 * "common" folder as well as their own folders. The base path for all FTP
 * users is defined by the system property <i>cleo.uri.myfs.basepath</i>.
 * The common folder shared by all users is defined by the system property
 * <i>cleo.uri.myfs.commonpath</i>.
 *
 * <p> It is largely implemented using the methods of
 * <a href="http://docs.oracle.com/javase/6/docs/api/java/io/File.html">java.io.File</a>
 * directly. This class does some filename manipulation to convert between
 * the <i>MyFS:</i> URI scheme and either the <i>base</i> or <i>common</i>
 * file system.
 *
 * <p> <b>NOTES:</b>
 * <ul><li>This is a <b>sample</b> implementation of a <i>Scheme</i>File class.</li>
 *     <li>All implementing <i>Scheme</i>File classes <b>MUST</b>
 *           extend <code>com.cleo.lexicom.beans.LexURIFile</code>.</li></ul>
 *
 * The basic format of the MyFS URI is:
 *
 * <ul>
 * <li> MyFS:\mailbox\subdir1\subdir2\filename
 * </ul>
 *
 * <p> Examples:
 * <ul>
 * <li> MyFS:\User1\inbox
 * <li> MyFS:\User1\outbox\payload
 * <li> MyFS:\User1\outbox\payload\test.edi
 * <li> MyFS:\User1\common\subdir\common.edi
 * </ul>
 *
 * <p> Six system properties are used to configure this scheme. The
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
 *     <td>cleo.uri.myfs.file</td>
 *     <td>Yes</td>
 *     <td>examples.URISchemeExamples.com.companyabc.myfs.MyFSFile</td>
 *   </tr>
 *   <tr>
 *     <td>cleo.uri.myfs.inputstream</td>
 *     <td>Yes</td>
 *     <td>examples.URISchemeExamples.com.companyabc.myfs.MyFSInputStream</td>
 *   </tr>
 *   <tr>
 *     <td>cleo.uri.myfs.outputstream</td>
 *     <td>Yes</td>
 *     <td>examples.URISchemeExamples.com.companyabc.myfs.MyFSOutputStream</td>
 *   </tr>
 *   <tr>
 *     <td>cleo.uri.myfs.classpath</td>
 *     <td>No</td>
 *     <td> This property defines the Java class path to the MyFSURI.jar which
 *          contains the scheme implementation.</td>
 *   </tr>
 *   <tr>
 *     <td>cleo.uri.myfs.basepath</td>
 *     <td>No</td>
 *     <td> This property defines the base path which will be the root (MyFS:\)
 *          path. If the system property is not present the base directory
 *          defaults to .\URITestDir\ in the installation folder.</td>
 *   </tr>
 *   <tr>
 *     <td>cleo.uri.myfs.commonpath</td>
 *     <td>No</td>
 *     <td> This property defines the path for the "common" directory under
 *          each mailbox. If the system property is not present the common
 *          directory is disabled.</td>
 *   </tr>
 * </table>
 *
 */
public class MyFSFile extends LexURIFile {
  private final static String MYFS_SCHEME = "myfs:";
  private final static String COMMON_DIR  = "common";

  private String myURI     = null;
  private String myMailbox = null;
  private File   myFile    = null;
  private String myAbsPath = null;

  private static boolean baseCommonInitialized = false;
  private static String  baseDir   = null;
  private static String  commonDir = null;

  private URISyntaxException uriSyntaxException = null;

//==============================================================================
// Constructors
//==============================================================================
  /**
   * MyFSFile constructor with entire URI.
   *
   * <p> <b>NOTE:</b> The first line should always be <code>super(uri);</code>
   *
   * @param uri URI for MyFSFile
   */
  public MyFSFile(String uri) {
    super(uri);
    LexURIFile.debug("MyFSFile(" + uri + ")");

    if (uri == null)
      throw new NullPointerException();

    if (!baseCommonInitialized) {
      getBaseDir();   // Retrieve base directory from system properties
      getCommonDir(); // Retrieve common directory from system properties
      baseCommonInitialized = true;
    }

    // Parse the URI
    parseURI(uri);
  }

  /**
   * Constructor for a MyFSFile where the filename is passed in as the child.
   *
   * <p> <b>NOTE:</b> The first line should always be
   *      <code>super(parentUri, child);</code>
   *
   * @param parentUri Parent URI representing the parent folder of the child.
   * @param child     Filename
   */
  public MyFSFile(String parentUri, String child) {
    super(parentUri, child);
    LexURIFile.debug("MyFSFile(" + parentUri + "," + child + ")");
    if (child == null)
      throw new NullPointerException();

    if (!baseCommonInitialized) {
      getBaseDir();   // Retrieve base directory from system properties
      getCommonDir(); // Retrieve common directory from system properties
      baseCommonInitialized = true;
    }

    // Build a single URI with child added to the parentURI and parse the
    // resulting URI.
    parseURI(buildURIString(parentUri, child));
  }

//==============================================================================
// File methods that MUST be overriden
//==============================================================================
  /**
   * Returns <code>true</code> if the MyFSFile can be read.
   *
   * @return <code>true</code> if the folder or file named in MyFSFile exists.
   */
  @Override
  public boolean canRead() {
    return (myFile != null) ? this.myFile.canRead() : false;
  }

  /**
   * Returns <code>true</code> if the MyFSFile can be written.
   *
   * @return <code>true</code> if the folder or file named in MyFSFile exists.
   */
  @Override
  public boolean canWrite() {
    return (myFile != null) ? this.myFile.canWrite() : false;
  }

  /**
   * Compares the URIs of two LexURIFiles lexicographically.
   *
   * @param pathname The <code>LexURIFile</code> to be compared to this
   *                 <code>MyFSFile</code>.
   * @return Zero if the argument is equal to this <code>MyFSFile</code>, a
   *         value less than zero if this <code>MyFSFile</code>'s URI is
   *         lexicographically less than the argument, or a value greater than
   *         zero if this <code>MyFSFile</code>'s URI is lexicographically
   *         greater than the argument.
   */
  @Override
  public int compareTo(LexURIFile pathname) {
    return (myFile != null) ? this.myFile.getAbsolutePath().compareToIgnoreCase(pathname.getAbsolutePath()) : -1;
  }

  /**
   * Atomically creates a new, empty file named by this abstract pathname if
   * and only if a file with this name does not yet exist.
   *
   * @return <code>true</code> if the named file does not exist and was
   *         successfully created; <code>false</code> if the named file
   *         already exists
   * @throws IOException
   */
  @Override
  public boolean createNewFile() throws IOException {
    return (myFile != null) ? this.myFile.createNewFile() : false;
  }

  /**
   * Deletes the named file or folder.
   *
   * @return <code>true</code> on a successful delete of the file or folder.
   */
  @Override
  public boolean delete() {
    return (myFile != null) ? myFile.delete() : false;
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
    if ((obj == null) || (obj.getClass() != MyFSFile.class))
      return false;
    return (obj == this);
  }

  /**
   * Checks if the specified <code>MyFSFile</code> URI exists.
   * @return <code>true</code> if the <code>MyFSFile</code> exists
   */
  @Override
  public boolean exists() {
    return (myFile != null) ? myFile.exists() : false;
  }

  /**
   * Returns a new <code>MyFSFile</code> object using the original URI as
   * all URI paths are absolute.
   *
   * @return A new <code>MyFSFile</code> object with the same URI as
   *         <code>this</code>.
   */
  @Override
  public MyFSFile getAbsoluteFile() {
    return new MyFSFile(this.myURI);
  }

  /**
   * Returns the original URI used when creating this object as all URI paths
   * are absolute.
   * @return The original URI used to create this object.
   */
  @Override
  public String getAbsolutePath() {
    return this.myURI;
  }

  /**
   * Returns the same result as <code>new MyFSFile(this.{@link #getAbsolutePath()})</code>.
   *
   * @return A new <code>MyFSFile</code> object matching the original URI.
   */
  @Override
  public MyFSFile getCanonicalFile() {
    return new MyFSFile(this.myURI);
  }

  /**
   * Returns the same URI as if <code>this.{@link #getPath()}</code> were called.
   *
   * @return The original URI.
   * @throws IOException
   */
  @Override
  public String getCanonicalPath() throws IOException {
    return this.myURI;
  }

  /**
   * Returns the name of the file or directory denoted by this
   * <code>MyFSFile</code>. This is just the last name in the pathname's
   * name sequence.
   *
   * @return The name of the file or directory represented in the URI
   */
  @Override
  public String getName() {
    if (myFile == null)
      return null;
    else if (myFile.getPath().equals(commonDir))
      return "common";
    else
      return myFile.getName();
  }

  /**
   * Returns the parent of the directory or file.
   *
   * @return Returns the name of the parent represented by this
   *         <code>MyFSFile</code>.
   */
  @Override
  public String getParent() {
    // Check if this is the root of the common folder
    String parentPath = getParentPath(this.myFile);
    String parentUri = filePathToURI(parentPath);
    return parentUri;
  }

  /**
   * Returns the <code>MyFSFile</code> object of this <code>MyFSFile</code>'s
   * parent, or <code>null</code> if this pathname does not name a parent
   * directory.
   *
   * <p> If this <code>MyFSFile</code> has a parent, then this is equivalent to
   * <code>new&nbsp;MyFSFile(this.{@link #getParent})</code>.
   *
   * @return  The <code>MyFSFile</code> object of the parent directory named
   *          by this <code>MyFSFile</code>, or <code>null</code> if this
   *          <code>MyFSFile</code> does not name a parent
   */
  @Override
  public MyFSFile getParentFile() {
    String parentPath = getParentPath(this.myFile);
    String parentUri = filePathToURI(parentPath);
    return new MyFSFile(parentUri);
  }

  /**
   * Returns the original URI string for the <code>MyFSFile</code>.
   *
   * @return Returns the URI string for the <code>MyFSFile</code> object
   */
  @Override
  public String getPath() {
    return this.myURI;
  }

  /**
   * Returns <code>true</code> as all <code>MyFSFile</code> URI paths are absolute.
   * @return <code>true</code>
   */
  @Override
  public boolean isAbsolute() {
    return true;
  }

  /**
   * Returns <code>true</code> if the URI represented by this
   * <code>MyFSFile</code> is a directory).
   *
   * @return <code>true</code> if <code>MyFSFile</code> represents a directory.
   */
  @Override
  public boolean isDirectory() {
    return (myFile != null) ? myFile.isDirectory() : false;
  }

  /**
   * Returns <code>true</code> if the URI represented by this
   * <code>MyFSFile</code> is a file.
   *
   * @return <code>true</code> if <code>MyFSFile</code> represents a file.
   */
  @Override
  public boolean isFile() {
    return (myFile != null) ? myFile.isFile() : false;
  }

  /**
   * Returns <code>true</code> if the file is considered a hidden file by
   * the base operating system.
   *
   * @return <code>true</code> if the file is hidden; <code>false</code> otherwise
   */
  @Override
  public boolean isHidden() {
    return (myFile != null) ? myFile.isHidden() : false;
  }

  /**
   * Returns the last modified time of the file.
   * @return Last modified time of the file.
   */
  @Override
  public long lastModified() {
    return (myFile != null) ? myFile.lastModified() : 0L;
  }

  /**
   * Returns the length of the file represented by this <code>MyFSFile</code>.
   * @return The length, in bytes, of the file represented by this
   *         <i>Scheme</i>File, or 0L if the file does not exist.
   */
  @Override
  public long length() {
    return (myFile != null) ? myFile.length() : 0L;
  }

  /**
   * Returns an array of strings naming the files and directories in the
   * directory represented by this <code>MyFSFile</code>.
   *
   * <p> An array of strings is returned, one for each file or in the directory.
   * Each string is a complete path including the file name.
   *
   * <p> For example:
   * <br> If the list() was called for a URI of
   *      <b>MyFS:\MyMailbox</b>
   * <br> The following is an example of return values:
   * <ul>
   * <li> MyFS:\MyMailbox\test.edi
   * <li> MyFS:\MyMailbox\abc.edi
   * <li> MyFS:\MyMailbox\test100K.edi
   * </ul>
   *
   * <p> There is no guarantee that the name strings in the resulting array
   *     will appear in any specific order; they are not, in particular,
   *     guaranteed to appear in alphabetical order.
   *
   * @return An array of strings naming the files and directories in the
   *         directory denoted by this <code>MyFSFile</code>. The array will be
   *         empty if the directory is empty. Returns null if this
   *         <code>MyFSFile</code> does not represent a directory, or if an I/O
   *         error occurs.
   */
  @Override
  public String[] list() {
    // Throw SecurityException if URI was bad in constructor
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);

    boolean addCommon = (commonDir != null) && (uriPathLevel(this.myAbsPath) == 1);
    File cmnFile = null;
    if (addCommon) {
      cmnFile = new File(commonDir);
      if (!cmnFile.exists() || !cmnFile.isDirectory())
        addCommon = false;
    }

    String[] files = (myFile != null) ? myFile.list() : new String[0];
    int filesLen = (files != null) ? files.length : 0;
    if (addCommon) {
      String[] retFiles = new String[filesLen+1];
      retFiles[0] = COMMON_DIR;
      if (filesLen > 0)
        System.arraycopy(files, 0, retFiles, 1, filesLen);
      return retFiles;
    } else
      return files;
  }

  /**
   * Returns an array of strings naming the files and directories in the
   * directory represented by this <code>MyFSFile</code> that satisfy the specified
   * filter.  The behavior of this method is the same as that of the
   * <code>{@link #list()}</code> method, except that the strings in the
   * returned array must satisfy the filter.  If the given
   * <code>filter</code> is <code>null</code> then all names are accepted.
   * Otherwise, a name satisfies the filter if and only if the value
   * <code>true</code> results when the
   * <a href="http://docs.oracle.com/javase/6/docs/api/java/io/FilenameFilter.html#accept()">FilenameFilter.accept()</a>
   * method of the filter is invoked on this
   * abstract pathname and the name of a file or directory in the directory
   * that it denotes.
   *
   * @param  filter  A filename filter
   *
   * @return  An array of strings naming the files and directories in the
   *          directory denoted by this this <i>Scheme</i>File that were accepted
   *          by the given <code>filter</code>.  The array will be empty if
   *          the directory is empty or if no names were accepted by the
   *          filter.  Returns <code>null</code> if this abstract pathname
   *          does not denote a directory, or if an I/O error occurs.
   */
  @Override
  public String[] list(FilenameFilter filter) {
    // Throw SecurityException if URI was bad in constructor
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);

    boolean addCommon = (commonDir != null) && (uriPathLevel(this.myAbsPath) == 1);
    File cmnFile = null;
    if (addCommon) {
      cmnFile = new File(commonDir);
      if (!cmnFile.exists() || !cmnFile.isDirectory())
        addCommon = false;
    }

    ArrayList<String> retArrayList = new ArrayList<String>();
    File[] files = (this.myFile != null) ? this.myFile.listFiles() : new File[0];

    int filesLen = (files != null) ? files.length : 0;
    String[] retFiles;
    int ctr = 0;
    boolean addItem;
    if (addCommon) {
      String tmpPath = filePathToURI(cmnFile.getPath());
      addItem = true;
      if (filter != null) {
        MyFSFile tmpFSFile = new MyFSFile(tmpPath);
        if (!filter.accept(new File(tmpFSFile.getParent()), tmpFSFile.getName()))
          addItem = false;
      }
      if (addItem) {
        retArrayList.add("common");
        LexURIFile.debug("MyFSFile.listFiles> files[" + ctr + "]=" + tmpPath);
        ctr++;
      }
    }

    for (int ii = 0; ii < filesLen; ii++) {
      String tmpPath = filePathToURI(files[ii].getPath());
      addItem = true;
      if (filter != null) {
        MyFSFile tmpFSFile = new MyFSFile(tmpPath);
        if (!filter.accept(new File(tmpFSFile.getParent()), tmpFSFile.getName()))
          addItem = false;
      }
      if (addItem) {
        retArrayList.add(files[ii].getName());
        LexURIFile.debug("MyFSFile.listFiles> files[" + ctr + "]=" + tmpPath);
        ctr++;
      }
    }

    retFiles = retArrayList.toArray(new String[retArrayList.size()]);
    return retFiles;
  }

  /**
   * Returns an array of <code>MyFSFile</code> objects in the directory denoted by
   * this <code>MyFSFile</code>.
   *
   * <p> An array of <code>MyFSFile</code> objects is returned, one for each
   * file in the directory.
   *
   * <p> There is no guarantee that the name strings in the resulting array
   * will appear in any specific order; they are not, in particular,
   * guaranteed to appear in alphabetical order.
   *
   * @return An array of <code>MyFSFile</code>s denoting the files and
   *         directories in the directory denoted by this <code>MyFSFile</code>.
   *         The array will be empty if the directory is empty.
   *         Returns <code>null</code> if this <code>MyFSFile</code> does not
   *         denote a directory, or if an I/O error occurs.
   */
  @Override
  public MyFSFile[] listFiles() {
    // Throw SecurityException if URI was bad in constructor
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);

    boolean addCommon = (commonDir != null) && (uriPathLevel(this.myAbsPath) == 1);
    File cmnFile = null;
    if (addCommon) {
      cmnFile = new File(commonDir);
      if (!cmnFile.exists() || !cmnFile.isDirectory())
        addCommon = false;
    }

    File[] files = (this.myFile != null) ? this.myFile.listFiles() : new File[0];
    int filesLen = (files != null) ? files.length : 0;
    MyFSFile[] retMyFSFiles;
    int ctr = 0;
    if (addCommon) {
      String tmpPath = filePathToURI(cmnFile.getPath());
      retMyFSFiles = new MyFSFile[filesLen+1];
      LexURIFile.debug("MyFSFile.listFiles> files[" + ctr + "]=" + tmpPath);
      retMyFSFiles[ctr++] = new MyFSFile(tmpPath);
    } else
      retMyFSFiles = new MyFSFile[filesLen];
    for (int ii = 0; ii < filesLen; ii++) {
      String tmpPath = filePathToURI(files[ii].getPath());
      LexURIFile.debug("MyFSFile.listFiles> files[" + ctr + "]=" + tmpPath);
      retMyFSFiles[ctr++] = new MyFSFile(tmpPath);
    }
    return retMyFSFiles;
  }

  /**
   * Returns an array of <code>MyFSFile</code> objects filtered by the
   * LexURIFileFilter.
   *
   * @param filter A LexURIFileFilter implemented with VLTrader. The
   *               <code>MyFSFile</code> implementation should call
   *               filter.accept(lexURIFile) method to determine if this
   *               <code>MyFSFile</code> should be included in the return array.
   * @return Returns an array of <code>MyFSFile</code> objects filtered by
   *         <code>filter</code>.
   */
  @Override
  public MyFSFile[] listFiles(LexURIFileFilter filter) {
    // Throw SecurityException if URI was bad in constructor
    if (this.uriSyntaxException != null)
      throw new SecurityException(this.uriSyntaxException);

    // Build list of all messages
    String[] uriFileList = this.list();
    if (uriFileList == null)
      return null;

    // Build list of acceptable messages
    ArrayList<MyFSFile> fileList = new ArrayList<MyFSFile>();
    for (int ii = 0; ii < uriFileList.length; ii++) {
      try {
        MyFSFile testFile = new MyFSFile(this.getPath(), uriFileList[ii]);
        if ((filter == null) || (filter.accept(testFile)))
          fileList.add(testFile);
      } catch (Exception ex) {
        LexURIFile.debug(true, "MyFSFile.listFiles(LexURIFileFilter) exception. "
                         + "uriFileList[" + ii + " of " + uriFileList.length
                         + "]='" + uriFileList[ii] + "'", ex);
      }
    }

    return fileList.toArray(new MyFSFile[fileList.size()]);
  }

  /**
   * Creates the directory specified in the <code>MyFSFile</code>.
   *
   * @return <code>true</code> if the directory was created; <code>false</code>
   *         otherwise
   */
  @Override
  public boolean mkdir() {
    return (myFile != null) ? myFile.mkdir() : false;
  }

  /**
   * Creates the directory named by this <code>MyFSFile</code>, including any
   * necessary but nonexistent parent directories. Note that if this operation
   * fails it may have succeeded in creating some of the necessary parent
   * directories.
   *
   * @return <code>true</code> if the directory was created, along with all
   *         necessary parent directories; <code>false</code> otherwise
   */
  @Override
  public boolean mkdirs() {
    return (myFile != null) ? myFile.mkdirs() : false;
  }

  /**
   * Renames the file denoted by this <code>MyFSFile</code>.
   *
   * @param dest The new pathname for the named file
   * @return <code>true</code> if the renaming succeeded; <code>false</code>
   *         otherwise
   */
  @Override
  public boolean renameTo(LexURIFile dest) {
    if (dest instanceof MyFSFile)
      return (myFile != null) ? myFile.renameTo(((MyFSFile)dest).myFile) : false;
    else {
      LexURIFile.debug("MyFSFile.renameTo()> needs to be fixed");
      return false;
    }
  }

  /**
   * Sets the last-modified time of the file or directory named by this
   * <code>MyFSFile</code>.
   *
   * @param time The new last-modified time, measured in milliseconds since the
   *             epoch (00:00:00 GMT, January 1, 1970)
   * @return <code>true</code> if the operation succeeded; <code>false</code>
   *         otherwise
   */
  @Override
  public boolean setLastModified(long time) {
    return (myFile != null) ? myFile.setLastModified(time) : false;
  }

  /**
   * A method to set the owner's read permission for this <code>MyFSFile</code>.
   *
   * @param readable If <code>true</code>, sets the access permission to allow
   *                 read operations; if <code>false</code> to disallow read
   *                 operations
   * @return <code>true</code> if the operation succeeded
   */
  @Override
  public boolean setReadable(boolean readable) {
    return (myFile != null) ? myFile.setReadable(readable) : false;
  }

  /**
   * Sets the owner's or everybody's read permission for this <code>MyFSFile</code>.
   *
   * @param readable  If <code>true</code>, sets the access permission to allow
   *                  read operations; if <code>false</code> to disallow read
   *                  operations
   * @param ownerOnly If <code>true</code>, the read permission applies only to
   *                  the owner's read permission; otherwise, it applies to
   *                  everybody. If the underlying file system can not
   *                  distinguish the owner's read permission from that of
   *                  others, then the permission will apply to everybody,
   *                  regardless of this value.
   * @return <code>true</code> if the operation succeeded
   */
  @Override
  public boolean setReadable(boolean readable, boolean ownerOnly) {
    return (myFile != null) ? myFile.setReadable(readable, ownerOnly) : false;
  }

  /**
   * Marks the file or directory named by this <code>MyFSFile</code> so that
   * only read operations are allowed. After invoking this method the file or
   * directory is guaranteed not to change until it is either deleted or
   * marked to allow write access. Whether or not a read-only file or directory
   * may be deleted depends upon the underlying system.
   *
   * @return <code>true</code> if the operation succeeded; <code>false</code>
   *         otherwise
   */
  @Override
  public boolean setReadOnly() {
    return (myFile != null) ? myFile.setReadOnly() : false;
  }

  /**
   * Returns the pathname string of this <code>MyFSFile</code>. This is the
   * same as calling <code>this.{@link #getPath()}</code>.
   *
   * @return This string form of this <code>MyFSFile</code>.
   */
  @Override
  public String toString() {
    return this.getPath();
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
    return super.hashCode();
  }

//==============================================================================
// Additional non-File methods that must be implemented
//==============================================================================
  /**
   * Returns the type of this <i>Scheme</i>File implementation.
   *
   * @return LexFileType.FILESYSTEM
   */
  @Override
  public LexFileType getType() {
    return LexFileType.FILESYSTEM;
  }

  /**
   * Returns an exception generated during the <code>MyFSFile</code> constructor.
   *
   * <p> If there is a problem with the URI, an exception should not be thrown
   * during the constructor. Instead the exception should be saved and
   * <b>getURIException()</b> should return this exception. In addition, if
   * any method is called after a constructor with a bad URI, the saved
   * exception should be thrown at the time the method is called.
   *
   * @return The exception generated during the <code>MyFSFile</code>
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
   * @return Returns a <code>com.cleo.lexicom.beans.LexWildcardInfo</code>
   *         object if a wildcard is present or <code>null</code> otherwise.
   */
  @Override
  public LexWildcardInfo getLexWildcardInfo() {
    String path = this.myURI;
    boolean regex = false;
    int begIdx, endIdx;
    int wildIdx;

    String wildString = "";

    if (path != null) {
      begIdx = path.lastIndexOf('[');
      endIdx = path.lastIndexOf(']');
/*------------------------------------------------------------------------------
 * First, look for a regex.  Regular expressions must follow these rules:
 *  - enclosed in brackets
 *  - closing bracket must be last character of path string
 *  - opening bracket must be first character of path string or just
 *    following a path separator
 *----------------------------------------------------------------------------*/
      if (begIdx == -1 || endIdx == -1)
        regex = false;
      else if (begIdx == 0 && endIdx == path.length()-1)
        regex = true;
      else if (endIdx == path.length()-1 &&
               endIdx > begIdx &&
               (path.regionMatches(begIdx - 1, "/", 0, 1) ||
                path.regionMatches(begIdx - 1, "\\", 0, 1) ||
                path.regionMatches(begIdx - 1, File.separator, 0, 1)))
        regex = true;

      if (regex) {
        wildIdx = begIdx;
     } else {
/*------------------------------------------------------------------------------
 *      If no regular expression is found, then look for traditional wildcards.
 *----------------------------------------------------------------------------*/
        path = path.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        int lastSeparator = path.lastIndexOf(File.separator);
        int begStarIdx = path.lastIndexOf('*');
        int begQIdx = path.lastIndexOf('?');
        if (begStarIdx != -1 && begStarIdx > lastSeparator)
          wildIdx = lastSeparator + 1;
        else if (begQIdx != -1 && begQIdx > lastSeparator)
          wildIdx = lastSeparator + 1;
        else
          wildIdx = -1;
      }

      path = path.trim();
      if (wildIdx != -1) {
        wildString = path.substring(wildIdx);
        path = path.substring(0,wildIdx);
      }
    } else {
      path = "";
      wildString = "";
    }

    return new LexWildcardInfo(path, wildString);
  }

//==============================================================================
// Helper methods
//==============================================================================
  /**
   * Builds a URI string from the <code>parent</code> and <code>child</code>.
   *
   * @param parent URI string representing the parent folder
   * @param child  Name of file within the parent folder
   * @return A URI string representing the full path to the child.
   */
  private String buildURIString(String parent, String child) {
    if ((parent == null) || (parent.trim().length() == 0))
      parent = null;
    if ((child == null) || (child.trim().length() == 0))
      child = null;
    if ((parent == null) && (child == null))
      return null;

    if ((parent != null) && (child == null))
      return parent;
    else if ((parent == null) && (child != null))
      return child;
    else {
      StringBuilder sb = new StringBuilder();
      sb.append(parent);
      sb.append(File.separator);
      sb.append(child);

      return normalizeURI(sb.toString());
    }
  }

  /**
   * This converts the actual file path to a MyFSFile URI string
   *
   * @param filePath Absolute path to the actual file.
   * @return A MyFSFile URI string representing the <code>filePath</code>.
   */
  private String filePathToURI(String filePath) {
    String uri;
    if (commonDir != null && filePath.startsWith(commonDir)) {
      if (filePath.length() > commonDir.length())
        uri = MYFS_SCHEME + File.separator + this.myMailbox
                + File.separator + COMMON_DIR
                + filePath.substring(commonDir.length());
      else if (filePath.length() < commonDir.length())
        return null;
      else
        uri = MYFS_SCHEME + File.separator + this.myMailbox
                + File.separator + COMMON_DIR;
    } else {
      if (filePath.length() > baseDir.length())
        uri = MYFS_SCHEME + filePath.substring(baseDir.length());
      else if (filePath.length() < baseDir.length())
        return null;
      else
        uri = MYFS_SCHEME + File.separator;
    }
    return uri;
  }

  /**
   * Returns the internal File object associated with this <code>MyFSFile</code>.
   * @return The internal File object associated with this <code>MyFSFile</code>.
   */
  protected File getInternalFile() {
    return this.myFile;
  }

  /**
   * Retrieves the mailbox from the <code>MyFSFile</code> URI. This is assumed
   * to be the highest level path.
   *
   * @param uri <code>MyFSFile</code> URI.
   * @return Name of the mailbox
   */
  private String getMailbox(String uri) {
    // Strip off scheme name
    String subDirName = uri.substring(MYFS_SCHEME.length());
    // Tokenize string based on File.separator
    StringTokenizer st = new StringTokenizer(subDirName, File.separator);
    String mbStr = null;
    if (st.hasMoreTokens())
      mbStr = st.nextToken();
    return mbStr;
  }

  /**
   * Returns the parent file of the <code>file</code> parameter. This performs
   * special  processing due as the common folders parent is the mailbox folder.
   *
   * @param file File for which to return parent.
   * @return A string representing the parent of <code>file</code>.
   */
  private String getParentPath(File file) {
    // Return null if null passed in
    if (file == null)
      return null;
    String parentPath = null;
    // If commonDir not defined, then return the parent of this file
    if (commonDir == null)
      parentPath = file.getParent();
    else {
      // If we are at the root "common" folder, then return the mailbox
      // folder. Otherwise return file's parent.
      String curPath = file.getPath();
      if (curPath.equals(commonDir))
        parentPath = baseDir + File.separator + this.myMailbox;
      else
        parentPath = file.getParent();
    }
    return parentPath;
  }

  /**
   * Normalizes the <code>MyFSFile</code> URI by removing double slashes
   * and converting all slashes to the standard directory separator for this
   * platform.
   *
   * @param uri URI to normalize
   * @return Normalized URI
   */
  private String normalizeURI(String uri) {
    String tmpUri = uri;
    // Update URI to use slashes based on the filesystem
    if (!"\\".equals(File.separator))
      tmpUri = uri.replace("\\", File.separator);
    if (!"/".equals(File.separator))
      tmpUri = uri.replace("/", File.separator);
    // Remove all double slashes in the URI
    String dblSeparator = File.separator + File.separator;
    while (tmpUri.indexOf(dblSeparator) > 0)
      tmpUri = tmpUri.replace(dblSeparator, File.separator);
    // Add separator after scheme if it is missing
    if ((tmpUri.length() > MYFS_SCHEME.length()) &&
        (tmpUri.charAt(MYFS_SCHEME.length()) != File.separatorChar))
      tmpUri = MYFS_SCHEME + File.separator + tmpUri.substring(MYFS_SCHEME.length());
    else if (tmpUri.equalsIgnoreCase(MYFS_SCHEME))
      tmpUri += File.separator;
    return tmpUri;
  }

  /**
   * Parses the URI string.
   * @param uri URI string to parse
   * @throws NullPointerException
   */
  private void parseURI(String uri) throws NullPointerException {
    // If null is passed in, then throw this exception now.
    // Otherwise we save the exception and throw it when the MyFSFile object is used.
    if (uri == null)
      throw new NullPointerException(MYFS_SCHEME + " URI is null");

    // Example: MyFS:\mailbox\file1

    // Make sure URI starts with "myfs:"
    if (!uri.toLowerCase().startsWith(MYFS_SCHEME)) {
      this.uriSyntaxException = new URISyntaxException(uri,
               "Invalid " + MYFS_SCHEME + " syntax-URI does not start with '"
               + MYFS_SCHEME + "'");
      LexURIFile.debug(true, this.uriSyntaxException.getMessage());
      return;
    }

    String subDirName = uri.substring(MYFS_SCHEME.length());
    if (subDirName.length() == 0) {
      this.uriSyntaxException = new URISyntaxException(uri, "Invalid " + MYFS_SCHEME + " syntax-Missing folders");
      LexURIFile.debug(true, this.uriSyntaxException.getMessage());
      return;
    }

    HierarchicalURI hierarchicalURI = HierarchicalURI.parse(uri).resolveDots(1);
    this.myURI = normalizeURI(hierarchicalURI.toString());

    this.myMailbox = getMailbox(this.myURI);
    this.myAbsPath = uriToFilePath(this.myURI);
    this.myFile    = new File(this.myAbsPath);
  }

  /**
   * Returns the number of levels in the URI path.
   * @param path Actual file system path
   * @return Number of levels in the <code>MyFSFile</code> path.
   */
  private int uriPathLevel(String path) {
    String uriPath = filePathToURI(path);
    StringTokenizer st = new StringTokenizer(uriPath, File.separator);
    int pathLevel = st.countTokens() - 1;
    return pathLevel;
  }

  /**
   *
   * @param uriPath
   * @return
   */
  /**
   * This converts the MyFSFile URI string to the actual file path
   *
   * @param uriPath A MyFSFile URI string.
   * @return Absolute path of the actual file represented by the
   *         <code>uriPath</code>.
   */
  private String uriToFilePath(String uriPath) {
    // Strip off scheme name
    int colonIdx = uriPath.indexOf(":");
    String path = uriPath.substring(colonIdx+1);
    // Remove leading separator
    if (path.startsWith(File.separator))
      path = path.substring(1);
    // Determine if this is the common directory
    boolean isCommonDir = false;
    if (commonDir != null) {
      // Check if <mailbox>/common/...
      StringTokenizer st = new StringTokenizer(path, File.separator);
      if (st.countTokens() >= 2) {
        st.nextToken(); // Skip mailbox
        String testStr = st.nextToken();
        if (testStr.equalsIgnoreCase(COMMON_DIR)) {
          isCommonDir = true;
          // Remove <mailbox>/common from path
          int idx = path.toLowerCase().indexOf(File.separator + COMMON_DIR);
          path = path.substring(idx + 1 + COMMON_DIR.length());
          if (path.startsWith(File.separator))
            path = path.substring(1);
        }
      }
    }
    // Build the "real" path
    if (isCommonDir)
      path = commonDir + File.separator + path;
    else
      path = baseDir + File.separator + path;
    return path;
  }

//==============================================================================
// Static helper methods
//==============================================================================
  /**
   * Retrieves the base directory for the MyFS: scheme from the system
   * property "cleo.uri.myfs.basepath". If the system property is not present
   * the base directory defaults to .\URITestDir\ in the installation folder.
   */
  private static void getBaseDir() {
    String basePath = System.getProperty("cleo.uri.myfs.basepath");
    if (basePath != null)
      basePath = basePath.trim();
    if ((basePath == null) || (basePath.length() == 0))
      baseDir = System.getProperty("user.dir") + File.separator + "URITestDir";
    else
      baseDir = basePath;

    if (baseDir.endsWith("/") || (baseDir.endsWith("\\")))
      baseDir = baseDir.substring(0, baseDir.length()-1);

    LexURIFile.debug("The base path for the 'MyFS:' scheme is:");
    LexURIFile.debug("\t[" + baseDir + "]");
  }

  /**
   * Retrieves the common directory for the MyFS: scheme from the system
   * property "cleo.uri.myfs.commonpath". If the system property is not present
   * the common directory is disabled.
   */
  private static void getCommonDir() {
    String commonPath = System.getProperty("cleo.uri.myfs.commonpath");
    if (commonPath != null)
      commonPath = commonPath.trim();
    if ((commonPath == null) || (commonPath.length() == 0)) {
      LexURIFile.debug("NO common path configured for the 'MyFS:' scheme!");
      return;
    } else
      commonDir = commonPath;

    if (commonDir.endsWith("/") || commonDir.endsWith("\\"))
      commonDir = commonDir.substring(0, commonDir.length()-1);

    LexURIFile.debug("The common path for the 'MyFS:' scheme is:");
    LexURIFile.debug("\t[" + commonDir + "]");
  }
}
