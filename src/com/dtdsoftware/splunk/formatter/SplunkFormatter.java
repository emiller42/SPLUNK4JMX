package com.dtdsoftware.splunk.formatter;

import java.net.InetAddress;
import java.util.Map;

/**
 * Abstract out a few common methods and params
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public abstract class SplunkFormatter {

	// first part of the SPLUNK output String, common to all MBeans
	StringBuffer outputPrefix;

	static final String KEY_VAL_DELIMITER_PARAM = "kvdelim";
	static final String PAIR_DELIMITER_PARAM = "pairdelim";

	String kvdelim = "=";// default
	String pairdelim = ",";// default

	/**
	 * Build a key value pair based on configured delimiters
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	String buildPair(String key, String value) {

		StringBuffer result = new StringBuffer();
		result.append(key).append(kvdelim).append("\"").append(value).append(
				"\"").append(pairdelim);

		return result.toString();
	}

	/**
	 * set parameters
	 * 
	 * @param parameters
	 */
	void setCommonSplunkParameters(Map<String, String> parameters) {

		String kv = parameters.get(KEY_VAL_DELIMITER_PARAM);
		if (kv != null && kv.length() > 0)
			kvdelim = kv;

		String pair = parameters.get(PAIR_DELIMITER_PARAM);
		if (pair != null && pair.length() > 0)
			pairdelim = pair;

	}

	/**
	 * format common meta data
	 * 
	 * @param metaData
	 */
	void setCommonSplunkMetaData(Map<String, String> metaData) {

		String outputHostName = "";
		String configuredHostName = metaData.get(Formatter.META_HOST);
		String pid = metaData.get(Formatter.META_PROCESS_ID);
		String jvmDescription = metaData.get(Formatter.META_JVM_DESCRIPTION);

		// replace localhost names with actual hostname
		if (configuredHostName.equalsIgnoreCase("localhost")
				|| configuredHostName.equals("127.0.0.1") || pid != null) {
			try {
				outputHostName = InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
			}
		} else {
			outputHostName = configuredHostName;
		}

		this.outputPrefix.append(buildPair("host", outputHostName));
		this.outputPrefix.append(buildPair("jvmDescription", jvmDescription));

		if (pid != null) {

			this.outputPrefix.append(buildPair("pid", pid));
		}

	}

}
