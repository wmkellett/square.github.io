The four scripts in this folder are:

	transferMonitor.js 		- Monitor the transfer rate
	higherPriorityState.js 		- Methods for checking and setting a higher priority state
	setHigherPriorityState.js 	- (Re)set the higher priority state
	multipleSessionFileDownload.js 	- FTP or SSH FTP client nested, sorted, multiple session file download


These scripts were designed to be used as a group.  Their general purpose is to place a higher priority on files being downloaded from a certain trading partner's FTP or SSH FTP server.  Higher priority is imposed in two different ways:

1) increasing the system "High Priority Transfers Percentage Available Bandwidth" if the transfer rate is "slow" or if there is a failure

2) starting a second, concurrent download session depending on how much payload is available

Certainly only 1) or 2) could be imployed.  In fact, 2) alone may be more than adequate - especially given the difficulty in determining the value that represents the "slow" transfer rate threshold.

Here are the commands that could be placed in a <receive> action to make full use of these scripts:

# Make sure downloads are treated as high priority
SET HighPriority=Incoming

# Monitor active transfer rate and set higher priority
# state if rate dips below 300 Kb/sec
SCRIPT -HALT scripts\transferMonitor.js 300

# If failure, set higher priority state for next attempt
SET ExecuteOnFail=$SCRIPT scripts\setHigherPriorityState.js 1

# Receive all files from remote host, starting a second download
# session if more than 50K bytes are available
# (replaces GET –REC –UNI -DEL *)
SCRIPT -HALT scripts\multipleSessionFileDownload.js 50000

# Reset higher priority state back to normal
SCRIPT scripts\setHigherPriorityState.js 0
