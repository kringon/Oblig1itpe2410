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
import javafx.stage.Stage;

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
	private String statusMessage;

	private static Logger logger = LoggerFactory.getLogger(Client.class);

	private int status;
	private boolean cycle;
	private int greenInterval;
	private int yellowInterval;
	private int redInterval;

	private ImageView displayedImage;

	private LightCycleTask cycleTask;
	private LightFlashingTask flashingTask;

	public Client(String ip, int port, ImageView dispImage, ClientGUI clientGUI) {
		this.ip = ip;
		this.port = port;
		this.displayedImage = dispImage;
		this.statusMessage = "Standby";

		this.status = 0;
		this.cycle = false;

		this.greenInterval = 0;
		this.yellowInterval = 0;
		this.redInterval = 0;
		
		this.clientGUI = clientGUI;
		id = -1; // updated in updateFromServerJSON(String fromServer) method.
		mapper = new ObjectMapper();
	}

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

				// Keep reading server output
				while ((fromServer = in.readLine()) != null) {
					logger.info("FromServer: " + fromServer);

					int tempStatus = this.status;

					updateFromServerJSON(fromServer);

					// Status has changed depending on input from server
					if (tempStatus != this.status) {
						Message msg = new Message();
						msg.setMessage("Status was updated");
						msg.setMessageType(Message.SEND_STATUS);
						msg.setStatus(this.status);
						msg.setClientId(this.getId());
						String sentMessage = mapper.writeValueAsString(msg);
						out.println(sentMessage);
						logger.info("Sending to server: " +sentMessage);
					}

				}
			} catch (UnknownHostException e) {
				logger.info("Don't know about host " + ip);
				Platform.runLater(new Runnable() {
			        public void run() {
			        	clientGUI.getStage().close(); // close "parent" clientGUI
			        }
			    });
				this.cancel(); // cancel this thread
			} catch (IOException e) {
				logger.info("Couldn't get I/O for the connection to " + ip);
				
				Platform.runLater(new Runnable() {
			        public void run() {
			        	clientGUI.getStage().close(); // close "parent" clientGUI
			        }
			    });
				this.cancel(); // cancel this thread
			}

		}

		// SocketComTask socketTask = new SocketComTask();

		return null;
	}
	
	public void closeSocket() {
		try { 
			
			Message msg = new Message();
			msg.setMessageType(Message.DISCONNECT);
			msg.setClientId(this.getId());
			out.println(mapper.writeValueAsString(msg));
			socket.shutdownOutput();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void updateFromServerJSON(String fromServer) {

		ObjectMapper mapper = new ObjectMapper();
		Message message;
		try {
			message = mapper.readValue(fromServer, Message.class);

			if (message.getMessageType() == Message.ACCEPT_ID_REQUEST) {

				this.id = message.getClientId(); // Client now has id.
				// Update title of Client GUI with id:
				Platform.runLater(new Runnable() {
			        public void run() {
			        	clientGUI.getStage().setTitle("Client: " + id);
			        }
			    });
				
				
			} else {

				boolean inList = false;
				for (Integer id : message.getIdList()) {
					if (id.intValue() == this.id) {
						inList = true;
					}
				}
				if (inList) {
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

					} else {
						this.cycle = false;
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
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	// Send status message:
	public void sendStatusToServer(String statusMessage) {
		ObjectMapper mapper = new ObjectMapper();

		Message message = new Message();
		message.setMessageType(Message.SEND_STATUS);
		message.setIp(ip);
		message.setPort(port);
		message.setClientId(id);
		message.setStatus(status);
		message.setStatusMessage(statusMessage);
		
		try {
			out.println(mapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	private void updateStatusMessage(int remainingCycleTime) {
		String message = Protocol.statusToString(this.status);
		if (cycle) {
			message += " (" + remainingCycleTime + "s)";
		}
		statusMessage = message;
		sendStatusToServer(message);
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

	private class LightFlashingTask extends Thread {

		@Override
		public void run() {

			boolean yellowOn = false; // used by status: Flashing
			updateStatusMessage(0);
			while (!Thread.currentThread().isInterrupted()) {
				
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