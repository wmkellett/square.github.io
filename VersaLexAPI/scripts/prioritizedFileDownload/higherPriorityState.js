/**
 * Methods for checking and setting a higher priority state.
 */
importPackage(com.cleo.lexicom.external);
/**
 * (Re)set the higher priority state
 * @param state 0 if reverting to normal state; otherwise setting higher priority state
 * @return 0 if successfully set state
 */
function setHigherPriorityState(state) {
  ISessionScript.debug("setHigherPriorityState(" + state + ")");
  var options = ISessionScript.getOptions();
  var percentage = null;
  // normal state
  if (state == 0) {
    percentage = "75";
  // higher priority state
  } else {
    percentage = "90";
  }
  options.setOther(options.HighPriorityTransfersPercentageAvailableBandwidth, percentage);
  options.save();
  return 0;
}
/**
 * Check if at higher priority state
 * @return true if at higher priority state; otherwise false
 */
function isHigherPriorityState() {
  var options = ISessionScript.getOptions();
  var value = options.getOther(options.HighPriorityTransfersPercentageAvailableBandwidth);
  var retval = (value == "90");
  ISessionScript.debug("isHigherPriorityState()=" + retval);
  return retval;
}