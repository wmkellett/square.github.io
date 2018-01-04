/*
getMailboxController.js

Demonstrates getMailboxController API method. 
*/
var mailboxController = ISessionScript.getMailboxController();
ISessionScript.logDetail("Mailbox packaging: " + mailboxController.getMailboxPackaging());