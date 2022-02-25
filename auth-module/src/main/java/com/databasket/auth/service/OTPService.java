package com.databasket.auth.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;

import com.databasket.auth.dto.ChangePasswordInfo;
import com.databasket.auth.dto.OTPRequestInfo;
import com.databasket.auth.dto.UserDto;
import com.databasket.auth.entity.User;

public interface OTPService {

	public String validateUserWithOTP(OTPRequestInfo otpRequest);
	//public void saveVerificationTokenForUser(User user, String otp, String token);
	public Map<String, String> saveVerificationTokenForUser(User user, String token, String otp);
		
	String resendOTP(OTPRequestInfo otpRequest);
	String sendOTP(OTPRequestInfo otpRequest);
	String generateOTP(User user);
	
	//Diagnostic API
	public List<User> registerCustomers(List<UserDto> users);
	
	//Grocery Vendor API
	public User registerCustomer(UserDto user);	
	
	//Grocery User API
	public User register(UserDto user);	
	
	public String sendPasswordResetCode(String email);
	public String changePasswordWithCode(ChangePasswordInfo credential);

}
