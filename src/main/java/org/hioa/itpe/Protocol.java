package org.hioa.itpe;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

import java.net.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;


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
    //private int intersection;
    
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
        }else if(theInput.contains("red")){
        	  theOutput = "graphics/red.png";
        }else{
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
    
    
    
}
