package com.od.vasdemo;

import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.od.actuator.KPIsRepository;
import com.od.vasdemo.persist.MetricsRepository;
import com.od.vasdemo.service.MCPJSONFile;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.configurationprocessor.json.JSONException;

@RestController
public class ApplicationController {

	Logger logger = LoggerFactory.getLogger(ApplicationController.class);
	@Autowired
	private ConfigProp appProperties;

	@Autowired
	MetricsRepository metricsRepository;
	
	@Autowired
	KPIsRepository kpis;
	
	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@RequestMapping("/greeting")
	public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
		return new Greeting(counter.incrementAndGet(),
				String.format(template, name));
	}

	//endpoint for processing a specific file
	//the file will be retrieved from the web location
	//the data will be persisted in the mysql database
	@RequestMapping("/process")
	public String processFile(@RequestParam(value="date", defaultValue="NoDate") String fileDate) throws JSONException {
		if (fileDate.matches("^(\\d{4}-\\d{2}\\-\\d{2})")) {

			//file matches the pattern
			logger.debug("File name matches pattern");

			StringTokenizer st = new StringTokenizer(fileDate, "-");
			String fileName="";
			while(st.hasMoreTokens()) fileName += st.nextToken();

			String url=appProperties.getFilesURL()+"MCP_"+fileName+".json";

			logger.debug("File to check: "+url);
			//check if file exits

			//processing file
			MCPJSONFile mCPJSON= new MCPJSONFile(url, "MCP_"+fileName+".json", appProperties);

			if (mCPJSON.fileProcessed()) {

				//persist the processed metrics
				boolean rowsInserted = metricsRepository.insertMetrics(fileDate, mCPJSON.getMetrics());
				if (!rowsInserted)
					return "File has been processed already and exists in the database";

				//save KPIs using MeteRegistry
				kpis.updateCounters(mCPJSON);

				return mCPJSON.getMetrics().toString();
			}
			else 
				return "File for " +  fileDate + " could not be processed!";
		}
		else
			return "The entered date does is not present or does not match the YYYY-MM-DD pattern";
	}

	//endpoint for returning the summary for a processed file
	//the mysql database will be checked for file processed
	//the metrics will be retrieved if that is the case
	@RequestMapping("/metrics")
	public String returnMetrics(@RequestParam(value="date") String fileDate) {
		return metricsRepository.getMetrics(fileDate);
	}

	//endpoint for returning the kpis 
	@RequestMapping("/kpis")
	public String returnKPIs() {
		return kpis.getKPIs().toString();
	}

	
}