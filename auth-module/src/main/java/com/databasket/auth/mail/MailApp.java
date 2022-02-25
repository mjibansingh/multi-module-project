package com.databasket.auth.mail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;


//@SpringBootApplication
public class MailApp 
{
    public static void main(String[] args) throws Exception{
        SpringApplication.run(MailApp.class, args);
    }
    
    @Autowired
    MailService mailService;
    
    @PostConstruct
    public void init() throws Exception{
    	// Send a test main at startup
    	MailRequest mailRequest = new MailRequest();
    	mailRequest.setFrom("Daya");
    	mailRequest.setSendMailAs("Daya");
    	mailRequest.setSubject("Test");
    	mailRequest.setTo("daiayum@gmail.com");
    	
    	String contentId = ContentIdGenerator.getContentId();
        String htmlText = "Hello,</br> <p>This is test with email inlines.</p><img style='width: 56px' src=\"cid:" + contentId + "\" />";
        
        File signatureLogoLocation = new File("logos/email-signatures");
        File[] signatureLogos = signatureLogoLocation.listFiles();
        
        for (File logo : signatureLogos) {
            System.out.println(logo.getName());
        }
        
        File footerLogoLocation = new File("logos/email-footer-logos");
        File[] footerLogos = footerLogoLocation.listFiles();
        
        for (File logo : footerLogos) {
            System.out.println(logo.getName());
        }
        
        File inlineAttachment = new File("databasket.png");
        Map<String, File> inlineAttachments = new HashMap<String, File>();
        inlineAttachments.put(contentId, inlineAttachment);
        mailRequest.setInlineAttachemnts(inlineAttachments);
        mailRequest.setMailbody(htmlText);
    	mailService.sendEmail(mailRequest);
    }
}
