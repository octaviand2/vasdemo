package com.od.vasdemo.service;

import java.util.HashMap;

public class MetricsDailyData {
	private Long rowsMissingFields;
	private Long rowsFieldErrors;
	private Long messagesBlankContent;
	private Long numberOKCalls;
	private Long numberKOCalls;
	private HashMap <String,Integer> numberCallsOrigDest = new HashMap<String,Integer>();
	private HashMap <Integer,Integer> avgDurationCallsDest = new HashMap<Integer,Integer>();
	private HashMap <String,Integer> wordOccurence = new HashMap<String,Integer>();

	public MetricsDailyData() {
		super();
	}

	public Long getrowsMissingFields() {
		return rowsMissingFields;
	}
	public Long getrowsFieldErrors() {
		return rowsFieldErrors;
	}
	public Long getmessagesBlankContent() {
		return messagesBlankContent;
	}
	public Long getnumberOKCallss() {
		return numberOKCalls;
	}
	public Long getnumberKOCalls() {
		return numberKOCalls;
	}

	public void setrowsMissingFields(Long rowsMissingFields) {
		this.rowsMissingFields = rowsMissingFields;
	}	
	public void setrowsFieldErrors(Long rowsFieldErrors) {
		this.rowsFieldErrors = rowsFieldErrors;
	}	
	public void setmessagesBlankContent(Long messagesBlankContent) {
		this.messagesBlankContent = messagesBlankContent;
	}	
	public void setnumberOKCalls(Long numberOKCalls) {
		this.numberOKCalls = numberOKCalls;
	}	
	public void setnumberCallsOrig(Long numberKOCalls) {
		this.numberKOCalls = numberKOCalls;
	}	
	
	public String getnumberCallsOrigDest() {
		return numberCallsOrigDest.toString();
	}
	public String getavgDurationCallsDest() {
		return avgDurationCallsDest.toString();
	}
	public String getwordOccurence() {
		return wordOccurence.toString();
	}
	
	public void setnumberCallsOrigDest(HashMap <String,Integer> numberCallsOrigDest) {
		this.numberCallsOrigDest = numberCallsOrigDest;
	}
	public void setavgDurationCallsDest(HashMap <Integer,Integer> avgDurationCallsDest) {
		this.avgDurationCallsDest = avgDurationCallsDest;
	}
	public void setwordOccurence(HashMap <String,Integer> wordOccurence) {
		this.wordOccurence = wordOccurence;
	}

	
}