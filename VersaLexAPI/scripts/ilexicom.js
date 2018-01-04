/*
lexiComFactory.js

This should fail. 
*/
var iLexiCom = com.cleo.lexicom.external.LexiComFactory.getCurrentInstance();
ISessionScript.logDetail("ILexiCom getVersion returned:" + iLexiCom.getVersion());
