/*
fileDelete.js

Returns 0 if the filename specified in the 1st argument exists was deleted sucessfully.
*/
function fileDelete(filename) {
 var file = new java.io.File(filename);
 if (filename.equalsIgnoreCase("%destfile%")) {
    ISessionScript.logDetail("fileDelete.js ignoring filename: '" + filename + "' ");
 } else if (file.exists()) {
    // Following syntax avoids conflict with java.io.File.delete() and the "delete"  Javascript keyword
   if (!file["delete"]()) {
    ISessionScript.logDetail("fileDelete.js filename: '" + filename + "' could not be deleted.");
    return 2;
   }
 } else {
    ISessionScript.logDetail("fileDelete.js filename: '" + filename + "' does not exist.");
   return 1;
 }
 return 0;
}
fileDelete(arguments[0]);