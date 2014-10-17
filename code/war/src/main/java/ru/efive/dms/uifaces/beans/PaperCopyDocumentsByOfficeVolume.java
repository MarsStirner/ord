package ru.efive.dms.uifaces.beans;

import ru.efive.dms.dao.PaperCopyDocumentDAOImpl;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.PaperCopyDocument;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.efive.dms.util.ApplicationDAONames.PAPER_COPY_DOCUMENT_FORM_DAO;

@Named("paperCopyDocumentsByOfficeVolume")
@ConversationScoped
public class PaperCopyDocumentsByOfficeVolume extends AbstractDocumentListHolderBean<PaperCopyDocument> {

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected int getTotalCount() {
        int in_result;
        in_result = new Long(sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).countAllDocuments(filters, filter, false, false)).intValue();
        return in_result;
    }

    @Override
    protected List<PaperCopyDocument> loadDocuments() {
        List<PaperCopyDocument> result = new ArrayList<PaperCopyDocument>(new HashSet<PaperCopyDocument>(sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocuments(filters, filter, false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc())));
        return result;
    }

    public List<PaperCopyDocument> getDocuments(int value) {
        if (filters.size() == 0) {
            filters.put("officeKeepingVolume", Integer.valueOf(value));
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