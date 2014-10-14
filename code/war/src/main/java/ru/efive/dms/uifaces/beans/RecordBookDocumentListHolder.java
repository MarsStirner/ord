package ru.efive.dms.uifaces.beans;

import ru.efive.dms.dao.RecordBookDocumentDAOImpl;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.entity.model.document.RecordBookDocument;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.RECORD_BOOK_DAO;

@Named("record_book_documents")
@SessionScoped
public class RecordBookDocumentListHolder extends AbstractDocumentListHolderBean<RecordBookDocument> {

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("creationDate,id", false);
    }

    @Override
    protected int getTotalCount() {
        int result = 0;
        try {
            return new Long(sessionManagement.getDAO(RecordBookDocumentDAOImpl.class, RECORD_BOOK_DAO).countDocumentByAuthor(filter, sessionManagement.getLoggedUser().getId(), false)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected List<RecordBookDocument> loadDocuments() {
        List<RecordBookDocument> result = new ArrayList<RecordBookDocument>();
        try {
            result = sessionManagement.getDAO(RecordBookDocumentDAOImpl.class, RECORD_BOOK_DAO).findDocumentsByAuthor(filter,
                    sessionManagement.getLoggedUser().getId(), false, getPagination().getOffset(), getPagination().getPageSize(),
                    getSorting().getColumnId(), getSorting().isAsc());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<RecordBookDocument> getDocuments() {
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