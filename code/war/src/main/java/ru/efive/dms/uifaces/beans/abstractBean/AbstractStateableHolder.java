package ru.efive.dms.uifaces.beans.abstractBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

import java.io.Serializable;

public abstract class AbstractStateableHolder implements Serializable, BeanNameAware {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Текущее состояние бина-обработчика
     */
    private State state;

    private String beanName;

    public boolean isCreateState() {
        return State.CREATE.equals(state);
    }

    public boolean isEditState() {
        return State.EDIT.equals(state);
    }

    public boolean isViewState() {
        return State.VIEW.equals(state);
    }

    public boolean isErrorState() {
        return State.ERROR.equals(state);
    }

    public State getState() {
        return state;
    }

    String getBeanName(){
        return beanName;
    }

    protected void setState(final State state) {
        log.info("Document state changed from {} to {}", this.state, state);
        this.state = state;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
