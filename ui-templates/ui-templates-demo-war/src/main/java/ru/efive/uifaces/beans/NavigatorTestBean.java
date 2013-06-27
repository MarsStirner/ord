package ru.efive.uifaces.beans;

import java.util.Date;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 *
 * @author Pavel Porubov
 */
@ManagedBean(name = "navigatorData")
@SessionScoped
public class NavigatorTestBean {

    private String lastAction = "none";

    public String getTimestamp() {
        return new Date().toString();
    }

    public String getLastAction() {
        return lastAction;
    }

    public String backAction() {
        lastAction = "back";
        return null;
    }

    public String forwardAction() {
        lastAction = "forward";
        return null;
    }
}
