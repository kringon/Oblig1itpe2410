package org.hioa.itpe;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import javafx.concurrent.Task;

/**
 * Container for all the server threads needed for a mulit-client setup
 *
 */
public class Server extends Task {
	public static int portNumber = 8080;
	public static String hostName = "127.0.0.1";
	public static ServerThread serverThread;
	private static Logger logger = Logger.getLogger(Server.class);
	private static List<ServerThread> serverThreads = new ArrayList<ServerThread>();

	private String ipAddress;

	public Server() {
		try {
			ipAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected Object call() throws Exception {

		boolean listening = true;

		try (ServerSocket serverSocket = new ServerSocket(portNumber);) {
			while (listening) {
				ServerThread thread = new ServerThread(serverSocket.accept());
				thread.start();
				serverThreads.add(thread);
			}
		} catch (IOException e) {
			logger.error("Could not listen on port " + portNumber);
		}
		return null;
	}

	/**
	 * Notifies all threads belonging to the selected client ids to send
	 * a new output to their client.
	 * @param message to send to client
	 * @param clientIds containing ids of selected clients.
	 */
	public void updateThreads(Message message, List<Integer> clientIds) {
		for (int clientId : clientIds) {
			for (ServerThread thread : serverThreads) {
				if (thread.getConnectedClientId() == clientId) {
					thread.output(message);
				}
			}

		}
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public static void removeThread(long l) {
		for (int i = 0; i < serverThreads.size(); i++) {
			if (serverThreads.get(i).getId() == l) {
				serverThreads.get(i).interrupt();
			}
		}
	}

	////////////////////////////////////////////
	// METHODS BELOW NOT IN USE //
	/////////////////////////////////////////
	/*
	// Update ServerThread with new protocol and send a message of the protocol
	// to out.println
	public void updateAllThreads(Protocol protocol) {
		for (ServerThread thread : serverThreads) {
			thread.updateProtocol(protocol);
		}
	}

	// Send a JSON String to ServerThreads out.println
	public void updateAllThreads(String message) {
		for (ServerThread thread : serverThreads) {
			thread.printMessage(message);
		}
	}

	public void updateAllThreads(int status, List<Integer> clientIds) {
		for (ServerThread thread : serverThreads) {
			thread.printMessage(Protocol.produceMessage(status, clientIds));
		}
	}

	public void updateThread(int status, int clientId) { // (intervals) could be
															// 2 seperate
															// methods
		for (ServerThread thread : serverThreads) {
			if (thread.getConnectedClientId() == clientId) {
				thread.printMessage(""); // TODO: produceMessage not compatibe
											// with cycle
			}
		}
	}
	*/
}
