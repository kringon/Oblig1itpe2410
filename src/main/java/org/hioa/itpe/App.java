package org.hioa.itpe;

import javafx.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.scene.control.skin.TableViewSkinBase;

import javafx.application.Application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.layout.BorderPane;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * 
 */

public class App extends Application {

	// Client table:
	private static TableView<MockClient> clientTable;
	private TableColumn<MockClient, Boolean> chkboxColumn;
	private TableColumn<MockClient, String> ipColumn;
	private TableColumn<MockClient, Integer> portColumn;
	private TableColumn<MockClient, Integer> idColumn;
	// private TableColumn<MockClient, String> intersectColumn;
	private TableColumn<MockClient, String> statusColumn;
	
	private ScrollPane clientTableScroll;

	// Spinners for cycle interval:
	private Spinner<Integer> greenSpinner;
	private Spinner<Integer> yellowSpinner;
	private Spinner<Integer> redSpinner;

	private static Logger logger = LoggerFactory.getLogger(App.class);
	private Server server;
	public static int clientCounter = 0;
	public static int serverCounter = 0;
	public static ObservableList<MockClient> mockClientList;
	// public static int lastAction = Protocol.NONE;

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		launch(App.class, args);
	}

	@Override
	public void start(Stage stage) {

		// Use a border pane as the root for scene
		BorderPane mainPane = new BorderPane();
		
		BorderPane contentPane = new BorderPane(); // if changed to gridpane, remember to remove padding in existing addGridPane()
		contentPane.setId("contentPane");

		HBox hbox = addHBox();
		mainPane.setTop(hbox);
		
		mockClientList = FXCollections.observableArrayList();
		// Create clientPane and place in border pane:
		contentPane.setLeft(addClientPane());
		initColumnsSize();

		// Add a stack to the HBox in the top region
		addStackPane(hbox);

		contentPane.setRight(addGridPane());
		mainPane.setCenter(contentPane);
		
		Scene scene = new Scene(mainPane);
		stage.setScene(scene);
		scene.getStylesheets().add("/CSS/AppStyle.css");
		stage.setTitle("Traffic Light Control Center");
		stage.show();

	}

	/*
	 * Creates an HBox with two buttons for the top region
	 */
	private HBox addHBox() {

		HBox hbox = new HBox();
		hbox.setPadding(new Insets(15, 12, 15, 12));
		hbox.setSpacing(10); // Gap between nodes
		hbox.getStyleClass().add("hbox");

		final Button btnCreate = new Button("Create Client");
		btnCreate.setDisable(false);
		btnCreate.setPrefSize(100, 20);
		btnCreate.getStyleClass().add("button-start");
		final App thisApp = this; // Reference to this running instance. Easier getting it her than from inside EventHandler
		btnCreate.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				logger.info("Opening up Client Connect window.");
				ClientConnectGUI clientConnectGui = new ClientConnectGUI(thisApp);
			}

		});

		final Button btnStart = new Button("Start Server");
		btnStart.setPrefSize(100, 20);
		btnStart.getStyleClass().add("button-start");

		btnStart.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				logger.info("Starting server..");

				server = new Server();
				new Thread(server).start();
				btnStart.setDisable(true);
				btnCreate.setDisable(false);

			}
		});

		hbox.getChildren().addAll(btnStart, btnCreate);

		return hbox;
	}

	/*
	 * Uses a stack pane to create a help icon and adds it to the right side of
	 * an HBox
	 * 
	 * @param hb HBox to add the stack to
	 */
	private void addStackPane(HBox hb) {

		StackPane stack = new StackPane();
		Rectangle helpIcon = new Rectangle(30.0, 25.0);
		helpIcon.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
				new Stop[] { new Stop(0, Color.web("#4977A3")), new Stop(0.5, Color.web("#B0C6DA")),
						new Stop(1, Color.web("#9CB6CF")), }));
		helpIcon.setStroke(Color.web("#D0E6FA"));
		helpIcon.setArcHeight(3.5);
		helpIcon.setArcWidth(3.5);

		Text helpText = new Text("?");
		helpText.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
		helpText.setFill(Color.WHITE);
		helpText.setStroke(Color.web("#7080A0"));

		stack.getChildren().addAll(helpIcon, helpText);
		stack.setAlignment(Pos.CENTER_RIGHT);
		// Add offset to right for question mark to compensate for RIGHT
		// alignment of all nodes
		StackPane.setMargin(helpText, new Insets(0, 10, 0, 0));

		hb.getChildren().add(stack);
		HBox.setHgrow(stack, Priority.ALWAYS);

	}

	/*
	 * Creates a horizontal (default) tile pane with four icons in one row
	 */
	private TilePane addTilePane() {

		TilePane tile = new TilePane();
		tile.setPadding(new Insets(5, 0, 5, 0));
		tile.setVgap(4);
		tile.setHgap(4);
		tile.setPrefColumns(1);
		tile.setMaxWidth(25);
		tile.setMaxHeight(1024);
		tile.setStyle("-fx-background-color: DAE6F3;");
		tile.getChildren().add(new Text("Clients"));

		String[] imageNames = { "red", "yellow", "green", "none" };

		for (String name : imageNames) {
			ImageView image = new ImageView(new Image(App.class.getResourceAsStream("graphics/" + name + ".png")));
			image.setPreserveRatio(true);
			image.setFitWidth(80);
			tile.getChildren().add(image);
		}

		return tile;
	}

	private GridPane addClientPane() {
		GridPane clientPane = new GridPane();

		Label clientsLabel = new Label("Clients");
		clientsLabel.getStyleClass().add("label-control");
		Label clientsDescription = new Label("Select clients to control");
		// clientsDescription.getStyleClass().add("");

		initClientTable();

		clientPane.add(clientsLabel, 0, 0);
		clientPane.add(clientsDescription, 0, 1);
		clientPane.add(clientTableScroll, 0, 2);

		return clientPane;
	}

	private void initClientTable() {
		
		
		clientTable = new TableView<>();
		clientTable.setEditable(false);
		clientTable.setPrefSize(400, 400); // width, height

		// Initialize columns with titles
		chkboxColumn = new TableColumn<MockClient, Boolean>();
		// Header CheckBox
		final CheckBox cb = new CheckBox();
		cb.setUserData(this.chkboxColumn);
		// cb.setOnAction(handleSelectAllCheckbox());
		cb.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				if (cb.isSelected()) {
					for (MockClient client : mockClientList) {
						client.setSelected(true);
					}
				} else {
					for (MockClient client : mockClientList) {
						client.setSelected(false);
					}
				}
			}

		});
		this.chkboxColumn.setGraphic(cb);

		ipColumn = new TableColumn<MockClient, String>("IP-address");
		portColumn = new TableColumn<MockClient, Integer>("Port");
		idColumn = new TableColumn<MockClient, Integer>("ID");
		// intersectColumn = new TableColumn<MockClient,
		// String>("Intersection");
		statusColumn = new TableColumn<MockClient, String>("Status");

		// Add Columns to the table
		clientTable.getColumns().addAll(chkboxColumn, ipColumn, portColumn, idColumn, statusColumn);

		chkboxColumn.setCellFactory(CheckBoxTableCell.forTableColumn(chkboxColumn));
		chkboxColumn.setCellValueFactory(new PropertyValueFactory<MockClient, Boolean>("selected"));
		chkboxColumn.setEditable(true);
		ipColumn.setCellValueFactory(new PropertyValueFactory<MockClient, String>("ip"));
		portColumn.setCellValueFactory(new PropertyValueFactory<MockClient, Integer>("port"));
		idColumn.setCellValueFactory(new PropertyValueFactory<MockClient, Integer>("id"));
		statusColumn.setCellValueFactory(new PropertyValueFactory<MockClient, String>("statusMessage"));
		clientTable.setEditable(true);

		// Allow the columns to space out over the full size of the table.
		clientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		// Wrap TableView in ScrollPane to get scrollbar:
		clientTableScroll = new ScrollPane();
		clientTableScroll.setHbarPolicy(ScrollBarPolicy.NEVER); // Never show horizontal scrollbar
		clientTableScroll.setVbarPolicy(ScrollBarPolicy.ALWAYS); // Always show vertical scrollbar
		clientTableScroll.setContent(clientTable);
		
		// Start thread to refresh table every 1 second
		Thread updateTableThread = new Thread() {
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					/*
					while (!activeCycles) { // Example: ServerThread sends a notification to a method in this class everytime it receives a cycle status message. The method in this class checks the time between previous and current notification and if greater than say 2 seconds, this thread is set to wait. If a new notification is recieved this thread is notified again. 
						wait();
					}
					*/
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					}
					clientTable.getProperties().put(TableViewSkinBase.RECREATE, Boolean.TRUE); // refresh
				}
			}
		};
		updateTableThread.start();

	}
	
	private void initColumnsSize() {  
        this.chkboxColumn.setMinWidth(15);  
        this.ipColumn.setMinWidth(50);  
        this.portColumn.setMinWidth(20);  
        this.idColumn.setMinWidth(20);
        this.statusColumn.setMinWidth(100);
	}

	private GridPane addGridPane() {

		GridPane grid = new GridPane();
		grid.setHgap(5);
		grid.setVgap(12);

		// Padding on the left to create space between this and the tablepane which are placed
		// besides each other in a borderpane: (alt: use gridPane)
		grid.setPadding(new Insets(0, 0, 0, 20));

		Label ctrlLabel = new Label("Control Panel");
		ctrlLabel.getStyleClass().add("label-control");
		Label autLabel = new Label("Automatic Cycle");
		autLabel.getStyleClass().add("label-control-type");
		Label staLabel = new Label("Status");
		staLabel.getStyleClass().add("label-interval");
		Label intLabel = new Label("Interval");
		intLabel.getStyleClass().add("label-interval");

		// spinner parameters
		final int MIN = 1;
		final int MAX = 60;
		final int INITIAL_GREEN = 10;
		final int INITIAL_YELLOW = 3;
		final int INITIAL_RED = 10;
		final int STEP = 1;

		Label greenLabel = new Label("Green");
		greenSpinner = new Spinner<Integer>();
		greenSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN, MAX, INITIAL_GREEN, STEP));
		greenSpinner.setEditable(true);

		Label yellowLabel = new Label("Yellow");
		yellowSpinner = new Spinner<Integer>();
		yellowSpinner
				.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN, MAX, INITIAL_YELLOW, STEP));
		yellowSpinner.setEditable(true);

		Label redLabel = new Label("Red");
		redSpinner = new Spinner<Integer>();
		redSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN, MAX, INITIAL_RED, STEP));
		redSpinner.setEditable(true);

		Button startCycleBtn = new Button("Start Cycle");
		startCycleBtn.getStyleClass().add("button-set");
		startCycleBtn.setMaxWidth(Double.MAX_VALUE);
		// Add action when pressing the "Start Cycle" button
		startCycleBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				handleStatusButtonClick(Protocol.CYCLE);
			}

		});

		// Manual options starts here.
		Label manLabel = new Label("Manual");
		manLabel.getStyleClass().add("label-control-type");
		Label manStatusLabel = new Label("Status");
		manStatusLabel.getStyleClass().add("label-interval");

		Label manIntervalLabel = new Label("Interval");
		manIntervalLabel.getStyleClass().add("label-interval");

		Label manGreenLabel = new Label("Green");
		Button greenManBtn = new Button("Set");
		greenManBtn.getStyleClass().add("button-set");
		greenManBtn.setMaxWidth(Double.MAX_VALUE);
		// Add action when pressing the "set green" button
		greenManBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				handleStatusButtonClick(Protocol.GREEN);
			}

		});

		Label manYellowLabel = new Label("Yellow");
		Button yellowManBtn = new Button("Set");
		yellowManBtn.getStyleClass().add("button-set");
		yellowManBtn.setMaxWidth(Double.MAX_VALUE);
		yellowManBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				handleStatusButtonClick(Protocol.YELLOW);
			}
		});

		Label manRedLabel = new Label("Red");
		Button redManBtn = new Button("Set");
		redManBtn.getStyleClass().add("button-set");
		redManBtn.setMaxWidth(Double.MAX_VALUE);
		redManBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				handleStatusButtonClick(Protocol.RED);
			}
		});

		Label manBlinkYellowLabel = new Label("Flashing");
		Button flashManBtn = new Button("Set");
		flashManBtn.getStyleClass().add("button-set");
		flashManBtn.setMaxWidth(Double.MAX_VALUE);
		flashManBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				handleStatusButtonClick(Protocol.FLASHING);
			}
		});

		Label manOffLabel = new Label("Off");

		Button offManBtn = new Button("Set");
		offManBtn.getStyleClass().add("button-set");
		offManBtn.setMaxWidth(Double.MAX_VALUE);
		offManBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				handleStatusButtonClick(Protocol.NONE);
			}
		});

		grid.add(ctrlLabel, 0, 0, 2, 1);
		grid.add(autLabel, 0, 1, 2, 1);

		grid.add(staLabel, 0, 2, 1, 1);
		grid.add(intLabel, 1, 2, 1, 1);

		grid.add(greenLabel, 0, 3, 1, 1);
		grid.add(greenSpinner, 1, 3, 1, 1);

		grid.add(yellowLabel, 0, 4, 1, 1);
		grid.add(yellowSpinner, 1, 4, 1, 1);

		grid.add(redLabel, 0, 5, 1, 1);
		grid.add(redSpinner, 1, 5, 1, 1);

		grid.add(startCycleBtn, 0, 6, 2, 1);

		// manual is starting here
		grid.add(manLabel, 0, 7, 2, 1);

		grid.add(manStatusLabel, 0, 8, 1, 1);

		grid.add(manGreenLabel, 0, 9, 1, 1);
		grid.add(greenManBtn, 1, 9, 1, 1);

		grid.add(manYellowLabel, 0, 10, 1, 1);
		grid.add(yellowManBtn, 1, 10, 1, 1);

		grid.add(manRedLabel, 0, 11, 1, 1);
		grid.add(redManBtn, 1, 11, 1, 1);

		grid.add(manBlinkYellowLabel, 0, 12, 1, 1);
		grid.add(flashManBtn, 1, 12, 1, 1);

		grid.add(manOffLabel, 0, 13, 1, 1);
		grid.add(offManBtn, 1, 13, 1, 1);

		return grid;
	}

	public static List<Integer> getSelectedClientIds() {
		List<Integer> clientIds = new ArrayList<Integer>();
		for (MockClient client : mockClientList) {
			logger.info("MockClient.isSelected: " + client.isSelected() + " (id: " + client.getId() + ")");
			if (client.isSelected()) {
				clientIds.add(client.getId());
			}
		}
		return clientIds;

	}

	public static int addNewMockClient(MockClient client) {
		int id = App.clientCounter;
		client.idProperty().set(id);
		mockClientList.add(client);
		updateMockClientTable();
		return id;
	}

	public static void updateMockClientTable() {
		clientTable.setItems(mockClientList);
		clientTable.refresh();
	}

	public static MockClient getMockClient(int id) {
		for (MockClient client : mockClientList) {
			if (client.getId() == id) {
				return client;
			}
		}
		return null;
	}

	private void handleStatusButtonClick(int status) {
		logger.info("Setting all selected clients " + Protocol.statusToString(status));
		Protocol prot = new Protocol();
		prot.setStatus(status);
		prot.setIdList(getSelectedClientIds());
		if (status == Protocol.CYCLE) {
			prot.setInterval(greenSpinner.getValue(), yellowSpinner.getValue(), redSpinner.getValue());
		}
		server.updateAllThreads(prot);
		// lastAction = status;
	}
}
