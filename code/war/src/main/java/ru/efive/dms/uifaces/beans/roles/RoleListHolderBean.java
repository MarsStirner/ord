package ru.efive.dms.uifaces.beans.roles;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForRole;
import ru.efive.sql.dao.user.RoleDAOHibernate;
import ru.entity.model.user.Role;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.ROLE_DAO;

@Named("roleList")
@ViewScoped
public class RoleListHolderBean extends AbstractDocumentLazyDataModelBean<Role> {
    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();

    private RoleDAOHibernate dao;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO);
        setLazyModel(new LazyDataModelForRole(dao));

    }

    public List<Role> getAvailableRoles() {
        List<Role> result = new ArrayList<Role>();
        try {
            result = dao.findRoles(-1, -1, "name", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}