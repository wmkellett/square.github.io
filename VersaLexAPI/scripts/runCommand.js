/*
runCommand.js

Demonstrates runCommand() built-in function. 
*/
// Execute the specified command with the given argument and options
// as a separate process and return the exit status of the process.
// Console should show date and result should be 0.
runCommand("cmd", "/C", "date /T");
