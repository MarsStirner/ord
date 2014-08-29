package ru.efive.dms.uifaces.beans.incoming;

import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;


@Named("in_documents")
@SessionScoped
public class IncomingDocumentListHolder extends AbstractDocumentListHolderBean<IncomingDocument> {
    private String filter;

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 50);
        //Ранжирование по страницам по-умолчанию  (50 на страницу, начиная с нуля)
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("registrationDate", false);
        //Сортировка по-умолчанию (дата регистрации документа)
    }

    @Override
    protected int getTotalCount() {
        final User loggedUser = sessionManagement.getLoggedUser();
        return new Long(
                sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO)
                        .countAllDocumentsByUser(filter, loggedUser, false, false)
        ).intValue();
    }

    @Override
    protected List<IncomingDocument> loadDocuments() {
        final User loggedUser = sessionManagement.getLoggedUser();
        return sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO)
                .findAllDocumentsByUser(filter, loggedUser, false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());

    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }


    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;


}