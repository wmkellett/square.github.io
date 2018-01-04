/*
hasProperty.js

Demonstrates hasProperty API method. 
*/
importPackage(com.cleo.lexicom.external);
var hostHasAlias = ISessionScript.hasProperty(ILexiCom.HOST, "alias");
var mailboxHasAlias = ISessionScript.hasProperty(ILexiCom.MAILBOX, "alias");
var actionHasAlias = ISessionScript.hasProperty(ILexiCom.ACTION, "alias");
ISessionScript.logDetail("Alias exists(?) for host: '" + hostHasAlias + "' mailbox: '" + mailboxHasAlias + "' action: '" + actionHasAlias + "'");