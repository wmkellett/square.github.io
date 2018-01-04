/*
getLicenser.js

Demonstrates getLicenser API method. 
*/
var licenser = ISessionScript.getLicenser();
var license = ISessionScript.getLicense();
var registrationInfo = licenser.registrationQuery(license.getSerialNumber());
// Required step if query() method successfully called
licenser.cancel();
ISessionScript.logDetail("License company name: " + registrationInfo.getCompany());