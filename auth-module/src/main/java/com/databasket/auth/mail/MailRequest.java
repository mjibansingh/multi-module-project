package com.databasket.auth.mail;

import java.io.File;
import java.util.Map;

/**
 * @author dainasharma
 *
 */
public class MailRequest {
	private String to;
	private String from;
	private String cc;
	private String sendMailAs;
	private String subject;
	private String attachedFileName;//full file name with path
	private String mailbody;
	private String fileName;//text file to be copied to mail body
	private Map<String, File> inlineAttachemnts;
	
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getAttachedFileName() {
		return attachedFileName;
	}
	public void setAttachedFileName(String attachedFileName) {
		this.attachedFileName = attachedFileName;
	}
	public String getMailbody() {
		return mailbody;
	}
	public void setMailbody(String mailbody) {
		this.mailbody = mailbody;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getSendMailAs() {
		return sendMailAs;
	}
	public void setSendMailAs(String sendMailAs) {
		this.sendMailAs = sendMailAs;
	}
	public Map<String, File> getInlineAttachemnts() {
		return inlineAttachemnts;
	}
	public void setInlineAttachemnts(Map<String, File> inlineAttachemnts) {
		this.inlineAttachemnts = inlineAttachemnts;
	}
	public String getCc() {
		return cc;
	}
	public void setCc(String cc) {
		this.cc = cc;
	}	
}
