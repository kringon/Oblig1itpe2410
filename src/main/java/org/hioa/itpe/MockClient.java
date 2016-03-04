package org.hioa.itpe;

import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Helper class to display the clients in the table at App.java
 * Only holds / provides placeholders for the clients values in the table
 * @author T820082
 *
 */
public class MockClient {
	
	private StringProperty ip;
	private IntegerProperty port;
	private BooleanProperty selected;
	private IntegerProperty id;
	private StringProperty statusMessage;
	
	private static Logger logger = Logger.getLogger(Client.class);
	
	// Default constructor necessary for TableView:
	public MockClient() {

	}
	
	public MockClient(String ip, int port){
		this.ip = new SimpleStringProperty(ip);
		this.port = new SimpleIntegerProperty(port);
		this.id = new SimpleIntegerProperty();
		this.selected = new SimpleBooleanProperty(false);
		this.statusMessage = new SimpleStringProperty("Standby");
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
	
	public void setSelected(boolean selected) {
		this.selected.set(selected);
	}
	
	public void setIp(String ip) {
		this.ip = new SimpleStringProperty(ip);
	}

	public void setPort(int port) {
		this.port = new SimpleIntegerProperty(port);
	}

	public void setId(int id) {
		this.id = new SimpleIntegerProperty(id);
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = new SimpleStringProperty(statusMessage);
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

	public StringProperty statusMessageProperty() {
		return statusMessage;
	}


}
