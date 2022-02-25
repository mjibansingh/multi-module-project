package com.databasket.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OTPRequestInfo {
	
	private Long userId;
	private String role;
	private String token;
	private String telephone;
    private String otp;
    
    /**
     * Check enum {@link OTPOperation}
     */
    private String operation;
    
    private String password; //used for grocery
    	
}
