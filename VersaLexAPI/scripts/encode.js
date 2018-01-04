/*
encode.js

Demonstrates encode/decode API methods.
*/
var x = "String to encode";
var encoded = ISessionScript.encode(x);
var decoded = ISessionScript.decode(encoded);
ISessionScript.logDetail("'" + x + "' encoded: '" + encoded + "' decoded: '" + decoded);
