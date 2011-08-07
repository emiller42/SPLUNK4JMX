package com.dtdsoftware.splunk.formatter;

import java.net.InetAddress;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Custom formatter implementation that outputs the mbean canonical name as split up tokens
 * Has some extra formatting specifics to deal with MBean property values that are sometimes quoted.
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class TokenizedMBeanNameQuotesStrippedFormatter implements Formatter {

	// first part of the SPLUNK output String, common to all MBeans
	private StringBuffer outputPrefix;

	public TokenizedMBeanNameQuotesStrippedFormatter() {

		this.outputPrefix = new StringBuffer();
	}

	@Override
	public void print(String mBean, Map<String, String> attributes,
			long timestamp) {

		// not using the timestamp, using the SPLUNK index time insteade

		// append the common prefix
		StringBuffer output = new StringBuffer();
		output.append(outputPrefix);

		SortedMap<String, String> mbeanNameParts = tokenizeMBeanCanonicalName(mBean);

		Set<String> mBeanNameKeys = mbeanNameParts.keySet();

		for (String key : mBeanNameKeys) {

			output.append(",").append(key).append("=\"").append(
					mbeanNameParts.get(key)).append("\"");
		}

		// add mbean attributes
		Set<String> keys = attributes.keySet();
		for (String key : keys) {

			output.append(",").append(key).append("=\"").append(
					attributes.get(key)).append("\"");
		}

		// write out to STDOUT for Splunk
		System.out.println(output.toString());

	}

	/**
	 * Take a canonical mbean name  ie: "domain:key=value, key2=value2" , and split out the parts into individual fields.
	 * @param mBean the canonical mbean name
	 * @return sorted map of the name parts
	 */
	private SortedMap<String, String> tokenizeMBeanCanonicalName(String mBean) {

		SortedMap<String, String> result = new TreeMap<String, String>();

		String[] parts = mBean.split(":");
		if(parts == null || parts.length != 2){
			return result;
		}
		//the mbean domain
		result.put("mbean_domain", parts[0]);

		//the mbean properties
		String[] properties = parts[1].split(",");
		if(properties == null){
			return result;
		}
		for (String prop : properties) {
			String[] property = prop.split("=");
			if(property == null || property.length != 2){
				continue;
			}
			
			result.put("mbean_property_" + property[0], trimQuotes(property[1]));
		}

		return result;
	}

	private String trimQuotes(String quotedString) {
		
		if(quotedString.startsWith("\"")){
			quotedString=quotedString.substring(1);
		}
		if(quotedString.endsWith("\"")){
			quotedString=quotedString.substring(0, quotedString.length()-1);
		}
		
		
		return quotedString;
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
				",jvmDescription=\"").append(jvmDescription).append("\"");

		if (pid != null) {
			this.outputPrefix.append(",pid=").append(pid);
		}

	}

}
