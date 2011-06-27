package com.dtdsoftware.splunk;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

import com.dtdsoftware.splunk.config.JMXServer;
import com.dtdsoftware.splunk.config.MBean;
import com.dtdsoftware.splunk.config.Attribute;
import com.dtdsoftware.splunk.config.Operation;
import com.dtdsoftware.splunk.formatter.Formatter;
import com.sun.tools.attach.VirtualMachine;

/**
 * Thread to lookup MBeans and Attributes for each JMX Server and write out a
 * formatted String to SYSOUT for SPLUNK
 * 
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class ProcessServerThread extends Thread {

	
	private Logger logger;
	
	private MBeanServerConnection serverConnection;
	private JMXConnector jmxc;

	private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";

	private JMXServer serverConfig;
	
	private boolean directJVMAttach = false;
	
	//output formatter
	private Formatter formatter;

	/**
	 * Thread to run each JMX Server connection in
	 * @param serverConfig config POJO for this JMX Server
	 * @param formatter config POJO for the formatter
	 */
	public ProcessServerThread(JMXServer serverConfig,Formatter formatter) {

		this.logger = Logger.getLogger(this.getName());		
		this.serverConfig = serverConfig;		
		this.formatter = formatter;
		
		
		//set up the formatter
		Map <String,String>meta = new HashMap<String,String>();
		
		if(serverConfig.getProcessID() > 0){
			this.directJVMAttach = true;
			meta.put(Formatter.META_PROCESS_ID, String.valueOf(serverConfig.getProcessID()));
		}
		
		meta.put(Formatter.META_HOST, this.serverConfig.getHost());
		meta.put(Formatter.META_JVM_DESCRIPTION, this.serverConfig.getJvmDescription());
		
		formatter.setMetaData(meta);
		
		
	}

	@Override
	public void run() {
		try {

			// establish connection to JMX Server
			connect();

			// get list of MBeans to Query
			List<MBean> mbeans = serverConfig.getMbeans();
			if (mbeans != null) {
				for (MBean bean : mbeans) {

					// the list of queried MBeans found on the server
					// if no values are specified for domain and properties
					// attributes , the value will default to the * wildcard
					Set<ObjectInstance> foundBeans = serverConnection
							.queryMBeans(new ObjectName(
									(bean.getDomain().length() == 0 ? "*"
											: bean.getDomain())
											+ ":"
											+ (bean.getPropertiesList()
													.length() == 0 ? "*" : bean
													.getPropertiesList())),
									null);

					for (ObjectInstance oi : foundBeans) {
						ObjectName on = oi.getObjectName();
						// the mbean specific part of the SPLUNK output String
						String mBeanName=on.getCanonicalName();
						Map <String,String>mBeanAttributes = new HashMap<String,String>();
						
						//execute operations
						if (bean.getOperations() != null) {
						
							for(Operation operation : bean.getOperations()){
								try{
								Object result = serverConnection.invoke(on, operation.getName(), operation.getParametersArray(),operation.getSignatureArray());
								String outputname = operation.getOutputname();
								if(outputname !=null && !outputname.isEmpty())
								  mBeanAttributes.put(operation.getOutputname(), result.toString());
								}
								catch(Exception e){logger.error("Error : "+e.getMessage());}
							}
						}
                        //extract attributes
						if (bean.getAttributes() != null) {

							// look up the attribute for the MBean
							for (Attribute singular : bean.getAttributes()) {
								List<String> tokens = singular.getTokens();
								Object attributeValue = null;
								// if the attribute pattern is multi level, loop
								// through the levels until the value is found
								for (String token : tokens) {
									// get root attribute object the first time
									if (attributeValue == null)
										try {
											attributeValue = serverConnection
													.getAttribute(on, token);
										} catch (Exception e) {
											logger.error("Error : "+e.getMessage());
										}
									else if (attributeValue instanceof CompositeDataSupport) {
										try {
											attributeValue = ((CompositeDataSupport) attributeValue)
													.get(token);
										} catch (Exception e) {
											logger.error("Error : "+e.getMessage());
										}
									} else if (attributeValue instanceof TabularDataSupport) {
										try {
											Object[] key = { token };
											attributeValue = ((TabularDataSupport) attributeValue)
													.get(key);
											attributeValue = ((CompositeDataSupport) attributeValue)
													.get("value");
										} catch (Exception e) {
											logger.error("Error : "+e.getMessage());
										}
									} else {
									}
								}

								String splunkValue = "";
								if (attributeValue != null) {
									if (attributeValue instanceof Object[]) {
										splunkValue = Arrays
												.toString((Object[]) attributeValue);
									} else {
										splunkValue = attributeValue.toString();
									}
								}

								
								mBeanAttributes.put(singular.getOutputname(), splunkValue);
								
							}

						}
						// write line out to SYSOUT
						formatter.print(mBeanName,mBeanAttributes,System.currentTimeMillis());
					}

				}

			}

		} catch (Exception e) {
			logger.error("Error : "+e.getMessage());
		}
		finally{
			if(jmxc != null){
			  try{jmxc.close();}catch(Exception e){}
			}
			
		}
		

	}

	/**
	 * Connect to the JMX Server
	 * 
	 * @throws Exception
	 */
	private void connect() throws Exception {

		// get the JMX URL
		JMXServiceURL url = getJMXServiceURL();


		// only send user/pass credentials if they have been set
		if (serverConfig.getJmxuser().length() > 0
				&& serverConfig.getJmxpass().length() > 0 && !this.directJVMAttach) {
			Map<String, String[]> env = new HashMap<String, String[]>();
			String[] creds = { serverConfig.getJmxuser(),
					serverConfig.getJmxpass() };
			env.put(JMXConnector.CREDENTIALS, creds);

			jmxc = JMXConnectorFactory.connect(url, env);

		} else {
			jmxc = JMXConnectorFactory.connect(url);
		}
		
		serverConnection = jmxc.getMBeanServerConnection();

	}

	/**
	 * Get a JMX URL
	 * @return the URL
	 * @throws Exception
	 */
	private JMXServiceURL getJMXServiceURL() throws Exception {
		JMXServiceURL url = null;

		// connect to local process
		if (serverConfig.getProcessID() > 0) {

			
			url = getURLForPid(serverConfig.getProcessID());
		}
		// connect to a remote process
		else {
			String urlPath = "/jndi/rmi://" + serverConfig.getHost() + ":"
					+ serverConfig.getJmxport() + "/jmxrmi";
			url = new JMXServiceURL("rmi", "", 0, urlPath);
		}

		return url;

	}

	/**
	 * Get a JMX URL for a process ID
	 * @param pid
	 * @return the URL
	 * @throws Exception
	 */
	private static JMXServiceURL getURLForPid(int pid) throws Exception {

		// attach to the target application
		final VirtualMachine vm = VirtualMachine.attach(String.valueOf(pid));

		// get the connector address
		String connectorAddress = vm.getAgentProperties().getProperty(
				CONNECTOR_ADDRESS);

		// no connector address, so we start the JMX agent
		if (connectorAddress == null) {
			String agent = vm.getSystemProperties().getProperty("java.home")
					+ File.separator + "lib" + File.separator
					+ "management-agent.jar";
			vm.loadAgent(agent);

			// agent is started, get the connector address
			connectorAddress = vm.getAgentProperties().getProperty(
					CONNECTOR_ADDRESS);
			assert connectorAddress != null;
		}
		return new JMXServiceURL(connectorAddress);
	}
}
