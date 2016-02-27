package org.hioa.itpe;

import java.net.Socket;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ClientGUI {
	private Stage stage;
	private ImageView displayedImage;
	private Socket socket = null;
	private final Client client;
	private Thread thread;

	public ClientGUI() {

		stage = new Stage();

		stage.setTitle("Client: " + App.clientCounter);
		StackPane root = new StackPane();

		displayedImage = new ImageView(new Image(App.class.getResourceAsStream("graphics/" + "none" + ".png")));
		displayedImage.setPreserveRatio(true);
		displayedImage.setFitWidth(80);
		root.getChildren().add(displayedImage);
		client = new Client(Server.hostName, Server.portNumber, displayedImage, App.clientCounter++);
		thread = new Thread(client);
		thread.start();

		stage.setScene(new Scene(root, 300, 250));
		stage.show();

		setOnCloseRequest();

	}

	private void setOnCloseRequest() {
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent ev) {
				System.out.println("closing");
				thread.stop();
				App.clientList.remove(client);
				App.updateClientTable();
			}
		});
	}

	public Client getClient() {
		return this.client;
	}

}
