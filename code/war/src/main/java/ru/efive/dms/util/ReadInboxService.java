package ru.efive.dms.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.mail.search.FlagTerm;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.dao.ScanCopyDocumentDAOImpl;
import ru.efive.dms.data.Attachment;
import ru.efive.dms.data.HistoryEntry;
import ru.efive.dms.data.ScanCopyDocument;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;

public class ReadInboxService implements Serializable {
    private static final long serialVersionUID = 4355914198294641591L;

    private SessionManagementBean sessionManagement;
    private FileManagementBean filesManagement;

    public void run() throws MessagingException, IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        filesManagement = (FileManagementBean) context.getApplication().evaluateExpressionGet(context, "#{fileManagement}", FileManagementBean.class);

        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("mail.properties"));

        Session session = Session.getDefaultInstance(properties);
        //session.setDebug(true);
        Store store = session.getStore();
        store.connect(properties.getProperty("mail.imap.host").toString(), properties.getProperty("mail.imap.user").toString(), properties.getProperty("mail.imap.password").toString());

        Folder inbox = store.getFolder("Inbox");

        inbox.open(Folder.READ_WRITE);
        //Message messages[] = inbox.search(ft);

        //inbox.open(Folder.READ_ONLY);
        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        Message messages[] = inbox.search(unseenFlagTerm);

        for (Message message : messages) {

            Address[] froms = message.getFrom();
            String email = froms == null ? null : ((InternetAddress) froms[0]).getAddress();
            User user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).getByEmailName(email);

            ScanCopyDocument doc = new ScanCopyDocument();
            doc.setDocumentStatus(DocumentStatus.NEW);
            Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
            doc.setCreationDate(created);
            if (user != null) {
                doc.setAuthor(user);
            } else {
                doc.setAuthor(sessionManagement.getLoggedUser());
            }

            doc.setShortDescription(message.getSubject());

            HistoryEntry historyEntry = new HistoryEntry();
            historyEntry.setCreated(created);
            historyEntry.setStartDate(created);
            historyEntry.setOwner(sessionManagement.getLoggedUser());
            historyEntry.setDocType(doc.getType());
            historyEntry.setParentId(doc.getId());
            historyEntry.setActionId(0);
            historyEntry.setFromStatusId(1);
            historyEntry.setEndDate(created);
            historyEntry.setProcessed(true);
            historyEntry.setCommentary("");
            Set<HistoryEntry> history = new HashSet<HistoryEntry>();
            history.add(historyEntry);
            doc.setHistory(history);

            List<File> attachments = new ArrayList<File>();
            Object content = message.getContent();
            if (content instanceof Multipart) {
                Multipart multipart = (Multipart) message.getContent();
                // System.out.println(multipart.getCount());

                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        continue; // dealing with attachments only
                    }
                    InputStream is = bodyPart.getInputStream();

                    // getFilename always have wrong characters set
                    byte[] fileBytes = bodyPart.getFileName().toString().getBytes();
                    String filename = MimeUtility.decodeText(new String(fileBytes, "UTF-8"));
                    //System.out.println(filename);

                    try {
                        doc = sessionManagement.getDAO(ScanCopyDocumentDAOImpl.class, ApplicationHelper.SCAN_DAO).save(doc);
                        saveDocumentFile(doc, filename, is);
                    } catch (Exception ex) {

                    }

                }
            }

        }

        inbox.close(false);
        store.close();
    }

    public void saveLocalFile(String fileName, InputStream input) throws IOException {
        File file = new File(fileName);
        for (int i = 0; file.exists(); i++) {
            file = new File(fileName + i);
        }
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        BufferedInputStream bis = new BufferedInputStream(input);
        int aByte;
        while ((aByte = bis.read()) != -1) {
            bos.write(aByte);
        }
        bos.flush();
        bos.close();
        bis.close();

    }


    public void saveDocumentFile(ScanCopyDocument document, String fileName, InputStream input) throws IOException {
        Attachment attachment = new Attachment();
        attachment.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
        attachment.setFileName(fileName);
        attachment.setAuthorId(sessionManagement.getLoggedUser().getId());
        attachment.setParentId(document.getUniqueId());

        byte[] byteArray;

        List<Byte> bytesList = new ArrayList<Byte>();
        BufferedInputStream bis = new BufferedInputStream(input);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int aByte;
        while ((aByte = bis.read()) != -1) {
            bos.write(aByte);
        }
        bos.flush();
        bos.close();
        bis.close();

        byteArray = bos.toByteArray();
        filesManagement.createFile(attachment, byteArray);

    }

    public static String convertEncoding(byte[] bytes, String from, String to) {
        String in_result = "";
        try {
            in_result = new String(new String(bytes, from).getBytes(to));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return in_result;
    }

}
