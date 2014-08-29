package ru.efive.dms.uifaces.beans.incoming;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("personal_in_documents")
@SessionScoped
public class PersonalIncomingDocumentListHolder extends AbstractDocumentListHolderBean<IncomingDocument> {

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("creationDate", false);
    }

    @Override
    protected int getTotalCount() {
        try {
            return new Long(sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).countDraftDocumentsByAuthor(
                    sessionManagement.getLoggedUser(), false)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    protected List<IncomingDocument> loadDocuments() {
        try {
           return sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).findDraftDocumentsByAuthor(filter, sessionManagement.getLoggedUser(),
                    false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<IncomingDocument>(0);
        }
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
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}