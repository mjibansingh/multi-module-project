package com.databasket.auth.service;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.databasket.auth.dto.AuthUserInfo;
import com.databasket.auth.dto.ChangePasswordInfo;
import com.databasket.auth.dto.UserDto;
import com.databasket.auth.dto.UserInfo;
import com.databasket.auth.entity.Role;
import com.databasket.auth.entity.User;

public interface UserService {

	public User get(String username);

	public UserInfo register(UserInfo userInfo, boolean generatePassword, boolean activate, boolean mergeAccount);

	public String activate(String token);

	public AuthUserInfo register(UserInfo userInfo);

	public User changePassword(ChangePasswordInfo credential);
	
	public String sendActivationLink(String email);
	
	public User updateProfile(UserInfo user);
	public User findUserByEmail(String email);
	
	/*employee user management*/
	public User resetPassword(UserDto userInfo, HttpServletRequest request);
	public User disableAccount(UserDto user);
	public User enableAccount(UserDto user);	
	public User updateRole(UserDto user);
	public UserDto updateProfile(UserDto user);
	
	//helper methods
	public String getAuditorRole();
	public String getUserRole(Collection<Role> roles);
	
	public List<String> getRolesHierachy(String auditorRole);
	
}
