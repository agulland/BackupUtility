/**
 * Backup.java
 * 
 * Who      When        What
 * -------- ----------  --------------------------------------------------------
 * agulland  1 Aug 2004 Class created
 */

package com.gulland.altair;

import java.io.File;

/**
 * <p>
 * Performs backup operation. It takes an array of <code>BackupTask</code>
 * objects as a constructor and is executed by use of the <code>backup</code>
 * method.
 * <p>
 * 
 * <p>
 * For example, if we have <code>t</code> as an array of BackupTask objects then
 * we can do,
 * </p>
 * 
 * <pre>
 * Backup b = new Backup(t);
 * b.start(false);
 * </pre>
 * 
 * <p>
 * This class can also perform restore operations by setting the boolean to true
 * in the above <code>start</code> method.
 * </p>
 * 
 * <p>
 * Note, the backup operation can be stopped, paused and resumed by calling the
 * <code>stop</code>, <code>pause</code> and <code>resume</code> methods.
 * </p>
 * 
 * <p>
 * Logging notes:
 * <ul>
 * <li>Trace - writes out info on every file found</li>
 * <li>Detail - only writes info if a file is copied or deleted</li>
 * <li>Info - information about start of task and metrics when complete</li>
 * <li>Warn - missing folders etc</li>
 * </p>
 */
public class Backup implements Runnable
{
	/** define logging object */
	private static final BackupLogger logger = BackupLogger.getLogger();

	/** collection of BackupTask objects */
	private BackupTask[] tasks;

	/** restore mode flag */
	private boolean isRestore = false;

	/** this class's internal thread */
	private volatile Thread internalThread;

	/** controls the pause of the thread */
	private volatile boolean threadSuspended = false;

	/**
	 * Creates a new instance of BackupUtility
	 * 
	 * @param tasks
	 *          an array of BackupTask objects
	 */
	public Backup(BackupTask[] tasks) {
		this.tasks = tasks;
	}

	/**
	 * This method is used to start the backup routine in a thread manageable way.
	 * Once started the routine can be stopped, paused and resumed using the
	 * appropriate methods.
	 */
	public void start(boolean isRestore) {
		this.isRestore = isRestore;
		this.internalThread = new Thread(this);
		this.internalThread.start();
	}

	/**
	 * stop the execution of the backup routine. Note, the code line doesn't check
	 * at every point for whether the routine has been stopped so there may be
	 * some delay before termination. The code checks before processing a source
	 * file so if the stop method is called while the routine is copying a file
	 * that file will be copied in it's entirety before the code aborts.
	 */
	public synchronized void stop() {
		logger.warn("backup operation cancelled");
		this.internalThread = null;
		notify();
	}

	/**
	 * Pauses the execution at the next available opportunity. See stop for more
	 * details. To resume normal execution use <code>resume</code> method.
	 */
	public void pause() {
		logger.trace("backup operation paused");
		threadSuspended = true;
	}

	/**
	 * resumes a paused execution
	 * 
	 */
	public synchronized void resume() {
		logger.trace("backup operation resumed");
		threadSuspended = false;
		notify();
	}

	/**
	 * Returns true if the thread is still active
	 * 
	 * @return true if thread is alive
	 */
	public boolean isRunning() {
		return ((internalThread != null) && internalThread.isAlive());
	}

	/**
	 * Executes a backup or a restore job.
	 * 
	 * @param isRestore
	 *          if true then the backup task executes in restore mode
	 */
	public void run() {
		Thread thisThread = Thread.currentThread();
		for (int i = 0; i < this.tasks.length; i++) {
			// Only do backup if we have not been stopped. Note we could replace above
			// if statement with a while loop but there probably won't be much gain
			// in performance
			if (internalThread == thisThread) {
				BackupTask task = this.tasks[i];
				if (task.isActive()) {
					logger.info("Processing task '" + task.getID() + "'");
					logger.info("Source '" + task.getSource() + "'");
					logger.info("Destination  '" + task.getDestination() + "'");
					logger.info("Using Rule '" + task.getRuleText() + "'");
					logger.info("Mirror Delete '" + task.isMirrorDelete() + "'");

					// check source exist - only backup if it does
					File f = new File(task.getSource());
					if (f.exists()) {
						// Launch backup task
						BackupMetric metric = this.backupFolder(task);
						logger.info("Task complete. " + metric.toString());
					} else {
						logger.info("Source doesn't exist.");
					}
				} else {
					logger.info("Task '" + task.getID() + "' is flagged inactive.");
				}

				// check for being paused
				try {
					synchronized (this) {
						while ((threadSuspended) && (internalThread == thisThread))
							wait();
					}
				} catch (InterruptedException e) {
				}

			}

		}
	}

	/**
	 * <p>
	 * Backups a given folder to a destination. Note this is a recursive function
	 * and will perform the same operation to subfolders if the recurse flag is
	 * set in the BackupTask object.
	 * </p>
	 * 
	 * <p>
	 * This method returns a BackupMetric object which contains data on the backup
	 * operation such as no of files backed up, deleted and time taken
	 * </p>
	 * 
	 * @param a
	 *          BackupTask object that defines the backup routine
	 * @param isRestore
	 *          if true then the backup executes in restore mode
	 * 
	 * @return BackupMetric holds metric data on backup operation
	 */
	private BackupMetric backupFolder(BackupTask task) {
		// holds metric data on backup operation
		BackupMetric metric = new BackupMetric();

		/*
		 * Determine source and destination folders depending on restore mode
		 */
		int rule = task.getRule();
		File fileSource;
		File fileDestination;
		if (isRestore) {
			fileDestination = new File(task.getSource());
			fileSource = new File(task.getDestination());
		} else {
			fileSource = new File(task.getSource());
			fileDestination = new File(task.getDestination());
		}

		// if destination folder does not exist then create if rules allow
		boolean destExist = true;
		if (!fileDestination.exists()) {
			if ((rule == BackupTask.ALL) || (rule == BackupTask.NEW)
					|| (rule == BackupTask.CHANGED)) {
				logger.detail("Creating target directory '"
						+ fileDestination.getAbsoluteFile() + "' ");
				if (fileDestination.mkdirs()) {
					// fileDestination.setLastModified(fileSource.lastModified()); ???? is
					// required
					destExist = true;
				} else {
					destExist = false;
					logger.warn("Couldn't find or create destination directory '"
							+ fileDestination.getAbsolutePath()
							+ "'. Check write permission on destination folder.");
				}
			}
		}

		// process source as directory or as file
		if (destExist) {
			if (fileSource.isDirectory()) {
				// Get files in the source folder
				File[] sourceFiles = fileSource.listFiles();

				// Iterate over all source files
				int iCounter = 0;
				Thread thisThread = Thread.currentThread();
				while ((iCounter < sourceFiles.length)
						&& (internalThread == thisThread)) {
					// TODO slow down execution so as not to utilise 100% CPU
					/*
					 * try { thisThread.sleep(2000); } catch(InterruptedException e) { }
					 */

					// determine destination file name
					String sourceName = sourceFiles[iCounter].getName();
					logger.trace("Processing '" + sourceName + "'");

					// Set destination folder accounting for restore mode
					File destination;
					if (isRestore) destination = new File(task.getSource()
							+ File.separator + sourceName);
					else destination = new File(task.getDestination() + File.separator
							+ sourceName);

					// if source is a directory and task allows subfolder processing
					if ((sourceFiles[iCounter].isDirectory()) && (task.isRecurse())) {
						/*
						 * create new BackupTask with correct source and destination, in
						 * restore we still must have the source as defined by script file
						 * as source in BackupTask object
						 */
						BackupTask newTask;
						if (isRestore) newTask = new BackupTask(destination.getPath(),
								sourceFiles[iCounter].getPath(), rule);
						else newTask = new BackupTask(sourceFiles[iCounter].getPath(),
								destination.getPath(), rule);

						// recurse backup operation
						BackupMetric subMetric = this.backupFolder(newTask);

						// add submetric's data to this metric to provide summary info
						metric.addMetric(subMetric);
					} else if (sourceFiles[iCounter].isFile()) {
						// count file found
						metric.addFilesFound(1);

						// backup single file
						if (this.backupFile(sourceFiles[iCounter], destination, rule)) metric
								.addFilesCopied(1);
					}
					// else do nothing - we won't backup a subfolder if not recurse

					// check for being paused
					try {
						synchronized (this) {
							while ((threadSuspended) && (internalThread == thisThread))
								wait();
						}
					} catch (InterruptedException e) {
					}

					iCounter++;
				} // end while loop

				// check for mirror delete. note, mirror delete never available in
				// restore mode
				if ((!isRestore) && (task.isMirrorDelete())) {
					File destContents[] = fileDestination.listFiles();
					File sourceContents[] = fileSource.listFiles();

					// iterate through destination folder file list
					if (destContents != null) {
						for (int iDestCntr = 0; iDestCntr < destContents.length; iDestCntr++) {
							// check if file exists in source file list
							String destFileName = destContents[iDestCntr].getName();
							boolean bExists = false;
							for (int iSourceCntr = 0; iSourceCntr < sourceContents.length; iSourceCntr++) {
								if (sourceContents[iSourceCntr].getName().equals(destFileName)) bExists = true;
							}

							// delete if not present
							if (!bExists) {
								try {
									if (destContents[iDestCntr].isDirectory()) {
										BackupUtil.deleteFolder(destContents[iDestCntr]);
										logger.detail("'" + destFileName
												+ "' deleted from destination");
										metric.addFilesDeleted(1);
									} else if (destContents[iDestCntr].delete()) {
										logger.detail("'" + destFileName
												+ "' deleted from destination");
										metric.addFilesDeleted(1);
									}
								} catch (SecurityException e) {
									logger.warn("Failed to delete file "
											+ destContents[iDestCntr].getAbsolutePath()
											+ ", exception thrown: " + e.getMessage());
								}
							}
						}
					}
				} // end mirror delete

				// only write out summary info if we have either copied or deleted
				if ((metric.getFilesCopied() > 0) || (metric.getFilesDeleted() > 0)) {
					logger.detail("Folder '" + fileSource.getName() + "' backed up. "
							+ metric.toString());
				}
			}
			// else process single file
			else {
				String sourceName = fileSource.getName();

				// determine name of destination file
				File destination;
				if (isRestore) destination = new File(task.getSource() + File.separator
						+ sourceName);
				else destination = new File(task.getDestination() + File.separator
						+ sourceName);

				// backup
				if (this.backupFile(fileSource, destination, rule)) {
					metric.addFilesCopied(1);
					logger.detail(fileSource.getPath() + " backed up. ");
				}
			}
		}

		metric.stop();
		return metric;
	}

	/**
	 * Backs up a single file to a destination according to a given rule
	 * 
	 * @param source
	 *          the source file
	 * @param destination
	 *          the destination file
	 * @param rule
	 *          the controlling rule
	 * 
	 * @return true if file was backed up
	 */
	private boolean backupFile(File source, File destination, int rule) {
		boolean wasCopied = false;

		/**
		 * always copy file if rule all is used
		 */
		if (rule == BackupTask.ALL) {
			// logger.detail("Applying rule ALL");
			wasCopied = BackupUtil.copyFile(source, destination);
		}
		/**
		 * if rule is 'changed' then only copy if timestamp is different or file
		 * does not already exist
		 */
		else if (rule == BackupTask.CHANGED) {
			// logger.detail("Applying rule CHANGED");
			if (destination.exists()) {
				long lSourceTimeStamp = source.lastModified();
				long lDestTimestamp = destination.lastModified();

				if (lSourceTimeStamp != lDestTimestamp) {
					wasCopied = BackupUtil.copyFile(source, destination);
				}
			} else {
				wasCopied = BackupUtil.copyFile(source, destination);
			}
		}
		/**
		 * If rule is 'IF EXIST' only copy files that already exist but ignore time
		 * stamp
		 */
		else if (rule == BackupTask.EXISTS) {
			// logger.detail("Applying rule EXISTS");
			if (destination.exists()) wasCopied = BackupUtil.copyFile(source, destination);
		}
		/**
		 * If rule is IF EXISTS CHANGED' then file must exist and must have been
		 * changed
		 */
		else if (rule == BackupTask.EXISTS_CHANGED) {
			// logger.detail("Applying rule EXISTS_CHANGED");
			if (destination.exists()) {
				long lSourceTimeStamp = source.lastModified();
				long lDestTimestamp = destination.lastModified();

				if (lSourceTimeStamp != lDestTimestamp) {
					wasCopied = BackupUtil.copyFile(source, destination);
				}
			}
		}
		/**
		 * If rule is 'NEW' then only copy files that don't already exist
		 */
		else if (rule == BackupTask.NEW) {
			// logger.detail("Applying rule NEW");
			//if (!destination.exists()) wasCopied = copyFile(source, destination);
			if (!destination.exists()) wasCopied = BackupUtil.copyFile(source, destination);
		}

		return wasCopied;
	}


}
