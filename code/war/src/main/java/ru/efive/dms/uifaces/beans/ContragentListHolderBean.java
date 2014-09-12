package ru.efive.dms.uifaces.beans;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.crm.dao.ContragentDAOHibernate;
import ru.efive.crm.data.Contragent;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("contragentList")
@SessionScoped
public class ContragentListHolderBean extends AbstractDocumentListHolderBean<Contragent> {

    @Override
    public Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    public Sorting initSorting() {
        return new Sorting("fullName", true);
    }

    @Override
    protected int getTotalCount() {
        try {
           return new Long(sessionManagement.getDAO(ContragentDAOHibernate.class, ApplicationHelper.CONTRAGENT_DAO).countDocument(filter, false)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected List<Contragent> loadDocuments() {
        try {
          return sessionManagement.getDAO(ContragentDAOHibernate.class, ApplicationHelper.CONTRAGENT_DAO).findDocuments(filter,
                    false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<Contragent>();
    }


    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }


    private String filter;

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();


    private static final long serialVersionUID = 1023130636261147049L;
}