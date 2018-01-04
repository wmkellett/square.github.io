/*
getRelative.js

Demonstrates getRelative API methods.
*/
var filename = "outbox\\test\\test.edi";
// String method
var relativeString = ISessionScript.getRelative(filename);
ISessionScript.logDetail("'" + filename + "' relative path: '" + relativeString);
// File method
importPackage(java.io);
var file = new File(filename);
relativeFilePath = ISessionScript.getRelative(file).getPath();
ISessionScript.logDetail("'" + filename + "' relative file path: '" + relativeFilePath);
