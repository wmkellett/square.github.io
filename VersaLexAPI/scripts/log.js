/*
log.js

Log global arguments (Strings) to XML log.
Example execute on setting: $SCRIPT scripts\log.js "%sourcefile%" %date% %index% "%host%" %status% %transferid%
*/
var args = arguments
for (var i=0; i < arguments.length; i++)
{
  // Use API log method to show each argument
  ISessionScript.logDetail("log.js arg[" + i + "]:" + args[i]);
}
// Log a colored messge to a different level.
ISessionScript.logDetail("log.js red message - low", 1, "red" );
ISessionScript.logDetail("log.js orange message - medium", 2, "orange" );
ISessionScript.logDetail("log.js black message - high", 3, "black" );
