/*
getProperty.js

Demonstrates getProperty API method. 
*/
importPackage(com.cleo.lexicom.external);
var hostAlias = ISessionScript.getProperty(ILexiCom.HOST, "alias");
var mailboxAlias = ISessionScript.getProperty(ILexiCom.MAILBOX, "alias");
var actionAlias = ISessionScript.getProperty(ILexiCom.ACTION, "alias");
ISessionScript.logDetail("Alias host: '" + hostAlias[0] + "' mailbox: '" + mailboxAlias[0] + "' action: '" + actionAlias[0] + "'");
