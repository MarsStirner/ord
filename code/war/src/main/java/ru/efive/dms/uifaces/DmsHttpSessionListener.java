package ru.efive.dms.uifaces;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

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
                logger.error("Invalidate session issue: " + e.getMessage());
            }
            SessionManagementBean sessionManagement = (SessionManagementBean)
                    context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
            if (sessionManagement != null && sessionManagement.isLoggedIn()) {
                sessionManagement.logOut();


            }
        }
    }


    private final static Logger logger = Logger.getLogger(DmsHttpSessionListener.class);
}