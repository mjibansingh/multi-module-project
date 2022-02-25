package com.databasket.auth.service;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.databasket.auth.entity.AuthenticationLog;
import com.databasket.auth.entity.User;
import com.databasket.auth.repository.AuthenticationLogRepo;
import com.databasket.auth.repository.UserRepo;
import com.databasket.auth.utility.DateUtil;

@Service
public class AuthenticationLogServiceImpl implements AuthenticationLogService {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private AuthenticationLogRepo authLogRepo;
	
	@Autowired
	UserRepo userRepo;	
	
	
	@Override
	public List<AuthenticationLog> getAuthLog(String search) {
		LOGGER.info("getAuthLog : {}", search);
		
		String fromPicker, toPicker, username , eventname;
    	
		//Parse post data
		JSONObject obj = new JSONObject(search);
		try{
	      fromPicker = obj.getString("fromPicker");	
	      toPicker = obj.getString("toPicker");	
	      username = obj.getString("username");	
	      eventname = obj.getString("eventname");	
	      
		}
		catch(JSONException e){
			 throw new RuntimeException(e.getMessage());
		}    	

		//Parse date string "2018-07-08T07:49:36.104Z" to java date using java time[modern approach]		
		Instant instantFrom = Instant.parse(fromPicker);
		Instant instantTo = Instant.parse(toPicker);
		Date startDate = Date.from( instantFrom ) ;
		Date endDate = Date.from( instantTo ) ;

		//Query filter
		List<AuthenticationLog> authLogs = null;
		if(!username.equals("") && eventname.equals("")){
			authLogs = authLogRepo.getAllAuthenticationLogBetweenForUsername(DateUtil.removeTimestampInfo(startDate), DateUtil.removeTimestampInfo(endDate), username);
			
		}
		else if(username.equals("") && !eventname.equals("")){
			authLogs = authLogRepo.getAllAuthenticationLogBetweenForEvent(DateUtil.removeTimestampInfo(startDate), DateUtil.removeTimestampInfo(endDate), eventname);
			
		}
		else if(!username.equals("") && !eventname.equals("")){
			authLogs = authLogRepo.getAllAuthenticationLogBetweenForUsernameAndEvent(DateUtil.removeTimestampInfo(startDate), DateUtil.removeTimestampInfo(endDate), username, eventname);
		}
		else{
			authLogs = authLogRepo.getAllAuthenticationLogBetween(DateUtil.removeTimestampInfo(startDate), DateUtil.removeTimestampInfo(endDate)); 
		}
		

		return authLogs;
	}


	@Override
	public List<AuthenticationLog> getAllAuthLog() {
		List<AuthenticationLog> authLogList = authLogRepo.findAll();
		return authLogList;
	}

	
	@Override
	public AuthenticationLog addLoginAuthLog(String username) {
		LOGGER.info("addAuthLog : {}", username);
		
		Assert.notNull(username, "Username is required.");
		User existUser = userRepo.findByUsername(username);
		Assert.notNull(existUser, "User does not exist.");
		
		AuthenticationLog authLog = new AuthenticationLog();
		authLog.setUsername(username);
		authLog.setEvent("Login");
		authLog.setTimestamp(new Date());
		authLogRepo.save(authLog);
		LOGGER.info("AuthLog Login added successfully.");

		return authLog;		
	}
	
	@Override
	public AuthenticationLog addLogoutAuthLog(String username) {
		LOGGER.info("addLogoutAuthLog : {}", username);

		Assert.notNull(username, "Username is required.");
		User existUser = userRepo.findByUsername(username);
		Assert.notNull(existUser, "User does not exist.");
				
		AuthenticationLog authLog = new AuthenticationLog();
		authLog.setUsername(username);
		authLog.setEvent("Logout");
		authLog.setTimestamp(new Date());
		authLogRepo.save(authLog);
		LOGGER.info("AuthLog Logout added successfully.");

		return authLog;		
	}	

}
