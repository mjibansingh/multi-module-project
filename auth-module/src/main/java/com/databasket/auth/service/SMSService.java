package com.databasket.auth.service;

import com.databasket.auth.dto.SMSRequestInfo;

public interface SMSService {
	public Object sendSMS(SMSRequestInfo smsRequestInfo) throws Exception;

	public Object sendOrderConfirmationSMSToRestaurant(Long orderId) throws RuntimeException;
	public Object sendOrderConfirmationSMSToDeliveryCompany(Long orderId) throws RuntimeException;
	
	public SMSRequestInfo buildRegistrationConfirmationSMSRequestInfo(String telephone, String token) throws RuntimeException;

}
