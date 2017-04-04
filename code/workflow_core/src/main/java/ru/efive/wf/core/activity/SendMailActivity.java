package ru.efive.wf.core.activity;


import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.efive.wf.core.IActivity;
import ru.efive.wf.core.MailSettings;
import ru.efive.wf.core.data.MailMessage;
import ru.entity.model.enums.DocumentType;
import ru.external.ProcessedData;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Properties;

@Component("sendMailActivity")
public class SendMailActivity implements IActivity {
    @Autowired
    public MailSettings mailSettings;
    Class<? extends ProcessedData> class_;
    private ProcessedData processedData;
    private MailMessage message;
    private String resultMessage;

    public SendMailActivity() {

    }

    public MailMessage getMessage() {
        return message;
    }

    public void setMessage(MailMessage message) {
        this.message = message;
    }

    @Override
    public boolean dispose() {
        return true;
    }

    public String getResult() {
        return resultMessage;
    }

    @Override
    public <T extends ProcessedData> boolean initialize(T t) {
        boolean result = false;
        try {
            processedData = t;
            class_ = t.getClass();
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean execute() {
        boolean result = false;
       try{
            MimeMessage mimeMessage = getMimeMessage();
            try {
                for (Address address : mimeMessage.getAllRecipients()) {
                    System.out.println("####DEBUG MESSAGE:\"" + mimeMessage.getSubject() + "\" TO: " + ((InternetAddress) address).getAddress());
                }
            } catch (Exception e) {
                //TODO
            }
            Transport.send(mimeMessage);
            result = true;
        } catch (Exception e) {
            resultMessage = e.getMessage();
            e.printStackTrace();
        }
        return result;
    }

    private MimeMessage getMimeMessage() throws MessagingException, NamingException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        MimeMessage mimeMessage = new MimeMessage(getSession());
        mimeMessage.setSubject(message.getSubject());
        String docNumber = getDocNumber();
        if (mailSettings.isTestServer())
            return getTestMimeMessage(mimeMessage, docNumber);
        return getRealMimeMessage(mimeMessage, docNumber);
    }

    private String getDocNumber() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object prop = null;
        if (!processedData.getDocumentType().equals(DocumentType.Task)) {
            prop = PropertyUtils.getProperty(processedData, "registrationNumber");
        } else if (processedData.getDocumentType().equals(DocumentType.Task)) {
            prop = PropertyUtils.getProperty(processedData, "taskNumber");
        }
        return (prop == null ? "" : (String) prop);
    }

    /**
     * Get session use jndi or smtp settings (settings in workflow-local.properties)
     *
     * @throws NamingException
     */
    private Session getSession() throws NamingException {
        if (mailSettings.isSmtpFlag()) {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", mailSettings.getSmtpHost());
            properties.put("mail.debug", "true");
            // properties.put("mail.pop3.socketFactory.class", "ru.efive.wf.core.util.DummySSLSocketFactory");
            return Session.getDefaultInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailSettings.getSmtpUser(), mailSettings.getSmptPassword());
                }
            });
        }
        final Session session = (Session) new InitialContext().lookup(mailSettings.getJndi());
        System.out.println(session.getProperties().toString());
        return session;
    }

    /**
     * Get test message when app runs in test server (settings in workflow-local.properties)
     *
     * @throws MessagingException
     */
    private MimeMessage getTestMimeMessage(MimeMessage mimeMessage, String docNumber) throws MessagingException {
        mimeMessage.setFrom(new InternetAddress(mailSettings.getSendFrom()));
        StringBuilder sb = new StringBuilder();
        sb.append(mailSettings.getSendTo()).append(" ");
        sb.append(mailSettings.getSendTo()).append(" ");
        mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(sb.toString().trim(), false));
        mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(sb.toString().trim(), false));
        mimeMessage.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(sb.toString().trim(), false));

        String subject = message.getSubject();
        if (subject.contains("@")) {
            if (subject.contains("@DocumentNumber")) {
                subject = subject.replace("@DocumentNumber", docNumber);
            }
            message.setSubject(subject);
        }

        mimeMessage.setSubject(message.getSubject(), "UTF-8");
        mimeMessage.setSentDate(new Date());
        mimeMessage.setContent(getMimeMultipart());
        return mimeMessage;
    }

    private MimeMessage getRealMimeMessage(MimeMessage mimeMessage, String docNumber) throws MessagingException {
        mimeMessage.setFrom();

        StringBuilder sb = new StringBuilder();
        for (String address : message.getSendTo()) {
            sb.append(address).append(",");
        }
        if (!sb.toString().trim().equals(""))
            mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(sb.toString().trim(), false));
        sb = new StringBuilder();
        for (String address : message.getCopyTo()) {
            sb.append(address).append(",");
        }
        if (!sb.toString().trim().equals(""))
            mimeMessage.addRecipients(Message.RecipientType.CC, InternetAddress.parse(sb.toString().trim(), false));
        sb = new StringBuilder();
        for (String address : message.getBlindCopyTo()) {
            sb.append(address).append(",");
        }
        if (!sb.toString().trim().equals(""))
            mimeMessage.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(sb.toString().trim(), false));

        String subject = message.getSubject();
        if (subject.contains("@")) {
            if (subject.contains("@DocumentNumber")) {
                subject = subject.replace("@DocumentNumber", docNumber);
            }
            message.setSubject(subject);
        }

        mimeMessage.setSubject(message.getSubject(), "UTF-8");
        mimeMessage.setSentDate(new Date());
        mimeMessage.setContent(getMimeMultipart());
        return mimeMessage;
    }

    private Multipart getMimeMultipart() throws MessagingException {
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        if (message.getContentType().equals("text/html")) {
            mimeBodyPart.setHeader("Content-Type", "text/html; charset=\"utf-8\"");
            mimeBodyPart.setContent(message.getBody(), "text/html; charset=utf-8");
            mimeBodyPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
        } else {
            mimeBodyPart.setText(message.getBody());
        }
        Multipart mimeMultipart = new MimeMultipart();
        mimeMultipart.addBodyPart(mimeBodyPart);
        return mimeMultipart;
    }
}