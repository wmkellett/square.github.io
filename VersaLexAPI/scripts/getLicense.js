/*
getLicense.js

Demonstrates getLicense API method. 
*/
var license = ISessionScript.getLicense();
ISessionScript.logDetail("License serial number: " + license.getSerialNumber());