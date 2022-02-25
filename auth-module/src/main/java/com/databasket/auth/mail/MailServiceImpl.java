
package com.databasket.auth.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailParseException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MailServiceImpl implements MailService{
    @Autowired
    private JavaMailSender sender;

    Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    @Override
    @Async
    public void sendEmail(MailRequest mailRequest) throws Exception{
    	
    	MimeMessage message = sender.createMimeMessage();
		try{
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(mailRequest.getFrom(), mailRequest.getSendMailAs());
			helper.setTo(mailRequest.getTo());
			if(mailRequest.getCc() != null && !mailRequest.getCc().isEmpty()) helper.setCc(mailRequest.getCc());
			helper.setSubject(mailRequest.getSubject());
			
			if(mailRequest.getAttachedFileName()!=null){
				FileSystemResource file = new FileSystemResource(mailRequest.getAttachedFileName());
				helper.addAttachment(file.getFilename(), file);
			}
			
			if(mailRequest.getFileName()!=null){
				StringBuffer sb = new StringBuffer();
				FileInputStream fstream = new FileInputStream(mailRequest.getFileName());
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				String singleLine;
				while ((singleLine = br.readLine()) != null) {
					sb.append(singleLine);
					sb.append("<br>");
				}
				br.close();
				String allLines = sb.toString();
				helper.setText(allLines+(mailRequest.getMailbody()!=null?mailRequest.getMailbody():""),true);
			}else{
				helper.setText(mailRequest.getMailbody(), true);
			}
			
			// Add all inline attachments
			if(mailRequest.getInlineAttachemnts() != null && !mailRequest.getInlineAttachemnts().isEmpty())
				for(Entry<String, File> entry : mailRequest.getInlineAttachemnts().entrySet())
					helper.addInline(entry.getKey(), entry.getValue());
		}
		catch (MessagingException e) {
			LOGGER.error("Error sending email. Error: " + e.getMessage(), e);
			throw new MailParseException(e);
		}
		
		sender.send(message);
		LOGGER.debug("Email request sent for " + StringUtils.collectionToDelimitedString(Arrays.asList(mailRequest.getTo()), ",", "[", "]"));
    }
    
}
