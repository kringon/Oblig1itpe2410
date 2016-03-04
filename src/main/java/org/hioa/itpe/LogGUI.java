package org.hioa.itpe;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.logging.log4j.Level;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * 
 * A simple GUI window showing the content of all loggers in a text area, with some options
 * for filtering the output.
 *
 */
public class LogGUI {

	private Stage stage;
	private static Logger logger = Logger.getLogger(ClientConnectGUI.class);

	private StringAppender stringAppender;
	
	private TextArea logTextArea;

	private ArrayList<LoggingEvent> events;

	private CheckBox serverCb;
	private CheckBox clientCb;
	private CheckBox cycleCb;
	private CheckBox warningsCb;
	private SimpleDateFormat sf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private volatile int indexOfLastLog = 0;
	

	public LogGUI() {
		stringAppender = (StringAppender)Logger.getRootLogger().getAppender("String"); 

		stage = new Stage();
		stage.setTitle("Log");
		stage.show();

		Scene scene = new Scene(createMainPane(), 800, 400);
		stage.setScene(scene);

		try {
			URL url = new File("src/main/resources/AppStyle.css").toURI().toURL();
			scene.getStylesheets().add(url.toExternalForm());
		} catch (MalformedURLException e) {
			logger.error("Malformed URL: " + e.getMessage());
		}
		stage.setTitle("Log");
		stage.setResizable(false);
		stage.show();
		Thread getEventsThread = new Thread() {
			@Override
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					events = stringAppender.getEvents(); // Get events
					try {
						sleep(20); // sleep for 20 ms
					} catch (InterruptedException e) {
						return;
					}
					// If any new events in the list that are not yet written to textArea, append with these:
					if (events != null && events.size() > indexOfLastLog + 1) {
						updateSelected(indexOfLastLog);
										
					}
					
				}
			}
		};
		getEventsThread.start();


	}
	
	// Helper method. Appends any new events to the text area, or if a new checkbox filter is applied, 
	// the text area is rewritten from bottom to top with all events.
	private void updateSelected(int startIndex) {
		StringBuilder sb = new StringBuilder();
		for (int i = startIndex; i < events.size(); i++) {
			if (events.get(i).getLoggerName().equals(ServerThread.class.getName()) && serverCb.isSelected()) {
				appendEvent(sb, events.get(i));
			} else if (events.get(i).getLoggerName().equals(Client.class.getName()) && clientCb.isSelected()) {
				appendEvent(sb, events.get(i));
			} else if (events.get(i).getLoggerName().equals("CycleStatus") && cycleCb.isSelected()) {
				appendEvent(sb, events.get(i));
			} else if ((events.get(i).getLevel().equals(Level.WARN) || events.get(i).getLevel().equals(Level.ERROR))
					&& warningsCb.isSelected()) {
				appendEvent(sb, events.get(i));
			}
		}
		indexOfLastLog = events.size()-1;
		if (startIndex == 0) {
			logTextArea.setText((sb.toString()));	
		} else {
			logTextArea.appendText((sb.toString()));		
		}
	}
	
	// Helper method. Appends an event to a sb, with proper format and fields.
	private void appendEvent(StringBuilder sb, LoggingEvent event) {
		sb.append(sf.format(event.getTimeStamp())).append(": ");
		sb.append(event.getLevel().toString()).append(": ");
		sb.append(event.getLoggerName()).append(": ");
		sb.append(event.getRenderedMessage().toString());
		sb.append("\n");
	}
	
	/**
	 * Initializes GUI content. Creates a GridPane containing all of the main content of this window.
	 * 
	 * @return main GridPane
	 */
	private GridPane createMainPane() {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(12);

		// Padding around entire grid to create space
		grid.setPadding(new Insets(10, 10, 10, 10));

		Label titleLabel = new Label("Log output");

		logTextArea = new TextArea();
		logTextArea.setEditable(false);
		logTextArea.setPrefRowCount(15);
		logTextArea.setPrefColumnCount(150);

		// Wrap logTextArea in ScrollPane to get scrollbar:
		ScrollPane logTextAreaSp = new ScrollPane();
		logTextAreaSp.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		logTextAreaSp.setContent(logTextArea);

		Label filterByLabel = new Label("Filter by:");

		serverCb = new CheckBox();
		serverCb.setText("Server");
		serverCb.setSelected(true);
		serverCb.setOnAction((ActionEvent ae) -> {
			updateSelected(0);				
		});	

		clientCb = new CheckBox();
		clientCb.setText("Client(s)");
		clientCb.setSelected(true);
		clientCb.setOnAction((ActionEvent ae) -> {
			updateSelected(0);				
		});	

		cycleCb = new CheckBox();
		cycleCb.setText("Light cycle updats from clients");
		cycleCb.setSelected(false);
		cycleCb.setOnAction((ActionEvent ae) -> {
			updateSelected(0);			
		});	

		warningsCb = new CheckBox();
		warningsCb.setText("Warnings");
		warningsCb.setSelected(false);
		warningsCb.setOnAction((ActionEvent ae) -> {
			updateSelected(0);
		});	

		// Grid arrangement:

		grid.add(titleLabel, 0, 0, 3, 1);

		grid.add(logTextAreaSp, 0, 1);

		grid.add(filterByLabel, 0, 2);

		grid.add(serverCb, 0, 3);
		grid.add(clientCb, 0, 4);
		grid.add(cycleCb, 0, 5);
		grid.add(warningsCb, 0, 6);

		return grid;
	}
	
	/**
	 * 
	 * @return this stage
	 */
	public Stage getStage() {
		return stage;
	}

}
