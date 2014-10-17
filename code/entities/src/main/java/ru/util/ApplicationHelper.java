package ru.util;


import ru.entity.model.crm.Contragent;
import ru.entity.model.document.DocumentForm;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.user.User;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class ApplicationHelper {


    private static final Locale locale = new Locale("ru", "RU");

    public static Locale getLocale() {
        return locale;
    }


    public static String STORE_NAME = "E5 DMS";
    public static String NAMESPACE = "http://www.efive.ru/model/dictionary/1.0";
    public static String NAMESPACE_PREFIX = "e5-dms";
    public static String TYPE_FILE = "File";

    public static boolean nonEmptyString(String str) {
        return !isEmptyString(str);
    }

    public static boolean isEmptyString(String str) {
        return ((str == null) || (str.trim().length() == 0));
    }

    public static String[] splitStr(String str, String delimeter) {
        String[] res = str.split(delimeter);
        for (int i = 0; i < res.length; i++) {
            res[i] = res[i].trim();
        }
        return res;
    }

    public static String[] splitStr(String str) {
        if (nonEmptyString(str)) {
            return splitStr(str, ",");
        } else {
            return new String[0];
        }
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

    public static String getFullTimeFormat() {
        return "dd.MM.yyyy HH:mm:ss z";
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