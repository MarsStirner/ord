package ru.efive.dms.uifaces.beans.roles;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import ru.entity.model.user.Role;
import ru.efive.uifaces.bean.ModalWindowHolderBean;

public class RoleListSelectModalBean extends ModalWindowHolderBean {

    public RoleListHolderBean getRoleList() {
        if (roleList == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            roleList = context.getApplication().evaluateExpressionGet(context, "#{roleList}", RoleListHolderBean.class);
        }
        return roleList;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        if (roles == null) {
            this.roles = new ArrayList<>();
        } else {
            this.roles = roles;
        }
    }

    public void select(Role role) {
        roles.add(role);
    }

    public void unselect(Role role) {
        roles.remove(role);
    }

    public boolean selected(Role role) {
        return roles.contains(role);
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


    private RoleListHolderBean roleList;

    private List<Role> roles = new ArrayList<>();
    //private List<role> roles;

    private static final long serialVersionUID = -9107594037615723746L;
}