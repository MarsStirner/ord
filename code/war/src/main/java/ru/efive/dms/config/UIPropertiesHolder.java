package ru.efive.dms.config;

import com.typesafe.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.annotation.PostConstruct;
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

    @Autowired
    @Qualifier("config")
    private Config config;

    //Used config
    private Config uiConfig;

    public UIPropertiesHolder() {
    }

    @PostConstruct
    public void init(){
        this.uiConfig = config.getConfig("ui");
    }

    @Override
    public String get(Object key) {
        return uiConfig.hasPath(key.toString()) ? uiConfig.getString(key.toString()) : "TODO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
    }
}
