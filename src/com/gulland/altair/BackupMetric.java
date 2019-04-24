/*
 * BackupMetric.java
 *
 * Created on 16 September 2004, 15:16
 */

package com.gulland.altair;

/**
 * Metrics are used to record data about a backup process. This information
 * includes data such as time taken, number of files found, bacedup and deleted
 * 
 * @author  agulland
 */
public class BackupMetric
{
  /** Records number of files found */
  private int filesFound;
  
  /**  Records number of files copied.    */
  private int filesCopied;

  /**  Records number of files deleted    */
  private int filesDeleted;    
  
  /** records start time    */
  private long startTime;
  
  /** records end time   */
  private long endTime;
  
/**
 * Constructor
 */
  public BackupMetric()
  {
    this.startTime = System.currentTimeMillis();    
    this.filesCopied = 0;
    this.filesDeleted = 0;
    this.endTime = 0;
  }
  
/**
 * Returns the total number of files found as recorded by this metric
 * @return Returns the number of files found.
 */
  public int getFilesFound()
  {
    return filesFound;
  }
/**
 * Used to record the number of files found
 * @param filesFound The number of files found to add to the metric.
 */
  public void addFilesFound(int filesFound)
  {
    this.filesFound += filesFound;
  }
  
/**
 * Returns the total number of files copied as recorded by this metric
 * @return the number of files copied.
 */
  public int getFilesCopied()
  {
    return this.filesCopied;
  }  
/**
 * Adds a number of files copied to this metric
 * @param count the number of files to add to the files copied parameter.
 */
  public void addFilesCopied(int count)
  {
    this.filesCopied += count;
  }
  
/**
 * Returns the total number of files deleted as recorded by this metric
 * @return the number of files deleted.
 */
  public int getFilesDeleted()
  {
    return this.filesDeleted;
  }
  
/**
 * Records the count of files deleted
 * @param count the number of files to add to the files deleted parameter.
 */
  public void addFilesDeleted(int count)
  {
    this.filesDeleted += count;
  }
  

/**
 * Stops the metric from further recording and therefore the timing
 */  
  public void stop()
  {
    this.endTime = System.currentTimeMillis();    
  }
  

/**
 * Returns the duration of metric in milliseconds. If metric is still recording
 * then a value of 0 is returned
 * @return duration in milliseconds
 */
  public long getDuration()
  {
    if(this.endTime>0)
      return this.endTime - this.startTime;
    else
      return 0;
  }
  
  
/**
 * Adds data from a given metric to this metric ignoring duration
 * @param metric the BackupMetric whose data you wish to add to this metric
 */  
  public void addMetric(BackupMetric metric)
  {
    this.addFilesFound(metric.getFilesFound());
    this.addFilesCopied(metric.getFilesCopied());
    this.addFilesDeleted(metric.getFilesDeleted());
  }   

/**
 * Returns metric data in a nicely formatted string
 */
  public String toString()
  {
    //long durationSeconds = this.getDuration() / 1000;
    
    //java.text.DecimalFormat myFormatter = new java.text.DecimalFormat("####0");    
    // myFormatter.format(durationSeconds)
    return "Found " + this.getFilesFound() + ", " + this.getFilesCopied() + " copied, " + this.getFilesDeleted() + " deleted.";
  }
}
