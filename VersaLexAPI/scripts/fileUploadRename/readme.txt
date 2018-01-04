This script contains three functions used in conjunction - clear(), add(), and send().  Their purpose along with the commands below are to autosend files to an FTP or SSH FTP server, but add a time/date stamp to the destination filename if the filename already exists on the server.

Here are the commands that could be placed in a auto-scheduled <send> action to make full use of this script:

# Clear the list of local files to send
SCRIPT scripts\fileUploadRename.js "Clear"

# For each file found by CHECK below add to the list of files to send
SET ExecuteOnCheckConditionsMet=$SCRIPT scripts\fileUploadRename.js "Add" "%file%"

# Set the date and time macros formats for the destination file needs to be renamed
SET MacroDateFormat=yyyyMMdd
SET MacroTimeFormat=HHmmssSSS

# Find files not ending in .tmp at least 10 seconds old
CHECK -FIL ..\outbox\[(?i)(?!(.*\.tmp$))(.*)] Age=>10S

# Send the local files found
SCRIPT scripts\fileUploadRename.js "Send"

