package com.dtdsoftware.splunk.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * POJO for a JMX Server
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class JMXServer {

	private static Logger logger = Logger.getLogger(JMXServer.class);

	// PID of locally running JVM
	public int processID;

	// PID File of locally running JVM
	public String pidFile;

	// JMX hostname, dns alias, ip address
	public String host = "";

	// meta data description of the JVM being connected to
	public String jvmDescription;

	// remote JMX port
	public int jmxport;

	// JMX username
	public String jmxuser = "";

	// JMX password
	public String jmxpass = "";

	// list of MBeans/MBeans Patterns to Query
	public List<MBean> mbeans;

	public JMXServer() {
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getJvmDescription() {
		return jvmDescription;
	}

	public void setJvmDescription(String jvmDescription) {

		this.jvmDescription = jvmDescription;
	}

	public int getJmxport() {
		return jmxport;
	}

	public void setJmxport(int jmxport) {
		this.jmxport = jmxport;
	}

	public String getJmxuser() {
		return jmxuser;
	}

	public void setJmxuser(String jmxuser) {
		this.jmxuser = jmxuser;
	}

	public String getJmxpass() {
		return jmxpass;
	}

	public void setJmxpass(String jmxpass) {
		this.jmxpass = jmxpass;
	}

	public List<MBean> getMbeans() {
		return mbeans;
	}

	public void setMbeans(List<MBean> mbeans) {
		this.mbeans = mbeans;
	}

	public int getProcessID() {
		return processID;
	}

	public void setProcessID(int processID) {
		this.processID = processID;
	}

	public String getPidFile() {
		return pidFile;
	}

	/**
	 * Attempts to obtain the pid from a pidFile
	 * @param pidFile
	 */
	public void setPidFile(String pidFile) {
		this.pidFile = pidFile;
		try {
			File f = new File(pidFile);
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = br.readLine();
			setProcessID(Integer.parseInt(line));
			br.close();
		} catch (Exception e) {
			logger.error("Error obtaining pid from file " + pidFile);
		}

	}

}
