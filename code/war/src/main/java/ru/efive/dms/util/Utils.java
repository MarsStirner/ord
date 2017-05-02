package ru.efive.dms.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;


public class Utils {
    public static Map<String, Object> getMapFromConfig(String prefix, Config config) {
        final Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
            switch (entry.getValue().valueType()) {
                case OBJECT:
                    result.putAll(getMapFromConfig(entry.getKey(), config.getConfig(entry.getKey())));
                    break;
                case NULL:
                    result.put(StringUtils.isEmpty(prefix) ? entry.getKey() : prefix + "." + entry.getKey(), null);
                    break;
                default:
                    result.put(StringUtils.isEmpty(prefix) ? entry.getKey() : prefix + "." + entry.getKey(), entry.getValue().unwrapped());
                    break;
            }
        }
        return result;
    }
}
