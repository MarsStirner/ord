package ru.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

@Converter(autoApply = true)
public class JsonObjectConverter implements AttributeConverter<Object, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(JsonObjectConverter.class);

    static {
        objectMapper.registerModule(new JSR353Module());
    }


    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if(attribute == null){
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException ex) {
            log.error("JPA JSON Converter: Fail convertToDatabaseColumn", ex);
            return null;
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        if(StringUtils.isEmpty(dbData)){
            return null;
        }
        try {
            return objectMapper.readValue(dbData, JsonObject.class);
        } catch (IOException ex) {
            log.error("JPA JSON Converter: Fail convertToEntityAttribute with value='{}'", dbData, ex);
            return null;
        }
    }
}
