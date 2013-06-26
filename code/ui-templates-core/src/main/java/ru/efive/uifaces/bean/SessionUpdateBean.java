package ru.efive.uifaces.bean;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;

/**
 * <code>SessionUpdateBean</code> class counts session update requests. The
 * class is used to keep user session active.
 * 
 * @author Ramil_Habirov
 */
@Named("e5ui_sessionUpdateBean")
@ConversationScoped
public class SessionUpdateBean implements Serializable {

    /**
     * Class version identifier used during serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Session update request count.
     */
    private int updateCount = 0;

    /**
     * Increments and returns session update request count.
     * 
     * @return session update request count.
     */
    public int getUpdateCount() {
        System.out.println("Returning update count: " + updateCount + "...");
        return ++updateCount;
    }
}
