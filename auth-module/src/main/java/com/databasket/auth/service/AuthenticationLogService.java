package com.databasket.auth.service;

import java.util.List;

import com.databasket.auth.entity.AuthenticationLog;

public interface AuthenticationLogService {
	public AuthenticationLog addLoginAuthLog(String username);
	public AuthenticationLog addLogoutAuthLog(String username);
	
	public List<AuthenticationLog> getAllAuthLog();
	public List<AuthenticationLog> getAuthLog(String search);

}
