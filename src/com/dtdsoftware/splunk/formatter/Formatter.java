package com.dtdsoftware.splunk.formatter;

import java.util.Map;

/**
 * This interface can be implemented to provide custom formatting logic to the
 * STDOUT format that is picked up by SPLUNK.
 * 
 * The custom implementation class can then be placed on the classpath and
 * declared in the configuration xml file.
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public interface Formatter {

	/**
	 * Constants , keys for the common meta data
	 */
	public static final String META_HOST = "meta_host";
	public static final String META_JVM_DESCRIPTION = "meta_description";
	public static final String META_PROCESS_ID = "meta_processid";

	/**
	 * Data that is common to each mbean line of attributes
	 * 
	 * @param metaData
	 *            map of meta data using Formatter constants as keys
	 */
	public void setMetaData(Map<String, String> metaData);

	/**
	 * This method is called to print each Mbean attribute line
	 * 
	 * @param Mbean
	 *            the canonical mbean name
	 * @param attributes
	 *            map of mbean attributes
	 * @param timestamp
	 *            internal timestamp that can optionally be used in the output
	 *            line
	 */
	public void print(String Mbean, Map<String, String> attributes,
			long timestamp);

}
