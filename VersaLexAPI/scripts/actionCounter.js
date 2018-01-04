/*
actionCounter.js

Simple counter using a system property to store the current value.
*/
function count(count) {
  if (count==0) {
  
    // Reset timer by updating a system property
	java.lang.System.setProperty("stopWatch.seconds", currentTime);
  }  
  var elapsedSeconds = currentTime - java.lang.System.getProperty("stopWatch.seconds");
  if (elapsedSeconds > 0) {
   ISessionScript.logDetail("Elapsed time:" + elapsedSeconds + " (seconds)");
  }
  return toint32(elapsedSeconds);
}
count(arguments[0]);
