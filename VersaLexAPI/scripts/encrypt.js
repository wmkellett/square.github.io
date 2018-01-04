/*
encrypt.js

Demonstrates encrypt/deccrypt API methods.
*/
var x = "String to encrypt";
var encrypted = ISessionScript.encrypt(x);
var decrypted = ISessionScript.decrypt(encrypted);
ISessionScript.logDetail("'" + x + "' encrypted: '" + encrypted + "' decrypted: '" + decrypted);