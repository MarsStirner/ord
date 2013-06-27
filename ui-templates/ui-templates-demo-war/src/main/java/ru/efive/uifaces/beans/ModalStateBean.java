package ru.efive.uifaces.beans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * <code>ModalStateBean</code> class holds modal window state.
 * 
 * @author Ramil_Habirov
 */
@ManagedBean
@SessionScoped
public class ModalStateBean {

    /**
     * Modal window render state. <code>true</code> if modal window is rendered
     * and <code>false</code> otherwise.
     */
    private Boolean rendered = false;

    /**
     * Sets modal window render state.
     * 
     * @param rendered
     *            render state.
     */
    public void setRendered(Boolean rendered) {
        System.out.println("setRendered: " + rendered);
        this.rendered = rendered;
    }

    /**
     * Returns modal window render state.
     * 
     * @return render state.
     */
    public Boolean getRendered() {
        System.out.println("getRendered: " + rendered);
        return rendered;
    }

    /**
     * Shows modal window.
     */
    public void show() {
        System.out.println("show");
        rendered = true;
    }
}
