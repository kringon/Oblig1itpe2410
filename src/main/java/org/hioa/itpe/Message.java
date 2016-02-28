package org.hioa.itpe;

import java.util.ArrayList;
import java.util.List;

public class Message {
	private ArrayList<Integer> idList;
	//private String status;
	private int status;
	private int greenInterval;
	private int yellowInterval;
	private int redInterval;


	public Message(ArrayList<Integer> idList){
		
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


	public void setIdList(ArrayList<Integer> idList) {
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
