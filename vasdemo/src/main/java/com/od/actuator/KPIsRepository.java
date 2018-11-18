package com.od.actuator;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
		registry.counter("Total number of processed JSON files");
		registry.counter("Total number of rows");
		registry.counter("Total number of calls");
		registry.counter("Total number of messages");
		registry.counter("Total number of different origin country codes");
		registry.counter("Total number of different destination country codes");
	}

	public void updateCounters( MCPJSONFile mCPJSON) {
		registry.counter("Total number of processed JSON files").increment(1.0);
		registry.counter("Total number of rows").increment(mCPJSON.getRowsProcessed());
		registry.counter("Total number of calls").increment(mCPJSON.getnumberOfcalls());
		registry.counter("Total number of messages").increment(mCPJSON.getnumberOfmessages());
	}
	
	public JSONObject getKPIs () {
		logger.debug(registry.getMeters().toString());
		return null;
	}
}


