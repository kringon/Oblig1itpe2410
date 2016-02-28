package org.hioa.itpe;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Protocol {

	private static Logger logger = LoggerFactory.getLogger(Protocol.class);
	public static final int NONE = 0;
	public static final int GREEN = 1;
	public static final int YELLOW = 2;
	public static final int RED = 3;
	public static final int RED_YELLOW = 4;
	public static final int FLASHING = 5;
	public static final int CYCLE = 6;

	private int status;
	private int greenInterval = 5;
	private int yellowInterval = 2;
	private int redInterval = 5;
	// private int intersection;

	private ArrayList<Integer> idList;

	public Protocol() {
		status = NONE;
		idList = new ArrayList<Integer>();
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

	public void setIdList(ArrayList<Integer> idList) {
		this.idList = idList;
	}

	public static String produceMessage(int status, List<Integer> clientIds) {
		Message message = new Message();
		ObjectMapper mapper = new ObjectMapper();
		message.setIdList(clientIds);
		try {
			switch (status) {
			case Protocol.GREEN:
				message.setStatus(Protocol.GREEN);
				return mapper.writeValueAsString(message);

			default:
				message.setStatus(Protocol.NONE);
				return "";
			}

		} catch (JsonProcessingException e) {
			logger.error("Failed to parse to Json: ", e.getLocalizedMessage());
		}

		return "failed to produce message";

	}

}
