package ru.efive.dms.uifaces.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.crm.dao.ContragentDAOHibernate;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.crm.Contragent;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.CONTRAGENT_DAO;

@ManagedBean(name = "contragentList")
@ViewScoped
public class ContragentListHolderBean extends AbstractDocumentListHolderBean<Contragent> {

    private static final Logger logger = LoggerFactory.getLogger("CONTRAGENT");

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
            return new Long(sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO)
                    .countDocument(filter, false)).intValue();
        } catch (Exception e) {
            logger.error("Exception on count documents", e);
        }
        return 0;
    }

    @Override
    protected List<Contragent> loadDocuments() {
        try {
            return sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO)
                    .findDocuments(filter, false, getPagination().getOffset(), getPagination().getPageSize(),
                            getSorting().getColumnId(), getSorting().isAsc());
        } catch (Exception e) {
            logger.error("Exception on loadDocuments [filter={}]", filter, e);
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