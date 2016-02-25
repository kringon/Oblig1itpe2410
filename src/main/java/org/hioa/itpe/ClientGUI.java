package org.hioa.itpe;

import java.net.Socket;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ClientGUI {
	private Stage stage;
	private ImageView displayedImage;
	private Socket socket = null;

	public ClientGUI() {

		stage = new Stage();

		stage.setTitle("Client: " + App.clientCounter);
		StackPane root = new StackPane();

		displayedImage = new ImageView(new Image(App.class.getResourceAsStream("graphics/" + "none" + ".png")));
		displayedImage.setPreserveRatio(true);
		displayedImage.setFitWidth(80);
		root.getChildren().add(displayedImage);

		new Thread(new Client(displayedImage)).start();;

		stage.setScene(new Scene(root, 300, 250));
		stage.show();
	}

}
