package ru.efive.dms.uifaces.beans.user;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import ru.entity.model.user.User;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

public class UserListSelectModalBean extends ModalWindowHolderBean {

    public UserListHolderBean getUserList() {
        if (userList == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            userList = context.getApplication().evaluateExpressionGet(context, "#{userList}", UserListHolderBean.class);
        }
        return userList;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        if (users == null) {
            this.users = new ArrayList<>();
        } else {
            this.users = users;
        }
    }

    public void select(User user) {
        users.add(user);
    }

    public void unselect(User user) {
        users.remove(user);
    }

    public boolean selected(User user) {
        return users.contains(user);
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

    private List<User> users = new ArrayList<>();

    private static final long serialVersionUID = -9107594037615723746L;
}