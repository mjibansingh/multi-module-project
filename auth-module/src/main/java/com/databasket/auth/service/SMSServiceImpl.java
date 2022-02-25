
package com.databasket.auth.service;

import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.databasket.auth.dto.SMSRequestInfo;

@Service
public class SMSServiceImpl implements SMSService {
	@Value("${security-core.sms.url}")
	private String smsUrl;

	@Value("${security-core.sms.endpoint.sendsms}")
	private String sendSMSEndpoint;
	
	@Value("${security-core.sms.license}")
	private String license;
	
	SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat();
	
	static ClientHttpRequestFactory clientHttpRequestFactory; 
	
	static {
	    int timeout = 10000;
	    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
	    factory.setConnectTimeout(timeout);
	    factory.setReadTimeout(timeout);
	    clientHttpRequestFactory = factory;
	}
	
	Logger LOGGER = LoggerFactory.getLogger(SMSServiceImpl.class);
	
	@Override
	@Async
	public CompletableFuture<Object> sendSMS(SMSRequestInfo smsRequestInfo) throws RuntimeException {
		LOGGER.info("Sending SMS...");
		smsRequestInfo.setLicense(license);
		RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
		Object responseBody = null;
		try{
			HttpEntity<SMSRequestInfo> request = new HttpEntity<SMSRequestInfo>(smsRequestInfo);
			LOGGER.info("Sending SMS to {}...", smsUrl + sendSMSEndpoint);
			HttpEntity<Object> response = restTemplate.exchange(smsUrl + sendSMSEndpoint, HttpMethod.POST, request, Object.class);
			if(response.getBody() !=null) responseBody = response.getBody();
			LOGGER.info("Response: {}", response.getBody());
		}catch(Exception e){
			HttpServerErrorException exception = new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to send SMS. This may be mostly because the SMS service is down or is under maintenance, please contact your system administrators. [Error: " + e.getMessage() + "]");
			LOGGER.error(exception.getMessage(), e);
		}
		
		return CompletableFuture.completedFuture(responseBody);
	}
	
	@Value("${security-core.sms.bill.title}")
	private String billSMSTitle;
	
	@Value("${security-core.sms.bill.sender}")
	private String billSMSSender;
	
	@Value("${security-core.sms.order.confirm.title}")
	private String orderConfirmationSMSTitle;
	
	@Value("${security-core.sms.order.confirm.sender}")
	private String orderConfirmationSMSSender;	
	
	@Override
	public Object sendOrderConfirmationSMSToRestaurant(Long orderId) throws RuntimeException {
		// TODO Auto-generated method stub
		LOGGER.info("Building SMS...");
		return null;
	}

	@Override
	public Object sendOrderConfirmationSMSToDeliveryCompany(Long orderId) throws RuntimeException {
		LOGGER.info("Building SMS...");
		// TODO Auto-generated method stub
		return null;
	}
	   
	@Override
	public SMSRequestInfo buildRegistrationConfirmationSMSRequestInfo(final String telephone, final String token) throws RuntimeException {
		SMSRequestInfo smsRequestInfo = null;
		//Customer customer = kot.getCustomer();
		if(telephone != null && !telephone.isEmpty()){
			smsRequestInfo = new SMSRequestInfo();
			
			try {
				smsRequestInfo.setMessage(token + " is your OTP. NEVER SHARE IT WITH ANYONE");
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);				
			}
			
			smsRequestInfo.setMobiles(telephone.substring(telephone.length() - 10));//
			smsRequestInfo.setSender(orderConfirmationSMSSender);
			smsRequestInfo.setSmsTitle(orderConfirmationSMSTitle);	
			return smsRequestInfo;
		}else{
			LOGGER.warn("Did not find contact information. Aborting SMS request...");
			return null;	
		}	
	}	

}
