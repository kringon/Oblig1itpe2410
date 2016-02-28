package org.hioa.itpe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Client extends Task {
	private StringProperty ip;
	private IntegerProperty port;
	private BooleanProperty selected;
	private IntegerProperty id;
	private StringProperty statusMessage;

	private static Logger logger = LoggerFactory.getLogger(Client.class);

	private int status;
	private boolean cycle;
	private int greenInterval;
	private int yellowInterval;
	private int redInterval;

	private ImageView displayedImage;

	public Client(String ip, int port, ImageView dispImage, int id) {
		this.ip = new SimpleStringProperty(ip);
		this.port = new SimpleIntegerProperty(port);
		this.selected = new SimpleBooleanProperty(true);
		this.displayedImage = dispImage;
		this.id = new SimpleIntegerProperty(id);
		this.statusMessage = new SimpleStringProperty("Standby");

		this.status = 0;
		this.cycle = false;

		this.greenInterval = 0;
		this.yellowInterval = 0;
		this.redInterval = 0;

	}

	@Override
	protected Object call() throws Exception {
		try (Socket socket = new Socket(ip.getValue(), port.getValue());
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));) {

			String fromServer;
			ObjectMapper mapper = new ObjectMapper();

			Message message = new Message(InetAddress.getLocalHost().getHostAddress(), port.getValue(),
					"connecting to server, requesting ID");
			out.println(mapper.writeValueAsString(message));

			// Counters for the cycle:
			int greenCounter = 0;
			int yellowCounter = 0;
			int redCounter = 0;
			// Keep reading server output
			while ((fromServer = in.readLine()) != null) {
				logger.info("FromServer: " + fromServer);

				int tempStatus = this.status;
				
				updateFromServerJSON(fromServer);
				
				//Status has changed depending on input from server
				if(tempStatus != this.status){
					Message msg = new Message();
					msg.setMessage("Status was updated");
					msg.setStatus(this.status);
					msg.setClientId(this.getId());
					out.println(mapper.writeValueAsString(msg));
				}
				// Run cycle status if cycle is set to true:

				while (cycle) {
					// Switch to RED when starting the cycle.(starts at CYCLE)
					if (this.status == Protocol.CYCLE) {
						this.status = Protocol.RED;
					} // Switch to YELLOW (if previously red or green)
					else if (this.status == Protocol.RED || this.status == Protocol.GREEN) {
						// If previously red switch to red and yellow.
						if (this.status == Protocol.RED) {
							this.status = Protocol.RED_YELLOW;
							redCounter = 0; // reset red counter.
						} // if previously green switch to normal yellow
						else if (this.status == Protocol.GREEN) {
							this.status = Protocol.YELLOW;
							greenCounter = 0; // reset green counter.
						}
						updateImage(); // update displayed image
						while (yellowCounter < yellowInterval) {
							updateStatusMessage(yellowInterval - yellowCounter); // update
																					// the
																					// status
																					// message.
							Thread.sleep(1000); // 1 second
							yellowCounter++; // update counter by 1 (1
												// second has passed)
						}
					} // Switch to GREEN (if previously red and yellow)
					else if (this.status == Protocol.RED_YELLOW) {
						this.status = Protocol.GREEN;
						updateImage(); // update displayed image
						yellowCounter = 0; // reset yellow counter.
						while (greenCounter < greenInterval) {
							updateStatusMessage(greenInterval - greenCounter);
							Thread.sleep(1000); // 1 second
							greenCounter++; // update counter by 1 (1 second
											// has passed)
						}
					} // Switch to RED (if previously normal yellow)
					else if (this.status == Protocol.YELLOW) {
						this.status = Protocol.RED;
						updateImage(); // update displayed image
						yellowCounter = 0; // reset yellow counter.
						while (redCounter < redInterval) {
							updateStatusMessage(redInterval - redCounter);
							Thread.sleep(1000); // 1 second
							redCounter++; // update counter by 1 (1 second
											// has passed)
						}
					}

				} // end of while (cycle)
					// TODO:
				boolean yellowOn = false; // used by status: Flashing

				while (status == Protocol.FLASHING) {
					updateStatusMessage(0);
					// Switch to NONE (if previous status = on)
					if (yellowOn) {
						updateImage(Protocol.NONE);
						Thread.sleep(this.yellowInterval);
						yellowOn = false; // set local variable to indicate
											// yellow off
					} // Switch to YELLOW (if previous status = off)
					else {
						updateImage(Protocol.YELLOW);
						Thread.sleep(this.yellowInterval);
						yellowOn = true;
					}
				}

			}
		} catch (

		UnknownHostException e) {
			System.err.println("Don't know about host " + ip.getValue());
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + ip.getValue());
			System.exit(1);
		}
		return null;
	}

	public void updateFromServerJSON(String fromServer) {

		ObjectMapper mapper = new ObjectMapper();
		Message message;
		try {
			message = mapper.readValue(fromServer, Message.class);
			if (message.getMessage() != null && message.getMessage().contains("Recieved connection, returning ID")) {

				this.id.set(message.getClientId());
			} else {

				// Do regular status updates
				this.setSelected(false);
				for (Integer id : message.getIdList()) {

					if (this.getId() == id) {
						this.setSelected(true);

					}
				}
				// this.setSelected(true);
				if (this.isSelected()) {
					int statusFromServer = message.getStatus();

					if (statusFromServer == Protocol.CYCLE) {
						this.status = Protocol.CYCLE;
						this.cycle = true;
						this.greenInterval = message.getGreenInterval();
						this.yellowInterval = message.getYellowInterval();
						this.redInterval = message.getRedInterval();
					} else if (statusFromServer == Protocol.FLASHING) {
						this.status = Protocol.FLASHING;
						this.yellowInterval = 1500;
					} else {
						this.status = statusFromServer;
						updateStatusMessage(0);
						updateImage();

					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setSelected(boolean selected) {
		this.selected.set(selected);
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = new SimpleStringProperty(statusMessage);
	}

	private void updateStatusMessage(int remainingCycleTime) {
		String message = Protocol.statusToString(this.status);
		if (cycle) {
			message += " (" + remainingCycleTime + "s)";
		}
		setStatusMessage(message);
	}

	

	// Updates the displayed traffic light image (uses field variable)
	public void updateImage() {
		updateImage(this.status);
	}

	// Updates the displayed traffic light image (uses paramater variable)
	public void updateImage(int status) {
		if (status == Protocol.GREEN) {
			displayedImage.setImage(new Image(App.class.getResourceAsStream("graphics/green.png")));
		} else if (status == Protocol.RED) {
			displayedImage.setImage(new Image(App.class.getResourceAsStream("graphics/red.png")));
		} else if (status == Protocol.RED_YELLOW) {
			displayedImage.setImage(new Image(App.class.getResourceAsStream("graphics/red_yellow.png")));
		} else if (status == Protocol.YELLOW) {
			displayedImage.setImage(new Image(App.class.getResourceAsStream("graphics/yellow.png")));
		} else {
			displayedImage.setImage(new Image(App.class.getResourceAsStream("graphics/none.png")));
		}
	}

	public boolean isSelected() {
		return selected.get();
	}

	public String getIp() {
		return ip.get();
	}

	public int getPort() {
		return port.get();
	}

	public int getId() {
		return id.get();
	}

	public int getStatus() {
		return status;
	}

	public StringProperty ipProperty() {
		return ip;
	}

	public IntegerProperty portProperty() {
		return port;
	}

	public IntegerProperty idProperty() {
		return id;
	}

	public BooleanProperty selectedProperty() {
		return selected;
	}

	public StringProperty statusProperty() {
		return statusMessage;
	}

}
