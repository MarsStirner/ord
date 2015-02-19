package ru.efive.dms.uifaces.beans;

import ru.efive.sql.dao.user.GroupDAOHibernate;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.user.Group;
import ru.util.ApplicationHelper;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.GROUP_DAO;

@ManagedBean(name="groups")
@ViewScoped
public class GroupsHolderBean extends AbstractDocumentListHolderBean<Group> {

    private static final long serialVersionUID = -3555790276506205190L;

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected int getTotalCount() {
        return new Long(sessionManagement.getDAO(GroupDAOHibernate.class, GROUP_DAO).countAllDocuments(filter, false)).intValue();
    }

    @Override
    protected List<Group> loadDocuments() {
        List<Group> in_results = sessionManagement.getDAO(GroupDAOHibernate.class, GROUP_DAO).findAllDocuments(
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