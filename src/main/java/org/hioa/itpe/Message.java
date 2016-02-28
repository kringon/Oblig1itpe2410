package org.hioa.itpe;

import java.util.List;

public class Message {
	private List<Integer> idList;
	//private String status;
	private int status;
	private int greenInterval;
	private int yellowInterval;
	private int redInterval;
	
	// Default constructor needed for JSON
	public Message() {
		
	}

	public Message(List<Integer> idList, int status, int greenInterval, int yellowInterval, int redInterval){
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
	
	public String toString(){
		return this.status + " " + this.greenInterval + " " + this.yellowInterval + " " + this.redInterval;
		
	}
}
