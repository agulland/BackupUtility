/** BackupTask.java
 *
 * Created on 08 August 2004, 17:28
 * 
 * TODO add an override for equals method based on rule id? and therefore 
 * enforce uniqueness of rule names?
 */

package com.gulland.altair;

import java.io.File;

/**
 * <p>
 * Defines a single backup task which is to define the backup of a source folder
 * to a destination folder based on a backup rule. The backup rules are defined
 * as follows,
 * </p>
 * 
 * <ul>
 * <li>BackupTask.ALL - all files will be copied from source to destination
 * <li>BackupTask.EXISTS - only copy files that already exist in destination
 * <li>BackupTask.CHANGED - only copy only source files whose timestamp is
 * different from destination OR if destination does not exist
 * <li>BackupTask.EXISTS_CHANGED - only copy source files whose timestamp is
 * different from destination AND file exists on destination
 * <li>BackupTask.NEW - only copy files that dont already exist on destination
 * </ul>
 * 
 * @author agulland
 */
public class BackupTask
{
	/** Holds value of property id. */
	private String id;

	/** Holds value of property source. */
	private String source;

	/** Holds value of property destination. */
	private String destination;

	/** Holds value of property rule. */
	private int rule;

	/** Holds value of property active. */
	private boolean active;

	/** Holds value of property recurse. */
	private boolean recurse;

	/** Holds value of property mirrorDelete. */
	private boolean mirrorDelete;

	/** summary metric to hold sum metric of all sub folders */
	private BackupMetric summaryMetric;

	/**
	 * Value for backup event rule. Copies all files to destination and replaces
	 * any existing files
	 */
	public static int ALL = 0;

	/**
	 * Value for backup event rule. Copies all files to destination whose time
	 * stamp is different to the same file on destination
	 */
	public static int CHANGED = 1;

	/**
	 * Value for backup event rule. Copies all files that already have a copy on
	 * destination
	 */
	public static int EXISTS = 2;

	/**
	 * Value for backup event rule. Copies all files that already have a copy on
	 * destination and only if time stamp is different
	 */
	public static int EXISTS_CHANGED = 3;

	/**
	 * Value for backup event rule. only copies files that don’t already exist on
	 * destination
	 */
	public static int NEW = 4;

	/**
	 * Array of rule names
	 */
	private static String[] ruleNames = new String[] { "all", "changed",
			"exists", "exists changed", "new" };

	/** Creates a new BackupTask object */
	public BackupTask() {
		this("", "", BackupTask.CHANGED);
	}

	/**
	 * Creates a new BackupTask object based on the given parameters
	 * 
	 * @param source
	 *          the source file to backup
	 * @param destination
	 *          the destination folder to where the source is backed up to
	 * @param rule
	 *          a rule that applies to the backup event
	 */
	public BackupTask(String source, String destination, int rule) {
		this.id = "new task";
		this.source = source;
		this.destination = destination;
		this.active = true;
		this.recurse = true;
		this.mirrorDelete = false;
		this.rule = rule;
	}

	/**
	 * Returns the task id
	 * 
	 * @return this tasks id.
	 */
	public String getID() {
		if ((this.id == null) || (this.id.equals(""))) return "no rule ID";
		else return this.id;
	}

	/**
	 * Sets the task id.
	 * 
	 * @param id
	 *          name for task
	 */
	public void setID(String id) {
		this.id = id;
	}

	/**
	 * Returns the source folder for the task
	 * 
	 * @return source folder.
	 */
	public String getSource() {
		return this.source;
	}

	/**
	 * Sets the source folder for the task
	 * 
	 * @param source
	 *          the source folder.
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Getter for property destination.
	 * 
	 * @return Value of property destination.
	 */
	public String getDestination() {
		return this.destination;
	}

	/**
	 * Setter for property destination.
	 * 
	 * @param destination
	 *          New value of property destination.
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * Returns the value of the rule for the task
	 * 
	 * @return Value of property rule.
	 */
	public int getRule() {
		return this.rule;
	}

	/**
	 * Sets the rule for the task
	 * 
	 * @param rule
	 *          New value of property rule.
	 */
	public void setRule(int rule) {
		this.rule = rule;
	}

	/**
	 * Returns the rule as a text string
	 * 
	 * @return the rule as a String
	 */
	public String getRuleText() {
		return BackupTask.getRuleName(this.rule);
	}

	/**
	 * Returns a boolean value to indicate whether this event is active
	 * 
	 * @return Value of property active.
	 */
	public boolean isActive() {
		return this.active;
	}

	/**
	 * Setter for property active.
	 * 
	 * @param active
	 *          New value of property active.
	 * 
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Returns a boolean value to indicate whether this event should recurse
	 * sub directories of the source folder.
	 * 
	 * @return Value of property recurse.
	 */
	public boolean isRecurse() {
		return this.recurse;
	}

	/**
	 * Setter for property recurse.
	 * 
	 * @param recurse
	 *          New value of property recurse.
	 */
	public void setRecurse(boolean recurse) {
		this.recurse = recurse;
	}

	/**
	 * Returns a boolean value indicating whether this event deletes files on
	 * destination that don't exist in source
	 * 
	 * @return Value of property mirrorDelete.
	 */
	public boolean isMirrorDelete() {
		return this.mirrorDelete;
	}

	/**
	 * Setter for property mirrorDelete.
	 * 
	 * @param mirrorDelete
	 *          New value of property mirrorDelete.
	 */
	public void setMirrorDelete(boolean mirrorDelete) {
		this.mirrorDelete = mirrorDelete;
	}

	/**
	 * Checks the source folder if looks like a valid folder and if it exists
	 * 
	 * @return a string with a failure message otherwise an empty string
	 */
	public String validateSource() {
		File f = new File(this.getSource());
		if (this.getSource().equals("")) return "Source folder is empty.";
		else if (!f.isAbsolute()) return "Source folder doesn't appear to be a valid folder.";
		else if (!f.exists()) return "Source folder can't be found.";
		else return "";
	}

	/**
	 * Checks the source folder if looks like a valid folder and if it exists
	 * 
	 * @return a string with a failure message otherwise an empty string
	 */
	public String validateDestination() {
		// TODO destination mustn't be a file?
		File f = new File(this.getDestination());
		if (this.getDestination().equals("")) return "Destination folder is empty.";
		else if (!f.isAbsolute()) return "The Destination folder doesn't appear to be a valid folder.";
		else if (!f.exists()) return "Destination folder can't be found";
		else return "";
	}

	/**
	 * Adds a metric to the summary metric for this Task
	 * 
	 * @param metric
	 *          the metric whose data you wish to add to this metrics data
	 */
	public void addMetric(BackupMetric metric) {
		this.summaryMetric.addMetric(metric);
	}

	/**
	 * Utility method to return a text description of a given rule
	 * 
	 * @param rule
	 *          a rule value
	 * @return the name of the rule
	 */
	public static String getRuleName(int rule) {
		if (rule < ruleNames.length) return ruleNames[rule];
		else return "unknown value";
	}

	/**
	 * Returns an array of rule names
	 * 
	 * @return the array of rule names
	 */
	public static String[] getRuleNames() {
		return BackupTask.ruleNames;
	}

	/**
	 * Overrides toString
	 */
	public String toString() {
		return "[id: " + this.id + ", source: " + this.source + ", dest: "
				+ this.destination + ", rule: " + this.rule + "]";
	}
}
