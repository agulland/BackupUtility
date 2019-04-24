/**
 * BackupUtil.java
 * 
 * Who      When        What
 * -------- ----------  --------------------------------------------------------
 * Alastair 22 Oct 2011 Class created
 */
package com.gulland.altair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Provides utility methods for backup utility
 * @author Alastair
 *
 */
public class BackupUtil
{
	/** define logging object */
	private static final BackupLogger logger = BackupLogger.getLogger();

	/**
	 * Copies a file from a specified source to destination. Note, will overwrite
	 * if destination file already exists)
	 * 
	 * @param sourceFile
	 *          fully qualified path to a source file
	 * @param destinationFile
	 *          fully qualified path to a destination file
	 * 
	 * @return true if file was copied
	 */
	public static boolean copyFile(File sourceFile, File destinationFile) {
		logger.detail("Copying file " + sourceFile.getAbsoluteFile());

		// check that we can read source and write destination
		boolean canDo = true;
		if (!sourceFile.canRead()) {
			canDo = false;
			logger
					.warn("Could not read source file: '"
							+ sourceFile.getPath()
							+ "'. Check permissions or that it is not locked by another process.");
		}

		/**
		 * TODO check target folder is writable?
		 * */

		/**
		 * For large files :
		 * http://en.allexperts.com/q/Java-1046/2009/3/BufferWriter.htm
		 */

		// if we can read and write ok then do file copy
		if (canDo) {			
			try {
				// copy file
				FileChannel in = new FileInputStream(sourceFile).getChannel();
				FileChannel out = new FileOutputStream(destinationFile).getChannel(); 

				// magic number for Windows, 64Mb - 32Kb)
        int maxCount = (64 * 1024 * 1024) - (32 * 1024);
        
        long size = in.size();
        long position = 0;
        while ( position < size ) {
           position += in.transferTo( position, maxCount, out);
        }        
          
        in.close(); 
			 	out.close();

				// Set date of destination file to be same as source otherwise it would
				// default to system date)
				destinationFile.setLastModified(sourceFile.lastModified());

				return true;
			} catch (FileNotFoundException e) {
				logger.warn("Could not access file " + e.getMessage());
				return false;
			} catch (IOException e) {
				logger.warn("Could not backup file " + e.getMessage());
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Recursively delete a folder and all it's contents and any subfolders
	 * 
	 * @param folder
	 *          the folder to be deleted
	 * @throws SecurityException
	 *           if the folder can not be deleted
	 */
	public static void deleteFolder(File folder) throws SecurityException {
		logger.detail("Deleting folder '" + folder.getAbsolutePath() + "'");

		// get folder contents
		File contents[] = folder.listFiles();

		// and delete
		for (int i = 0; i < contents.length; i++) {
			// if sub item is a folder then recurse
			if (contents[i].isDirectory()) deleteFolder(contents[i]);
			else contents[i].delete();
		}

		// finish by deleting given folder
		folder.delete();
	}
	
}


