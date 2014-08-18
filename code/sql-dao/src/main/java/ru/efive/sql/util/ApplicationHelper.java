package ru.efive.sql.util;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ApplicationHelper {

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
}
