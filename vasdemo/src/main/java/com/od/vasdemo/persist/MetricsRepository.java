package com.od.vasdemo.persist;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.od.vasdemo.ApplicationController;

@Repository
public class MetricsRepository {

	@Autowired
	JdbcTemplate jdbcTemplate;

	Logger logger = LoggerFactory.getLogger(ApplicationController.class);

	private String insertSQL = "INSERT INTO VAS_METRICS (filedate, json) values (?, ?)";

	public boolean insertMetrics(String fileDate, JSONObject metrics) {

		logger.debug("Inserting metrics for: " +fileDate+" Metrics: " + metrics.toString());
        try {
        	int row = jdbcTemplate.update(insertSQL, fileDate, metrics.toString());
            logger.debug("Rows updated: " + row);
        }
        catch (Exception e)
        {
        	logger.debug("Existing Row");
        	return false;
        }
        return true;
	}

	public String getMetrics(String fileDate) {

		logger.debug("Getting metrics for: " +fileDate);
        try {
        	String json = (String) jdbcTemplate.queryForObject("select json from vas_metrics where filedate=?", new Object[] { fileDate }, String.class);
            logger.debug("JSON retrieved from database: " + json);
            return json;
        }
        catch (Exception e)
        {
        	return "";
        }
	}

}
