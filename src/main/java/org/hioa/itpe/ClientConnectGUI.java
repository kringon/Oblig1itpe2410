package org.hioa.itpe;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ClientConnectGUI {
	private Stage stage;
	private App parentGUI;

	private static Logger logger = LoggerFactory.getLogger(ClientConnectGUI.class);

	private Label ipInvalidLabel;
	private Label portInvalidLabel;

	private TextField ipField;
	private TextField portField;

	public ClientConnectGUI(App parentGUI) {
		this.parentGUI = parentGUI;

		stage = new Stage();
		stage.setTitle("Create Client");
		stage.show();

		Scene scene = new Scene(createGridPane(), 500, 180);
		stage.setScene(scene);
		File file = new File("src/main/resources/AppStyle.css");

		try {
			URL url = file.toURI().toURL();
			scene.getStylesheets().add(url.toExternalForm());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stage.setTitle("Create Client");
		stage.setResizable(false);
		stage.show();

	}

	private GridPane createGridPane() {

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(12);

		// Padding around entire grid to create space
		grid.setPadding(new Insets(10, 10, 10, 10));

		Label titleLabel = new Label("Connect to Server");

		Label ipLabel = new Label("Ip:");
		ipInvalidLabel = new Label();
		ipInvalidLabel.setStyle("-fx-text-fill: red;");
		Label portLabel = new Label("Port:");
		portInvalidLabel = new Label();
		portInvalidLabel.setStyle("-fx-text-fill: red;");

		ipField = new TextField(Server.hostName);
		ipField.setPrefColumnCount(20);
		
		ipField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.equals(oldValue) && !ipInvalidLabel.equals("")) {
				ipInvalidLabel.setText("");
			}
		});
		

		portField = new TextField(Server.portNumber + "");
		
		portField.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.equals(oldValue) && !portInvalidLabel.equals("")) {
				portInvalidLabel.setText("");
			}
		});
		

		final CheckBox logCheckBox = new CheckBox();
		Label checkBoxLabel = new Label("Open log on connect");

		Button connectButton = new Button("Connect");

		connectButton.setPrefWidth(100);

		connectButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				String hostIp = ipField.getText();
				boolean validIp = validateIp(hostIp);
				String hostPortString = portField.getText();
				int hostPort = validatePort(hostPortString);

				if (validIp && hostPort != -1) {
					// If checkbox is selected:
					logger.info("Creating new client: " + App.clientCounter);
					ClientGUI clientGui = new ClientGUI(hostIp, hostPort);
					if (logCheckBox.isSelected()) {
						/*
						 * log not implemented yet if (parentGUI.getLogGUI() ==
						 * null) { parentGui.createLogGUI(); } else {
						 * parentGUI.getLogGUI().getStage().toFront(); }
						 */

					}
					stage.close(); // close this window

				}
			}

		});

		// Grid arrangement:

		grid.add(titleLabel, 0, 0, 3, 1);

		grid.add(ipLabel, 0, 1, 1, 1);
		grid.add(ipField, 1, 1, 2, 1);
		grid.add(ipInvalidLabel, 3, 1);

		grid.add(portLabel, 0, 2, 1, 1);
		grid.add(portField, 1, 2, 2, 1);
		grid.add(portInvalidLabel, 3, 2);

		grid.add(logCheckBox, 1, 3, 1, 1);
		grid.add(checkBoxLabel, 2, 3, 1, 1);

		grid.add(connectButton, 1, 4, 2, 1);

		return grid;
	}

	private boolean validateIp(String ip) {
		String validIpAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
		String validHostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
		boolean valid = false;
		if (ip.matches(validIpAddressRegex)) { // valid
			ipInvalidLabel.setText("");
			valid = true;
		} else if (ip.matches(validHostnameRegex)) { // valid
			ipInvalidLabel.setText("");
			valid = true;
		} else { // invalid
			ipInvalidLabel.setText("* Does not match a valid hostname of IP-address");
			valid = false;
		}

		return valid;

	}

	// Validates updates labels and returns portnumber in int format if valid,
	// otherwise -1
	private int validatePort(String port) {
		int portInt;
		String validPort = "^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$";

		if (port.matches(validPort)) { // valid
			portInvalidLabel.setText("");
			portInt = Integer.parseInt(port);
			return portInt;
		} else { // invalid
			portInvalidLabel.setText("* Does not match a valid port number (Range: 0-65535)");
			return -1;
		}
	}
}
