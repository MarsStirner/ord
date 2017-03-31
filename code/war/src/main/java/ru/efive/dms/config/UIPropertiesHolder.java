package ru.efive.dms.config;

import com.typesafe.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.HashMap;

/**
 * Author: Upatov Egor <br>
 * Date: 21.03.2017, 16:06 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
@Named("ui")
@ApplicationScoped
public class UIPropertiesHolder extends HashMap<String, String> {

    private final Config config;

    @Autowired
    public UIPropertiesHolder(
            @Qualifier("config") Config config
    ) {
        this.config = config.getConfig("ui");
    }

    @Override
    public String get(Object key) {
        return config.getString((String) key);
    }
}
