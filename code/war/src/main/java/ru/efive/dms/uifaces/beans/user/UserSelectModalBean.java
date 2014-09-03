package ru.efive.dms.uifaces.beans.user;

import javax.faces.context.FacesContext;

import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

public class UserSelectModalBean extends ModalWindowHolderBean {

    private static final long serialVersionUID = -5464081842966601485L;

    public UserListHolderBean getUserList() {
        if (userList == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            userList = context.getApplication().evaluateExpressionGet(context, "#{userList}", UserListHolderBean.class);
        }
        return userList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void select(User user) {
        this.user = user;
    }

    public boolean selected(User user) {
        return this.user == null ? false : this.user.equals(user);
    }

    @Override
    protected void doSave() {
        super.doSave();
    }

    @Override
    protected void doShow() {
        super.doShow();
    }

    @Override
    protected void doHide() {
        super.doHide();
    }


    private UserListHolderBean userList;

    private User user;


}