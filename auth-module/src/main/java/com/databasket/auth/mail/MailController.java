
package com.databasket.auth.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

	@Controller
	@CrossOrigin(origins="*")
	public class MailController {
	    @Autowired
	    MailService mailService;

	    @RequestMapping("/simpleemail")
	    public @ResponseBody String home() {
	    	/* TEST MAIL*/
	    	MailRequest mailRequest = new MailRequest();
	    	mailRequest.setTo("dainasharma453@gmail.com");
	    	mailRequest.setFrom("dynesharma@gmail.com");
	    	mailRequest.setSubject("Test Subject");
	    	mailRequest.setMailbody("This is test mail body");
	    	mailRequest.setFileName("G:\\test.txt");
	    	mailRequest.setAttachedFileName("G:\\test.txt");
	        try {
	            mailService.sendEmail(mailRequest);
	            return "Email Sent!";
	        }catch(Exception ex) {
	            return "Error in sending email: "+ex;
	        }
	    }

	   }
