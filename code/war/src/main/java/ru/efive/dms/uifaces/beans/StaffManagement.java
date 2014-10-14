package ru.efive.dms.uifaces.beans;

import ru.efive.sql.dao.user.GroupDAOHibernate;
import ru.entity.model.user.Group;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.efive.dms.util.ApplicationDAONames.GROUP_DAO;

@Named("staffManagement")
@RequestScoped
public class StaffManagement {

    /*	public List<Group> getAllGroupsList(){
            List<Group> in_result=new ArrayList<Group>();
            try{
                in_result=sessionManagement.getDAO(GroupDAOHibernate.class, "groupDao").getAllDocuments();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return in_result;
        }
    */
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