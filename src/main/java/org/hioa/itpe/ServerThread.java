package org.hioa.itpe;

import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ServerThread extends Thread {
	private Socket socket = null;
	private static Logger logger = LoggerFactory.getLogger(ServerThread.class);
	private int lastPrintedAction;

	public ServerThread(Socket socket) {

		super("ServerThread");
		this.socket = socket;
		lastPrintedAction = -1;
	}

	public void run() {

		while (true) {
			try {
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				if (lastPrintedAction != App.lastAction) {
					String message = Protocol.produceMessage(App.lastAction, App.getSelectedClientIds());

					if (!message.isEmpty()){
						out.println(message);
						lastPrintedAction = App.lastAction;
					}
						
				}

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

}