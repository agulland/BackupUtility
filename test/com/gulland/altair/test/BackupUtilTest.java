/**
 * BackupUtilTest.java
 * 
 * Who      When        What
 * -------- ----------  --------------------------------------------------------
 * Alastair 22 Oct 2011 Class created
 */
package com.gulland.altair.test;

import static org.junit.Assert.assertEquals;
import java.io.File;
import org.junit.Test;
import com.gulland.altair.BackupUtil;


public class BackupUtilTest
{
  @Test
  public void testCopyFile() {
    String sourceFileName = "TestFile.txt";
    String sourceFolder = "C:\\_MYSTUFF\\Development\\BackupUtility\\Test\\SOURCE";
    String destinationFolder = "C:\\_MYSTUFF\\Development\\BackupUtility\\Test\\TARGET";   
    
    File sourceFile = new File(sourceFolder + File.separator + sourceFileName);
    File destinationFile = new File(destinationFolder + File.separator + sourceFileName);
    
    long sourceFileSize = sourceFile.length();
    
    BackupUtil.copyFile(sourceFile, destinationFile);
    
    //check copy worked
    long destinationFileSize = destinationFile.length();
    
    assertEquals("Target file copied", sourceFileSize, destinationFileSize);
    
  }

  /*
  @Test
  public void testCopyVeryLargeFile() {
    String sourceFileName = "LargeTestFile.dat";
    String sourceFolder = "E:/Users/Alastair/TEMP";
    String destinationFolder = "E:/Users/Alastair/TEMP/Test Target";   
    
    File sourceFile = new File(sourceFolder + File.separator + sourceFileName);
    File destinationFile = new File(destinationFolder + File.separator + sourceFileName);
    
    long sourceFileSize = sourceFile.length();
    
    BackupUtil.copyFile(sourceFile, destinationFile);
    
    //check copy worked
    long destinationFileSize = destinationFile.length();
    
    assertEquals("Target file copied", sourceFileSize, destinationFileSize);
    
  }  
  */
  
  /*
  @Test
  public void testDeleteFolder() {
  //create folder structure
  // delete
    fail("Not yet implemented");
  }
  */
}


