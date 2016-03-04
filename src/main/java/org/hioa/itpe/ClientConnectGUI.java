package org.hioa.itpe;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ClientConnectGUI {

	private Stage stage;
	private App parentGUI;

	private static Logger logger = Logger.getLogger(ClientConnectGUI.class);

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

		try {
			URL url = new File("src/main/resources/AppStyle.css").toURI().toURL();
			scene.getStylesheets().add(url.toExternalForm());
		} catch (MalformedURLException e) {
			logger.error("Malformed URL: " + e.getMessage());
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
					new ClientGUI(hostIp, hostPort);
					if (logCheckBox.isSelected()) {
						if (parentGUI.getLogGui() != null) {
							if (parentGUI.getLogGui().getStage() != null) {
								parentGUI.getLogGui().getStage().show();
							}
						} else {
							LogGUI logGui = new LogGUI();
							parentGUI.setLogGui(logGui);
						}

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

		InetAddressValidator ipValidator = new InetAddressValidator();
		UrlValidator urlValidator = new UrlValidator();

		// String validIpAddressRegex =
		// "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
		// String validHostnameRegex =
		// "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
		boolean valid = false;
		if (ipValidator.isValid(ip)) { // valid
			ipInvalidLabel.setText("");
			valid = true;
		} else if (urlValidator.isValid(ip)) { // valid
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
