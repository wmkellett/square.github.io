/*
test return code.js

Example action command: SCRIPT [SuccessCodes=3] "scripts\test return code.js" 3
*/
var result = arguments[0];
function getReturnCode() {
  return result;
}
// Execute function and return its value
getReturnCode()