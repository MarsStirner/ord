package ru.efive.dms.uifaces.beans;

import ru.entity.model.user.Group;
import ru.hitsl.sql.dao.user.GroupDAOHibernate;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.GROUP_DAO;

@Named("staffManagement")
@RequestScoped
public class StaffManagement {

    public Group findGroupByAlias(String name) {
        Group in_result = null;
        try {
            if (this.filter.isEmpty()) {
                in_result = sessionManagement.getDAO(GroupDAOHibernate.class, GROUP_DAO).findGroupByAlias(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return in_result;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    String filter = "";

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();
}