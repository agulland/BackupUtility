/**
 * BackupEventListener.java
 *
 * Who      When        What
 * -------- ----------  --------------------------------------------------------
 * agulland 21 Aug 2004 Class created
 * agulland 08 Feb 2010 Updated to include endLog method 
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
public interface LogListener
{
	/**
	 * This method is executed by the BackupLogger whenever a message is being
	 * posted to the log file.
	 * 
	 * @param msg
	 *          the message being posted
	 */
	public void writeLog(String msg);
	
	public void endLog(String msg);
}
