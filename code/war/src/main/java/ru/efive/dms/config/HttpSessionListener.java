package ru.efive.dms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSessionEvent;

/**
 * Created by EUpatov on 05.04.2017.
 */
public class HttpSessionListener implements javax.servlet.http.HttpSessionListener {
    private static final Logger log = LoggerFactory.getLogger(HttpSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        log.info("NEW SESSION [{}]", se.getSession().getId());
        se.getSession().setMaxInactiveInterval(45 * 60);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        log.info("END SESSION [{}]", se.getSession().getId());
    }
}
