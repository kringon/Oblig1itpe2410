package org.hioa.itpe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created when matching input from a new client. Implements logic to ensure
 * that the info-stream follows the protocol and act accordingly.
 */
public class ServerThread extends Thread {

	private int connectedClientId;
	private int serverThreadId;
	private Socket socket = null;
	private static Logger logger = Logger.getLogger(ServerThread.class);
	private static Logger cycleLogger = Logger.getLogger("CycleStatus");
	private Protocol protocol;

	private PrintWriter out;

	public ServerThread(Socket socket) {

		super("ServerThread");
		this.socket = socket;
		connectedClientId = -1;
		protocol = new Protocol();

	}

	public void run() {
		outerLoop: while (!Thread.currentThread().isInterrupted()) {
			try {
				out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				String inputLine = "";
				while ((inputLine = in.readLine()) != null) {
					ObjectMapper mapper = new ObjectMapper();
					Message msg = mapper.readValue(inputLine, Message.class);
					// Log content of input from client:
					if (msg.getMessageType() == Message.SEND_CYCLE_STATUS) {
						cycleLogger.info(logId() + ": From client: " + inputLine);
					} else {
						logger.info(logId() + ": From client: " + inputLine);
					}
					// Process input according to our protocol to create an
					// output:
					Message output = Protocol.processClientOutput(msg);
					if (output != null) {
						if (output.getMessageType() == Message.ACCEPT_DISCONNECT) {
							logger.info(logId() + " Sending accept disconnect request.");
							out.println(mapper.writeValueAsString(output));
							this.interrupt();
							if (Thread.interrupted()) {
								throw new InterruptedException();
							}
							break outerLoop;

						} else if (output.getMessageType() == Message.ACCEPT_ID_REQUEST) {
							logger.info(logId() + " Sending accept id request.");
							out.println(mapper.writeValueAsString(output));
							serverThreadId = connectedClientId = output.getClientId();

						} else {
							// out.println(mapper.writeValueAsString(output));
						}
					}

				}
				if (!socket.isClosed() && inputLine == null) {
					logger.warn(logId() + ": Connection closed abbruptly");
					closeThread();
				}

			} catch (InterruptedException ie) {
				closeThread();
				logger.info(logId() + ": Closing thread.");
				return;
			} catch (IOException e1) {
				logger.error(logId() + ": " + e1.getMessage());
			}
		}

	}

	/**
	 * Updates this thread to a new protocol, and prints a new output from that
	 * protocol
	 * 
	 * @param protocol
	 *            the protocol to set
	 */
	public void output(Message message) {
		logger.info(logId() + " Sending new status to client.");
		out.println(message.toJSON());
	}

	public void printMessage(String message) {
		out.println(message);
	}

	public int getConnectedClientId() {
		return connectedClientId;
	}

	/**
	 * Helper method to close the current thread
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void closeThread() {
		try {
			if (!socket.isClosed()) {
				logger.info(logId() + ": Shutting down socket input.");
				socket.shutdownInput();
				logger.info(logId() + ": Shutting down socket output.");
				socket.shutdownOutput();
				// Closes the communication socket
				logger.info(logId() + ": Closing socket.");
				socket.close();
			}
		} catch (IOException ioe) {
			logger.warn(logId() + ": Attempting to close sockets failed. " + ioe.getMessage());
		}
		// Remove mock client from list in App
		for (int i = 0; i < App.mockClientList.size(); i++) {
			MockClient cli = App.mockClientList.get(i);
			if (cli.getId() == connectedClientId) {
				logger.info(logId() + ": Removing mock client from App.");
				App.mockClientList.remove(i);
				App.updateMockClientTable(); // Update table of clients.
			}
		}
	}

	/**
	 * Helper method. Returns a String represenation of server thread id for use
	 * by the logger.
	 * 
	 * @return
	 */
	private String logId() {
		String id;
		if (serverThreadId == -1) {
			id = "Not connected";
		} else {
			id = serverThreadId + "";
		}
		return "Server(thread id:" + serverThreadId + ")";
	}

}