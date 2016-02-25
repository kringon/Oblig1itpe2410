package org.hioa.itpe;

import java.io.IOException;
import java.net.ServerSocket;

import javafx.concurrent.Task;

public class Server extends Task {
	private int portNumber = 8080;

	@Override
	protected Object call() throws Exception {

		boolean listening = true;

		try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
			while (listening) {
				new ServerThread(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			System.err.println("Could not listen on port " + portNumber);
			System.exit(-1);
		}
		return null;
	}

}
