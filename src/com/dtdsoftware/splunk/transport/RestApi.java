package com.dtdsoftware.splunk.transport;

import java.util.Map;

/**
 * Transport implementation that uses the Splunk Java SDK to send events to the
 * receivers/simple or receivers/stream REST endpoints
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class RestApi implements Transport {

	//connection parameters
	private static final String USER_PARAM = "user";
	private static final String PASS_PARAM = "pass";
	private static final String HOST_PARAM = "host";
	private static final String PORT_PARAM = "port";
	private static final String DELIVERY_PARAM = "delivery";
	
	//event meta field parameters
	private static final String META_INDEX_PARAM = "meta_index";
	private static final String META_SOURCE_PARAM = "meta_source";
	private static final String META_SOURCETYPE_PARAM = "meta_sourcetype";
	private static final String META_HOSTREGEX_PARAM = "meta_hostregex";
	
	//connection
	private String user="";
	private String pass="";
	private String host="";
	private int port=8089;
	private String delivery="stream"; //stream or simple
	
	//event meta data
	private String metaSource = "rest";
	private String metaSourcetype = "jmx";
	private String metaIndex = "jmx";
	private String metaHostRegex = "host=(([a-zA-Z0-9\\._-]+))";
	
	//TODO uncomment when SDK librarys are present
	//private Service service;
	  
	@Override
	public void setParameters(Map<String, String> parameters) {
		
		String userparam = parameters.get(USER_PARAM);
		if (userparam != null && userparam.length() > 0)
			user = userparam;
		
		String passparam = parameters.get(PASS_PARAM);
		if (passparam != null && passparam.length() > 0)
			pass = passparam;
			
		
		String hostparam = parameters.get(HOST_PARAM);
		if (hostparam != null && hostparam.length() > 0)
			host = hostparam;
		
		String portparam = parameters.get(PORT_PARAM);
		if (portparam != null && portparam.length() > 0){
			try {
				port = Integer.parseInt(portparam);
			} catch (NumberFormatException e) {
				
			}
		}		
		String deliveryparam = parameters.get(DELIVERY_PARAM);
		if (deliveryparam != null && deliveryparam.length() > 0)
			delivery = deliveryparam;		
		
		String metaIndexparam = parameters.get(META_INDEX_PARAM);
		if (metaIndexparam != null && metaIndexparam.length() > 0)
			metaIndex = metaIndexparam;
		
		String metaSourceparam = parameters.get(META_SOURCE_PARAM);
		if (metaSourceparam != null && metaSourceparam.length() > 0)
			metaSource = metaSourceparam;
		
		String metaSourceTypeparam = parameters.get(META_SOURCETYPE_PARAM);
		if (metaSourceTypeparam != null && metaSourceTypeparam.length() > 0)
			metaSourcetype = metaSourceTypeparam;
		
		String metaHostRegexparam = parameters.get(META_HOSTREGEX_PARAM);
		if (metaHostRegexparam != null && metaHostRegexparam.length() > 0)
			metaHostRegex = metaHostRegexparam;
		
		//TODO uncomment when SDK librarys are present
		//this.service = new Service(host, port);
        //service.login(user, pass);

	}
	
	@Override
	/**
	 * Write payload to REST endpoint
	 * 
	 * TODO
	 */
	public void transport(String payload) {
		
		/**
		 * 
        
        if (delivery.equals("stream") ) {
               
        	//TODO
        }
        else if (delivery.equals("simple") ) {
            
        	//TODO
        }
        else{
        	//no other delivery types supported
        }
        
        **/


	}

}
