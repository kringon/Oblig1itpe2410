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
	private Protocol protocol;

	private PrintWriter out;

	public ServerThread(Socket socket) {

		super("ServerThread");
		this.socket = socket;
		connectedClientId = -1;
		protocol = new Protocol();

	}

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {

			try {
				out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				String inputLine = "";

				while ((inputLine = in.readLine()) != null) {
					logger.info("Getting info from client: " + inputLine);
					ObjectMapper mapper = new ObjectMapper();
					Message msg = mapper.readValue(inputLine, Message.class);

					Message output = Protocol.processClientOutput(msg);
					if (output != null) {
						if (output.getMessageType() == Message.ACCEPT_DISCONNECT) {
							out.println(mapper.writeValueAsString(output));
							closeThread();

						} else if (output.getMessageType() == Message.ACCEPT_ID_REQUEST) {
							connectedClientId = output.getClientId();
						}
						out.println(mapper.writeValueAsString(output));
					}

				}
				if (!socket.isClosed() && inputLine == null) {
					logger.warn("Connection closed abbruptly");
					closeThread();
				}

			} catch (IOException e1) {
				logger.error(e1.getMessage());
			}
		}

	}

	/**
	 * Updates this thread to a new protocol, and prints a new output from that protocol
	 * @param protocol the protocol to set
	 */
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

	/**
	 * Helper method to close the current thread
	 * @throws IOException
	 */
	private void closeThread() throws IOException {
		socket.shutdownInput();
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
		socket.shutdownOutput();
		socket.close();
		this.interrupt();

	}

}