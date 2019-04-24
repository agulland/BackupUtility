/**
 * Main.java
 * 
 * Who      When        What
 * -------- ----------  --------------------------------------------------------
 * agulland 12 Aug 2004 Class created 
 * agulland 08 Feb 2010 Updated to use log folder defined in script file
 * 
 * TODO ? check if log folder is null or empty and disable logging if so ?
 */


package com.gulland.altair;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

/**
 * <p>
 * The main class for launching backup engine
 * </p>
 * <p>
 * This is an executable class and takes the filename of a backup script file as
 * a parameter. For example,
 * </p>
 * 
 * <code>java BackupUtility c:/backup/mybackupscript.xml</code>
 * 
 * <p>
 * For details on the structure of the backup script file, please refer to the
 * documentation.
 * </p>
 * 
 * @since 1.0
 * @author agulland
 */
public class Main
{
	/** logging object */
	private static final BackupLogger logger = BackupLogger.getLogger();

	/**
	 * Start routine for app. Takes a fully qualified path and file name of a
	 * backup script file.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {

		/* test stuff  
		System.out.println("User home: " + System.getProperty("user.home"));
		System.out.println("Directory in which app is running: " + System.getProperty("user.dir"));
		// temp folder
		System.out.println("OS current temporary directory is "
				+ System.getProperty("java.io.tmpdir"));
		// java install
		System.out.println("Java install is " + System.getProperty("java.home"));
		System.exit(0);
		*/

		// if no script file then warn user.
		if (args.length < 1) {
			System.out.println("Missing argument. To execute this package you need "
					+ "to provide the path and filename of a backup script file.");
			System.exit(0);
		}

		// get script file from args
		String scriptFileArg = args[0];

		// parse backup script file
		File scriptFile = new File(scriptFileArg);
		if (scriptFile.canRead()) {
			try {
				ScriptParser sp = new ScriptParser();
				sp.parseScript(scriptFile);

				// get log level
				int iLogLevel = sp.getScriptLogLevel();								
				logger.setLogLevel(iLogLevel);

				// initiate HTML logger
				HTMLListener h = new HTMLListener(sp.getScriptLogFolder());
				logger.addListener(h);
				logger.detail("Log level: " + iLogLevel);
				logger.info("Processing script file '" + scriptFile.getAbsolutePath() + "'");

				// get array of backup events
				BackupTask tasks[] = sp.getTasks();

				// Write log file header info
				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
				logger.info("Run date: " + sdf.format(d));
				logger.info("Number of tasks: " + tasks.length);

				// execute backup
				long lStartTime = System.currentTimeMillis();
				Backup myBackup = new Backup(tasks);
				myBackup.start(false);

				while (myBackup.isRunning()) {
					// wait till finish
				}

				// TODO we should get this info from BackupMetric
				long iDuration = (System.currentTimeMillis() - lStartTime) / 1000;
				long hrs = iDuration / 3600;
				long mins = (iDuration % 3600) / 60;
				long secs = iDuration % 60;

				// Call routines to tidy up end of log file
				logger.endLog("Backup job complete. Total time: " + hrs + ":" + mins
						+ ":" + secs + ".");
				
			} catch (IOException e) {
				logger.warn("Exception reading script file: " + e.getMessage());
			}
		} else {
			logger.warn("Application Failure. Cannot read backup script file at "
					+ scriptFile + ". Check path or permissions.");
		}

	}

}
