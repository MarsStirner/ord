package ru.efive.dms.uifaces.beans;

import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.GroupDAOHibernate;
import ru.efive.sql.entity.user.Group;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Named("groups")
@SessionScoped
public class GroupsHolderBean extends AbstractDocumentListHolderBean<Group> {

    private static final long serialVersionUID = -3555790276506205190L;

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected int getTotalCount() {
        int result = new Long(sessionManagement.getDAO(GroupDAOHibernate.class, ApplicationHelper.GROUP_DAO).countAllDocuments(filter, false)).intValue();
        return result;
    }

    @Override
    protected List<Group> loadDocuments() {
        List<Group> in_results = sessionManagement.getDAO(GroupDAOHibernate.class, ApplicationHelper.GROUP_DAO).findAllDocuments(
                filter, false);

        Collections.sort(in_results, new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                int result = 0;
                String colId = ApplicationHelper.getNotNull(getSorting().getColumnId());

                if(colId.equalsIgnoreCase("description")) {
                    result = ApplicationHelper.getNotNull(o1.getDescription()).compareTo(ApplicationHelper.getNotNull(o2.getDescription()));
                } else if(colId.equalsIgnoreCase("alias")) {
                    result = ApplicationHelper.getNotNull(o1.getAlias()).compareTo(ApplicationHelper.getNotNull(o2.getAlias()));
                }

                if(getSorting().isAsc()) {
                    result *= -1;
                }

                return result;
            }
        });

        return in_results;
    }

    @Override
    public List<Group> getDocuments() {
        return super.getDocuments();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    private String filter = "";

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
}