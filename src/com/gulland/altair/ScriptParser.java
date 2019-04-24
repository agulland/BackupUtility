/**
 * ScriptParser.java
 *
 * Who      When        What
 * -------- ----------  --------------------------------------------------------
 * agulland 10 Aug 2004 Class created
 * agulland 08 Feb 2010 Updated to now read log level and log folder from script
 *                      file. 
 *                      This Class solely determines the default log folder   
 */

package com.gulland.altair;

import java.util.Vector;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.*;

/**
 * This class is used to parse the Backup XML Script file and generate a series
 * of BackupTask objects
 * 
 * @author agulland
 */
public class ScriptParser
{
	/** log level read from script file */
	private int iScriptLogLevel;

	/** log folder read from script file */
	private String sScriptLogFolder;

	/** internal object that holds the script tasks */
	private Vector<BackupTask> tasks = new Vector<BackupTask>();

	/** default log folder is 'logs' below folder from which this app is launched */
	private static String DEFAULT_LOG_FOLDER = System.getProperty("user.dir")
			+ File.separator + "logs";

	/**
	 * Returns the value of the log level as defined by the script file
	 * 
	 * @return Returns the log level as one of the constants defined in
	 *         BackupLogger.
	 */
	public int getScriptLogLevel() {
		return iScriptLogLevel;
	}

	public String getScriptLogFolder() {
		return sScriptLogFolder;
	}

	/**
	 * Returns the default log folder. It will also attempt to create the folder
	 * if it does not exist
	 * 
	 * @return either the default log folder or empty string if default could not
	 *         be created
	 */
	private static String getDefaultLogFolder() {
		File f = new File(DEFAULT_LOG_FOLDER);
		if (f.exists()) {
			return DEFAULT_LOG_FOLDER;
		} else {
			try {
				f.mkdir();
				return DEFAULT_LOG_FOLDER;
			} catch (SecurityException e) {
				System.out.println("Could not create log folder'" + DEFAULT_LOG_FOLDER);
				return "";
			}
		}
	}

	/**
	 * Returns an array of BackupTask objects
	 * 
	 * @return an array
	 */
	public BackupTask[] getTasks() {
		BackupTask t[] = new BackupTask[tasks.size()];
		return (BackupTask[]) tasks.toArray(t);
	}

	/**
	 * For a given script file populates the tasks object
	 * 
	 * @param backupScriptFile
	 *          the backup script file
	 * @throws IOException
	 *           for any problems incurred while processing the script file
	 */
	public void parseScript(File backupScriptFile) throws IOException {

		// validate and parse script.xml
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		docBuilderFactory.setValidating(false);

		try {
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

			// create new error handler that writes validation errors to log file
			MyErrorHandler meh = new MyErrorHandler();
			docBuilder.setErrorHandler(meh);
			Document doc = docBuilder.parse(backupScriptFile);

			// if parsed was successful then proceed
			Node n;
			Node nVal;
			String val;
			if (!meh.raisedErrors()) {
				// get log level
				NodeList nl = doc.getElementsByTagName("log-level");
				if (nl.getLength() > 0) {
					n = nl.item(0);
					nVal = n.getFirstChild();
					if (nVal != null) {
						val = nVal.getNodeValue();
						try {
							int iVal = Integer.parseInt(val);
							if (BackupLogger.isValidLogLevel(iVal)) this.iScriptLogLevel = iVal;
						} catch (NumberFormatException e) {
						}
					}

					// get log folder
					nl = doc.getElementsByTagName("log-folder");
					if (nl.getLength() > 0) {
						n = nl.item(0);
						nVal = n.getFirstChild();
						if (nVal != null) {
							val = nVal.getNodeValue();
							// If folder can't be found then use default log folder
							File f = new File(val);
							if (!f.exists()) {
								System.out
										.println("Log folder in script file not found. Using default '"
												+ DEFAULT_LOG_FOLDER + "'");
								val = ScriptParser.getDefaultLogFolder();
							}

							this.sScriptLogFolder = val;
						} else {
							// empty log-folder node
							this.sScriptLogFolder = ScriptParser.getDefaultLogFolder();
						}
					} else {
						// log-folder node not found
						this.sScriptLogFolder = ScriptParser.getDefaultLogFolder();
					}
				}

				NodeList taskNodes = doc.getElementsByTagName("task");

				// iterate over nodes
				for (int i = 0; i < taskNodes.getLength(); i++) {
					Node taskNode = taskNodes.item(i);
					this.parseTask(taskNode);
				}
			} else {
				System.out
						.println("Backup script file failed validation, please correct and rerun.");
			}
		} catch (ParserConfigurationException e) {
			throw new IOException("Fatal Exception parsing script file.");
		} catch (SAXException e) {
			throw new IOException("Fatal Exception parsing script file.");
		}
	}

	/**
	 * Adds a BackupTask object to the collection
	 * 
	 * @param task
	 *          a BackupTask object
	 */
	private void addTask(BackupTask task) {
		this.tasks.add(task);
	}

	/**
	 * Creates a BackupTask object from an task node
	 * 
	 * @param taskNode
	 *          a task node in the XML file
	 */
	private void parseTask(Node taskNode) {
		BackupTask task = new BackupTask();

		// process task tag attributes
		NamedNodeMap taskAttrs = taskNode.getAttributes();

		// Process each attribute - expecting 'recurse', 'active' and
		// 'mirror-delete'
		int numAttrs = taskAttrs.getLength();
		for (int i = 0; i < numAttrs; i++) {
			Attr attr = (Attr) taskAttrs.item(i);

			// get attribute value
			String attrValue = attr.getNodeValue();

			// process boolean values
			boolean bVal = false;
			if ((attrValue.equals("on")) || (attrValue.equals("yes"))
					|| (attrValue.equals("true"))) bVal = true;

			// determine and set appropriate task property
			String attrName = attr.getNodeName();
			if (attrName.equals("id")) task.setID(attrValue);
			else if (attrName.equals("recurse")) task.setRecurse(bVal);
			else if (attrName.equals("active")) task.setActive(bVal);
			else if (attrName.equals("mirror-delete")) task.setMirrorDelete(bVal);
			else System.out.println("Unkown task attribute '" + attrName
					+ "' defined in task.");
		}

		// Process task tag child nodes
		NodeList childNodes = taskNode.getChildNodes();

		for (int j = 0; j < childNodes.getLength(); j++) {
			Node childNode = childNodes.item(j);

			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				NodeList list = childNode.getChildNodes();

				// set source value
				if (childNode.getNodeName().equals("source")) {
					String value = ((Node) list.item(0)).getNodeValue();
					task.setSource(value);
				}

				// set destination value
				if (childNode.getNodeName().equals("destination")) {
					String value = ((Node) list.item(0)).getNodeValue();
					task.setDestination(value);
				}

				// set rule
				if (childNode.getNodeName().equals("rule")) {
					String value = ((Node) list.item(0)).getNodeValue();
					int intRule;
					if (value.equals("all")) intRule = BackupTask.ALL;
					else if (value.equals("exists")) intRule = BackupTask.EXISTS;
					else if (value.equals("changed")) intRule = BackupTask.CHANGED;
					else if (value.equals("exists changed")) intRule = BackupTask.EXISTS_CHANGED;
					else if (value.equals("new")) intRule = BackupTask.NEW;
					else {
						// unknown rule, by default set to changed and flag task as not
						// active
						intRule = BackupTask.CHANGED;
						task.setActive(false);
						System.out.println("Unkown rule value '" + value
								+ "' defined in task. This task will not be processed");
					}

					task.setRule(intRule);
				}
			}
		}
		this.addTask(task);
	}

	/**
	 * Error Handler for parsing XML script file. If any parse errors are
	 * generated then the static variable parse_failed is flagged as true.
	 */
	private static class MyErrorHandler implements ErrorHandler
	{
		// flagged to true if parsing the XML script file failed
		private boolean parse_failed = false;

		public void warning(SAXParseException e) {
			parse_failed = true;
			System.out.println("Warning on line " + e.getLineNumber() + ": "
					+ e.getMessage());
		}

		public void error(org.xml.sax.SAXParseException e) {
			parse_failed = true;
			System.out.println("Error on line " + e.getLineNumber() + ": "
					+ e.getMessage());
		}

		public void fatalError(org.xml.sax.SAXParseException e) {
			parse_failed = true;
			System.out.println("Fatal error on line " + e.getLineNumber() + ": "
					+ e.getMessage());
		}

		public boolean raisedErrors() {
			return parse_failed;
		}
	}

}
