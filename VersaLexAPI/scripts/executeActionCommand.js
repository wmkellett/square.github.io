/*
executeActionCommand.js

Execute an action command which is passed in the first argument.
*/
var args = arguments
function executeCommand() {
  var i = 0;
  ISessionScript.logDetail("executeActionCommand.js executing: '" + args[i] + "'");
  var status =ISessionScript.executeActionCommand(args[i]);
  if (status) {
    return 0;
  } else {
    return status;
  }
}
executeCommand();
