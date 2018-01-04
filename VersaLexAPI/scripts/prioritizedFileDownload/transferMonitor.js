/**
 * Monitor the transfer rate
 */
importPackage(com.cleo.lexicom.external);
load("scripts\\higherPriorityState.js");

// start the transfer monitor
transferMonitor(arguments[0]);

/**
 * Start the transfer monitor.
 * @param minRate the minimum rate (Kb/sec)
 * @return 0 if successfully started transfer monitor thread
 */
function transferMonitor(minRate) {
  var action = ISessionScript.getActionController();
  // make sure can get mailbox w/o throwing exception
  action.getMailboxAlias();

  // if already at a higher priority state, then no need to monitor
  if (isHigherPriorityState())
    return;

  // get a database connection
  var connection = LexUtil.getDatabaseConnection();

  // spawn the monitor thread
  spawn(function () {transferMonitorThread(action, connection, minRate);});
  return 0;
}
/**
 * Monitor the transfer rate via the db for the action's active transfer.
 * If rate dips below minimum rate, then set higher priority state.
 * @param action the active IActionController
 * @param connection a database Connection object
 * @param minRate the minimum rate (Kb/sec)
 */
function transferMonitorThread(action, connection, minRate) {
  // select the time and bytes for the active transfer for this action
  var sql = "SELECT TransferTime, TransferBytes " +
              "FROM VLTransfers " +
             "WHERE Host = '" + action.getHostAlias() + "' " +
               "AND Mailbox = '" + action.getMailboxAlias() + "' " +
               "AND Action = '" + action.getAlias() + "' " +
               "AND Status = 'In Progress'";

  try {
    // keep monitoring until the action is done
    while (action.isRunning()) {
      ISessionScript.debug("transferMonitorThread(" + minRate + ")");
      var stmt = null;
      var rs = null;
      try {
        stmt = connection.createStatement();
        rs = stmt.executeQuery(sql);
        if (rs != null && rs.next()) {
          var time = rs.getFloat(1);
          var bytes = rs.getLong(2);
          ISessionScript.debug(" time=" + time + ",byte=" + bytes);
          // if its been transferring at least 2 seconds
          if (time >= 2) {
            // calculcate the transfer rate
            var kbsec = (bytes/1024.)/time;
            ISessionScript.debug(" kbsec=" + kbsec);
            // if below the minimum
            if (kbsec < minRate) {
              // set the high priority state
              setHigherPriorityState(1);
              break;
            }
          }
        }
      } finally {
        try {
          rs.close();
        } catch (ex) {
        }
        try {
          stmt.close();
        } catch (ex) {
        }
      }

      // wait one second between checks
      try {
        action.join(1000);
      } catch (ex) {
      }
    }
  } finally {
    // put the database connection back
    LexUtil.putDatabaseConnection(connection);
  }
}
