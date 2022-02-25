package com.databasket.auth.events;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.databasket.auth.dto.UserDto;
import com.databasket.auth.entity.User;
import com.databasket.auth.entity.VerificationToken;
import com.databasket.auth.mail.MailRequest;
import com.databasket.auth.mail.MailService;
import com.databasket.auth.repository.VerificationTokenRepo;
import com.databasket.auth.service.UserService;
import com.databasket.auth.utility.SecConstants.TokenStatus;
import com.databasket.auth.utility.SecConstants.UserEventSubtype;

@Component
public class UserEventListener {

	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	UserService userService;
	
	@Autowired
    private MessageSource messages;
	
	@Autowired
	private VerificationTokenRepo verificationTokenRepository;
	
	@EventListener
	public void onApplicationEvent(UserEvent event) {
		User user = event.getUser();

        if(UserEventSubtype.USER_REGISTERED.toString().equals(event.getEventSubtype())) {
        	if (!user.isEnabled()) {
        		VerificationToken token = verificationTokenRepository.findByUser(user);
        		
        		if(token == null) {
        			token = new VerificationToken();
        			token.setUser(user);
        			token.setToken(UUID.randomUUID().toString());
        		}
        				 
        		
        		if(token != null && TokenStatus.USED.toString().equals(token.getStatus())) {
        			token.setToken(UUID.randomUUID().toString());
        		}
        		
        		token.setExpiryDate(generateTokenExpiryDate(60*24*7)); //set expiry time 7 days
        		token.setOtp(null);
        		token.setStatus(TokenStatus.GENERATED.toString());
        		verificationTokenRepository.save(token);
        		
        		//VerificationToken token = userService.saveVerificationToken(user);
            	String emailBody = messages.getMessage(
    					"message.registration.email", 
    					new Object[]{getAppUrl() + env.getProperty("auth.endpoint.user.activate") + "?token=" + token.getToken()}, 
    					event.getLocale());
    			sendEmail(user.getEmail(), emailBody, "Registration successful");	
        	}
            return;
        }
        
        if(UserEventSubtype.PASSWORD_RESET.toString().equals(event.getEventSubtype())) {
        	String emailBody = "Your password has been changed. Your default password is " + event.getMessage() + ". <a href='"+ env.getProperty("pms.baseurl") +"'>Login</a>.";
			sendEmail(user.getEmail(), emailBody, "Password reset sucessful");
        }
        
        if(UserEventSubtype.PASSWORD_RESET_CODE.toString().equals(event.getEventSubtype())) {
        	if (user.isEnabled()) {
        		VerificationToken token = verificationTokenRepository.findByUser(user);
            	String emailBody = messages.getMessage(
    					"message.password.reset.otp.email", 
    					new Object[]{token.getOtp()}, 
    					event.getLocale());
    			sendEmail(user.getEmail(), emailBody, "Password reset code");	
        	}
            return;
        }        
		
	}
	
	private Date generateTokenExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        return new Date(cal.getTimeInMillis() + (1000 * 60 * expiryTimeInMinutes));
    }
	
	@Autowired
	Environment env;
	
	@Autowired
	MailService emailService;
	
	Logger LOGGER = LoggerFactory.getLogger(UserEventListener.class);
	
	public final void sendEmail(String toEmail, String body, String subject) {
    	MailRequest mailRequest = new MailRequest();
    	mailRequest.setFrom(env.getProperty("support.email"));
    	mailRequest.setSendMailAs(env.getProperty("email.send-mail-as"));
    	mailRequest.setSubject(subject);
    	mailRequest.setTo(toEmail);
//    	mailRequest.setTo(new String[] {toEmail});
    	mailRequest.setMailbody(body);
    	try {
    		emailService.sendEmail(mailRequest);
		} catch (Exception e) {
			LOGGER.error("Error sending user account registration email");
		}    	
    }
	
	private String getAppUrl() {
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		return env.getProperty("baseurl") + "/" + request.getContextPath();
	}
	
}