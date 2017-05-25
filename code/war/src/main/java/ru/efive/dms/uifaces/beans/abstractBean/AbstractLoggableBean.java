package ru.efive.dms.uifaces.beans.abstractBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;

public class AbstractLoggableBean implements Serializable, BeanNameAware {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private String beanName;

    String getBeanName() {
        return beanName;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

}
