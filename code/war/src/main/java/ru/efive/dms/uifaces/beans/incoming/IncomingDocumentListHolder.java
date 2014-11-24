package ru.efive.dms.uifaces.beans.incoming;

import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.user.User;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.INCOMING_DOCUMENT_FORM_DAO;


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
        if (!sessionManagement.isSubstitution()) {
            return new Long(
                    sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                            .countAllDocumentsByUser(filter, sessionManagement.getLoggedUser(), false, false)
            ).intValue();
        }  else {
            final List<User> userList = new ArrayList<User>(sessionManagement.getSubstitutedUsers());
            userList.add(sessionManagement.getLoggedUser());
            return new Long(sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).countAllDocumentsByUserList(filter, userList, false, false)).intValue();
        }
    }

    @Override
    protected List<IncomingDocument> loadDocuments() {
        if (!sessionManagement.isSubstitution()) {
            return sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                    .findAllDocumentsByUser(filter, sessionManagement.getLoggedUser(), false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        } else {
            final List<User> userList = new ArrayList<User>(sessionManagement.getSubstitutedUsers());
            userList.add(sessionManagement.getLoggedUser());
            return sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                    .findAllDocumentsByUserList(filter, userList, false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        }
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