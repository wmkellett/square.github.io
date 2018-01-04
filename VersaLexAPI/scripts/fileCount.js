/*
fileCount.js

Returns the number of files matching the regular expression specified (1st argument) in the folder (second argument)
*/
importPackage(java.io)
// Use JavaAdapter to implement java.io.FileFilter interface
var fileFilter = new JavaAdapter(FileFilter, {
	accept : acceptFunction
	}
);

// Implementation of java.io.FileFilter.accept(File f)
function acceptFunction(f) {
  var fileName = f.getName();
  var matches = fileName.matches(regex);
  if (matches) {
    ISessionScript.logDetail("fileCount.js acceptFunction matched:" + fileName);
  }
  return matches;
}   

function listFiles(directoryName) {
  var directory = new java.io.File(directoryName);
  // File.listFiles() returns an array. Retrun the length of this array.
  return directory.listFiles(fileFilter).length;
}
var regex = arguments[0];
listFiles(arguments[1]);