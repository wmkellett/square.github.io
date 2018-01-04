/**
 * FTP or SSH FTP client nested, sorted file download
 */
importPackage(com.cleo.lexicom.external);
importPackage(java.io);
importPackage(java.util);

// download files
onlyDownloadNewOrChangedFiles();

/**
 * Download full directory tree, retaining directory structure locally. Download largest files first.
 * Only download if the file is new or has changed.
 * @return 0 on success
 */
function onlyDownloadNewOrChangedFiles() {
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
        // get the saved list for comparison
        var dir = new File("lists");
        if (!dir.exists())
          dir.mkdir();
        var file = new File(dir, action.getHostAlias() + "_" + action.getMailboxAlias() + "_" + action.getAlias() + ".list");
        var savedList = new ArrayList();
        // if previous saved list, deserialize it
        var j;
        if (file.exists()) {
          savedList = deserialize(file.getPath());
          for (j = 0; j < savedList.size(); j++) {
            var savedEntry = savedList.get(j);
            ISessionScript.debug("onlyDownloadNewOrChangedFiles() saved=" + savedEntry.relativePath() + ",length=" + savedEntry.length() + ",date=>" + savedEntry.date());
          }
        }

        var options = java.lang.reflect.Array.newInstance(java.lang.String, 1);
        options[0] = "-UNI";

        // download each file one-by-one if it is new or changed
        var i;
        for (i = 0; i < list.length; i++) {
          var download = true;
          for (j = 0; j < savedList.size(); j++) {
            var savedEntry = savedList.get(j);
            // if downloaded before
            if (savedEntry.relativePath().equals(list[i].relativePath())) {
              // if hasn't changed
              if (savedEntry.length() == list[i].length() &&
                  savedEntry.date().equals(list[i].date())) {
                download = false;
              // if has changed
              } else {
                savedList.remove(j);
              }
              break;
            }
          }
          
          if (download) {
            ISessionScript.debug("onlyDownloadNewOrChangedFiles() newOrUpdated=" + list[i].relativePath() + ",length=" + list[i].length() + ",date=>" + list[i].date());
            // retain the directory structure locally by building the absolute inbox path
            var destfile = new File(ISessionScript.getAbsolute(ISessionScript.applyDefaultBox(list[i].relativePath(), true)));
            // make sure the subdirectories exist
            destfile.getParentFile().mkdirs();
            // download the file
            if (!action.get(options, list[i].relativePath(), destfile, null)) {
              break;
            }
            // add to the saved list and serialize after each download
            savedList.add(list[i]);
            serialize(savedList, file.getPath());
          }
        }
        if (i == list.length)
          retval = 0;
      }
    }
    return retval;
  
  } finally {
    // end the action controller
    action.end();
  }
}
