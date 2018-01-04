/**
 * FTP or SSH FTP client nested, sorted, multiple session file download
 */
importPackage(com.cleo.lexicom.external);
importPackage(java.io);
importPackage(java.util.concurrent);

// download files
multipleSessionFileDownload(arguments[0]);

/**
 * Download full directory tree, retaining directory structure locally. Download largest files first.
 * @param byteThreshold available bytes threshold for starting a second download action
 * @return 0 on success
 */
function multipleSessionFileDownload(byteThreshold) {
  // get the active action
  var action = ISessionScript.getActionController();

  try {
    var retval = -1;
    // get a directory controller
    var idirectory = action.getDirectoryController();
    if (idirectory != null) {
      // get the list of available files sorted from largest to smallest
      var filter = new IDirectoryFilter({ include: function(entry) {return entry.isFile();}});
      var list = idirectory.list(null, true, filter, idirectory.SIZE_DESCENDING);
      if (list != null) {
        // create the queue of available files and add up the total bytes
        var queue = new ConcurrentLinkedQueue();
        var totalbytes = 0;
        var i;
        for (i = 0; i < list.length; i++) {
          totalbytes += list[i].length();
          queue.add(list[i].relativePath());
        }
        ISessionScript.debug("multipleSessionFileDownload() files=" + list.length + " total bytes=" + totalbytes + " byteThreshold=" + byteThreshold);
        
        // if total number of bytes available is more than threshold and there's at least two files
        // create another action that downloads files concurrently with this one
        if (list.length > 1 && totalbytes > byteThreshold) {
          spawn(function() {var anotherAction = action.getAnotherActionController();
                            try {
                              getFiles(action, anotherAction, queue);
                            } finally {
                              anotherAction.end()
                            }});
        }
        retval = getFiles(action, action, queue);
      }
    }

    // action controller API already performed ExecuteOnFail
    // clear now so that script return errorcode doesn't repeat it
    if (retval == -1) {
      action.execute("CLEAR ExecuteOnFail");
    }
    return retval;
  
  } finally {
    // end the action controller
    action.end();
  }
}

/**
 * Download queued files one-by-one.
 * @param parentAction parent action that has to be running
 * @param action an active IActionController
 * @param queue ConcurrentLinkedQueue containing available file paths
 * @return 0 on success
 */
function getFiles(parentAction, action, queue) {
  var options = java.lang.reflect.Array.newInstance(java.lang.String, 2);
  options[0] = "-UNI";
  options[1] = "-DEL";

  // pull next file off of queue one-by-one and download
  var file = queue.poll();
  while (parentAction.isRunning() && file != null) {
    ISessionScript.debug("multipleSessionFileDownload() action=" + action.getAlias() + " file=" + file);
    // retain the directory structure locally by building the absolute inbox path
    var destfile = new File(ISessionScript.getAbsolute(ISessionScript.applyDefaultBox(file, true)));
    // make sure the subdirectories exist
    destfile.getParentFile().mkdirs();
    if (!action.get(options, file, destfile, null))
      return -1;
    file = queue.poll();
  }
  return 0;
}
