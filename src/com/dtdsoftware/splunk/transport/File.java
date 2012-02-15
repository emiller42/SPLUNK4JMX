package com.dtdsoftware.splunk.transport;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transport that outputs to file
 * 
 * Uses the file.logger logger in the underlying log configuration file
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 *
 */
public class File implements Transport {

private Logger logger;
	
	@Override
	public void setParameters(Map<String, String> parameters) {
		//do nothing
	}

	@Override
	/**
	 * Write payload to File
	 * 
	 */
	public void transport(String payload) {

		//use the logging appender
		logger.info(payload);
	}

	@Override
	public void open() {

		logger = LoggerFactory.getLogger("file.logger");
	}

	@Override
	public void close() {
		//do nothing
	}


}
