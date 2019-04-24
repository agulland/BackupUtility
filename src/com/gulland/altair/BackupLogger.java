/**
 * BackupLogger.java
 * 
 * Who      When        What
 * -------- ----------  --------------------------------------------------------
 * agulland 21 Oct 2004 Class created
 * agulland  4 Feb 2010 Added new log level of TRACE - most detailed and should 
 *                      only be used for debugging
 * agulland 08 Feb 2010 Added isValidLogLevel utility method
 *                      Added endLog method                      
 */

package com.gulland.altair;

import java.util.*;

/**
 * <p>
 * Creates log message. Listeners are used to output this these messages to log
 * files or to screen.
 * </p>
 * 
 * <p>
 * Currently supports 3 Levels of logging:
 * </p>
 * 
 * <ul>
 * <li>Warn - highest level. outputs warnings only</li>
 * <li>Info - lower level, more detailed information plus warnings</li>
 * <li>Detail - lowest level. Most detailed information plus info plus warnings</li>
 * </ul>
 * 
 * @author agulland
 */
public class BackupLogger
{
	/** the level of logging. By default it is set to INFO */
	private int logLevel = BackupLogger.INFO;

	/** log level of TRACE is used for debugging */
	public static int TRACE = 0;
	/** a log level of DETAIL produces detail on backup process */
	public static int DETAIL = 1;
	/** a log level of INFO produces detail on parsing script file */
	public static int INFO = 2;
	/** log level of WARN is used for warnings - file not found etc */
	public static int WARN = 3;

	/** holds the application wide logger */
	private static BackupLogger logger;

	/** an array of backup event listeners */
	Vector<LogListener> eventListeners = new Vector<LogListener>();

	/** a map of available log levels */
	private static Map<Integer, BackupLogLevel> LogLevels = new HashMap<Integer, BackupLogLevel>();

	/**
	 * Constructor
	 */
	private BackupLogger() {
		this.logLevel = BackupLogger.INFO;

		// Build collection of available log levels
		LogLevels.put(0, new BackupLogLevel("Trace", "Provides the most detailed information, typically only used for testing and debugging."));
		LogLevels.put(1, new BackupLogLevel("Detail", "Provides detailed log information, listing each file backed up."));
		LogLevels.put(2, new BackupLogLevel("Info", "Provides summary information of a backup process such as number of files backedup and duration."));
		LogLevels.put(3, new BackupLogLevel("Warn", "Only logs any errors such as problems reading or writing files."));
	}

	/**
	 * Instantiates and returns a new logger or returns an existing logger
	 */
	public static BackupLogger getLogger() {
		if (BackupLogger.logger == null) BackupLogger.logger = new BackupLogger();
		return BackupLogger.logger;
	}

	/**
	 * Writes a warning message to the log file
	 * 
	 * @param msg
	 *          the warning message to be written
	 */
	public void warn(String msg) {
		if (this.logLevel <= BackupLogger.WARN) {
			write("WARN " + msg);
			System.out.println("WARN " + msg);
		}
	}

	/**
	 * Writes an info message to the log file
	 * 
	 * @param msg
	 *          the message to be written
	 */
	public void info(String msg) {
		if (this.logLevel <= BackupLogger.INFO) {
			write("INFO " + msg);
			System.out.println("INFO " + msg);
		}
	}

	/**
	 * Writes a detail message to the log file
	 * 
	 * @param msg
	 *          the message to be written
	 */
	public void detail(String msg) {
		if (this.logLevel <= BackupLogger.DETAIL) {
			write("DETL " + msg);
			System.out.println("DETL " + msg);
		}
	}

	/**
	 * Writes a detail message to the log file
	 * 
	 * @param msg
	 *          the message to be written
	 */
	public void trace(String msg) {
		if (this.logLevel <= BackupLogger.TRACE) {
			write("TRCE " + msg);
			System.out.println("TRCE " + msg);
		}
	}

	/**
	 * Sets the log level. This will override the default value defined by the
	 * applications preferences. If not set the default value is taken.
	 * 
	 * @param level
	 *          a valid log level: BackupLogger.INFO, BackupLogger.DETAIL,
	 *          BackupLogger.WARN
	 */
	public void setLogLevel(int level) {
		// only change if a valid level
		if (isValidLogLevel(level)) this.logLevel = level;
	}

	/**
	 * Returns log level that the logger is set to
	 * 
	 * @return int the log level
	 */
	public int getLogLevel() {
		return this.logLevel;
	}

	/**
	 * Returns a log level name for a given log level value
	 * @param level the log level you want the name for
	 * @return name of a log level
	 */
	public static String getLogLevelName(int level) {
		if( BackupLogger.LogLevels.containsKey(level) ) {
			return BackupLogger.LogLevels.get(level).logLevelName;
		} else {
			return "Unknown";
		}
	}

	/**
	 * Returns a description for a given log level value
	 * @param level the log level you want the description for
	 * @return String a log level description.
	 */
	public static String getLogLevelDescription(int level) {
		if( BackupLogger.LogLevels.containsKey(level) ) {
			return BackupLogger.LogLevels.get(level).logLevelDescription;
		} else {
			return "Unknown";
		} 
	}
	
	/**
	 * Returns number of log levels. Note, the log levels are zero based
	 * So if this returns 4 then we have log levels 0,1,2 and 3
	 * @return number of log levels
	 */
	public static int getNumberLogLevels() {
		return BackupLogger.LogLevels.size();
	}
	
	/**
	 * Returns true if a given log level is one of the allowed log levels. Use
	 * this to establish if a log level is valid before setting
	 * 
	 * @param level
	 * @return true if level is valid otherwise false
	 */
	public static boolean isValidLogLevel(int level) {
		if ((level == BackupLogger.INFO) || (level == BackupLogger.DETAIL)
				|| (level == BackupLogger.WARN) || (level == BackupLogger.TRACE)) {
			return true;
		} else return false;
	}

	/**
	 * Add a new BackupEvenetListner to the logger
	 * 
	 * @param listener
	 *          the BackupEvenetListner to be added
	 */
	public void addListener(LogListener listener) {
		this.eventListeners.add(listener);
	}

	/**
	 * Removes an event BackupEventListner
	 * 
	 * @param listener
	 *          the BackupEventListner to be removed
	 */
	public void removeListener(LogListener listener) {
		this.eventListeners.remove(listener);
	}

	/**
	 * writes a message to the listeners
	 * 
	 * @param msg
	 *          the message to be written
	 */
	private void write(String msg) {
		Iterator<LogListener> listeners = this.eventListeners.iterator();
		while (listeners.hasNext()) {
			listeners.next().writeLog(msg);
		}
	}

	/**
	 * calls the end log method on listeners so that they can write appropriate
	 * data to the end of the log
	 */
	public void endLog(String msg) {
		Iterator<LogListener> listeners = this.eventListeners.iterator();
		while (listeners.hasNext()) {
			listeners.next().endLog(msg);
		}
		System.out.println(msg);
	}

	/**
	 * Inner class that provides a Log Level object that is used to describe
	 * available log levels
	 * 
	 * @author Alastair
	 */
	public class BackupLogLevel
	{
		public String logLevelName;
		public String logLevelDescription;

		public BackupLogLevel(String name, String description) {
			logLevelName = name;
			logLevelDescription = description;
		}

	}

}