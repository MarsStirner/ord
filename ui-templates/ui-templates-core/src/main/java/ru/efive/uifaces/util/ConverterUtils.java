package ru.efive.uifaces.util;

/**
 *
 * @author Denis Kotegov
 */
public final class ConverterUtils {

    private ConverterUtils() {

    }

    /**
     *
     * @param object
     * @param defaultValue
     * @return
     * @throws NumberFormatException when object is a not a {@link Number} and {@link Strign} representation
     * contains invalid integer inside.
     */
    public static Integer objectAsInteger(Object object) {
        return objectAsInteger(object, null);
    }

    public static String objectAsString(Object object) {
        return objectAsString(object, null);
    }

    public static Boolean objectAsBoolean(Object object) {
        return objectAsBoolean(object, null);
    }

    /**
     *
     * @param object
     * @param defaultValue 
     * @return
     * @throws NumberFormatException when object is a not a {@link Number} and {@link Strign} representation
     * contains invalid integer inside.
     */
    public static Integer objectAsInteger(Object object, Integer defaultValue) {
        Integer result;
        
        if (object == null) {
            result = defaultValue;
        } else if (object.getClass() == Integer.class) {
            result = (Integer) object;
        } else if (object instanceof Number) {
            result = Integer.valueOf(((Number) object).intValue());
        } else {
            result = Integer.valueOf(objectAsString(object));
        }

        return result;
    }

    public static String objectAsString(Object object, String defaultValue) {
        String result;

        if (object == null) {
            result = defaultValue;
        } else if (object.getClass() == String.class) {
            result = (String) object;
        } else {
            result = object.toString();
        }

        return result;
    }

    public static Boolean objectAsBoolean(Object object, Boolean defaultValue) {
        Boolean result;

        if (object == null) {
            result =  defaultValue;
        } else if (object.getClass() == Boolean.class) {
            result = (Boolean) object;
        } else {
            result = Boolean.valueOf(objectAsString(object));
        }

        return result;
    }

}
