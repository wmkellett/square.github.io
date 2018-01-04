/*
version.js

Demonstrates  version() built-in function. Version numbers logged to action XML log.
*/
ISessionScript.logDetail("JavaScript version:" + version());
// Set the version to 1.0
version(100)
ISessionScript.logDetail("JavaScript version:" + version());
