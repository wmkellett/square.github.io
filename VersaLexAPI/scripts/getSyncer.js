/*
getSyncer.js

Demonstrates getSyncer API method. 
*/
var syncer = ISessionScript.getSyncer();
importPackage(com.cleo.lexicom);
var result = syncer.isActiveVersalex(Sync.SCHEDULE, false);
ISessionScript.logDetail("Schedule is controlled by this VersaLex : " + result);