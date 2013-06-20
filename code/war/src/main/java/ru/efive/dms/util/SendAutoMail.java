package ru.efive.dms.util;

import javax.mail.*;
import javax.mail.internet.*;

import java.util.Properties;
import javax.activation.FileDataSource;
import javax.activation.DataHandler;


public class SendAutoMail {
    private static final String hostName = "smtp.gmail.com";
    private static final int hostPort = 465;
    private static final String senderAddress = "emailAddress@gmail.com";
    private static final String password = "emailPassword";

    public void sendEmail(String company, String licence, String remdate, String expdate, String attachments, String supportcontract, String signedcontract, String licenceorigin) throws Exception {
        //Properties  
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", hostName);
        props.put("mail.smtps.auth", "true");

        //Session  
        Session mailSession = Session.getDefaultInstance(props);
        mailSession.setDebug(true);

        //Text  
        MimeBodyPart text = new MimeBodyPart();
        text.setText("Company: " + company + "\n Licence: " + licence + "\n Expiration Date: " + expdate + "\n Support Contract: " + supportcontract +
                "\n Signed Contract: " + signedcontract + "\n Licence Origin: " + licenceorigin);

        //Attachment  
        FileDataSource fds = new FileDataSource("c:\\somefile.txt");
        MimeBodyPart attachment = new MimeBodyPart();
        attachment.setDataHandler(new DataHandler(fds));
        attachment.setFileName(fds.getName());

        //Create Multipart & Add Parts  
        Multipart content = new MimeMultipart();
        content.addBodyPart(text);
        content.addBodyPart(attachment);

        //Compile Message  
        MimeMessage message = new MimeMessage(mailSession);
        message.setSubject("Testing Email");
        message.setContent(content);
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("maja88g@hotmail.com"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("maja88g@gmail.com"));

        //Send Message  
        Transport transport = mailSession.getTransport();
        transport.connect(hostName, hostPort, senderAddress, password);
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
        transport.close();
    }

    public static void main(String[] args) throws Exception {
        new SendAutoMail().sendEmail("a", "b", "c", "d", "e", "f", "g", "h");
    }
}  