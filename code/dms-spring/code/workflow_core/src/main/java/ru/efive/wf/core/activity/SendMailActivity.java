package ru.efive.wf.core.activity;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.beanutils.PropertyUtils;

import ru.efive.wf.core.IActivity;
import ru.efive.wf.core.ProcessedData;
import ru.efive.wf.core.Settings;
import ru.efive.wf.core.data.MailMessage;

public class SendMailActivity implements IActivity {

	public SendMailActivity() {

	}

	@Override
	public <T extends ProcessedData> boolean initialize(T t) {
		boolean result = false;
		try {
			processedData = t;
			class_ = t.getClass();
			result = true;
		}
		catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}

	//TODO: for testing
	public void setMessage(MailMessage message) {
		this.message = message;
	}

	public MailMessage getMessage() {
		return message;
	}

	@Override
	public boolean execute() {
		boolean result = false;
		try {
			Object prop=null;
			if(!processedData.getType().equals("Task")){					
				prop = PropertyUtils.getProperty(processedData, "registrationNumber");				
			}else if(processedData.getType().equals("Task")){				
				prop = PropertyUtils.getProperty(processedData, "taskNumber");
			}
			
			String docNumber = (prop == null ? "" : (String) prop);
					
			Session session = (Session) new InitialContext().lookup(Settings.getMessagingService());
			MimeMessage mimeMessage = new MimeMessage(session);
			mimeMessage.setSubject(message.getSubject());

			mimeMessage.setFrom();

			StringBuilder sb = new StringBuilder();
			for (String address : message.getSendTo()) {				
				sb.append(address).append(" ");
			}
			if (!sb.toString().trim().equals(""))
				mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(sb.toString().trim(), false));
			sb = new StringBuilder();
			for (String address : message.getCopyTo()) {
				sb.append(address).append(" ");
			}
			if (!sb.toString().trim().equals(""))
				mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(sb.toString().trim(), false));
			sb = new StringBuilder();
			for (String address : message.getBlindCopyTo()) {
				sb.append(address).append(" ");
			}
			if (!sb.toString().trim().equals(""))
				mimeMessage.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(sb.toString().trim(), false));

			String subject=message.getSubject();
			if(subject.indexOf("@")>-1){
				if(subject.indexOf("@DocumentNumber")>-1){		
					subject=subject.replace("@DocumentNumber", docNumber);			
				}
				message.setSubject(subject);	
			}
			
			mimeMessage.setSubject(message.getSubject(), "UTF-8");
			mimeMessage.setSentDate(new Date());

			MimeBodyPart mbp = new MimeBodyPart();			
			if (message.getContentType().equals("text/html")) {				
				mbp.setHeader("Content-Type","text/html; charset=\"utf-8\"");
				mbp.setContent(message.getBody(), "text/html; charset=utf-8");
				mbp.setHeader("Content-Transfer-Encoding", "quoted-printable");
			}
			else {
				mbp.setText(message.getBody());
			}
			System.out.println(mbp.getContentType());
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp);
			mimeMessage.setContent(mp);

			Transport.send(mimeMessage);				
			result = true;
		}
		catch (NamingException e) {
			result = false;
			resultMessage=e.getMessage();
			e.printStackTrace();
		}
		catch (MessagingException e) {
			result = false;
			resultMessage=e.getMessage();
			e.printStackTrace();		
		} catch (IllegalAccessException e) {
			result = false;
			resultMessage=e.getMessage();			
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			result = false;
			resultMessage=e.getMessage();			
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			result = false;
			resultMessage=e.getMessage();	
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public boolean dispose() {
		return true;
	}

	public String getResult() {
		return resultMessage;
	}


	Class<? extends ProcessedData> class_;
	private ProcessedData processedData;
	private MailMessage message;

	private String resultMessage;
}