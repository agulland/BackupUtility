/*
 * BackupEventListener.java
 *
 * Created on 21 October 2004, 18:19
 */

package com.gulland.altair;

/**
 * This interface is a listener for backup events. These events are currently
 * messages being raised in the BackupLogger. A listener added to the
 * BackupLogger will receive messages or event actions that it can then use, for
 * example, display the message in a window
 * 
 * @author agulland
 */
public interface BackupEventListener
{
	/**
	 * This method is executed by the BackupLogger whenever a message is being
	 * posted to the log file.
	 * 
	 * @param msg
	 *          the message being posted
	 */
	public void handleEvent(String msg);
}
