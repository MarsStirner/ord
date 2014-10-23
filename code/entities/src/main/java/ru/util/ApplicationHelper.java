package ru.util;
import ru.entity.model.crm.Contragent;
import ru.entity.model.document.DocumentForm;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.user.User;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class ApplicationHelper {
    public static String STORE_NAME = "E5 DMS";
    public static String NAMESPACE = "http://www.efive.ru/model/dictionary/1.0";
    public static String NAMESPACE_PREFIX = "e5-dms";
    public static String TYPE_FILE = "File";

    private static final Locale locale = new Locale("ru", "RU");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", locale);

    public static Locale getLocale() {
        return locale;
    }

    public static SimpleDateFormat getDateFormat() {
        return DATE_FORMAT;
    }

    public static String formatDate(Date date) {
        if(date == null) {
            return "NULL";
        }
        return DATE_FORMAT.format(date);
    }

    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public static String getNotNull(String param) {
        if (param == null) {
            param = "";
        }
        return param;
    }

    public static Date getNotNull(Date param) {
        if (param == null) {
            Calendar calendar = Calendar.getInstance(ru.util.ApplicationHelper.getLocale());
            calendar.set(0, 0, 0);
            param = calendar.getTime();
        }
        return param;
    }

    public static DocumentStatus getNotNull(DocumentStatus param) {
        if (param == null) {
            param = DocumentStatus.NEW;
        }
        return param;
    }

    public static DocumentForm getNotNull(DocumentForm param) {
        if (param == null) {
            param = new DocumentForm();
        }
        return param;
    }

    public static User getNotNull(User param) {
        if (param == null) {
            param = new User();
        }
        return param;
    }

    public static Contragent getNotNull(Contragent param) {
        if (param == null) {
            param = new Contragent();
        }
        return param;
    }


}