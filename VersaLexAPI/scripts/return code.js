/*
return code.js

Example action command: SCRIPT [SuccessCodes=3] "scripts\return code.js"
*/
// Set to a different value to observe a failure
var result = 3;
function getReturnCode() {
  return result;
}
// Execute function and return its value
getReturnCode()