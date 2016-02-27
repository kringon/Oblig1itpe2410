package org.hioa.itpe;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
	private Socket socket = null;
	private int counter=0;

	public ServerThread(Socket socket) {

		super("ServerThread");
		this.socket = socket;

	}

	public void run() {

		try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {
			String inputLine, outputLine;
			Protocol kkp = new Protocol();
			outputLine = kkp.processInput("none");
			out.println(outputLine);
			System.out.println("After outputline");
			while (true) {
				sleep(3000);
				if (counter % 2 == 0) {
					inputLine = "green";
					counter++;
				} else {
					inputLine = "red";
					counter++;
				}

				outputLine = kkp.processInput(inputLine);
				out.println(outputLine);

				if (inputLine == "stop") {
					break;
				}

			}
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}