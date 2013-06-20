package ru.efive.dms.uifaces.beans.user;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import ru.efive.sql.entity.user.User;
import ru.efive.dms.uifaces.beans.EmployesBean;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

public class UserListSelectModalBean extends ModalWindowHolderBean {

    public EmployesBean getUserList() {
        if (userList == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            userList = (EmployesBean) context.getApplication().evaluateExpressionGet(context, "#{employes}", EmployesBean.class);
        }
        return userList;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        if (users == null) {
            this.users = new ArrayList<User>();
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


    private EmployesBean userList;

    private List<User> users = new ArrayList<User>();
    //private List<User> users;

    private static final long serialVersionUID = -9107594037615723746L;
}