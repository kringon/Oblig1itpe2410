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

		while (!Thread.currentThread().isInterrupted()) {

			try {

				out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String inputLine = "";

				while ((inputLine = in.readLine()) != null) {
					logger.info("Getting info from client: " + inputLine);
					ObjectMapper mapper = new ObjectMapper();
					Message msg = mapper.readValue(inputLine, Message.class);

					Message output = Protocol.processClientOutput(msg);
					if (output != null) {
						out.println(mapper.writeValueAsString(output));
						if (output.getMessageType() == Message.ACCEPT_DISCONNECT) {
							closeThread();
						} else if (output.getMessageType() == Message.ACCEPT_ID_REQUEST) {
							connectedClientId = output.getClientId();
						}
					}

					if (socket.getInputStream().read() == -1) {
						logger.warn("Connection closed abbruptly");
						closeThread();
						logger.warn("Socket was terminated successfully");

					}

				}

			} catch (IOException e1) {
				logger.error("Could not connect to socket: ", e1.getLocalizedMessage());
				logger.error(e1.getMessage());
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

	private void closeThread() throws IOException {
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

			socket.close();
			this.interrupt();
		}
	}

}