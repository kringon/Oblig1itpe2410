package org.hioa.itpe;

import javafx.event.EventHandler;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
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

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		launch(App.class, args);
	}

	public static int clientCounter = 0;

	@Override
	public void start(Stage stage) {

		// Use a border pane as the root for scene
		BorderPane border = new BorderPane();

		HBox hbox = addHBox();
		border.setTop(hbox);
		border.setLeft(addVBox());

		// Add a stack to the HBox in the top region
		addStackPane(hbox);

		// Choose either a TilePane or FlowPane for right region and comment out
		// the
		// one you aren't using
		// border.setRight(addFlowPane());
		border.setRight(addTilePane());

		// To see only the grid in the center, comment out the following
		// statement
		// If both setCenter() calls are executed, the anchor pane from the
		// second
		// call replaces the grid from the first call

		GridPane centerGrid = new GridPane();
		centerGrid.setMinSize(768, 1024);
		border.setCenter(centerGrid);

		Scene scene = new Scene(border);
		stage.setScene(scene);
		stage.setTitle("Layout Sample");
		stage.show();
	}

	/*
	 * Creates an HBox with two buttons for the top region
	 */

	private HBox addHBox() {

		HBox hbox = new HBox();
		hbox.setPadding(new Insets(15, 12, 15, 12));
		hbox.setSpacing(10); // Gap between nodes
		hbox.setStyle("-fx-background-color: #336699;");

		Button btnCreate = new Button("Create Client");
		btnCreate.setPrefSize(100, 20);
		
		btnCreate.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				System.out.println("Creating new client: " + clientCounter++);
				new Client();
			}
			
		});
		
		Button btnStart = new Button("Start Server");
		btnStart.setPrefSize(100, 20);

		hbox.getChildren().addAll(btnCreate, btnStart);

		return hbox;
	}

	/*
	 * Creates a VBox with a list of links for the left region
	 */
	private VBox addVBox() {

		VBox vbox = new VBox();
		vbox.setPadding(new Insets(10)); // Set all sides to 10
		vbox.setSpacing(8); // Gap between nodes

		Text title = new Text("Meny");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		vbox.getChildren().add(title);

		Hyperlink options[] = new Hyperlink[] { new Hyperlink("Set red"), new Hyperlink("Set yellow"),
				new Hyperlink("Set green"), new Hyperlink("Set cycle") };

		for (int i = 0; i < 4; i++) {
			// Add offset to left side to indent from title
			VBox.setMargin(options[i], new Insets(0, 0, 0, 8));
			vbox.getChildren().add(options[i]);
		}

		return vbox;
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
}