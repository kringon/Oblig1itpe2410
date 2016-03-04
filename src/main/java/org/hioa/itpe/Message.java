package org.hioa.itpe;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Main usage is to map our info going across the TCP socket to a json-string. 
 * Also defines unique message types to define type of message received.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Message {
	private int messageId; // Instead of using lastAction. May not be necessary
							// now?

	private int messageType;

	// Messagetypes
	public static final int REQUEST_ID = 1;
	public static final int ACCEPT_ID_REQUEST = 2;
	public static final int ID_RECEIVED = 3;
	public static final int SEND_STATUS = 4;
	public static final int SEND_CYCLE_STATUS = 5;
	public static final int PROPOSE_DISCONNECT = 6;
	public static final int ACCEPT_DISCONNECT = 7;
	public static final int DISCONNECTED = 8;
	public static final int CLOSED = 9;




	private List<Integer> idList;
	// private String status;
	private int status;
	private String statusMessage;

	private int greenInterval;
	private int yellowInterval;
	private int redInterval;

	private String ip;
	private int port;
	private String message;
	private int clientId;

	// Default constructor needed for JSON
	public Message() {

	}

	public Message(String ip, int port, String message) {
		this.setIp(ip);
		this.setPort(port);
		this.setMessage(message);
	}

	public Message(String ip, int port) {
		this.setIp(ip);
		this.setPort(port);

	}

	public Message(List<Integer> idList, int status) {
		this.idList = idList;
		this.status = status;
	}

	public Message(List<Integer> idList, int status, int greenInterval, int yellowInterval, int redInterval) {
		this.idList = idList;
		this.status = status;
		this.greenInterval = greenInterval;
		this.yellowInterval = yellowInterval;
		this.redInterval = redInterval;
	}

	public int getGreenInterval() {
		return greenInterval;
	}

	public List<Integer> getIdList() {
		return idList;
	}

	public int getRedInterval() {
		return redInterval;
	}

	public int getStatus() {
		return status;
	}

	public int getYellowInterval() {
		return yellowInterval;
	}

	public void setGreenInterval(int greenInterval) {
		this.greenInterval = greenInterval;
	}

	public void setIdList(List<Integer> idList) {
		this.idList = idList;
	}

	public void setRedInterval(int redInterval) {
		this.redInterval = redInterval;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setYellowInterval(int yellowInterval) {
		this.yellowInterval = yellowInterval;
	}

	public String toString() {
		return this.status + " " + this.greenInterval + " " + this.yellowInterval + " " + this.redInterval;

	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}
	
	/**
	 * Returns a JSON String of this message.
	 * @param message
	 * @return JSON String of this message
	 */
	public String toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
}
