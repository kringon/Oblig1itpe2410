package org.hioa.itpe;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;

public class Server extends Task {
	public static int portNumber = 8080;
	public static String hostName = "127.0.0.1";
	public static ServerThread serverThread;
	private static Logger logger = LoggerFactory.getLogger(Server.class);

	@Override
	protected Object call() throws Exception {

		boolean listening = true;

		try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
			while (listening) {
				serverThread = new ServerThread(serverSocket.accept());
				serverThread.start();
				
			}
		} catch (IOException e) {
			logger.error("Could not listen on port " + portNumber);
		}
		return null;
	}
	
	public void setProtocol(Protocol protocol) {
		serverThread.setProtocol(protocol);
	}

}
