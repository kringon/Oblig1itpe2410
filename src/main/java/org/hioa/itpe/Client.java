package org.hioa.itpe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

@SuppressWarnings({ "restriction", "rawtypes" })
public class Client extends Task {

	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private ObjectMapper mapper;
	private ClientGUI clientGUI;

	private String ip;
	private int port;
	private boolean selected;
	private int id;
	private static Logger logger = LoggerFactory.getLogger(Client.class);

	private int status;
	private boolean cycle;
	private int greenInterval;
	private int yellowInterval;
	private int redInterval;

	private ImageView displayedImage;

	private LightCycleTask cycleTask;
	private LightFlashingTask flashingTask;

	/**
	 * Constructor to create a client
	 * 
	 * @param ip
	 *            the IP to create a socket to
	 * @param port
	 *            the Port for the socket
	 * @param dispImage
	 *            the default image to be displayed
	 * @param clientGUI
	 *            the ClientGUI it belongs to
	 */
	public Client(String ip, int port, ImageView dispImage, ClientGUI clientGUI) {
		this.ip = ip;
		this.port = port;
		this.displayedImage = dispImage;
		this.status = 0;
		this.cycle = false;

		this.greenInterval = 0;
		this.yellowInterval = 0;
		this.redInterval = 0;

		this.clientGUI = clientGUI;
		// Default value of ID to signify that it has not recieved the ID from
		// the server yet. updated in updateFromServerJSON(String fromServer)
		// method.
		id = -1;
		mapper = new ObjectMapper();
	}

	/**
	 * Invoked upon creation of the client thread. Similar to run(). Attempts a
	 * server connection upon creation, and listens to any incoming server
	 * messages.
	 */
	@Override
	protected Object call() throws Exception {

		cycleTask = new LightCycleTask();
		flashingTask = new LightFlashingTask();

		while (!Thread.currentThread().isInterrupted()) {

			try {
				socket = new Socket(ip, port);
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				String fromServer;
				ObjectMapper mapper = new ObjectMapper();

				Message message = new Message(InetAddress.getLocalHost().getHostAddress(), port);
				message.setMessageType(Message.REQUEST_ID);
				out.println(mapper.writeValueAsString(message));
				logger.info(logId() + ": Sending request id to server.");

				// Keep reading server output
				while ((fromServer = in.readLine()) != null) {
					logger.info(logId() + ": FromServer: " + fromServer);
					
					// Update client depending on the input from server:
					updateFromServerJSON(fromServer);

				}
			} catch (UnknownHostException e) {
				logger.info(logId() + ": Don't know about host " + ip);
				Platform.runLater(() -> clientGUI.getStage().close());
				this.cancel(); // cancel this thread
			} catch (IOException e) {
				logger.info(logId() + ": Couldn't get I/O for the connection to " + ip);
				// Close the platform if the connection fails
				Platform.runLater(() -> {
					clientGUI.getStage().close();
				});
				this.cancel(); // cancel this thread
			}

		}

		return null;
	}

	/**
	 * Sends a propose disconnect message to server, and shuts down any further
	 * output to server.
	 * 
	 * @see java.net.Socket#close()
	 */
	public void closeSocketOutput() {
		try {

			Message msg = new Message();
			msg.setMessageType(Message.PROPOSE_DISCONNECT);
			logger.info(logId() + ": Sending propose disconnect to server.");
			out.println(mapper.writeValueAsString(msg));
			socket.shutdownOutput();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Helper method to read the info from the server.
	 * <p>
	 * Converts the String of text from the server into a Message object and
	 * updates client and output message depending.
	 * 
	 * @param fromServer
	 *            the info recieved from the server
	 */
	private void updateFromServerJSON(String fromServer) {

		Message message;
		try {
			message = mapper.readValue(fromServer, Message.class);

			// Set logic depending on what type of message it is
			if (message.getMessageType() == Message.ACCEPT_ID_REQUEST) {

				this.id = message.getClientId(); // Client now has id.
				// Update title of Client GUI with id:
				Platform.runLater(() -> {
					clientGUI.getStage().setTitle("Client: " + id);
				});
				// Send acknowledgment of received id to server.
				Message outMsg = new Message();
				outMsg.setMessageType(Message.ID_RECEIVED);
				String outJSON = mapper.writeValueAsString(outMsg);
				out.println(outJSON);
				logger.info("Client(id:" + id + "): " + "Sending to server acknowledgment of id: " + outJSON);

			} else if (message.getMessageType() == Message.ACCEPT_DISCONNECT) {
				socket.shutdownInput();
				socket.close();
				logger.info("Client(id:" + id + "): " + "Closing client after server disconnect aknowledgment");
				Thread.currentThread().interrupt();

			} else {
				// Default action if no other messageType is implemented

				/*
				 * if (message.getIdList() != null) { for (Integer id :
				 * message.getIdList()) { if (id.intValue() == this.id) {
				 * //inList = true; } } }
				 * 
				 * if (true) {
				 */
				int statusFromServer = message.getStatus();
				if (statusFromServer == Protocol.CYCLE) {
					if (flashingTask.isAlive()) {
						flashingTask.interrupt();
					}
					if (cycleTask.isAlive()) {
						cycleTask.interrupt();
					}
					this.status = Protocol.CYCLE;
					this.cycle = true;
					this.greenInterval = message.getGreenInterval();
					this.yellowInterval = message.getYellowInterval();
					this.redInterval = message.getRedInterval();

					cycleTask = new LightCycleTask();
					cycleTask.start();
					sendStatusToServer();
				} else if (statusFromServer == Protocol.FLASHING) {
					this.cycle = false;
					if (cycleTask.isAlive()) {
						cycleTask.interrupt();
					}
					if (flashingTask.isAlive()) {
						flashingTask.interrupt();
					}
					status = statusFromServer;

					flashingTask = new LightFlashingTask();
					flashingTask.start();
					sendStatusToServer();

				} else {
					this.cycle = false;
					if (cycleTask.isAlive()) {
						cycleTask.interrupt();
					}
					if (flashingTask.isAlive()) {
						flashingTask.interrupt();
					}
					this.status = statusFromServer;
					sendStatusToServer();
					updateImage();
				}

			}

		} catch (IOException e) {
			logger.error("Client(id:" + id + "): " + "There was an IOException reading info from the server: ",
					e.getMessage());
		}
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	// Send status message:
	public void sendStatusToServer() {
		String statusMessage = Protocol.statusToString(this.status);
		Message message = new Message();
		message.setMessageType(Message.SEND_STATUS);
		message.setStatus(status);
		message.setStatusMessage(statusMessage);

		String jsonMsg = message.toJSON();
		out.println(jsonMsg);
		logger.info(logId() + "Sending aknowledgement of status change to server: " + jsonMsg);
	}

	private void sendCycleStatusToServer(int remainingCycleTime) {

		ObjectMapper mapper = new ObjectMapper();
		Message message = new Message();
		message.setMessageType(Message.SEND_CYCLE_STATUS);
		message.setStatus(status);
		String statusMessage = Protocol.statusToString(this.status) + " (" + remainingCycleTime + "s)";
		message.setStatusMessage(statusMessage);

		try {
			out.println(mapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			logger.error("Client(id:" + id + "): " + "There was a JsonProcessingException: ", e.getMessage());
		}

	}

	// Updates the displayed traffic light image (uses field variable)
	public void updateImage() {
		updateImage(this.status);

	}

	// Updates the displayed traffic light image (uses paramater variable)
	public void updateImage(int status) {

		if (status == Protocol.GREEN) {
			displayedImage.setImage(new Image(new File("src/main/resources/green.png").toURI().toString()));
		} else if (status == Protocol.RED) {
			displayedImage.setImage(new Image(new File("src/main/resources/red.png").toURI().toString()));
		} else if (status == Protocol.RED_YELLOW) {
			displayedImage.setImage(new Image(new File("src/main/resources/red_yellow.png").toURI().toString()));
		} else if (status == Protocol.YELLOW) {
			displayedImage.setImage(new Image(new File("src/main/resources/yellow.png").toURI().toString()));
		} else {
			displayedImage.setImage(new Image(new File("src/main/resources/none.png").toURI().toString()));
		}
	}

	public boolean isSelected() {
		return selected;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public int getId() {
		return id;
	}

	public int getStatus() {
		return status;
	}

	/**
	 * Helper method. Returns a String representation of client id for use by
	 * the logger.
	 * 
	 * @return
	 */
	private String logId() {
		String id;
		if (this.id == -1) {
			id = "Not connected";
		} else {
			id = this.id + "";
		}
		return "Client(id:" + id + ")";
	}

	private class LightCycleTask extends Thread {

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
						sendCycleStatusToServer(yellowInterval - yellowCounter);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							logger.debug("Client(id:" + id + "): " + "Cycle interruped. Exiting cycle thread.");
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
						sendCycleStatusToServer(greenInterval - greenCounter);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							logger.debug("Client(id:" + id + "): " + "Cycle interruped. Exiting cycle thread.");
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
						sendCycleStatusToServer(redInterval - redCounter);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							logger.debug("Client(id:" + id + "): " + "Cycle interruped. Exiting cycle thread.");
							return;
						}
						redCounter++; // update counter by 1 (1 second
										// has passed)
					}
				}

			}

		}
	}

	private class LightFlashingTask extends Thread {

		@Override
		public void run() {

			boolean yellowOn = false; // used by status: Flashing
			while (!Thread.currentThread().isInterrupted()) {

				// Switch to NONE (if previous status = on)
				if (yellowOn) {
					updateImage(Protocol.NONE);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						logger.debug("Client(id:" + id + "): " + "Flashing interruped. Exiting flashing thread.");
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
						logger.debug("Client(id:" + id + "): " + "Flashing interruped. Exiting flashing thread.");
						return;
					}
					yellowOn = true; // set local variable to indicate yellow on
				}
			}

		}

	}
}