Input files:
    
    https://raw.githubusercontent.com/vas-test/test1/master/logs/MCP_YYYYMMDD.json
    
    https://raw.githubusercontent.com/vas-test/test1/master/logs/MCP_20180131.json
    
    https://raw.githubusercontent.com/vas-test/test1/master/logs/MCP_20180201.json
    
    https://raw.githubusercontent.com/vas-test/test1/master/logs/MCP_20180202.json
    

Overview: 
    Spring Boot Application
    Dependencies: Web, Actuator, H2, libphonenumber

Example service invocation: 

    http://localhost:8086/process?date=2018-01-31
    
    http://localhost:8086/metrics?date=2018-01-31
    
    http://localhost:8086/kpis
    
    http://localhost:8086/actuator/metrics
    

The goal of this exercise is to create a service which exposes an API through which we will retrieve information related with certain Mobile Communication Platform. This information provided by the Mobile Communication Platform will be generated per day in separated files which will contain the following information in JSON format:
- message_type: The type of the message. Two values are valid: {CALL|MSG}
- timestamp: The timestamp of the message.
- origin: Mobile identifier of the origin mobile (MSISDN)
- destination: Mobile identifier of the destination mobile (MSISDN)
- duration: Call duration. Only for CALL (message_type)
- status_code: Status code of the call. Only for CALL (message_type). Two values are valid: {OK|KO}
- status_description: Status description of the call. Only for CALL (message_type)
- message_content: Content of the message. Only for MSG (message_type)
- message_status: Status of the message. Two values are valid: {DELIVERED|SEEN}
    
This information represents certain communications between to end users and is given as a JSON object per line. All the attributes are mandatory and their order is fixed:


	CALL -> message_type, timestamp, origin, destination, duration, status_code, status_description
	[
	  {
	    "message_type": "CALL",
	    "timestamp": 151764570,
	    "origin": 34969000001,
	    "destination": 34969000101,
	    "duration": 120,
	    "status_code": "OK",
	    "status_description": "OK"
	  }
	]
  
  
	MSG -> message_type, timestamp, origin, destination, message_content, message_status
	[
	  {
	    "message_type": "MSG",
	    "timestamp": 1517559332,
	    "origin": 34960000003,
	    "destination": 34960000103,
	    "me": "B",
	    "message_status": "SEEN"
	  }
	]

Using the API exposed by this service we will be able to retrieve some metrics and kpis that will be define below based on a given day.\
The service MUST implement the following requirements:
   Java programming language has to be used.
	 Sourcecode has to compile and run.
	 A public GIT repository has to be used so its usage can be evaluated (https://github.com/ or similar)
	 The input JSON may have some errors (missing fields, wrong order, invalid value...)

The service will have an HTTP endpoint that receives a date parameter (YYYY-MM-DD). This method will be requested to select the JSON file to process.

The service will have an HTTP endpoint (/metrics) that returns a set of counters related with the processed JSON file:
- Number of rows with missing fields
- Number of messages with blank content
- Number of rows with fields errors
- Number of calls origin/destination grouped by country code (https://en.wikipedia.org/wiki/MSISDN)
- Relationship between OK/KO calls
- Average call duration grouped by country code (https://en.wikipedia.org/wiki/MSISDN)
- Word occurrence ranking for the given words in message_content field.

The service will have an HTTP endpoint (/kpis) that returns a set of counters related with the service:
- Total number of processed JSON files
- Total number of rows
- Total number of calls
- Total number of messages
- Total number of different origin country codes (https://en.wikipedia.org/wiki/MSISDN)
- Total number of different destination country codes (https://en.wikipedia.org/wiki/MSISDN)
- Duration of each JSON process
