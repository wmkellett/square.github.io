/*
fileCompare.js

Returns 0 if the files specified in the 1st two arguments have the same binary content.
Requires org.apache.commons.io.FileUtils (lib/ws/commons-io.jar) 
*/
importPackage(org.apache.commons.io);
function fileCompare(filename1, filename2) {
 var file1 = new java.io.File(filename1);
 var file2 = new java.io.File(filename2);
 if (FileUtils.contentEquals(file1, file2)) {
   return 0;
 } else {
   return 1;
 }
}
fileCompare(arguments[0], arguments[1]);