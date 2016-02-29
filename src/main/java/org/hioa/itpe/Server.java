package org.hioa.itpe;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;

public class Server extends Task {
	public static int portNumber = 8080;
	public static String hostName = "127.0.0.1";
	public static ServerThread serverThread;
	private static Logger logger = LoggerFactory.getLogger(Server.class);
	private List<ServerThread> serverThreads = new ArrayList<ServerThread>();

	@Override
	protected Object call() throws Exception {

		boolean listening = true;

		try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
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
	
	// TODO: fix Protocol.
	public void setProtocol(Protocol protocol) {
		serverThread.setProtocol(protocol);
	}

	public void updateAllThreads(int status,List<Integer> clientIds){
		for(ServerThread thread: serverThreads){
			thread.printMessage(Protocol.produceMessage(status, clientIds));
		}
	}

}
