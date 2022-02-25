package com.databasket.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordInfo {

	private String userName;
	private String currentPassword;
	private String newPassword;
	
	private String token;
	private String otp;
	
	public ChangePasswordInfo() {}
		
}
