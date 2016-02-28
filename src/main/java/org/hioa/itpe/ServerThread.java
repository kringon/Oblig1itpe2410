package org.hioa.itpe;

import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ServerThread extends Thread {
	private Socket socket = null;
	private static Logger logger = LoggerFactory.getLogger(ServerThread.class);
	private int lastPrintedAction;
	PrintWriter out;

	public ServerThread(Socket socket) {

		super("ServerThread");
		this.socket = socket;
		lastPrintedAction = -1;
	}

	public void run() {

		while (true) {

			try {

				out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String inputLine = "";

				while ((inputLine = in.readLine()) != null) {
					logger.info("Getting info from client: " + inputLine);
					String output = Protocol.processClientOutput(inputLine);
					if (output.contains("Recieved connection, returning ID")) {
						out.println(output);
					}
					
				}

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

	public void printMessage(String message) {
		out.println(message);
	}

}