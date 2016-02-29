package org.hioa.itpe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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

	private LightCycleTask cycleTask;
	private LightFlashingTask flashingTask;

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
		cycleTask = new LightCycleTask();
		flashingTask = new LightFlashingTask();

		while (!Thread.currentThread().isInterrupted()) {
			try (Socket kkSocket = new Socket(ip.getValue(), port.getValue());
					PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));) {

				String fromServer;

				// Keep reading server output
				while ((fromServer = in.readLine()) != null) {
					logger.info("FromServer: " + fromServer);
					// logger.info("In.readline: " + in.readLine());

					updateFromServerJSON(fromServer);

				}
			} catch (

			UnknownHostException e) {
				System.err.println("Don't know about host " + ip.getValue());
				System.exit(1);
			} catch (IOException e) {
				System.err.println("Couldn't get I/O for the connection to " + ip.getValue());
				System.exit(1);
			}

		}

		// SocketComTask socketTask = new SocketComTask();

		return null;
	}

	public void updateFromServerJSON(String fromServer) {

		ObjectMapper mapper = new ObjectMapper();
		Message message;
		try {
			message = mapper.readValue(fromServer, Message.class);
			boolean inList = false;
			for (Integer id : message.getIdList()) {
				if (id.intValue() == this.getId()) {
					inList = true;
				}
			}
			if (inList) {
				int statusFromServer = message.getStatus();
				if (statusFromServer == Protocol.CYCLE) {
					if (flashingTask.isAlive()) {
						flashingTask.interrupt();
					}
					this.status = Protocol.CYCLE;
					this.cycle = true;
					this.greenInterval = message.getGreenInterval();
					this.yellowInterval = message.getYellowInterval();
					this.redInterval = message.getRedInterval();

					cycleTask = new LightCycleTask();
					cycleTask.start();
				} else if (statusFromServer == Protocol.FLASHING) {
					if (cycleTask.isAlive()) {
						cycleTask.interrupt();
					}
					status = statusFromServer;

					flashingTask = new LightFlashingTask();
					flashingTask.start();

				} else {
					if (cycleTask.isAlive()) {
						cycleTask.interrupt();
					}
					if (flashingTask.isAlive()) {
						flashingTask.interrupt();
					}
					this.status = statusFromServer;
					updateStatusMessage(0);
					updateImage();
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
		String message = statusToString(this.status);
		if (cycle) {
			message += " (" + remainingCycleTime + "s)";
		}
		setStatusMessage(message);
	}

	public String statusToString(int status) {
		switch (status) {
		case Protocol.NONE:
			return "Standby";
		case Protocol.GREEN:
			return "Green";
		case Protocol.YELLOW:
			return "Yellow";
		case Protocol.RED:
			return "Red";
		case Protocol.RED_YELLOW:
			return "Red/Yellow";
		case Protocol.FLASHING:
			return "Flashing yellow";
		case Protocol.CYCLE:
			return "Cycle";
		default:
			return "Standby";
		}
	}

	// Updates the displayed traffic light image (uses field variable)
	public void updateImage() {
		updateImage(status);
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

	class LightCycleTask extends Thread {

		@Override
		public void run() {
			// Counters for the cycle:
			int greenCounter = 0;
			int yellowCounter = 0;
			int redCounter = 0;
			while (!Thread.currentThread().isInterrupted()) {

				// Switch to RED when starting the cycle.(starts at
				// CYCLE)
				if (status == Protocol.CYCLE) {
					status = Protocol.RED;
				} // Switch to YELLOW (if previously red or green)
				else if (status == Protocol.RED || status == Protocol.GREEN) {
					// If previously red switch to red and yellow.
					if (status == Protocol.RED) {
						status = Protocol.RED_YELLOW;
						redCounter = 0; // reset red counter.
					} // if previously green switch to normal yellow
					else if (status == Protocol.GREEN) {
						status = Protocol.YELLOW;
						greenCounter = 0; // reset green counter.
					}
					updateImage(); // update displayed image
					while (yellowCounter < yellowInterval) {
						updateStatusMessage(yellowInterval - yellowCounter);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt(); 
							e.printStackTrace();
							return;
						}
						yellowCounter++; // update counter by 1 (1
											// second has passed)
					}
				} // Switch to GREEN (if previously red and yellow)
				else if (status == Protocol.RED_YELLOW) {
					status = Protocol.GREEN;
					updateImage(); // update displayed image
					yellowCounter = 0; // reset yellow counter.
					while (greenCounter < greenInterval) {
						updateStatusMessage(greenInterval - greenCounter);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt(); 
							e.printStackTrace();
							return;
						}
						greenCounter++; // update counter by 1 (1 second
										// has passed)
					}
				} // Switch to RED (if previously normal yellow)
				else if (status == Protocol.YELLOW) {
					status = Protocol.RED;
					updateImage(); // update displayed image
					yellowCounter = 0; // reset yellow counter.
					while (redCounter < redInterval) {
						updateStatusMessage(redInterval - redCounter);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt(); 
							e.printStackTrace();
							return;
						}
						redCounter++; // update counter by 1 (1 second
										// has passed)
					}
				}

			}

		}
	}

	class LightFlashingTask extends Thread {

		@Override
		public void run() {

			boolean yellowOn = false; // used by status: Flashing
			while (!Thread.currentThread().isInterrupted()) {
				updateStatusMessage(0);
				// Switch to NONE (if previous status = on)
				if (yellowOn) {
					updateImage(Protocol.NONE);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
						Thread.currentThread().interrupt(); 
						return;
					}
					yellowOn = false; // set local variable to indicate
										// yellow off
				} // Switch to YELLOW (if previous status = off)
				else if (!yellowOn) {
					updateImage(Protocol.YELLOW);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt(); 
						e.printStackTrace();
						return;
					}
					yellowOn = true; // set local variable to indicate yellow on
				}
			}

		}

	}
}