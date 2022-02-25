package com.databasket.auth.events;

import java.util.Arrays;

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
import com.databasket.auth.service.UserService;

@Component
public class PasswordResetListener implements ApplicationListener<OnPasswordResetCompleteEvent> {
	Logger LOGGER = LoggerFactory.getLogger(PasswordResetListener.class);
	
    @Autowired
    private UserService service;

    @Autowired
    MailService mailService;
    
    @Autowired
    private MessageSource messages;

    @Autowired
    private Environment env;
    
    @Override
    public void onApplicationEvent(final OnPasswordResetCompleteEvent event) {
        this.confirmPasswordReset(event);
    }
   
    private void confirmPasswordReset(final OnPasswordResetCompleteEvent event) {
    	LOGGER.error("confirmPasswordReset event...");
        final UserDto userInfo = event.getUserInfo();
        User user = service.findUserByEmail(userInfo.getEmail());

		// Compose email
		String message = messages.getMessage(
				"message.registration.resetPassword", 
				new Object[]{"<a href='" + event.getUserInfo().getLoginURL() + "'>Click here</a>", "<b>'" + event.getUserInfo().getPassword() + "'</b>"}, 
				event.getLocale());
		
		sendEmail(user.getEmail(), message);		
    }    

    public final void sendEmail(String toEmail, String body) {
    	LOGGER.info("sending email...");
    	MailRequest mailRequest = new MailRequest();
    	mailRequest.setFrom(env.getProperty("support.email"));
    	mailRequest.setSubject("Pasword reset successful");
    	mailRequest.setTo(toEmail);
    	//mailRequest.setTo(new String[] {toEmail});
    	mailRequest.setMailbody(body);
    	try {
			mailService.sendEmail(mailRequest);
			LOGGER.info("sent email successfully...");
		} catch (Exception e) {
			LOGGER.error("Error sending user account registration email");
		}    	
    }

}
