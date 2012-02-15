package com.dtdsoftware.splunk.transport;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transport the sends events over a TCP socket
 * 
 * Uses the tcp.logger logger in the underlying log configuration file
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 *
 */
public class TCP implements Transport {

private Logger logger;
	
	@Override
	public void setParameters(Map<String, String> parameters) {
		//do nothing
	}

	@Override
	/**
	 * Write payload to TCP stream
	 * 
	 */
	public void transport(String payload) {

		//use the log4j appender
		logger.info(payload);
	}

	@Override
	public void open() {

		logger = LoggerFactory.getLogger("tcp.logger");
	}

	@Override
	public void close() {
		//do nothing
	}


}
