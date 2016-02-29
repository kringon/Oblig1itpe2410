package org.hioa.itpe;

import java.io.IOException;
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

	public static String processClientOutput(String message)
			throws JsonParseException, JsonMappingException, IOException {
		if (message.contains("connecting to server, requesting ID")) {
			ObjectMapper mapper = new ObjectMapper();

			Message msg = mapper.readValue(message, Message.class);

			int id = App.addNewMockClient(new MockClient(msg.getIp(), msg.getPort()));
			msg = new Message();
			msg.setMessage("Recieved connection, returning ID: " + id);
			msg.setClientId(id);

			return mapper.writeValueAsString(msg);

		} else if (message.contains("Status was updated")) {
			logger.info("status was updated");
			ObjectMapper mapper = new ObjectMapper();
			Message msg = mapper.readValue(message, Message.class);
			App.getMockClient(msg.getClientId()).setStatusMessage(Protocol.statusToString(msg.getStatus()));
			App.updateMockClientTable();
		}
		return "";
	}

	public static String produceMessage(int status, List<Integer> clientIds) {
		Message message = new Message();
		ObjectMapper mapper = new ObjectMapper();
		message.setIdList(clientIds);
		try {
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

}
