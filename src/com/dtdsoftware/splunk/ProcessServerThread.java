package com.dtdsoftware.splunk;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
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

	// output formatter
	private Formatter formatter;

	/**
	 * Thread to run each JMX Server connection in
	 * 
	 * @param serverConfig
	 *            config POJO for this JMX Server
	 * @param formatter
	 *            config POJO for the formatter
	 */
	public ProcessServerThread(JMXServer serverConfig, Formatter formatter) {

		this.logger = Logger.getLogger(this.getName());
		this.serverConfig = serverConfig;
		this.formatter = formatter;

		// set up the formatter
		Map<String, String> meta = new HashMap<String, String>();

		if (serverConfig.getProcessID() > 0) {
			this.directJVMAttach = true;
			meta.put(Formatter.META_PROCESS_ID, String.valueOf(serverConfig
					.getProcessID()));
		}

		meta.put(Formatter.META_HOST, this.serverConfig.getHost());
		meta.put(Formatter.META_JVM_DESCRIPTION, this.serverConfig
				.getJvmDescription());

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
						String mBeanName = on.getCanonicalName();
						Map<String, String> mBeanAttributes = new HashMap<String, String>();

						// execute operations
						if (bean.getOperations() != null) {

							for (Operation operation : bean.getOperations()) {
								try {
									Object result = serverConnection.invoke(on,
											operation.getName(), operation
													.getParametersArray(),
											operation.getSignatureArray());
									String outputname = operation
											.getOutputname();
									if (outputname != null
											&& !outputname.isEmpty())
										mBeanAttributes.put(operation
												.getOutputname(),
												resolveObjectToString(result));
								} catch (Exception e) {

									logger.error("Error : " + e.getMessage());
								}
							}
						}
						// extract all attributes
						if (bean.isDumpAllAttributes()) {
							MBeanAttributeInfo[] attributes = serverConnection
									.getMBeanInfo(on).getAttributes();
							for (MBeanAttributeInfo attribute : attributes) {
								try {
									Object attributeValue = serverConnection
											.getAttribute(on, attribute
													.getName());
									extractAttributeValue(attributeValue,
											mBeanAttributes, attribute
													.getName());
								} catch (Exception e) {

									logger.error("Error : " + e.getMessage());
								}

							}

						}
						// extract attributes
						else if (bean.getAttributes() != null) {

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

											logger.error("Error : "
													+ e.getMessage());
										}
									else if (attributeValue instanceof CompositeDataSupport) {
										try {

											attributeValue = ((CompositeDataSupport) attributeValue)
													.get(token);
										} catch (Exception e) {

											logger.error("Error : "
													+ e.getMessage());
										}
									} else if (attributeValue instanceof TabularDataSupport) {
										try {

											Object[] key = { token };

											attributeValue = ((TabularDataSupport) attributeValue)
													.get(key);

										} catch (Exception e) {

											logger.error("Error : "
													+ e.getMessage());
										}
									} else {
									}
								}

								mBeanAttributes.put(singular.getOutputname(),
										resolveObjectToString(attributeValue));

							}

						}
						// write line out to SYSOUT
						formatter.print(mBeanName, mBeanAttributes, System
								.currentTimeMillis());
					}

				}

			}

		} catch (Exception e) {

			logger.error(serverConfig+",systemErrorMessage=\"" + e.getMessage()+"\"");
		} finally {
			if (jmxc != null) {
				try {
					jmxc.close();
				} catch (Exception e) {
				}
			}

		}

	}


	/**
	 * Extract MBean attributes and if necessary, deeply inspect and resolve
	 * composite and tabular data.
	 * 
	 * @param attributeValue
	 *            the attribute object
	 * @param mBeanAttributes
	 *            the map used to hold attribute values before being handed off
	 *            to the formatter
	 * @param attributeName
	 *            the attribute name
	 */
	private void extractAttributeValue(Object attributeValue,
			Map<String, String> mBeanAttributes, String attributeName) {


		if (attributeValue instanceof Object[]) {
			try {
				int index = 0;
				for (Object obj : (Object[]) attributeValue) {
					index++;
					extractAttributeValue(obj, mBeanAttributes, attributeName
							+ "_" + index);
				}
			} catch (Exception e) {

				logger.error("Error : " + e.getMessage());
			}
		} 
		else if (attributeValue instanceof Collection) {
			try {
				int index = 0;
				for (Object obj : (Collection)attributeValue) {
					index++;
					extractAttributeValue(obj, mBeanAttributes, attributeName
							+ "_" + index);
				}
			} catch (Exception e) {

				logger.error("Error : " + e.getMessage());
			}
		} 
		else if (attributeValue instanceof CompositeDataSupport) {
			try {
				CompositeDataSupport cds = ((CompositeDataSupport) attributeValue);
				CompositeType ct = cds.getCompositeType();

				Set<String> keys = ct.keySet();

				for (String key : keys) {
					extractAttributeValue(cds.get(key), mBeanAttributes,
							attributeName + "_" + key);
				}

			} catch (Exception e) {

				logger.error("Error : " + e.getMessage());
			}
		} else if (attributeValue instanceof TabularDataSupport) {
			try {
				TabularDataSupport tds = ((TabularDataSupport) attributeValue);
				Set<Object> keys = tds.keySet();
				for (Object key : keys) {

					Object keyName = ((List) key).get(0);
					Object[] keyArray = { keyName };
					extractAttributeValue(tds.get(keyArray), mBeanAttributes,
							attributeName + "_" + keyName);
				}

			} catch (Exception e) {
				logger.error("Error : " + e.getMessage());
			}
		} else {

			try {
				mBeanAttributes.put(attributeName,
						resolveObjectToString(attributeValue));
			} catch (Exception e) {

				logger.error("Error : " + e.getMessage());
			}
		}

	}

	/**
	 * Resolve an Object to a String representation. Arrays, Lists, Sets and
	 * Maps will be recursively deep resolved
	 * 
	 * @param obj
	 * @return
	 */
	private String resolveObjectToString(Object obj) {

		StringBuffer sb = new StringBuffer();
		if (obj != null) {

			// convert an array to a List view
			if (obj instanceof Object[]) {
				obj = Arrays.asList((Object[]) obj);
			}

			if (obj instanceof Map) {
				sb.append("[");
				Map map = (Map) obj;
				Set keys = map.keySet();
				int totalEntrys = keys.size();
				int index = 0;
				for (Object key : keys) {
					index++;
					Object value = map.get(key);
					sb.append(resolveObjectToString(key));
					sb.append("=");
					sb.append(resolveObjectToString(value));
					if (index < totalEntrys)
						sb.append(",");
				}
				sb.append("]");
			} else if (obj instanceof List) {
				sb.append("[");
				List list = (List) obj;
				int totalEntrys = list.size();
				int index = 0;
				for (Object item : list) {
					index++;
					sb.append(resolveObjectToString(item));
					if (index < totalEntrys)
						sb.append(",");
				}
				sb.append("]");
			} else if (obj instanceof Set) {
				sb.append("[");
				Set set = (Set) obj;
				int totalEntrys = set.size();
				int index = 0;
				for (Object item : set) {
					index++;
					sb.append(resolveObjectToString(item));
					if (index < totalEntrys)
						sb.append(",");
				}
				sb.append("]");
			} else {
				sb.append(obj.toString());
			}
		}
		return sb.toString();

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
				&& serverConfig.getJmxpass().length() > 0
				&& !this.directJVMAttach) {
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
	 * 
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
			String rawURL = serverConfig.getJmxServiceURL();
			String protocol = serverConfig.getProtocol();
			// use raw URL
			if (rawURL != null && rawURL.length() > 0) {
				url = new JMXServiceURL(rawURL);
			}
			// construct URL for MX4J connectors
			else if (protocol.startsWith("soap")
					|| protocol.startsWith("hessian")
					|| protocol.startsWith("burlap")
					|| protocol.equalsIgnoreCase("local")) {

				String lookupPath = serverConfig.getLookupPath();
				if (lookupPath == null || lookupPath.length() == 0) {
					if (protocol.startsWith("soap")) {
						lookupPath = "/jmxconnector";

					} else {
						lookupPath = "/" + protocol;
					}
				}
				url = new JMXServiceURL(serverConfig.getProtocol(),
						serverConfig.getHost(), serverConfig.getJmxport(),
						lookupPath);
			}
			// use remote encoded stub for JSR160 iiop and rmi
			else if (serverConfig.getStubSource().equalsIgnoreCase("ior")
					|| serverConfig.getStubSource().equalsIgnoreCase("stub")) {
				url = new JMXServiceURL(protocol, "", 0, "/"
						+ serverConfig.getStubSource() + "/"
						+ serverConfig.getEncodedStub());
			}
			// use jndi lookup for JSR160 iiop and rmi stub
			else if (serverConfig.getStubSource().equalsIgnoreCase("jndi")) {

				String lookupPath = serverConfig.getLookupPath();
				if (lookupPath == null || lookupPath.length() == 0) {
					lookupPath = "/jmxrmi";
				}

				String urlPath = "/" + serverConfig.getStubSource() + "/"
						+ protocol + "://" + serverConfig.getHost() + ":"
						+ serverConfig.getJmxport() + lookupPath;

				url = new JMXServiceURL(protocol, "", 0, urlPath);
			}

		}

		return url;

	}

	/**
	 * Get a JMX URL for a process ID
	 * 
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
