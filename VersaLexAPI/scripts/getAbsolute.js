/*
getAbsolute.js

Demonstrates getAbsolute API methods.
*/
var filename = "outbox\\test\\test.edi";
// String method
var absoluteString = ISessionScript.getAbsolute(filename);
ISessionScript.logDetail("'" + filename + "' absolute path: '" + absoluteString);
// File method
importPackage(java.io);
var file = new File(filename);
absoluteFilePath = ISessionScript.getAbsolute(file).getAbsolutePath();
ISessionScript.logDetail("'" + filename + "' absolute file path: '" + absoluteFilePath);
