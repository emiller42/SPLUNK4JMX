package com.dtdsoftware.splunk.config;

import java.util.List;

/**
 * Root config POJO
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class JMXPoller {

	// the list of JMX Servers to connect to
	public List<JMXServer> servers;

	// a custom formatter
	public Formatter formatter;

	public JMXPoller() {
	}

	public List<JMXServer> getServers() {
		return servers;
	}

	public void setServers(List<JMXServer> servers) {
		this.servers = servers;
	}

	public Formatter getFormatter() {
		return formatter;
	}

	public void setFormatter(Formatter formatter) {
		this.formatter = formatter;
	}

}
