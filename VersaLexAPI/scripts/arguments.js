/*
arguments.js

Print global arguments (Strings) to debug and return the first argument.
*/
var args = arguments
for (var i=0; i < arguments.length; i++)
{
  // Use predefined print function to print each argument to debug
  ISessionScript.debug(args[i]);
}
// Return the 1st argument
args[0];
