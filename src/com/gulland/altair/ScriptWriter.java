/**
 * ScriptWriter.java
 *
 * Who      When        What
 * -------- ----------  --------------------------------------------------------
 * agulland 23 Aug 2004 Class created
 * agulland 08 Feb 2010 Updated class to now write out log level and log folder
 *                      to script file.
 *                      Replaced any valid logging with system.out.println     
 */

package com.gulland.altair;

import java.io.*;

/**
 * <p>
 * This class is used to create a backup script file from a series of defined
 * BackupTask objects.
 * </p>
 * 
 * <p>
 * The constructor takes an array of BackupTask objects as a parameter and it is
 * these backup tasks that are written to the file. Once the class has be
 * initialised the script file can be created using the
 * <code>ScriptWriter.write</code> method which takes a File object as a
 * parameter which is the file that the backup tasks are written to. An example,
 * </p>
 * 
 * <pre>
 * File f = new File(&quot;f:/mytest.xml&quot;);
 * ScriptWriter sw = new ScriptWriter(tasks);
 * sw.write(f);
 * </pre>
 * 
 * @author agulland
 */
public class ScriptWriter
{
	/** internal array of tasks to be written to script file */
	private BackupTask[] tasks;

	/** log level. initialise to -1 an invalid value so we only write valid values to script file */
	private int scriptLogLevel =-1;

	/** log folder */
	private String scriptLogFolder;

	/**
	 * Sets the script log level that will be written to script file. Log level is
	 * an optional attribute and if not set the backup engine will use the default
	 * log level.
	 * 
	 * Note use BackupLogger to validate a log level
	 * 
	 * @param scriptLogLevel
	 *          (int) a valid log level
	 */
	public void setScriptLogLevel(int scriptLogLevel) {
		this.scriptLogLevel = scriptLogLevel;
	}

	/**
	 * Sets the script log folder that will be written to script file. The log
	 * folder is an optional attribute in the script file. if not set the backup
	 * engine will use the default log folder.
	 * 
	 * Note this class doesn't check that the log folder is be valid.
	 * 
	 * @param scriptLogFolder
	 *          (String) the log folder
	 */
	public void setScriptLogFolder(String scriptLogFolder) {
		this.scriptLogFolder = scriptLogFolder;
	}

	/**
	 * Creates a new instance of ScriptWriter
	 **/
	public ScriptWriter(BackupTask tasks[]) {
		this.tasks = tasks;
	}

	/**
	 * Creates a script file of BackupTasks for the given file
	 */
	public void write(File scriptFile) throws FileNotFoundException,
			IllegalArgumentException, IOException {

		BufferedWriter bw = null;
		try {
			// use buffering
			bw = new BufferedWriter(new FileWriter(scriptFile));

			// write script file XML header info
			bw.write("<?xml version='1.0'  encoding='utf8' standalone='yes' ?>");
			bw.newLine();
			// this is drop so we don't need to rely on a DTD
			// bw.write("<!DOCTYPE backup-script SYSTEM \"script.dtd\">");
			bw.newLine();

			// write opening backup script tag
			bw.write("<backup-script>");
			bw.newLine();

			// write log level if we have one defined
			if (scriptLogLevel>=0) {
				bw.write("<log-level>" + scriptLogLevel + "</log-level>");
			}
			
			//write log folder if one defined
			if (scriptLogFolder!=null) {
				bw.write("<log-folder>" + scriptLogFolder + "</log-folder>");
			}

			// write tags for each task
			for (int i = 0; i < this.tasks.length; i++) {
				String sXML = this.taskToXML(this.tasks[i]);
				bw.write(sXML);
				bw.newLine();
			}

			// write closing backup script tag
			bw.write("</backup-script>");

		} finally {
			// flush and close both "output" and its underlying FileWriter
			if (bw != null) bw.close();
		}
	}

	/**
	 * Returns an XML string representation of a backup task
	 */
	private String taskToXML(BackupTask task) {		
		String sNewLine = System.getProperty("line.separator", "\n");
		StringBuffer sb = new StringBuffer();

		// generate task tag
		sb.append("  <task id=\"");
		sb.append(task.getID());
		sb.append("\" ");

		if (task.isRecurse()) sb.append("recurse=\"on\" ");
		else sb.append("recurse=\"off\" ");

		if (task.isMirrorDelete()) sb.append("mirror-delete=\"on\" ");
		else sb.append("mirror-delete=\"off\" ");

		if (task.isActive()) sb.append("active=\"on\" ");
		else sb.append("active=\"off\" ");

		sb.append(">");
		sb.append(sNewLine);

		// generate source tag
		sb.append("    <source>");
		sb.append(task.getSource());
		sb.append("</source>");
		sb.append(sNewLine);

		// generate destination tag
		sb.append("    <destination>");
		sb.append(task.getDestination());
		sb.append("</destination>");
		sb.append(sNewLine);

		// generate rule tag
		sb.append("    <rule>");
		sb.append(BackupTask.getRuleName(task.getRule()));
		sb.append("</rule>");
		sb.append(sNewLine);

		// closing tag
		sb.append("  </task>");

		return sb.toString();
	}

}
