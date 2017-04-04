package ru.util;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Date;
import java.time.LocalDate;

/**
 * Author: Upatov Egor <br>
 * Date: 07.11.2016, 19:00 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */

@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, Date> {


    @Override
    public Date convertToDatabaseColumn(final LocalDate attribute) {
        return (attribute == null ? null : Date.valueOf(attribute));
    }

    @Override
    public LocalDate convertToEntityAttribute(final Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
