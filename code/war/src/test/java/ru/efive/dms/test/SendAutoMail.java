package ru.efive.dms.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.wf.core.MailSettings;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

@Named("sendAutoMail")
@ApplicationScoped
public class SendAutoMail {

    @Autowired
    @Qualifier("mailSettings")
    private MailSettings mailSettings;


    public void sendEmail(String company, String licence, String remdate, String expdate, String attachments, String supportcontract, String signedcontract, String licenceorigin) throws Exception {

        //Properties
        Properties props = new Properties();
        props.put("mail.smtp.host", mailSettings.getSmtpHost());

        //Session
        Session mailSession = Session.getDefaultInstance(props);
        mailSession.setDebug(true);

        //Text
        MimeBodyPart text = new MimeBodyPart();
        text.setText("Company: " + company + "\n Licence: " + licence + "\n Expiration Date: " + expdate + "\n Support Contract: " + supportcontract +
                "\n Signed Contract: " + signedcontract + "\n Licence Origin: " + licenceorigin);

        //Create Multipart & Add Parts
        Multipart content = new MimeMultipart();
        content.addBodyPart(text);

        //Compile Message
        MimeMessage message = new MimeMessage(mailSession);
        message.setSubject("Testing Email");
        message.setContent(content);
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailSettings.getSendTo()));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailSettings.getSendTo()));

        //Send Message
        Transport transport = mailSession.getTransport();
        transport.connect(mailSettings.getSmtpHost(), mailSettings.getSmtpUser(), mailSettings.getSmptPassword());
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
        transport.close();
    }
}  