package ru.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ApplicationHelper {


    private static final Locale locale = new Locale("ru", "RU");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", locale);

    private static final Pattern idPattern = Pattern.compile(".*_(\\d+)");

    public static Locale getLocale() {
        return locale;
    }

    public static SimpleDateFormat getDateFormat() {
        return DATE_FORMAT;
    }

    public static String formatDate(LocalDateTime date) {
        if (date == null) {
            return "NULL";
        }
        return DATE_FORMAT.format(date);
    }

    public static Integer getIdFromUniqueIdString(String uniqueId) {
        if (StringUtils.isEmpty(uniqueId)) {
            return null;
        }
        final Matcher matcher = idPattern.matcher(uniqueId);
        if (matcher.find()) {
            final String subResult = matcher.group(1);
            try {
                return Integer.valueOf(subResult);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
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
}