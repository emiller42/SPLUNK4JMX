package com.dtdsoftware.splunk;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

import com.dtdsoftware.splunk.config.Formatter;
import com.dtdsoftware.splunk.config.JMXPoller;
import com.dtdsoftware.splunk.config.JMXServer;
import com.dtdsoftware.splunk.config.Transport;

/**
 * Processes an XML config file that specifies a list of JMX Servers, MBeans on
 * those Servers, and attributes of those MBeans which are to be read and
 * written out to a SYSOUT stream for a SPLUNK "scripted input" to read and
 * index.
 * 
 * Each JMX Server in the config file will be run in its own thread.
 * 
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class JMXMBeanPoller {

	private static Logger logger = LoggerFactory
			.getLogger(JMXMBeanPoller.class);

	/**
	 * Application entry point Usage : java
	 * com.dtdsoftware.splunk.JMXMBeanPoller [configFile]
	 * 
	 * @param args
	 *            array of length 1 containing the path of the config xml file
	 */
	public static void main(String[] args) {

		logger.info("Starting JMX Poller");
		try {
			// the path to the XML config file
			if (args.length != 1)
				throw new Exception(
						"Incorrect program usage.Expected : java com.dtdsoftware.splunk.JMXMBeanPoller [configFile] ");
			// parse XML config into POJOs
			JMXPoller config = loadConfig(args[0]);
			config.normalizeClusters();
			Formatter formatter = config.getFormatter();
			if (formatter == null) {
				formatter = new Formatter();// default
			}
			Transport transport = config.getTransport();
			if (transport == null) {
				transport = new Transport();// default
			}
			if (config != null) {
				// get list of JMX Servers and process in their own thread.
				List<JMXServer> servers = config.normalizeMultiPIDs();
				if (servers != null) {

					for (JMXServer server : servers) {
						new ProcessServerThread(server,
								formatter.getFormatterInstance(),
								transport.getTransportInstance()).start();
					}
				} else {
					logger.error("No JMX servers have been specified");
				}
			} else{
				logger.error("The root config object(JMXPoller) failed to initialize");
			}
		} catch (Exception e) {

			logger.error("Error : " + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Parse the config XML into Java POJOs and validate against XSD
	 * 
	 * @param configFileName
	 * @return The configuration POJO root
	 * @throws Exception
	 */
	private static JMXPoller loadConfig(String configFileName) throws Exception {

		
		File file = new File(configFileName);
		if (file.isDirectory()) {
			throw new Exception(
					configFileName
							+ " is a directory, you must pass the file NAME to this program and this file must adhere to the config.xsd schema");
		} else if (!file.exists()) {
			throw new Exception("The config file " + configFileName
					+ " does not exist");
		}
		// xsd validation
		FileReader fr = new FileReader(file);
		InputSource inputSource = new InputSource(fr);
		SchemaValidator validator = new SchemaValidator();
		validator.validateSchema(inputSource);

		// use CASTOR to parse XML into Java POJOs
		Mapping mapping = new Mapping();
		URL mappingURL = JMXPoller.class.getResource("/mapping.xml");
		mapping.loadMapping(mappingURL);
		Unmarshaller unmar = new Unmarshaller(mapping);

		// for some reason the xsd validator closes the file stream, so re-open
		fr = new FileReader(file);
		inputSource = new InputSource(fr);

		JMXPoller poller = (JMXPoller) unmar.unmarshal(inputSource);

		
		return poller;

	}

}
