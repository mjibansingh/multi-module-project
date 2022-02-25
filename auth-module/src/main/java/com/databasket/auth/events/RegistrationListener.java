package com.databasket.auth.events;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.databasket.auth.dto.UserDto;
import com.databasket.auth.entity.User;
import com.databasket.auth.mail.MailRequest;
import com.databasket.auth.mail.MailService;
import com.databasket.auth.service.OTPService;
import com.databasket.auth.service.SMSService;
import com.databasket.auth.service.UserService;
import com.databasket.auth.utility.SecConstants.UserEventSubtype;


@Component
public class RegistrationListener implements ApplicationListener<UserEvent> {
	Logger LOGGER = LoggerFactory.getLogger(RegistrationListener.class);
	
    @Autowired
    private UserService service;

    @Autowired
    MailService mailService;
    
    @Autowired
    private MessageSource messages;

    @Autowired
    private Environment env;
    
    @Autowired
    SMSService smsService;
    
    @Autowired
    OTPService otpService;
    
    @Override
    public void onApplicationEvent(final UserEvent event) {
        
    	//UserDto userInfo = event.getUserInfo();
        //User user = service.findUserByEmail(userInfo.getEmail());
    	User user = event.getUser();
        LOGGER.debug("Event Subtype: {}", event.getEventSubtype());
   
        if(UserEventSubtype.USER_REGISTERED.toString().equals(event.getEventSubtype())) {
        	String token = UUID.randomUUID().toString();   	     
            otpService.saveVerificationTokenForUser(user, token, null);//TODO : null=?????
		    String emailBody = messages.getMessage(
					"message.registration.email", 
					new Object[]{event.getAppUrl() + "?token=" + token}, 
					event.getLocale());
			sendEmail(user.getEmail(), emailBody);
			return;
        }
        
        if(UserEventSubtype.OTP_USER_REGISTERED.toString().equals(event.getEventSubtype())) {
    	 	otpService.generateOTP(user);
    		String emailBody = "Thanks for registering with us. We have sent an OTP to your registered mobile number. Thank you.";
        	sendEmail(user.getEmail(), emailBody);
        	return;
        }
        
//        if(UserEventSubtype.PASSWORD_GENERATED.toString().equals(event.getEventSubtype())) {
//    	    String emailBody = messages.getMessage(
//					"message.registration.emailwithpassword", 
//					new Object[]{"<a href='" + event.getUserInfo().getLoginURL() + "'>Click here</a>", "<b>'" + event.getUserInfo().getPassword() + "'</b>"}, 
//					event.getLocale());
//        	sendEmail(user.getEmail(), emailBody);
//        	return;
//        }
        
        if(UserEventSubtype.ACCOUNT_ACTIVATED.toString().equals(event.getEventSubtype())) {
    	 	String emailBody = "Welcome! You are registered with us.";
        	sendEmail(user.getEmail(), emailBody);
        	return;
        }
        
    }
    
    public final void sendEmail(String toEmail, String body) {
    	MailRequest mailRequest = new MailRequest();
    	mailRequest.setFrom(env.getProperty("support.email"));
    	mailRequest.setSubject("Registration successful");
    	mailRequest.setTo(toEmail);
    	//mailRequest.setTo(new String[] {toEmail});
    	mailRequest.setMailbody(body);
    	try {
			mailService.sendEmail(mailRequest);
		} catch (Exception e) {
			LOGGER.error("Error sending user account registration email");
		}    	
    }

}
