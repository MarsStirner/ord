package ru.efive.uifaces.beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class AuthenticationBean {

    /**
     * Indicates that user is logged in.
     */
    private boolean loggedIn = false;

    /**
     * Returns <code>true</code> if user is logged in, otherwise returns
     * <false>.
     * 
     * @return <code>true</code> if user is logged in, otherwise returns
     *         <false>.
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Makes user logged in.
     */
    public void logIn() {
        loggedIn = true;
    }

    /**
     * Makes user logged out.
     */
    public void logOut() {
        loggedIn = false;
    }
}
