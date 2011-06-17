package com.dtdsoftware.splunk.config;

import java.util.ArrayList;
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
	
	// a list of JMX Server Clusters
	public List<Cluster> clusters;

	// a custom formatter
	public Formatter formatter;

	public JMXPoller() {
	}

	public List<JMXServer> getServers() {
		return servers;
	}

	public void setServers(List<JMXServer> servers) {
				
		if(this.servers != null){
			this.servers.addAll(servers);
		}
		else
			this.servers = servers;
		
	}

	public Formatter getFormatter() {
		return formatter;
	}

	public void setFormatter(Formatter formatter) {
		this.formatter = formatter;
	}

	public List<Cluster> getClusters() {
		return clusters;
	}

	/**
	 * Set the clusters and resolve the JMXServer objects
	 * @param clusters
	 */
	public void setClusters(List<Cluster> clusters) {
		this.clusters = clusters;
		
		if(this.servers == null){
			this.servers=new ArrayList<JMXServer>();
		}
		for(Cluster cluster:clusters){
			
			List <MBean>mbeans = cluster.getMbeans();
			List <JMXServer>clusterServers = cluster.getServers();
			for(JMXServer server:clusterServers){
				server.setMbeans(mbeans);
				this.servers.add(server);
			}
		}
	}
	
	

}
