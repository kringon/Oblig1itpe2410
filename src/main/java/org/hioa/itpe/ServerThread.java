package org.hioa.itpe;

import java.net.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class ServerThread extends Thread {
	private Socket socket = null;
	private static Logger logger = LoggerFactory.getLogger(ServerThread.class);
	private int lastPrintedAction;
	private int lastProtocolId;
	
	private Protocol protocol;

	public ServerThread(Socket socket) {

		super("ServerThread");
		this.socket = socket;
		lastPrintedAction = -1;
		protocol = new Protocol();
	}

	public void run() {

		while (true) {
			try {
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				
				if (lastProtocolId != protocol.getProtocolId()) {
					String message = protocol.output();
					
					if (!message.isEmpty()){
						out.println(message);
						lastProtocolId = protocol.getProtocolId();
					}
				}
					
				
				/*
				if (lastPrintedAction != App.lastAction) {
					String message = Protocol.produceMessage(App.lastAction, App.getSelectedClientIds());

					if (!message.isEmpty()){
						out.println(message);
						lastPrintedAction = App.lastAction;
					}
						
				}
				*/

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}
	
	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

}