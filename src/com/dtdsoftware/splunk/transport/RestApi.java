package com.dtdsoftware.splunk.transport;


import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <pre>
 * Transport implementation that uses the Splunk Java SDK to send events to the
 * receivers/simple or receivers/stream REST endpoints
 * 
 * Uses the rest.logger logger in the underlying log configuration file
 * 
 * </pre>
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class RestApi implements Transport {


	private Logger logger;
	
	@Override
	public void setParameters(Map<String, String> parameters) {
		//do nothing
	}

	@Override
	/**
	 * Write payload to REST endpoint
	 * 
	 */
	public void transport(String payload) {

		//use the logging appender
		logger.info(payload);
	}

	@Override
	public void open() {

		logger = LoggerFactory.getLogger("rest.logger");
	}

	@Override
	public void close() {
		//do nothing
	}

	
}
