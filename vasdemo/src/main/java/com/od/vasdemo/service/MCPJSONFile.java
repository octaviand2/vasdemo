package com.od.vasdemo.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.od.vasdemo.ApplicationController;
import com.od.vasdemo.ConfigProp;


public class MCPJSONFile {

	private boolean processed=false;
	
	//metrics for each processed file
	private int rowsProcessed=0;
	private int rowsMissingFields=0;
	private int rowsFieldErrors=0;
	private int numberOfcalls=0;
	private int numberOfmessages=0;
	private int messagesBlankContent=0;
	private HashMap <Integer,Integer> numberCallsOrig = new HashMap<Integer,Integer>();
	private HashMap <Integer,Integer> numberCallsDest = new HashMap<Integer,Integer>();
	private HashMap <String,Integer> numberCallsOrigDest = new HashMap<String,Integer>();
	private int numberOKCalls=0;
	private int numberKOCalls=0;
	private HashMap <Integer,Integer> durationCallsDest = new HashMap<Integer,Integer>();
	private HashMap <Integer,Integer> avgDurationCallsDest = new HashMap<Integer,Integer>();
	private HashMap <String,Integer> wordOccurence = new HashMap<String,Integer>();

	public int getRowsProcessed()
	{
		return rowsProcessed;
	}
	public int getnumberOfcalls()
	{
		return numberOfcalls;
	}
	public int getnumberOfmessages()
	{
		return numberOfmessages;
	}
	
	/* contructor - read file and process it */
	public MCPJSONFile(String link, String fileName, ConfigProp appProps){

		Logger logger = LoggerFactory.getLogger(ApplicationController.class);
		logger.debug("Processing the remote file: " + link);
		List<String> rankingwords = appProps.getRankingWords();

		try {
			URL url = new URL( link );

			HttpURLConnection con =
					(HttpURLConnection) new URL(link).openConnection();
			if (con.getResponseCode() != HttpURLConnection.HTTP_OK)
				return;

			BufferedInputStream in = new BufferedInputStream(url.openStream());
			BufferedReader reader = new BufferedReader (new InputStreamReader (in, StandardCharsets.UTF_8));
		    String line;
		    while ((line = reader.readLine()) != null) {
		    	try {
		    		rowsProcessed++;
		    		JSONObject record = new JSONObject(line);
		    		logger.debug("Parsed JSON: "+ record.toString());

		    		
		    		//Calculating metric missing fields
		    		if (!record.has(RecConstants.F_message_type)) { rowsMissingFields++; logger.debug("Missing Message Type"); }
		    		else {
		    			if (record.getString(RecConstants.F_message_type).equals(RecConstants.CALL_TYPE)) {
		    				//analyse call types
				    		if ( !record.has(RecConstants.F_timestamp)|| !record.has(RecConstants.F_origin) || !record.has(RecConstants.F_destination) || !record.has(RecConstants.F_duration) 
								|| !record.has(RecConstants.F_status_code) || !record.has(RecConstants.F_status_description) 
								|| record.getString(RecConstants.F_duration).equals("")) 
								{ rowsMissingFields++; logger.debug("Missing Call Fields"); }
				    		else {
				    			//call with all fields present
				    			if (!record.getString(RecConstants.F_status_code).equals(RecConstants.CALL_STATUS_OK) && 
				    					!record.getString(RecConstants.F_status_code).equals(RecConstants.CALL_STATUS_KO))
				    				{ rowsFieldErrors++; logger.debug("Call Status Code not ok"); }
				    			else {
				    				//check if OK or KO call
				    				if (record.getString(RecConstants.F_status_code).equals(RecConstants.CALL_STATUS_OK))
				    					numberOKCalls++;
				    				else numberKOCalls++;
				    					
				    				int originCountryCode, destinationCountryCode;
				    				PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
				    				try {
				    				    Phonenumber.PhoneNumber origProto = phoneUtil.parse("+"+record.getString(RecConstants.F_origin), "");
				    				    Phonenumber.PhoneNumber destProto = phoneUtil.parse("+"+record.getString(RecConstants.F_destination), "");
				    				    originCountryCode=origProto.getCountryCode();
				    				    destinationCountryCode=destProto.getCountryCode();
				    				    //add number of calls per current origin and destination
					    				if (numberCallsOrigDest.containsKey(originCountryCode+","+destinationCountryCode)) 
					    					numberCallsOrigDest.put(originCountryCode+","+destinationCountryCode, numberCallsOrigDest.get(originCountryCode+","+destinationCountryCode)+1);
					    				else numberCallsOrigDest.put(originCountryCode+","+destinationCountryCode, 1);
					    				
				    				    //add number of calls per current origin 
					    				if (numberCallsOrig.containsKey(originCountryCode)) 
					    					numberCallsOrig.put(originCountryCode, numberCallsOrig.get(originCountryCode)+1);
					    				else numberCallsOrig.put(originCountryCode, 1);

					    				
					    				//add number of calls per current destination, required to calculate average duration at the end
					    				if (numberCallsDest.containsKey(destinationCountryCode)) {
					    					numberCallsDest.put(destinationCountryCode, numberCallsDest.get(destinationCountryCode)+1);
					    					durationCallsDest.put(destinationCountryCode, durationCallsDest.get(destinationCountryCode)+record.getInt(RecConstants.F_duration));
					    				}
					    				else {
					    					numberCallsDest.put(destinationCountryCode, 1);
					    					durationCallsDest.put(destinationCountryCode, record.getInt(RecConstants.F_duration));
					    				}
				    				} catch (NumberParseException e) {
				    					rowsFieldErrors++;
				    					logger.debug("Error parsing origin/destination of the call");
				    					e.printStackTrace();
				    				}
				    			}		
				    		}
				    				
				    	}
				    	else if (record.getString(RecConstants.F_message_type).equals(RecConstants.MSG_TYPE)) {
				    		//analyse message type
				    		if ( !record.has(RecConstants.F_timestamp)|| !record.has(RecConstants.F_origin) || !record.has(RecConstants.F_destination)  
								|| !record.has(RecConstants.F_message_content) || !record.has(RecConstants.F_message_status)) 
								{ rowsMissingFields++; logger.debug("Missing Message Fields"); }
				    		else {
				    			//message with all fields present
				    			if (!record.getString(RecConstants.F_message_status).equals(RecConstants.MSG_STATUS_DELIVERED) && 
				    					!record.getString(RecConstants.F_message_status).equals(RecConstants.MSG_STATUS_SEEN))
				    				{ rowsFieldErrors++; logger.debug("Message status incorrect"); }
				    			else {
				    				//check for blank content
					    			if (record.getString(RecConstants.F_message_content).equals(""))
					    				{ messagesBlankContent++; logger.debug("Message with blank content"); }
					    			
					    			//check occurence of keywords
					    			rankingwords.forEach((temp) -> {
					    				try {
											if (record.getString(RecConstants.F_message_content).contains(temp))
											{
												if (wordOccurence.containsKey(temp)) wordOccurence.put(temp, wordOccurence.get(temp)+1);
												else wordOccurence.put(temp, 1);
											}
										} catch (JSONException e) {
										}
					    					
					    			});

				    			}
				    		}
		    			}
				    	else
				    		//invalid message_type
				    		{ rowsFieldErrors++; logger.debug("Invalid message type"); }
		    		}
		    		
		    		/*
		    		if (record.has(RecConstants.F_timestamp)) logger.debug("RecConstants.F_timestamp:"+record.getString(RecConstants.F_timestamp));
		    		if (record.has(RecConstants.F_origin)) logger.debug("origin:"+record.getString(RecConstants.F_origin));
		    		if (record.has(RecConstants.F_destination)) logger.debug("destination:"+record.getString(RecConstants.F_destination));
		    		if (record.has("duration")) logger.debug("duration:"+record.getString("duration"));
		    		if (record.has(RecConstants.F_status_code)) logger.debug("status_code:"+record.getString(RecConstants.F_status_code));
		    		if (record.has(RecConstants.F_status_description)) logger.debug("status_description:"+record.getString(RecConstants.F_status_description));
		    		if (record.has(RecConstants.F_message_content)) logger.debug("message_content:"+record.getString(RecConstants.F_message_content));
		    		if (record.has(RecConstants.F_message_status)) logger.debug("message_status:"+record.getString(RecConstants.F_message_status));
		    		*/
		    	}
		    	catch (Exception e)
		    	{
		    		logger.debug("Cannot process line: " + line + "Exception: "+ e.getMessage());
		    	}		    	
		    }

		} catch (IOException e) {
			// handle exception
			e.printStackTrace();
			return;
		}
		
		//calculate average call duration
		for (Map.Entry<Integer, Integer> entry : durationCallsDest.entrySet()) {
	        logger.debug("Destination Code: " + entry.getKey() + ": Total Duration: " + entry.getValue() + " Number of Calls:" + numberCallsDest.get(entry.getKey()));
	        avgDurationCallsDest.put(entry.getKey(), entry.getValue()/numberCallsDest.get(entry.getKey()));
	    }
		
		processed=true;
	}

	public boolean fileProcessed()
	{
		return processed;
	}
	
	public JSONObject getMetrics() throws JSONException
	{
		JSONObject statistics = new JSONObject();
		statistics.put("Number of rows with missing fields", rowsMissingFields);
		statistics.put("Number of messages with blank content", messagesBlankContent);
		statistics.put("Number of rows with fields errors",  rowsFieldErrors);
		statistics.put("Number of calls origin and destination groupped by country code", numberCallsOrigDest);
		statistics.put("Relationship between OK vs KO calls", String.valueOf(numberOKCalls)+" vs "+ String.valueOf(numberKOCalls));
		statistics.put("Average call duration grouped by country code",  avgDurationCallsDest);
		statistics.put("Word occurrence ranking for the given words in message_content field",  wordOccurence);

		return statistics;
	}
}
