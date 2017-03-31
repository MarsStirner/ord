package ru.efive.dms.config;

import com.github.javaplugs.jsf.ViewScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class InitViewScope extends CustomScopeConfigurer {
    private static final Logger log = LoggerFactory.getLogger("CONFIG");

    public InitViewScope() {
        log.info("Init ViewScope");
        Map<String, Object> map = new HashMap<>();
        map.put("view", new ViewScope());
        super.setScopes(map);
    }
}