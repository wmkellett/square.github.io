package examples.URISchemeExamples.com.companyabc.mydb;

import com.cleo.lexicom.beans.LexURIFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;

/**
 * The <Code>MyDBUtil</code> class contains utility methods for accessing the
 * MySQL database for the <i>MyDB:</i> scheme.
 * 
 * <p> <b>Note:</b> This is an example only. A better implementation would
 * use a database connection pool and make multiple attempts to update the
 * database in the case of a failure.
 * 
 */
public class MyDBUtil {
  // Database connection information
  private static String DRIVER   = "com.mysql.jdbc.Driver";
  private static String URL      = null;
  private static String USERNAME = null;
  private static String PW       = null;
  
  // Database table column names
  private static String ID_COLNAME           = "ID";
  private static String FILETIME_COLNAME     = "FileTime";
  private static String FILENAME_COLNAME     = "Filename";
  private static String FILELENGTH_COLNAME   = "FileLength";
  private static String FILECONTENTS_COLNAME = "FileContents";
  
  // Other internal flags
  private static boolean driverLoaded = false;
  
  /**
   * Loads the database driver
   * 
   * @param driverName Database driver class
   * @throws Exception
   */
  public static void loadDriver(String driverName) throws Exception {
    try {
      LexURIFile.debug("MyDBUtil.loadDriver> Loading Database driver ["
                       + driverName + "]");
      Class.forName(driverName).newInstance();
      driverLoaded = true;
    } catch (Exception ex) {
      LexURIFile.debug("MyDBUtil.loadDriver> Failed to load Database driver: "
                       + driverName);
      throw ex;
    }
  }
  
  /**
   * Retrieves a connection to the database.
   * 
   * <p> In an actual implementation, it is strongly recommended to use a
   * database connection pool.
   * 
   * @return     A connection to the database
   * @throws Exception
   */
  public static Connection getDBConnection() throws Exception {
    if (!driverLoaded)
      loadDriver(DRIVER);
    
    if (URL == null)
      URL = System.getProperty("cleo.uri.mydb.url");
    if (URL == null)
      URL = "jdbc:mysql://localhost:3306/mysqlvltrader";
    
    if (USERNAME == null)
      USERNAME = System.getProperty("cleo.uri.mydb.username");
    if (USERNAME == null)
      USERNAME = "root";

    if (PW == null)
      PW = System.getProperty("cleo.uri.mydb.pw");
    if (PW == null)
      PW = "mysqlroot";
    
    Connection connection;
    if ((USERNAME != null && USERNAME.length() > 0) || (PW != null && PW.length() > 0))
      connection = DriverManager.getConnection(URL, USERNAME, PW);
    else
      connection = DriverManager.getConnection(URL);
    
    return connection;
  }

  /**
   * Checks if a database table exists in the database
   * @param tableName Name of the table to check
   * @return <code>true</code> if the database table exists;
   *         <code>false</code> otherwise
   */
  protected static boolean dbTableExists(String tableName) {
    boolean tableExists = false;
    Connection dbCon = null;
    try {
      // Get a new connection to the database
      dbCon = getDBConnection();
      // Check if the tble exists
      tableExists = dbGetCaseSensitiveTableName(dbCon, tableName) != null;
    } catch (Exception ex){
      LexURIFile.debug("MyDBUtil.dbTableExists(" + tableName + ")", ex);
      tableExists = false;
    } finally {
      if (dbCon != null) {
        try {
          dbCon.close();
        } catch (Exception closeEx) {
          LexURIFile.debug("MyDBUtil.dbTableExists(" + tableName + ")", closeEx);
        }
      }
    }
    return tableExists;
  }
  
  /**
   * Checks if a database table exists
   * @param connection Current connection to the database
   * @param tableName  Name of table to check
   * @return The name of the matching table or <code>null</code> if no match
   *         was found.
   */
  private static String dbGetCaseSensitiveTableName(Connection connection, String tableName) {
    LexURIFile.debug("MyDBUtil.dbGetCaseSensitiveTableName> Check if table ["
            + tableName + "] exists...");
    ResultSet tblrs = null;
    try {
      DatabaseMetaData md = connection.getMetaData();
      if (md.storesLowerCaseIdentifiers())
        tableName = tableName.toLowerCase();
      else if (md.storesUpperCaseIdentifiers())
        tableName = tableName.toUpperCase();
      tblrs = md.getTables(null, null, tableName, new String[] {"TABLE"});
      while (tblrs.next()) {
        String tblName = tblrs.getString(3);
        LexURIFile.debug("tblName[" + tblName + "]");
        if (tblName.equalsIgnoreCase(tableName)) {
          LexURIFile.debug("MyDBUtil.dbGetCaseSensitiveTableName> " + tblName);
          return tblName;
        }
      }
    } catch (Exception ex) {
      LexURIFile.debug("MyDBUtil.dbGetCaseSensitiveTableName(" + tableName + ")", ex);
    } finally {
      try {
        if (tblrs != null)
          tblrs.close();
      } catch (Exception ex) {}
    }

    return null;
  }

  /**
   * Writes bytes as a file to the database.
   * 
   * @param dbConnection Connection to the database
   * @param tableName    Name of the database table to write to
   * @param filename     Name of the file being written
   * @param fileBytes    File contents
   * @throws Exception 
   */
  protected static void writeRecord(Connection dbConnection, String tableName, 
                                    String filename, byte[] fileBytes)
          throws Exception {
    if ((dbConnection == null) || dbConnection.isClosed())
      throw new Exception("Database connection is not open");
    
    // Build SQL statement for to insert the record
    String sql = "INSERT INTO " + tableName + " " +
                          " ( " + FILETIME_COLNAME + ", " +
                                  FILENAME_COLNAME + ", " +
                                  FILELENGTH_COLNAME + ", " + 
                                  FILECONTENTS_COLNAME + ") " +
                    "VALUES ( " + System.currentTimeMillis() + ", " +
                                  "'" + filename + "', " +
                                  fileBytes.length + ", " +
                                  "? )"; // ? indicates that the value is provided later
    LexURIFile.debug("MyDBUtil.writeRecord> sql[" + sql + "]");

    // Insert the record
    PreparedStatement stmt = dbConnection.prepareStatement(sql);
    InputStream in = new ByteArrayInputStream(fileBytes);
    stmt.setBinaryStream(1, in, (int)fileBytes.length);
    stmt.executeUpdate();
    stmt.close();
    in.close();
  }
  
  /**
   * Deletes a specific record out of the named table.
   * @param dbConnection  Connection to the database
   * @param tableName     Name of the table 
   * @param recIdToDelete Record ID of the recrod to delete
   * @throws Exception 
   */
  protected static void deleteRecord(Connection dbConnection, String tableName, long recIdToDelete) throws Exception {
    String sql = "DELETE FROM " + tableName
                    + " WHERE " + ID_COLNAME + " = " + recIdToDelete;
    LexURIFile.debug("MyDBUtil.deleteRecord> sql[" + sql + "]");
    
    Statement stmt = null;
    try {
      stmt = dbConnection.createStatement();
      int retVal = stmt.executeUpdate(sql);
      LexURIFile.debug("SQL (" + sql + ") return value=" + retVal);
    } catch (Exception ex) {
      LexURIFile.debug("SQL (" + sql + ") Exception", ex);
      throw ex;
    } finally {
      try {
        stmt.close();
      } catch (Exception ex) {
        LexURIFile.debug("stmt.close() Exception", ex);
      }
    }
    
  }

  /**
   * Reads a specific (or the first) record out of the database table.
   * @param dbConnection Connection to the database
   * @param tableName    Name of the table to read
   * @param recordID     Record ID of the record to read. If this is < 0, then
   *                     the first record will be read.
   * @return             Returns a
   *                     {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBItem}
   *                     object containing the data from the record.
   * @throws Exception 
   */
  protected static MyDBItem readRecord(Connection dbConnection, String tableName, long recordID) throws Exception {
    MyDBItem myDBItem = null;
    
    // Build the SQL statement to read a single record
    String sql;
    if (recordID >= 0) {
      sql = "SELECT " + ID_COLNAME + ", " +
                        FILETIME_COLNAME + ", " +
                        FILENAME_COLNAME + ", " +
                        FILELENGTH_COLNAME + ", " +
                        FILECONTENTS_COLNAME +
             " FROM " + tableName +
            " WHERE " + ID_COLNAME + " = " + recordID;
    } else {
      sql = "SELECT " + ID_COLNAME + ", " +
                        FILETIME_COLNAME + ", " +
                        FILENAME_COLNAME + ", " +
                        FILELENGTH_COLNAME + ", " +
                        FILECONTENTS_COLNAME +
             " FROM " + tableName +
         " ORDER BY " + ID_COLNAME +
            " LIMIT 1";
    }
    LexURIFile.debug("MyDBUtil.readRecord> sql[" + sql + "]");
    
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = dbConnection.createStatement();
      rs = queryDB(stmt, sql);
      if (rs != null && rs.next()) {
        long   dbRecordID      = rs.getLong(1);
        long   dbRecDateTimeMS = rs.getLong(2);
        String dbRecFilename   = rs.getString(3);
        long   dbRecFileLen    = rs.getLong(4);
        InputStream in = rs.getBinaryStream(5);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1)
          baos.write(c);
        byte[] dbRecFileBytes = baos.toByteArray();
        
        myDBItem = new MyDBItem(dbRecordID, dbRecFilename, dbRecDateTimeMS, dbRecFileLen, dbRecFileBytes);
        LexURIFile.debug("MyDBUtil.readRecord> " + myDBItem.toString());
      }
    } finally {
      try {
        rs.close();
      } catch (Exception ex) {
      }
      try {
        stmt.close();
      } catch (Exception ex) {
      }
    }
    return myDBItem;
  }
  
  /**
   * Returns an array of {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBItem}
   * objects which contain records matching <code>srchFilename</code> and/or
   * <code>srchRecordID</code>. If neither search criteria are specified, then
   * all records will be returned.
   * 
   * <p><b>Note:</b> The bytes of the database files will not be returned as
   * this is used for 'directory' listings.
   * 
   * @param dbConnection Connection to the database
   * @param tableName    Name of the table to query
   * @param srchFilename Filename to match or <code>null</code> for no filename
   *                     matching.
   * @param srchRecordID Database table record ID to match or -1 for no
   *                     record ID matching.
   * @return             An array of
   *                     {@link examples.URISchemeExamples.com.companyabc.mydb.MyDBItem}
   *                     objects matching the search criteria.
   * @throws Exception 
   */
  protected static MyDBItem[] getDBItems(Connection dbConnection,
                                         String tableName,
                                         String srchFilename,
                                         long srchRecordID) throws Exception {
    // Buid the SQL statement to select matching record(s)
    String sql = "SELECT " + ID_COLNAME + ", " +
                             FILETIME_COLNAME + ", " +
                             FILENAME_COLNAME + ", " +
                             FILELENGTH_COLNAME +
                   " FROM " + tableName;
    boolean whereAdded = false;
    if ((srchFilename != null) && (srchFilename.length() > 0)) {
      sql += " WHERE (" + FILENAME_COLNAME + " = '" + srchFilename + "')";
      whereAdded = true;
    }
    if (srchRecordID >= 0) {
      sql += (whereAdded) ? " AND " : " WHERE ";
      sql += "(" + ID_COLNAME + " = " + srchRecordID + ")";
    }
    sql += " ORDER BY " + ID_COLNAME;
    LexURIFile.debug("MyDBUtil.getDBItems> sql[" + sql + "]");
    
    ArrayList<MyDBItem> dbItems = new ArrayList<MyDBItem>();
    
    Statement stmt = null;
    ResultSet rs = null;
    try {
      // Execute the query
      stmt = dbConnection.createStatement();
      rs = queryDB(stmt, sql);
      // Loop through the results building the return array
      while (rs != null && rs.next()) {
        long   dbRecordID      = rs.getLong(1);
        long   dbRecDateTimeMS = rs.getLong(2);
        String dbRecFilename   = rs.getString(3);
        long   dbRecFileLen    = rs.getLong(4);
        MyDBItem myDBItem = new MyDBItem(dbRecordID, dbRecFilename, dbRecDateTimeMS, dbRecFileLen, null);
        LexURIFile.debug("MyDBUtil.getDBItems> " + myDBItem.toString());
        dbItems.add(myDBItem);
      }
    } finally {
      try {
        rs.close();
      } catch (Exception ex) {
      }
      try {
        stmt.close();
      } catch (Exception ex) {
      }
    }
    
    LexURIFile.debug("MyDBUtil.getDBItems> Found " + dbItems.size() + " item(s)");
    return dbItems.toArray(new MyDBItem[dbItems.size()]);
  }

  /**
   * Executes a SQL query and returns the ResultSet.
   * @param stmt Database connection Statement to use
   * @param sql  SQL to execute
   * @return     A ResultSet as the result of the query or <code>null</code>
   *             if an error occurred.
   */
  private static ResultSet queryDB(Statement stmt, String sql) {
    try {
      return stmt.executeQuery(sql);
    } catch (Exception ex) {
      LexURIFile.debug("MyDBUtil.queryDB> Exception during SQL [" + sql + "]", ex);
      return null;
    }
  }
  
}
