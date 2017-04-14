package ru.efive.uifaces.bean;

import java.io.Serializable;

/**
 * State Holder for Dynamic Modal Window component.
 *
 * @author Denis Kotegov
 */
public class ModalWindowHolderBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean modalVisible;

    public boolean isModalVisible() {
        return modalVisible;
    }

    public void setModalVisible(boolean modalVisible) {
        this.modalVisible = modalVisible;
    }

    public void show() {
        doShow();
        modalVisible = true;
    }

    public void hide() {
        doHide();
        modalVisible = false;
    }

    public void save() {
        doSave();
        doHide();
        modalVisible = false;
    }

    public void apply() {
        doSave();
    }

    protected void doSave() {

    }

    protected void doShow() {

    }

    protected void doHide() {

    }

}
