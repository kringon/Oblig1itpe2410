package org.hioa.itpe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Protocol {

	private static Logger logger = LoggerFactory.getLogger(Protocol.class);

	public static final int NONE = 1;
	public static final int GREEN = 2;
	public static final int YELLOW = 3;
	public static final int RED = 4;
	public static final int RED_YELLOW = 5;
	public static final int FLASHING = 6;
	public static final int CYCLE = 7;

	private static int protocolIdCounter = 0;
	private int protocolId;

	private int status;
	private int greenInterval = 0;
	private int yellowInterval = 0;
	private int redInterval = 0;
	// private int intersection;
	
	private List<Integer> idList;

	public Protocol() {
		status = NONE;
		idList = new ArrayList<Integer>();
		protocolId = protocolIdCounter++;
	}

	// returns a JSON String
	public String output() {
		ObjectMapper mapper = new ObjectMapper();

		Message message = new Message(idList, status, greenInterval, yellowInterval, redInterval);

		// Convert object to JSON string
		try {
			String jsonInString = mapper.writeValueAsString(message);
			return jsonInString;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	public String processInput(String theInput) {

		String theOutput = "graphics/none.png";

		if (theInput.contains("green")) {
			theOutput = "graphics/green.png";
		} else if (theInput.contains("red")) {
			theOutput = "graphics/red.png";
		} else {
			theOutput = "graphics/none.png";
		}
		return theOutput;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setInterval(int green, int yellow, int red) {
		greenInterval = green;
		yellowInterval = yellow;
		redInterval = red;
	}

	public void setIdList(List<Integer> idList) {
		this.idList = idList;
	}

	public static Message processClientOutput(Message message) {

		if (message.getMessageType() == Message.REQUEST_ID) {
			Message msg = new Message();
			int id = App.addNewMockClient(new MockClient(message.getIp(), message.getPort()));

			msg.setMessageType(Message.ACCEPT_ID_REQUEST);
			msg.setClientId(id);
			return msg;

		} else if (message.getMessageType() == Message.SEND_STATUS) {
			MockClient mock = App.getMockClient(message.getClientId());
			if (mock != null) {
				mock.setStatusMessage(message.getStatusMessage());
				// App.updateMockClientTable();
			}
		} else if (message.getMessageType() == Message.PROPOSE_DISCONNECT) {
			Message msg = new Message();
			msg.setMessageType(Message.ACCEPT_DISCONNECT);
			msg.setClientId(message.getClientId());
			return msg;
		}
		return null;
	}

	public static String produceMessage(int status, List<Integer> clientIds) {
		Message message = new Message();
		ObjectMapper mapper = new ObjectMapper();
		message.setIdList(clientIds);
		try { // Funker ikke message.setStatus(status) ganske greit her, eller
				// bruker du også verdier untenfor 1-7?
			switch (status) {
			case Protocol.GREEN:
				message.setStatus(Protocol.GREEN);
				break;
			case Protocol.YELLOW:
				message.setStatus(Protocol.YELLOW);
				break;
			case Protocol.RED:
				message.setStatus(Protocol.RED);
				break;
			case Protocol.RED_YELLOW:
				message.setStatus(Protocol.RED_YELLOW);
				break;
			case Protocol.FLASHING:
				message.setStatus(Protocol.FLASHING);
				break;
			default:
				message.setStatus(Protocol.NONE);
				break;
			}
			return mapper.writeValueAsString(message);

		} catch (JsonProcessingException e) {
			logger.error("Failed to parse to Json: ", e.getLocalizedMessage());
		}

		return "failed to produce message";

	}

	public int getProtocolId() {
		return protocolId;
	}

	public static String statusToString(int status) {
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

	public static String getExternalIp() {
		URL whatismyip;
		String ip;
		try {
			whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			ip = in.readLine(); // ip as String
			return ip;

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";

	}

}
