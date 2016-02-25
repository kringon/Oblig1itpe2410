package org.hioa.itpe;

import java.io.*;
import java.net.*;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Client extends Task {

	private int portNumber = 8080;
	private String hostName = "127.0.0.1";
	private ImageView displayedImage;
	
	public Client(ImageView displayedImage){
		this.displayedImage = displayedImage;
	}

	@Override
	protected Object call() throws Exception {
		try (Socket kkSocket = new Socket(hostName, portNumber);
				PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));) {

			String fromServer;

			//Keep reading server output
			while ((fromServer = in.readLine()) != null) {
				System.out.println("Server: " + fromServer);		
				displayedImage.setImage(new Image(App.class.getResourceAsStream(fromServer)));
			}
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + hostName);
			System.exit(1);
		}
		return null;
	}
}