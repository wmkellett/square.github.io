/*
getActionController.js

Demonstrates getActionController API method. See API  documentation for IActionController available methods.
*/
var actionController = ISessionScript.getActionController();
var actionAlias = actionController.getAlias();
ISessionScript.logDetail("Currently running action: '" + actionAlias + "'");