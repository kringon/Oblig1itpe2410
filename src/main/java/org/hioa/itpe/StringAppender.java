package org.hioa.itpe;

import java.util.ArrayDeque;
import java.util.ArrayList;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Custom subclass of AppenderSkeleton. Used by LogGUI to access logger information.
 *
 */
public class StringAppender extends AppenderSkeleton {

    private ArrayDeque<String> log;
    private ArrayList<LoggingEvent> events;
    
    // public static final Level CYCLE = Level.forName("CYCLE", 100);

    private int size;

    public StringAppender(String name) {
        this.log = new ArrayDeque<String>();
        int size = 1000;
        super.setName(name);
        events = new ArrayList<>();
    }
    
    public void setSize(int size) {
    	this.size = size;
    	trimToSize();
    }

    public ArrayDeque getLog() {
        return log;
    }
    
    public ArrayList getEvents() {
        return events;
    }
    
    public void setEventList(ArrayList listToSet) {
    	listToSet = events;
    }
    
    /**
     * Appends an event to ArrayList events.
     */
    @Override
	public void append(LoggingEvent event) {
    	/*
        // Generate message
        StringBuilder sb = new StringBuilder();
        String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(event.getTimeStamp());
        sb.append(timestamp).append(": ");
        sb.append(event.getLevel().toString()).append(": ");
        sb.append(event.getLoggerName()).append(": ");
        sb.append(event.getRenderedMessage().toString());
        // add it to queue
        if(size != 0 && log.size() == size) {
            trimToSize();
        }
        log.add(sb.toString());
        */
        events.add(event);
        
    }

    @Override
    public void close() {
        //log = Collections.unmodifiableCollection(log);
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
    
    private void trimToSize() {
    	while (log.size() >= size) {
    		((ArrayDeque<String>) log).removeFirst();
    	}
    }
}