/*
activeIO.js

Demonstrates activeIO API method.
*/
var filename = "outbox\\test\\test.edi";
var isActiveIO = ISessionScript.activeIO(filename);
ISessionScript.logDetail("'" + filename + "' is currently being input or output:" + isActiveIO);
