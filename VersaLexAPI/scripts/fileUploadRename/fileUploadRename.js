/**
 * FTP or SSH FTP client file upload, checking for file existence
 * and adding a date/time stamp if necessary
 */
importPackage(com.cleo.lexicom.external);
importPackage(java.lang);
importPackage(java.util);
importPackage(java.io);

if (arguments[0] == "Clear")
  clear();
else if (arguments[0] == "Add")
  add(arguments[1]);
else if (arguments[0] == "Send")
  send();

// clear the list of local files to send
function clear() {
  System.setProperty(getPropertyName(), "");
}

// add to the list of local files to send
function add(file) {
  var name = getPropertyName();
  var llist = System.getProperty(name);
  if (llist.length() > 0)
    llist += "\n";
  llist += file;
  System.setProperty(name, llist);
}

// send the local files
function send() {
  // get the list of local files to send
  var llist = System.getProperty(getPropertyName());
  if (llist.length() == 0)
    return 0;
  
  var alist = new String(llist).split("\n");
  var retval = -1;

  // get the active action
  var action = ISessionScript.getActionController();

  try {
    // get a directory controller
    var idirectory = action.getDirectoryController();
    if (idirectory != null) {
      // get the list of files on the server
      var filter = new IDirectoryFilter({ include: function(entry) {return entry.isFile();}});
      var rlist = idirectory.list(null, false, filter, idirectory.NONE);
      // delete the local file as part of the PUT
      var options = java.lang.reflect.Array.newInstance(java.lang.String, 1);
      options[0] = "-DEL";

      // upload each file one-by-one
      var i;
      for (i = 0; i < alist.length; i++) {
        var sourcefile = new File(alist[i]);
        var destfile = sourcefile.getName();
        var j;
        for (j = 0; rlist != null && j < rlist.length; j++) {
          // if the filename already exists on the server, add a date/time stamp
          if (rlist[j].name() == sourcefile.getName()) {
            destfile = sourcefile.getName() + ".%date%%time%";
            break;
          }
        }

        // upload the file
        if (!action.put(options, sourcefile, destfile, null)) {
          break;
        }
      }
      if (i == alist.length)
        retval = 0;
    }
    return retval;
  } finally {
    // end the action controller
    action.end();
  }
}

function getPropertyName() {
  var action = ISessionScript.getActionController();
  return "Cleo.VLTrader." + 
         action.getHostAlias() + "." +
         action.getMailboxAlias() + "." +
         action.getAlias() + ".LocalFileList";
}

