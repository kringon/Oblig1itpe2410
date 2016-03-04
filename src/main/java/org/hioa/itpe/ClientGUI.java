package org.hioa.itpe;

import java.io.File;

import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.*;

/**
 * 
 * The GUI of the client itself. Displays an image with the traffic light status.
 *
 */
public class ClientGUI {
	private Stage stage;
	private ImageView displayedImage;
	private final Client client;
	private Thread thread;
	/**
	 * Constructor. Initializes a new Client.
	 * @param hostIp
	 * @param hostPort
	 */
	public ClientGUI(String hostIp, int hostPort) {

		stage = new Stage();

		
		StackPane root = new StackPane();

		displayedImage = new ImageView(new Image(new File("src/main/resources/none.png").toURI().toString()));
		displayedImage.setPreserveRatio(true);
		displayedImage.setFitWidth(80);
		root.getChildren().add(displayedImage);
		client = new Client(hostIp, hostPort, displayedImage, this);
		stage.setTitle("Client: (Waiting for id)");
		thread = new Thread(client);
		thread.start();

		stage.setScene(new Scene(root, 300, 250));
		stage.show();
		setOnCloseRequest();

	}
	
	// Close request for this window. When this window closes, the client gets updated so that it can notify
	// the server and close its connection properly
	private void setOnCloseRequest() {
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent ev) {
				client.closeSocketOutput();
//				thread.interrupt();
			
				
			}
		});
	}
	
	/**
	 * 
	 * @return this client
	 */
	public Client getClient() {
		return this.client;
	}
	
	/**
	 * 
	 * @return this stage.
	 */
	public Stage getStage() {
		return this.stage;
	}

}
