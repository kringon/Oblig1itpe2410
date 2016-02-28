package org.hioa.itpe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MockClient {
	
	private StringProperty ip;
	private IntegerProperty port;
	private BooleanProperty selected;
	private IntegerProperty id;
	private StringProperty statusMessage;
	
	private static Logger logger = LoggerFactory.getLogger(Client.class);
	
	public MockClient(String ip, int port){
		this.ip = new SimpleStringProperty(ip);
		this.port = new SimpleIntegerProperty(port);
		this.id = new SimpleIntegerProperty();
		this.selected = new SimpleBooleanProperty(true);
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

	public StringProperty statusProperty() {
		return statusMessage;
	}
	
	public void setSelected(boolean selected) {
		this.selected.set(selected);
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = new SimpleStringProperty(statusMessage);
	}

}
