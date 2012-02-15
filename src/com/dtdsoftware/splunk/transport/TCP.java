package com.dtdsoftware.splunk.transport;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Map;

/**
 * Transport that sends events over a TCP socket
 * 
 * 
 * @author Damien Dallimore damien@dtdsoftware.com
 * 
 */
public class TCP implements Transport {

	static final String HOST_PARAM = "host";
	static final String PORT_PARAM = "port";

	String host = "";// default
	int port;// default

	// streaming objects
	private Socket streamSocket = null;
	private OutputStream ostream;
	private Writer writerOut = null;

	@Override
	public void setParameters(Map<String, String> parameters) {

		String hostParam = parameters.get(HOST_PARAM);
		if (hostParam != null && hostParam.length() > 0)
			host = hostParam;

		String portParam = parameters.get(PORT_PARAM);
		if (portParam != null && portParam.length() > 0) {
			try {
				port = Integer.parseInt(portParam);
			} catch (Exception e) {

			}
		}

	}

	@Override
	/**
	 * Write payload to TCP stream
	 * 
	 */
	public void transport(String payload) {
		try {
			if (writerOut != null) {
				writerOut.write(payload + "\n");
				writerOut.flush();
			}
		} catch (Exception e) {

		}
	}

	@Override
	public void open() {

		try {
			streamSocket = new Socket(host, port);
			if (streamSocket.isConnected()) {
				ostream = streamSocket.getOutputStream();
				writerOut = new OutputStreamWriter(ostream, "UTF8");
			}
		} catch (Exception e) {

		}
	}

	@Override
	public void close() {
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

}
