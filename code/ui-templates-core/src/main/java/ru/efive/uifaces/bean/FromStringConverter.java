package ru.efive.uifaces.bean;

import java.io.Serializable;

/**
 *
 * @author Denis Kotegov
 */
public abstract class FromStringConverter<I extends Serializable> {

    public static final FromStringConverter<Integer> INTEGER_CONVERTER = new FromStringConverter<Integer>() {

        @Override
        public Integer getValueFromString(String string) {
            try {
                return Integer.valueOf(string);
            } catch (NumberFormatException ex) {
                return null;
            }
        }

    };

    public static final FromStringConverter<String> STRING_CONVERTER = new FromStringConverter<String>() {

        @Override
        public String getValueFromString(String string) {
            return string;
        }
    };

    public abstract I getValueFromString(String string);

}
