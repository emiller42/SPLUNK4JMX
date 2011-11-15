package com.dtdsoftware.splunk.formatter;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

/**
 * Default formatter implementation
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class DefaultFormatter implements Formatter {

	// first part of the SPLUNK output String, common to all MBeans
	private StringBuffer outputPrefix;

	public DefaultFormatter() {

		this.outputPrefix = new StringBuffer();
	}

	@Override
	public void print(String mBean, Map<String, String> attributes,
			long timestamp) {

		// not using the timestamp, using the SPLUNK index time insteade

		// append the common prefix
		StringBuffer output = new StringBuffer();
		output.append(outputPrefix);

		// append the mbean name
		output.append("mbean=\"").append(mBean).append("\"");

		// add mbean attributes
		Set<String> keys = attributes.keySet();
		for (String key : keys) {

			output.append(",").append(key).append("=\"").append(
					FormatterUtils.stripNewlines(attributes.get(key))).append("\"");
		}

		// write out to STDOUT for Splunk
		System.out.println(output.toString());

	}

	@Override
	public void setMetaData(Map<String, String> metaData) {

		String outputHostName = "";
		String configuredHostName = metaData.get(META_HOST);
		String pid = metaData.get(META_PROCESS_ID);
		String jvmDescription = metaData.get(META_JVM_DESCRIPTION);

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

		this.outputPrefix.append("host=").append(outputHostName).append(
				",jvmDescription=\"").append(jvmDescription).append("\",");

		if (pid != null) {
			this.outputPrefix.append("pid=").append(pid).append(",");
		}

	}

}
