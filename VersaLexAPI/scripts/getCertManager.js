/*
getCertManager.js

Demonstrates getCertManager API method. See API  documentation for ICertManagerRunTime available methods.
*/
var certManager = ISessionScript.getCertManager();
// certManager.getCAFilesAliases();
var certDetails = certManager.displayCertificate(false, "cleo-test.cer\\", false)
// Show details in debug
ISessionScript.debug("Certificate details: '" + certDetails);