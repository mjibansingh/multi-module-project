package com.databasket.auth.service;

import java.util.List;

import com.databasket.auth.dto.UserDto;
import com.databasket.auth.entity.User;


public interface UserManagement {
	public UserDto registerUser(UserDto userInfo);
	public UserDto registerUserByAdmin(UserDto userInfo);
	public UserDto activeteRegisterUser(UserDto userInfo);
	
	public UserDto resetUserPassword(UserDto userInfo);
	public UserDto resetUserPasswordByAdmin(UserDto userInfo);
	
	public List<String> getAllRole();
	
	public List<UserDto> getAllUser();
	
	//Diagnostic API
	public List<User> registerCustomers(List<UserDto> users);
}
