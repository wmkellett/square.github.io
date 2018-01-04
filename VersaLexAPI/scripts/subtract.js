/*
subtract.js

Perform simple math operations, file loading, writing to debug file
*/
function subtract (a, b)
{
 var difference = a - b;
 // Show the result in the debug file
 ISessionScript.debug("function subtract:" + difference);
 return difference;
}
var difference = subtract(8,5);
// Load a second script which includes the add() function
load("scripts\\add.js");
// Return the result of the add() function
add(1,difference);
