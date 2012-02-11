package com.dtdsoftware.splunk.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Map;

import com.splunk.Args;
import com.splunk.Receivers;
import com.splunk.Service;

/**
 * Transport implementation that uses the Splunk Java SDK to send events to the
 * receivers/simple or receivers/stream REST endpoints
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class RestApi implements Transport {

	public static final String STREAM = "stream";
	public static final String SIMPLE = "simple";

	// Java SDK objects
	private Service service;
	private Receivers receivers;
	private Args args;

	// streaming objects
	private Socket streamSocket = null;
	private OutputStream ostream;
	private Writer writerOut = null;

	// connection parameters
	private static final String USER_PARAM = "user";
	private static final String PASS_PARAM = "pass";
	private static final String HOST_PARAM = "host";
	private static final String PORT_PARAM = "port";
	private static final String DELIVERY_PARAM = "delivery";

	// event meta field parameters
	private static final String META_INDEX_PARAM = "meta_index";
	private static final String META_SOURCE_PARAM = "meta_source";
	private static final String META_SOURCETYPE_PARAM = "meta_sourcetype";
	private static final String META_HOSTREGEX_PARAM = "meta_hostregex";
	private static final String META_HOST_PARAM = "meta_host";

	// connection
	private String user = "";
	private String pass = "";
	private String host = "";
	private int port = 8089;
	private String delivery = "stream"; // stream or simple

	// event meta data
	private String metaSource = "rest";
	private String metaSourcetype = "jmx";
	private String metaIndex = "jmx";
	private String metaHostRegex = "host=(([a-zA-Z0-9\\._-]+))";
	private String metaHost = "";

	// TODO uncomment when SDK librarys are present
	// private Service service;

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
		if (portparam != null && portparam.length() > 0) {
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

		String metaHostparam = parameters.get(META_HOST_PARAM);
		if (metaHostparam != null && metaHostparam.length() > 0)
			metaHost = metaHostparam;

		this.service = new Service(host, port);
		this.service.login(user, pass);
		this.receivers = new Receivers(this.service);

		RestEventData red = new RestEventData(metaSource, metaSourcetype,
				metaIndex, metaHost, metaHostRegex);
		this.args = createArgs(red);

		if (delivery.equals(STREAM)) {
			try {
				this.streamSocket = this.receivers.attach(args);
				ostream = streamSocket.getOutputStream();
				writerOut = new OutputStreamWriter(ostream, "UTF8");
			} catch (Exception e) {

			}
		}

	}

	/**
	 * Create a SDK Args object from a RestEventData object
	 * 
	 * @param red
	 * @return
	 */
	private Args createArgs(RestEventData red) {

		Args urlArgs = new Args();

		if (red != null) {
			if (red.getIndex().length() > 0)
				urlArgs.add(RestEventData.RECEIVERS_SIMPLE_ARG_INDEX, red
						.getIndex());
			if (red.getSource().length() > 0)
				urlArgs.add(RestEventData.RECEIVERS_SIMPLE_ARG_SOURCE, red
						.getSource());
			if (red.getSourcetype().length() > 0)
				urlArgs.add(RestEventData.RECEIVERS_SIMPLE_ARG_SOURCETYPE, red
						.getSourcetype());
			if (red.getHost().length() > 0)
				urlArgs.add(RestEventData.RECEIVERS_SIMPLE_ARG_HOST, red
						.getHost());
			if (red.getHostRegex().length() > 0)
				urlArgs.add(RestEventData.RECEIVERS_SIMPLE_ARG_HOSTREGEX, red
						.getHostRegex());

		}
		return urlArgs;
	}

	@Override
	/**
	 * Write payload to REST endpoint
	 * 
	 */
	public void transport(String payload) {

		if (delivery.equals(STREAM)) {

			streamEvent(payload);
		} else if (delivery.equals(SIMPLE)) {

			sendEvent(payload);
		} else {
			// no other delivery types supported
		}

	}

	/**
	 * close the stream
	 */
	public void closeStream() {
		try {
			if (writerOut != null) {
				writerOut.flush();
				writerOut.close();
				if (streamSocket != null)
					streamSocket.close();
			}
		} catch (Exception e) {
		}
	}

	/**
	 * send a single event , not so good for performance, streaming is better
	 * 
	 * @param message
	 */
	public void sendEvent(String message) {

		if (streamSocket == null)
			this.receivers.submit(message, args);
	}

	/**
	 * send an event via stream
	 * 
	 * @param message
	 */
	public void streamEvent(String message) {

		try {
			if (writerOut != null) {
				writerOut.write(message + "\n");
				writerOut.flush();
			}
		} catch (IOException e) {
		}

	}

}
