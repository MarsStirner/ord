package ru.efive.dms.uifaces;


import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.efive.dms.uifaces.beans.SessionManagementBean;


public class DmsHttpSessionListener implements HttpSessionListener {

    public void sessionCreated(HttpSessionEvent arg0) {

    }

    public void sessionDestroyed(HttpSessionEvent arg0) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null && context.getApplication() != null) {
            try {
                ((HttpSession) context.getExternalContext().getSession(false)).invalidate();
            } catch (Exception e) {
                LOGGER.error("Invalidate session issue: ", e);
            }
            SessionManagementBean sessionManagement =
                    context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
            if (sessionManagement != null && sessionManagement.isLoggedIn()) {
                sessionManagement.logOut();


            }
        }
    }


    private final static Logger LOGGER = LoggerFactory.getLogger("SESSION_LISTENER");
}