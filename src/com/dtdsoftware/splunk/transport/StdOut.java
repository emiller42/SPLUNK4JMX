package com.dtdsoftware.splunk.transport;

import java.util.Map;

/**
 * <pre>
 * Default transport.
 * 
 * Very simple transport implementation that writes to STD OUT
 * 
 * </pre>
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class StdOut implements Transport {

	public StdOut() {
	}

	@Override
	/**
	 * this transport doesn't use params
	 */
	public void setParameters(Map<String, String> parameters) {
		// do nothing
	}

	@Override
	/**
	 * Write payload to STD OUT
	 */
	public void transport(String payload) {

		System.out.println(payload);

	}

	@Override
	public void close() {
		// do nothing
		
	}

	@Override
	public void open() {
		// do nothing
		
	}

}
