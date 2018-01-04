/*
sync.js

Demonstrates  creating a synchronized function (in the sense of a Java synchronized method) from an existing function.
*/
var o = { f : sync(function(x) {
	ISessionScript.logDetail("entry");
	Packages.java.lang.Thread.sleep(x*1000);
	ISessionScript.logDetail("exit");
	})
};
spawn(function() {o.f(5);});
spawn(function() {o.f(5);});
// Messages should be logged to action XML after this script has completed execution.
// Return 0
0
