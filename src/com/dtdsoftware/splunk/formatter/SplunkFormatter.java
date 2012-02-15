package com.dtdsoftware.splunk.formatter;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
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
	static final String QUOTE_CHAR_PARAM = "quotechar";
	static final String QUOTEVALUES_PARAM = "quotevalues";
	static final String PREPENDED_DATE_FORMAT_PARAM = "dateformat";
	static final String PREPEND_DATE_PARAM = "prependDate";

	String kvdelim = "=";// default
	String pairdelim = ",";// default
	char quotechar = '"';// default
	boolean prependDate = false;//default
	boolean quotevalues = true;//default
	/**
	 * default date format is using internal generated date
	 */
	private static final String DEFAULT_DATEFORMATPATTERN = "yyyy-MM-dd HH:mm:ss:SSSZ";
	/**
	 * Date Formatter instance
	 */
	private SimpleDateFormat DATEFORMATTER = new SimpleDateFormat(
			DEFAULT_DATEFORMATPATTERN);

	
	
	/**
	 * Build a key value pair based on configured delimiters
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	String buildPair(String key, String value) {

		return buildPair(key,value,quotevalues);
	}

	/**
	 * Build a key value pair based on configured delimiters
	 * 
	 * @param key
	 * @param value
	 * @param quote whether or not to quote values
	 * @return
	 */
	String buildPair(String key, String value,boolean quoteVal) {

		StringBuffer result = new StringBuffer();
		if (quoteVal)
			result.append(key).append(kvdelim).append(quotechar).append(
					value).append(quotechar).append(pairdelim);
		else
			result.append(key).append(kvdelim).append(value).append(
					pairdelim);
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
		
		String qc  = parameters.get(QUOTE_CHAR_PARAM);
		if (qc != null && qc.length() == 1)
			quotechar = qc.toCharArray()[0];
		
		String quote = parameters.get(QUOTEVALUES_PARAM);
		if (quote != null && quote.length() > 0){
			quotevalues= Boolean.parseBoolean(quote);
		}
		
		String prependDateOption = parameters.get(PREPEND_DATE_PARAM);
		if (prependDateOption != null && prependDateOption.length() > 0){
			prependDate = Boolean.parseBoolean(prependDateOption);
		}
		
		String predateformat = parameters.get(PREPENDED_DATE_FORMAT_PARAM);
		if (predateformat != null && predateformat.length() > 0){
			DATEFORMATTER = new SimpleDateFormat(predateformat);
		}

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

		this.outputPrefix.append(buildPair("host", outputHostName,false));
		this.outputPrefix.append(buildPair("jvmDescription", jvmDescription));

		if (pid != null) {

			this.outputPrefix.append(buildPair("pid", pid));
		}

	}
	
	void prependDate(long timestamp, StringBuffer output) {
		
		if(prependDate){
			output.append(DATEFORMATTER.format(new Date(timestamp))).append(" ");
		}
		
	}

}
