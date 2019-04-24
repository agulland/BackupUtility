/**
 * HTMLListener
 *
 * Who      When        What
 * -------- ----------  --------------------------------------------------------
 * agulland 24 Aug 2004 Class created
 * agulland 08 Feb 2010 Updated to include endLog method.
 *                      Removed reading log level and folder from options file 
 *                     
 * 
 */
package com.gulland.altair;

import java.io.*;
import java.util.*;
import java.text.*;

/**
 * A listener for generating HTML format log files
 * 
 * @author AGULLAND
 */
public class HTMLListener implements LogListener
{

	/** The file writer used to output to log file */
	private BufferedWriter bw;

	/** used to ensure we only write out first failure message */
	private boolean writeLogErrorFlag = false;

	/**
	 * Initialise HTML logger. Initialise with the path to a log folder
	 * 
	 * @param logfolder
	 *          a path to where log files are written
	 */
	public HTMLListener(String logFolder) {
		// create log file
		if ((logFolder == null) || (!logFolder.equals(""))) {
			try {
				// date stamp for file name
				Date d = new Date();
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy HH.mm");
				String sDateStamp = sdf.format(d);

				// create file writer
				FileWriter fw = new FileWriter(new File(logFolder + File.separator
						+ "BackupLog " + sDateStamp + ".html"), true);
				this.bw = new BufferedWriter(fw);
				this.startLog();
			} catch (IOException e) {
				System.out
						.println("Can't find log folder '"
								+ logFolder
								+ "' or unable to write to the log folder. No logging will occur for this session.");
			}
		} else {
			System.out
					.println("Log folder not defined in script file. No logging will occur for this session.");
		}
	}

	/**
	 * creates and writes the header for log file
	 */
	private void startLog() {
		if (bw != null) {
			StringBuffer sb = new StringBuffer();

			sb
					.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
			sb.append("<html>\n");
			sb.append("<head>\n");
			sb.append("<title>Backup Utility Log File</title>\n");
			sb.append("<style type=\"text/css\">\n");
			sb
					.append("body { font-family: verdana; font-size: 12px; margin: 10px;}\n");
			sb
					.append("h2 { border-bottom: #a9a9a9 1px solid; padding-bottom: 4px; margin-bottom: 24px;}\n");
			sb
					.append("table { width: 80%; table-layout: auto; border-collapse: collapse; border: #a9a9a9 1px solid; margin-bottom: 12px;}\n");
			sb
					.append("td { padding: 3px 6px 3px 6px; border: #a9a9a9 1px solid; }\n");
			sb
					.append("th { padding: 3px 6px 3px 6px; border: #a9a9a9 1px solid; background: #a9a9a9; text-align: left; font-weight: normal;}\n");
			sb.append("</style>\n");
			sb.append("</head>\n");
			sb.append("<body>\n");
			sb.append("<h2>Backup Utility Log</h2>\n");
			sb.append("\n\n");

			// output to file
			try {
				bw.write(sb.toString());
				bw.flush();
				bw.newLine();
			} catch (IOException e) {
				System.out.println("Error writing to log file: " + e.getMessage());
			}
		}
	}

	/**
	 * write to HTML log file
	 */
	public void writeLog(String msg) {
		// output to log file
		if (bw != null) {
			try {
				bw.write("<p>" + msg + "</p>");
				bw.flush();
				bw.newLine();
			} catch (IOException e) {
				// only write first occurrence of error so as not to swamp output
				if (!writeLogErrorFlag) {
					System.out.println("Error writing to log file: " + e.getMessage());
					writeLogErrorFlag = true;
				}
			}
		}

	}

	@Override
	/**
	 * write out completing html to log file
	 */
	public void endLog(String msg) {
		if (bw != null) {
			try {
				bw.write("<p>" + msg + "</p>");
				bw.write("</body></html>");
				bw.flush();
				bw.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Is this used?
	 */
	public void destroyListner() {
		if (bw != null) {
			try {
				bw.write("</body></html>");
				bw.flush();
				bw.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * reads configuration file and returns log folder
	 * 
	 * @return string path to log folder
	 */
	/*
	 * private String getLogFolder() { String sVal = "";
	 * 
	 * // open global settings file DocumentBuilderFactory docBuilderFactory =
	 * DocumentBuilderFactory .newInstance();
	 * docBuilderFactory.setValidating(false);
	 * 
	 * try { DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	 * 
	 * // create new error handler that writes validation errors to log file
	 * MyErrorHandler meh = new MyErrorHandler(); docBuilder.setErrorHandler(meh);
	 * 
	 * // read doc String settingsFile = System.getProperty("user.dir") +
	 * File.separator + "settings" + File.separator + "global.xml"; Document doc =
	 * docBuilder.parse(settingsFile);
	 * 
	 * // if parsed was successful then proceed if (!meh.raisedErrors()) {
	 * NodeList nl = doc.getElementsByTagName("log"); if (nl.getLength() > 0) {
	 * Node n = nl.item(0); Node nVal = n.getFirstChild(); if (nVal != null) sVal
	 * = nVal.getNodeValue(); } } } catch (ParserConfigurationException e) {
	 * System.out.println("Could not parse global settings file: " +
	 * e.getMessage()); } catch (SAXException e) {
	 * System.out.println("Could not parse global settings file: " +
	 * e.getMessage()); } catch (IOException e) {
	 * System.out.println("IO error parsing global settings file: " +
	 * e.getMessage()); }
	 * 
	 * // if value was not found then return default if (sVal.equals("")) sVal =
	 * System.getProperty("user.dir") + File.separator + "log";
	 * 
	 * return sVal; }
	 */
	/**
	 * Error Handler for parsing XML script file. If any parse errors are
	 * generated then the static variable parse_failed is flagged as true.
	 */
	/*
	 * private static class MyErrorHandler implements ErrorHandler { // flagged to
	 * true if parsing the XML script file failed private boolean parse_failed =
	 * false;
	 * 
	 * public void warning(SAXParseException e) { parse_failed = true;
	 * System.out.println("Warning on line " + e.getLineNumber() + ": " +
	 * e.getMessage()); }
	 * 
	 * public void error(org.xml.sax.SAXParseException e) { parse_failed = true;
	 * System.out.println("Error on line " + e.getLineNumber() + ": " +
	 * e.getMessage()); }
	 * 
	 * public void fatalError(org.xml.sax.SAXParseException e) { parse_failed =
	 * true; System.out.println("Fatal error on line " + e.getLineNumber() + ": "
	 * + e.getMessage()); }
	 * 
	 * public boolean raisedErrors() { return parse_failed; } }
	 */

}
