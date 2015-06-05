package ru.efive.dms.uifaces.beans;

import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.PaperCopyDocument;
import ru.hitsl.sql.dao.PaperCopyDocumentDAOImpl;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.PAPER_COPY_DOCUMENT_FORM_DAO;

@Named("paperCopyDocumentsByOfficeVolume")
@ConversationScoped
public class PaperCopyDocumentsByOfficeVolume extends AbstractDocumentListHolderBean<PaperCopyDocument> {

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected int getTotalCount() {
        return new Long(sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).countAllDocuments(filters, filter, false, false)).intValue();
    }

    @Override
    protected List<PaperCopyDocument> loadDocuments() {
        return new ArrayList<PaperCopyDocument>(new HashSet<PaperCopyDocument>(sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocuments(filters, filter, false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc())));
    }

    public List<PaperCopyDocument> getDocumentsById(int value) {
        if (filters.size() == 0) {
            filters.put("officeKeepingVolume", value);
        }
        return super.getDocuments();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    private void initFilters() {

    }

    private String filter;
    private Map<String, Object> filters = new HashMap<String, Object>();

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();

    private static final long serialVersionUID = 1426067769816981240L;
}