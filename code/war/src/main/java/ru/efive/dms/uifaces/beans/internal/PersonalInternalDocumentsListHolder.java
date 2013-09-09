package ru.efive.dms.uifaces.beans.internal;

import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.data.InternalDocument;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("personal_internal_documents")
@SessionScoped
public class PersonalInternalDocumentsListHolder extends AbstractDocumentListHolderBean<InternalDocument> {

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("registrationDate, registrationNumber", false);
    }

    @Override
    protected int getTotalCount() {
        return new Long(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).countDraftDocumentsByAuthor(
                sessionManagement.getLoggedUser(), false)).intValue();
    }

    @Override
    protected List<InternalDocument> loadDocuments() {
        return sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDraftDocumentsByAuthor(filter, sessionManagement.getLoggedUser(),
                false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
    }

    @Override
    public List<InternalDocument> getDocuments() {
        return super.getDocuments();
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