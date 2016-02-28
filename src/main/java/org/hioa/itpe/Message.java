package org.hioa.itpe;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Message {
	private List<Integer> idList;
	// private String status;
	private int status;
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
}
