package com.databasket.auth.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SecConstants {
	//Generic status
	
	public static final String STATUS_ACTIVE = "Active";
	public static final String STATUS_INACTIVE = "Inactive";
	
	public static final String STATUS_SUCCESS = "Success";
	public static final String STATUS_ERROR = "Error";
	
	public static final String TELEPHONE_VALID = "Valid telephone";
	public static final String TELEPHONE_INVALID = "Invalid telephone"; 
	
	public enum StatusType {
		ACTIVE						{ @Override public String toString() {return "Active";} },
		INACTIVE					{ @Override public String toString() {return "Inactive";} };
	}
	
	public enum SystemRole {
		DEFAULT						{ @Override public String toString() {return "Default";} },
		ROOT						{ @Override public String toString() {return "Root";} };		
	}
	
	public enum DefaultRole {		
		SUPER_ADMIN					{ @Override public String toString() {return "Super Admin";} },
		ADMIN						{ @Override public String toString() {return "Admin";} },
		CASHIER						{ @Override public String toString() {return "Cashier";} },
		DOCTOR						{ @Override public String toString() {return "Doctor";} },
		NURSE						{ @Override public String toString() {return "Nurse";} },
		RECEPTION					{ @Override public String toString() {return "Reception";} };
	}
	
	public static final List<String> ROLE_LIST;
	static{
		List<String> roles = new ArrayList<String>();
		roles.add(DefaultRole.SUPER_ADMIN.toString());
		roles.add(DefaultRole.ADMIN.toString());
		roles.add(DefaultRole.CASHIER.toString());
		roles.add(DefaultRole.DOCTOR.toString());
		roles.add(DefaultRole.NURSE.toString());
		roles.add(DefaultRole.RECEPTION.toString());
		ROLE_LIST = Collections.unmodifiableList(roles);
	}	

	public enum OTPOperation {
		LOGIN					{ @Override public String toString() {return "Login";} },
		CHANGE_MOBILE_NUMBER	{ @Override public String toString() {return "Change Mobile Number";} };
	}
	
	public static final List<String> OTP_OPERATIONS;
	static{
		List<String> operations = new ArrayList<>();
		operations.add(OTPOperation.LOGIN.toString());
		operations.add(OTPOperation.CHANGE_MOBILE_NUMBER.toString());
		OTP_OPERATIONS = Collections.unmodifiableList(operations);
	}
	
	public enum UserEventSubtype {
		PASSWORD_RESET_CODE			{ @Override public String toString() {return "Password Reset Code";} },
		USER_REGISTERED			{ @Override public String toString() {return "User Registered";} },
		OTP_USER_REGISTERED		{ @Override public String toString() {return "OTP User Registered";} },
		ACCOUNT_ACTIVATED		{ @Override public String toString() {return "Account Activated";} },
		PASSWORD_GENERATED		{ @Override public String toString() {return "Password Generated";} },
		PASSWORD_RESET			{ @Override public String toString() {return "Password Reset";} };
	}
	
	public enum TokenStatus{
		GENERATED			{ @Override public String toString() {return "Generated";} },
		USED				{ @Override public String toString() {return "Used";} },
		EXPIRED				{ @Override public String toString() {return "Expired";} }
		
	}
	
}

