package org.hioa.itpe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Client extends Task {
	private StringProperty ip;
	private IntegerProperty port;
	private BooleanProperty selected;
	private IntegerProperty id; 
	private ImageView displayedImage;

	public Client(String ip, int port, ImageView dispImage, int id) {
		this.ip = new SimpleStringProperty(ip);
		this.port = new SimpleIntegerProperty(port);
		this.selected = new SimpleBooleanProperty(true);
		this.displayedImage = dispImage;
		this.id = new SimpleIntegerProperty(id);
	}

	@Override
	protected Object call() throws Exception {
		try (Socket kkSocket = new Socket(ip.getValue(), port.getValue());
				PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));) {

			String fromServer;

			// Keep reading server output
			while ((fromServer = in.readLine()) != null) {
				System.out.println("Server: " + fromServer);
				displayedImage.setImage(new Image(App.class.getResourceAsStream(fromServer)));
			}
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + ip.getValue());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + ip.getValue());
			System.exit(1);
		}
		return null;
	}

	public void setSelected(boolean selected) {
		this.selected.set(selected);
	}

	public boolean getSelected() {
		return selected.get();
	}

	public String getIp() {
		return ip.get();
	}

	public int getPort() {
		return port.get();
	}
	
	public int getId(){
		return id.get();
	}

	public StringProperty ipProperty() {
		return ip;
	}

	public IntegerProperty portProperty() {
		return port;
	}
	
	public IntegerProperty idProperty(){
		return id;
	}

	public BooleanProperty selectedProperty() {
		return selected;
	}

}