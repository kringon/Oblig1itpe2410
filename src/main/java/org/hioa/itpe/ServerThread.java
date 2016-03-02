package org.hioa.itpe;

import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class ServerThread extends Thread {

	private int connectedClientId;

	private Socket socket = null;
	private static Logger logger = LoggerFactory.getLogger(ServerThread.class);
	// private int lastPrintedAction;

	private int lastProtocolId;
	private Protocol protocol;

	private PrintWriter out;
	
	private volatile boolean running;

	public ServerThread(Socket socket) {

		super("ServerThread");
		this.socket = socket;
		connectedClientId = -1;
		protocol = new Protocol();
		running = true;

		// lastPrintedAction = -1;
	}

	public void run() {
		
		while(!Thread.currentThread().isInterrupted()) {

			try {

				out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String inputLine = "";

				while ((inputLine = in.readLine()) != null) {

					logger.info("Getting info from client: " + inputLine);
					String output = Protocol.processClientOutput(inputLine);
					if (!inputLine.equals("")) {
						ObjectMapper mapper = new ObjectMapper();
						Message msg = mapper.readValue(inputLine, Message.class);
						if (output.contains("Recieved connection, returning ID")) {
							// Store id of client:
							Message msg2 = mapper.readValue(output, Message.class);
							connectedClientId = msg2.getClientId();
							// Send id to client:
							out.println(output);
						} else if (msg.getMessageType() == Message.SEND_STATUS_MSG) {
							MockClient mock = App.getMockClient(msg.getClientId());
							if (mock != null) {
								mock.setStatusMessage(msg.getStatusMessage());
							}
						}
					}
					
					if (socket.getInputStream().read() == -1) {
						logger.info("Removing mockclient from list");
						int index = -1;
						for (int i = 0; i < App.mockClientList.size(); i++) {
							MockClient cli = App.mockClientList.get(i);
							if (cli.getId() == connectedClientId) {
								index = i;
							}
						}
						if (index != -1) {
							App.mockClientList.remove(index);
							App.updateMockClientTable();
						}
						logger.info("Closing socket");
						socket.close();
						this.interrupt();
						//running = false;
					}

				}
				/*
				 * if (lastProtocolId != protocol.getProtocolId()) { String
				 * message = protocol.output();
				 * 
				 * if (!message.isEmpty()){ out.println(message); lastProtocolId
				 * = protocol.getProtocolId(); } }
				 */

				// logger.info("lastPrintedAction: " + lastPrintedAction + "
				// App.lastAction: " + App.lastAction);
				// if (lastPrintedAction != App.lastAction) {
				// String message = Protocol.produceMessage(App.lastAction,
				// App.getSelectedClientIds());
				// logger.info("Sending message to Client: " + message);
				// if (!message.isEmpty()) {
				// out.println(message);
				// lastPrintedAction = App.lastAction;
				// }
				//
				// }
				
			} catch (IOException e1) {
				logger.error("Could not connect to socket: ", e1.getLocalizedMessage());
			}
		}

	}

	public void updateProtocol(Protocol protocol) {
		this.protocol = protocol;
		out.println(protocol.output());
	}

	public void printMessage(String message) {
		out.println(message);
	}

	public int getConnectedClientId() {
		return connectedClientId;
	}

}