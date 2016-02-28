package org.hioa.itpe;

import javafx.event.EventHandler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
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
 * Sample application that shows examples of the different layout panes provided
 * by the JavaFX layout API. The resulting UI is for demonstration purposes only
 * and is not interactive.
 */

public class App extends Application {

	private static TableView clientTable;
    private TableColumn chkboxColumn;
    private TableColumn ipColumn;
    private TableColumn portColumn;
    private TableColumn idColumn;
    private TableColumn intersectColumn;
    private TableColumn statusColumn;

	private static Logger logger = LoggerFactory.getLogger(App.class);

    public static int clientCounter = 0;
    public static int serverCounter = 0;
    private boolean serverStarted = false;
    public static List<Client> clientList;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(App.class, args);
    }

    @Override
    public void start(Stage stage) {

        // Use a border pane as the root for scene
        BorderPane border = new BorderPane();

        HBox hbox = addHBox();
        border.setTop(hbox);

        ///////////////////////

        clientList = new ArrayList<Client>();

        border.setLeft(addClientPane());
        updateClientTable();

        // Add a stack to the HBox in the top region
        addStackPane(hbox);

        GridPane centerGrid = new GridPane();
        centerGrid.setMinSize(768, 1024);
        // border.setCenter(centerGrid);
        border.setRight(addGridPane());

        Scene scene = new Scene(border);
        stage.setScene(scene);
        scene.getStylesheets().add("CSS/AppStyle.css");
        stage.setTitle("Traffic light control ");
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

        Button btnCreate = new Button("Create Client");
        btnCreate.setPrefSize(100, 20);
        btnCreate.getStyleClass().add("button-start");
        btnCreate.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
				logger.info("Creating new client: " + clientCounter);
                ClientGUI gui = new ClientGUI();
				clientList.add(gui.getClient());
                updateClientTable();
            }

        });

        final Button btnStart = new Button("Start Server");
        btnStart.setPrefSize(100, 20);
         btnStart.getStyleClass().add("button-start");

        btnStart.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
				logger.info("Starting server..");
                new Thread(new Server()).start();
                btnStart.setDisable(true);

            }
        });

        hbox.getChildren().addAll(btnCreate, btnStart);

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
        Label clientsDescription = new Label("Select clients to control");
         clientsDescription.getStyleClass().add("label-control");

        buildClientTable();

        clientPane.add(clientsLabel, 0, 0);
        clientPane.add(clientsDescription, 0, 1);
        clientPane.add(clientTable, 0, 2);

        return clientPane;
    }

    private void buildClientTable() {
        clientTable = new TableView<>();
        clientTable.setEditable(false);
        clientTable.setPrefSize(600, 400); // width, height

        // Initialize columns with titles
        chkboxColumn = new TableColumn<Client, Boolean>("Select");
        ipColumn = new TableColumn<Client, String>("IP-address");
        portColumn = new TableColumn<Client, Integer>("Port");
        idColumn = new TableColumn<Client, String>("ID");
        intersectColumn = new TableColumn<Client, String>("Intersection");
        statusColumn = new TableColumn<Client, String>("Status");

        // Add Columns to the table
        clientTable.getColumns().addAll(chkboxColumn, ipColumn, portColumn, idColumn, intersectColumn, statusColumn);

        ipColumn.setCellValueFactory(new PropertyValueFactory<Client, String>("ip"));
        portColumn.setCellValueFactory(new PropertyValueFactory<Client, Integer>("port"));
        chkboxColumn.setCellValueFactory(new PropertyValueFactory<Client, Boolean>("selected"));
        idColumn.setCellValueFactory(new PropertyValueFactory<Client, Integer>("id"));
        chkboxColumn.setCellFactory(CheckBoxTableCell.forTableColumn(chkboxColumn));
        chkboxColumn.setEditable(true);
        clientTable.setEditable(true);

        // Allow the columns to space out over the full size of the table.
        clientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    }

    // TODO: Change to append new client to list, and remove any clients that
    // has disconnected(seperate method?)
    public static void updateClientTable() {
        // Creates an observable list from the received clients list.
        ObservableList<Client> obList = FXCollections.observableArrayList(clientList);
        // Places this list in the client table view.
        clientTable.setItems(obList);

    }

    private GridPane addGridPane() {

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);

        // Padding arround entire grid to create space
        grid.setPadding(new Insets(0, 10, 10, 10));

        Label ctrlLabel = new Label("Control Panel");
        ctrlLabel.getStyleClass().add("label-control");
        Label autLabel = new Label("Automatic Cycle");
        autLabel.getStyleClass().add("label-control-type");
        Label staLabel = new Label("Status");
        staLabel.getStyleClass().add("label-interval");
        Label intLabel = new Label("Interval");
        intLabel.getStyleClass().add("label-interval");

        //spinner parameters
        final int MIN = 1;
        final int MAX = 60;
        final int INITIAL = 30;
        final int STEP = 1;

        Label greenLabel = new Label("Green");
        final Spinner<Integer> greenSpinner = new Spinner<Integer>();
        greenSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN, MAX, INITIAL, STEP));
        greenSpinner.setEditable(false);

        Label yellowLabel = new Label("Yellow");
        final Spinner<Integer> yellowSpinner = new Spinner<Integer>();
        yellowSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN, MAX, INITIAL, STEP));
        yellowSpinner.setEditable(false);

        Label redLabel = new Label("Red");
        final Spinner<Integer> redSpinner = new Spinner<Integer>();
        redSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MIN, MAX, INITIAL, STEP));
        redSpinner.setEditable(false);

        Button startCycleBtn = new Button("Start Cycle");
        startCycleBtn.getStyleClass().add("button-set");
        startCycleBtn.setMaxWidth(Double.MAX_VALUE);

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

        Label manYellowLabel = new Label("Yellow");
        Button yellowManBtn = new Button("Set");
        yellowManBtn.getStyleClass().add("button-set");
        yellowManBtn.setMaxWidth(Double.MAX_VALUE);

        Label manRedLabel = new Label("Red");
        Button redManBtn = new Button("Set");
        redManBtn.getStyleClass().add("button-set");
        redManBtn.setMaxWidth(Double.MAX_VALUE);

        Label manBlinkYellowLabel = new Label("Blinking");
        Button blinkManBtn = new Button("Set");
        blinkManBtn.getStyleClass().add("button-set");
        blinkManBtn.setMaxWidth(Double.MAX_VALUE);

        Label manOffLabel = new Label("Off");

        Button offManBtn = new Button("Set");
        offManBtn.getStyleClass().add("button-set");
        offManBtn.setMaxWidth(Double.MAX_VALUE);

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
        grid.add(blinkManBtn, 1, 12, 1, 1);

        grid.add(manOffLabel, 0, 13, 1, 1);
        grid.add(offManBtn, 1, 13, 1, 1);

        return grid;
    }
}
