/*
fileSize.js

Returns the file size (integer) of the filename specified in the 1st argument
*/
function fileSize(filename) {
 var file = new java.io.File(filename);
 return file.length();
}
var args = arguments;
fileSize(args[0]);
