package com.dtdsoftware.splunk.config;

import java.util.List;

/**
 * POJO for an MBean
 * 
 * For MBean definitions , standard JMX object name wildcard patterns * and ?
 * supported for the domain and properties string attributes
 * http://download.oracle
 * .com/javase/1,5.0/docs/api/javax/management/ObjectName.html
 * 
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class MBean {

	// MBean domain literal string or pattern
	public String domain = "";

	// MBean properties list string or pattern in "key=value, key2=value2"
	// format
	public String propertiesList = "";


	public List<Attribute> attributes;

	public MBean() {
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getPropertiesList() {
		return propertiesList;
	}

	public void setPropertiesList(String propertiesList) {
		this.propertiesList = propertiesList;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

}
