/*
spawn.js

Demonstrates executing a function in a separate thread using the built-in spawn() function.
*/
var c = 3;
var d = 7;
var a = c;

function change()
{
 a = d;
 ISessionScript.logDetail("change() result:" + a);
 return a;
}
spawn(change);
ISessionScript.logDetail("'a': " + a + " should be equal to original value: " + c);
a = 4;
result = change();
ISessionScript.logDetail("'a': " + a + " should be equal to changed value: " + d);
// Return 0
a=0
