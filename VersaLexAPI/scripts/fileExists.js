/*
fileExists.js

Returns 0 if the filename specified in the 1st argument exists or -1 if it does not.
*/
function fileExists(filename) {
 var file = new java.io.File(filename);
 if (file.exists()) {
   return 0;
 } else {
   return 1;
 }
}
fileExists(arguments[0]);