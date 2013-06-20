package ru.efive.wf.core.data;

import java.util.ArrayList;
import java.util.List;

public class MailMessage {
	
	public MailMessage(List<String> sendTo, List<String> copyTo, String subject, String body) {
		this.sendTo = sendTo;
		this.copyTo = copyTo;
		this.subject = subject;
		this.body = body;
	}
	
	
	public List<String> getSendTo() {
		return sendTo == null? new ArrayList<String>(): sendTo;
	}
	
	public void setSendTo(List<String> sendTo) {
		this.sendTo = sendTo;
	}
	
	public List<String> getCopyTo() {
		return copyTo == null? new ArrayList<String>(): copyTo;
	}
	
	public void setCopyTo(List<String> copyTo) {
		this.copyTo = copyTo;
	}
	
	public List<String> getBlindCopyTo() {
		return blindCopyTo == null? new ArrayList<String>(): blindCopyTo;
	}
	
	public void setBlindCopyTo(List<String> blindCopyTo) {
		this.blindCopyTo = blindCopyTo;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	
	private List<String> sendTo;
	private List<String> copyTo;
	private List<String> blindCopyTo;
	
	private String subject;
	
	private String body;
	
	private String contentType;
}