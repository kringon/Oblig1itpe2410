package org.hioa.itpe;

import java.io.File;
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

	private void setOnCloseRequest() {
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent ev) {
				System.out.println("closing");
				thread.stop();
				int index = -1;
				for(int i = 0; i< App.mockClientList.size(); i++){
					MockClient cli = App.mockClientList.get(i);
					if(cli.getId() == client.getId()){
						index = i;
					}
				}
				if (index != -1) {
					App.mockClientList.remove(index);
					App.updateMockClientTable();
				}
			}
		});
	}

	public Client getClient() {
		return this.client;
	}
	
	public Stage getStage() {
		return this.stage;
	}

}
