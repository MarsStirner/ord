package ru.efive.dms.uifaces.beans.user;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import ru.entity.model.user.Group;
import ru.entity.model.user.User;
import ru.efive.dms.uifaces.beans.GroupsHolderBean;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

public class UserUnitsSelectModalBean extends ModalWindowHolderBean {

    private static final long serialVersionUID = -8259481211747795752L;

    public UserListHolderBean getUserList() {
        if (userList == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            userList = (UserListHolderBean) context.getApplication().evaluateExpressionGet(context, "#{userList}", UserListHolderBean.class);
        }
        return userList;
    }

    public GroupsHolderBean getGroupsView() {
        if (groupsView == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            groupsView = (GroupsHolderBean) context.getApplication().evaluateExpressionGet(context, "#{groups}", GroupsHolderBean.class);
        }
        return groupsView;
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

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        if (groups == null) {
            this.groups = new ArrayList<Group>();
        } else {
            this.groups = groups;
        }
    }

    public void selectUser(User user) {
        users.add(user);
    }

    public void unselectUser(User user) {
        users.remove(user);
    }

    public boolean selectedUser(User user) {
        return users.contains(user);
    }

    public void selectGroup(Group group) {
        groups.add(group);
    }

    public void unselectGroup(Group group) {
        groups.remove(group);
    }

    public boolean selectedGroup(Group group) {
        return groups.contains(group);
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
    private List<User> users = new ArrayList<User>();

    private GroupsHolderBean groupsView;
    private List<Group> groups = new ArrayList<Group>();
}