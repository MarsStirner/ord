package ru.efive.sql.util;

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
}
