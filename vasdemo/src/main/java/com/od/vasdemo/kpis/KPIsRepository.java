package com.od.vasdemo.kpis;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Repository;

import com.od.vasdemo.ApplicationController;
import com.od.vasdemo.service.MCPJSONFile;

import io.micrometer.core.instrument.MeterRegistry;

@Repository
public class KPIsRepository {	
	
	@Autowired
	MeterRegistry registry;

	Logger logger = LoggerFactory.getLogger(ApplicationController.class);
	
	@PostConstruct
	public void init_counters()
	{
		registry.counter("custom.nr_processed_json_files");
		registry.counter("custom.nr_rows");
		registry.counter("custom.nr_calls");
		registry.counter("custom.nr_messages");
	}

	public void updateCounters( MCPJSONFile mCPJSON) {
		registry.counter("custom.nr_processed_json_files").increment(1.0);
		registry.counter("custom.nr_rows").increment(mCPJSON.getRowsProcessed());
		registry.counter("custom.nr_calls").increment(mCPJSON.getnumberOfcalls());
		registry.counter("custom.nr_messages").increment(mCPJSON.getnumberOfmessages());

		//add counters for new origins
		mCPJSON.getnumberCallsOrig().forEach((k,v)->registry.counter("Orig:"+k).increment(v));
		//add counters for new destinations
		mCPJSON.getnumberCallsDest().forEach((k,v)->registry.counter("Dest:"+k).increment(v));
	}
	
	public JSONObject getKPIs () throws JSONException {
		logger.debug(registry.getMeters().toString());
		JSONObject recordKPIs = new JSONObject();
		
		recordKPIs.put("Total number of processed JSON files", registry.counter("custom.nr_processed_json_files").count());
		recordKPIs.put("Total number of rows", registry.counter("custom.nr_rows").count());
		recordKPIs.put("Total number of calls", registry.counter("custom.nr_calls").count());
		recordKPIs.put("Total number of messages", registry.counter("custom.nr_messages").count());
		
		return recordKPIs;
	}
}


